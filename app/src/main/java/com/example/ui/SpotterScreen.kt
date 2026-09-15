package com.example.ui

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.LocationTelemetryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class SpotterCategory(val displayName: String, val icon: String) {
    ALL("Összes Spot", "🌐"),
    CASTLES("Várak & Kastélyok", "🏰"),
    LAKES("Tavak & Folyók", "🌊"),
    MOUNTAINS("Hegyek & Kilátók", "⛰️"),
    PARKS("Parkok & Szabadidő", "🌳"),
    URBAN("Város & Ipari", "🏙️")
}

data class SpotterLocation(
    val id: String,
    val name: String,
    val county: String,
    val category: SpotterCategory,
    val lat: Double,
    val lng: Double,
    val bestTime: String,
    val airspaceStatus: String, // e.g. "🟢 Szabad Légtér (120m)", "🟡 Nemzeti Park", "🔴 CTR Közelében"
    val airspaceColor: Color,
    val advice: String,
    val recommendedDrone: String,
    val isFavorite: Boolean = false,
    val userSubmitted: Boolean = false
)

data class FieldGpsDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val county: String,
    val lat: Double,
    val lng: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val quickNotes: String = "",
    val category: SpotterCategory = SpotterCategory.PARKS,
    val bestTime: String = "🌅 Naplemente"
)

private fun loadFieldDraftsFromStorage(context: Context): List<FieldGpsDraft> {
    val prefs = context.getSharedPreferences("spotter_field_drafts", Context.MODE_PRIVATE)
    val rawJson = prefs.getString("drafts_list", null) ?: return emptyList()
    val result = mutableListOf<FieldGpsDraft>()
    try {
        val arr = JSONArray(rawJson)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val catName = obj.optString("category", SpotterCategory.PARKS.name)
            val category = try { SpotterCategory.valueOf(catName) } catch (_: Exception) { SpotterCategory.PARKS }
            result.add(
                FieldGpsDraft(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    name = obj.optString("name", "Terepi Spot"),
                    county = obj.optString("county", ""),
                    lat = obj.optDouble("lat", 47.3755),
                    lng = obj.optDouble("lng", 18.9230),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    quickNotes = obj.optString("quickNotes", ""),
                    category = category,
                    bestTime = obj.optString("bestTime", "🌅 Naplemente")
                )
            )
        }
    } catch (_: Exception) {}
    return result
}

