package com.example.data

data class TelemetryData(
    val locationName: String = "Budapest",
    val latitude: Double = 47.4979,
    val longitude: Double = 19.0402,
    val altitudeMeters: Double = 112.0,
    val windSpeedKmh: Int = 11,
    val windGustKmh: Int = 18,
    val satellitesCount: Int = 18,
    val kpIndex: Int = 2,
    val visibilityKm: Int = 10,
    val statusText: String = "Biztonságos felszállás",
    val isGpsActive: Boolean = false,
    val lastUpdated: String = "Éppen most",
    val isLoading: Boolean = false,
    val dataSource: String = "Open-Meteo"
)

