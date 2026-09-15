package com.example.data

data class CountryRule(
    val name: String,
    val flag: String,
    val maxAltitude: String,
    val registrationRequired: Boolean,
    val registrationDetail: String,
    val summary: String,
    val detailedRules: List<String>,
    val lat: Double = 47.1625,
    val lng: Double = 19.5033,
    val mapZoom: Int = 7,
    val officialMapName: String = "Nemzeti Légtér Térkép",
    val officialMapUrl: String = "https://mydronespace.hu",
    val restrictedZonesSummary: String = "Repülőterek 8 km-es körzete, katonai bázisok, védett természeti területek."
)

object DroneRules {
    val countries: List<CountryRule> = (
        DroneRulesEurope.countries +
        DroneRulesAmericas.countries +
        DroneRulesAsia.countries +
        DroneRulesAfrica.countries +
        DroneRulesOceania.countries
    ).distinctBy { it.name }
}
