package com.example.auth

/**
 * Model representing a logged-in or registered drone pilot.
 */
data class PilotUser(
    val uid: String,
    val email: String,
    val pilotName: String,
    val nickname: String = "", // Becenév a chathoz és a közösségi profilhoz
    val pilotStatus: String = "Online / Készenlétben", // Pl.: Online, Repülésben 🛸, Fotózás 📸, Elfoglalt ⛔
    val pilotLicense: String = "A1/A3 Nyílt kategória", // e.g., A1/A3, A2, Speciális
    val primaryDrone: String = "DJI Mini 4 Pro",
    val homeCounty: String = "Budapest / Pest vármegye",
    val isVerifiedPilot: Boolean = true,
    val avatarUrl: String? = null, // Preset avatar azonosító (pl. "avatar_pilot_1") vagy saját kép URI
    val bio: String = "",
    val isCloudSynced: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
) {
    /**
     * Visszaadja a megjelenítendő nevet: ha van egyedi becenév, akkor azt, egyébként a pilótanevet.
     */
    val effectiveDisplayName: String
        get() = if (nickname.isNotBlank()) nickname.trim() else pilotName.trim().ifBlank { "Drón Pilóta" }
}

sealed class AuthResult {
    data class Success(val user: PilotUser, val message: String) : AuthResult()
    data class Error(val errorMessage: String) : AuthResult()
}
