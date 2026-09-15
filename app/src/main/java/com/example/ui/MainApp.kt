package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ChatMessage
import com.example.data.CountryRule
import com.example.data.DroneRules
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: ChatViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val currentPilotUser by viewModel.currentPilotUser.collectAsStateWithLifecycle()

    var showSplashScreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showAdminPasswordDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var displayedTitleScreen by remember { mutableStateOf(currentScreen) }
    var secretTapCount by remember { mutableIntStateOf(0) }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }

    val context = LocalContext.current

    // Trigger automatic live NOTAM & aviation weather network fetch safely in background
    LaunchedEffect(Unit) {
        kotlinx.coroutines.Dispatchers.IO.let {
            try {
                com.example.data.LiveAviationNetworkManager.syncOnAppStartup()
            } catch (_: Exception) {}
        }
    }

    if (showSplashScreen) {
        DroneTakeoffSplashScreen(
            onSplashFinished = {
                showSplashScreen = false
            }
        )
        return
    }

    // Dynamic Background Canvas following active theme gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = selectedTheme.backgroundGradientColors
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            val titleText = when (displayedTitleScreen) {
                                AppScreen.HOME -> "FŐOLDAL"
                                AppScreen.RADAR -> "IDŐKÉP RADAR & TÉRKÉP"
                                AppScreen.CHAT -> "ÉLŐ CHAT"
                                AppScreen.RULES -> "REPÜLÉSI SZABÁLYZATOK"
                                AppScreen.SPOTTER -> "SPOTTER & FOTÓS TÉRKÉP"
                                AppScreen.MARKETPLACE -> "DRÓNOK ELADÁSA ÉS VÉTELE"
                                AppScreen.EXAM_AND_INSURANCE -> "VIZSGÁK ÉS BIZTOSÍTÁSOK"
                                AppScreen.PROFILE -> "PILÓTAFIÓK & PROFIL"
                                AppScreen.ADMIN -> "👑 ADMINISZTRÁCIÓ"
                            }

                            AnimatedContent(
                                targetState = titleText,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.92f, animationSpec = tween(280))).togetherWith(
                                        fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 1.05f, animationSpec = tween(180))
                                    )
                                },
                                label = "header_title_anim"
                            ) { text ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .testTag("app_header_title_row")
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            val now = System.currentTimeMillis()
                                            if (now - lastTapTimestamp > 2500L) {
                                                secretTapCount = 1
                                            } else {
                                                secretTapCount++
                                            }
                                            lastTapTimestamp = now

                                            if (secretTapCount >= 5) {
                                                secretTapCount = 0
                                                showAdminPasswordDialog = true
                                            }
                                        }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_app_drone_logo),
                                            contentDescription = "Drón Kalauz logó",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    // Visual Theme Style Badge (Interactive: tap to switch styles)
                                    Surface(
                                        onClick = { showThemeDialog = true },
                                        shape = RoundedCornerShape(selectedTheme.buttonCornerRadiusDp.dp),
                                        color = selectedTheme.primaryColor.copy(alpha = 0.12f),
                                        border = BorderStroke(0.8.dp, selectedTheme.primaryColor.copy(alpha = 0.5f)),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(selectedTheme.primaryColor)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "${selectedTheme.iconEmoji} ${selectedTheme.uiTag}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = selectedTheme.primaryColor,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    navigationIcon = {
                        if (currentScreen != AppScreen.HOME) {
                            // Back Button to Home when on a sub-screen
                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                    modifier = Modifier
                                        .testTag("nav_back_button")
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Vissza a főoldalra",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        } else {
                            // Left Corner Button on Home: Open Live Community Chat
                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.CHAT) },
                                    modifier = Modifier
                                        .testTag("chat_nav_button")
                                        .background(
                                            if (currentScreen == AppScreen.CHAT)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (currentScreen == AppScreen.CHAT)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Élő közösségi chat megnyitása",
                                        tint = if (currentScreen == AppScreen.CHAT)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Right Corner: Pilot Profile / Account Icon
                        Box(modifier = Modifier.padding(end = 4.dp)) {
                            IconButton(
                                onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                                modifier = Modifier
                                    .testTag("nav_profile_button")
                                    .background(
                                        if (currentScreen == AppScreen.PROFILE)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else if (currentPilotUser != null)
                                            Color(0xFF10B981).copy(alpha = 0.15f)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (currentScreen == AppScreen.PROFILE)
                                            MaterialTheme.colorScheme.primary
                                        else if (currentPilotUser != null)
                                            Color(0xFF10B981)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (currentPilotUser != null) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                                    contentDescription = "Pilótafiók megnyitása",
                                    tint = if (currentScreen == AppScreen.PROFILE)
                                        MaterialTheme.colorScheme.primary
                                    else if (currentPilotUser != null)
                                        Color(0xFF10B981)
                                    else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Right Corner: Bell Notification Icon next to menu dropdown
                        Box(modifier = Modifier.padding(end = 4.dp)) {
                            IconButton(
                                onClick = { showNotificationDialog = true },
                                modifier = Modifier
                                    .testTag("notification_bell_button")
                                    .background(
                                        if (unreadNotificationsCount > 0)
                                            Color(0xFF0284C7).copy(alpha = 0.15f)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (unreadNotificationsCount > 0)
                                            Color(0xFF0284C7)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationsCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFEF4444),
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (unreadNotificationsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = "Értesítések",
                                        tint = if (unreadNotificationsCount > 0)
                                            Color(0xFF0284C7)
                                        else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        // Right Corner: Navigation Dropdown Menu
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(
                                onClick = { showMenu = !showMenu },
                                modifier = Modifier
                                    .testTag("menu_dropdown_button")
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Navigációs menü",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            DroneTowedDropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                isDarkMode = isDarkMode,
                                currentPilotUser = currentPilotUser,
                                isAdminUnlocked = isAdminUnlocked,
                                selectedTheme = selectedTheme,
                                onNavigate = { destination ->
                                    viewModel.navigateTo(destination)
                                },
                                onToggleDarkMode = {
                                    viewModel.toggleDarkMode()
                                },
                                onOpenThemeDialog = {
                                    showThemeDialog = true
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = selectedTheme.surfaceColor.copy(alpha = if (selectedTheme == AppTheme.OLED_BLACK) 1f else 0.94f),
                        titleContentColor = if (selectedTheme.isDark) Color.White else Color(0xFF0F172A)
                    )
                )
                HorizontalDivider(
                    thickness = selectedTheme.borderWidthDp.dp,
                    color = selectedTheme.topBarBorderColor
                )
            }
        }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val isDroneFlyingRight = targetState.ordinal >= initialState.ordinal

                        if (isDroneFlyingRight) {
                            // Drón balról jobbra húzza a címet -> az oldal is balról jobbra suhan
                            (
                                slideInHorizontally(
                                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> -fullWidth }
                                ) + fadeIn(animationSpec = tween(300))
                            ).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> fullWidth }
                                ) + fadeOut(animationSpec = tween(240))
                            )
                        } else {
                            // Drón jobbról balra húzza a címet -> az oldal is jobbról balra suhan
                            (
                                slideInHorizontally(
                                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> fullWidth }
                                ) + fadeIn(animationSpec = tween(300))
                            ).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> -fullWidth }
                                ) + fadeOut(animationSpec = tween(240))
                            )
                        }
                    },
                    label = "drone_screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                        AppScreen.RADAR -> RadarScreen(viewModel = viewModel)
                        AppScreen.RULES -> RulesScreen(searchQuery, viewModel)
                        AppScreen.SPOTTER -> SpotterScreen(viewModel = viewModel)
                        AppScreen.MARKETPLACE -> MarketplaceScreen(viewModel = viewModel)
                        AppScreen.EXAM_AND_INSURANCE -> ExamInsuranceScreen(viewModel = viewModel)
                        AppScreen.ADMIN -> AdminScreen(viewModel = viewModel)
                        AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                        AppScreen.CHAT -> {
                            ChatScreen(
                                viewModel = viewModel,
                                onShowAttachment = { showAttachmentSheet = true }
                            )
                        }
                    }
                }
            }
        }

        // Drone towing paper banner overlay flying directly across top bar header
        DroneFlightTransitionOverlay(
            currentScreen = currentScreen,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            onTitleDelivered = { deliveredScreen ->
                displayedTitleScreen = deliveredScreen
            }
        )

        // Animated Attachment Selector overlay
        if (showAttachmentSheet) {
            AttachmentSheet(
                onDismiss = { showAttachmentSheet = false },
                onSelectPreset = { preset ->
                    viewModel.selectPresetImage(preset)
                    showAttachmentSheet = false
                },
                onSelectCustom = { uri ->
                    viewModel.selectCustomImageUri(uri?.toString())
                    showAttachmentSheet = false
                }
            )
        }

        // Admin Authentication Password Dialog
        if (showAdminPasswordDialog) {
            AdminPasswordDialog(
                isDarkMode = isDarkMode,
                onDismiss = { showAdminPasswordDialog = false },
                onSuccess = {
                    showAdminPasswordDialog = false
                    viewModel.unlockAdminMode()
                    android.widget.Toast.makeText(
                        context,
                        "👑 Sikeres azonosítás! Admin felület feloldva.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // Notification Center Dialog
        if (showNotificationDialog) {
            NotificationDialog(
                viewModel = viewModel,
                onDismiss = { showNotificationDialog = false },
                onNavigateToScreen = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = selectedTheme,
                onSelectTheme = { theme ->
                    viewModel.selectTheme(theme)
                },
                onDismiss = { showThemeDialog = false }
            )
        }
    }
}

@Composable
fun AdminPasswordDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("drone_admin_prefs", android.content.Context.MODE_PRIVATE) }
    val savedPassword = remember { prefs.getString("saved_admin_password", "") ?: "" }

    var password by remember { mutableStateOf(savedPassword) }
    var rememberPassword by remember { mutableStateOf(savedPassword.isNotEmpty()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val handleLogin = {
        if (password == "LIlioM2000Marton") {
            if (rememberPassword) {
                prefs.edit().putString("saved_admin_password", password).apply()
            } else {
                prefs.edit().remove("saved_admin_password").apply()
            }
            onSuccess()
        } else {
            isError = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEAB308).copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, Color(0xFFEAB308), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Adminisztrátori Belépés",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "A feloldáshoz add meg az adminisztrátori mesterjelszót:",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (isError) isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    label = { Text("Jelszó") },
                    placeholder = { Text("Mesterjelszó...") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handleLogin() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Jelszó elrejtése" else "Jelszó megjelenítése",
                                tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEAB308),
                        focusedLabelColor = Color(0xFFEAB308),
                        errorBorderColor = Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444)
                    )
                )

                // Remember Password Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberPassword = !rememberPassword }
                        .padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = rememberPassword,
                        onCheckedChange = { rememberPassword = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFEAB308),
                            checkmarkColor = Color.Black,
                            uncheckedColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("admin_remember_password_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Jelszó megjegyzése",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )
                }

                if (isError) {
                    Text(
                        text = "❌ Hibás jelszó! Hozzáférés megtagadva.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { handleLogin() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEAB308),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp),
                modifier = Modifier.testTag("admin_password_confirm_button")
            ) {
                Text("Belépés", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse", color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B))
            }
        },
        containerColor = appTheme.surfaceColor,
        shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp)
    )
}

