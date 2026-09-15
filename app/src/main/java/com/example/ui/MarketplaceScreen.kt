package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MarketplaceListing
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

enum class MarketplaceTab {
    LISTINGS,
    ESTIMATOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val allListings by viewModel.marketplaceListings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.marketplaceSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.marketplaceCategoryFilter.collectAsStateWithLifecycle()
    val selectedType by viewModel.marketplaceTypeFilter.collectAsStateWithLifecycle()
    val currentPilotUser by viewModel.currentPilotUser.collectAsStateWithLifecycle()
    val marketplaceAuthWarning by viewModel.marketplaceAuthWarning.collectAsStateWithLifecycle()

    var isNetworkAvailable by remember { mutableStateOf(com.example.util.NetworkUtils.isNetworkAvailable(context)) }

    LaunchedEffect(Unit) {
        isNetworkAvailable = com.example.util.NetworkUtils.isNetworkAvailable(context)
    }

    var currentTab by remember { mutableStateOf(MarketplaceTab.LISTINGS) }
    var showSafetyTipsFullScreen by remember { mutableStateOf(false) }
    var shouldReturnSafetyTipsBanner by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAuthRequiredDialog by remember { mutableStateOf(false) }
    var prefilledTitle by remember { mutableStateOf("") }
    var prefilledPrice by remember { mutableStateOf("") }
    var prefilledCondition by remember { mutableStateOf("Újszerű / Garanciális") }
    var prefilledAccessories by remember { mutableStateOf("") }

    var selectedListingForDetail by remember { mutableStateOf<MarketplaceListing?>(null) }
    var listingToDelete by remember { mutableStateOf<MarketplaceListing?>(null) }

