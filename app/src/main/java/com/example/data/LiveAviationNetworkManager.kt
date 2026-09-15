package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class LiveSyncStatus(
    val isSyncing: Boolean = false,
    val isConnected: Boolean = false,
    val lastUpdatedTime: String? = null,
    val fetchedNotamCount: Int = 0,
    val statusMessage: String = "Szerver kapcsolat inicializálása..."
)

object LiveAviationNetworkManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    var syncStatus by mutableStateOf(LiveSyncStatus())
        private set

    val liveNotams = mutableStateListOf<AirspaceZone>()

    suspend fun syncOnAppStartup() {
        withContext(Dispatchers.Main) {
            syncStatus = syncStatus.copy(
                isSyncing = true,
                statusMessage = "Élő NOTAM & SIGMET adatok lekérése a szerverről..."
            )
        }

        withContext(Dispatchers.IO) {
            val newlyFetched = mutableListOf<AirspaceZone>()
            var success = false
            val currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            try {
                // 1. Fetch Live International SIGMETs (Severe Convective Weather / Flight Warnings) from NOAA Aviation Weather
                val sigmetRequest = Request.Builder()
                    .url("https://aviationweather.gov/api/data/isigmet?format=json")
                    .header("User-Agent", "Mozilla/5.0 (DroneKalauz-Android-App)")
                    .build()

                val sigmetResponse = client.newCall(sigmetRequest).execute()
                if (sigmetResponse.isSuccessful) {
                    val sigmetBody = sigmetResponse.body?.string()
                    if (!sigmetBody.isNullOrBlank() && sigmetBody.trim().startsWith("[")) {
                        val jsonArray = JSONArray(sigmetBody)
                        for (i in 0 until minOf(jsonArray.length(), 15)) {
                            val item = jsonArray.optJSONObject(i) ?: continue
                            val firName = item.optString("firName", "Európai Légtér (FIR)")
                            val hazard = item.optString("hazard", "Légi Veszélyesség")
                            val rawText = item.optString("rawSigmet", "")
                            val validFrom = item.optString("validTimeFrom", "Azonnal")
                            val validTo = item.optString("validTimeTo", "Következő értesítésig")

                            // Map FIR to Country name
                            val countryName = when {
                                firName.contains("BUDAPEST", ignoreCase = true) || firName.contains("LHCC", ignoreCase = true) -> "Magyarország"
                                firName.contains("RHEIN", ignoreCase = true) || firName.contains("MUNCHEN", ignoreCase = true) || firName.contains("LANGEN", ignoreCase = true) -> "Németország"
                                firName.contains("WIEN", ignoreCase = true) || firName.contains("LOVV", ignoreCase = true) -> "Ausztria"
                                else -> "Európai Unió"
                            }

                            val isHu = countryName == "Magyarország"
                            val isDe = countryName == "Németország"
                            val lat = if (isHu) 47.4979 else if (isDe) 51.1657 else 48.2082
                            val lng = if (isHu) 19.0402 else if (isDe) 10.4515 else 16.3738

                            newlyFetched.add(
                                AirspaceZone(
                                    id = "LIVE_SIGMET_$i",
                                    countryName = countryName,
                                    name = "ÉLŐ SIGMET NOTAM: $hazard ($firName)",
                                    type = ZoneType.DYNAMIC_NOTAM,
                                    lat = lat + (i * 0.05),
                                    lng = lng + (i * 0.04),
                                    radiusMeters = 25000,
                                    lowerLimit = "GND",
                                    upperLimit = "FL 100",
                                    maxDroneAltitude = "0 m (Élő légi veszélyességi távirat)",
                                    activityHours = "Érvényes: $validFrom - $validTo UTC",
                                    description = "Valós időben lekért hivatalos SIGMET/NOTAM riasztás: ${if (rawText.isNotEmpty()) rawText else hazard}",
                                    permitRequired = "Közvetlen légi irányítási (ATC/FIS) koordináció kötelező.",
                                    contactAuthority = "Aviation Weather Center / NOAA Live Feed",
                                    penalties = "Azonnali repülési tilalom a veszélyzónában.",
                                    nearbyCities = "$firName Ellenőrzött Körzet"
                                )
                            )
                        }
                        success = true
                    }
                }

                // 2. Fetch Live Airport METAR Weather & Flight Category Advisories
                val metarRequest = Request.Builder()
                    .url("https://aviationweather.gov/api/data/metar?ids=LHBP,EDDF,EDDM,EDDH,EDDK,EDDL,EDDS,LOWW,EPWA,LKPR,LJLJ,LDZA,EGLL,LFPG,EHAM&format=json")
                    .header("User-Agent", "Mozilla/5.0 (DroneKalauz-Android-App)")
                    .build()

                val metarResponse = client.newCall(metarRequest).execute()
                if (metarResponse.isSuccessful) {
                    val metarBody = metarResponse.body?.string()
                    if (!metarBody.isNullOrBlank() && metarBody.trim().startsWith("[")) {
                        val jsonArray = JSONArray(metarBody)
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.optJSONObject(i) ?: continue
                            val icao = item.optString("icaoId", "")
                            val name = item.optString("name", icao)
                            val rawOb = item.optString("rawOb", "")
                            val fltCat = item.optString("flightCategory", "VFR")
                            val temp = item.optInt("temp", 20)
                            val wspd = item.optInt("wspd", 5)

                            if (fltCat == "IFR" || fltCat == "LIFR" || fltCat == "MVFR" || wspd >= 12) {
                                val countryName = when {
                                    icao.startsWith("LH") -> "Magyarország"
                                    icao.startsWith("ED") || icao.startsWith("ET") -> "Németország"
                                    icao.startsWith("LO") -> "Ausztria"
                                    icao.startsWith("LK") -> "Szlovákia"
                                    else -> "Európai Unió"
                                }

                                val (lat, lng) = when (icao) {
                                    "LHBP" -> 47.4369 to 19.2556
                                    "EDDF" -> 50.0379 to 8.5622
                                    "EDDM" -> 48.3538 to 11.7861
                                    "LOWW" -> 48.1103 to 16.5697
                                    else -> 50.0 to 10.0
                                }

                                newlyFetched.add(
                                    AirspaceZone(
                                        id = "LIVE_METAR_$icao",
                                        countryName = countryName,
                                        name = "ÉLŐ IDŐJÁRÁSI NOTAM: $icao ($name)",
                                        type = ZoneType.DYNAMIC_NOTAM,
                                        lat = lat,
                                        lng = lng,
                                        radiusMeters = 15000,
                                        lowerLimit = "GND",
                                        upperLimit = "2000 ft AMSL",
                                        maxDroneAltitude = if (fltCat == "IFR" || fltCat == "LIFR") "0 m (Látási viszonyok kritikusak)" else "Fokozott óvatossággal (Szél: ${wspd} kts)",
                                        activityHours = "Frissítve: $currentTimeStr (Élő adás)",
                                        description = "Légi időjárási állapot: $fltCat ($rawOb). Hőmérséklet: ${temp}°C, Szélsebesség: $wspd csomó (${(wspd * 1.852).toInt()} km/h).",
                                        permitRequired = "Drónos felszállás előtt szél- és felhőalap mérés kötelező.",
                                        contactAuthority = "AWC Weather Radar / ICAO Service",
                                        penalties = "Rossz látási viszonyok alatti drónvesztés és baleset felelősség.",
                                        nearbyCities = "$name és repülőtéri körzet"
                                    )
                                )
                            }
                        }
                        success = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            }

            withContext(Dispatchers.Main) {
                if (success || newlyFetched.isNotEmpty()) {
                    liveNotams.clear()
                    liveNotams.addAll(newlyFetched)
                    syncStatus = LiveSyncStatus(
                        isSyncing = false,
                        isConnected = true,
                        lastUpdatedTime = currentTimeStr,
                        fetchedNotamCount = newlyFetched.size,
                        statusMessage = "NOAA & AWC Élő Szerver Csatlakozva • $currentTimeStr"
                    )
                } else {
                    syncStatus = LiveSyncStatus(
                        isSyncing = false,
                        isConnected = false,
                        lastUpdatedTime = currentTimeStr,
                        fetchedNotamCount = liveNotams.size,
                        statusMessage = "Munkamenet offline • Helyi mentett hatósági NOTAM-ok aktívak"
                    )
                }
            }
        }
    }
}
