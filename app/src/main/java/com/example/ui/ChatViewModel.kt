package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.auth.AuthResult
import com.example.auth.DroneAuthService
import com.example.auth.PilotUser
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.DroneRepository
import com.example.data.TelemetryData
import com.example.data.UserProfile
import com.example.util.LocationTelemetryManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class NotificationType {
    ALERT,
    INFO,
    SYSTEM,
    WEATHER,
    MARKETPLACE
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.INFO,
    val targetScreen: AppScreen? = null,
    val isRead: Boolean = false
)

enum class AppScreen {
    HOME,
    RADAR,
    RULES,
    SPOTTER,
    MARKETPLACE,
    EXAM_AND_INSURANCE,
    CHAT,
    PROFILE,
    ADMIN
}

enum class AppTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val isDark: Boolean,
    val primaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardBorderColor: Color,
    val iconEmoji: String,
    // Visual interface & shape styling
    val uiStyleName: String,
    val uiTag: String,
    val cardCornerRadiusDp: Float,
    val buttonCornerRadiusDp: Float,
    val borderWidthDp: Float,
    val topBarBorderColor: Color,
    val backgroundGradientColors: List<Color>,
    // Dialog / Window specific attributes
    val dialogCornerRadiusDp: Float,
    val dialogBorderWidthDp: Float,
    val dialogWindowTag: String,
    // Hero image configuration for main page
    val heroImageRes: Int,
    val heroImageTitle: String,
    val heroImageSubtitle: String,
    val heroBadge: String
) {
    CYBER_DARK(
        id = "cyber_dark",
        displayName = "Sötét Kiber",
        subtitle = "Sci-Fi HUD, szögletes élek és neon ciánkék keretek",
        isDark = true,
        primaryColor = Color(0xFF00F0FF),
        backgroundColor = Color(0xFF070B14),
        surfaceColor = Color(0xFF0F172A),
        cardBorderColor = Color(0xFF00F0FF).copy(alpha = 0.45f),
        iconEmoji = "🌌",
        uiStyleName = "Kiber-Tech HUD",
        uiTag = "[HUD//KIBER-RÁCS]",
        cardCornerRadiusDp = 8f,
        buttonCornerRadiusDp = 6f,
        borderWidthDp = 1.5f,
        topBarBorderColor = Color(0xFF00F0FF).copy(alpha = 0.55f),
        backgroundGradientColors = listOf(Color(0xFF070B14), Color(0xFF0F172A), Color(0xFF0B132B)),
        dialogCornerRadiusDp = 6f,
        dialogBorderWidthDp = 2.0f,
        dialogWindowTag = "[SYS//TERMINAL_HUD]",
        heroImageRes = R.drawable.drone_hero,
        heroImageTitle = "Kiber Térképészeti FPV Drón",
        heroImageSubtitle = "Neon telemetria & Kiber-optika HUD",
        heroBadge = "⚡ HUD 4K // CYBER-LINK"
    ),
    LIGHT_SKY(
        id = "light_sky",
        displayName = "Tiszta Égbolt",
        subtitle = "Légies kerekített formák, lágy tiszta kék kiemelések",
        isDark = false,
        primaryColor = Color(0xFF0284C7),
        backgroundColor = Color(0xFFF0F9FF),
        surfaceColor = Color(0xFFFFFFFF),
        cardBorderColor = Color(0xFFBAE6FD),
        iconEmoji = "☀️",
        uiStyleName = "Légies Aero",
        uiTag = "AERO·PILL",
        cardCornerRadiusDp = 26f,
        buttonCornerRadiusDp = 50f,
        borderWidthDp = 1.0f,
        topBarBorderColor = Color(0xFF38BDF8).copy(alpha = 0.4f),
        backgroundGradientColors = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFF8FAFC)),
        dialogCornerRadiusDp = 28f,
        dialogBorderWidthDp = 1.2f,
        dialogWindowTag = "AERO·MODAL",
        heroImageRes = R.drawable.img_drone_light_sky,
        heroImageTitle = "Nappali Kristály Légifotózás",
        heroImageSubtitle = "Tiszta kék égbolt & napfényes látási viszonyok",
        heroBadge = "☀️ DAYLIGHT CLEAR SKY"
    ),
    OLED_BLACK(
        id = "oled_black",
        displayName = "OLED Éjfél",
        subtitle = "Brutalista éles sarkok, tiszta fekete és pro zöld indikátorok",
        isDark = true,
        primaryColor = Color(0xFF10B981),
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF0C0F12),
        cardBorderColor = Color(0xFF10B981).copy(alpha = 0.5f),
        iconEmoji = "🖤",
        uiStyleName = "Pro Terminál",
        uiTag = "TERMINAL_PRO",
        cardCornerRadiusDp = 3f,
        buttonCornerRadiusDp = 2f,
        borderWidthDp = 1.2f,
        topBarBorderColor = Color(0xFF10B981).copy(alpha = 0.75f),
        backgroundGradientColors = listOf(Color(0xFF000000), Color(0xFF06090A), Color(0xFF000000)),
        dialogCornerRadiusDp = 2f,
        dialogBorderWidthDp = 1.6f,
        dialogWindowTag = "TERMINAL_ROOT",
        heroImageRes = R.drawable.drone_preset_city,
        heroImageTitle = "Éjszakai Taktikai Városi Küldetés",
        heroImageSubtitle = "Lopakodó fekete váz & zöld LED navigáció",
        heroBadge = "⚫ STEALTH NIGHT VISION"
    ),
    BALATON_NAVY(
        id = "balaton_navy",
        displayName = "Balatoni Tengerkék",
        subtitle = "Organikus hullámformák, áttetsző panelek és vízkék árnyalatok",
        isDark = true,
        primaryColor = Color(0xFF38BDF8),
        backgroundColor = Color(0xFF040D1A),
        surfaceColor = Color(0xFF0E2238),
        cardBorderColor = Color(0xFF0284C7).copy(alpha = 0.5f),
        iconEmoji = "🌊",
        uiStyleName = "Balatoni Hullám",
        uiTag = "BALATON_WAVE",
        cardCornerRadiusDp = 18f,
        buttonCornerRadiusDp = 16f,
        borderWidthDp = 1.0f,
        topBarBorderColor = Color(0xFF38BDF8).copy(alpha = 0.45f),
        backgroundGradientColors = listOf(Color(0xFF040D1A), Color(0xFF0B1E36), Color(0xFF0E2E4F)),
        dialogCornerRadiusDp = 18f,
        dialogBorderWidthDp = 1.2f,
        dialogWindowTag = "BALATON_DECK",
        heroImageRes = R.drawable.drone_preset_lake,
        heroImageTitle = "Balatoni Vízparti Repülés",
        heroImageSubtitle = "Türkizkék hullámok & part menti panoráma",
        heroBadge = "🌊 BALATON LAKE PATROL"
    ),
    SUNSET_AMBER(
        id = "sunset_amber",
        displayName = "Arany Naplemente",
        subtitle = "Finom elegancia, alkonyati lila és arany prémium keretek",
        isDark = true,
        primaryColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF150A1C),
        surfaceColor = Color(0xFF23112E),
        cardBorderColor = Color(0xFFF59E0B).copy(alpha = 0.5f),
        iconEmoji = "🌅",
        uiStyleName = "Arany Alkonyat",
        uiTag = "GOLD_EDITION",
        cardCornerRadiusDp = 14f,
        buttonCornerRadiusDp = 10f,
        borderWidthDp = 1.5f,
        topBarBorderColor = Color(0xFFF59E0B).copy(alpha = 0.65f),
        backgroundGradientColors = listOf(Color(0xFF150A1C), Color(0xFF1F0F28), Color(0xFF2E132D)),
        dialogCornerRadiusDp = 14f,
        dialogBorderWidthDp = 1.8f,
        dialogWindowTag = "GOLD_FLIGHT_DECK",
        heroImageRes = R.drawable.drone_preset_sunset,
        heroImageTitle = "Polgári Szürkület & Aranyóra",
        heroImageSubtitle = "Aranyfényű felhőzet & romantikus alkonyat",
        heroBadge = "🌅 GOLDEN HOUR FLIGHT"
    )
}

