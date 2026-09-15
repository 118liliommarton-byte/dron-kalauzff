package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.data.TelemetryData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.random.Random

data class ResolvedLocationResult(
    val title: String,
    val locality: String,
    val county: String = ""
)

class LocationTelemetryManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val prefs = context.getSharedPreferences("telemetry_prefs", Context.MODE_PRIVATE)

    fun getOpenWeatherApiKey(): String? {
        return prefs.getString("open_weather_api_key", "677d4d59365779f0453246cf583314e5")?.ifBlank { "677d4d59365779f0453246cf583314e5" }
    }

    fun saveOpenWeatherApiKey(apiKey: String?) {
        prefs.edit().putString("open_weather_api_key", apiKey?.trim()).apply()
    }

    fun getAccuWeatherApiKey(): String? {
        return prefs.getString("accuweather_api_key", null)?.ifBlank { null }
    }

    fun saveAccuWeatherApiKey(apiKey: String?) {
        prefs.edit().putString("accuweather_api_key", apiKey?.trim()).apply()
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            continuation.resume(lastLoc)
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        }
    }

    suspend fun resolveLocationDetails(
        lat: Double,
        lon: Double,
        knownSpots: List<Pair<String, Pair<Double, Double>>> = emptyList()
    ): ResolvedLocationResult = withContext(Dispatchers.IO) {
        // 1. Check if user is near a known spot in the database (< 1.5 km)
        for ((spotName, spotCoords) in knownSpots) {
            val dist = calculateDistanceMeters(lat, lon, spotCoords.first, spotCoords.second)
            if (dist <= 1500.0) {
                val spotLocality = if (spotName.contains("Érd", ignoreCase = true) || spotName.contains("Papi", ignoreCase = true)) "Érd, Pest megye" else "Magyarország"
                if (dist <= 500.0) {
                    return@withContext ResolvedLocationResult(
                        title = spotName,
                        locality = spotLocality,
                        county = spotLocality
                    )
                } else {
                    return@withContext ResolvedLocationResult(
                        title = "$spotName környéke",
                        locality = spotLocality,
                        county = spotLocality
                    )
                }
            }
        }

        // 2. Try Nominatim (OpenStreetMap reverse geocoding API for Hungarian landmarks, parks, towns)
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=18&addressdetails=1&accept-language=hu")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3500
                readTimeout = 3500
                setRequestProperty("User-Agent", "DroneSpotterApp/1.0 (Android; Hungarian Drone Community)")
            }
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val address = json.optJSONObject("address")
                if (address != null) {
                    val landmark = address.optString("tourism").ifBlank {
                        address.optString("historic").ifBlank {
                            address.optString("leisure").ifBlank {
                                address.optString("natural").ifBlank {
                                    address.optString("amenity").ifBlank {
                                        address.optString("building")
                                    }
                                }
                            }
                        }
                    }
                    val road = address.optString("road")
                    val suburb = address.optString("suburb").ifBlank { address.optString("neighbourhood") }
                    val town = address.optString("town").ifBlank {
                        address.optString("city").ifBlank {
                            address.optString("village").ifBlank {
                                address.optString("municipality")
                            }
                        }
                    }
                    val state = address.optString("state")

                    var generatedTitle = ""
                    if (landmark.isNotBlank()) {
                        generatedTitle = if (town.isNotBlank()) "$landmark ($town)" else landmark
                    } else if (suburb.isNotBlank() && town.isNotBlank()) {
                        generatedTitle = "$town, $suburb"
                    } else if (road.isNotBlank() && town.isNotBlank()) {
                        generatedTitle = "$town - $road"
                    } else if (town.isNotBlank()) {
                        generatedTitle = "$town drónos helyszín"
                    }

                    val generatedLocality = if (town.isNotBlank() && state.isNotBlank()) "$town, $state" else town.ifBlank { state }

                    if (generatedTitle.isNotBlank()) {
                        return@withContext ResolvedLocationResult(
                            title = generatedTitle,
                            locality = generatedLocality,
                            county = state.ifBlank { town }
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Fallback to Android Geocoder
        try {
            val geocoder = Geocoder(context, Locale("hu", "HU"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull()
            if (addr != null) {
                val feature = addr.featureName?.takeIf { !it.matches(Regex("^[0-9/\\-\\s]+$")) }
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Magyarország"
                val subLocality = addr.subLocality ?: addr.thoroughfare
                val adminArea = addr.adminArea ?: ""

                val title = when {
                    feature != null && feature != locality -> "$feature ($locality)"
                    subLocality != null -> "$locality, $subLocality"
                    else -> "$locality fotós pont"
                }

                val county = if (adminArea.isNotBlank() && adminArea != locality) "$locality, $adminArea" else locality

                return@withContext ResolvedLocationResult(
                    title = title,
                    locality = locality,
                    county = county
                )
            }
        } catch (_: Exception) {}

        // 4. Default formatted coordinates
        val formattedCoords = "${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lon)}"
        return@withContext ResolvedLocationResult(
            title = "GPS: $formattedCoords",
            locality = "Magyarország",
            county = "Magyarország"
        )
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    suspend fun resolveLocationName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        return@withContext resolveLocationDetails(lat, lon).title
    }

    private data class CachedWeather(
        val timestamp: Long,
        val lat: Double,
        val lon: Double,
        val windSpeed: Int,
        val windGust: Int,
        val visibility: Int,
        val source: String
    )

    private var lastCachedWeather: CachedWeather? = null
    private var lastCachedKpIndex: Int = 2
    private var lastKpFetchTime: Long = 0L

    suspend fun fetchRealTelemetry(location: Location): TelemetryData = withContext(Dispatchers.IO) {
        val lat = location.latitude
        val lon = location.longitude
        val alt = if (location.hasAltitude()) location.altitude else 125.0
        val locationName = resolveLocationName(lat, lon)

        // 1. Weather Data with 5-minute intelligent caching to prevent jitter
        val now = System.currentTimeMillis()
        var windSpeed = 12
        var windGust = 18
        var visibility = 10
        var sourceName = "Open-Meteo Live API"

        val cached = lastCachedWeather
        val isCacheValid = cached != null &&
                (now - cached.timestamp < 5 * 60 * 1000L) &&
                kotlin.math.abs(cached.lat - lat) < 0.02 &&
                kotlin.math.abs(cached.lon - lon) < 0.02

        if (isCacheValid && cached != null) {
            windSpeed = cached.windSpeed
            windGust = cached.windGust
            visibility = cached.visibility
            sourceName = cached.source
        } else {
            val openWeatherKey = getOpenWeatherApiKey()
            val accuKey = getAccuWeatherApiKey()
            var apiSuccess = false

            // Try OpenWeatherMap API if configured
            if (!openWeatherKey.isNullOrBlank()) {
                val owmData = fetchOpenWeatherMapTelemetry(lat, lon, openWeatherKey)
                if (owmData != null) {
                    windSpeed = owmData.windSpeed
                    windGust = owmData.windGust
                    visibility = owmData.visibility
                    sourceName = "OpenWeatherMap API"
                    apiSuccess = true
                }
            }

            // Try AccuWeather API if needed
            if (!apiSuccess && !accuKey.isNullOrBlank()) {
                val accuData = fetchAccuWeatherTelemetry(lat, lon, accuKey)
                if (accuData != null) {
                    windSpeed = accuData.windSpeed
                    windGust = accuData.windGust
                    visibility = accuData.visibility
                    sourceName = "AccuWeather API"
                    apiSuccess = true
                }
            }

            // Primary reliable Open-Meteo free API
            if (!apiSuccess) {
                try {
                    val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=wind_speed_10m,wind_gusts_10m,visibility"
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 4000
                    connection.readTimeout = 4000

                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)
                        if (json.has("current")) {
                            val current = json.getJSONObject("current")
                            if (current.has("wind_speed_10m")) {
                                windSpeed = current.getDouble("wind_speed_10m").toInt()
                            }
                            if (current.has("wind_gusts_10m")) {
                                windGust = current.getDouble("wind_gusts_10m").toInt()
                            }
                            if (current.has("visibility")) {
                                val visMeters = current.getDouble("visibility")
                                visibility = (visMeters / 1000.0).toInt().coerceAtLeast(1)
                            }
                        }
                        sourceName = "Open-Meteo Live API"
                        apiSuccess = true
                    }
                    connection.disconnect()
                } catch (_: Exception) {
                    // Fallback to previous cache or stable deterministic value
                }
            }

            if (apiSuccess) {
                lastCachedWeather = CachedWeather(
                    timestamp = now,
                    lat = lat,
                    lon = lon,
                    windSpeed = windSpeed,
                    windGust = windGust,
                    visibility = visibility,
                    source = sourceName
                )
            } else if (cached != null) {
                windSpeed = cached.windSpeed
                windGust = cached.windGust
                visibility = cached.visibility
                sourceName = "${cached.source} (Mentett)"
            } else {
                // Deterministic offline estimate based on location coordinates (never random jitter)
                val coordHash = (kotlin.math.abs(lat * 100).toInt() + kotlin.math.abs(lon * 100).toInt())
                windSpeed = 10 + (coordHash % 6)
                windGust = windSpeed + 5 + (coordHash % 4)
                visibility = 10
                sourceName = "Helyi Becslés (Offline)"
            }
        }

        // 2. Real Planetary Kp Index from NOAA Space Weather Prediction Center (or cached 30 mins)
        val kp = if (now - lastKpFetchTime > 30 * 60 * 1000L) {
            val fetchedKp = fetchNoaaKpIndex()
            if (fetchedKp != null) {
                lastCachedKpIndex = fetchedKp
                lastKpFetchTime = now
                fetchedKp
            } else {
                lastCachedKpIndex
            }
        } else {
            lastCachedKpIndex
        }

        // 3. Stable Satellite calculation based on real GPS accuracy (Deterministic, no random jumping)
        val satellites = when {
            location.extras?.containsKey("satellites") == true && (location.extras?.getInt("satellites") ?: 0) > 0 -> {
                location.extras?.getInt("satellites") ?: 18
            }
            location.hasAccuracy() -> {
                val acc = location.accuracy
                when {
                    acc <= 3.5f -> 22
                    acc <= 6.0f -> 20
                    acc <= 10.0f -> 18
                    acc <= 15.0f -> 15
                    acc <= 25.0f -> 12
                    else -> 9
                }
            }
            else -> 18
        }

        val statusText = when {
            windSpeed > 35 || windGust > 45 -> "Erős szél, nem ajánlott!"
            windSpeed > 24 -> "Mérsékelt szél, óvatosság szükséges"
            satellites < 10 -> "Gyenge GPS műhold lefedettség"
            kp >= 5 -> "Geomágneses vihar (KP >= 5), zavarhatja az iránytűt"
            else -> "Biztonságos felszállás"
        }

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val updatedStr = timeFormat.format(Date())

        return@withContext TelemetryData(
            locationName = locationName,
            latitude = lat,
            longitude = lon,
            altitudeMeters = alt,
            windSpeedKmh = windSpeed,
            windGustKmh = windGust,
            satellitesCount = satellites,
            kpIndex = kp,
            visibilityKm = visibility,
            statusText = statusText,
            isGpsActive = true,
            lastUpdated = updatedStr,
            isLoading = false,
            dataSource = sourceName
        )
    }

    private suspend fun fetchNoaaKpIndex(): Int? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val jsonArray = JSONArray(response)
                if (jsonArray.length() > 1) {
                    val lastRow = jsonArray.getJSONArray(jsonArray.length() - 1)
                    val kpVal = lastRow.optDouble(1, 2.0)
                    return@withContext kpVal.toInt().coerceIn(0, 9)
                }
            }
            connection.disconnect()
            return@withContext null
        } catch (_: Exception) {
            return@withContext null
        }
    }

    private suspend fun fetchOpenWeatherMapTelemetry(lat: Double, lon: Double, apiKey: String): OpenWeatherData? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=${apiKey.trim()}&units=metric"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val json = JSONObject(response)
            var speed = 12
            var gust = 18
            var vis = 10

            if (json.has("wind")) {
                val windObj = json.getJSONObject("wind")
                if (windObj.has("speed")) {
                    val speedMs = windObj.getDouble("speed")
                    speed = (speedMs * 3.6).toInt()
                }
                if (windObj.has("gust")) {
                    val gustMs = windObj.getDouble("gust")
                    gust = (gustMs * 3.6).toInt()
                } else {
                    gust = speed + 6
                }
            }

            if (json.has("visibility")) {
                val visMeters = json.getDouble("visibility")
                vis = (visMeters / 1000.0).toInt().coerceAtLeast(1)
            }

            return@withContext OpenWeatherData(speed, gust, vis)
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private data class OpenWeatherData(val windSpeed: Int, val windGust: Int, val visibility: Int)

    private suspend fun fetchAccuWeatherTelemetry(lat: Double, lon: Double, apiKey: String): AccuWeatherData? = withContext(Dispatchers.IO) {
        try {
            val geoUrl = "https://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey=${apiKey.trim()}&q=$lat,$lon"
            val conn1 = URL(geoUrl).openConnection() as HttpURLConnection
            conn1.requestMethod = "GET"
            conn1.connectTimeout = 4000
            conn1.readTimeout = 4000
            if (conn1.responseCode != 200) {
                conn1.disconnect()
                return@withContext null
            }
            val res1 = conn1.inputStream.bufferedReader().use { it.readText() }
            conn1.disconnect()

            val json1 = JSONObject(res1)
            val locationKey = json1.optString("Key")
            if (locationKey.isNullOrBlank()) return@withContext null

            val condUrl = "https://dataservice.accuweather.com/currentconditions/v1/$locationKey?apikey=${apiKey.trim()}&details=true"
            val conn2 = URL(condUrl).openConnection() as HttpURLConnection
            conn2.requestMethod = "GET"
            conn2.connectTimeout = 4000
            conn2.readTimeout = 4000
            if (conn2.responseCode != 200) {
                conn2.disconnect()
                return@withContext null
            }
            val res2 = conn2.inputStream.bufferedReader().use { it.readText() }
            conn2.disconnect()

            val array2 = JSONArray(res2)
            if (array2.length() == 0) return@withContext null
            val currentObj = array2.getJSONObject(0)

            var speed = 12
            var gust = 18
            var vis = 10

            if (currentObj.has("Wind")) {
                val windObj = currentObj.optJSONObject("Wind")
                val speedObj = windObj?.optJSONObject("Speed")
                val metricObj = speedObj?.optJSONObject("Metric")
                if (metricObj != null && metricObj.has("Value")) {
                    speed = metricObj.getDouble("Value").toInt()
                }
            }

            if (currentObj.has("WindGust")) {
                val gustObj = currentObj.optJSONObject("WindGust")
                val speedObj = gustObj?.optJSONObject("Speed")
                val metricObj = speedObj?.optJSONObject("Metric")
                if (metricObj != null && metricObj.has("Value")) {
                    gust = metricObj.getDouble("Value").toInt()
                }
            } else {
                gust = speed + 6
            }

            if (currentObj.has("Visibility")) {
                val visObj = currentObj.optJSONObject("Visibility")
                val metricObj = visObj?.optJSONObject("Metric")
                if (metricObj != null && metricObj.has("Value")) {
                    vis = metricObj.getDouble("Value").toInt().coerceAtLeast(1)
                }
            }

            return@withContext AccuWeatherData(speed, gust, vis)
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private data class AccuWeatherData(val windSpeed: Int, val windGust: Int, val visibility: Int)

}
