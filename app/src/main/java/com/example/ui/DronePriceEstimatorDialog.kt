package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DroneModelSpec
import com.example.data.DronePriceDatabase
import com.example.data.PriceEstimationResult
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DronePriceEstimatorFullScreen(
    onBack: () -> Unit,
    onApplyToNewListing: ((title: String, suggestedPrice: Int, condition: String, accessories: String) -> Unit)? = null
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    Scaffold(
        containerColor = appTheme.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00F0FF).copy(alpha = 0.25f), Color(0xFF10B981).copy(alpha = 0.25f))
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DRÓN ÉRTÉKBECSLŐ",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Pontos használtpiaci árkalkuláció",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("estimator_fullscreen_back_button")
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Vissza a piactérhez",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1524),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        DronePriceEstimatorContent(
            onApplyToNewListing = onApplyToNewListing,
            onClose = onBack,
            isEmbedded = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DronePriceEstimatorDialog(
    onDismiss: () -> Unit,
    onApplyToNewListing: ((title: String, suggestedPrice: Int, condition: String, accessories: String) -> Unit)? = null
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .testTag("drone_price_estimator_dialog"),
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            border = BorderStroke(appTheme.dialogBorderWidthDp.dp, appTheme.cardBorderColor),
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp)
        ) {
            DronePriceEstimatorContent(
                onApplyToNewListing = { title, price, condition, accessories ->
                    onApplyToNewListing?.invoke(title, price, condition, accessories)
                    onDismiss()
                },
                onClose = onDismiss,
                isEmbedded = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Direct & Private Drone Valuation Lab
 * - No tedious catalog browsing step
 * - User types or taps their drone name, condition, bundle, battery
 * - Directly calculates and outputs the price
 * - Results are STRICTLY PRIVATE for the user only (never listed publicly below)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DronePriceEstimatorContent(
    onApplyToNewListing: ((title: String, suggestedPrice: Int, condition: String, accessories: String) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    isEmbedded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("hu", "HU")).apply {
            maximumFractionDigits = 0
        }
    }
    val focusManager = LocalFocusManager.current

    // Direct User Inputs (Empty by default)
    var droneNameInput by remember { mutableStateOf("") }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var selectedBrandFilter by remember { mutableStateOf("Mind") }
    var customOriginalPriceInput by remember { mutableStateOf("") }
    var selectedBundlePackage by remember { mutableStateOf("") }
    var selectedControllerType by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    var selectedBatteryCycles by remember { mutableStateOf("") }
    var hasValidWarranty by remember { mutableStateOf(false) }
    val extraAccessories = remember { mutableStateListOf<String>() }

    // Filtered drone models for searchable dropdown
    val filteredDroneModels = remember(droneNameInput, selectedBrandFilter) {
        val query = droneNameInput.trim().lowercase()
        val allModels = DronePriceDatabase.droneModels

        val brandFiltered = if (selectedBrandFilter == "Mind") {
            allModels
        } else {
            allModels.filter { it.brand.equals(selectedBrandFilter, ignoreCase = true) }
        }

        if (query.isEmpty()) {
            brandFiltered
        } else {
            val matched = allModels.filter { model ->
                model.modelName.lowercase().contains(query) ||
                model.brand.lowercase().contains(query) ||
                model.category.lowercase().contains(query) ||
                model.description.lowercase().contains(query)
            }
            if (selectedBrandFilter != "Mind") {
                val matchedInBrand = matched.filter { it.brand.equals(selectedBrandFilter, ignoreCase = true) }
                if (matchedInBrand.isNotEmpty()) matchedInBrand else matched
            } else {
                matched
            }
        }
    }

    // Result State (null = Input Form, non-null = Private Result Card)
    var estimationResult by remember { mutableStateOf<PriceEstimationResult?>(null) }

    val quickModelPresets = listOf(
        "DJI Mini 4 Pro",
        "DJI Mini 3 Pro",
        "DJI Mini 3",
        "DJI Mini 2 SE",
        "DJI Air 3",
        "DJI Air 2S",
        "DJI Avata 2",
        "DJI Mavic 3 Pro",
        "Autel Evo Lite+",
        "5\" Custom FPV Drón"
    )

    val bundlePackageOptions = listOf(
        "Alap csomag (1 akkumulátor)" to "Alap",
        "Fly More Combo (3 akku + hub + táska)" to "+Értéknövelő",
        "Fly More Combo Plus (Nagy akkuk + táska)" to "+Kiemelt",
        "Csak dróntest (Akku / töltő nélkül)" to "-Értékcsökkentő"
    )

    val controllerOptions = listOf(
        "Standard távirányító (Telefonos RC-N1 / RC-N2 / RC-N3)" to "Alap",
        "Beépített kijelzős Smart távirányító (DJI RC / RC 2 / Smart)" to "+Értéknövelő",
        "FPV Szemüveg + Vezérlő (DJI Goggles + Motion / FPV)" to "+Prémium FPV",
        "Távirányító nélkül" to "-Értékcsökkentő"
    )

    val conditionOptions = listOf(
        "Újszerű / Karcmentes (Dobozos)" to "+8% érték",
        "Megkímélt / Normál használat" to "Reális piaci ár",
        "Használt / Kisebb esztétikai hiba" to "-12% érték",
        "Sérült / Javított" to "-28% érték"
    )

    val batteryCycleOptions = listOf(
        "1-15 ciklus (Alig használt)" to "+Friss akkuk",
        "15-40 ciklus (Normál)" to "Normál",
        "40+ ciklus (Fáradt akkuk)" to "-Kapacitásvesztés"
    )

    val availableExtraAccessories = listOf(
        "ND szűrőkészlet" to "+12.000 Ft",
        "Vízálló kemény bőrönd" to "+14.000 Ft",
        "128GB+ Gyors memóriakártya" to "+6.000 Ft",
        "Pótpropellerek" to "+4.000 Ft",
        "Landolópad" to "+3.000 Ft"
    )

    fun runEstimation() {
        focusManager.clearFocus()
        val customNewPrice = customOriginalPriceInput.toIntOrNull()
        val modelSpec = DronePriceDatabase.findModelOrEstimate(droneNameInput, customNewPrice)
        val finalBundle = when {
            selectedBundlePackage.isNotBlank() && selectedControllerType.isNotBlank() -> "$selectedBundlePackage • $selectedControllerType"
            selectedBundlePackage.isNotBlank() -> selectedBundlePackage
            selectedControllerType.isNotBlank() -> selectedControllerType
            else -> "Alap (1 akku)"
        }
        val result = DronePriceDatabase.calculateDroneValue(
            model = modelSpec,
            bundleType = finalBundle,
            condition = selectedCondition.ifBlank { "Megkímélt / Normál használat" },
            batteryCycles = selectedBatteryCycles.ifBlank { "15-40 ciklus (Normál)" },
            hasValidWarranty = hasValidWarranty,
            extraAccessories = extraAccessories.toList()
        )
        estimationResult = result
    }

    Column(
        modifier = modifier
            .background(if (isEmbedded) Color.Transparent else Color(0xFF0D1524))
            .padding(if (isEmbedded) 16.dp else 20.dp)
            .testTag("drone_price_estimator_content")
    ) {
        // Top Header Bar (Shown only in Dialog mode)
        if (!isEmbedded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F0FF).copy(alpha = 0.25f), Color(0xFF10B981).copy(alpha = 0.25f))
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "DRÓN ÉRTÉKBECSLŐ",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Add meg a drónod adatait és tudd meg a pontos piaci árát",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Bezárás",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Main Content Area with Animated Transition
        AnimatedContent(
            targetState = estimationResult,
            label = "estimator_flow_transition",
            modifier = Modifier.weight(1f)
        ) { result ->
            if (result == null) {
                // ==========================================
                // 1. DIRECT INPUT FORM (NO BROWSING STEP)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // FIELD 1: Drone Name / Model Input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. Milyen típusú drónod van?",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Írd be vagy válassz",
                                    fontSize = 10.sp,
                                    color = Color(0xFF00F0FF)
                                )
                            }

                            OutlinedTextField(
                                value = droneNameInput,
                                onValueChange = {
                                    droneNameInput = it
                                    isModelDropdownExpanded = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("estimator_drone_name_input")
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            isModelDropdownExpanded = true
                                        }
                                    },
                                placeholder = {
                                    Text("Pl. DJI Mini 4 Pro, Air 3, Avata 2, BetaFPV...", color = Color(0xFF64748B), fontSize = 13.sp)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = Color(0xFF00F0FF)
                                    )
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (droneNameInput.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    droneNameInput = ""
                                                    isModelDropdownExpanded = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Törlés",
                                                    tint = Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                isModelDropdownExpanded = !isModelDropdownExpanded
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isModelDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isModelDropdownExpanded) "Lista bezárása" else "Lista megnyitása",
                                                tint = Color(0xFF00F0FF)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0D1524),
                                    unfocusedContainerColor = Color(0xFF0D1524),
                                    focusedBorderColor = Color(0xFF00F0FF),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    isModelDropdownExpanded = false
                                    focusManager.clearFocus()
                                })
                            )

                            // Searchable Dropdown List with Autocomplete & Brand Filter
                            AnimatedVisibility(
                                visible = isModelDropdownExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A111E)),
                                    border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Header & Brand Filter Pills
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00F0FF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Elérhető modellek (${filteredDroneModels.size})",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Text(
                                                text = "Bezárás ✕",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .clickable { isModelDropdownExpanded = false }
                                                    .padding(4.dp)
                                            )
                                        }

                                        // Brand Category Filter Tabs
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Mind", "DJI", "Autel", "FPV", "Egyéb").forEach { brand ->
                                                val isBrandSelected = selectedBrandFilter == brand
                                                FilterChip(
                                                    selected = isBrandSelected,
                                                    onClick = { selectedBrandFilter = brand },
                                                    label = {
                                                        Text(
                                                            text = brand,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isBrandSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF00F0FF),
                                                        selectedLabelColor = Color(0xFF0F172A),
                                                        containerColor = Color(0xFF131E30),
                                                        labelColor = Color(0xFF94A3B8)
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        borderColor = if (isBrandSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                                                        enabled = true,
                                                        selected = isBrandSelected
                                                    )
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                                        // Scrollable List of Filtered Models
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 260.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (filteredDroneModels.isNotEmpty()) {
                                                filteredDroneModels.forEach { spec ->
                                                    val isSelected = droneNameInput.trim().equals(spec.modelName, ignoreCase = true)
                                                    val brandBadgeColor = when (spec.brand) {
                                                        "DJI" -> Color(0xFF00F0FF)
                                                        "Autel" -> Color(0xFFFF9800)
                                                        "FPV" -> Color(0xFFE040FB)
                                                        else -> Color(0xFF94A3B8)
                                                    }

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) Color(0xFF00F0FF).copy(alpha = 0.12f) else Color.Transparent)
                                                            .clickable {
                                                                droneNameInput = spec.modelName
                                                                isModelDropdownExpanded = false
                                                                focusManager.clearFocus()
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            // Brand Pill
                                                            Surface(
                                                                color = brandBadgeColor.copy(alpha = 0.15f),
                                                                shape = RoundedCornerShape(4.dp),
                                                                border = BorderStroke(1.dp, brandBadgeColor.copy(alpha = 0.4f))
                                                            ) {
                                                                Text(
                                                                    text = spec.brand,
                                                                    color = brandBadgeColor,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                )
                                                            }

                                                            Column {
                                                                Text(
                                                                    text = spec.modelName,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                    color = if (isSelected) Color(0xFF00F0FF) else Color.White
                                                                )
                                                                Text(
                                                                    text = "${spec.category} • ${spec.releaseYear}",
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF64748B)
                                                                )
                                                            }
                                                        }

                                                        // Price preview pill
                                                        Column(
                                                            horizontalAlignment = Alignment.End
                                                        ) {
                                                            Text(
                                                                text = currencyFormatter.format(spec.baseUsedPriceHuf),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFFFD700)
                                                            )
                                                            Text(
                                                                text = "használt alapár",
                                                                fontSize = 9.sp,
                                                                color = Color(0xFF94A3B8)
                                                            )
                                                        }
                                                    }
                                                    HorizontalDivider(color = Color(0xFF1E293B).copy(alpha = 0.5f), thickness = 0.5.dp)
                                                }
                                            } else {
                                                // If no database model matched the query
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Nem találtunk pontos típusmegfelelést a katalógusban.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF94A3B8),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Button(
                                                        onClick = {
                                                            isModelDropdownExpanded = false
                                                            focusManager.clearFocus()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF00F0FF).copy(alpha = 0.2f),
                                                            contentColor = Color(0xFF00F0FF)
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = "Kalkuláció ezzel: \"$droneNameInput\" ➔",
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

                            // Quick Model Tap Chips
                            Text(
                                text = "Gyakori modellek egy érintéssel:",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickModelPresets.forEach { preset ->
                                    val isSelected = droneNameInput.trim().equals(preset, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { droneNameInput = preset },
                                        label = {
                                            Text(
                                                text = preset,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF00F0FF),
                                            selectedLabelColor = Color(0xFF0F172A),
                                            containerColor = Color(0xFF1E293B),
                                            labelColor = Color(0xFFCBD5E1)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                                            enabled = true,
                                            selected = isSelected
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // FIELD 2: Condition Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "2. Milyen a drón fizikai és műszaki állapota?",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            conditionOptions.forEach { (cond, badge) ->
                                val isSelected = selectedCondition == cond
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCondition = cond },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0D1524)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155).copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cond,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (badge.contains("+")) Color(0xFF10B981).copy(alpha = 0.2f)
                                                    else if (badge.contains("-")) Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    else Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = badge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (badge.contains("+")) Color(0xFF10B981)
                                                else if (badge.contains("-")) Color(0xFFF87171)
                                                else Color(0xFF60A5FA)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FIELD 3: Bundle Package Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "3. Csomag kiszerelés",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            bundlePackageOptions.forEach { (bundlePkg, badge) ->
                                val isSelected = selectedBundlePackage == bundlePkg
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBundlePackage = bundlePkg },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0D1524)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155).copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = bundlePkg,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (badge.contains("+")) Color(0xFF10B981).copy(alpha = 0.2f)
                                                    else if (badge.contains("-")) Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    else Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = badge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (badge.contains("+")) Color(0xFF10B981)
                                                else if (badge.contains("-")) Color(0xFFF87171)
                                                else Color(0xFF60A5FA)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FIELD 4: Remote Controller Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "4. Távirányító",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            controllerOptions.forEach { (ctrl, badge) ->
                                val isSelected = selectedControllerType == ctrl
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedControllerType = ctrl },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0D1524)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155).copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ctrl,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (badge.contains("+")) Color(0xFF10B981).copy(alpha = 0.2f)
                                                    else if (badge.contains("-")) Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    else Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = badge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (badge.contains("+")) Color(0xFF10B981)
                                                else if (badge.contains("-")) Color(0xFFF87171)
                                                else Color(0xFF60A5FA)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FIELD 5: Battery Cycles & Warranty
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "5. Akkumulátorok állapota & Garancia",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            batteryCycleOptions.forEach { (cycle, label) ->
                                val isSelected = selectedBatteryCycles == cycle
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBatteryCycles = cycle },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0D1524)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155).copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cycle,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            color = if (label.contains("+")) Color(0xFF10B981) else Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Warranty Switch Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hasValidWarranty = !hasValidWarranty },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (hasValidWarranty) Color(0xFF1E293B) else Color(0xFF0D1524)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (hasValidWarranty) Color(0xFF10B981) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🛡️ Még érvényes garancia / DJI Care Refresh",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Jelentős értéknövelő tényező (+15-30 ezer Ft)",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Checkbox(
                                        checked = hasValidWarranty,
                                        onCheckedChange = { hasValidWarranty = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF10B981),
                                            uncheckedColor = Color(0xFF64748B)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // FIELD 6: Extra Accessories (Optional)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "6. Extra tartozékok a drónhoz (Opcionális)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            availableExtraAccessories.forEach { (acc, bonus) ->
                                val isChecked = extraAccessories.contains(acc)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) extraAccessories.remove(acc)
                                            else extraAccessories.add(acc)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isChecked) Color(0xFF1E293B) else Color(0xFF0D1524)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChecked) Color(0xFF00F0FF) else Color(0xFF334155).copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = {
                                                    if (it) extraAccessories.add(acc)
                                                    else extraAccessories.remove(acc)
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(0xFF00F0FF),
                                                    uncheckedColor = Color(0xFF64748B)
                                                )
                                            )
                                            Text(
                                                text = acc,
                                                fontSize = 12.sp,
                                                color = if (isChecked) Color.White else Color(0xFFCBD5E1)
                                            )
                                        }
                                        Text(
                                            text = bonus,
                                            fontSize = 10.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                // ==========================================
                // 2. STRICTLY PRIVATE RESULT VIEW
                // (NOT LISTED BELOW - EXCLUSIVELY ON THIS SCREEN)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Privacy Watermark Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1928)),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔒 BIZALMAS / PRIVÁT ÉRTÉKBECSLÉS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Ezt az összeget és kalkulációt kizárólag te látod ezen a képernyőn. Nem került közzétételre a piactéren.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Main Big Glowing Result Value Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .testTag("estimator_private_result_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2E)),
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF10B981), Color(0xFF3B82F6)))
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF00F0FF).copy(alpha = 0.18f), Color(0xFF0F1B2E)),
                                        radius = 600f
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = result.modelName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF00F0FF),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "REÁLIS MAGYAR HASZNÁLTPIACI ÉRTÉK",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currencyFormatter.format(result.estimatedAveragePriceHuf),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Recommended Listing Range Chip
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF08101E), RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Ajánlott hirdetési sáv: ${currencyFormatter.format(result.recommendedMinPriceHuf)} – ${currencyFormatter.format(result.recommendedMaxPriceHuf)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Strategic Pricing Targets (Gyors eladás vs Türelmes ár)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Sale
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF162235)),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GYORS ELADÁS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currencyFormatter.format(result.quickSalePriceHuf),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "1-3 napon belüli azonnali vevőhöz",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        // Maximum / Patient Sale
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF162235)),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "MAXIMÁLIS ÁR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currencyFormatter.format(result.patientSalePriceHuf),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Ha ráérsz kivárni a csúcsajánlatot",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    // Detailed Factor Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📊 Árkalkuláció részletezése (Hogyan jött ki az összeg?):",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            result.breakdown.forEach { factor ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = factor.label,
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (factor.isPositive) "+${currencyFormatter.format(factor.amountHuf)}"
                                        else "-${currencyFormatter.format(Math.abs(factor.amountHuf))}",
                                        fontSize = 12.sp,
                                        color = if (factor.isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Market Liquidity Notes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Piaci kereslet és forgási sebesség:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = result.marketPopularity,
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = result.marketNotes,
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Pro Seller Tips
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2E)),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "💡 Tippek, ha úgy döntesz, hogy eladod:",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold
                            )
                            result.tipsForSeller.forEach { tip ->
                                Text(
                                    text = tip,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Action Bar
        if (estimationResult == null) {
            // Button to Calculate Price
            Button(
                onClick = { runEstimation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("estimator_calculate_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00F0FF),
                    contentColor = Color(0xFF0F172A)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Drón Értékének Kiszámítása",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            // Action buttons after calculation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to recalculate / change input
                OutlinedButton(
                    onClick = { estimationResult = null },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("estimator_recalculate_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Új kalkuláció", fontWeight = FontWeight.Bold)
                }

                // Optional: User explicitly wants to create a listing
                if (onApplyToNewListing != null) {
                    val res = estimationResult!!
                    Button(
                        onClick = {
                            val accessoriesList = (listOf(res.bundleType) + res.extraAccessories).filter { it.isNotBlank() }.joinToString(", ")
                            onApplyToNewListing(
                                res.modelName,
                                res.estimatedAveragePriceHuf,
                                res.condition,
                                accessoriesList
                            )
                            onClose?.invoke()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("estimator_apply_to_listing_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hirdetés feladása ezzel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { onClose?.invoke() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text("Kész", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