// 1. HOME SCREEN (Főoldal)
@Composable
fun HomeScreen(viewModel: ChatViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val telemetry by viewModel.telemetryState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentPilotUser by viewModel.currentPilotUser.collectAsStateWithLifecycle()

    // GPS Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.refreshLocationTelemetry()
        }
    }

    // Auto-fetch on first entry
    LaunchedEffect(Unit) {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.refreshLocationTelemetry()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Calculator states
    var payloadWeight by remember { mutableStateOf(150f) } // grams
    var batteryCapacity by remember { mutableStateOf(2450f) } // mAh
    var estimatedFlightTime by remember { mutableStateOf(0) }
    val appTheme = com.example.ui.theme.LocalAppTheme.current

    // Recalculate estimated flight time based on weight and battery
    LaunchedEffect(payloadWeight, batteryCapacity) {
        // Simple physical heuristic: 2450mAh battery with 249g (0 payload) drone flies for ~31 mins
        // Every additional 100g of weight reduces flight time by ~4 minutes.
        // Higher battery capacity increases time proportionally.
        val baseCapacity = 2450f
        val weightPenalty = (payloadWeight / 100f) * 4.5f
        val capacityMultiplier = batteryCapacity / baseCapacity
        val rawTime = (31f - weightPenalty) * capacityMultiplier
        estimatedFlightTime = rawTime.coerceIn(5f, 45f).toInt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Frame with dynamic theme image, style badge, and responsive rounded corners
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp),
            border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = appTheme.heroImageRes),
                    contentDescription = appTheme.heroImageTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Bottom gradient shadow for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.78f)
                                ),
                                startY = 120f
                            )
                        )
                )
                // Top theme style badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.4f).coerceAtLeast(3f).dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(0.8.dp, appTheme.primaryColor.copy(alpha = 0.8f), RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.4f).coerceAtLeast(3f).dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = appTheme.heroBadge,
                        color = appTheme.primaryColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                // Bottom title & subtitle
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = appTheme.heroImageTitle,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = appTheme.heroImageSubtitle,
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp))
                            .background(appTheme.primaryColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = appTheme.uiStyleName,
                            color = if (appTheme.isDark && appTheme != AppTheme.OLED_BLACK) Color.Black else Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bemutatkozó & Üdvözlő Kártya Drón Légi Kézbesítéssel (Animated Drone Delivery)
        DroneDeliveredIntroCard()

        // Live Real GPS Location & Modular Telemetry Widget Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telemetry_dashboard_card"),
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
            shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header: Location Badge, Coordinate Chip, and Live Refresh Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (telemetry.isGpsActive) Color(0xFF06B6D4).copy(alpha = 0.15f) else Color(0xFF334155).copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (telemetry.isGpsActive) Color(0xFF06B6D4) else Color(0xFF475569),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (telemetry.isGpsActive) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                contentDescription = "GPS Helyzet",
                                tint = if (telemetry.isGpsActive) Color(0xFF06B6D4) else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = telemetry.locationName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (telemetry.isGpsActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (telemetry.isGpsActive) "ÉLŐ GPS" else "ALAPÉRT.",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (telemetry.isGpsActive) Color(0xFF10B981) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Text(
                                text = "${String.format(Locale.US, "%.3f", telemetry.latitude)}° É, ${String.format(Locale.US, "%.3f", telemetry.longitude)}° K • ${telemetry.lastUpdated}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // GPS Refresh button
                    IconButton(
                        onClick = {
                            val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
                                viewModel.refreshLocationTelemetry()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(appTheme.backgroundColor, CircleShape)
                            .border(appTheme.borderWidthDp.dp, appTheme.cardBorderColor, CircleShape)
                            .testTag("refresh_gps_telemetry_button")
                    ) {
                        if (telemetry.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = appTheme.primaryColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "GPS és Időjárás Frissítése",
                                tint = appTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Safety Status Smart Pill Banner
                val statusBgColor = when {
                    telemetry.statusText.contains("nem ajánlott", ignoreCase = true) -> Color(0xFFEF4444).copy(alpha = 0.2f)
                    telemetry.statusText.contains("óvatosság", ignoreCase = true) || telemetry.statusText.contains("zavarhatja", ignoreCase = true) -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                    else -> Color(0xFF10B981).copy(alpha = 0.15f)
                }
                val statusTextColor = when {
                    telemetry.statusText.contains("nem ajánlott", ignoreCase = true) -> Color(0xFFEF4444)
                    telemetry.statusText.contains("óvatosság", ignoreCase = true) || telemetry.statusText.contains("zavarhatja", ignoreCase = true) -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(statusBgColor, RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.6f).dp))
                        .border(1.dp, statusTextColor.copy(alpha = 0.4f), RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.6f).dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusTextColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = telemetry.statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (telemetry.statusText.contains("nem ajánlott", ignoreCase = true)) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modular Widget Grid (Row 1: Wind speed widget & KP Geomagnetic widget)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Widget 1: Wind Gauge
                    TelemetryWidgetCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Air,
                        iconTint = if (telemetry.windSpeedKmh > 25) Color(0xFFF59E0B) else Color(0xFF06B6D4),
                        title = "SZÉLSEBESSÉG",
                        primaryValue = "${telemetry.windSpeedKmh}",
                        unit = "km/h",
                        secondaryBadge = "Lökés: ${telemetry.windGustKmh} km/h",
                        statusText = if (telemetry.windSpeedKmh > 25) "Erős szél" else "Ideális szél",
                        statusColor = if (telemetry.windSpeedKmh > 25) Color(0xFFF59E0B) else Color(0xFF10B981),
                        gaugeProgress = (telemetry.windSpeedKmh / 50f).coerceIn(0.1f, 1f)
                    )

                    // Widget 2: KP Geomagnetic Index
                    TelemetryWidgetCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Thunderstorm,
                        iconTint = if (telemetry.kpIndex <= 3) Color(0xFF10B981) else if (telemetry.kpIndex <= 4) Color(0xFFF59E0B) else Color(0xFFEF4444),
                        title = "KP INDEX (NAP)",
                        primaryValue = "Kp ${telemetry.kpIndex}",
                        unit = "/ 9",
                        secondaryBadge = if (telemetry.kpIndex <= 3) "Nyugodt" else "Zavar",
                        statusText = if (telemetry.kpIndex <= 3) "Tiszta jel" else "Geomágneses",
                        statusColor = if (telemetry.kpIndex <= 3) Color(0xFF10B981) else if (telemetry.kpIndex <= 4) Color(0xFFF59E0B) else Color(0xFFEF4444),
                        gaugeProgress = (telemetry.kpIndex / 9f).coerceIn(0.1f, 1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modular Widget Grid (Row 2: GPS Satellites & Visibility / Altitude)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Widget 3: GPS Satellites Lock
                    TelemetryWidgetCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SatelliteAlt,
                        iconTint = if (telemetry.satellitesCount >= 12) Color(0xFF10B981) else Color(0xFFF59E0B),
                        title = "GPS MŰHOLDAK",
                        primaryValue = "${telemetry.satellitesCount}",
                        unit = "SAT",
                        secondaryBadge = if (telemetry.satellitesCount >= 12) "HDOP Kiváló" else "Gyenge",
                        statusText = if (telemetry.satellitesCount >= 12) "Zárolva (3D Lock)" else "Keresés...",
                        statusColor = if (telemetry.satellitesCount >= 12) Color(0xFF10B981) else Color(0xFFF59E0B),
                        gaugeProgress = (telemetry.satellitesCount / 24f).coerceIn(0.1f, 1f)
                    )

                    // Widget 4: Visibility & Ground Altitude
                    TelemetryWidgetCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Visibility,
                        iconTint = Color(0xFF38BDF8),
                        title = "LÁTÓTÁVOLSÁG",
                        primaryValue = ">${telemetry.visibilityKm}",
                        unit = "km",
                        secondaryBadge = "Terep: ${telemetry.altitudeMeters.toInt()} m",
                        statusText = "Tiszta VLOS",
                        statusColor = Color(0xFF10B981),
                        gaugeProgress = 0.95f
                    )
                }
            }
        }

        // Civil Twilight & Golden Hour Legal Limits Calculator Card
        CivilTwilightCalculatorCard(
            latitude = telemetry.latitude,
            longitude = telemetry.longitude,
            locationName = telemetry.locationName
        )

        // Gyors Időkép Radar & Élő Térkép Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_radar_card"),
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
            shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with Radar Pulse Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(appTheme.primaryColor.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, appTheme.primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = appTheme.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GYORS IDŐKÉP RADAR",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (appTheme.isDark) Color.White else Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ÉLŐ TÉRKÉP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Csapadék, széllökések és viharzónák",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Open direct button
                    IconButton(
                        onClick = { viewModel.openRadar(RadarLayer.WIND) },
                        modifier = Modifier.testTag("home_open_radar_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Megnyitás",
                            tint = appTheme.primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info badge on current radar flight viability
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appTheme.backgroundColor, RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.5f).dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = appTheme.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Élő térkép: ${telemetry.locationName} körzetében a légmozgás (${telemetry.windSpeedKmh} km/h) ${if (telemetry.windSpeedKmh < 25) "biztonságos repülést tesz lehetővé." else "fokozott figyelmet igényel."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (appTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Quick Layer Chips (Wind, Cloud, Windy)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickRadarChip(
                        icon = Icons.Default.Air,
                        label = "Széltérkép",
                        onClick = { viewModel.openRadar(RadarLayer.WIND) }
                    )
                    QuickRadarChip(
                        icon = Icons.Default.Cloud,
                        label = "Műhold & Felhőzet",
                        onClick = { viewModel.openRadar(RadarLayer.CLOUD) }
                    )
                    QuickRadarChip(
                        icon = Icons.Default.Explore,
                        label = "Globális Szélmodell",
                        onClick = { viewModel.openRadar(RadarLayer.WINDY) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Width Primary Button
                Button(
                    onClick = { viewModel.openRadar(RadarLayer.WIND) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("open_radar_view_button"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appTheme.primaryColor,
                        contentColor = if (appTheme.isDark) Color(0xFF0A0F1D) else Color.White
                    ),
                    shape = RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BEÉPÍTETT TÉRKÉP & SZÉLMODELL MEGNYITÁSA",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Simulator / Flight Time Estimator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
            border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
            shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = appTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REPÜLÉSI IDŐ BECSLŐ (Kalkulátor)",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (appTheme.isDark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Payload Input
                Text(
                    text = "Hozzáadott súly / Kamera / Tartozék: ${payloadWeight.toInt()} g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (appTheme.isDark) Color.White else Color(0xFF0F172A)
                )
                Slider(
                    value = payloadWeight,
                    onValueChange = { payloadWeight = it },
                    valueRange = 0f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = appTheme.primaryColor,
                        activeTrackColor = appTheme.primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Battery Capacity Input
                Text(
                    text = "Akkumulátor kapacitás: ${batteryCapacity.toInt()} mAh",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (appTheme.isDark) Color.White else Color(0xFF0F172A)
                )
                Slider(
                    value = batteryCapacity,
                    onValueChange = { batteryCapacity = it },
                    valueRange = 1500f..4500f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Estimated Output Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appTheme.backgroundColor, RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.5f).dp))
                        .border(appTheme.borderWidthDp.dp, appTheme.cardBorderColor.copy(alpha = 0.5f), RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.5f).dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BECSÜLT MAXIMÁLIS REPÜLÉSI IDŐ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "$estimatedFlightTime perc",
                            style = MaterialTheme.typography.titleLarge,
                            color = appTheme.primaryColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryWidgetCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    primaryValue: String,
    unit: String,
    secondaryBadge: String,
    statusText: String,
    statusColor: Color,
    gaugeProgress: Float = 0.5f
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
        border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
        shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Widget Top Bar: Icon + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(iconTint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Metric Value + Unit
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (appTheme.isDark) Color.White else Color(0xFF0F172A),
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = appTheme.primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini Gauge Progress Bar
            LinearProgressIndicator(
                progress = { gaugeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = iconTint,
                trackColor = appTheme.backgroundColor,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Secondary Sub-value badge & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secondaryBadge,
                    fontSize = 10.sp,
                    color = if (appTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// 2. RULES SCREEN (Repülési szabályzatok)
@Composable
fun RulesScreen(searchQuery: String, viewModel: ChatViewModel) {
    val countries = DroneRules.countries
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            countries
        } else {
            countries.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var expandedCountryName by remember { mutableStateOf<String?>(null) }
    var selectedMapCountry by remember { mutableStateOf<CountryRule?>(null) }

    if (selectedMapCountry != null) {
        CountryAirspaceMapDialog(
            country = selectedMapCountry!!,
            onDismiss = { selectedMapCountry = null },
            onOpenRadar = {
                selectedMapCountry = null
                viewModel.navigateTo(AppScreen.RADAR)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("country_search_input"),
            placeholder = { Text("Keresés ország neve alapján...", color = Color(0xFF94A3B8)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF161F30),
                unfocusedContainerColor = Color(0xFF161F30)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Country List Header
        Text(
            text = "ORSZÁGOK SZABÁLYZATAI ÉS LÉGTÉR TÉRKÉPEK (${filteredCountries.size})",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCountries.isEmpty()) {
            // Empty Search State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nem találtunk országot ezzel a névvel.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCountries) { country ->
                    CountryCard(
                        country = country,
                        isExpanded = expandedCountryName == country.name,
                        onToggleExpand = {
                            expandedCountryName = if (expandedCountryName == country.name) null else country.name
                        },
                        onOpenMap = {
                            selectedMapCountry = country
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CountryCard(
    country: CountryRule,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenMap: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("country_card_${country.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color(0xFF334155)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Country flag & Name top header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = country.flag,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Height,
                                contentDescription = null,
                                tint = if (country.maxAltitude == "0 m") Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Max magasság: ${country.maxAltitude}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (country.maxAltitude == "0 m") Color(0xFFEF4444) else Color(0xFF94A3B8),
                                fontWeight = if (country.maxAltitude == "0 m") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick map trigger button
                    IconButton(
                        onClick = { onOpenMap() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Légtér térkép",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = { onToggleExpand() }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand details",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main summary rule
            Text(
                text = country.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = if (country.maxAltitude == "0 m") Color(0xFFEF4444) else Color.White,
                fontWeight = if (country.maxAltitude == "0 m") FontWeight.Bold else FontWeight.Normal
            )

            // Quick Map Action Pill
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF06B6D4)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Légtér & Zónák", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(country.officialMapUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF94A3B8)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hatósági Portál", fontSize = 12.sp, fontWeight = FontWeight.Normal)
                }
            }

            // Expanded detail section with animated Drone Pulldown / Pullup
            DronePullExpandable(
                isExpanded = isExpanded,
                onToggle = onToggleExpand
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(bottom = 12.dp))

                    // Registration badge block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (country.registrationRequired) Color(0xFF7F1D1D).copy(alpha = 0.2f)
                                else Color(0xFF064E3B).copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (country.registrationRequired) Color(0xFFEF4444).copy(alpha = 0.5f)
                                else Color(0xFF10B981).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (country.registrationRequired) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (country.registrationRequired) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = country.registrationDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    // Restricted zones section
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kiemelt Tiltott / Korlátozott Övezetek:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = country.restrictedZonesSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Részletes helyi szabályzatok:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bullets list
                    country.detailedRules.forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = rule,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryAirspaceMapDialog(
    country: CountryRule,
    onDismiss: () -> Unit,
    onOpenRadar: () -> Unit
) {
    FullAirspaceDialog(
        country = country,
        onDismiss = onDismiss,
        onOpenRadar = onOpenRadar
    )
}

// 3. CHAT SCREEN (Direct community access without auth wall)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onShowAttachment: () -> Unit
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val chatText by viewModel.chatInputText.collectAsStateWithLifecycle()
    val selectedPreset by viewModel.selectedPresetImage.collectAsStateWithLifecycle()
    val customUri by viewModel.customImageUri.collectAsStateWithLifecycle()
    val senderDisplayName by viewModel.senderDisplayName.collectAsStateWithLifecycle()
    val currentPilotUser by viewModel.currentPilotUser.collectAsStateWithLifecycle()
    val chatAuthWarning by viewModel.chatAuthWarning.collectAsStateWithLifecycle()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var fullScreenImageInfo by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var messageToDeleteByAdmin by remember { mutableStateOf<ChatMessage?>(null) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Name change dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "Pilóta Név Megadása",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Add meg a nevet, amellyel az üzeneteid megjelennek a chatben:",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        placeholder = { Text("pl. Drón Pilóta", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF161F30),
                            unfocusedContainerColor = Color(0xFF161F30)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameText.isNotBlank()) {
                            viewModel.setSenderDisplayName(editNameText.trim())
                        }
                        showEditNameDialog = false
                    },
                    shape = RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Mentés", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Mégse", color = Color(0xFF94A3B8))
                }
            },
            containerColor = appTheme.surfaceColor,
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Active Pilot Header Profile Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161F30), RoundedCornerShape(10.dp))
                .border(1.dp, if (currentPilotUser != null) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFF334155), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (currentPilotUser != null) {
                            viewModel.navigateTo(AppScreen.PROFILE)
                        } else {
                            editNameText = senderDisplayName
                            showEditNameDialog = true
                        }
                    }
                    .padding(2.dp)
            ) {
                // Avatar with online status ring
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFF10B981))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatar = currentPilotUser?.avatarUrl
                        if (!avatar.isNullOrBlank() && (avatar.startsWith("emoji:") || avatar.length <= 4)) {
                            val emojiText = if (avatar.startsWith("emoji:")) avatar.removePrefix("emoji:") else avatar
                            Text(
                                text = emojiText,
                                fontSize = 22.sp
                            )
                        } else {
                            Text(
                                text = "🚁",
                                fontSize = 22.sp
                            )
                        }
                    }

                    // Online indicator badge
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (currentPilotUser != null) Color(0xFF10B981) else Color(0xFFEF4444),
                                CircleShape
                            )
                            .border(1.5.dp, Color(0xFF161F30), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = senderDisplayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (currentPilotUser?.email.equals("118liliommarton@gmail.com", ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEAB308).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFEAB308))
                            ) {
                                Text(
                                    text = "👑 Tulajdonos",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEAB308),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (currentPilotUser != null && currentPilotUser!!.isVerifiedPilot) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Hitelesített pilóta",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    val subtitle = if (currentPilotUser != null) {
                        "Hitelesített drónpilóta • Élő csevegés"
                    } else {
                        "⚠️ Nincs bejelentkezve • Olvasási mód"
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentPilotUser != null) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile or Login quick action button
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (currentPilotUser != null) Color(0xFF38BDF8) else Color(0xFF10B981)
                    )
                ) {
                    Icon(
                        imageVector = if (currentPilotUser != null) Icons.Default.AccountCircle else Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentPilotUser != null) "Profil" else "Belépés",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Chat előzmények törlése",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Authentication Required Warning Banner
        if (chatAuthWarning != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("chat_auth_warning_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = chatAuthWarning ?: "",
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row {
                        TextButton(
                            onClick = {
                                viewModel.clearChatAuthWarning()
                                viewModel.navigateTo(AppScreen.PROFILE)
                            }
                        ) {
                            Text(
                                text = "Bejelentkezés",
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.clearChatAuthWarning() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Bezárás",
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Message Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Még nincsenek üzenetek. Kezdd el a beszélgetést!",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        val isMe = message.senderName == senderDisplayName
                        val isOwnerUser = currentPilotUser?.email.equals("118liliommarton@gmail.com", ignoreCase = true)
                        val isOwnerMessage = (isOwnerUser && isMe) ||
                                (isOwnerUser && (message.senderName == currentPilotUser?.effectiveDisplayName || message.senderName == currentPilotUser?.pilotName)) ||
                                message.senderName.contains("118liliommarton", ignoreCase = true)

                        val canDelete = isOwnerUser || isMe || message.senderName.contains("Admin") || message.senderName.contains("Tulajdonos")

                        MessageBubble(
                            message = message,
                            isMe = isMe,
                            isOwner = isOwnerMessage,
                            onImageClick = { uri, sender, caption ->
                                fullScreenImageInfo = Triple(uri, sender, caption)
                            },
                            onDeleteClick = if (canDelete) { { messageToDeleteByAdmin = message } } else null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Image Attachment preview container
        if (selectedPreset != null || customUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val targetUri = selectedPreset ?: customUri
                            if (targetUri != null) {
                                fullScreenImageInfo = Triple(targetUri, senderDisplayName, "Előkészített melléklet")
                            }
                        }
                    ) {
                        // Render miniature preview of selected asset
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            if (selectedPreset != null) {
                                val resId = getDrawableIdByName(selectedPreset!!)
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Preset image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = Uri.parse(customUri),
                                    contentDescription = "Custom image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (selectedPreset != null) "Melléklet: Előbeállítás" else "Melléklet: Saját kép",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Koppints az előnézethez",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.selectPresetImage(null)
                        viewModel.selectCustomImageUri(null)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Eltávolítás",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // Bottom Rich Input Bar (or Login Prompt if not authenticated)
        if (currentPilotUser == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.PROFILE) },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Csak bejelentkezett pilóták írhatnak a chatbe",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Koppints ide a bejelentkezéshez vagy fiók létrehozásához",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Belépés", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment Select button
                IconButton(
                    onClick = onShowAttachment,
                    modifier = Modifier
                        .testTag("attach_button")
                        .background(Color(0xFF1E293B), CircleShape)
                        .size(48.dp)
                        .border(1.dp, Color(0xFF334155), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Kép hozzáadása",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Field
                OutlinedTextField(
                    value = chatText,
                    onValueChange = { viewModel.setChatInputText(it) },
                    placeholder = { Text("Írj egy üzenetet (${currentPilotUser?.effectiveDisplayName})...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF161F30),
                        unfocusedContainerColor = Color(0xFF161F30)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = { viewModel.sendMessage(context) },
                    modifier = Modifier
                        .testTag("chat_send_button")
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Küldés",
                        tint = Color(0xFF0A0F1D)
                    )
                }
            }
        }
    }

    // Fullscreen lightbox popup overlay for zoomed image preview
    if (fullScreenImageInfo != null) {
        val (uri, sender, _) = fullScreenImageInfo!!
        FullScreenImageViewerDialog(
            imageUri = uri,
            senderName = sender,
            onDismiss = { fullScreenImageInfo = null }
        )
    }

    // Owner / Admin Announcement or Message Deletion Dialog
    if (messageToDeleteByAdmin != null) {
        val msg = messageToDeleteByAdmin!!
        val isAnnouncement = msg.senderName.contains("Admin") || msg.senderName.contains("Tulajdonos") || msg.senderName.contains("Rendszer")
        AlertDialog(
            onDismissRequest = { messageToDeleteByAdmin = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAnnouncement) "Tulajdonosi Értesítés Törlése" else "Üzenet Törlése",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isAnnouncement)
                            "Biztosan törölni szeretnéd ezt a közzétett tulajdonosi értesítést? A törlés után az értesítés minden pilótánál azonnal eltűnik a chatből."
                        else
                            "Biztosan törölni szeretnéd ezt az üzenetet a chatből?",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF161F30),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${msg.message}\"",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChatMessage(msg.id)
                        messageToDeleteByAdmin = null
                        Toast.makeText(context, if (isAnnouncement) "Tulajdonosi értesítés törölve!" else "Üzenet törölve!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Törlés", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDeleteByAdmin = null }) {
                    Text("Mégse", color = Color(0xFF94A3B8))
                }
            },
            containerColor = appTheme.surfaceColor,
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp)
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    isOwner: Boolean = false,
    onImageClick: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteClick: (() -> Unit)? = null
) {
    val isAdmin = message.senderName.contains("Admin")
    val bubbleColor = when {
        isMe -> Color(0xFF0284C7) // Distinctive royal blue for user's own comment
        isAdmin -> Color(0xFF1E293B)
        else -> Color(0xFF1E293B)
    }
    val textColor = if (isMe) Color.White else Color(0xFFF8FAFC)
    val secondaryTextColor = if (isMe) Color(0xFFE0F2FE) else Color(0xFF94A3B8)
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start // Left-aligned for all comments
    ) {
        // Sender Name Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, bottom = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isOwner -> Color(0xFFEAB308)
                        isAdmin -> Color(0xFFEAB308)
                        isMe -> Color(0xFF38BDF8)
                        else -> Color(0xFF94A3B8)
                    }
                )
                if (isOwner) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEAB308).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFEAB308))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("👑", fontSize = 9.5.sp)
                            Text(
                                text = "Tulajdonos",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEAB308)
                            )
                        }
                    }
                }
            }

            if (onDeleteClick != null) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Értesítés/Üzenet törlése",
                        tint = Color(0xFFEF4444).copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Main Bubble Surface
        Surface(
            color = bubbleColor,
            shape = shape,
            modifier = Modifier.widthIn(min = 140.dp, max = 300.dp),
            border = BorderStroke(
                width = 1.dp,
                color = when {
                    isMe -> Color(0xFF38BDF8).copy(alpha = 0.8f)
                    isAdmin -> Color(0xFFEAB308).copy(alpha = 0.6f)
                    else -> Color(0xFF334155)
                }
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Loaded Attachment rendering
                if (message.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onImageClick(message.imageUri, message.senderName, message.message) }
                    ) {
                        val isPreset = message.imageUri == "drone_preset_sunset" ||
                                message.imageUri == "drone_preset_forest" ||
                                message.imageUri == "drone_preset_lake" ||
                                message.imageUri == "drone_preset_city"

                        if (isPreset) {
                            val resId = getDrawableIdByName(message.imageUri)
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Attached aerial photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = Uri.parse(message.imageUri),
                                contentDescription = "Attached user photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Zoom Badge Indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Kinagyítás",
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Nagyítás",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (message.message.isNotEmpty()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time Indicator row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Elküldve",
                            tint = Color(0xFFE0F2FE),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImageViewerDialog(
    imageUri: String,
    senderName: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() }
        ) {
            val isPreset = imageUri == "drone_preset_sunset" ||
                    imageUri == "drone_preset_forest" ||
                    imageUri == "drone_preset_lake" ||
                    imageUri == "drone_preset_city"

            // Central Zoomed Image Preview ONLY (no border or frame)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = false, onClick = {})
                ) {
                    if (isPreset) {
                        val resId = getDrawableIdByName(imageUri)
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Kinagyított drónfotó",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 600.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = Uri.parse(imageUri),
                            contentDescription = "Kinagyított drónfotó",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 600.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Header Overlay with Sender Info & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF00F0FF).copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color(0xFF00F0FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = senderName ?: "Közösségi Drónfotó",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kinagyított kép • Érintsd meg a bezáráshoz",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.85f), CircleShape)
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
        }
    }
}

// Custom bottom sheet / overlay to pick images easily
@Composable
fun AttachmentSheet(
    onDismiss: () -> Unit,
    onSelectPreset: (String) -> Unit,
    onSelectCustom: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onSelectCustom(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Inner Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}) // stop click propagate
                .border(
                    BorderStroke(1.dp, Color(0xFF334155)),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KÉP KÜLDÉSE",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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

                Text(
                    text = "Válassz egyet a prémium drónfotóink közül:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                // Presets scrollable Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PresetItem(
                        name = "Naplemente",
                        resId = R.drawable.drone_preset_sunset,
                        onClick = { onSelectPreset("drone_preset_sunset") }
                    )
                    PresetItem(
                        name = "Fenyves",
                        resId = R.drawable.drone_preset_forest,
                        onClick = { onSelectPreset("drone_preset_forest") }
                    )
                    PresetItem(
                        name = "Tengerszem",
                        resId = R.drawable.drone_preset_lake,
                        onClick = { onSelectPreset("drone_preset_lake") }
                    )
                    PresetItem(
                        name = "Városkép",
                        resId = R.drawable.drone_preset_city,
                        onClick = { onSelectPreset("drone_preset_city") }
                    )
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Custom Picker Button
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("gallery_picker_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kép kiválasztása a galériából",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetItem(
    name: String,
    resId: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Utility mapper to load drawables dynamically by local string name
fun getDrawableIdByName(name: String): Int {
    return when (name) {
        "drone_preset_sunset" -> R.drawable.drone_preset_sunset
        "drone_preset_forest" -> R.drawable.drone_preset_forest
        "drone_preset_lake" -> R.drawable.drone_preset_lake
        "drone_preset_city" -> R.drawable.drone_preset_city
        else -> R.drawable.drone_preset_sunset
    }
}

// 4. RADAR & LIVE MAP SCREEN (Időkép Radar, Széltérkép, Villámtérkép, Műhold)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(viewModel: ChatViewModel) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    val currentLayer by viewModel.selectedRadarLayer.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetryState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var showLimitsDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.stopLoading()
                webViewRef?.onPause()
                webViewRef?.destroy()
                webViewRef = null
            } catch (_: Exception) {}
        }
    }

    // Weather limits info dialog
    if (showLimitsDialog) {
        AlertDialog(
            onDismissRequest = { showLimitsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Drón Időjárási Határértékek",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Biztonságos repülési határok és időjárási szabályok drónpilótáknak:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    WeatherRuleItem(
                        icon = Icons.Default.Air,
                        title = "Szélsebesség & Lökés",
                        desc = "• <250g drónok (pl. Mini): max 28 km/h szél\n• Félprofi drónok (Air, Mavic): max 38 km/h\n• Hirtelen széllökés esetén azonnali leszállás javasolt!",
                        accentColor = Color(0xFF06B6D4)
                    )

                    WeatherRuleItem(
                        icon = Icons.Default.WaterDrop,
                        title = "Csapadék & Pára",
                        desc = "• 0 mm csapadék megengedett. A legtöbb drón nem vízálló!\n• Köd és eső zárlatot és motorleállást okozhat.",
                        accentColor = Color(0xFF38BDF8)
                    )

                    WeatherRuleItem(
                        icon = Icons.Default.Bolt,
                        title = "Zivatar & Villám",
                        desc = "• Zivatarcellák közeledtével azonnali leszállás kötelező!\n• Hirtelen leáramló szélvihar (downburst) veszély.",
                        accentColor = Color(0xFFF59E0B)
                    )

                    WeatherRuleItem(
                        icon = Icons.Default.GpsFixed,
                        title = "Geomágneses Kp Index",
                        desc = "• Kp <= 3: Kiváló GPS műholdvétel és pozíciótartás\n• Kp >= 5: Geomágneses vihar, GPS lebegési bizonytalanság.",
                        accentColor = Color(0xFF10B981)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLimitsDialog = false },
                    shape = RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Értem", color = if (appTheme.isDark && appTheme != AppTheme.OLED_BLACK) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = appTheme.surfaceColor,
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Status & Telemetry Strip Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${telemetry.locationName} Légtér",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live Wind & Kp badges
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "💨 ${telemetry.windSpeedKmh} km/h",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (telemetry.windSpeedKmh > 25) Color(0xFFF59E0B) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Kp: ${telemetry.kpIndex}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (telemetry.kpIndex <= 3) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentLayer.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Radar Layer Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RadarLayer.values().forEach { layer ->
                val isSelected = currentLayer == layer
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectRadarLayer(layer) },
                    label = {
                        Text(
                            text = layer.shortTitle,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        val icon = when (layer) {
                            RadarLayer.WIND -> Icons.Default.Air
                            RadarLayer.CLOUD -> Icons.Default.Cloud
                            RadarLayer.WINDY -> Icons.Default.Explore
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF161F30),
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        labelColor = Color(0xFF94A3B8),
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF334155),
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("radar_layer_chip_${layer.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Refresh Button
                OutlinedButton(
                    onClick = {
                        hasError = false
                        webViewRef?.reload()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF161F30)),
                    modifier = Modifier.testTag("radar_reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Újratöltés",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Újratöltés", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                // Limits / Safety rules button
                OutlinedButton(
                    onClick = { showLimitsDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF161F30)),
                    modifier = Modifier.testTag("radar_limits_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Határértékek",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Határértékek", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Open in external browser / Idokep button
            TextButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentLayer.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // fallback
                    }
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("radar_external_browser_button")
            ) {
                Text(
                    text = "Időkép.hu ↗",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar if loading
        if (isLoading) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // WebView Card Viewport
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasError) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "A radarkép betöltése nem sikerült",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kérjük ellenőrizd az internetkapcsolatot vagy nyisd meg a külső böngészőben.",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    webViewRef?.reload()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Újrapróbálás", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentLayer.url))
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Megnyitás Böngészőben", color = Color.White)
                            }
                        }
                    }
                } else {
                    @SuppressLint("SetJavaScriptEnabled")
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.setSupportZoom(true)
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                settings.mediaPlaybackRequiresUserGesture = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false
                                setBackgroundColor(0xFF0F172A.toInt())

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        loadProgress = newProgress
                                        if (newProgress >= 100) {
                                            isLoading = false
                                        }
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                        hasError = false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            hasError = true
                                            isLoading = false
                                        }
                                    }
                                }

                                loadUrl(currentLayer.url)
                                webViewRef = this
                            }
                        },
                        update = { webView ->
                            if (webView.url != currentLayer.url) {
                                webView.loadUrl(currentLayer.url)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("radar_webview")
                    )
                }
            }
        }
    }
}