private fun saveFieldDraftsToStorage(context: Context, drafts: List<FieldGpsDraft>) {
    val prefs = context.getSharedPreferences("spotter_field_drafts", Context.MODE_PRIVATE)
    try {
        val arr = JSONArray()
        for (d in drafts) {
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("name", d.name)
            obj.put("county", d.county)
            obj.put("lat", d.lat)
            obj.put("lng", d.lng)
            obj.put("timestamp", d.timestamp)
            obj.put("quickNotes", d.quickNotes)
            obj.put("category", d.category.name)
            obj.put("bestTime", d.bestTime)
            arr.put(obj)
        }
        prefs.edit().putString("drafts_list", arr.toString()).apply()
    } catch (_: Exception) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotterScreen(viewModel: ChatViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Spotter Térkép, 1: Kinti Mentések & Piszkozatok, 2: Légtér Kisokos
    var selectedCategory by remember { mutableStateOf(SpotterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    
    var showAddSpotDialog by remember { mutableStateOf(false) }
    var showQuickGpsRecordDialog by remember { mutableStateOf(false) }
    var draftToPublish by remember { mutableStateOf<FieldGpsDraft?>(null) }
    var selectedSpotForDetails by remember { mutableStateOf<SpotterLocation?>(null) }

    val initialSpots by viewModel.spotterLocations.collectAsStateWithLifecycle()
    val fieldDrafts by viewModel.fieldDrafts.collectAsStateWithLifecycle()

    // Favorites set
    var favorites by remember { mutableStateOf(setOf<String>()) }

    // Filtered list
    val filteredSpots = remember(searchQuery, selectedCategory, initialSpots.size, favorites) {
        initialSpots.filter { spot ->
            val matchesCategory = selectedCategory == SpotterCategory.ALL || spot.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    spot.name.contains(searchQuery, ignoreCase = true) ||
                    spot.county.contains(searchQuery, ignoreCase = true) ||
                    spot.advice.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TOP HEADER: Hero Banner with Field GPS Quick-Save Button
        SpotterHeaderHero(
            spotCount = initialSpots.size,
            draftCount = fieldDrafts.size,
            onQuickGpsClick = { showQuickGpsRecordDialog = true }
        )

        // TAB SELECTOR: 3 Tabs (Térkép, Terepi Piszkozatok / Otthoni Közzététel, Légtér Kisokos)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF00F0FF),
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFE2E8F0))
                .padding(4.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selectedTab == 0)
                            if (isDarkMode) Color(0xFF1E293B) else Color.White
                        else Color.Transparent
                    ),
                text = {
                    Text("🗺️ TÉRKÉP", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selectedTab == 1)
                            if (isDarkMode) Color(0xFF1E293B) else Color.White
                        else Color.Transparent
                    ),
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📍 PISZKOZATOK", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        if (fieldDrafts.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${fieldDrafts.size}",
                                        color = Color(0xFF0A0F1D),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selectedTab == 2)
                            if (isDarkMode) Color(0xFF1E293B) else Color.White
                        else Color.Transparent
                    ),
                text = {
                    Text("🛑 LÉGTÉR", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            )
        }

        when (selectedTab) {
            0 -> {
                // SEARCH & CATEGORY FILTERS + SPOTS LIST
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Keresés várak, tavak, hegyek vagy megye szerint...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Keresés",
                                tint = Color(0xFF00F0FF)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Törlés")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spotter_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isDarkMode) Color(0xFF0F172A) else Color.White,
                            unfocusedContainerColor = if (isDarkMode) Color(0xFF0F172A) else Color.White
                        ),
                        singleLine = true
                    )

                    // Horizontal Category Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpotterCategory.values().forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = "${cat.icon} ${cat.displayName}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(0xFF00F0FF),
                                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    labelColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
                                    selectedBorderColor = Color(0xFF00F0FF)
                                )
                            )
                        }
                    }
                }

                // LIST OF SPOTS
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSpots, key = { it.id }) { spot ->
                        val isFav = favorites.contains(spot.id)
                        SpotterLocationCard(
                            spot = spot,
                            isFavorite = isFav,
                            isDarkMode = isDarkMode,
                            onFavoriteToggle = {
                                favorites = if (isFav) favorites - spot.id else favorites + spot.id
                            },
                            onOpenMap = {
                                launchGoogleMaps(context, spot.lat, spot.lng, spot.name, spot.county)
                            },
                            onCardClick = {
                                selectedSpotForDetails = spot
                            }
                        )
                    }
                }
            }
            1 -> {
                // FIELD DRAFTS & HOME PUBLISHING SECTION
                FieldDraftsSection(
                    drafts = fieldDrafts,
                    isDarkMode = isDarkMode,
                    onQuickRecordClick = { showQuickGpsRecordDialog = true },
                    onPublishDraft = { draft ->
                        draftToPublish = draft
                        showAddSpotDialog = true
                    },
                    onOpenMap = { draft ->
                        launchGoogleMaps(context, draft.lat, draft.lng, draft.name, draft.county)
                    },
                    onDeleteDraft = { draft ->
                        viewModel.deleteFieldDraft(draft.id)
                        Toast.makeText(context, "🗑️ Piszkozat törölve", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            2 -> {
                // NO-FLY & AIRSPACE KISOKOS
                AirspaceKisokosSection(isDarkMode = isDarkMode)
            }
        }
    }

    // Detail Dialog
    selectedSpotForDetails?.let { spot ->
        SpotDetailDialog(
            spot = spot,
            isDarkMode = isDarkMode,
            onDismiss = { selectedSpotForDetails = null },
            onOpenMap = { launchGoogleMaps(context, spot.lat, spot.lng, spot.name, spot.county) }
        )
    }

    // Quick GPS Field Recorder Dialog
    if (showQuickGpsRecordDialog) {
        QuickGpsRecordDialog(
            isDarkMode = isDarkMode,
            onDismiss = { showQuickGpsRecordDialog = false },
            onSaveDraft = { newDraft ->
                viewModel.addFieldDraft(newDraft)
                showQuickGpsRecordDialog = false
                Toast.makeText(context, "🎯 GPS elmentve a piszkozatok közé! Otthon kényelmesen közzéteheted.", Toast.LENGTH_LONG).show()
            },
            onDirectPublish = { newSpot ->
                viewModel.addSpotterLocation(newSpot)
                showQuickGpsRecordDialog = false
                Toast.makeText(context, "🚀 Helyszín közvetlenül közzétéve a térképen!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add New Spot Modal / Home Publishing Form
    if (showAddSpotDialog) {
        AddNewSpotDialog(
            presetDraft = draftToPublish,
            existingSpots = initialSpots,
            isDarkMode = isDarkMode,
            onDismiss = {
                showAddSpotDialog = false
                draftToPublish = null
            },
            onSaveDraftOnly = { newDraft ->
                if (draftToPublish != null) {
                    viewModel.deleteFieldDraft(draftToPublish!!.id)
                }
                viewModel.addFieldDraft(newDraft)
                showAddSpotDialog = false
                draftToPublish = null
                Toast.makeText(context, "💾 Piszkozat mentve!", Toast.LENGTH_SHORT).show()
            },
            onAddSpot = { newSpot ->
                viewModel.addSpotterLocation(newSpot)
                // If this came from a draft, remove the draft as it is now published
                if (draftToPublish != null) {
                    viewModel.deleteFieldDraft(draftToPublish!!.id)
                }
                showAddSpotDialog = false
                draftToPublish = null
                Toast.makeText(context, "🎉 Helyszín sikeresen közzétéve a közösségi térképen!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun SpotterHeaderHero(
    spotCount: Int,
    draftCount: Int,
    onQuickGpsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF0D1C38), Color(0xFF0F2942), Color(0xFF060B14))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF00F0FF).copy(alpha = 0.8f), Color(0xFF3B82F6).copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF00F0FF).copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color(0xFF00F0FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "DRÓNOS SPOTTER",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Terepi GPS Mentés & Otthoni Közzététel",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick GPS Save button positioned at the right edge of the window
                Button(
                    onClick = onQuickGpsClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("field_gps_quick_save_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS Mentés", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
            }

            Text(
                text = "📍 Kint drónozás közben 1 kattintással rögzítsd a pozíciót, otthon pedig kényelmesen szerkeszd és tedd közzé a térképen! ($spotCount aktív spot, $draftCount mentett piszkozat)",
                color = Color(0xFFCBD5E1),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SpotterLocationCard(
    spot: SpotterLocation,
    isFavorite: Boolean,
    isDarkMode: Boolean,
    onFavoriteToggle: () -> Unit,
    onOpenMap: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spot_card_${spot.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161F30) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = spot.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00F0FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${spot.category.icon} ${spot.category.displayName}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "📍 ${spot.county}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Kedvenc",
                        tint = if (isFavorite) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                    )
                }
            }

            // Airspace Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = spot.airspaceColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, spot.airspaceColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(spot.airspaceColor, CircleShape)
                    )
                    Text(
                        text = spot.airspaceStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = spot.airspaceColor
                    )
                }
            }

            // Advice snippet
            Text(
                text = spot.advice,
                fontSize = 12.sp,
                color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            // Bottom bar actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spot.bestTime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8)
                )

                Button(
                    onClick = onOpenMap,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF).copy(alpha = 0.18f),
                        contentColor = Color(0xFF00F0FF)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("TÉRKÉP (GPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AirspaceKisokosSection(isDarkMode: Boolean) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(24.dp))
                    Text("NO-FLY ZÓNÁK ÉS LÉGTÉR SZABÁLYOK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Text(
                    text = "Magyarországon a repülés előtt mindig kötelező ellenőrizni a HungaroControl és E-ÁRPÁS (MyAirspace) alkalmazásban az érvényes eseti és korlátozott légtereket!",
                    color = Color(0xFFC7D2FE),
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Airspace items breakdown
        AirspaceRuleCard(
            title = "🔴 CTR (Repülőtéri Zónák)",
            badge = "SZIGORÚ KORLÁTOZÁS",
            badgeColor = Color(0xFFEF4444),
            description = "Budapest Liszt Ferenc (LHBP), Debrecen (LHDC), Sármellék (LHSM) és Kecskemét (LHKE) repülőterek 15 km-es körzetében magasságkorlát (max 50m) és eseti légtér igénylés szükséges!",
            isDarkMode = isDarkMode
        )

        AirspaceRuleCard(
            title = "🟡 Nemzeti Parkok & Tájvédelmi Körzetek",
            badge = "TERMÉSZETVÉDELEM",
            badgeColor = Color(0xFFF59E0B),
            description = "A Hortobágyi, Bükki, Balaton-felvidéki és Duna-Ipoly Nemzeti Parkok területén a védett madárfajok nyugalma érdekében csak a nemzeti parki igazgatóság előzetes engedélyével szabad repülni.",
            isDarkMode = isDarkMode
        )

        AirspaceRuleCard(
            title = "🟢 Nyílt Kategória (C0 / C1 / C2)",
            badge = "120M MAGASSÁGKORLÁT",
            badgeColor = Color(0xFF10B981),
            description = "Nappali fényviszonyok között, közvetlen vizuális rálátás (VLOS) mellett legfeljebb 120 méter magasságig engedélyezett a repülés, ha a terület nem tartozik tiltott vagy eseti légtér alá.",
            isDarkMode = isDarkMode
        )

        AirspaceRuleCard(
            title = "📜 Eseti Légtér Igénylés Kisokos",
            badge = "MYESATI / HUNGAROCONTROL",
            badgeColor = Color(0xFF38BDF8),
            description = "Lakott terület feletti repüléshez (pl. városi videózás esküvőn vagy ingatlannál) a Honvédelmi Minisztérium légtérgazdálkodási osztályánál legalább 30 nappal előre eseti légteret kell igényelni.",
            isDarkMode = isDarkMode
        )
    }
}

@Composable
private fun AirspaceRuleCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    isDarkMode: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161F30) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontSize = 14.sp)
                Surface(shape = RoundedCornerShape(6.dp), color = badgeColor.copy(alpha = 0.15f)) {
                    Text(text = badge, fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = badgeColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Text(text = description, fontSize = 12.sp, color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569), lineHeight = 17.sp)
        }
    }
}

