package com.example.ui

import com.example.auth.PilotUser
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ChatMessage
import com.example.data.MarketplaceListing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val chatMessages by viewModel.messages.collectAsStateWithLifecycle()
    val marketplaceListings by viewModel.marketplaceListings.collectAsStateWithLifecycle()
    val spotterLocations by viewModel.spotterLocations.collectAsStateWithLifecycle()
    val fieldDrafts by viewModel.fieldDrafts.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📊 Áttekintés", "👥 Pilóták / Fiókok", "📍 Spotter Moderáció", "🛒 Piactér Moderáció", "💬 Chat & Képek Kezelése")

    var showClearChatDialog by remember { mutableStateOf(false) }
    var showResetMarketplaceDialog by remember { mutableStateOf(false) }
    var showResetSpotterDialog by remember { mutableStateOf(false) }
    var listingToDelete by remember { mutableStateOf<MarketplaceListing?>(null) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    var imageToDeleteFromMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var previewingImageMessage by remember { mutableStateOf<ChatMessage?>(null) }

    var announcementText by remember { mutableStateOf("") }

    val bgGradient = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0B0F19))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Admin Banner Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.5.dp, Color(0xFFEAB308).copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFEAB308).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(0xFFEAB308), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ADMINISZTRÁCIÓS VEZÉRLŐPULT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                            }
                            Text(
                                text = "Titkos feloldás aktív • 5 perc után auto-zárolás",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Lock & Hide Admin Mode Button
                    Button(
                        onClick = {
                            viewModel.lockAdminMode()
                            Toast.makeText(context, "🔒 Admin mód zárolva és elrejtve!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("admin_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Zárolás",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFEAB308),
                edgePadding = 0.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFFEAB308) else if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content with horizontal swoosh sliding animation
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val isForward = targetState >= initialState
                    if (isForward) {
                        (
                            slideInHorizontally(
                                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                                initialOffsetX = { it }
                            ) + fadeIn(animationSpec = tween(260))
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                                targetOffsetX = { -it }
                            ) + fadeOut(animationSpec = tween(200))
                        )
                    } else {
                        (
                            slideInHorizontally(
                                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                                initialOffsetX = { -it }
                            ) + fadeIn(animationSpec = tween(260))
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                                targetOffsetX = { it }
                            ) + fadeOut(animationSpec = tween(200))
                        )
                    }
                },
                label = "admin_tab_switch",
                modifier = Modifier.fillMaxSize()
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> AdminOverviewTab(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        messagesCount = chatMessages.size,
                        imagesCount = chatMessages.count { it.imageUri != null },
                        listingsCount = marketplaceListings.size,
                        spotsCount = spotterLocations.size,
                        draftsCount = fieldDrafts.size,
                        chatMessages = chatMessages,
                        announcementText = announcementText,
                        onAnnouncementChange = { announcementText = it },
                        onBroadcast = {
                            if (announcementText.isNotBlank()) {
                                viewModel.broadcastAdminAnnouncement(announcementText)
                                Toast.makeText(context, "📢 Rendszerértesítés kiküldve a chatbe!", Toast.LENGTH_SHORT).show()
                                announcementText = ""
                            }
                        },
                        onDeleteAnnouncement = { messageToDelete = it },
                        onResetMarketplace = { showResetMarketplaceDialog = true },
                        onResetSpotter = { showResetSpotterDialog = true },
                        onClearChat = { showClearChatDialog = true }
                    )
                    1 -> AdminUsersTab(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode
                    )
                    2 -> AdminSpotterTab(
                        isDarkMode = isDarkMode,
                        spotterLocations = spotterLocations,
                        fieldDrafts = fieldDrafts,
                        onAddSpot = { viewModel.addSpotterLocation(it) },
                        onUpdateSpot = { viewModel.updateSpotterLocation(it) },
                        onDeleteSpot = { viewModel.deleteSpotterLocation(it.id) },
                        onPublishDraft = { draft ->
                            val newSpot = SpotterLocation(
                                id = UUID.randomUUID().toString(),
                                name = draft.name,
                                county = draft.county,
                                category = draft.category,
                                lat = draft.lat,
                                lng = draft.lng,
                                bestTime = draft.bestTime,
                                airspaceStatus = "🟢 Szabad Légtér (Max 120m VLOS)",
                                airspaceColor = Color(0xFF10B981),
                                advice = draft.quickNotes.ifBlank { "Nincs megadva különös tanács." },
                                recommendedDrone = "C0 / C1 Mini Drónok",
                                userSubmitted = true
                            )
                            viewModel.addSpotterLocation(newSpot)
                            viewModel.deleteFieldDraft(draft.id)
                        },
                        onDeleteDraft = { viewModel.deleteFieldDraft(it.id) },
                        onResetSpotter = { showResetSpotterDialog = true }
                    )
                    3 -> AdminMarketplaceTab(
                        isDarkMode = isDarkMode,
                        listings = marketplaceListings,
                        onDeleteListing = { listingToDelete = it }
                    )
                    4 -> AdminChatTab(
                        isDarkMode = isDarkMode,
                        messages = chatMessages,
                        onDeleteMessage = { messageToDelete = it },
                        onDeleteImage = { imageToDeleteFromMessage = it },
                        onPreviewImage = { previewingImageMessage = it },
                        onClearAll = { showClearChatDialog = true }
                    )
                }
            }
        }

        // Delete Image Confirmation Dialog
        imageToDeleteFromMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { imageToDeleteFromMessage = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HideImage,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Csatolt kép törlése", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Biztosan el szeretnéd távolítani a beküldött nem megfelelő képet ${msg.senderName} üzenetéből?")
                        if (msg.message.isNotBlank()) {
                            Text(
                                text = "Üzenet szövege: \"${msg.message}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "(A szöveges üzenet megmarad a fórumban, kizárólag a csatolt fotó kerül végleges törlésre.)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEAB308)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeImageFromChatMessage(msg.id)
                            imageToDeleteFromMessage = null
                            if (previewingImageMessage?.id == msg.id) {
                                previewingImageMessage = null
                            }
                            Toast.makeText(context, "🖼️ Kép sikeresen törölve!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Kép törlése", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { imageToDeleteFromMessage = null }) {
                        Text("Mégse")
                    }
                }
            )
        }

        // Enlarged Image Inspector Dialog for Admin
        previewingImageMessage?.let { msg ->
            val uri = msg.imageUri
            if (uri != null) {
                AdminImageInspectorDialog(
                    imageUri = uri,
                    senderName = msg.senderName,
                    messageText = msg.message,
                    onDeleteImage = {
                        imageToDeleteFromMessage = msg
                    },
                    onDeleteMessage = {
                        messageToDelete = msg
                    },
                    onDismiss = { previewingImageMessage = null }
                )
            }
        }

        // Delete Listing Confirmation Dialog
        listingToDelete?.let { listing ->
            AlertDialog(
                onDismissRequest = { listingToDelete = null },
                title = { Text("Hirdetés törlése", fontWeight = FontWeight.Bold) },
                text = { Text("Biztosan törölni szeretnéd a(z) '${listing.title}' hirdetést a piactérről?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMarketplaceListing(listing.id)
                            listingToDelete = null
                            Toast.makeText(context, "Hirdetés törölve!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Törlés", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listingToDelete = null }) {
                        Text("Mégse")
                    }
                }
            )
        }

        // Delete Message Confirmation Dialog
        messageToDelete?.let { msg ->
            AlertDialog(
                onDismissRequest = { messageToDelete = null },
                title = { Text("Üzenet törlése", fontWeight = FontWeight.Bold) },
                text = { Text("Biztosan törölni szeretnéd ${msg.senderName} teljes üzenetét?\n\n\"${msg.message.ifBlank { "(Képes üzenet)" }}\"") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteChatMessage(msg.id)
                            messageToDelete = null
                            if (previewingImageMessage?.id == msg.id) {
                                previewingImageMessage = null
                            }
                            Toast.makeText(context, "Üzenet eltávolítva!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Törlés", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { messageToDelete = null }) {
                        Text("Mégse")
                    }
                }
            )
        }

        // Clear Chat Confirmation Dialog
        if (showClearChatDialog) {
            AlertDialog(
                onDismissRequest = { showClearChatDialog = false },
                title = { Text("Chat Teljes Ürítése", fontWeight = FontWeight.Bold) },
                text = { Text("FIGYELEM: Ez a művelet visszaállíthatatlanul törli az összes chat üzenetet és képet a helyi adatbázisból!") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearChat()
                            showClearChatDialog = false
                            Toast.makeText(context, "Chat adatbázis kiürítve!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Minden törlése", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearChatDialog = false }) {
                        Text("Mégse")
                    }
                }
            )
        }

        // Reset Marketplace Confirmation Dialog
        if (showResetMarketplaceDialog) {
            AlertDialog(
                onDismissRequest = { showResetMarketplaceDialog = false },
                title = { Text("Piactér Alaphelyzetbe állítása", fontWeight = FontWeight.Bold) },
                text = { Text("Visszaállítod az eredeti beépített minta hirdetéseket a piactérre?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetMarketplaceToDefaults()
                            showResetMarketplaceDialog = false
                            Toast.makeText(context, "Piactér frissítve!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308))
                    ) {
                        Text("Visszaállítás", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetMarketplaceDialog = false }) {
                        Text("Mégse")
                    }
                }
            )
        }

        // Reset Spotter Locations Confirmation Dialog
        if (showResetSpotterDialog) {
            AlertDialog(
                onDismissRequest = { showResetSpotterDialog = false },
                title = { Text("Spotter Helyszínek Alaphelyzetbe Állítása", fontWeight = FontWeight.Bold) },
                text = { Text("Biztosan visszaállítod az eredeti beépített drónos spotter helyszíneket?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetSpotterLocationsToDefaults()
                            showResetSpotterDialog = false
                            Toast.makeText(context, "Spotter helyszínek alaphelyzetbe állítva!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color.Black)
                    ) {
                        Text("Visszaállítás", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetSpotterDialog = false }) {
                        Text("Mégse")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminUsersTab(
    viewModel: ChatViewModel,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var pilotToDelete by remember { mutableStateOf<PilotUser?>(null) }

    val registeredPilots = remember(refreshTrigger) { viewModel.getAllRegisteredPilots() }

    val filteredPilots = remember(searchQuery, registeredPilots) {
        if (searchQuery.isBlank()) {
            registeredPilots
        } else {
            registeredPilots.filter { pilot ->
                pilot.pilotName.contains(searchQuery, ignoreCase = true) ||
                        pilot.email.contains(searchQuery, ignoreCase = true) ||
                        pilot.homeCounty.contains(searchQuery, ignoreCase = true) ||
                        pilot.primaryDrone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale("hu", "HU")) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("admin_users_tab"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Summary & Search Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                    ),
                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, Color(0xFF3B82F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "REGISZTRÁLT PILÓTÁK (${registeredPilots.size})",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Összes fiók, regisztráció ideje és törlési opció",
                                        fontSize = 11.sp,
                                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { refreshTrigger++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Frissítés",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Keresés név, email, megye vagy drón alapján...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Törlés",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Pilot Accounts List
            if (filteredPilots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "Még nem található regisztrált fiók." else "Nincs a keresésnek megfelelő találat.",
                                fontSize = 13.sp,
                                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredPilots, key = { it.uid + "_" + it.email }) { pilot ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                        ),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, Color(0xFF3B82F6), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pilot.pilotName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3B82F6),
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = pilot.pilotName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Hitelesített",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = pilot.email,
                                            fontSize = 12.sp,
                                            color = Color(0xFF3B82F6),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Delete Account Button
                                OutlinedButton(
                                    onClick = { pilotToDelete = pilot },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFEF4444)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Fiók törlése",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Fiók törlése",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            // License, Drone & Location
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = pilot.primaryDrone,
                                        fontSize = 11.5.sp,
                                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = pilot.homeCounty,
                                        fontSize = 11.5.sp,
                                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)
                                    )
                                }

                                // License Tag
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = pilot.pilotLicense,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Registration Time Highlight
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFEAB308),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Regisztráció ideje: ",
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                                Text(
                                    text = dateFormat.format(Date(pilot.registeredAt)),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEAB308)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delete Account Confirmation Dialog
        pilotToDelete?.let { pilot ->
            AlertDialog(
                onDismissRequest = { pilotToDelete = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilótafiók Törlése", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Biztosan törölni szeretnéd ${pilot.pilotName} (${pilot.email}) fiókját az adatbázisból?",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "A művelet végleges és nem vonható vissza!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = viewModel.deleteUserAccount(pilot.email)
                            if (success) {
                                Toast.makeText(context, "🗑️ ${pilot.pilotName} fiókja sikeresen törölve!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Hiba a fiók törlése során.", Toast.LENGTH_SHORT).show()
                            }
                            pilotToDelete = null
                            refreshTrigger++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Fiók Végleges Törlése", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pilotToDelete = null }) {
                        Text("Mégse")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminOverviewTab(
    viewModel: ChatViewModel,
    isDarkMode: Boolean,
    messagesCount: Int,
    imagesCount: Int,
    listingsCount: Int,
    spotsCount: Int,
    draftsCount: Int,
    chatMessages: List<ChatMessage> = emptyList(),
    announcementText: String,
    onAnnouncementChange: (String) -> Unit,
    onBroadcast: () -> Unit,
    onDeleteAnnouncement: (ChatMessage) -> Unit = {},
    onResetMarketplace: () -> Unit,
    onResetSpotter: () -> Unit,
    onClearChat: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Quick Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Spotter Helyszínek",
                        value = "$spotsCount spot • $draftsCount piszkozat",
                        icon = Icons.Default.Place,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                    AdminStatCard(
                        title = "Piactér Hirdetés",
                        value = "$listingsCount db",
                        icon = Icons.Default.ShoppingCart,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Chat / Képek",
                        value = "$messagesCount msg • $imagesCount kép",
                        icon = Icons.Default.Image,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }
            }
        }

        // Announcement Broadcaster Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hivatalos Rendszerértesítés Kiküldése",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Az itt megadott üzenet '👑 Rendszer Adminisztrátor' feladóval azonnal megjelenik az összes pilóta élő chatjében.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = onAnnouncementChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_announcement_input"),
                        placeholder = { Text("Pl.: Figyelem: Balaton térségében erős szélvihar várható 14:00-tól!") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEAB308),
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onBroadcast,
                        enabled = announcementText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_broadcast_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEAB308),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Értesítés Kiküldése Most", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Published Active Announcements List Card
        val activeAnnouncements = chatMessages.filter {
            it.senderName.contains("Admin") || it.senderName.contains("Tulajdonos") || it.senderName.contains("Rendszer")
        }
        if (activeAnnouncements.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📢 Közzétett Tulajdonosi Értesítések (${activeAnnouncements.size} db)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEAB308)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        activeAnnouncements.forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = msg.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onDeleteAnnouncement(msg) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                                            contentColor = Color(0xFFEF4444)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Törlés",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Törlés", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Maintenance Tools Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rendszer Karbantartási Műveletek",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetSpotter,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF00F0FF))
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Spotter Reset", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onResetMarketplace,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEAB308))
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Piactér Reset", color = Color(0xFFEAB308), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onClearChat,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Chat Ürítés", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMarketplaceTab(
    isDarkMode: Boolean,
    listings: List<MarketplaceListing>,
    onDeleteListing: (MarketplaceListing) -> Unit
) {
    if (listings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nincsenek hirdetések a rendszerben.", color = Color(0xFF94A3B8))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Összes hirdetés (${listings.size} db)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }

            items(listings, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                    ),
                    border = BorderStroke(1.dp, if (item.isUserCreated) Color(0xFF38BDF8) else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (item.type == "ELADÁS") Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.type,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.type == "ELADÁS") Color(0xFF10B981) else Color(0xFF38BDF8)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                if (item.isUserCreated) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEAB308).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("FELHASZNÁLÓI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFEAB308), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.GERMANY).format(item.price)} Ft • ${item.sellerName} (${item.location})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteListing(item) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Törlés")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminChatTab(
    isDarkMode: Boolean,
    messages: List<ChatMessage>,
    onDeleteMessage: (ChatMessage) -> Unit,
    onDeleteImage: (ChatMessage) -> Unit,
    onPreviewImage: (ChatMessage) -> Unit,
    onClearAll: () -> Unit
) {
    var showOnlyImages by remember { mutableStateOf(false) }

    val filteredMessages = remember(messages, showOnlyImages) {
        if (showOnlyImages) {
            messages.filter { it.imageUri != null }
        } else {
            messages
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Controls header: filter chips and Clear All button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showOnlyImages,
                    onClick = { showOnlyImages = false },
                    label = { Text("Mind (${messages.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEAB308),
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = showOnlyImages,
                    onClick = { showOnlyImages = true },
                    label = {
                        val imgCount = messages.count { it.imageUri != null }
                        Text("📷 Képek ($imgCount)", fontSize = 12.sp, fontWeight = if (showOnlyImages) FontWeight.Bold else FontWeight.Normal)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF38BDF8),
                        selectedLabelColor = Color.Black
                    )
                )
            }

            TextButton(onClick = onClearAll) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Chat ürítése", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showOnlyImages) "Nincsenek beküldött képek a chatben." else "Nincsenek üzenetek a chatben.",
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMessages.reversed(), key = { it.id }) { msg ->
                    val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                    val timeStr = sdf.format(Date(msg.timestamp))
                    val hasImage = msg.imageUri != null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                msg.senderName.contains("Admin") -> Color(0xFFEAB308)
                                hasImage -> Color(0xFF38BDF8).copy(alpha = 0.6f)
                                else -> if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header: Pilot Name, Timestamp, Badges, Delete Message Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (msg.senderName.contains("Admin")) Color(0xFFEAB308) else Color(0xFF38BDF8)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                    if (hasImage) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "📷 FOTÓ",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Delete Entire Message Button
                                IconButton(
                                    onClick = { onDeleteMessage(msg) },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Üzenet törlése",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Message text (if any)
                            if (msg.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                            }

                            // Image attachment preview & direct image moderation button
                            if (hasImage && msg.imageUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        // Image thumbnail with click-to-preview
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onPreviewImage(msg) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AdminImageThumbnail(imageUri = msg.imageUri)

                                            // Zoom icon badge overlay
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.ZoomIn,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Nagyítás", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Image Moderation Action Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Nem megfelelő tartalmú kép?",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8)
                                            )

                                            // Direct Button to remove specifically the image
                                            Button(
                                                onClick = { onDeleteImage(msg) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFEF4444).copy(alpha = 0.9f),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.HideImage,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Csak kép törlése",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminImageThumbnail(imageUri: String) {
    val isPreset = imageUri == "drone_preset_sunset" ||
            imageUri == "drone_preset_forest" ||
            imageUri == "drone_preset_lake" ||
            imageUri == "drone_preset_city"

    if (isPreset) {
        val resId = getDrawableIdByName(imageUri)
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Csatolt drónfotó",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = Uri.parse(imageUri),
            contentDescription = "Csatolt drónfotó",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun AdminImageInspectorDialog(
    imageUri: String,
    senderName: String,
    messageText: String,
    onDeleteImage: () -> Unit,
    onDeleteMessage: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable { onDismiss() }
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEAB308).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(0xFFEAB308), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kép ellenőrzése: $senderName",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (messageText.isNotBlank()) {
                                Text(
                                    text = messageText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_app_drone_logo),
                            contentDescription = "Bezárás",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center Zoomed Image View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isPreset = imageUri == "drone_preset_sunset" ||
                            imageUri == "drone_preset_forest" ||
                            imageUri == "drone_preset_lake" ||
                            imageUri == "drone_preset_city"

                    if (isPreset) {
                        val resId = getDrawableIdByName(imageUri)
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Kinagyított kép",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = Uri.parse(imageUri),
                            contentDescription = "Kinagyított kép",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Moderation Action Bar at Bottom
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false, onClick = {}),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Adminisztrátori műveletek ezen a képen:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onDeleteImage,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.HideImage, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Csak a kép törlése", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onDeleteMessage,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Teljes üzenet törlése", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSpotterTab(
    isDarkMode: Boolean,
    spotterLocations: List<SpotterLocation>,
    fieldDrafts: List<FieldGpsDraft>,
    onAddSpot: (SpotterLocation) -> Unit,
    onUpdateSpot: (SpotterLocation) -> Unit,
    onDeleteSpot: (SpotterLocation) -> Unit,
    onPublishDraft: (FieldGpsDraft) -> Unit,
    onDeleteDraft: (FieldGpsDraft) -> Unit,
    onResetSpotter: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SpotterCategory.ALL) }

    var spotToEdit by remember { mutableStateOf<SpotterLocation?>(null) }
    var spotToDelete by remember { mutableStateOf<SpotterLocation?>(null) }
    var draftToDelete by remember { mutableStateOf<FieldGpsDraft?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredSpots = remember(searchQuery, selectedCategory, spotterLocations) {
        spotterLocations.filter { spot ->
            val matchesCategory = selectedCategory == SpotterCategory.ALL || spot.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    spot.name.contains(searchQuery, ignoreCase = true) ||
                    spot.county.contains(searchQuery, ignoreCase = true) ||
                    spot.advice.contains(searchQuery, ignoreCase = true) ||
                    spot.airspaceStatus.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar & Add / Reset Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Keresés spot név, megye...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00F0FF)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Új Spot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Category Filter Chips Row
        ScrollableTabRow(
            selectedTabIndex = SpotterCategory.entries.indexOf(selectedCategory),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {}
        ) {
            SpotterCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("${cat.icon} ${cat.displayName}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00F0FF),
                        selectedLabelColor = Color.Black,
                        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ACTIVE SPOTS SECTION HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 Aktív Spotter Helyszínek (${filteredSpots.size} db)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )

                    TextButton(onClick = onResetSpotter) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gyári Alaphelyzet", color = Color(0xFFEAB308), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredSpots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Text(
                            text = "Nincs a keresésnek megfelelő spotter bejegyzés.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredSpots, key = { it.id }) { spot ->
                    AdminSpotCard(
                        spot = spot,
                        isDarkMode = isDarkMode,
                        onEdit = { spotToEdit = spot },
                        onDelete = { spotToDelete = spot },
                        onOpenMap = { launchGoogleMaps(context, spot.lat, spot.lng, spot.name, spot.county) }
                    )
                }
            }

            // DRAFTS SECTION HEADER
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📝 Terepen Mentett Piszkozatok (${fieldDrafts.size} db)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }

            if (fieldDrafts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Text(
                            text = "Nincsenek függőben lévő terepi piszkozatok.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                items(fieldDrafts, key = { it.id }) { draft ->
                    AdminDraftCard(
                        draft = draft,
                        isDarkMode = isDarkMode,
                        onPublish = {
                            onPublishDraft(draft)
                            Toast.makeText(context, "🚀 Piszkozat közzétéve a térképen!", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { draftToDelete = draft },
                        onOpenMap = { launchGoogleMaps(context, draft.lat, draft.lng, draft.name, draft.county) }
                    )
                }
            }
        }
    }

    // Dialogs
    spotToEdit?.let { spot ->
        AdminEditSpotterDialog(
            spot = spot,
            isDarkMode = isDarkMode,
            onDismiss = { spotToEdit = null },
            onSave = { updatedSpot ->
                onUpdateSpot(updatedSpot)
                spotToEdit = null
                Toast.makeText(context, "Módosítások elmentve!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddDialog) {
        AdminAddSpotterDialog(
            isDarkMode = isDarkMode,
            onDismiss = { showAddDialog = false },
            onSave = { newSpot ->
                onAddSpot(newSpot)
                showAddDialog = false
                Toast.makeText(context, "Új spot sikeresen hozzáadva!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    spotToDelete?.let { spot ->
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            title = { Text("Spotter Helyszín Törlése", fontWeight = FontWeight.Bold) },
            text = { Text("Biztosan törölni szeretnéd a(z) \"${spot.name}\" spotter bejegyzést?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSpot(spot)
                        spotToDelete = null
                        Toast.makeText(context, "Bejegyzés törölve!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Törlés", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) {
                    Text("Mégse")
                }
            }
        )
    }

    draftToDelete?.let { draft ->
        AlertDialog(
            onDismissRequest = { draftToDelete = null },
            title = { Text("Piszkozat Törlése", fontWeight = FontWeight.Bold) },
            text = { Text("Biztosan törölni szeretnéd a(z) \"${draft.name}\" terepi piszkozatot?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDraft(draft)
                        draftToDelete = null
                        Toast.makeText(context, "Piszkozat törölve!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Törlés", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { draftToDelete = null }) {
                    Text("Mégse")
                }
            }
        )
    }
}

@Composable
fun AdminSpotCard(
    spot: SpotterLocation,
    isDarkMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(spot.category.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = spot.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = spot.county,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00F0FF)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (spot.userSubmitted) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (spot.userSubmitted) Color(0xFF3B82F6) else Color(0xFF10B981))
                ) {
                    Text(
                        text = if (spot.userSubmitted) "Felhasználói" else "Hivatalos",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (spot.userSubmitted) Color(0xFF3B82F6) else Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = spot.airspaceColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, spot.airspaceColor)
                ) {
                    Text(
                        text = spot.airspaceStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = spot.airspaceColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${spot.lat}, ${spot.lng}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "💡 ${spot.advice}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F0FF))
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Térkép", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Szerkesztés", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Törlés", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminDraftCard(
    draft: FieldGpsDraft,
    isDarkMode: Boolean,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onOpenMap: () -> Unit
) {
    val dateStr = remember(draft.timestamp) {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(draft.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(draft.category.icon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = draft.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }

            if (draft.county.isNotBlank()) {
                Text(
                    text = draft.county,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00F0FF)
                )
            }

            if (draft.quickNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 ${draft.quickNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F0FF))
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Térkép", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPublish,
                    modifier = Modifier.weight(1.3f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Közzététel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Törlés", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditSpotterDialog(
    spot: SpotterLocation,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (SpotterLocation) -> Unit
) {
    var name by remember { mutableStateOf(spot.name) }
    var county by remember { mutableStateOf(spot.county) }
    var category by remember { mutableStateOf(spot.category) }
    var latText by remember { mutableStateOf(spot.lat.toString()) }
    var lngText by remember { mutableStateOf(spot.lng.toString()) }
    var bestTime by remember { mutableStateOf(spot.bestTime) }
    var airspaceStatus by remember { mutableStateOf(spot.airspaceStatus) }
    var advice by remember { mutableStateOf(spot.advice) }
    var recommendedDrone by remember { mutableStateOf(spot.recommendedDrone) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) Color(0xFF1E293B) else Color.White,
            border = BorderStroke(1.5.dp, Color(0xFFEAB308))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditLocation, contentDescription = null, tint = Color(0xFFEAB308))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Spotter Bejegyzés Szerkesztése", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Bezárás")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Helyszín Megnevezése") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = county,
                    onValueChange = { county = it },
                    label = { Text("Település / Vármegye") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Kategória:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpotterCategory.entries.filter { it != SpotterCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("${cat.icon} ${cat.displayName}", fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Szélesség (Lat)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Hosszúság (Lng)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Légtér Státusz & Szín:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val statusOptions = listOf(
                        "🟢 Szabad Légtér (Max 120m VLOS)" to Color(0xFF10B981),
                        "🟡 Duna-Ipoly Nemzeti Park" to Color(0xFFF59E0B),
                        "🟡 Lakott terület (Eseti)" to Color(0xFFF59E0B),
                        "🔴 CTR / Tilalom / Mytria" to Color(0xFFEF4444)
                    )
                    statusOptions.forEach { (status, _) ->
                        FilterChip(
                            selected = airspaceStatus == status,
                            onClick = { airspaceStatus = status },
                            label = { Text(status, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = bestTime,
                    onValueChange = { bestTime = it },
                    label = { Text("Legjobb Időszak") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = recommendedDrone,
                    onValueChange = { recommendedDrone = it },
                    label = { Text("Ajánlott Drón Típus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = advice,
                    onValueChange = { advice = it },
                    label = { Text("Fotós & Repülési Tanácsok") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Mégse")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lat = latText.toDoubleOrNull() ?: spot.lat
                            val lng = lngText.toDoubleOrNull() ?: spot.lng
                            val color = when {
                                airspaceStatus.contains("🔴") -> Color(0xFFEF4444)
                                airspaceStatus.contains("🟡") -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            onSave(
                                spot.copy(
                                    name = name.trim(),
                                    county = county.trim(),
                                    category = category,
                                    lat = lat,
                                    lng = lng,
                                    bestTime = bestTime.trim(),
                                    airspaceStatus = airspaceStatus.trim(),
                                    airspaceColor = color,
                                    advice = advice.trim(),
                                    recommendedDrone = recommendedDrone.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color.Black)
                    ) {
                        Text("Mentés", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddSpotterDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (SpotterLocation) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SpotterCategory.PARKS) }
    var latText by remember { mutableStateOf("47.4979") }
    var lngText by remember { mutableStateOf("19.0402") }
    var bestTime by remember { mutableStateOf("🌅 Naplemente (Aranyóra)") }
    var airspaceStatus by remember { mutableStateOf("🟢 Szabad Légtér (Max 120m VLOS)") }
    var advice by remember { mutableStateOf("") }
    var recommendedDrone by remember { mutableStateOf("C0 Mini Drónok (<249g)") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) Color(0xFF1E293B) else Color.White,
            border = BorderStroke(1.5.dp, Color(0xFF00F0FF))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddLocation, contentDescription = null, tint = Color(0xFF00F0FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Új Hivatalos Spot Hozzáadása", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Bezárás")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Helyszín Megnevezése") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = county,
                    onValueChange = { county = it },
                    label = { Text("Település / Vármegye") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Kategória:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpotterCategory.entries.filter { it != SpotterCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("${cat.icon} ${cat.displayName}", fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Szélesség (Lat)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Hosszúság (Lng)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = airspaceStatus,
                    onValueChange = { airspaceStatus = it },
                    label = { Text("Légtér Státusz") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bestTime,
                    onValueChange = { bestTime = it },
                    label = { Text("Legjobb Időszak") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = recommendedDrone,
                    onValueChange = { recommendedDrone = it },
                    label = { Text("Ajánlott Drón Típus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = advice,
                    onValueChange = { advice = it },
                    label = { Text("Fotós & Repülési Tanácsok") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Mégse")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val lat = latText.toDoubleOrNull() ?: 47.4979
                                val lng = lngText.toDoubleOrNull() ?: 19.0402
                                val color = when {
                                    airspaceStatus.contains("🔴") -> Color(0xFFEF4444)
                                    airspaceStatus.contains("🟡") -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }
                                onSave(
                                    SpotterLocation(
                                        id = UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        county = county.trim(),
                                        category = category,
                                        lat = lat,
                                        lng = lng,
                                        bestTime = bestTime.trim(),
                                        airspaceStatus = airspaceStatus.trim(),
                                        airspaceColor = color,
                                        advice = advice.trim().ifBlank { "Közösségi drónos spotter helyszín." },
                                        recommendedDrone = recommendedDrone.trim(),
                                        userSubmitted = false
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color.Black)
                    ) {
                        Text("Hozzáadás", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