    fun handleOpenCreateListing() {
        val isConnected = com.example.util.NetworkUtils.isNetworkAvailable(context)
        isNetworkAvailable = isConnected
        if (!isConnected) {
            Toast.makeText(
                context,
                "⚠️ Hirdetés feladásához aktív internetkapcsolatra van szükség!",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (currentPilotUser == null) {
            showAuthRequiredDialog = true
        } else {
            showCreateDialog = true
        }
    }

    // Filter listings based on type, category, and search query
    val filteredListings = remember(allListings, searchQuery, selectedCategory, selectedType) {
        allListings.filter { listing ->
            val matchesType = when (selectedType) {
                "ELADÁS" -> listing.type == "ELADÁS"
                "VÉTEL" -> listing.type == "VÉTEL"
                else -> true
            }

            val matchesCategory = if (selectedCategory == "Mind") true else {
                listing.category.equals(selectedCategory, ignoreCase = true)
            }

            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                listing.title.lowercase().contains(q) ||
                        listing.description.lowercase().contains(q) ||
                        listing.location.lowercase().contains(q) ||
                        listing.sellerName.lowercase().contains(q) ||
                        (listing.accessories?.lowercase()?.contains(q) == true)
            }

            matchesType && matchesCategory && matchesQuery
        }
    }

    val categories = listOf("Mind", "DJI", "FPV", "Autel", "Tartozék", "Egyéb")
    val types = listOf("Összes", "ELADÁS", "VÉTEL")

    if (showSafetyTipsFullScreen) {
        MarketplaceSafetyTipsFullScreen(
            onBack = {
                showSafetyTipsFullScreen = false
                shouldReturnSafetyTipsBanner = true
            }
        )
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (currentTab == MarketplaceTab.LISTINGS) {
                    ExtendedFloatingActionButton(
                        onClick = { handleOpenCreateListing() },
                        containerColor = if (currentPilotUser != null) Color(0xFF00F0FF) else Color(0xFF38BDF8),
                        contentColor = Color(0xFF0F172A),
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        icon = {
                            Icon(
                                imageVector = if (currentPilotUser != null) Icons.Default.Add else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = if (currentPilotUser != null) "Hirdetés Feladása" else "Hirdetés Feladása (Belépés szükséges)",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        modifier = Modifier
                            .testTag("marketplace_new_listing_button")
                            .padding(bottom = 8.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ==========================================
                // TOP SELECTABLE MENU / TAB SELECTOR
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1524), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Option 1: Piactér
                        val isListingsSelected = currentTab == MarketplaceTab.LISTINGS
                        Surface(
                            onClick = { currentTab = MarketplaceTab.LISTINGS },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isListingsSelected) Color(0xFF00F0FF) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("marketplace_menu_tab_listings")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isListingsSelected) Color(0xFF0A0F1D) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Piactér",
                                    fontSize = 14.sp,
                                    fontWeight = if (isListingsSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isListingsSelected) Color(0xFF0A0F1D) else Color(0xFFCBD5E1)
                                )
                            }
                        }

                        // Option 2: Drón Értékbecslő
                        val isEstimatorSelected = currentTab == MarketplaceTab.ESTIMATOR
                        Surface(
                            onClick = { currentTab = MarketplaceTab.ESTIMATOR },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEstimatorSelected) Color(0xFF00F0FF) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("marketplace_menu_tab_estimator")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = if (isEstimatorSelected) Color(0xFF0A0F1D) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Drón Értékbecslő",
                                    fontSize = 14.sp,
                                    fontWeight = if (isEstimatorSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isEstimatorSelected) Color(0xFF0A0F1D) else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // SCREEN CONTENT BASED ON SELECTED TAB
                // ==========================================
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        val isForward = targetState.ordinal >= initialState.ordinal
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
                    label = "marketplace_view_switch",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        MarketplaceTab.LISTINGS -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("marketplace_screen"),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Hero Banner
                                item {
                                    MarketplaceHeroCard()
                                }

                                // Authentication Warning Banner if user tried to perform restricted action
                                if (marketplaceAuthWarning != null) {
                                    item {
                                        Surface(
                                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("marketplace_auth_warning_banner")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = marketplaceAuthWarning ?: "",
                                                        color = Color(0xFFFCA5A5),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    TextButton(
                                                        onClick = {
                                                            viewModel.clearMarketplaceAuthWarning()
                                                            viewModel.navigateTo(AppScreen.PROFILE)
                                                        }
                                                    ) {
                                                        Text(
                                                            text = "Belépés",
                                                            color = Color(0xFFFCA5A5),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.clearMarketplaceAuthWarning() },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Figyelmeztetés bezárása",
                                                            tint = Color(0xFFFCA5A5).copy(alpha = 0.7f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Safety Tips Banner With Interactive Drone Pull Animation
                                item {
                                    MarketplaceSafetyTipsBannerWithDroneAnimation(
                                        shouldReturnBanner = shouldReturnSafetyTipsBanner,
                                        onReturnAnimationFinished = { shouldReturnSafetyTipsBanner = false },
                                        onOpenSafetyTips = { showSafetyTipsFullScreen = true }
                                    )
                                }

                                if (!isNetworkAvailable) {
                                    item {
                                        Surface(
                                            color = Color(0xFF0F172A),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp)
                                                .testTag("marketplace_offline_card")
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WifiOff,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "A Piactér eléréséhez internetkapcsolat szükséges",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "A hirdetések böngészéséhez és új hirdetés feladásához aktív internetkapcsolatra van szükség. Kérlek, csatlakozz a hálózathoz!",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF94A3B8),
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 18.sp
                                                )
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Button(
                                                    onClick = {
                                                        val connected = com.example.util.NetworkUtils.isNetworkAvailable(context)
                                                        isNetworkAvailable = connected
                                                        if (!connected) {
                                                            Toast.makeText(context, "⚠️ Még mindig nincs internetkapcsolat!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "⚡ Internetkapcsolat helyreállt!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF00F0FF),
                                                        contentColor = Color(0xFF0F172A)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Frissítés / Próbáld újra", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Search Bar
                                    item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setMarketplaceSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("marketplace_search_input"),
                                placeholder = {
                                    Text(
                                        "Keresés modell, város, alkatrész szerint (pl. Mini 4 Pro, FPV, Budapest)...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Keresés",
                                        tint = Color(0xFF00F0FF)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setMarketplaceSearchQuery("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Törlés",
                                                tint = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1E293B),
                                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                    focusedBorderColor = Color(0xFF00F0FF),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )
                        }

            // Type Filter Selector (Összes / Csak Eladás / Csak Vétel)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = selectedType == type
                        val label = when (type) {
                            "ELADÁS" -> "🟢 Eladó Drónok (${allListings.count { it.type == "ELADÁS" }})"
                            "VÉTEL" -> "🔵 Keresem / Vétel (${allListings.count { it.type == "VÉTEL" }})"
                            else -> "Összes (${allListings.size})"
                        }

                        Surface(
                            onClick = { viewModel.setMarketplaceTypeFilter(type) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setMarketplaceCategoryFilter(category) },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00F0FF),
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.6f),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color(0xFF00F0FF),
                                borderColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Listing Count / Status Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Találatok: ${filteredListings.size} db hirdetés",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (searchQuery.isNotEmpty() || selectedCategory != "Mind" || selectedType != "Összes") {
                        TextButton(
                            onClick = {
                                viewModel.setMarketplaceSearchQuery("")
                                viewModel.setMarketplaceCategoryFilter("Mind")
                                viewModel.setMarketplaceTypeFilter("Összes")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Szűrők törlése",
                                color = Color(0xFF00F0FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Empty State
            if (filteredListings.isEmpty()) {
                item {
                    MarketplaceEmptyState(
                        searchQuery = searchQuery,
                        onClearFilters = {
                            viewModel.setMarketplaceSearchQuery("")
                            viewModel.setMarketplaceCategoryFilter("Mind")
                            viewModel.setMarketplaceTypeFilter("Összes")
                        },
                        onPostListing = { handleOpenCreateListing() }
                    )
                }
            } else {
                // Listing Cards
                items(filteredListings, key = { it.id }) { listing ->
                    MarketplaceListingCard(
                        listing = listing,
                        onClick = { selectedListingForDetail = listing },
                        onDeleteClick = if (listing.isUserCreated) {
                            { listingToDelete = listing }
                        } else null,
                        onQuickContact = {
                            openPhoneDialer(context, listing.contactPhone)
                        }
                    )
                }
            }

            } // End of else (isNetworkAvailable)

            // Bottom Spacing for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
    MarketplaceTab.ESTIMATOR -> {
        DronePriceEstimatorContent(
            onApplyToNewListing = { title, suggestedPrice, condition, accessories ->
                prefilledTitle = title
                prefilledPrice = suggestedPrice.toString()
                prefilledCondition = condition
                prefilledAccessories = accessories
                currentTab = MarketplaceTab.LISTINGS
                handleOpenCreateListing()
            },
            onClose = {
                currentTab = MarketplaceTab.LISTINGS
            },
            isEmbedded = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}
}
}
}
}

    // Modal: Detail View
    selectedListingForDetail?.let { listing ->
        MarketplaceDetailDialog(
            listing = listing,
            onDismiss = { selectedListingForDetail = null },
            onDelete = if (listing.isUserCreated) {
                {
                    viewModel.deleteMarketplaceListing(listing.id)
                    selectedListingForDetail = null
                    Toast.makeText(context, "Hirdetés sikeresen törölve", Toast.LENGTH_SHORT).show()
                }
            } else null
        )
    }

    // Modal: Create New Listing
    if (showCreateDialog) {
        CreateMarketplaceListingDialog(
            initialTitle = prefilledTitle,
            initialPrice = prefilledPrice,
            initialCondition = prefilledCondition,
            initialAccessories = prefilledAccessories,
            initialSellerName = currentPilotUser?.effectiveDisplayName ?: "",
            initialEmail = currentPilotUser?.email ?: "",
            onDismiss = {
                showCreateDialog = false
                prefilledTitle = ""
                prefilledPrice = ""
                prefilledAccessories = ""
            },
            onSubmit = { title, type, category, price, condition, location, description, batteryCycles, accessories, sellerName, phone, email, imageUri ->
                viewModel.postNewListing(
                    title = title,
                    type = type,
                    category = category,
                    price = price,
                    condition = condition,
                    location = location,
                    description = description,
                    batteryCycles = batteryCycles,
                    accessories = accessories,
                    sellerName = sellerName,
                    contactPhone = phone,
                    contactEmail = email,
                    imageUri = imageUri
                )
                showCreateDialog = false
                prefilledTitle = ""
                prefilledPrice = ""
                prefilledAccessories = ""
                Toast.makeText(context, "Hirdetésed sikeresen feladva a piactéren!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: Authentication Required Dialog for Ad Posting
    if (showAuthRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showAuthRequiredDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Bejelentkezés szükséges",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "A drónos hirdetések böngészése mindenki számára szabadon elérhető, azonban új hirdetés feladásához be kell jelentkezned a pilóta profilodba.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "Jelentkezz be vagy hozz létre gyorsan egy fiókot a Profil menüpontban!",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthRequiredDialog = false
                        viewModel.clearMarketplaceAuthWarning()
                        viewModel.navigateTo(AppScreen.PROFILE)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bejelentkezés", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAuthRequiredDialog = false }
                ) {
                    Text("Mégse", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirm Delete Dialog
    listingToDelete?.let { listing ->
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = { Text("Hirdetés törlése", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Biztosan törölni szeretnéd a(z) \"${listing.title}\" című hirdetésedet a piactérről?",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMarketplaceListing(listing.id)
                        listingToDelete = null
                        Toast.makeText(context, "Hirdetés törölve", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Törlés", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { listingToDelete = null }) {
                    Text("Mégse", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Standout Hero Section - Open, immersive layout without a boxy container.
 * Features an organic radar sweep line, glowing drone silhouette, high-impact headline,
 * and live pilot community counters.
 */
@Composable
private fun MarketplaceHeroCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "marketplace_hero_anim")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // TOP ROW: Organic Flight Trajectory Bar & Live Radar Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Pilot Community Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.18f), Color(0xFF3B82F6).copy(alpha = 0.05f))
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.6f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF10B981), CircleShape)
                        .border(1.5.dp, Color(0xFF00F0FF), CircleShape)
                )
                Text(
                    text = "MAGYARORSZÁGI PILÓTA KÖZÖSSÉG",
                    color = Color(0xFF00F0FF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            // Interactive Live Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier
                        .size(16.dp)
                        .offset(y = floatOffset.dp)
                )
                Text(
                    text = "ADÁSVÉTEL",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // MAIN HEADLINE: Bold, open display typography without enclosing box
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Magyarországi Drónosok",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Közösségi Adásvételi Felülete",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF00F0FF), Color(0xFF38BDF8), Color(0xFF818CF8))
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }

        // DYNAMIC HUD SEPARATOR LINE: Glowing radar sweep effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            // Base subtle line
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00F0FF).copy(alpha = 0.5f),
                                Color(0xFF38BDF8).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Scanning laser glow dot
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.25f)
                    .offset(x = (radarPulse * 280).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFF00F0FF), Color.Transparent)
                        )
                    )
            )
        }

        // SUBTITLE & QUICK PILLS
        Text(
            text = "Vásárolj közvetlenül hazai pilótáktól: bevizsgált DJI kamerás drónok, épített FPV versenyrendszerek és tartozékok megbízható forrásból.",
            color = Color(0xFF94A3B8),
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        )

        // TAGS / FAST FILTERS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarketplaceFeaturePill(text = "🛡️ Garanciális DJI", color = Color(0xFF00F0FF))
            MarketplaceFeaturePill(text = "⚡ FPV Versenygépek", color = Color(0xFFF59E0B))
            MarketplaceFeaturePill(text = "📦 Gyári Tartozékok", color = Color(0xFF10B981))
        }
    }
}

@Composable
private fun MarketplaceFeaturePill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = Color(0xFFE2E8F0),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
        )
    }
}

/**
 * High-Tech Standout Banner Card for Drone Price Estimator
 */
@Composable
private fun MarketplacePriceEstimatorCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("marketplace_price_estimator_banner"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091424)),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(Color(0xFF00F0FF), Color(0xFF10B981), Color(0xFF3B82F6))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF00F0FF).copy(alpha = 0.15f),
                            Color(0xFF10B981).copy(alpha = 0.08f),
                            Color(0xFF091424)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F0FF).copy(alpha = 0.3f), Color(0xFF10B981).copy(alpha = 0.2f))
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, Color(0xFF00F0FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "DRÓN ÉRTÉKBECSLŐ",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Mennyit ér a drónod a magyar használtpiacon? Azonnali, pontos árkalkuláció.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample quick valuation chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickPriceSampleBadge(name = "DJI Mini 4 Pro", price = "~215.000 Ft", color = Color(0xFF00F0FF))
                QuickPriceSampleBadge(name = "DJI Air 3", price = "~285.000 Ft", color = Color(0xFF10B981))
                QuickPriceSampleBadge(name = "DJI Avata 2", price = "~235.000 Ft", color = Color(0xFFF59E0B))
                QuickPriceSampleBadge(name = "DJI Mini 2 SE", price = "~85.000 Ft", color = Color(0xFF38BDF8))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Full-width CTA button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00F0FF),
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Árkalkuláció indítása a te drónodra ➔",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun QuickPriceSampleBadge(
    name: String,
    price: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(Color(0xFF131D2E), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = price,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Safety Tips Banner with an Interactive Drone Pulling Animation
 * When tapped, an animated drone flies in, attaches tow lines, drags the card out to the right,
 * and then the drone pulls the dedicated Safety Tips view down from the top!
 * When returning from Safety Tips, the drone pulls the card from the right back to the left into place!
 */
@Composable
private fun MarketplaceSafetyTipsBannerWithDroneAnimation(
    shouldReturnBanner: Boolean = false,
    onReturnAnimationFinished: () -> Unit = {},
    onOpenSafetyTips: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPullingAway by remember { mutableStateOf(false) }
    var isPullingBack by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(0f) }
    val returnAnimProgress = remember { Animatable(0f) }

    LaunchedEffect(shouldReturnBanner) {
        if (shouldReturnBanner) {
            isPullingBack = true
            returnAnimProgress.snapTo(0f)
            returnAnimProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
            isPullingBack = false
            onReturnAnimationFinished()
        }
    }

    // Infinite propeller rotation for active drone
    val infiniteTransition = rememberInfiniteTransition(label = "drone_propeller")
    val propAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "prop_angle"
    )

    // Strobe navigation light pulsation
    val strobeGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("marketplace_safety_tips_banner_container")
    ) {
        val pullProgress = animProgress.value
        val retProgress = returnAnimProgress.value

        // Phase 1: Card Translation & Rotation as the drone pulls it away to the RIGHT
        // Phase 2 (Return): Drone pulls it back from the RIGHT to the LEFT into original position
        val cardTranslationX = if (isPullingAway) {
            if (pullProgress <= 0.25f) {
                // Hook tension micro-shake
                sin(pullProgress * 50f) * 6f
            } else {
                val pullRatio = ((pullProgress - 0.25f) / 0.75f).coerceIn(0f, 1f)
                pullRatio * 1800f // Pull completely out to the right
            }
        } else if (isPullingBack) {
            if (retProgress <= 0.75f) {
                val p = (retProgress / 0.75f).coerceIn(0f, 1f)
                (1f - p) * 1400f // Slide in from the right to the left
            } else {
                0f
            }
        } else 0f

        val cardRotationZ = if (isPullingAway && pullProgress > 0.25f) {
            val pullRatio = ((pullProgress - 0.25f) / 0.75f).coerceIn(0f, 1f)
            pullRatio * 12f
        } else if (isPullingBack && retProgress <= 0.75f) {
            val p = (retProgress / 0.75f).coerceIn(0f, 1f)
            (1f - p) * (-8f)
        } else 0f

        val cardAlpha = if (isPullingAway && pullProgress > 0.25f) {
            val pullRatio = ((pullProgress - 0.25f) / 0.75f).coerceIn(0f, 1f)
            (1f - (pullRatio * 0.9f)).coerceIn(0f, 1f)
        } else if (isPullingBack && retProgress <= 0.75f) {
            val p = (retProgress / 0.75f).coerceIn(0f, 1f)
            (0.35f + 0.65f * p).coerceIn(0f, 1f)
        } else 1f

        // The Safety Tips Card itself
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = cardTranslationX
                    rotationZ = cardRotationZ
                    alpha = cardAlpha
                }
                .clip(RoundedCornerShape(18.dp))
                .clickable(enabled = !isPullingAway && !isPullingBack) {
                    coroutineScope.launch {
                        isPullingAway = true
                        animProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
                        )
                        onOpenSafetyTips()
                        animProgress.snapTo(0f)
                        isPullingAway = false
                    }
                }
                .testTag("marketplace_safety_tips_bubble"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2F)),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFF00F0FF), Color(0xFF10B981))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.15f),
                                Color(0xFF00F0FF).copy(alpha = 0.08f),
                                Color(0xFF131D2F)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape)
                                .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "BIZTONSÁGI TIPPEK ADÁSVÉTELHEZ",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPullingAway) "🚀 A drón elhúzza a panelt jobbra..."
                                else if (isPullingBack) "🛬 A drón visszahúzza a panelt balra a helyére..."
                                else "Koppints ide: a kis drón elhúzza jobbra, majd fentről behozza a tippeket! ➔",
                                color = if (isPullingAway || isPullingBack) Color(0xFF00F0FF) else Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = if (isPullingAway || isPullingBack) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Mini Drone Icon Badge on the Card
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF00F0FF).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Kattints",
                                color = Color(0xFF00F0FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniSafetyBadge(text = "🔓 DJI Unbind", color = Color(0xFF00F0FF))
                    MiniSafetyBadge(text = "🔋 Akku Ciklus", color = Color(0xFFF59E0B))
                    MiniSafetyBadge(text = "📜 Adásvételi Minta", color = Color(0xFF10B981))
                }
            }
        }

        // =========================================================================
        // ANIMATED TOWING DRONE OVERLAY (PULL TO RIGHT OR PULL BACK TO LEFT)
        // =========================================================================
        if (isPullingAway || isPullingBack) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { clip = false }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Compute Drone X & Y position
                val droneX: Float
                val droneY: Float

                if (isPullingAway) {
                    if (pullProgress <= 0.25f) {
                        val p = pullProgress / 0.25f
                        // Swoop down from top-left to card top
                        droneX = -100f + p * (canvasWidth * 0.7f + 100f)
                        droneY = -80f + p * 60f
                    } else {
                        val p = ((pullProgress - 0.25f) / 0.75f).coerceIn(0f, 1f)
                        // Accelerate to the RIGHT pulling the card
                        droneX = canvasWidth * 0.7f + p * (canvasWidth * 1.8f)
                        droneY = -20f - p * 20f
                    }

                    // 1. Draw glowing Tow Cables connecting Drone to Card
                    if (pullProgress > 0.15f) {
                        val cableStartDrone = Offset(droneX, droneY + 16f)
                        val cableEndLeft = Offset(cardTranslationX + 40f, canvasHeight * 0.35f)
                        val cableEndRight = Offset(cardTranslationX + canvasWidth * 0.6f, canvasHeight * 0.35f)

                        // Laser cable glow
                        drawLine(
                            color = Color(0xFF00F0FF).copy(alpha = 0.9f),
                            start = cableStartDrone,
                            end = cableEndLeft,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                            start = cableStartDrone,
                            end = cableEndRight,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Energy sparks at connection points
                        drawCircle(
                            color = Color(0xFF00F0FF),
                            radius = 4.dp.toPx(),
                            center = cableEndLeft
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = 4.dp.toPx(),
                            center = cableEndRight
                        )
                    }

                    // 2. Draw Jet Exhaust Trail behind Drone when pulling right
                    if (pullProgress > 0.25f) {
                        val trailLength = 90.dp.toPx()
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0xFF00F0FF).copy(alpha = 0.85f))
                            ),
                            start = Offset(droneX - trailLength, droneY),
                            end = Offset(droneX - 25f, droneY),
                            strokeWidth = 5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0xFFF59E0B).copy(alpha = 0.75f))
                            ),
                            start = Offset(droneX - trailLength * 0.7f, droneY + 7f),
                            end = Offset(droneX - 25f, droneY + 7f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    // PULLING BACK: Drone pulls the card from the RIGHT back to the LEFT into place
                    if (retProgress <= 0.75f) {
                        // Drone leads on the left side of the card, pulling it towards left
                        droneX = cardTranslationX - 45.dp.toPx()
                        droneY = -15f

                        val cableStartDrone = Offset(droneX + 18f, droneY + 16f)
                        val cableEndLeft = Offset(cardTranslationX + 30f, canvasHeight * 0.35f)
                        val cableEndRight = Offset(cardTranslationX + canvasWidth * 0.5f, canvasHeight * 0.35f)

                        drawLine(
                            color = Color(0xFF00F0FF).copy(alpha = 0.9f),
                            start = cableStartDrone,
                            end = cableEndLeft,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                            start = cableStartDrone,
                            end = cableEndRight,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawCircle(color = Color(0xFF00F0FF), radius = 4.dp.toPx(), center = cableEndLeft)
                        drawCircle(color = Color(0xFFF59E0B), radius = 4.dp.toPx(), center = cableEndRight)

                        // Jet exhaust pointing to the RIGHT (behind the drone as it pulls LEFT)
                        val trailLength = 80.dp.toPx()
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF).copy(alpha = 0.85f), Color.Transparent)
                            ),
                            start = Offset(droneX + 25f, droneY),
                            end = Offset(droneX + 25f + trailLength, droneY),
                            strokeWidth = 5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFF59E0B).copy(alpha = 0.75f), Color.Transparent)
                            ),
                            start = Offset(droneX + 25f, droneY + 7f),
                            end = Offset(droneX + 25f + trailLength * 0.7f, droneY + 7f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    } else {
                        // Release & Fly Off to the LEFT
                        val exitP = ((retProgress - 0.75f) / 0.25f).coerceIn(0f, 1f)
                        droneX = -45.dp.toPx() - exitP * (canvasWidth * 0.8f)
                        droneY = -15f - exitP * 30f

                        val sparkAlpha = (1f - exitP).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFF00F0FF).copy(alpha = sparkAlpha),
                            radius = 6.dp.toPx() * (1f + exitP * 2f),
                            center = Offset(30f, canvasHeight * 0.35f)
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = sparkAlpha),
                            radius = 6.dp.toPx() * (1f + exitP * 2f),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.35f)
                        )

                        val trailLength = 65.dp.toPx()
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF).copy(alpha = 0.75f * (1f - exitP)), Color.Transparent)
                            ),
                            start = Offset(droneX + 25f, droneY),
                            end = Offset(droneX + 25f + trailLength, droneY),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 3. Draw The Quadcopter Drone
                drawQuadcopterDrone(
                    droneX = droneX,
                    droneY = droneY,
                    propAngle = propAngle,
                    strobeGlow = strobeGlow
                )
            }
        }
    }
}

