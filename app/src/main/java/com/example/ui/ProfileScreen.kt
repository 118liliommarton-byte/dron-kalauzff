package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale
import com.example.auth.AuthResult
import com.example.auth.PilotUser

@Composable
fun ProfileScreen(viewModel: ChatViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val pilotUser by viewModel.currentPilotUser.collectAsStateWithLifecycle()

    if (pilotUser == null) {
        AuthCardView(
            isDarkMode = isDarkMode,
            viewModel = viewModel
        )
    } else {
        LoggedInProfileView(
            pilot = pilotUser!!,
            isDarkMode = isDarkMode,
            viewModel = viewModel
        )
    }
}

/**
 * Authentication view: Tabs for Sign In & Registration with responsive validation.
 */
@Composable
private fun AuthCardView(
    isDarkMode: Boolean = true,
    viewModel: ChatViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Bejelentkezés, 1: Regisztráció
    val focusManager = LocalFocusManager.current

    // Input States
    val initialRemember = remember { viewModel.isRememberLoginEnabled() }
    var rememberCredentials by remember { mutableStateOf(initialRemember) }
    var email by remember { mutableStateOf(if (initialRemember) viewModel.getSavedLoginEmail() else "") }
    var password by remember { mutableStateOf(if (initialRemember) viewModel.getSavedLoginPassword() else "") }
    var confirmPassword by remember { mutableStateOf("") }
    var pilotName by remember { mutableStateOf("") }
    var primaryDrone by remember { mutableStateOf("DJI Mini 4 Pro") }
    var licenseType by remember { mutableStateOf("A1/A3 Nyílt kategória") }
    var county by remember { mutableStateOf("Budapest / Pest") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Drón Kalauz Pilótafiók",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )

                Text(
                    text = "Jelentkezz be vagy regisztrálj a felhőalapú szinkronizációhoz, saját hirdetéseid és spotjaid kezeléséhez!",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Row for Sign In vs Register
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("auth_tab_row")
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                            successMessage = null
                        },
                        text = {
                            Text(
                                "Bejelentkezés",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                            successMessage = null
                        },
                        text = {
                            Text(
                                "Új Regisztráció",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Messages
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (successMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(successMessage!!, color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Registration specific inputs
                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = pilotName,
                        onValueChange = { pilotName = it },
                        label = { Text("Pilótanév vagy Teljes név") },
                        placeholder = { Text("pl. Kovács Gergő") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pilot_name_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    OutlinedTextField(
                        value = primaryDrone,
                        onValueChange = { primaryDrone = it },
                        label = { Text("Elsődleges drónod típusa") },
                        placeholder = { Text("pl. DJI Mini 4 Pro, Avata 2, Air 3") },
                        leadingIcon = { Icon(Icons.Default.FlightTakeoff, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_drone_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    OutlinedTextField(
                        value = county,
                        onValueChange = { county = it },
                        label = { Text("Bázis / Vármegye") },
                        placeholder = { Text("pl. Budapest, Pest, Győr-Moson-Sopron") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_county_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                // Common inputs: Email & Password
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail cím") },
                    placeholder = { Text("pilot@pelda.hu") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Jelszó") },
                    placeholder = { Text("Legalább 6 karakter...") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Jelszó elrejtése" else "Jelszó felfedése"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { focusManager.clearFocus() }
                    )
                )

                // Confirm Password (Csak regisztráció esetén)
                if (selectedTab == 1) {
                    val passwordsMatch = password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword
                    val hasError = confirmPassword.isNotEmpty() && password != confirmPassword

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Jelszó megerősítése") },
                        placeholder = { Text("Írd be újra a jelszót...") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        trailingIcon = {
                            if (passwordsMatch) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Jelszavak egyeznek",
                                    tint = Color(0xFF10B981)
                                )
                            } else {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isConfirmPasswordVisible) "Jelszó elrejtése" else "Jelszó felfedése"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        isError = hasError,
                        supportingText = if (hasError) {
                            { Text("A két jelszó nem egyezik meg!", color = MaterialTheme.colorScheme.error) }
                        } else if (passwordsMatch) {
                            { Text("A jelszavak megegyeznek", color = Color(0xFF10B981)) }
                        } else null,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_input"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }

                // Remember Credentials Option (Csak Bejelentkezés fülön)
                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                rememberCredentials = !rememberCredentials
                                if (!rememberCredentials) {
                                    viewModel.setRememberLoginCredentials(false)
                                }
                            }
                            .background(
                                if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF1F5F9)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("auth_remember_me_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberCredentials,
                            onCheckedChange = {
                                rememberCredentials = it
                                if (!it) {
                                    viewModel.setRememberLoginCredentials(false)
                                }
                            },
                            modifier = Modifier.testTag("auth_remember_me_checkbox"),
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Adatok megjegyzése",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "E-mail cím és jelszó megjegyzése ezen az eszközön",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                        if (rememberCredentials && (email.isNotBlank() || password.isNotBlank())) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        errorMessage = null
                        successMessage = null

                        if (selectedTab == 1) {
                            if (password.length < 6) {
                                errorMessage = "A jelszónak legalább 6 karakter hosszúnak kell lennie!"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = "A megadott két jelszó nem egyezik meg! Kérlek ellenőrizd."
                                return@Button
                            }
                        }

                        isLoading = true

                        if (selectedTab == 0) {
                            viewModel.signInWithEmail(
                                email = email,
                                password = password,
                                rememberCredentials = rememberCredentials,
                                onResult = { result ->
                                    isLoading = false
                                    when (result) {
                                        is AuthResult.Success -> {
                                            successMessage = result.message
                                        }
                                        is AuthResult.Error -> {
                                            errorMessage = result.errorMessage
                                        }
                                    }
                                }
                            )
                        } else {
                            viewModel.registerWithEmail(
                                email = email,
                                password = password,
                                pilotName = pilotName,
                                primaryDrone = primaryDrone,
                                license = licenseType,
                                county = county,
                                onResult = { result ->
                                    isLoading = false
                                    when (result) {
                                        is AuthResult.Success -> {
                                            successMessage = result.message
                                        }
                                        is AuthResult.Error -> {
                                            errorMessage = result.errorMessage
                                        }
                                    }
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Login else Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (selectedTab == 0) "Bejelentkezés Pilótaként" else "Fiók Létrehozása",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Google Sign-In Button
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        viewModel.signInWithGoogle(context) { result ->
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> {
                                    successMessage = result.message
                                }
                                is AuthResult.Error -> {
                                    errorMessage = result.errorMessage
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_google_signin_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        contentColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.GTranslate,
                        contentDescription = "Google Bejelentkezés",
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Bejelentkezés Google Fiókkal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }

        // Theme Selection Card
        val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
        AppThemeSelectionCard(
            currentTheme = selectedTheme,
            onSelectTheme = { viewModel.selectTheme(it) }
        )
    }
}

/**
 * Logged In Pilot Profile View: displays pilot details, active fleet, stats, chat profile and profile editing.
 */
@Composable
private fun LoggedInProfileView(
    pilot: PilotUser,
    isDarkMode: Boolean,
    viewModel: ChatViewModel
) {
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }

    val allListings by viewModel.marketplaceListings.collectAsStateWithLifecycle()
    val myListings = remember(allListings, pilot) {
        allListings.filter { listing ->
            (listing.contactEmail != null && listing.contactEmail.equals(pilot.email, ignoreCase = true)) ||
            listing.sellerName.equals(pilot.effectiveDisplayName, ignoreCase = true) ||
            listing.sellerName.equals(pilot.pilotName, ignoreCase = true) ||
            (pilot.nickname.isNotBlank() && listing.sellerName.equals(pilot.nickname, ignoreCase = true)) ||
            listing.isUserCreated
        }
    }

    var selectedListingForDetail by remember { mutableStateOf<com.example.data.MarketplaceListing?>(null) }
    var listingToDelete by remember { mutableStateOf<com.example.data.MarketplaceListing?>(null) }
    var showCreateListingDialog by remember { mutableStateOf(false) }

    var editName by remember(pilot.pilotName) { mutableStateOf(pilot.pilotName) }
    var editNickname by remember(pilot.nickname) { mutableStateOf(pilot.nickname) }
    var editAvatarUrl by remember(pilot.avatarUrl) { mutableStateOf(pilot.avatarUrl) }
    var editDrone by remember(pilot.primaryDrone) { mutableStateOf(pilot.primaryDrone) }
    var editLicense by remember(pilot.pilotLicense) { mutableStateOf(pilot.pilotLicense) }
    var editCounty by remember(pilot.homeCounty) { mutableStateOf(pilot.homeCounty) }
    var editBio by remember(pilot.bio) { mutableStateOf(pilot.bio) }

    // Preset avatars available to choose (drone themed)
    val avatarPresets = listOf(
        "Multikopter" to "🚁",
        "FPV Verseny" to "🛸",
        "Merevszárnyú" to "🛩️",
        "FPV Szemüveg" to "🥽",
        "Drónpilóta" to "👨‍✈️",
        "Légi Kamera" to "📹",
        "Távirányító" to "🎮",
        "Telemetria" to "📡"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pilot Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Box with status badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF10B981))
                                ),
                                CircleShape
                            )
                            .border(3.dp, if (isDarkMode) Color(0xFF1E293B) else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatar = pilot.avatarUrl
                        if (!avatar.isNullOrBlank() && (avatar.startsWith("emoji:") || avatar.length <= 4)) {
                            val emojiText = if (avatar.startsWith("emoji:")) avatar.removePrefix("emoji:") else avatar
                            Text(
                                text = emojiText,
                                fontSize = 40.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "🚁",
                                fontSize = 40.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val isOwner = pilot.email.equals("118liliommarton@gmail.com", ignoreCase = true)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pilot.effectiveDisplayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    if (isOwner) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEAB308).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFEAB308))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("👑", fontSize = 12.sp)
                                Text(
                                    text = "Tulajdonos",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEAB308)
                                )
                            }
                        }
                    }
                    if (pilot.isVerifiedPilot) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Hitelesített pilóta",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (pilot.nickname.isNotBlank() && pilot.nickname != pilot.pilotName) {
                    Text(
                        text = "Pilótanév: ${pilot.pilotName}",
                        fontSize = 12.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                Text(
                    text = pilot.email,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (isOwner) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("👑 Tulajdonos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFEAB308).copy(alpha = 0.15f),
                                labelColor = Color(0xFFEAB308)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.4f))
                        )
                    }

                    SuggestionChip(
                        onClick = {},
                        label = { Text(pilot.pilotLicense, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                            labelColor = Color(0xFF0284C7)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                    )

                    SuggestionChip(
                        onClick = {},
                        label = { Text("HU Pilóta", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                            labelColor = Color(0xFF10B981)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                    )
                }
            }
        }

        // My Marketplace Listings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    Color(0xFF0284C7).copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Saját Hirdetéseim",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "Drón Piac aktív hirdetéseid",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Count Badge & Add Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${myListings.size} db",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { showCreateListingDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF0284C7), CircleShape)
                                .testTag("add_listing_from_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Új hirdetés feladása",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                if (myListings.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = if (isDarkMode) Color(0xFF475569) else Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Még nem adtál fel hirdetést a Drón Piacon",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showCreateListingDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Új Hirdetés Feladása", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        myListings.forEach { listing ->
                            MyProfileListingItem(
                                listing = listing,
                                isDarkMode = isDarkMode,
                                onClickDetail = { selectedListingForDetail = listing },
                                onDelete = { listingToDelete = listing }
                            )
                        }
                    }
                }
            }
        }

        // Details / Edit Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilóta Profil & Élő Chat Beállítások",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )

                    TextButton(
                        onClick = {
                            if (isEditing) {
                                // Save changes
                                viewModel.updatePilotProfile(
                                    pilot.copy(
                                        pilotName = editName,
                                        nickname = editNickname.trim(),
                                        avatarUrl = editAvatarUrl,
                                        primaryDrone = editDrone,
                                        pilotLicense = editLicense,
                                        homeCounty = editCounty,
                                        bio = editBio
                                    )
                                )
                            }
                            isEditing = !isEditing
                        },
                        modifier = Modifier.testTag("edit_profile_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEditing) "Mentés" else "Szerkesztés", fontWeight = FontWeight.Bold)
                    }
                }

                if (!isEditing) {
                    ProfileInfoRow(
                        icon = Icons.Default.ChatBubble,
                        label = "Chat Becenév (Megjelenített név)",
                        value = if (pilot.nickname.isNotBlank()) pilot.nickname else "${pilot.pilotName} (alapértelmezett)",
                        isDarkMode = isDarkMode
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Flight,
                        label = "Elsődleges Drón",
                        value = pilot.primaryDrone,
                        isDarkMode = isDarkMode
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Otthoni Vármegye / Bázis",
                        value = pilot.homeCounty,
                        isDarkMode = isDarkMode
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.CardMembership,
                        label = "Jogosítvány kategória",
                        value = pilot.pilotLicense,
                        isDarkMode = isDarkMode
                    )
                    ProfileInfoRow(
                        icon = Icons.Default.Notes,
                        label = "Bemutatkozás",
                        value = pilot.bio.ifBlank { "Nincs megadva" },
                        isDarkMode = isDarkMode
                    )
                } else {
                    Text(
                        text = "Élő Chat Profil Testreszabása:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        label = { Text("Chat Becenév (ez jelenik meg üzenetküldéskor)") },
                        placeholder = { Text("pl. Falcon99, SkyHunter") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_nickname_input")
                    )

                    Column {
                        Text(
                            text = "Profil Avatar választása:",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            avatarPresets.forEach { (label, emoji) ->
                                val isSelected = editAvatarUrl == "emoji:$emoji"
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                            else if (isDarkMode) Color(0xFF0F172A)
                                            else Color(0xFFF1F5F9)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            editAvatarUrl = "emoji:$emoji"
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else if (isDarkMode) Color.White else Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                    Text(
                        text = "Pilóta Alapadatok & Flotta:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Pilótanév") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDrone,
                        onValueChange = { editDrone = it },
                        label = { Text("Elsődleges Drón") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCounty,
                        onValueChange = { editCounty = it },
                        label = { Text("Vármegye / Bázis") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLicense,
                        onValueChange = { editLicense = it },
                        label = { Text("Jogosítvány Kategória") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bemutatkozás") },
                        placeholder = { Text("Írj pár szót magadról...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                // Logout Button
                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pilot_logout_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                        contentColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kijelentkezés a Fiókból", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Theme Selection Card
        val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
        AppThemeSelectionCard(
            currentTheme = selectedTheme,
            onSelectTheme = { viewModel.selectTheme(it) }
        )
    }

    // Modal dialogs for My Listings
    if (selectedListingForDetail != null) {
        MarketplaceDetailDialog(
            listing = selectedListingForDetail!!,
            onDismiss = { selectedListingForDetail = null },
            onDelete = {
                viewModel.deleteMarketplaceListing(selectedListingForDetail!!.id)
                selectedListingForDetail = null
            }
        )
    }

    if (listingToDelete != null) {
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = { Text("Hirdetés törlése", fontWeight = FontWeight.Bold) },
            text = { Text("Biztosan törölni szeretnéd a(z) \"${listingToDelete?.title}\" nevű hirdetésedet?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        listingToDelete?.let { viewModel.deleteMarketplaceListing(it.id) }
                        listingToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Törlés", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { listingToDelete = null }) {
                    Text("Mégse")
                }
            }
        )
    }

    if (showCreateListingDialog) {
        CreateMarketplaceListingDialog(
            initialSellerName = pilot.effectiveDisplayName,
            initialEmail = pilot.email,
            onDismiss = { showCreateListingDialog = false },
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
                showCreateListingDialog = false
            }
        )
    }
}

@Composable
private fun MyProfileListingItem(
    listing: com.example.data.MarketplaceListing,
    isDarkMode: Boolean,
    onClickDetail: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedPrice = remember(listing.price) {
        if (listing.price <= 0) "Megegyezés szerint"
        else "${NumberFormat.getNumberInstance(Locale("hu", "HU")).format(listing.price)} Ft"
    }

    val firstImageUri = remember(listing.imageUri) {
        listing.getImageUris().firstOrNull()
    }

    val isForSale = listing.type == "ELADÁS"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetail() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(
            1.dp,
            if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image thumbnail or category icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (!firstImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = firstImageUri,
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    val iconText = when (listing.category) {
                        "DJI" -> "🚁"
                        "FPV" -> "🛸"
                        "Autel" -> "🛩️"
                        "Tartozék" -> "📹"
                        else -> "📦"
                    }
                    Text(iconText, fontSize = 24.sp)
                }
            }

            // Info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isForSale) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = listing.type,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isForSale) Color(0xFF10B981) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = listing.category,
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                Text(
                    text = listing.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedPrice,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )

                    Text(
                        text = "• ${listing.location}",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onClickDetail,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Megtekintés",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Törlés",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
            )
        }
    }
}