@Composable
private fun SpotDetailDialog(
    spot: SpotterLocation,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onOpenMap: () -> Unit
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
            border = BorderStroke(appTheme.dialogBorderWidthDp.dp, appTheme.cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = spot.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Zárás")
                    }
                }

                Text(text = "📍 ${spot.county} (${spot.lat}, ${spot.lng})", color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                Text(text = "🛡️ Légtéri Státusz:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                Text(text = spot.airspaceStatus, color = spot.airspaceColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Text(text = "📸 Ajánlott Időszak:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                Text(text = spot.bestTime, color = Color(0xFF38BDF8), fontSize = 12.5.sp)

                Text(text = "🚁 Felszállási & Biztonsági Tippek:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                Text(text = spot.advice, color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569), fontSize = 12.sp, lineHeight = 17.sp)

                Text(text = "🛸 Ajánlott Gép:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black)
                Text(text = spot.recommendedDrone, color = Color(0xFFF59E0B), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onOpenMap,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF0A0F1D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NAVIGÁCIÓ GOOGLE TÉRKÉPEN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class PresetSpotItem(
    val name: String,
    val county: String,
    val category: SpotterCategory,
    val lat: Double,
    val lng: Double,
    val advice: String = "",
    val bestTime: String = "🌅 Naplemente"
)

private val PRESET_SPOTS_DATABASE = listOf(
    PresetSpotItem(
        name = "Papi földek (Szabadidőpark & Modellpálya)",
        county = "Érd, Pest megye",
        category = SpotterCategory.PARKS,
        lat = 47.3755,
        lng = 18.9230,
        advice = "Nyílt, tágas sík terep, ideális drónozásra és modellrepülésre. Figyelj a kerékpárosokra és a sétálókra!",
        bestTime = "🌅 Naplemente / Késő délután"
    ),
    PresetSpotItem(
        name = "Bory-vár",
        county = "Székesfehérvár, Fejér megye",
        category = SpotterCategory.CASTLES,
        lat = 47.2025,
        lng = 18.4419,
        advice = "A tornyok és szobrok miatt vizuális rálátás (VLOS) kötelező! A szomszédos utcákból kényelmesen fel lehet szállni.",
        bestTime = "☀️ Kora délelőtti napfény"
    ),
    PresetSpotItem(
        name = "Bokodi Lebegő Falu",
        county = "Oroszlány / Bokod, Komárom-Esztergom megye",
        category = SpotterCategory.LAKES,
        lat = 47.4925,
        lng = 18.2547,
        advice = "Magánstégek övezik, ne repülj alacsonyan a kertek felett! A tó túlsó oldala csendesebb felszállópont.",
        bestTime = "🌆 Késő délutáni tükröződés"
    ),
    PresetSpotItem(
        name = "Szigligeti Vár",
        county = "Szigliget, Veszprém megye (Balaton)",
        category = SpotterCategory.CASTLES,
        lat = 46.7869,
        lng = 17.4367,
        advice = "A várhegy lábánál van parkoló. Szeles időben a Balaton felőli áramlatok miatt óvatosan manőverezz!",
        bestTime = "🌅 Naplemente (Aranyóra)"
    ),
    PresetSpotItem(
        name = "Prédikálószék & Dunakanyar",
        county = "Visegrádi-hegység, Pest megye",
        category = SpotterCategory.MOUNTAINS,
        lat = 47.7208,
        lng = 18.9189,
        advice = "Duna-Ipoly Nemzeti Park terület. A panoráma kereszt mellett jó a műholdjel.",
        bestTime = "🌤️ Kora reggeli ködös napkelte"
    ),
    PresetSpotItem(
        name = "Visegrádi Fellegvár",
        county = "Visegrád, Pest megye",
        category = SpotterCategory.CASTLES,
        lat = 47.7944,
        lng = 18.9814,
        advice = "A bobpálya és a várparkoló mellől szép a rálátás. Tartsd be a látogatóktól való távolságot!",
        bestTime = "🌄 Kora reggeli napkelte"
    ),
    PresetSpotItem(
        name = "Tihanyi Bencés Apátság & Belső-tó",
        county = "Tihany, Veszprém megye",
        category = SpotterCategory.CASTLES,
        lat = 46.9142,
        lng = 17.8894,
        advice = "A Belső-tó partjáról nyílik a legszebb akadálymentes rálátás az Apátságra és a Balatonra.",
        bestTime = "🌅 Naplemente & Levendulaszüret"
    ),
    PresetSpotItem(
        name = "Megyer-hegyi Tengerszem",
        county = "Sárospatak, Borsod-Abaúj-Zemplén megye",
        category = SpotterCategory.LAKES,
        lat = 48.3586,
        lng = 21.5714,
        advice = "A kanyon sziklafalai árnyékolhatják a GPS jelet! Használj vizuális pozicionálást.",
        bestTime = "🍂 Őszi napsütés (Déli órák)"
    ),
    PresetSpotItem(
        name = "Normafa & Erzsébet-kilátó",
        county = "Budapest, XII. kerület",
        category = SpotterCategory.MOUNTAINS,
        lat = 47.5097,
        lng = 18.9664,
        advice = "Nagy a gyalogosforgalom hétvégén. A sípálya alsó lankás részén biztonságosabb a felszállás.",
        bestTime = "🌄 Napkelte a város felett"
    ),
    PresetSpotItem(
        name = "Citadella & Gellért-hegy",
        county = "Budapest, XI. kerület",
        category = SpotterCategory.URBAN,
        lat = 47.4870,
        lng = 19.0458,
        advice = "Budapest belvárosi légtér, eseti légtérhasználati engedély szükséges lehet!",
        bestTime = "🌃 Éjszakai kivilágítás (Kék óra)"
    ),
    PresetSpotItem(
        name = "Budaörsi Kő-hegy & Kápolna",
        county = "Budaörs, Pest megye",
        category = SpotterCategory.MOUNTAINS,
        lat = 47.4652,
        lng = 18.9608,
        advice = "Kopár, sziklás dombtető, fantasztikus körpanorámával a budai hegyekre és a medencére.",
        bestTime = "🌅 Naplemente"
    ),
    PresetSpotItem(
        name = "Százhalombatta Régészeti Park & Duna-part",
        county = "Százhalombatta, Pest megye",
        category = SpotterCategory.PARKS,
        lat = 47.3183,
        lng = 18.9114,
        advice = "Nyugodt folyóparti szakasz, tágas látómező a Duna kanyarulataira.",
        bestTime = "🌤️ Délelőtti órák"
    ),
    PresetSpotItem(
        name = "Gödöllői Királyi Kastély Park",
        county = "Gödöllő, Pest megye",
        category = SpotterCategory.CASTLES,
        lat = 47.5965,
        lng = 19.3551,
        advice = "A kastélypark mögötti rét felől érdemes repülni, figyelve a védett öreg fák lombkoronájára.",
        bestTime = "🍂 Őszi színek / Késő délután"
    ),
    PresetSpotItem(
        name = "Velencei-tó Bence-hegyi Kilátó",
        county = "Sukoró / Velence, Fejér megye",
        category = SpotterCategory.LAKES,
        lat = 47.2392,
        lng = 18.6189,
        advice = "Magaslati pont, ahonnan az egész Velencei-tó és a nádasok beláthatók.",
        bestTime = "🌅 Naplemente a tó tükrében"
    ),
    PresetSpotItem(
        name = "Tata Öreg-tó és Vár",
        county = "Tata, Komárom-Esztergom megye",
        category = SpotterCategory.LAKES,
        lat = 47.6447,
        lng = 18.3197,
        advice = "Őszi vadlúdvonulás idején tilos a zavarás! Egyébként a tóparti sétányról kényelmes a start.",
        bestTime = "🌤️ Tükörsima reggeli vízfelszín"
    ),
    PresetSpotItem(
        name = "Esztergomi Bazilika & Mária Valéria híd",
        county = "Esztergom, Komárom-Esztergom megye",
        category = SpotterCategory.CASTLES,
        lat = 47.7992,
        lng = 18.7369,
        advice = "Határmenti légtér (Duna közepe az államhatár). Ne lépd át a határvonalat engedély nélkül!",
        bestTime = "🌅 Naplemente a Bazilika mögött"
    ),
    PresetSpotItem(
        name = "Egri Vár & Dobó tér",
        county = "Eger, Heves megye",
        category = SpotterCategory.CASTLES,
        lat = 47.9042,
        lng = 20.3794,
        advice = "A várdomb környezetében szép panoráma nyílik a Bükk vonulataira és a belvárosra.",
        bestTime = "☀️ Délelőtti napfény"
    ),
    PresetSpotItem(
        name = "Hollókő Ófalu & Vár",
        county = "Hollókő, Nógrád megye",
        category = SpotterCategory.CASTLES,
        lat = 47.9986,
        lng = 19.5828,
        advice = "Világörökségi falu, a várhegy gerincéről gyönyörűen kirajzolódnak a hagyományos parasztházak.",
        bestTime = "🌄 Kora reggeli fények"
    ),
    PresetSpotItem(
        name = "Balatonkenese Magaspart & Soós-hegy",
        county = "Balatonkenese, Veszprém megye",
        category = SpotterCategory.LAKES,
        lat = 47.0345,
        lng = 18.1092,
        advice = "A partfal peremén erős felszálló légáramlatok lehetnek, viszont a Balaton keleti medencéje lélegzetelállító.",
        bestTime = "🌅 Aranyhíd naplemente"
    ),
    PresetSpotItem(
        name = "Füzéri Vár",
        county = "Füzér, Borsod-Abaúj-Zemplén megye",
        category = SpotterCategory.CASTLES,
        lat = 48.5422,
        lng = 21.4597,
        advice = "Vulkanikus sziklakúpon magasodó fehér vár, a Zemplén egyik legikonikusabb drónos látványa.",
        bestTime = "🌤️ Napos délelőtt"
    ),
    PresetSpotItem(
        name = "Szentendre Duna-korzó & Postás-strand",
        county = "Szentendre, Pest megye",
        category = SpotterCategory.URBAN,
        lat = 47.6631,
        lng = 19.0792,
        advice = "A Duna-parti füves sétányról könnyű a fel- és leszállás.",
        bestTime = "🌆 Késő délután"
    ),
    PresetSpotItem(
        name = "Dunakeszi Duna-parti Sétány",
        county = "Dunakeszi, Pest megye",
        category = SpotterCategory.PARKS,
        lat = 47.6333,
        lng = 19.1333,
        advice = "Kellemes füves partszakasz a Duna mentén, szabad kilátással a Szentendrei-szigetre.",
        bestTime = "🌅 Naplemente a Duna felett"
    )
)

@Composable
private fun AddNewSpotDialog(
    presetDraft: FieldGpsDraft? = null,
    existingSpots: List<SpotterLocation> = emptyList(),
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSaveDraftOnly: (FieldGpsDraft) -> Unit,
    onAddSpot: (SpotterLocation) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(presetDraft?.name ?: "") }
    var county by remember { mutableStateOf(presetDraft?.county ?: "") }
    var category by remember { mutableStateOf(presetDraft?.category ?: SpotterCategory.PARKS) }
    var advice by remember { mutableStateOf(presetDraft?.quickNotes ?: "") }
    var bestTime by remember { mutableStateOf(presetDraft?.bestTime ?: "🌅 Naplemente") }

    var customLat by remember {
        mutableStateOf(
            if (presetDraft != null) String.format(Locale.US, "%.5f", presetDraft.lat) else "47.3755"
        )
    }
    var customLng by remember {
        mutableStateOf(
            if (presetDraft != null) String.format(Locale.US, "%.5f", presetDraft.lng) else "18.9230"
        )
    }
    var geocodedAddressText by remember {
        mutableStateOf(
            if (presetDraft != null) "${presetDraft.name} (${presetDraft.county})" else null
        )
    }
    var isGeocoding by remember { mutableStateOf(false) }
    var isDropdownOpen by remember { mutableStateOf(false) }

    // Combine all spots currently on the map + preset database without duplicates
    val allMapSpots = remember(existingSpots) {
        val mapConverted = existingSpots.map {
            PresetSpotItem(
                name = it.name,
                county = it.county,
                category = it.category,
                lat = it.lat,
                lng = it.lng,
                advice = it.advice,
                bestTime = it.bestTime
            )
        }
        val combined = (mapConverted + PRESET_SPOTS_DATABASE)
        combined.distinctBy { "${it.name.trim().lowercase()}_${it.lat}_${it.lng}" }
    }

    // Filter preset suggestions based on user input (or display all when opened)
    val filteredSuggestions = remember(name, county, allMapSpots) {
        val query = "$name $county".trim().lowercase()
        if (query.isBlank()) {
            allMapSpots
        } else {
            val terms = query.split(" ").filter { it.isNotBlank() }
            allMapSpots.filter { spot ->
                val spotName = spot.name.lowercase()
                val spotCounty = spot.county.lowercase()
                val spotAdvice = spot.advice.lowercase()
                spotName.contains(query) ||
                spotCounty.contains(query) ||
                terms.all { term -> spotName.contains(term) || spotCounty.contains(term) || spotAdvice.contains(term) }
            }
        }
    }

    fun applyPreset(spot: PresetSpotItem) {
        name = spot.name
        county = spot.county
        category = spot.category
        customLat = String.format(Locale.US, "%.5f", spot.lat)
        customLng = String.format(Locale.US, "%.5f", spot.lng)
        advice = spot.advice
        bestTime = spot.bestTime
        geocodedAddressText = "${spot.name} (${spot.county})"
        isDropdownOpen = false
        Toast.makeText(context, "📍 Kiválasztva: ${spot.name}", Toast.LENGTH_SHORT).show()
    }

    fun fetchCurrentDeviceGps() {
        coroutineScope.launch {
            isGeocoding = true
            val locManager = LocationTelemetryManager(context)
            val loc = locManager.fetchCurrentLocation()
            if (loc != null) {
                customLat = String.format(Locale.US, "%.5f", loc.latitude)
                customLng = String.format(Locale.US, "%.5f", loc.longitude)
                val knownSpotsPairs = allMapSpots.map { it.name to (it.lat to it.lng) }
                val resolved = locManager.resolveLocationDetails(loc.latitude, loc.longitude, knownSpotsPairs)
                name = resolved.title
                county = resolved.locality
                geocodedAddressText = "${resolved.title} (${resolved.locality})"
                Toast.makeText(context, "📍 Helyszín automatikusan kitöltve: ${resolved.title}!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Nem sikerült a GPS pozíció lekérése. Ellenőrizd a helymeghatározást!", Toast.LENGTH_SHORT).show()
            }
            isGeocoding = false
        }
    }

    fun resolveCoordinates(locName: String, locCounty: String, onDone: (Double, Double) -> Unit) {
        coroutineScope.launch {
            isGeocoding = true
            val (resolvedLat, resolvedLng, resolvedLabel) = withContext(Dispatchers.IO) {
                var latRes = 0.0
                var lngRes = 0.0
                var labelRes = ""

                val combinedQuery = listOf(locName.trim(), locCounty.trim(), "Magyarország")
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                // 1. Check presets first
                val cleanQuery = "$locName $locCounty".lowercase()
                val presetMatch = allMapSpots.firstOrNull {
                    cleanQuery.contains(it.name.lowercase().take(6)) ||
                    (cleanQuery.contains("papi") && cleanQuery.contains("érd"))
                }
                if (presetMatch != null) {
                    return@withContext Triple(presetMatch.lat, presetMatch.lng, "${presetMatch.name} (${presetMatch.county})")
                }

                // 2. Try Android Geocoder
                try {
                    val geocoder = Geocoder(context, Locale("hu", "HU"))
                    val results = geocoder.getFromLocationName(combinedQuery, 3)
                    if (!results.isNullOrEmpty()) {
                        val first = results[0]
                        latRes = first.latitude
                        lngRes = first.longitude
                        labelRes = first.getAddressLine(0) ?: "$latRes, $lngRes"
                    }
                } catch (_: Exception) {}

                // 3. Fallback city / county geocoder
                if (latRes == 0.0 && locCounty.isNotBlank()) {
                    try {
                        val geocoder = Geocoder(context, Locale("hu", "HU"))
                        val results = geocoder.getFromLocationName("${locCounty.trim()}, Magyarország", 1)
                        if (!results.isNullOrEmpty()) {
                            latRes = results[0].latitude
                            lngRes = results[0].longitude
                            labelRes = results[0].getAddressLine(0) ?: "$latRes, $lngRes"
                        }
                    } catch (_: Exception) {}
                }

                if (latRes == 0.0) {
                    latRes = 47.3755
                    lngRes = 18.9230
                    labelRes = "$locName, $locCounty"
                }

                Triple(latRes, lngRes, labelRes)
            }

            customLat = String.format(Locale.US, "%.5f", resolvedLat)
            customLng = String.format(Locale.US, "%.5f", resolvedLng)
            geocodedAddressText = resolvedLabel
            isGeocoding = false
            onDone(resolvedLat, resolvedLng)
        }
    }

    val appTheme = com.example.ui.theme.LocalAppTheme.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
            border = BorderStroke(appTheme.dialogBorderWidthDp.dp, appTheme.cardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (presetDraft != null) "🏡 Otthoni Közzététel" else "➕ Új Fotós Spot",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )

                    FilledTonalButton(
                        onClick = { fetchCurrentDeviceGps() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF00F0FF).copy(alpha = 0.2f),
                            contentColor = Color(0xFF00F0FF)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GPS bemérés", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (presetDraft != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Terepen rögzített GPS adatok betöltve! Töltsd ki a részleteket a közzétételhez.",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Helyszín neve beviteli mező
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isDropdownOpen = it.isNotBlank()
                        if (it.length > 2 && county.isNotBlank()) {
                            resolveCoordinates(it, county) { _, _ -> }
                        }
                    },
                    label = { Text("Helyszín Neve (pl. Papi földek)") },
                    trailingIcon = {
                        IconButton(onClick = { isDropdownOpen = !isDropdownOpen }) {
                            Icon(
                                imageVector = if (isDropdownOpen) Icons.Default.ExpandLess else Icons.Default.ArrowDropDown,
                                contentDescription = "Lenyitás",
                                tint = Color(0xFF00F0FF)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Közvetlenül a Helyszín neve alatt lefelé gördülő javaslati ablak
                AnimatedVisibility(
                    visible = isDropdownOpen && filteredSuggestions.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Találatok a térképről (${filteredSuggestions.size} helyszín):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
                            )

                            filteredSuggestions.forEach { spot ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDarkMode) Color(0xFF0F172A) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (name.contains(spot.name.take(6), ignoreCase = true)) Color(0xFF00F0FF) else Color(0xFF334155).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { applyPreset(spot) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(spot.category.icon, fontSize = 12.sp)
                                                Text(
                                                    text = spot.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                                )
                                            }
                                            Text(
                                                text = "📍 ${spot.county}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF00F0FF)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "GPS 🎯",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = county,
                    onValueChange = {
                        county = it
                        if (it.length > 2 && name.isNotBlank()) {
                            resolveCoordinates(name, it) { _, _ -> }
                        }
                    },
                    label = { Text("Megye / Település (pl. Érd, Pest megye)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Kategória választó
                Text("Kategória:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SpotterCategory.values().filter { it != SpotterCategory.ALL }.forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text("${cat.icon} ${cat.displayName}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFF00F0FF)
                            )
                        )
                    }
                }

                // Coordinates manual preview / adjust
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customLat,
                        onValueChange = { customLat = it },
                        label = { Text("Szélesség (Lat)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customLng,
                        onValueChange = { customLng = it },
                        label = { Text("Hosszúság (Lng)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = advice,
                    onValueChange = { advice = it },
                    label = { Text("Felszállási tanácsok, parkolás & tapasztalatok") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Dialog Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Mégse", color = Color(0xFF94A3B8))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                val parsedLat = customLat.toDoubleOrNull() ?: 47.3755
                                val parsedLng = customLng.toDoubleOrNull() ?: 18.9230
                                onSaveDraftOnly(
                                    FieldGpsDraft(
                                        id = presetDraft?.id ?: UUID.randomUUID().toString(),
                                        name = name.ifBlank { "Mentett GPS Pont" },
                                        county = county.ifBlank { "Ismeretlen helyszín" },
                                        lat = parsedLat,
                                        lng = parsedLng,
                                        quickNotes = advice,
                                        category = category,
                                        bestTime = bestTime
                                    )
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Piszkozatként", fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank() && county.isNotBlank()) {
                                    val parsedLat = customLat.toDoubleOrNull() ?: 47.3820
                                    val parsedLng = customLng.toDoubleOrNull() ?: 18.9160

                                    onAddSpot(
                                        SpotterLocation(
                                            id = presetDraft?.id ?: System.currentTimeMillis().toString(),
                                            name = name.trim(),
                                            county = county.trim(),
                                            category = category,
                                            lat = parsedLat,
                                            lng = parsedLng,
                                            bestTime = bestTime,
                                            airspaceStatus = "🟢 Közösségi Bevizsgált Spot",
                                            airspaceColor = Color(0xFF10B981),
                                            advice = advice.ifBlank { "Közösségi tag által beküldött fotós pont." },
                                            recommendedDrone = "C0 / C1 Kategória",
                                            userSubmitted = true
                                        )
                                    )
                                } else {
                                    Toast.makeText(context, "Kérlek add meg a nevet és a települést!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF0A0F1D))
                        ) {
                            Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Közzététel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickGpsRecordDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSaveDraft: (FieldGpsDraft) -> Unit,
    onDirectPublish: (SpotterLocation) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var quickTitle by remember { mutableStateOf("Terepi drónos helyszín") }
    var quickLocality by remember { mutableStateOf("Helyszín meghatározása...") }
    var quickNotes by remember { mutableStateOf("") }
    var lat by remember { mutableDoubleStateOf(47.3755) }
    var lng by remember { mutableDoubleStateOf(18.9230) }
    var isAcquiringGps by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isAcquiringGps = true
        val locManager = LocationTelemetryManager(context)
        val location = locManager.fetchCurrentLocation()
        if (location != null) {
            lat = location.latitude
            lng = location.longitude
            val resolved = locManager.resolveLocationDetails(lat, lng)
            quickTitle = resolved.title
            quickLocality = resolved.locality
        } else {
            quickLocality = "Érd és környéke (Becsült pozíció)"
            quickTitle = "Papi-földek"
        }
        isAcquiringGps = false
    }

    val appTheme = com.example.ui.theme.LocalAppTheme.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
            border = BorderStroke(appTheme.dialogBorderWidthDp.dp, appTheme.cardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Gyors Terepi GPS Mentés", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDarkMode) Color.White else Color(0xFF0F172A))
                        Text("Mentés 1 kattintással, publikálás később", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }

                // GPS display pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rögzített GPS Koordináta:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            if (isAcquiringGps) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF10B981))
                            }
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.5f", lat)}, ${String.format(Locale.US, "%.5f", lng)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "📍 $quickLocality",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                OutlinedTextField(
                    value = quickTitle,
                    onValueChange = { quickTitle = it },
                    label = { Text("Megnevezés / Emlékeztető") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = quickNotes,
                    onValueChange = { quickNotes = it },
                    label = { Text("Gyors megjegyzés terepről (opcionális)") },
                    placeholder = { Text("pl. jó starthely a fa mellett, sok a kavics...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val draft = FieldGpsDraft(
                                name = quickTitle.trim(),
                                county = quickLocality,
                                lat = lat,
                                lng = lng,
                                quickNotes = quickNotes.trim()
                            )
                            onSaveDraft(draft)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0A0F1D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mentés Piszkozatként (Otthon közzéteszem)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val newSpot = SpotterLocation(
                                id = System.currentTimeMillis().toString(),
                                name = quickTitle.trim(),
                                county = quickLocality,
                                category = SpotterCategory.PARKS,
                                lat = lat,
                                lng = lng,
                                bestTime = "🌅 Naplemente",
                                airspaceStatus = "🟢 Terepen rögzített új spot",
                                airspaceColor = Color(0xFF10B981),
                                advice = quickNotes.ifBlank { "Helyszínen rögzített fotós pont." },
                                recommendedDrone = "C0 / C1 Kategória",
                                userSubmitted = true
                            )
                            onDirectPublish(newSpot)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF)),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Azonnali Közzététel a Térképre", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Mégse", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldDraftsSection(
    drafts: List<FieldGpsDraft>,
    isDarkMode: Boolean,
    onQuickRecordClick: () -> Unit,
    onPublishDraft: (FieldGpsDraft) -> Unit,
    onOpenMap: (FieldGpsDraft) -> Unit,
    onDeleteDraft: (FieldGpsDraft) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Explanatory Banner Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text(
                        text = "Terepi Rögzítés & Otthoni Közzététel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF00F0FF)
                    )
                }
                Text(
                    text = "Amikor kint vagy a terepen és találsz egy szuper drónos starthelyet, mentsd el 1 kattintással a GPS pontot! Otthon, a fotók letöltése után kényelmesen leírhatod a tapasztalataidat és közzéteheted a térképen.",
                    fontSize = 11.5.sp,
                    color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569),
                    lineHeight = 16.sp
                )
                Button(
                    onClick = onQuickRecordClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0A0F1D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Új Terepi GPS Pont Rögzítése Most", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            }
        }

        if (drafts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00F0FF).copy(alpha = 0.1f),
                        modifier = Modifier.size(70.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📍", fontSize = 32.sp)
                        }
                    }
                    Text(
                        text = "Még nincsenek elmentett piszkozataid",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Drónozás közben nyomj a 'GPS Mentés' gombra, hogy a pontos koordináták megmaradjanak az otthoni közzétételhez!",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(drafts, key = { it.id }) { draft ->
                    DraftItemCard(
                        draft = draft,
                        isDarkMode = isDarkMode,
                        onPublish = { onPublishDraft(draft) },
                        onOpenMap = { onOpenMap(draft) },
                        onDelete = { onDeleteDraft(draft) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftItemCard(
    draft: FieldGpsDraft,
    isDarkMode: Boolean,
    onPublish: () -> Unit,
    onOpenMap: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale("hu", "HU")) }
    val formattedDate = remember(draft.timestamp) { dateFormatter.format(Date(draft.timestamp)) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF0F172A) else Color.White),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = draft.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "📍 ${draft.county}",
                        fontSize = 12.sp,
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⏱️ $formattedDate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 ${String.format(Locale.US, "%.5f", draft.lat)}, ${String.format(Locale.US, "%.5f", draft.lng)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF334155)
                    )
                }
            }

            if (draft.quickNotes.isNotBlank()) {
                Text(
                    text = "📝 \"${draft.quickNotes}\"",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPublish,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF0A0F1D)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🏡 Otthoni Közzététel", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                OutlinedButton(
                    onClick = onOpenMap,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Térkép", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Törlés", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

internal fun launchGoogleMaps(context: Context, lat: Double, lng: Double, label: String, county: String = "") {
    try {
        val hasCustomValidCoords = lat != 0.0 && lng != 0.0 && !(lat == 47.1625 && lng == 19.5033)
        val fullSearchQuery = when {
            county.isNotBlank() && !label.contains(county, ignoreCase = true) -> "$label, $county"
            else -> label
        }

        val uri = if (hasCustomValidCoords) {
            Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(fullSearchQuery)})")
        } else {
            Uri.parse("geo:0,0?q=${Uri.encode(fullSearchQuery)}")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val webUrl = if (hasCustomValidCoords) {
            "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        } else {
            "https://www.google.com/maps/search/?api=1&query=${Uri.encode(fullSearchQuery)}"
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Nem sikerült megnyitni a térképet: ${e.localizedMessage ?: ""}", Toast.LENGTH_SHORT).show()
    }
}