/**
 * Shared Quadcopter Drone Drawing routine for realistic drone visualization
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawQuadcopterDrone(
    droneX: Float,
    droneY: Float,
    propAngle: Float,
    strobeGlow: Float,
    scaleFactor: Float = 1f
) {
    val center = Offset(droneX, droneY)

    // Central Core Frame
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 16.dp.toPx() * scaleFactor,
        center = center
    )
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = 16.dp.toPx() * scaleFactor,
        center = center,
        style = Stroke(width = 2.dp.toPx() * scaleFactor)
    )

    // Center Cockpit / Sensor Eye
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = 6.dp.toPx() * scaleFactor,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx() * scaleFactor,
        center = center
    )

    // 4 Motor Arms
    val armOffsets = listOf(
        Offset(-22f * scaleFactor, -16f * scaleFactor),
        Offset(22f * scaleFactor, -16f * scaleFactor),
        Offset(-22f * scaleFactor, 16f * scaleFactor),
        Offset(22f * scaleFactor, 16f * scaleFactor)
    )

    armOffsets.forEachIndexed { index, armOffset ->
        val motorCenter = Offset(droneX + armOffset.x, droneY + armOffset.y)
        // Arm carbon rod
        drawLine(
            color = Color(0xFF334155),
            start = center,
            end = motorCenter,
            strokeWidth = 3.dp.toPx() * scaleFactor,
            cap = StrokeCap.Round
        )
        // Motor bell
        drawCircle(
            color = Color(0xFF1E293B),
            radius = 5.dp.toPx() * scaleFactor,
            center = motorCenter
        )
        // Navigation LEDs (Green front, Red rear)
        val ledColor = if (index < 2) Color(0xFF10B981) else Color(0xFFEF4444)
        drawCircle(
            color = ledColor.copy(alpha = strobeGlow),
            radius = 3.dp.toPx() * scaleFactor,
            center = motorCenter
        )

        // Spinning Propeller Disc
        val propRadius = 13.dp.toPx() * scaleFactor
        val rad = Math.toRadians((propAngle + index * 90).toDouble())
        val p1 = Offset(
            motorCenter.x + (cos(rad) * propRadius).toFloat(),
            motorCenter.y + (sin(rad) * propRadius).toFloat()
        )
        val p2 = Offset(
            motorCenter.x - (cos(rad) * propRadius).toFloat(),
            motorCenter.y - (sin(rad) * propRadius).toFloat()
        )
        drawLine(
            color = Color(0xFF00F0FF).copy(alpha = 0.75f),
            start = p1,
            end = p2,
            strokeWidth = 2.dp.toPx() * scaleFactor,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFF00F0FF).copy(alpha = 0.2f),
            radius = propRadius,
            center = motorCenter
        )
    }
}

@Composable
private fun MiniSafetyBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(Color(0xFF0B1320), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Full Screen Dedicated Safety Tips & Inspection Checklist Page
 * Triggered when the drone drags the safety tips window away to the right,
 * and enters with the drone pulling the view down from the top of the screen!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceSafetyTipsFullScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isExiting by remember { mutableStateOf(false) }
    val exitAnim = remember { Animatable(0f) }

    val handleBack: () -> Unit = {
        if (!isExiting) {
            isExiting = true
            coroutineScope.launch {
                exitAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
                )
                onBack()
            }
        }
    }

    BackHandler(enabled = !isExiting) {
        handleBack()
    }

    // Entry animation: The drone pulls the view DOWN all the way to the bottom
    val entryAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entryAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing)
        )
    }

    // Infinite propeller rotation for active drone
    val infiniteTransition = rememberInfiniteTransition(label = "drone_fullscreen_propeller")
    val propAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "prop_angle_full"
    )

    val strobeGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_glow_full"
    )

    // Interactive Checklist items that pilot can tick off during physical inspection
    var checkUnbind by remember { mutableStateOf(false) }
    var checkEasa by remember { mutableStateOf(false) }
    var checkBatteryCells by remember { mutableStateOf(false) }
    var checkBatteryCycles by remember { mutableStateOf(false) }
    var checkFlightTest by remember { mutableStateOf(false) }
    var checkGimbalSensors by remember { mutableStateOf(false) }
    var checkContract by remember { mutableStateOf(false) }

    val checkedCount = listOf(
        checkUnbind,
        checkEasa,
        checkBatteryCells,
        checkBatteryCycles,
        checkFlightTest,
        checkGimbalSensors,
        checkContract
    ).count { it }

    val totalChecks = 7
    val checkProgress = checkedCount.toFloat() / totalChecks.toFloat()

    val progress = entryAnim.value
    val exitProgress = exitAnim.value

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        val screenHeightPx = constraints.maxHeight.toFloat()
        val screenWidthPx = constraints.maxWidth.toFloat()

        // Translation calculation: Pull down on entry, pull back UP on back/exit
        val sheetTranslationY = if (isExiting) {
            if (exitProgress < 0.2f) {
                0f
            } else {
                val p = ((exitProgress - 0.2f) / 0.8f).coerceIn(0f, 1f)
                -screenHeightPx * p
            }
        } else {
            if (progress < 0.75f) {
                val p = (progress / 0.75f).coerceIn(0f, 1f)
                -screenHeightPx * (1f - p)
            } else {
                0f
            }
        }

        Scaffold(
            containerColor = Color(0xFF0A0F1D),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = sheetTranslationY
                },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Biztonsági Tippek & Kisokos",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Megbízható drónos adásvételi útmutató",
                                color = Color(0xFF00F0FF),
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = handleBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Vissza a piactérhez",
                                tint = Color(0xFF00F0FF)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("marketplace_safety_tips_fullscreen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Hero Banner with Mascot Drone
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131F33)),
                        border = BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFFF59E0B))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFF00F0FF).copy(alpha = 0.2f), CircleShape)
                                    .border(2.dp, Color(0xFF00F0FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ADÁSVÉTELI BIZTONSÁG",
                                color = Color(0xFFF59E0B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Kerüld el a csalókat és a hibás gépeket!",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Használd az alábbi ellenőrzőlistát és útmutatókat akár a helyszíni próbarepülés közben.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Interactive On-Site Inspection Checklist Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111C2E)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (checkedCount == totalChecks) Color(0xFF10B981) else Color(0xFF00F0FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Helyszíni Átvételi Csekklista",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "$checkedCount / $totalChecks kész",
                                color = if (checkedCount == totalChecks) Color(0xFF10B981) else Color(0xFF00F0FF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Linear Progress Indicator
                        LinearProgressIndicator(
                            progress = { checkProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (checkedCount == totalChecks) Color(0xFF10B981) else Color(0xFF00F0FF),
                            trackColor = Color(0xFF1E293B)
                        )

                        if (checkedCount == totalChecks) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Minden pont ellenőrizve! A gép biztonságosan megvásárolható.",
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B))

                        // Checklist Items
                        SafetyCheckItemRow(
                            checked = checkUnbind,
                            onCheckedChange = { checkUnbind = it },
                            title = "1. DJI / Autel fiók lecsatolva (Unbind)",
                            desc = "Az eladó szétkapcsolta a gép és a távirányító kötését a DJI Fly appban."
                        )

                        SafetyCheckItemRow(
                            checked = checkEasa,
                            onCheckedChange = { checkEasa = it },
                            title = "2. EASA regisztrációs matrica eltávolítva",
                            desc = "A korábbi tulajdonos HUN... üzembentartói matricája lekerült a vázról."
                        )

                        SafetyCheckItemRow(
                            checked = checkBatteryCells,
                            onCheckedChange = { checkBatteryCells = it },
                            title = "3. Akku cellafeszültségek egyenletesek",
                            desc = "A cellák közötti maximális eltérés 0.03V alatt van (pl. 4.18V - 4.19V)."
                        )

                        SafetyCheckItemRow(
                            checked = checkBatteryCycles,
                            onCheckedChange = { checkBatteryCycles = it },
                            title = "4. Nincs akkumulátor púposodás & ciklus OK",
                            desc = "Az akkuk nem feszülnek a foglalatban, a ciklusszám a hirdetés szerinti."
                        )

                        SafetyCheckItemRow(
                            checked = checkFlightTest,
                            onCheckedChange = { checkFlightTest = it },
                            title = "5. Próbarepülés: GPS lock & RTH teszt",
                            desc = "12+ műhold rögzítve, szélben is stabil lebegés, automatikus hazatérés működik."
                        )

                        SafetyCheckItemRow(
                            checked = checkGimbalSensors,
                            onCheckedChange = { checkGimbalSensors = it },
                            title = "6. Gimbal & Szenzorok elakadásmentesek",
                            desc = "Nem rezeg a kép, a kamera vízszintez, az akadályérzékelők észlelnek."
                        )

                        SafetyCheckItemRow(
                            checked = checkContract,
                            onCheckedChange = { checkContract = it },
                            title = "7. Írásos adásvételi szerződés kitöltve",
                            desc = "Szerepel rajta a drón és a távirányító pontos gyári sorozatszáma (SN)."
                        )
                    }
                }
            }

            // Detailed Tip 1: Unbind Guide
            item {
                DetailedSafetyGuideCard(
                    icon = Icons.Default.LinkOff,
                    iconTint = Color(0xFF00F0FF),
                    title = "1. DJI Fiók Lecsatolása (Unbind Device) – KRITIKUS!",
                    content = "A leggyakoribb hiba használt drón vásárlásakor! Ha az eladó nem csatolja le a drónt a DJI Fly appban a saját fiókjáról, akkor a vevő hiába csatlakoztatja, a DJI Flyaway garancia a régi tulajdonoshoz marad kötve, és bármikor letilthatja a gépet.\n\n" +
                            "Lépések az eladónak:\n" +
                            "1. Nyisd meg a DJI Fly appot ➔ Profil ➔ Eszközkezelés (Device Management)\n" +
                            "2. Válaszd ki a drónt ➔ 'Fiók szétkapcsolása' (Unbind Account)\n" +
                            "3. Ugyanitt csatold le a távirányítót is (Unbind Remote Controller)\n" +
                            "4. A vevő ezután a saját DJI fiókjával jelentkezzen be!"
                )
            }

            // Detailed Tip 2: Battery Health & Diagnostics
            item {
                DetailedSafetyGuideCard(
                    icon = Icons.Default.BatteryAlert,
                    iconTint = Color(0xFFF59E0B),
                    title = "2. Akkumulátor Diagnosztika & Ciklusszám",
                    content = "Az intelligens repülési akkumulátorok a drón legdrágább fogyóeszközei. Mindig vizsgáld meg fizikailag és a szoftverben is:\n\n" +
                            "• Púposodás vizsgálata: Ha az akku nehezen csúszik be a drón vázába, vagy a műanyag burkolat ki van dudorodva, AZONNAL SELEJTEZNI KELL, mert repülés közben leállhat!\n" +
                            "• Ciklusszám: 0-50 ciklus (újszerű), 50-120 ciklus (jó állapot), 150+ ciklus (csökkent repülési idő).\n" +
                            "• Cellafeszültség delta: Teli töltöttségnél a cellák közötti eltérés maximum 0.03V lehet. 0.08V feletti eltérésnél a cella hibás."
                )
            }

            // Detailed Tip 3: Flight & Hardware Test
            item {
                DetailedSafetyGuideCard(
                    icon = Icons.Default.FlightTakeoff,
                    iconTint = Color(0xFF10B981),
                    title = "3. Személyes Átvétel & Próbarepülés",
                    content = "Soha ne vásárolj használt drónt kizárólag postán, kipróbálás nélkül! Kérj 5 perces próbarepülést egy nyílt területen:\n\n" +
                            "• Várjátok meg a 12+ GPS műhold lockot és a Home Point frissítését.\n" +
                            "• Emelkedj 3 méter magasra, engedd el a karokat: a gépnek egy helyben kell lebegnie sodródás nélkül.\n" +
                            "• Teszteld a gimbal függőleges mozgását és forgását.\n" +
                            "• Forgass egy 4K videót és ellenőrizd az SD kártyán a kép tisztaságát és a remegésmentességet."
                )
            }

            // Detailed Tip 4: Bill of Sale & Serial Number
            item {
                DetailedSafetyGuideCard(
                    icon = Icons.Default.Description,
                    iconTint = Color(0xFF3B82F6),
                    title = "4. Írásos Adásvételi Szerződés & Gyári Szám (SN)",
                    content = "A drón légijárműnek minősül, ezért elengedhetetlen a tulajdonjog tiszta igazolása:\n\n" +
                            "• Ellenőrizd a gép testén (általában az akkumulátorfoglalatban vagy a karon lévő QR-kód mellett) a gyári sorozatszámot (Serial Number), és hasonlítsd össze a dobozon és a DJI appban lévő számmal!\n" +
                            "• Töltsétek ki a 2 tanús adásvételi szerződést mindkét fél adataival és a sorozatszámmal.\n" +
                            "• Ezzel véded magad a lopott gépekkel és az utólagos vitákkal szemben."
                )
            }

            // Detailed Tip 5: FPV Drone Inspection
            item {
                DetailedSafetyGuideCard(
                    icon = Icons.Default.Build,
                    iconTint = Color(0xFFA855F7),
                    title = "5. FPV és Egyedi Építésű Versenygépek",
                    content = "Ha épített 5 colos vagy CineWhoop FPV drónt vásárolsz:\n\n" +
                            "• Váz ellenőrzése: Nincs-e rétegleválás (delamináció) a szénszálas karokon egy korábbi zuhanás miatt.\n" +
                            "• Forrasztások: Fényes, tiszta forrasztási pontok a motorok és az ESC között (nem hidegforrasztás).\n" +
                            "• VTX és Antenna: Nem volt-e bekapcsolva antenna nélkül az adó (mert ilyenkor a VTX chip azonnal leéghet).\n" +
                            "• Kérd el az eladótól az aktuális Betaflight CLI beállítások mentését!"
                )
            }

            // Bottom CTA Button to go back to listings
            item {
                Button(
                    onClick = handleBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vissza a Piactér Hirdetéseihez",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // =========================================================================
    // ANIMATED DRONE OVERLAY (ENTRY PULL-DOWN & EXIT PULL-UP)
    // =========================================================================
    if (progress < 1f || isExiting) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val droneX: Float
            val droneY: Float

            if (isExiting) {
                if (exitProgress < 0.2f) {
                    // Drone swoops in from top to hook up to the top of the sheet
                    val enterP = (exitProgress / 0.2f).coerceIn(0f, 1f)
                    droneX = canvasWidth / 2f
                    droneY = -60f + enterP * (60f + 45.dp.toPx())

                    val cableStartDrone = Offset(droneX, droneY + 16f)
                    val cableLeft = Offset(canvasWidth * 0.18f, 0f)
                    val cableRight = Offset(canvasWidth * 0.82f, 0f)

                    if (enterP > 0.3f) {
                        drawLine(
                            color = Color(0xFF00F0FF).copy(alpha = 0.9f),
                            start = cableStartDrone,
                            end = cableLeft,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                            start = cableStartDrone,
                            end = cableRight,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(color = Color(0xFF00F0FF), radius = 5.dp.toPx(), center = cableLeft)
                        drawCircle(color = Color(0xFFF59E0B), radius = 5.dp.toPx(), center = cableRight)
                    }
                } else {
                    // Drone pulls the entire sheet UP out of the top of the screen
                    droneX = canvasWidth / 2f
                    val currentTopEdge = sheetTranslationY
                    droneY = currentTopEdge + 45.dp.toPx()

                    val cableStartDrone = Offset(droneX, droneY + 16f)
                    val cableLeft = Offset(canvasWidth * 0.18f, currentTopEdge + 15.dp.toPx())
                    val cableRight = Offset(canvasWidth * 0.82f, currentTopEdge + 15.dp.toPx())

                    drawLine(
                        color = Color(0xFF00F0FF).copy(alpha = 0.9f),
                        start = cableStartDrone,
                        end = cableLeft,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                        start = cableStartDrone,
                        end = cableRight,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color(0xFF00F0FF), radius = 5.dp.toPx(), center = cableLeft)
                    drawCircle(color = Color(0xFFF59E0B), radius = 5.dp.toPx(), center = cableRight)

                    // Powerful downward jet thrust to propel the drone and sheet upwards
                    val thrustLen = 65.dp.toPx()
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.9f), Color.Transparent)
                        ),
                        start = Offset(droneX - 14f, droneY + 16f),
                        end = Offset(droneX - 14f, droneY + thrustLen),
                        strokeWidth = 4.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.9f), Color.Transparent)
                        ),
                        start = Offset(droneX + 14f, droneY + 16f),
                        end = Offset(droneX + 14f, droneY + thrustLen),
                        strokeWidth = 4.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            } else {
                if (progress < 0.75f) {
                    val p = (progress / 0.75f).coerceIn(0f, 1f)
                    val currentBottomEdge = sheetTranslationY + canvasHeight
                    
                    droneX = canvasWidth / 2f
                    // The drone leads the lowering sheet, situated right at the descending bottom edge
                    droneY = (currentBottomEdge - 45.dp.toPx()).coerceAtLeast(30.dp.toPx())

                    // Draw Tow Cables holding the bottom of the descending sheet
                    val cableStartDrone = Offset(droneX, droneY - 14f)
                    val cableLeft = Offset(canvasWidth * 0.18f, currentBottomEdge)
                    val cableRight = Offset(canvasWidth * 0.82f, currentBottomEdge)

                    drawLine(
                        color = Color(0xFF00F0FF).copy(alpha = 0.9f),
                        start = cableStartDrone,
                        end = cableLeft,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                        start = cableStartDrone,
                        end = cableRight,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Cable attachment glowing anchor points
                    drawCircle(color = Color(0xFF00F0FF), radius = 5.dp.toPx(), center = cableLeft)
                    drawCircle(color = Color(0xFFF59E0B), radius = 5.dp.toPx(), center = cableRight)

                    // Downward jet thrust exhaust trails as the drone powers downward
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.85f), Color.Transparent)
                        ),
                        start = Offset(droneX - 14f, droneY + 16f),
                        end = Offset(droneX - 14f, droneY + 55f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF00F0FF).copy(alpha = 0.85f), Color.Transparent)
                        ),
                        start = Offset(droneX + 14f, droneY + 16f),
                        end = Offset(droneX + 14f, droneY + 55f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Detachment & Exit Phase: Drone reached the bottom, releases cables and flies away
                    val exitP = ((progress - 0.75f) / 0.25f).coerceIn(0f, 1f)
                    val baseBottomY = canvasHeight - 45.dp.toPx()
                    
                    droneX = canvasWidth / 2f + exitP * (canvasWidth * 0.75f)
                    droneY = baseBottomY + exitP * 120.dp.toPx()

                    // Quick detachment sparks
                    val sparkAlpha = (1f - exitP).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color(0xFF00F0FF).copy(alpha = sparkAlpha),
                        radius = (8.dp.toPx() * (1f + exitP * 2f)),
                        center = Offset(canvasWidth * 0.18f, canvasHeight)
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = sparkAlpha),
                        radius = (8.dp.toPx() * (1f + exitP * 2f)),
                        center = Offset(canvasWidth * 0.82f, canvasHeight)
                    )
                }
            }

            // Draw Quadcopter Drone
            drawQuadcopterDrone(
                droneX = droneX,
                droneY = droneY,
                propAngle = propAngle,
                strobeGlow = strobeGlow,
                scaleFactor = 1.15f
            )
        }
    }
}
}

@Composable
private fun SafetyCheckItemRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .background(if (checked) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFF0B1320))
            .border(
                1.dp,
                if (checked) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF1E293B),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF10B981),
                checkmarkColor = Color(0xFF0F172A),
                uncheckedColor = Color(0xFF64748B)
            ),
            modifier = Modifier.size(24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = if (checked) Color(0xFF10B981) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = desc,
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun DetailedSafetyGuideCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101A2C)),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconTint.copy(alpha = 0.18f), CircleShape)
                        .border(1.dp, iconTint.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = content,
                color = Color(0xFFCBD5E1),
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Individual Marketplace Card Component
 */