enum class RadarLayer(
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val url: String
) {
    WIND(
        title = "Élő Széltérkép & Lökés",
        shortTitle = "Széltérkép",
        subtitle = "Aktuális szélerősség és maximális széllökések (km/h)",
        url = "https://www.idokep.hu/szel"
    ),
    CLOUD(
        title = "Műhold & Felhőkép",
        shortTitle = "Felhőzet",
        subtitle = "Látási viszonyok, köd és felhőréteg vastagság",
        url = "https://www.idokep.hu/felhokep"
    ),
    WINDY(
        title = "Interaktív Szélmodell (Windy)",
        shortTitle = "Globális Szélmodell",
        subtitle = "Dinamikus magassági légáramlatok és ECMWF előrejelzés",
        url = "https://embed.windy.com/embed2.html?lat=47.1625&lon=19.5033&detailLat=47.4979&detailLon=19.0402&width=650&height=450&zoom=7&level=surface&overlay=wind&product=ecmwf&metricWind=km%2Fh&metricTemp=%C2%B0C"
    )
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DroneRepository
    private val locationTelemetryManager = LocationTelemetryManager(application)
    private val authService = DroneAuthService(application)

    // Pilot Authentication State
    private val _currentPilotUser = MutableStateFlow<PilotUser?>(authService.getCurrentUser())
    val currentPilotUser: StateFlow<PilotUser?> = _currentPilotUser.asStateFlow()

    // GPS Telemetry State
    private val _telemetryState = MutableStateFlow(TelemetryData())
    val telemetryState: StateFlow<TelemetryData> = _telemetryState.asStateFlow()

    // Navigation and UX State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Admin Mode Hidden Security State (Triggered exclusively via Secret Gestures)
    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private var adminAutoLockJob: Job? = null
    private val ADMIN_INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L // 5 perc (300.000 ms)

    private fun startAdminAutoLockCountdown() {
        adminAutoLockJob?.cancel()
        if (_isAdminUnlocked.value) {
            adminAutoLockJob = viewModelScope.launch {
                delay(ADMIN_INACTIVITY_TIMEOUT_MS)
                if (_currentScreen.value != AppScreen.ADMIN && _isAdminUnlocked.value) {
                    lockAdminMode()
                }
            }
        }
    }

    private fun cancelAdminAutoLockCountdown() {
        adminAutoLockJob?.cancel()
        adminAutoLockJob = null
    }

    fun unlockAdminMode() {
        _isAdminUnlocked.value = true
        _currentScreen.value = AppScreen.ADMIN
        cancelAdminAutoLockCountdown()
    }

    fun lockAdminMode() {
        cancelAdminAutoLockCountdown()
        _isAdminUnlocked.value = false
        if (_currentScreen.value == AppScreen.ADMIN) {
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun toggleAdminMode() {
        if (_isAdminUnlocked.value) {
            lockAdminMode()
        } else {
            unlockAdminMode()
        }
    }

    // App Theme & Appearance State
    private val themePrefs = application.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
    private val initialThemeId = themePrefs.getString("selected_theme_id", AppTheme.CYBER_DARK.id) ?: AppTheme.CYBER_DARK.id
    private val initialTheme = AppTheme.values().find { it.id == initialThemeId } ?: AppTheme.CYBER_DARK

    private val _selectedTheme = MutableStateFlow(initialTheme)
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(initialTheme.isDark)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun selectTheme(theme: AppTheme) {
        _selectedTheme.value = theme
        _isDarkMode.value = theme.isDark
        themePrefs.edit().putString("selected_theme_id", theme.id).apply()
    }

    fun toggleDarkMode() {
        val nextTheme = if (_selectedTheme.value.isDark) AppTheme.LIGHT_SKY else AppTheme.CYBER_DARK
        selectTheme(nextTheme)
    }

    fun setDarkMode(isDark: Boolean) {
        val nextTheme = if (isDark) AppTheme.CYBER_DARK else AppTheme.LIGHT_SKY
        selectTheme(nextTheme)
    }

    // Radar Active Layer State (Default to Live Wind Map)
    private val _selectedRadarLayer = MutableStateFlow(RadarLayer.WIND)
    val selectedRadarLayer: StateFlow<RadarLayer> = _selectedRadarLayer.asStateFlow()

    // Active Pilot Display Name
    private val _senderDisplayName = MutableStateFlow("Drón Pilóta")
    val senderDisplayName: StateFlow<String> = _senderDisplayName.asStateFlow()

    // Auth warning state for live chat
    private val _chatAuthWarning = MutableStateFlow<String?>(null)
    val chatAuthWarning: StateFlow<String?> = _chatAuthWarning.asStateFlow()

    fun dismissChatAuthWarning() {
        _chatAuthWarning.value = null
    }

    fun clearChatAuthWarning() {
        _chatAuthWarning.value = null
    }

    // Auth warning state for marketplace post creation
    private val _marketplaceAuthWarning = MutableStateFlow<String?>(null)
    val marketplaceAuthWarning: StateFlow<String?> = _marketplaceAuthWarning.asStateFlow()

    fun dismissMarketplaceAuthWarning() {
        _marketplaceAuthWarning.value = null
    }

    fun clearMarketplaceAuthWarning() {
        _marketplaceAuthWarning.value = null
    }

    // Chat Inputs
    private val _chatInputText = MutableStateFlow("")
    val chatInputText: StateFlow<String> = _chatInputText.asStateFlow()

    private val _selectedPresetImage = MutableStateFlow<String?>(null)
    val selectedPresetImage: StateFlow<String?> = _selectedPresetImage.asStateFlow()

    private val _customImageUri = MutableStateFlow<String?>(null)
    val customImageUri: StateFlow<String?> = _customImageUri.asStateFlow()

    // Search query for drone rules
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Marketplace Filters & Search
    private val _marketplaceSearchQuery = MutableStateFlow("")
    val marketplaceSearchQuery: StateFlow<String> = _marketplaceSearchQuery.asStateFlow()

    private val _marketplaceCategoryFilter = MutableStateFlow("Mind")
    val marketplaceCategoryFilter: StateFlow<String> = _marketplaceCategoryFilter.asStateFlow()

    private val _marketplaceTypeFilter = MutableStateFlow("Összes") // "Összes", "ELADÁS", "VÉTEL"
    val marketplaceTypeFilter: StateFlow<String> = _marketplaceTypeFilter.asStateFlow()

    // Database reactive lists
    val messages: StateFlow<List<ChatMessage>>
    val marketplaceListings: StateFlow<List<com.example.data.MarketplaceListing>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DroneRepository(database.chatDao(), database.marketplaceDao())

        messages = repository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        marketplaceListings = repository.allMarketplaceListings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedMarketplaceIfEmpty()
        }

        // Keep senderDisplayName in sync if logged in
        _currentPilotUser.value?.let { pilot ->
            _senderDisplayName.value = pilot.effectiveDisplayName
        }
    }

    // Pilot Authentication & Profile Management
    fun signInWithGoogle(activityContext: Context, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = authService.signInWithGoogle(activityContext)
            if (result is AuthResult.Success) {
                _currentPilotUser.value = result.user
                _senderDisplayName.value = result.user.effectiveDisplayName
            }
            onResult(result)
        }
    }

    fun isRememberLoginEnabled(): Boolean = authService.isRememberLoginEnabled()
    fun getSavedLoginEmail(): String = authService.getSavedLoginEmail()
    fun getSavedLoginPassword(): String = authService.getSavedLoginPassword()
    fun setRememberLoginCredentials(remember: Boolean, email: String = "", password: String = "") {
        authService.setRememberLoginCredentials(remember, email, password)
    }

    fun signInWithEmail(
        email: String,
        password: String,
        rememberCredentials: Boolean = false,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authService.signInWithEmail(email, password)
            if (result is AuthResult.Success) {
                _currentPilotUser.value = result.user
                _senderDisplayName.value = result.user.effectiveDisplayName
                authService.setRememberLoginCredentials(rememberCredentials, email, password)
            }
            onResult(result)
        }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        pilotName: String,
        primaryDrone: String,
        license: String,
        county: String,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authService.registerWithEmail(
                email = email,
                password = password,
                pilotName = pilotName,
                primaryDrone = primaryDrone,
                license = license,
                county = county
            )
            // Synchronize current user state with authService state (if user was not automatically logged in because verification is required, _currentPilotUser remains null)
            val updatedUser = authService.getCurrentUser()
            _currentPilotUser.value = updatedUser
            if (updatedUser != null) {
                _senderDisplayName.value = updatedUser.effectiveDisplayName
            }
            onResult(result)
        }
    }

    fun resendVerificationEmail(onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = authService.resendVerificationEmail()
            onResult(result)
        }
    }

    fun getAllRegisteredPilots(): List<PilotUser> {
        return authService.getAllRegisteredPilots()
    }

    fun deleteUserAccount(email: String): Boolean {
        val result = authService.deleteUserAccount(email)
        _currentPilotUser.value = authService.getCurrentUser()
        return result
    }

    fun quickLogin(pilotName: String, primaryDrone: String) {
        viewModelScope.launch {
            val result = authService.quickLogin(pilotName, primaryDrone)
            if (result is AuthResult.Success) {
                _currentPilotUser.value = result.user
                _senderDisplayName.value = result.user.effectiveDisplayName
            }
        }
    }

    fun updatePilotProfile(user: PilotUser) {
        authService.updateProfile(user)
        _currentPilotUser.value = user
        _senderDisplayName.value = user.effectiveDisplayName
    }

    fun signOut() {
        authService.signOut()
        _currentPilotUser.value = null
        _senderDisplayName.value = "Drón Pilóta"
    }

    private fun prepopulateMessages() {
        viewModelScope.launch {
            repository.insertMessage(
                ChatMessage(
                    senderName = "Kovács Gergő",
                    message = "Sziasztok! Repült mostanában valaki a Balaton felett? Be kellett jelenteni a MyDroneSpace-ben?",
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                )
            )
            repository.insertMessage(
                ChatMessage(
                    senderName = "Sipos Márk",
                    message = "Szia! Igen, én tegnap este fotóztam egy gyönyörű naplementét a déli parton, simán jóváhagyta a légtérigénylést az app! Nézzétek ezt a képet:",
                    timestamp = System.currentTimeMillis() - 3600000 + 120000,
                    imageUri = "drone_preset_sunset"
                )
            )
            repository.insertMessage(
                ChatMessage(
                    senderName = "Nagy Anita",
                    message = "Gyönyörű lett ez a fotó, Márk! 😍 Ausztriába készülök a hétvégén drónozni, ott mennyire szigorúak a természetvédelmi területeken a szabályok?",
                    timestamp = System.currentTimeMillis() - 1800000
                )
            )
            repository.insertMessage(
                ChatMessage(
                    senderName = "Varga Dániel",
                    message = "Ausztriában nagyon kell figyelni, nemzeti parkok felett szinte mindenhol repülési tilalom van érvényben, komoly bírságot szabnak ki ha rajtakapnak.",
                    timestamp = System.currentTimeMillis() - 900000
                )
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.ADMIN) {
            cancelAdminAutoLockCountdown()
        } else if (_isAdminUnlocked.value) {
            startAdminAutoLockCountdown()
        }
    }

    fun selectRadarLayer(layer: RadarLayer) {
        _selectedRadarLayer.value = layer
    }

    fun openRadar(layer: RadarLayer = RadarLayer.WIND) {
        _selectedRadarLayer.value = layer
        navigateTo(AppScreen.RADAR)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setChatInputText(text: String) {
        _chatInputText.value = text
    }

    fun selectPresetImage(presetName: String?) {
        _selectedPresetImage.value = presetName
        _customImageUri.value = null // clear other
    }

    fun selectCustomImageUri(uri: String?) {
        _customImageUri.value = uri
        _selectedPresetImage.value = null // clear other
    }

    fun setSenderDisplayName(name: String) {
        _senderDisplayName.value = name
    }

    fun sendMessage(context: android.content.Context? = null) {
        if (context != null && !com.example.util.NetworkUtils.isNetworkAvailable(context)) {
            _chatAuthWarning.value = "⚠️ Nincs internetkapcsolat! Élő chat üzenetek küldéséhez aktív internetkapcsolat szükséges."
            return
        }

        val currentUser = _currentPilotUser.value
        val sender = currentUser?.effectiveDisplayName?.ifBlank { null }
            ?: _senderDisplayName.value.ifBlank { null }
            ?: "Drón Pilóta"

        val text = _chatInputText.value.trim()
        val preset = _selectedPresetImage.value
        val custom = _customImageUri.value

        if (text.isEmpty() && preset == null && custom == null) {
            return
        }

        viewModelScope.launch {
            val imgUri = preset ?: custom
            repository.insertMessage(
                ChatMessage(
                    senderName = sender,
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    imageUri = imgUri
                )
            )
            // Clear inputs
            _chatInputText.value = ""
            _selectedPresetImage.value = null
            _customImageUri.value = null
        }
    }

    fun setMarketplaceSearchQuery(query: String) {
        _marketplaceSearchQuery.value = query
    }

    fun setMarketplaceCategoryFilter(category: String) {
        _marketplaceCategoryFilter.value = category
    }

    fun setMarketplaceTypeFilter(type: String) {
        _marketplaceTypeFilter.value = type
    }

    fun postNewListing(
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
        contactPhone: String,
        contactEmail: String?,
        imageUri: String? = null
    ): Boolean {
        val currentUser = _currentPilotUser.value
        if (currentUser == null) {
            _marketplaceAuthWarning.value = "Hirdetés feladásához kérjük, jelentkezz be a pilótafiókodba!"
            return false
        }

        _marketplaceAuthWarning.value = null
        val resolvedSellerName = sellerName.trim().ifBlank { currentUser.effectiveDisplayName.ifBlank { "Drón Pilóta" } }
        val resolvedEmail = contactEmail?.trim()?.ifBlank { null } ?: currentUser.email

        viewModelScope.launch {
            val newListing = com.example.data.MarketplaceListing(
                title = title.trim(),
                type = type,
                category = category,
                price = price,
                condition = condition,
                location = location.trim(),
                description = description.trim(),
                batteryCycles = batteryCycles?.trim()?.ifBlank { null },
                accessories = accessories?.trim()?.ifBlank { null },
                sellerName = resolvedSellerName,
                contactPhone = contactPhone.trim(),
                contactEmail = resolvedEmail,
                timestamp = System.currentTimeMillis(),
                isUserCreated = true,
                imageUri = imageUri?.trim()?.ifBlank { null }
            )
            repository.insertMarketplaceListing(newListing)
        }
        return true
    }

    fun deleteMarketplaceListing(id: Int) {
        viewModelScope.launch {
            repository.deleteMarketplaceListing(id)
        }
    }

    fun deleteChatMessage(id: Int) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun removeImageFromChatMessage(id: Int) {
        viewModelScope.launch {
            repository.removeImageFromMessage(id)
        }
    }

    // --- Notifications State & Persistence ---
    private val notificationPrefs = application.getSharedPreferences("app_notifications_prefs_v2", Context.MODE_PRIVATE)

    private fun serializeNotification(notif: AppNotification): JSONObject {
        return JSONObject().apply {
            put("id", notif.id)
            put("title", notif.title)
            put("message", notif.message)
            put("timestamp", notif.timestamp)
            put("type", notif.type.name)
            put("targetScreen", notif.targetScreen?.name ?: "")
            put("isRead", notif.isRead)
        }
    }

    private fun deserializeNotification(json: JSONObject): AppNotification? {
        return try {
            val id = json.getString("id")
            val title = json.getString("title")
            val message = json.getString("message")
            val timestamp = json.optLong("timestamp", System.currentTimeMillis())
            val typeStr = json.optString("type", NotificationType.INFO.name)
            val type = try { NotificationType.valueOf(typeStr) } catch (_: Exception) { NotificationType.INFO }
            val targetScreenStr = json.optString("targetScreen", "")
            val targetScreen = if (targetScreenStr.isNotBlank()) {
                try { AppScreen.valueOf(targetScreenStr) } catch (_: Exception) { null }
            } else null
            val isRead = json.optBoolean("isRead", false)
            AppNotification(
                id = id,
                title = title,
                message = message,
                timestamp = timestamp,
                type = type,
                targetScreen = targetScreen,
                isRead = isRead
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun isMarketplaceNotification(notif: AppNotification): Boolean {
        return notif.type == NotificationType.MARKETPLACE ||
               notif.id == "notif_marketplace_1" ||
               notif.id.contains("marketplace", ignoreCase = true) ||
               notif.id.startsWith("listing_created_")
    }

    private fun getDeletedIds(): MutableSet<String> {
        val set = mutableSetOf<String>()
        val jsonStr = notificationPrefs.getString("deleted_ids_json", null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    set.add(array.getString(i))
                }
            } catch (_: Exception) {}
        }
        try {
            val legacy = notificationPrefs.getStringSet("deleted_notification_ids", null)
            if (legacy != null) {
                set.addAll(legacy)
            }
        } catch (_: Exception) {}
        // Always permanently blacklist marketplace notifications
        set.add("notif_marketplace_1")
        return set
    }

    private fun saveDeletedIds(ids: Set<String>) {
        try {
            val array = JSONArray()
            for (id in ids) {
                array.put(id)
            }
            notificationPrefs.edit()
                .putString("deleted_ids_json", array.toString())
                .commit()
        } catch (_: Exception) {}
    }

    private fun addDeletedId(id: String) {
        val current = getDeletedIds()
        current.add(id)
        saveDeletedIds(current)
    }

    private fun addDeletedIds(ids: Collection<String>) {
        val current = getDeletedIds()
        current.addAll(ids)
        saveDeletedIds(current)
    }

    private fun parseNotificationsJson(jsonStr: String, deletedIds: Set<String>): List<AppNotification> {
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<AppNotification>()
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                val notif = deserializeNotification(itemObj)
                if (notif != null && !deletedIds.contains(notif.id) && !isMarketplaceNotification(notif)) {
                    list.add(notif)
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadSavedNotifications(): List<AppNotification> {
        val isAllCleared = notificationPrefs.getBoolean("all_cleared_by_user", false)
        val isInitialized = notificationPrefs.getBoolean("notifications_initialized", false)
        val deletedIds = getDeletedIds()

        if (isAllCleared) {
            // User explicitly cleared notifications; NEVER revive initial/mock notifications!
            val jsonStr = notificationPrefs.getString("saved_notifications", "[]") ?: "[]"
            return parseNotificationsJson(jsonStr, deletedIds)
        }

        if (!isInitialized) {
            // First time initialization: Real informative system notifications (NO marketplace notifications)
            val initial = listOf(
                AppNotification(
                    id = "notif_weather_1",
                    title = "Időjárási & Repülési Feltételek",
                    message = "A jelenlegi szélsebesség és látási viszonyok a legtöbb térségben ideálisak nyílt kategóriás (A1/A3) drónozásra. Ellenőrizd a helyi radart!",
                    timestamp = System.currentTimeMillis() - 15 * 60 * 1000L,
                    type = NotificationType.WEATHER,
                    targetScreen = AppScreen.RADAR,
                    isRead = false
                ),
                AppNotification(
                    id = "notif_rules_1",
                    title = "Kötelező Regisztráció & Biztosítás",
                    message = "Minden kamerás vagy 120 grammnál nehezebb drón esetén kötelező a nyilvántartásba vétel és a felelősségbiztosítás kötése!",
                    timestamp = System.currentTimeMillis() - 2 * 3600 * 1000L,
                    type = NotificationType.ALERT,
                    targetScreen = AppScreen.EXAM_AND_INSURANCE,
                    isRead = false
                ),
                AppNotification(
                    id = "notif_app_welcome",
                    title = "Üdvözöl a Drón Kalauz!",
                    message = "Fedezd fel a valós idejű radart, a repülési szabályzatokat, a drónvizsga tudnivalókat és a közösségi chatet.",
                    timestamp = System.currentTimeMillis() - 24 * 3600 * 1000L,
                    type = NotificationType.INFO,
                    targetScreen = AppScreen.HOME,
                    isRead = true
                )
            ).filter { !deletedIds.contains(it.id) && !isMarketplaceNotification(it) }

            saveNotificationsToPrefs(initial)
            notificationPrefs.edit().putBoolean("notifications_initialized", true).commit()
            return initial
        }

        val jsonStr = notificationPrefs.getString("saved_notifications", "[]") ?: "[]"
        val loaded = parseNotificationsJson(jsonStr, deletedIds)
        saveNotificationsToPrefs(loaded)
        return loaded
    }

    private fun saveNotificationsToPrefs(list: List<AppNotification>) {
        try {
            val jsonArray = JSONArray()
            for (notif in list) {
                if (!isMarketplaceNotification(notif)) {
                    jsonArray.put(serializeNotification(notif))
                }
            }
            notificationPrefs.edit()
                .putString("saved_notifications", jsonArray.toString())
                .commit()
        } catch (_: Exception) {}
    }

    private val _notifications = MutableStateFlow<List<AppNotification>>(loadSavedNotifications())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _notifications.value.count { !it.isRead })

    fun markNotificationAsRead(id: String) {
        val updated = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _notifications.value = updated
        saveNotificationsToPrefs(updated)
    }

    fun markAllNotificationsAsRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        saveNotificationsToPrefs(updated)
    }

    fun deleteNotification(id: String) {
        addDeletedId(id)
        val updated = _notifications.value.filter { it.id != id }
        _notifications.value = updated
        saveNotificationsToPrefs(updated)
        if (updated.isEmpty()) {
            notificationPrefs.edit().putBoolean("all_cleared_by_user", true).commit()
        }
    }

    fun clearAllNotifications() {
        val currentIds = _notifications.value.map { it.id }.toSet()
        val allIdsToBlacklist = currentIds + setOf(
            "notif_weather_1",
            "notif_rules_1",
            "notif_app_welcome",
            "notif_marketplace_1"
        )
        addDeletedIds(allIdsToBlacklist)
        _notifications.value = emptyList()
        notificationPrefs.edit()
            .putBoolean("all_cleared_by_user", true)
            .putBoolean("notifications_initialized", true)
            .putString("saved_notifications", "[]")
            .commit()
    }

    fun addNotification(notification: AppNotification) {
        if (isMarketplaceNotification(notification)) {
            return
        }
        val deletedIds = getDeletedIds()
        if (deletedIds.contains(notification.id)) {
            return
        }
        // Clean any previously deleted or marketplace notifications and prepend the new one
        val filtered = _notifications.value.filter { 
            it.id != notification.id && !deletedIds.contains(it.id) && !isMarketplaceNotification(it) 
        }
        val updated = listOf(notification) + filtered
        _notifications.value = updated
        notificationPrefs.edit().putBoolean("all_cleared_by_user", false).commit()
        saveNotificationsToPrefs(updated)
    }

    fun broadcastAdminAnnouncement(announcementText: String) {
        if (announcementText.isBlank()) return
        viewModelScope.launch {
            repository.insertMessage(
                ChatMessage(
                    senderName = "👑 Rendszer Adminisztrátor",
                    message = announcementText.trim(),
                    timestamp = System.currentTimeMillis()
                )
            )
            // Push high-priority system notification to users and persist
            val newNotif = AppNotification(
                title = "👑 Hivatalos Admin Közlemény",
                message = announcementText.trim(),
                timestamp = System.currentTimeMillis(),
                type = NotificationType.SYSTEM,
                targetScreen = AppScreen.CHAT,
                isRead = false
            )
            addNotification(newNotif)
        }
    }

    fun resetMarketplaceToDefaults() {
        viewModelScope.launch {
            repository.resetMarketplaceToDefault()
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearMessages()
        }
    }

    fun refreshLocationTelemetry() {
        viewModelScope.launch {
            _telemetryState.value = _telemetryState.value.copy(isLoading = true)
            val location = locationTelemetryManager.fetchCurrentLocation()
            if (location != null) {
                val realData = locationTelemetryManager.fetchRealTelemetry(location)
                _telemetryState.value = realData
            } else {
                // If location not obtained, update with simulated Budapest/offline data
                _telemetryState.value = _telemetryState.value.copy(
                    isLoading = false,
                    isGpsActive = false,
                    statusText = "GPS jel keresése (Alapértelmezett koordináták)"
                )
            }
        }
    }

    // --- SPOTTER LOCATIONS & FIELD DRAFTS MANAGED STATE ---
    private val spotterPrefs = application.getSharedPreferences("spotter_prefs", Context.MODE_PRIVATE)
    private val draftsPrefs = application.getSharedPreferences("spotter_field_drafts", Context.MODE_PRIVATE)

    private val DEFAULT_SPOTTER_LOCATIONS = listOf(
        SpotterLocation(
            id = "1",
            name = "Szigligeti Vár",
            county = "Veszprém megye (Balaton)",
            category = SpotterCategory.CASTLES,
            lat = 46.7869,
            lng = 17.4367,
            bestTime = "🌅 Naplemente (Aranyóra)",
            airspaceStatus = "🟢 Szabad Légtér (Max 120m VLOS)",
            airspaceColor = Color(0xFF10B981),
            advice = "A várhegy lábánál van parkoló. Szeles időben a Balaton felőli áramlatok miatt óvatosan manőverezz a falak közelében!",
            recommendedDrone = "C0 / C1 Cinematic drónok (DJI Mini 4 Pro, Air 3)"
        ),
        SpotterLocation(
            id = "2",
            name = "Prédikálószék & Dunakanyar",
            county = "Pest megye (Visegrádi-hegység)",
            category = SpotterCategory.MOUNTAINS,
            lat = 47.7208,
            lng = 18.9189,
            bestTime = "🌤️ Kora reggeli ködös napkelte",
            airspaceStatus = "🟡 Duna-Ipoly Nemzeti Park (Fokozott oltalom)",
            airspaceColor = Color(0xFFF59E0B),
            advice = "Fészkelési időszakban (tavasz) ne közelítsd meg a sziklafalakat! A panoráma kereszt mellett jó a műholdjel.",
            recommendedDrone = "Cinematic / FPV Versenydrón"
        ),
        SpotterLocation(
            id = "3",
            name = "Bokodi Lebegő Falu",
            county = "Komárom-Esztergom megye",
            category = SpotterCategory.LAKES,
            lat = 47.4925,
            lng = 18.2547,
            bestTime = "🌆 Késő délutáni tükröződés",
            airspaceStatus = "🟢 Szabad Légtér (120m gátig)",
            airspaceColor = Color(0xFF10B981),
            advice = "Magánstégek övezik, ne repülj alacsonyan a kertek felett! A tó túlsó oldala csendesebb felszállópont.",
            recommendedDrone = "C0 Ultra light (<249g)"
        ),
        SpotterLocation(
            id = "4",
            name = "Bory-vár (Székesfehérvár)",
            county = "Fejér megye",
            category = SpotterCategory.CASTLES,
            lat = 47.2025,
            lng = 18.4419,
            bestTime = "☀️ Kora délelőtti napfény",
            airspaceStatus = "🟡 Lakott terület feletti repülés (Eseti légtér javasolt)",
            airspaceColor = Color(0xFFF59E0B),
            advice = "A tornyok és szobrok miatt vizuális rálátás (VLOS) kötelező! A szomszédos utcákból kényelmesen fel lehet szállni.",
            recommendedDrone = "C0 Kategória"
        ),
        SpotterLocation(
            id = "5",
            name = "Megyer-hegyi Tengerszem",
            county = "Borsod-Abaúj-Zemplén megye",
            category = SpotterCategory.LAKES,
            lat = 48.3586,
            lng = 21.5714,
            bestTime = "🍂 Őszi napsütés (Déli órák)",
            airspaceStatus = "🟡 Természetvédelmi Terület",
            airspaceColor = Color(0xFFF59E0B),
            advice = "A kanyon sziklafalai árnyékolhatják a GPS jelet! Használj vizuális pozicionálást és emelkedj a perem fölé.",
            recommendedDrone = "FPV Freestyle / Cinematic"
        ),
        SpotterLocation(
            id = "6",
            name = "Tihany - Belső-tó & Apátság",
            county = "Veszprém megye",
            category = SpotterCategory.CASTLES,
            lat = 46.9142,
            lng = 17.8894,
            bestTime = "🌅 Naplemente & Levendulaszüret",
            airspaceStatus = "🟡 Balaton-felvidéki Nemzeti Park",
            airspaceColor = Color(0xFFF59E0B),
            advice = "Nyáron nagy a turistaforgalom. A Belső-tó partjáról nyílik a legszebb akadálymentes rálátás az Apátságra.",
            recommendedDrone = "C0 / C1 Drónok"
        ),
        SpotterLocation(
            id = "7",
            name = "Egerszalóki Sódomb & Hőforrás",
            county = "Heves megye",
            category = SpotterCategory.URBAN,
            lat = 47.8542,
            lng = 20.3236,
            bestTime = "🌃 Esti kivilágítás / Kora reggel",
            airspaceStatus = "🟢 Szabad Légtér",
            airspaceColor = Color(0xFF10B981),
            advice = "Gőzölgő meleg víz felett a pára lecsapódhat a lencsére. Tarts megfelelő biztonsági magasságot a mészkőmedencéktől!",
            recommendedDrone = "C0 Mini Drónok"
        ),
        SpotterLocation(
            id = "8",
            name = "Visegrádi Fellegvár",
            county = "Pest megye",
            category = SpotterCategory.CASTLES,
            lat = 47.7944,
            lng = 18.9814,
            bestTime = "🌄 Kora reggeli napkelte",
            airspaceStatus = "🔴 Mytria / Korlátozott Légtér közelében",
            airspaceColor = Color(0xFFEF4444),
            advice = "Nagy magasságú hegycsúcs: az erős szélviharok miatt ellenőrizd a széllökéseket a felszállás előtt!",
            recommendedDrone = "C1 / C2 Szélálló drónok"
        )
    )

    private val _spotterLocations = MutableStateFlow<List<SpotterLocation>>(loadSpotterLocations())
    val spotterLocations: StateFlow<List<SpotterLocation>> = _spotterLocations.asStateFlow()

    private val _fieldDrafts = MutableStateFlow<List<FieldGpsDraft>>(loadFieldDrafts())
    val fieldDrafts: StateFlow<List<FieldGpsDraft>> = _fieldDrafts.asStateFlow()

    private fun loadSpotterLocations(): List<SpotterLocation> {
        val rawJson = spotterPrefs.getString("locations_list", null) ?: return emptyList()
        val result = mutableListOf<SpotterLocation>()
        try {
            val arr = JSONArray(rawJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val catName = obj.optString("category", SpotterCategory.PARKS.name)
                val category = try { SpotterCategory.valueOf(catName) } catch (_: Exception) { SpotterCategory.PARKS }
                val colorLong = obj.optLong("airspaceColor", 0xFF10B981)
                result.add(
                    SpotterLocation(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Fotós Spot"),
                        county = obj.optString("county", ""),
                        category = category,
                        lat = obj.optDouble("lat", 47.3755),
                        lng = obj.optDouble("lng", 18.9230),
                        bestTime = obj.optString("bestTime", "🌅 Naplemente"),
                        airspaceStatus = obj.optString("airspaceStatus", "🟢 Szabad Légtér"),
                        airspaceColor = Color(colorLong.toULong()),
                        advice = obj.optString("advice", ""),
                        recommendedDrone = obj.optString("recommendedDrone", "C0 Mini Drónok"),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        userSubmitted = obj.optBoolean("userSubmitted", false)
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return result
    }

    private fun saveSpotterLocations(list: List<SpotterLocation>) {
        try {
            val arr = JSONArray()
            for (s in list) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("name", s.name)
                obj.put("county", s.county)
                obj.put("category", s.category.name)
                obj.put("lat", s.lat)
                obj.put("lng", s.lng)
                obj.put("bestTime", s.bestTime)
                obj.put("airspaceStatus", s.airspaceStatus)
                obj.put("airspaceColor", s.airspaceColor.value.toLong())
                obj.put("advice", s.advice)
                obj.put("recommendedDrone", s.recommendedDrone)
                obj.put("isFavorite", s.isFavorite)
                obj.put("userSubmitted", s.userSubmitted)
                arr.put(obj)
            }
            spotterPrefs.edit().putString("locations_list", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadFieldDrafts(): List<FieldGpsDraft> {
        val rawJson = draftsPrefs.getString("drafts_list", null) ?: return emptyList()
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

    private fun saveFieldDrafts(list: List<FieldGpsDraft>) {
        try {
            val arr = JSONArray()
            for (d in list) {
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
            draftsPrefs.edit().putString("drafts_list", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addSpotterLocation(spot: SpotterLocation) {
        val updated = listOf(spot) + _spotterLocations.value
        _spotterLocations.value = updated
        saveSpotterLocations(updated)
    }

    fun updateSpotterLocation(spot: SpotterLocation) {
        val updated = _spotterLocations.value.map { if (it.id == spot.id) spot else it }
        _spotterLocations.value = updated
        saveSpotterLocations(updated)
    }

    fun deleteSpotterLocation(id: String) {
        val updated = _spotterLocations.value.filter { it.id != id }
        _spotterLocations.value = updated
        saveSpotterLocations(updated)
    }

    fun resetSpotterLocationsToDefaults() {
        _spotterLocations.value = DEFAULT_SPOTTER_LOCATIONS
        saveSpotterLocations(DEFAULT_SPOTTER_LOCATIONS)
    }

    fun addFieldDraft(draft: FieldGpsDraft) {
        val updated = listOf(draft) + _fieldDrafts.value
        _fieldDrafts.value = updated
        saveFieldDrafts(updated)
    }

    fun deleteFieldDraft(id: String) {
        val updated = _fieldDrafts.value.filter { it.id != id }
        _fieldDrafts.value = updated
        saveFieldDrafts(updated)
    }

    fun clearAllFieldDrafts() {
        _fieldDrafts.value = emptyList()
        saveFieldDrafts(emptyList())
    }
}