@Composable
fun QuickRadarChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun WeatherRuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161F30), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}

// ==========================================
// 🌅 POLGÁRI SZÜRKÜLET & ARANYÓRA KALKULÁTOR
// ==========================================

data class SunEvents(
    val sunriseTime: String,
    val sunsetTime: String,
    val goldenHourStartTime: String,
    val civilTwilightEndTime: String,
    val remainingDaylightMinutes: Long,
    val isNight: Boolean,
    val isCivilTwilight: Boolean,
    val isGoldenHour: Boolean,
    val daylightPercentage: Float
)

fun calculateSunEvents(lat: Double, lng: Double, calendar: Calendar = Calendar.getInstance()): SunEvents {
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
    val minuteOfHour = calendar.get(Calendar.MINUTE)
    val currentMinutesFromMidnight = hourOfDay * 60 + minuteOfHour

    // Approximate Solar Declination (rad)
    val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1 + (hourOfDay - 12) / 24.0)
    val eqtime = 229.18 * (0.000075 + 0.001868 * Math.cos(gamma) - 0.032077 * Math.sin(gamma) - 0.014615 * Math.cos(2 * gamma) - 0.040849 * Math.sin(2 * gamma))
    val decl = 0.006918 - 0.399912 * Math.cos(gamma) + 0.070257 * Math.sin(gamma) - 0.006758 * Math.cos(2 * gamma) + 0.000907 * Math.sin(2 * gamma) - 0.002697 * Math.cos(3 * gamma) + 0.00148 * Math.sin(3 * gamma)

    val latRad = Math.toRadians(lat)

    // Zenith angle for Sunrise/Sunset = 90.833°
    val cosZenithSun = Math.cos(Math.toRadians(90.833))
    val haSunArg = (cosZenithSun / (Math.cos(latRad) * Math.cos(decl))) - (Math.tan(latRad) * Math.tan(decl))
    val haSun = Math.toDegrees(Math.acos(haSunArg.coerceIn(-1.0, 1.0)))

    // Hour angle for Civil Twilight (-6° altitude => zenith 96.0°)
    val cosZenithCivil = Math.cos(Math.toRadians(96.0))
    val haCivilArg = (cosZenithCivil / (Math.cos(latRad) * Math.cos(decl))) - (Math.tan(latRad) * Math.tan(decl))
    val haCivil = Math.toDegrees(Math.acos(haCivilArg.coerceIn(-1.0, 1.0)))

    // Solar noon in UTC minutes
    val timezoneOffsetMinutes = calendar.timeZone.getOffset(calendar.timeInMillis) / (1000 * 60)
    val solarNoonUtc = 720 - (4 * lng) - eqtime
    val solarNoonLocal = solarNoonUtc + timezoneOffsetMinutes

    val sunriseMinutes = (solarNoonLocal - haSun * 4).toInt()
    val sunsetMinutes = (solarNoonLocal + haSun * 4).toInt()
    val goldenHourStartMinutes = sunsetMinutes - 60
    val civilTwilightEndMinutes = (solarNoonLocal + haCivil * 4).toInt()

    fun formatMinutes(mins: Int): String {
        val normalized = (mins + 1440) % 1440
        val h = normalized / 60
        val m = normalized % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    val isNight = currentMinutesFromMidnight >= civilTwilightEndMinutes || currentMinutesFromMidnight < sunriseMinutes
    val isCivilTwilight = currentMinutesFromMidnight >= sunsetMinutes && currentMinutesFromMidnight < civilTwilightEndMinutes
    val isGoldenHour = currentMinutesFromMidnight >= goldenHourStartMinutes && currentMinutesFromMidnight < sunsetMinutes

    val remainingMinutes = if (currentMinutesFromMidnight < civilTwilightEndMinutes) {
        (civilTwilightEndMinutes - currentMinutesFromMidnight).toLong()
    } else 0L

    val dayLength = (civilTwilightEndMinutes - sunriseMinutes).coerceAtLeast(1)
    val elapsed = (currentMinutesFromMidnight - sunriseMinutes).coerceIn(0, dayLength)
    val daylightPercentage = (elapsed.toFloat() / dayLength.toFloat()).coerceIn(0f, 1f)

    return SunEvents(
        sunriseTime = formatMinutes(sunriseMinutes),
        sunsetTime = formatMinutes(sunsetMinutes),
        goldenHourStartTime = formatMinutes(goldenHourStartMinutes),
        civilTwilightEndTime = formatMinutes(civilTwilightEndMinutes),
        remainingDaylightMinutes = remainingMinutes,
        isNight = isNight,
        isCivilTwilight = isCivilTwilight,
        isGoldenHour = isGoldenHour,
        daylightPercentage = daylightPercentage
    )
}