@Composable
private fun MarketplaceListingCard(
    listing: MarketplaceListing,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    onQuickContact: () -> Unit
) {
    val isForSale = listing.type == "ELADÁS"
    val formattedPrice = remember(listing.price) {
        if (listing.price <= 0) "Megegyezés szerint"
        else "${NumberFormat.getNumberInstance(Locale("hu", "HU")).format(listing.price)} Ft"
    }

    val imageList = remember(listing.imageUri) { listing.getImageUris() }
    var activeImageIndex by remember(listing.imageUri) { mutableIntStateOf(0) }
    var showZoomViewer by remember { mutableStateOf(false) }

    if (showZoomViewer && imageList.isNotEmpty()) {
        FullscreenImageViewerModal(
            images = imageList,
            initialIndex = activeImageIndex.coerceIn(0, imageList.size - 1),
            onDismiss = { showZoomViewer = false }
        )
    }

    val timeString = remember(listing.timestamp) {
        val diffHours = (System.currentTimeMillis() - listing.timestamp) / (1000 * 60 * 60)
        when {
            diffHours < 1 -> "Nemrég feladva"
            diffHours < 24 -> "${diffHours} órája"
            else -> SimpleDateFormat("yyyy.MM.dd", Locale("hu", "HU")).format(Date(listing.timestamp))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("marketplace_listing_card_${listing.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isForSale) Color(0xFF06B6D4).copy(alpha = 0.35f) else Color(0xFF3B82F6).copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Type Badge + Category + Timestamp + Optional Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isForSale) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isForSale) Color(0xFF10B981) else Color(0xFF3B82F6)
                        )
                    ) {
                        Text(
                            text = if (isForSale) "ELADÓ" else "KERESEM / VÉTEL",
                            color = if (isForSale) Color(0xFF10B981) else Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF334155)
                    ) {
                        Text(
                            text = listing.category,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // User Created Badge
                    if (listing.isUserCreated) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                        ) {
                            Text(
                                text = "Saját hirdetés",
                                color = Color(0xFFF59E0B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeString,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )

                    if (onDeleteClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Törlés",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Title
            Text(
                text = listing.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Listing Images (Multi-image preview & Zoom)
            if (imageList.isNotEmpty()) {
                val currentPhoto = imageList[activeImageIndex.coerceIn(0, imageList.size - 1)]
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { showZoomViewer = true },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = currentPhoto,
                            contentDescription = listing.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top-End Zoom Badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clickable { showZoomViewer = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Nagyítás",
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Nagyítás",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Bottom badge overlay (Photo count)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (imageList.size > 1) "${activeImageIndex + 1} / ${imageList.size} fotó" else "Fénykép",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Multi-image Thumbnail Selector Row on Card
                    if (imageList.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            imageList.forEachIndexed { index, imgUrl ->
                                val isSelected = index == activeImageIndex
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeImageIndex = index }
                                ) {
                                    AsyncImage(
                                        model = imgUrl,
                                        contentDescription = "Fotó $index",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Price & Condition Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedPrice,
                    color = if (isForSale) Color(0xFF00F0FF) else Color(0xFF60A5FA),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = listing.condition,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Highlights (Battery / Accessories summary if present)
            if (!listing.batteryCycles.isNullOrBlank() || !listing.accessories.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!listing.batteryCycles.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = listing.batteryCycles,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (!listing.accessories.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = listing.accessories,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Footer Row: Location & Seller & Contact Button
            Divider(color = Color(0xFF334155).copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.location,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Eladó: ${listing.sellerName}",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 18.dp)
                    )
                }

                // Quick Call Button
                FilledTonalButton(
                    onClick = onQuickContact,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF00F0FF).copy(alpha = 0.15f),
                        contentColor = Color(0xFF00F0FF)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Hívás",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kapcsolat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Detailed Modal Dialog for a Selected Listing
 */
@Composable
fun MarketplaceDetailDialog(
    listing: MarketplaceListing,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    val context = LocalContext.current
    val isForSale = listing.type == "ELADÁS"
    val formattedPrice = remember(listing.price) {
        if (listing.price <= 0) "Megegyezés szerint"
        else "${NumberFormat.getNumberInstance(Locale("hu", "HU")).format(listing.price)} Ft"
    }

    val imageList = remember(listing.imageUri) { listing.getImageUris() }
    var selectedPhotoIndex by remember(listing.imageUri) { mutableIntStateOf(0) }
    var showDetailZoomModal by remember { mutableStateOf(false) }

    if (showDetailZoomModal && imageList.isNotEmpty()) {
        FullscreenImageViewerModal(
            images = imageList,
            initialIndex = selectedPhotoIndex.coerceIn(0, imageList.size - 1),
            onDismiss = { showDetailZoomModal = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appTheme.surfaceColor,
        shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isForSale) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isForSale) Color(0xFF10B981) else Color(0xFF3B82F6)
                            )
                        ) {
                            Text(
                                text = if (isForSale) "ELADÓ" else "KERESEM / VÉTEL",
                                color = if (isForSale) Color(0xFF10B981) else Color(0xFF60A5FA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = listing.category,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_app_drone_logo),
                            contentDescription = "Bezárás",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Images Gallery & Zoom in Details Dialog
                if (imageList.isNotEmpty()) {
                    val currentPhoto = imageList[selectedPhotoIndex.coerceIn(0, imageList.size - 1)]
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { showDetailZoomModal = true },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = currentPhoto,
                                contentDescription = listing.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Tap to Zoom hint overlay
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clickable { showDetailZoomModal = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Kinagyítás",
                                        tint = Color(0xFF00F0FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (imageList.size > 1) "🔍 ${selectedPhotoIndex + 1}/${imageList.size} • Koppints a nagyításhoz" else "🔍 Koppints a nagyításhoz",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Thumbnail carousel if multiple images
                        if (imageList.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                imageList.forEachIndexed { index, imgUrl ->
                                    val isSelected = index == selectedPhotoIndex
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedPhotoIndex = index }
                                    ) {
                                        AsyncImage(
                                            model = imgUrl,
                                            contentDescription = "Részletkép $index",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Title & Price
                Text(
                    text = listing.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formattedPrice,
                    color = Color(0xFF00F0FF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                // Key Specs Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(icon = Icons.Default.CheckCircle, label = "Állapot:", value = listing.condition)
                    DetailRow(icon = Icons.Default.LocationOn, label = "Helyszín:", value = listing.location)
                    DetailRow(icon = Icons.Default.Person, label = "Hirdető:", value = listing.sellerName)
                    if (!listing.batteryCycles.isNullOrBlank()) {
                        DetailRow(icon = Icons.Default.BatteryChargingFull, label = "Akkumulátorok:", value = listing.batteryCycles)
                    }
                }

                // Description
                Text(
                    text = "Részletes leírás:",
                    color = Color(0xFF00F0FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = listing.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Accessories
                if (!listing.accessories.isNullOrBlank()) {
                    Text(
                        text = "Mellékelt tartozékok & felszereltség:",
                        color = Color(0xFF00F0FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = listing.accessories,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                // Contact Actions Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kapcsolatfelvétel az eladóval",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dial Button
                        Button(
                            onClick = { openPhoneDialer(context, listing.contactPhone) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hívás",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // SMS Button
                        OutlinedButton(
                            onClick = { openSms(context, listing.contactPhone, listing.title) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Copy Phone Number
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                copyToClipboard(context, listing.contactPhone, "Telefonszám kimásolva")
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneIphone,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = listing.contactPhone,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Másolás",
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (!listing.contactEmail.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    copyToClipboard(context, listing.contactEmail, "Email cím kimásolva")
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = listing.contactEmail,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Másolás",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saját hirdetés törlése", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Bezárás", color = appTheme.primaryColor, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF00F0FF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Modal to Post a New Listing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMarketplaceListingDialog(
    initialTitle: String = "",
    initialPrice: String = "",
    initialCondition: String = "Újszerű / Garanciális",
    initialAccessories: String = "",
    initialSellerName: String = "",
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        type: String,
        category: String,
        price: Int,
        condition: String,
        location: String,
        description: String,
        batteryCycles: String?,
        accessories: String?,
        sellerName: String,
        phone: String,
        email: String?,
        imageUri: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var type by remember { mutableStateOf("ELADÁS") }
    var category by remember {
        mutableStateOf(
            if (initialTitle.contains("DJI", ignoreCase = true)) "DJI"
            else if (initialTitle.contains("Autel", ignoreCase = true)) "Autel"
            else if (initialTitle.contains("FPV", ignoreCase = true)) "FPV"
            else "DJI"
        )
    }
    var priceText by remember { mutableStateOf(initialPrice) }
    var condition by remember { mutableStateOf(initialCondition) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var batteryCycles by remember { mutableStateOf("") }
    var accessories by remember { mutableStateOf(initialAccessories) }
    var sellerName by remember { mutableStateOf(initialSellerName) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(initialEmail) }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var zoomPreviewIndex by remember { mutableStateOf<Int?>(null) }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val newUris = uris.map { it.toString() }
        selectedImages = (selectedImages + newUris).distinct().take(8)
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (!selectedImages.contains(it.toString())) {
                selectedImages = (selectedImages + it.toString()).take(8)
            }
        }
    }

    if (zoomPreviewIndex != null && selectedImages.isNotEmpty()) {
        FullscreenImageViewerModal(
            images = selectedImages,
            initialIndex = zoomPreviewIndex!!.coerceIn(0, selectedImages.size - 1),
            onDismiss = { zoomPreviewIndex = null }
        )
    }

    val categories = listOf("DJI", "FPV", "Autel", "Tartozék", "Egyéb")
    val conditions = listOf("Bontatlan új", "Újszerű / Garanciális", "Kiváló állapot", "Használt", "Hibás / Alkatrész")
    val appTheme = com.example.ui.theme.LocalAppTheme.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appTheme.surfaceColor,
        shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Új Hirdetés Feladása",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_app_drone_logo),
                        contentDescription = "Bezárás",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector (Eladás vs Vétel)
                Text("Hirdetés típusa:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "ELADÁS" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "ELADÁS") Color(0xFF10B981) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🟢 Eladni szeretnék", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { type = "VÉTEL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "VÉTEL") Color(0xFF3B82F6) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔵 Keresek / Vétel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Drón / Tartozék megnevezése *") },
                    placeholder = { Text("pl. DJI Mini 4 Pro Fly More Combo...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Photo Upload Section (Multi-image support up to 8 photos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hirdetés fotói (max 8 db):",
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedImages.isNotEmpty()) {
                        Text(
                            text = "${selectedImages.size}/8 fotó csatolva",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (selectedImages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Horizontal list of selected image cards
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectedImages.forEachIndexed { index, imgUrl ->
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(
                                            1.dp,
                                            if (index == 0) Color(0xFF00F0FF) else Color(0xFF334155),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { zoomPreviewIndex = index }
                                ) {
                                    AsyncImage(
                                        model = imgUrl,
                                        contentDescription = "Feltöltött fotó $index",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Order Badge (Főfotó vs #)
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            if (index == 0) Color(0xFF00F0FF) else Color(0xFF64748B)
                                        )
                                    ) {
                                        Text(
                                            text = if (index == 0) "1. Főfotó" else "${index + 1}.",
                                            color = if (index == 0) Color(0xFF00F0FF) else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Delete Single Photo Button
                                    IconButton(
                                        onClick = {
                                            selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color(0xFFEF4444).copy(alpha = 0.9f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Fotó eltávolítása",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Add More Photos Tile if under limit
                            if (selectedImages.size < 8) {
                                Surface(
                                    onClick = { multiPhotoPickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(85.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = Color(0xFF00F0FF),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+ További kép",
                                            color = Color(0xFF00F0FF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Buttons underneath
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { multiPhotoPickerLauncher.launch("image/*") },
                                enabled = selectedImages.size < 8,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.6f)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fotók hozzáadása", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { selectedImages = emptyList() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Összes törlése", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Surface(
                        onClick = { multiPhotoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("marketplace_upload_photo_button")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📷 Fotók Feltöltése a Galériából",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Több képet is kiválaszthatsz egyszerre (max 8 db)",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Sample photo chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Vagy válassz mintafotókat (hozzáadás a galériához):",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "DJI Mini" to "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800&q=80",
                            "FPV Quad" to "https://images.unsplash.com/photo-1508614589041-895b88991e3e?w=800&q=80",
                            "Mavic Pro" to "https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=800&q=80",
                            "Tartozék" to "https://images.unsplash.com/photo-1507582020474-9a35b7d455d9?w=800&q=80",
                            "Kamera & Gimbal" to "https://images.unsplash.com/photo-1533558701576-23c65e0272fb?w=800&q=80"
                        ).forEach { (label, sampleUrl) ->
                            val isAdded = selectedImages.contains(sampleUrl)
                            FilterChip(
                                selected = isAdded,
                                onClick = {
                                    if (isAdded) {
                                        selectedImages = selectedImages.filter { it != sampleUrl }
                                    } else {
                                        if (selectedImages.size < 8) {
                                            selectedImages = selectedImages + sampleUrl
                                        }
                                    }
                                },
                                label = { Text(if (isAdded) "✓ $label" else "+ $label", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(0xFF00F0FF),
                                    containerColor = Color(0xFF1E293B).copy(alpha = 0.6f),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }
                }

                // Category Selector
                Text("Kategória:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00F0FF),
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                // Price (Ft)
                Text("Ár (Ft) *", color = Color(0xFF94A3B8), fontSize = 12.sp)
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) priceText = it },
                    placeholder = { Text("pl. 350000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Condition
                Text("Állapot:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    conditions.forEach { cond ->
                        FilterChip(
                            selected = condition == cond,
                            onClick = { condition = cond },
                            label = { Text(cond, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00F0FF).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00F0FF),
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Város / Átvétel helye *") },
                    placeholder = { Text("pl. Budapest XI. kerület vagy Foxpost...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Battery Cycles (Optional)
                OutlinedTextField(
                    value = batteryCycles,
                    onValueChange = { batteryCycles = it },
                    label = { Text("Akku ciklusszám & állapot (opcionális)") },
                    placeholder = { Text("pl. 12 és 14 ciklus (2 db akku)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Accessories (Optional)
                OutlinedTextField(
                    value = accessories,
                    onValueChange = { accessories = it },
                    label = { Text("Mellékelt tartozékok (opcionális)") },
                    placeholder = { Text("pl. DJI RC 2, hordtáska, ND szűrők, pótlapátok...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Részletes leírás *") },
                    placeholder = { Text("Írd le a drón előéletét, garanciát, esetleges hibákat...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Seller Name
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Neved *") },
                    placeholder = { Text("pl. Kovács Tamás") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Contact Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefonszámod *") },
                    placeholder = { Text("pl. +36 30 123 4567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )

                // Email (Optional)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email címed (opcionális)") },
                    placeholder = { Text("pl. pilóta@email.hu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors()
                )
            }
        },
        confirmButton = {
            val dialogContext = LocalContext.current
            Button(
                onClick = {
                    val isConnected = com.example.util.NetworkUtils.isNetworkAvailable(dialogContext)
                    if (!isConnected) {
                        Toast.makeText(
                            dialogContext,
                            "⚠️ Nincs internetkapcsolat! Hirdetés közzététele csak aktív internetkapcsolattal lehetséges.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }
                    if (title.isNotBlank() && location.isNotBlank() && description.isNotBlank() && phone.isNotBlank()) {
                        val parsedPrice = priceText.toIntOrNull() ?: 0
                        val joinedImageUri = if (selectedImages.isNotEmpty()) selectedImages.joinToString("|||") else null
                        onSubmit(
                            title,
                            type,
                            category,
                            parsedPrice,
                            condition,
                            location,
                            description,
                            batteryCycles,
                            accessories,
                            sellerName,
                            phone,
                            email,
                            joinedImageUri
                        )
                    }
                },
                enabled = title.isNotBlank() && location.isNotBlank() && description.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("marketplace_save_listing_button")
            ) {
                Text("Hirdetés közzététele", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse", color = Color(0xFF94A3B8))
            }
        }
    )
}

/**
 * Modal Dialog with Safe Drone Trading Tips
 */
@Composable
private fun MarketplaceSafetyTipsDialog(
    onDismiss: () -> Unit
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appTheme.surfaceColor,
        shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFFF59E0B))
                Text(
                    text = "Biztonságos Drón Adásvétel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SafetyTipItem(
                    title = "1. DJI Fiók szétkapcsolása (Unbind)",
                    desc = "Eladás előtt a drón előző tulajdonosának a DJI Fly alkalmazásban kötelező lecsatolnia a gépét a fiókjáról (Profile -> Device Management -> Unbind), különben az új vevő nem tudja saját fiókjához kötni!",
                    icon = Icons.Default.LinkOff
                )
                SafetyTipItem(
                    title = "2. EASA Üzembentartói azonosító levétele",
                    desc = "Mielőtt átadod a drónt, távolítsd el a rá ragasztott HUN... EASA üzembentartói matricát.",
                    icon = Icons.Default.Badge
                )
                SafetyTipItem(
                    title = "3. Akkumulátor cellafeszültség és ciklusszám",
                    desc = "Kérd el az akkumulátorok ciklusszámát! Ellenőrizd a DJI Fly appban a cellafeszültségek egyenletességét (nem lehet 0.05V-nál nagyobb eltérés a cellák között) és vizsgáld meg, hogy nincs-e felpúposodva az akku.",
                    icon = Icons.Default.BatteryAlert
                )
                SafetyTipItem(
                    title = "4. Próbarepülés és Gimbal teszt",
                    desc = "Személyes átvételnél mindig kérj egy 2 perces próbarepülést: ellenőrizd a műholdjelet (GPS lock), a gimbal akadálymentes mozgását, a videóképet és a szenzorokat.",
                    icon = Icons.Default.FlightTakeoff
                )
                SafetyTipItem(
                    title = "5. Írásos adásvételi szerződés",
                    desc = "Mindig írjatok adásvételi szerződést a drón gyári sorozatszámával (Serial Number), így a tulajdonjog vitathatatlanul átszáll a vevőre.",
                    icon = Icons.Default.Description
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Értettem, köszönöm", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SafetyTipItem(title: String, desc: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = desc, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

/**
 * Empty State for search / filter results
 */
@Composable
private fun MarketplaceEmptyState(
    searchQuery: String,
    onClearFilters: () -> Unit,
    onPostListing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = Color(0xFF00F0FF),
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = if (searchQuery.isNotBlank()) "Nincs találat erre a keresésre" else "Nincsenek hirdetések a kiválasztott szűrőkkel",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Próbálj más kulcsszót keresni, vagy add fel az első hirdetést ebben a kategóriában!",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Szűrők törlése", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPostListing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Hirdetés feladása", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1E293B),
    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.8f),
    focusedBorderColor = Color(0xFF00F0FF),
    unfocusedBorderColor = Color(0xFF334155),
    focusedLabelColor = Color(0xFF00F0FF),
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

private fun openPhoneDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phone.replace(" ", "")}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        copyToClipboard(context, phone, "Telefonszám kimásolva: $phone")
    }
}

private fun openSms(context: Context, phone: String, title: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${phone.replace(" ", "")}")
            putExtra("sms_body", "Szia! Érdeklődnék a Drón Kalauz appban hirdetett \"$title\" iránt.")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        copyToClipboard(context, phone, "Telefonszám kimásolva: $phone")
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Drón Piactér", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * Fullscreen Interactive Zoomable Image Viewer Modal
 * Supports pinch-to-zoom, double-tap zoom, panning, carousel navigation, and thumbnail strip.
 */
@Composable
fun FullscreenImageViewerModal(
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (images.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, images.size - 1)) }
    var scale by remember(currentIndex) { mutableFloatStateOf(1f) }
    var offset by remember(currentIndex) { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
        ) {
            // Main Zoomable Image Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentIndex) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.8f, 6f)
                            if (scale > 1f) {
                                val maxOffset = (scale - 1f) * 1000f
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                    y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = images[currentIndex],
                    contentDescription = "Kinagyított drón fotó",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${currentIndex + 1} / ${images.size} fotó",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (scale != 1f || offset != Offset.Zero) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1E293B).copy(alpha = 0.9f), CircleShape)
                                .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Nagyítás visszaállítása",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.9f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Bezárás",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Navigation Left / Right Arrows (if multiple images)
            if (images.size > 1) {
                // Prev button
                if (currentIndex > 0) {
                    IconButton(
                        onClick = { currentIndex-- },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(44.dp)
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Előző kép",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Next button
                if (currentIndex < images.size - 1) {
                    IconButton(
                        onClick = { currentIndex++ },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Következő kép",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Gallery Strip & Zoom Tip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Thumbnail Strip if multiple images
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        images.forEachIndexed { index, imgUrl ->
                            val isSelected = index == currentIndex
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { currentIndex = index }
                            ) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = "Kép előnézet $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = "💡 Kétujjas nagyítás (pinch-to-zoom) • Dupla koppintás",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
