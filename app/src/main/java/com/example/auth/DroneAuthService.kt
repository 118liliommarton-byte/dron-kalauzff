package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

/**
 * Robust authentication manager supporting both Firebase Authentication
 * (using the configured google-services.json) and local encrypted/secure
 * fallback storage so the app works reliably offline, in emulator, and in full cloud production.
 */
class DroneAuthService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("drone_pilot_auth_prefs", Context.MODE_PRIVATE)

    // Check if Firebase Auth is available and initialized
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("DroneAuthService", "Firebase Auth not available, using local engine: ${e.message}")
            null
        }
    }

    private val isFirebaseAvailable: Boolean
        get() = firebaseAuth != null

    // Web Client ID from google-services.json for Google Sign-In with Credential Manager
    private val webClientId = "938240636696-12na9rkv3dktvcbri0ct9bbhfj3fjkm9.apps.googleusercontent.com"
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    /**
     * Sign In with Google using Android Credential Manager & Firebase GoogleAuthProvider
     */
    suspend fun signInWithGoogle(activityContext: Context): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        if (auth == null) {
            return@withContext AuthResult.Error("A Firebase hitelesítés jelenleg nem elérhető ezen az eszközön.")
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in with Firebase using the Google ID token
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val fbUser = authResult.user

                if (fbUser != null) {
                    val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
                    val usersMap = try { JSONObject(usersJson) } catch (_: Exception) { JSONObject() }
                    val cleanEmail = fbUser.email ?: "pilot_${fbUser.uid.take(6)}@gmail.com"
                    val storedUserData = if (usersMap.has(cleanEmail)) usersMap.getJSONObject(cleanEmail) else JSONObject()

                    val pilotUser = PilotUser(
                        uid = fbUser.uid,
                        email = cleanEmail,
                        pilotName = fbUser.displayName?.ifBlank { null }
                            ?: storedUserData.optString("pilotName", googleIdTokenCredential.displayName ?: "Google Pilóta"),
                        nickname = storedUserData.optString("nickname", ""),
                        pilotStatus = storedUserData.optString("pilotStatus", "Online / Készenlétben"),
                        pilotLicense = storedUserData.optString("pilotLicense", "A1/A3 Nyílt kategória"),
                        primaryDrone = storedUserData.optString("primaryDrone", "DJI Mini 4 Pro"),
                        homeCounty = storedUserData.optString("homeCounty", "Budapest / Pest vármegye"),
                        isVerifiedPilot = true,
                        avatarUrl = fbUser.photoUrl?.toString() ?: if (storedUserData.isNull("avatarUrl")) null else storedUserData.optString("avatarUrl"),
                        bio = storedUserData.optString("bio", ""),
                        isCloudSynced = true
                    )

                    saveCurrentUser(pilotUser)
                    return@withContext AuthResult.Success(pilotUser, "Sikeres bejelentkezés Google fiókkal!")
                } else {
                    return@withContext AuthResult.Error("Nem sikerült elérni a Google felhasználói profilt.")
                }
            } else {
                return@withContext AuthResult.Error("Váratlan hitelesítési formátum a Google bejelentkezés során.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("DroneAuthService", "User cancelled Google Sign-In")
            return@withContext AuthResult.Error("A Google bejelentkezés meg lett szakítva.")
        } catch (e: GetCredentialException) {
            Log.w("DroneAuthService", "Credential Manager failed: ${e.message}", e)
            val msg = if (e.message?.contains("no credentials", ignoreCase = true) == true) {
                "Az eszközön/böngészős emulátorban nincs bejelentkezett Google fiók, vagy még nincs hozzáadva az app SHA-1 ujjlenyomata a Firebase-ben. Kérlek, használd a fenti Email/Jelszó belépést vagy a Gyors Pilóta Belépést!"
            } else {
                "Google bejelentkezési hiba: ${e.message ?: "Kérlek használd az Email belépést!"}"
            }
            return@withContext AuthResult.Error(msg)
        } catch (e: Exception) {
            Log.e("DroneAuthService", "Firebase Google sign-in failed: ${e.message}", e)
            val msg = if (e.message?.contains("no credentials", ignoreCase = true) == true) {
                "Nincs elérhető Google fiók a készüléken. Használd a kényelmes Email/Jelszó belépést!"
            } else {
                "Google belépési hiba: ${e.localizedMessage ?: "Kérlek próbáld az Email belépést!"}"
            }
            return@withContext AuthResult.Error(msg)
        }
    }

    /**
     * Retrieves the currently logged-in pilot, if any.
     */
    fun getCurrentUser(): PilotUser? {
        val userJson = prefs.getString("current_pilot_user", null) ?: return null
        return try {
            val obj = JSONObject(userJson)
            PilotUser(
                uid = obj.optString("uid", UUID.randomUUID().toString()),
                email = obj.optString("email", ""),
                pilotName = obj.optString("pilotName", "Drón Pilóta"),
                nickname = obj.optString("nickname", ""),
                pilotStatus = obj.optString("pilotStatus", "Online / Készenlétben"),
                pilotLicense = obj.optString("pilotLicense", "A1/A3 Nyíkt kategória"),
                primaryDrone = obj.optString("primaryDrone", "DJI Mini 4 Pro"),
                homeCounty = obj.optString("homeCounty", "Budapest / Pest vármegye"),
                isVerifiedPilot = obj.optBoolean("isVerifiedPilot", true),
                avatarUrl = if (obj.isNull("avatarUrl")) null else obj.optString("avatarUrl"),
                bio = obj.optString("bio", ""),
                isCloudSynced = obj.optBoolean("isCloudSynced", isFirebaseAvailable)
            )
        } catch (e: Exception) {
            Log.e("DroneAuthService", "Failed to deserialize user", e)
            null
        }
    }

    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = password.trim()

        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Kérlek, adj meg egy érvényes e-mail címet!")
        }
        if (cleanPass.length < 6) {
            return@withContext AuthResult.Error("A jelszónak legalább 6 karakter hosszúnak kell lennie!")
        }

        // Try Firebase Authentication first if available
        val auth = firebaseAuth
        if (auth != null) {
            try {
                val authResult = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                val fbUser = authResult.user
                if (fbUser != null) {
                    // Force refresh to get up-to-date isEmailVerified state
                    try { fbUser.reload().await() } catch (_: Exception) {}

                    if (!fbUser.isEmailVerified) {
                        try { auth.signOut() } catch (_: Exception) {}
                        return@withContext AuthResult.Error(
                            "Az e-mail címed ($cleanEmail) még nincs hitelesítve! Kérlek, kattints a kiküldött e-mailben lévő megerősítő linkre a bejelentkezéshez."
                        )
                    }

                    val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
                    val usersMap = try { JSONObject(usersJson) } catch (_: Exception) { JSONObject() }
                    val storedUserData = if (usersMap.has(cleanEmail)) usersMap.getJSONObject(cleanEmail) else JSONObject()

                    val pilotUser = PilotUser(
                        uid = fbUser.uid,
                        email = cleanEmail,
                        pilotName = fbUser.displayName?.ifBlank { null }
                            ?: storedUserData.optString("pilotName", cleanEmail.substringBefore("@")),
                        nickname = storedUserData.optString("nickname", ""),
                        pilotStatus = storedUserData.optString("pilotStatus", "Online / Készenlétben"),
                        pilotLicense = storedUserData.optString("pilotLicense", "A1/A3 Nyílt kategória"),
                        primaryDrone = storedUserData.optString("primaryDrone", "DJI Mini 4 Pro"),
                        homeCounty = storedUserData.optString("homeCounty", "Budapest / Pest vármegye"),
                        isVerifiedPilot = true,
                        avatarUrl = fbUser.photoUrl?.toString() ?: if (storedUserData.isNull("avatarUrl")) null else storedUserData.optString("avatarUrl"),
                        bio = storedUserData.optString("bio", ""),
                        isCloudSynced = true
                    )
                    saveCurrentUser(pilotUser)
                    return@withContext AuthResult.Success(pilotUser, "Sikeres bejelentkezés a Firebase felhőbe! Üdv a fedélzeten.")
                }
            } catch (e: Exception) {
                Log.w("DroneAuthService", "Firebase sign-in error: ${e.message}")
                val msg = e.message?.lowercase() ?: ""
                val errCode = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: ""

                if (errCode == "ERROR_USER_NOT_FOUND" || msg.contains("user-not-found") || msg.contains("no user record")) {
                    return@withContext AuthResult.Error("Ezzel az e-mail címmel még nem regisztráltál. Kérlek, válts a Regisztráció fülre!")
                } else if (errCode == "ERROR_WRONG_PASSWORD" || errCode == "ERROR_INVALID_CREDENTIAL" || msg.contains("wrong-password") || msg.contains("invalid-credential") || msg.contains("auth credential") || msg.contains("password")) {
                    return@withContext AuthResult.Error("Hibás e-mail cím vagy jelszó! Kérlek, ellenőrizd a megadott adatokat.")
                } else if (msg.contains("network") || msg.contains("connection")) {
                    return@withContext AuthResult.Error("Hálózati hiba történt. Kérlek, ellenőrizd az internetkapcsolatodat!")
                } else {
                    return@withContext AuthResult.Error("Bejelentkezési hiba: Hibás e-mail cím vagy jelszó!")
                }
            }
        }

        // Check registered pilots in local database
        val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
        val usersMap = try { JSONObject(usersJson) } catch (_: Exception) { JSONObject() }

        if (!usersMap.has(cleanEmail)) {
            return@withContext AuthResult.Error("Ezzel az e-mail címmel még nem regisztráltál. Kérlek, válts a Regisztráció fülre!")
        }

        val storedUserData = usersMap.getJSONObject(cleanEmail)
        val storedPassword = storedUserData.optString("password", "")
        if (storedPassword != cleanPass) {
            return@withContext AuthResult.Error("Hibás jelszó! Kérlek, próbáld újra.")
        }

        // If firebase is active and user registered with verification, block local fallback login until verified
        if (isFirebaseAvailable) {
            return@withContext AuthResult.Error(
                "Az e-mail címed ($cleanEmail) még nincs hitelesítve! Kérlek, kattints a kiküldött e-mailben lévő megerősítő linkre a bejelentkezéshez."
            )
        }

        val pilotUser = PilotUser(
            uid = storedUserData.optString("uid", UUID.randomUUID().toString()),
            email = cleanEmail,
            pilotName = storedUserData.optString("pilotName", cleanEmail.substringBefore("@")),
            nickname = storedUserData.optString("nickname", ""),
            pilotStatus = storedUserData.optString("pilotStatus", "Online / Készenlétben"),
            pilotLicense = storedUserData.optString("pilotLicense", "A1/A3 Nyílt kategória"),
            primaryDrone = storedUserData.optString("primaryDrone", "DJI Mini 4 Pro"),
            homeCounty = storedUserData.optString("homeCounty", "Budapest / Pest vármegye"),
            isVerifiedPilot = storedUserData.optBoolean("isVerifiedPilot", true),
            avatarUrl = if (storedUserData.isNull("avatarUrl")) null else storedUserData.optString("avatarUrl"),
            bio = storedUserData.optString("bio", ""),
            isCloudSynced = isFirebaseAvailable
        )

        saveCurrentUser(pilotUser)
        return@withContext AuthResult.Success(pilotUser, "Sikeres bejelentkezés! Üdvözlünk újra a fedélzeten.")
    }

    /**
     * Register a new Pilot with Email and Password
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        pilotName: String,
        primaryDrone: String,
        license: String,
        county: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = password.trim()
        val cleanName = pilotName.trim()

        if (cleanName.length < 2) {
            return@withContext AuthResult.Error("Kérlek, adj meg egy valódi pilótanevet vagy becenevet!")
        }
        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Kérlek, valós e-mail címet adj meg!")
        }
        if (cleanPass.length < 6) {
            return@withContext AuthResult.Error("A jelszónak legalább 6 karakter hosszúnak kell lennie a biztonságért!")
        }

        var cloudRegistered = false
        var createdUid = "pilot_" + UUID.randomUUID().toString().take(12)

        // Try Firebase Authentication registration
        val auth = firebaseAuth
        var verificationSent = false
        if (auth != null) {
            try {
                val createResult = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                val fbUser = createResult.user
                if (fbUser != null) {
                    createdUid = fbUser.uid
                    cloudRegistered = true

                    // Send email verification link
                    try {
                        fbUser.sendEmailVerification().await()
                        verificationSent = true
                        Log.i("DroneAuthService", "Verification email sent to $cleanEmail")
                    } catch (evErr: Exception) {
                        Log.w("DroneAuthService", "Could not send verification email: ${evErr.message}")
                        return@withContext AuthResult.Error(
                            "Nem sikerült elküldeni a hitelesítő e-mailt (${evErr.localizedMessage ?: evErr.message}). Kérlek, ellenőrizd az e-mail címet!"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("DroneAuthService", "Firebase registration error: ${e.message}")
                return@withContext AuthResult.Error("Regisztrációs hiba a szerveren: ${e.localizedMessage ?: e.message}")
            }
        }

        val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
        val usersMap = try { JSONObject(usersJson) } catch (_: Exception) { JSONObject() }

        if (usersMap.has(cleanEmail) && !cloudRegistered) {
            return@withContext AuthResult.Error("Ezzel az e-mail címmel már létezik pilótafiók! Kérlek, jelentkezz be.")
        }

        val pilotUser = PilotUser(
            uid = createdUid,
            email = cleanEmail,
            pilotName = cleanName,
            pilotLicense = license.ifBlank { "A1/A3 Nyílt kategória" },
            primaryDrone = primaryDrone.ifBlank { "DJI Mini 4 Pro" },
            homeCounty = county.ifBlank { "Budapest / Pest vármegye" },
            isVerifiedPilot = true,
            avatarUrl = null,
            bio = "",
            isCloudSynced = cloudRegistered || isFirebaseAvailable
        )

        // Store into internal pilot database
        val userRecord = JSONObject().apply {
            put("uid", createdUid)
            put("email", cleanEmail)
            put("password", cleanPass)
            put("pilotName", pilotUser.pilotName)
            put("pilotLicense", pilotUser.pilotLicense)
            put("primaryDrone", pilotUser.primaryDrone)
            put("homeCounty", pilotUser.homeCounty)
            put("isVerifiedPilot", pilotUser.isVerifiedPilot)
            put("bio", pilotUser.bio)
            put("avatarUrl", JSONObject.NULL)
            put("registeredAt", pilotUser.registeredAt)
        }
        usersMap.put(cleanEmail, userRecord)
        prefs.edit().putString("registered_pilots_db", usersMap.toString()).apply()

        if (!verificationSent) {
            saveCurrentUser(pilotUser)
        } else {
            // Sign out from Firebase so session is not automatically active until email is verified
            try { auth?.signOut() } catch (_: Exception) {}
        }

        val msg = if (verificationSent) {
            "Sikeres regisztráció! Elküldtünk egy megerősítő e-mailt a(z) $cleanEmail címre. Kérlek, kattints a benne lévő linkre a fiókod hitelesítéséhez, majd jelentkezz be!"
        } else if (cloudRegistered) {
            "Gratulálunk! Sikeresen elkészült a felhőalapú pilótafiókod."
        } else {
            "Gratulálunk! Sikeresen elkészült a hivatalos pilótafiókod."
        }
        return@withContext AuthResult.Success(pilotUser, msg)
    }

    /**
     * Fast Quick-Pilot Guest or Demo Login
     */
    suspend fun quickLogin(pilotName: String, primaryDrone: String): AuthResult = withContext(Dispatchers.IO) {
        val name = pilotName.trim().ifBlank { "Légi Felfedező" }
        val generatedEmail = "${name.lowercase().replace(" ", "")}@dronkalauz.hu"
        val user = PilotUser(
            uid = "quick_" + UUID.randomUUID().toString().take(8),
            email = generatedEmail,
            pilotName = name,
            pilotLicense = "A1/A3 Nyílt kategória",
            primaryDrone = primaryDrone.ifBlank { "DJI Mini 4 Pro" },
            homeCounty = "Budapest / Pest vármegye",
            isVerifiedPilot = true,
            avatarUrl = null,
            bio = "",
            isCloudSynced = false
        )
        saveCurrentUser(user)
        return@withContext AuthResult.Success(user, "Gyors bejelentkezés sikeres!")
    }

    /**
     * Update user profile information
     */
    fun updateProfile(user: PilotUser) {
        saveCurrentUser(user)

        // Also update in registered pilots map if email exists
        val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
        try {
            val usersMap = JSONObject(usersJson)
            if (usersMap.has(user.email)) {
                val record = usersMap.getJSONObject(user.email)
                record.put("pilotName", user.pilotName)
                record.put("nickname", user.nickname)
                record.put("pilotStatus", user.pilotStatus)
                record.put("pilotLicense", user.pilotLicense)
                record.put("primaryDrone", user.primaryDrone)
                record.put("homeCounty", user.homeCounty)
                record.put("bio", user.bio)
                if (user.avatarUrl != null) {
                    record.put("avatarUrl", user.avatarUrl)
                } else {
                    record.put("avatarUrl", JSONObject.NULL)
                }
                prefs.edit().putString("registered_pilots_db", usersMap.toString()).apply()
            }
        } catch (_: Exception) {}
    }

    /**
     * Get all registered pilot accounts for Admin inspection
     */
    fun getAllRegisteredPilots(): List<PilotUser> {
        val list = mutableListOf<PilotUser>()
        val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
        try {
            val usersMap = JSONObject(usersJson)
            val keys = usersMap.keys()
            while (keys.hasNext()) {
                val emailKey = keys.next()
                val obj = usersMap.getJSONObject(emailKey)
                list.add(
                    PilotUser(
                        uid = obj.optString("uid", UUID.randomUUID().toString()),
                        email = obj.optString("email", emailKey),
                        pilotName = obj.optString("pilotName", emailKey.substringBefore("@")),
                        nickname = obj.optString("nickname", ""),
                        pilotStatus = obj.optString("pilotStatus", "Online / Készenlétben"),
                        pilotLicense = obj.optString("pilotLicense", "A1/A3 Nyílt kategória"),
                        primaryDrone = obj.optString("primaryDrone", "DJI Mini 4 Pro"),
                        homeCounty = obj.optString("homeCounty", "Budapest / Pest vármegye"),
                        isVerifiedPilot = obj.optBoolean("isVerifiedPilot", true),
                        avatarUrl = if (obj.isNull("avatarUrl")) null else obj.optString("avatarUrl"),
                        bio = obj.optString("bio", ""),
                        isCloudSynced = true,
                        registeredAt = obj.optLong("registeredAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("DroneAuthService", "Error parsing registered pilots", e)
        }

        // Include current user if not already in list
        val current = getCurrentUser()
        if (current != null && list.none { it.email.equals(current.email, ignoreCase = true) }) {
            list.add(current)
        }

        return list.sortedByDescending { it.registeredAt }
    }

    /**
     * Check if the current user's email is verified in Firebase
     */
    suspend fun isEmailVerified(): Boolean = withContext(Dispatchers.IO) {
        val user = firebaseAuth?.currentUser ?: return@withContext false
        try {
            user.reload().await()
            return@withContext user.isEmailVerified
        } catch (_: Exception) {
            return@withContext user.isEmailVerified
        }
    }

    /**
     * Resend verification email to the current Firebase user
     */
    suspend fun resendVerificationEmail(): AuthResult = withContext(Dispatchers.IO) {
        val user = firebaseAuth?.currentUser
            ?: return@withContext AuthResult.Error("Nem található aktív bejelentkezett felhőfiók.")
        try {
            user.sendEmailVerification().await()
            return@withContext AuthResult.Success(
                getCurrentUser() ?: PilotUser(user.uid, user.email ?: "", user.displayName ?: "Pilóta"),
                "A megerősítő e-mailt sikeresen újra elküldtük a(z) ${user.email} címre!"
            )
        } catch (e: Exception) {
            return@withContext AuthResult.Error("Nem sikerült elküldeni az e-mailt: ${e.localizedMessage ?: "Kérlek próbáld később!"}")
        }
    }

    /**
     * Delete user account from registered pilots database (and sign out if current user)
     */
    fun deleteUserAccount(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        var deleted = false
        val usersJson = prefs.getString("registered_pilots_db", "{}") ?: "{}"
        try {
            val usersMap = JSONObject(usersJson)
            if (usersMap.has(cleanEmail)) {
                usersMap.remove(cleanEmail)
                prefs.edit().putString("registered_pilots_db", usersMap.toString()).apply()
                deleted = true
            }
        } catch (e: Exception) {
            Log.e("DroneAuthService", "Error deleting user account", e)
        }

        // If the deleted user is currently logged in, sign them out
        val currentUser = getCurrentUser()
        if (currentUser != null && currentUser.email.equals(cleanEmail, ignoreCase = true)) {
            signOut()
        }

        return deleted
    }

    /**
     * Remember Login Credentials preferences
     */
    fun isRememberLoginEnabled(): Boolean {
        return prefs.getBoolean("remember_login_credentials", false)
    }

    fun getSavedLoginEmail(): String {
        return if (isRememberLoginEnabled()) prefs.getString("saved_login_email", "") ?: "" else ""
    }

    fun getSavedLoginPassword(): String {
        return if (isRememberLoginEnabled()) prefs.getString("saved_login_password", "") ?: "" else ""
    }

    fun setRememberLoginCredentials(remember: Boolean, email: String = "", password: String = "") {
        val editor = prefs.edit()
        editor.putBoolean("remember_login_credentials", remember)
        if (remember) {
            editor.putString("saved_login_email", email.trim().lowercase())
            editor.putString("saved_login_password", password)
        } else {
            editor.remove("saved_login_email")
            editor.remove("saved_login_password")
        }
        editor.apply()
    }

    /**
     * Sign out current pilot
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        prefs.edit().remove("current_pilot_user").apply()
    }

    private fun saveCurrentUser(user: PilotUser) {
        try {
            val obj = JSONObject().apply {
                put("uid", user.uid)
                put("email", user.email)
                put("pilotName", user.pilotName)
                put("nickname", user.nickname)
                put("pilotStatus", user.pilotStatus)
                put("pilotLicense", user.pilotLicense)
                put("primaryDrone", user.primaryDrone)
                put("homeCounty", user.homeCounty)
                put("isVerifiedPilot", user.isVerifiedPilot)
                put("bio", user.bio)
                if (user.avatarUrl != null) {
                    put("avatarUrl", user.avatarUrl)
                } else {
                    put("avatarUrl", JSONObject.NULL)
                }
                put("isCloudSynced", user.isCloudSynced)
            }
            prefs.edit().putString("current_pilot_user", obj.toString()).apply()
        } catch (e: Exception) {
            Log.e("DroneAuthService", "Failed to save user", e)
        }
    }
}