@Composable
fun CivilTwilightCalculatorCard(
    latitude: Double = 47.4979,
    longitude: Double = 19.0402,
    locationName: String = "Budapest",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isReminderActive by remember {
        mutableStateOf(com.example.alarm.GoldenHourAlarmScheduler.isAlarmEnabled(context))
    }

    val sunEvents = remember(latitude, longitude) {
        calculateSunEvents(latitude, longitude)
    }

    // Permission launcher for POST_NOTIFICATIONS on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isReminderActive = true
            com.example.alarm.GoldenHourAlarmScheduler.setAlarmEnabled(context, true, latitude, longitude)
            android.widget.Toast.makeText(
                context,
                "🔔 Aranyóra & Szürkület riasztás bekapcsolva! Lezárt képernyőn is értesítést kapsz ${sunEvents.civilTwilightEndTime} előtt 15 perccel.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } else {
            isReminderActive = false
            com.example.alarm.GoldenHourAlarmScheduler.setAlarmEnabled(context, false)
            android.widget.Toast.makeText(
                context,
                "⚠️ Értesítési engedély hiányában a riasztás nem tud megjelenni a zárolt képernyőn.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    val toggleReminder: (Boolean) -> Unit = { active ->
        if (active) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                isReminderActive = true
                com.example.alarm.GoldenHourAlarmScheduler.setAlarmEnabled(context, true, latitude, longitude)
                android.widget.Toast.makeText(
                    context,
                    "🔔 Aranyóra & Szürkület riasztás bekapcsolva! Lezárt képernyőn és háttérben is jelez ${sunEvents.civilTwilightEndTime} előtt 15 perccel.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } else {
            isReminderActive = false
            com.example.alarm.GoldenHourAlarmScheduler.setAlarmEnabled(context, false)
            android.widget.Toast.makeText(context, "🔕 Riasztás kikapcsolva.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val appTheme = com.example.ui.theme.LocalAppTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("civil_twilight_card"),
        colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
        border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
        shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color(0xFFF59E0B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "POLGÁRI SZÜRKÜLET & ARANYÓRA",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (appTheme.isDark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Törvényes nappali repülési határidők ($locationName)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            when {
                                sunEvents.isNight -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                sunEvents.isCivilTwilight -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                sunEvents.isGoldenHour -> Color(0xFFEAB308).copy(alpha = 0.2f)
                                else -> Color(0xFF10B981).copy(alpha = 0.2f)
                            },
                            RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.4f).dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            sunEvents.isNight -> "ÉJSZAKA (TILOS)"
                            sunEvents.isCivilTwilight -> "SZÜRKÜLET"
                            sunEvents.isGoldenHour -> "ARANYÓRA 📸"
                            else -> "NAPPALI REPÜLÉS 🟢"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            sunEvents.isNight -> Color(0xFFEF4444)
                            sunEvents.isCivilTwilight -> Color(0xFFF59E0B)
                            sunEvents.isGoldenHour -> Color(0xFFFACC15)
                            else -> Color(0xFF10B981)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Status Banner
            val bannerBg = when {
                sunEvents.isNight -> Color(0xFFEF4444).copy(alpha = 0.15f)
                sunEvents.isCivilTwilight -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                sunEvents.isGoldenHour -> Color(0xFFEAB308).copy(alpha = 0.15f)
                else -> Color(0xFF10B981).copy(alpha = 0.12f)
            }
            val bannerBorder = when {
                sunEvents.isNight -> Color(0xFFEF4444)
                sunEvents.isCivilTwilight -> Color(0xFFF59E0B)
                sunEvents.isGoldenHour -> Color(0xFFEAB308)
                else -> Color(0xFF10B981)
            }

            Surface(
                color = bannerBg,
                shape = RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.6f).dp),
                border = BorderStroke(1.dp, bannerBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            sunEvents.isNight -> Icons.Default.NightsStay
                            sunEvents.isCivilTwilight -> Icons.Default.WbTwilight
                            sunEvents.isGoldenHour -> Icons.Default.CameraAlt
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        tint = bannerBorder,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                sunEvents.isNight -> "A POLGÁRI SZÜRKÜLET VÉGET ÉRT"
                                sunEvents.isCivilTwilight -> "NAPNYUGTA UTÁN: UTOLSÓ PERCEK!"
                                sunEvents.isGoldenHour -> "ARANYÓRA: FOTÓZÁSHOZ IDEÁLIS"
                                else -> "NAPPALI HÁTRALÉVŐ IDŐSÁV"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = bannerBorder
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                sunEvents.isNight -> "Nappali engedéllyel a felszállás TILOS. Éjszakai repüléshez zöld villogó fényszóró és külön kivilágítás kötelező!"
                                sunEvents.isCivilTwilight -> "Még ${sunEvents.remainingDaylightMinutes} perc van a szürkület végéig (${sunEvents.civilTwilightEndTime}). Drón kivilágítása kötelező!"
                                sunEvents.isGoldenHour -> "Még ${sunEvents.remainingDaylightMinutes} perc van a szürkület végéig (${sunEvents.civilTwilightEndTime}). Tökéletes meleg fények fotózáshoz!"
                                else -> "Még ${sunEvents.remainingDaylightMinutes / 60}ó ${sunEvents.remainingDaylightMinutes % 60}p van hátra a törvényes nappali repülési időből (${sunEvents.civilTwilightEndTime}-ig)."
                            },
                            fontSize = 10.5.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Arc / Timeline Progress
            Text(
                text = "NAPI NAPÁLLÁS ÉS IDŐVONAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E293B))
            ) {
                // Progress bar indicating current day progress
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(sunEvents.daylightPercentage)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFFF59E0B),
                                    Color(0xFFEF4444)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Column Sun Events Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SunTimeMetric(
                    title = "Napkelte",
                    time = sunEvents.sunriseTime,
                    icon = Icons.Default.WbSunny,
                    iconColor = Color(0xFF38BDF8)
                )
                SunTimeMetric(
                    title = "Aranyóra",
                    time = sunEvents.goldenHourStartTime,
                    icon = Icons.Default.Camera,
                    iconColor = Color(0xFFFACC15)
                )
                SunTimeMetric(
                    title = "Napnyugta",
                    time = sunEvents.sunsetTime,
                    icon = Icons.Default.WbTwilight,
                    iconColor = Color(0xFFF59E0B)
                )
                SunTimeMetric(
                    title = "Szürkület Vége",
                    time = sunEvents.civilTwilightEndTime,
                    icon = Icons.Default.Gavel,
                    iconColor = Color(0xFFEF4444),
                    isLawMetric = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFF334155))

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Switch / Button for Reminder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        toggleReminder(!isReminderActive)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (isReminderActive) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = if (isReminderActive) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Riasztás 15 perccel a szürkület vége előtt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = if (isReminderActive) "Aktív riasztás: ${sunEvents.civilTwilightEndTime} előtt 15 perccel (zárolt képernyőn is)" else "Kattints a figyelmeztetés bekapcsolásához",
                            fontSize = 9.5.sp,
                            color = if (isReminderActive) Color(0xFFF59E0B) else Color(0xFF64748B)
                        )
                    }
                }
                Switch(
                    checked = isReminderActive,
                    onCheckedChange = { active ->
                        toggleReminder(active)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFF59E0B),
                        checkedTrackColor = Color(0xFFF59E0B).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }

            if (isReminderActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 A riasztás a napnyugta időpontjában fog jelezni. Teszteld le most:",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = {
                            com.example.alarm.GoldenHourAlarmScheduler.scheduleTestAlarmIn5Seconds(context)
                            android.widget.Toast.makeText(
                                context,
                                "⏱️ Teszt riasztás beállítva! Zárold a képernyőt, 5 másodperc múlva jelezni fog.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Teszt 5 mp", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SunTimeMetric(
    title: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isLawMetric: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            color = if (isLawMetric) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
            fontWeight = if (isLawMetric) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = time,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLawMetric) Color(0xFFEF4444) else Color.White
        )
    }
}

