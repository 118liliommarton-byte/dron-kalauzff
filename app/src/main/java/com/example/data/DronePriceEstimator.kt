package com.example.data

data class DroneModelSpec(
    val id: String,
    val modelName: String,
    val brand: String, // "DJI", "Autel", "FPV", "Egyéb"
    val category: String,
    val newRetailPriceHuf: Int,
    val baseUsedPriceHuf: Int,
    val flyMoreComboBonusHuf: Int,
    val smartControllerBonusHuf: Int,
    val popularity: String, // "🔥 Kiemelten keresett", "🟢 Gyorsan forog", "⚖️ Stabil piac"
    val averageTurnoverDays: String, // "2-4 nap", "4-7 nap", "1-2 hét"
    val description: String,
    val releaseYear: Int = 2023
)

data class PriceEstimationResult(
    val modelName: String,
    val brand: String,
    val condition: String,
    val bundleType: String,
    val batteryCycleRange: String,
    val hasWarranty: Boolean,
    val extraAccessories: List<String>,
    val estimatedAveragePriceHuf: Int,
    val recommendedMinPriceHuf: Int,
    val recommendedMaxPriceHuf: Int,
    val quickSalePriceHuf: Int,
    val patientSalePriceHuf: Int,
    val marketPopularity: String,
    val turnoverSpeed: String,
    val marketNotes: String,
    val breakdown: List<PriceFactor>,
    val tipsForSeller: List<String>
)

data class PriceFactor(
    val label: String,
    val amountHuf: Int,
    val isPositive: Boolean = true
)

object DronePriceDatabase {

    val droneModels: List<DroneModelSpec> = listOf(
        // DJI Mini Series
        DroneModelSpec(
            id = "dji_mini_4_pro",
            modelName = "DJI Mini 4 Pro",
            brand = "DJI",
            category = "Sub-249g Profi C0",
            newRetailPriceHuf = 330000,
            baseUsedPriceHuf = 195000,
            flyMoreComboBonusHuf = 55000,
            smartControllerBonusHuf = 35000,
            popularity = "🔥 Kiemelten keresett (Top 1)",
            averageTurnoverDays = "1-3 nap",
            description = "A legmodernebb C0-s alatti drón mindenirányú akadályérzékeléssel, 4K/60fps HDR-rel és ActiveTrack 360-nal.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "dji_mini_4k",
            modelName = "DJI Mini 4K",
            brand = "DJI",
            category = "Sub-249g Belépő 4K",
            newRetailPriceHuf = 135000,
            baseUsedPriceHuf = 80000,
            flyMoreComboBonusHuf = 25000,
            smartControllerBonusHuf = 0,
            popularity = "🔥 Nagyon népszerű belépő",
            averageTurnoverDays = "1-3 nap",
            description = "4K 30fps videó, 3 tengelyes gimbal, megbízható OcuSync jeltovábbítás és 31 perc repülési idő.",
            releaseYear = 2024
        ),
        DroneModelSpec(
            id = "dji_mini_3_pro",
            modelName = "DJI Mini 3 Pro",
            brand = "DJI",
            category = "Sub-249g Haladó",
            newRetailPriceHuf = 250000,
            baseUsedPriceHuf = 145000,
            flyMoreComboBonusHuf = 45000,
            smartControllerBonusHuf = 30000,
            popularity = "🔥 Nagyon népszerű",
            averageTurnoverDays = "2-5 nap",
            description = "Kiváló képminőség, vertikális kameramód TikTok/Instagram felvételekhez és 3 irányú érzékelés.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "dji_mini_3",
            modelName = "DJI Mini 3",
            brand = "DJI",
            category = "Sub-249g Kezdő/Haladó",
            newRetailPriceHuf = 175000,
            baseUsedPriceHuf = 105000,
            flyMoreComboBonusHuf = 35000,
            smartControllerBonusHuf = 25000,
            popularity = "🟢 Gyorsan forog",
            averageTurnoverDays = "2-5 nap",
            description = "Megbízható 4K kamera vertikális móddal, akadályérzékelő szenzorok nélkül, hosszú repülési idővel.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "dji_mini_2_se",
            modelName = "DJI Mini 2 SE",
            brand = "DJI",
            category = "Sub-249g Belépő szint",
            newRetailPriceHuf = 125000,
            baseUsedPriceHuf = 75000,
            flyMoreComboBonusHuf = 25000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Kiváló ár-érték arány",
            averageTurnoverDays = "2-4 nap",
            description = "A legkeresettebb kezdő drón Magyarországon. Stabil OcuSync 2.0 jeltovábbítás és 2.7K videó.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "dji_mini_2",
            modelName = "DJI Mini 2",
            brand = "DJI",
            category = "Sub-249g Kezdő",
            newRetailPriceHuf = 140000,
            baseUsedPriceHuf = 70000,
            flyMoreComboBonusHuf = 25000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Stabil kereslet",
            averageTurnoverDays = "3-5 nap",
            description = "4K 30fps videó, OcuSync 2.0, szélállóság 5-ös szintig. Időtálló klasszikus.",
            releaseYear = 2020
        ),
        DroneModelSpec(
            id = "dji_mini_se",
            modelName = "DJI Mini SE",
            brand = "DJI",
            category = "Sub-249g Belépő szint",
            newRetailPriceHuf = 110000,
            baseUsedPriceHuf = 55000,
            flyMoreComboBonusHuf = 20000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Átlagos kereslet",
            averageTurnoverDays = "5-10 nap",
            description = "Mavic Mini váz Mini 2 aerodinamikával és Wi-Fi átvitellel, 2.7K felbontással.",
            releaseYear = 2021
        ),
        DroneModelSpec(
            id = "dji_mavic_mini",
            modelName = "DJI Mavic Mini (1. generáció)",
            brand = "DJI",
            category = "Sub-249g Régebbi belépő",
            newRetailPriceHuf = 100000,
            baseUsedPriceHuf = 45000,
            flyMoreComboBonusHuf = 18000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Átlagos kereslet",
            averageTurnoverDays = "7-14 nap",
            description = "Wi-Fi jelátvitelű első generációs Mini, 2.7K kamerával.",
            releaseYear = 2019
        ),

        // DJI Neo Series
        DroneModelSpec(
            id = "dji_neo",
            modelName = "DJI Neo",
            brand = "DJI",
            category = "135g Szuperkönnyű Vlog / AI",
            newRetailPriceHuf = 85000,
            baseUsedPriceHuf = 60000,
            flyMoreComboBonusHuf = 35000,
            smartControllerBonusHuf = 0,
            popularity = "🔥 Rendkívül felkapott újdonság",
            averageTurnoverDays = "1-3 nap",
            description = "135 grammos tenyérről indítható AI követő vlog drón, 4K stabilizált videóval, távirányító nélkül vagy telefonnal/Goggles-szel is irányítható.",
            releaseYear = 2024
        ),

        // DJI Air Series
        DroneModelSpec(
            id = "dji_air_3s",
            modelName = "DJI Air 3S",
            brand = "DJI",
            category = "Középkategória 1\" + 70mm Dupla Kamera",
            newRetailPriceHuf = 480000,
            baseUsedPriceHuf = 320000,
            flyMoreComboBonusHuf = 75000,
            smartControllerBonusHuf = 45000,
            popularity = "🔥 Nagyon friss csúcsmodell",
            averageTurnoverDays = "2-4 nap",
            description = "1 hüvelykes 50MP főkamera LiDAR akadályérzékeléssel és 70mm-es teleobjektívvel, 4K/120fps HDR-rel.",
            releaseYear = 2024
        ),
        DroneModelSpec(
            id = "dji_air_3",
            modelName = "DJI Air 3",
            brand = "DJI",
            category = "Középkategória Dupla Kamera",
            newRetailPriceHuf = 420000,
            baseUsedPriceHuf = 265000,
            flyMoreComboBonusHuf = 70000,
            smartControllerBonusHuf = 40000,
            popularity = "🔥 Kiemelten keresett profiknak",
            averageTurnoverDays = "3-6 nap",
            description = "Dupla 48 MP-es kamera (széles látószög + 3x teleobjektív), C1-es tanúsítvány, 46 perc repülési idő.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "dji_air_2s",
            modelName = "DJI Air 2S",
            brand = "DJI",
            category = "1 colos szenzoros fotós drón",
            newRetailPriceHuf = 290000,
            baseUsedPriceHuf = 145000,
            flyMoreComboBonusHuf = 40000,
            smartControllerBonusHuf = 30000,
            popularity = "🟢 Nagyon kedvelt fotósok körében",
            averageTurnoverDays = "3-7 nap",
            description = "1 hüvelykes 20 MP CMOS szenzor, 5.4K videó, MasterShots és 4 irányú környezetérzékelés.",
            releaseYear = 2021
        ),
        DroneModelSpec(
            id = "dji_mavic_air_2",
            modelName = "DJI Mavic Air 2",
            brand = "DJI",
            category = "Középkategória 4K/60fps",
            newRetailPriceHuf = 220000,
            baseUsedPriceHuf = 95000,
            flyMoreComboBonusHuf = 30000,
            smartControllerBonusHuf = 20000,
            popularity = "⚖️ Stabil használtpiac",
            averageTurnoverDays = "5-10 nap",
            description = "1/2 hüvelykes 48 MP szenzor, 4K 60fps, megbízható 34 perces akkumulátoridő.",
            releaseYear = 2020
        ),
        DroneModelSpec(
            id = "dji_mavic_air_1",
            modelName = "DJI Mavic Air (1. generáció)",
            brand = "DJI",
            category = "Kompakt összecsukható",
            newRetailPriceHuf = 180000,
            baseUsedPriceHuf = 65000,
            flyMoreComboBonusHuf = 20000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Korosabb modell",
            averageTurnoverDays = "7-15 nap",
            description = "4K 30fps kamera, összecsukható karok és hátrafelé is néző akadályérzékelők.",
            releaseYear = 2018
        ),

        // DJI Mavic Pro & Classic Series
        DroneModelSpec(
            id = "dji_mavic_3_pro",
            modelName = "DJI Mavic 3 Pro",
            brand = "DJI",
            category = "Tripla kamerás Csúcskategória",
            newRetailPriceHuf = 750000,
            baseUsedPriceHuf = 460000,
            flyMoreComboBonusHuf = 95000,
            smartControllerBonusHuf = 60000,
            popularity = "🔥 Professzionális filmesek kedvence",
            averageTurnoverDays = "4-9 nap",
            description = "Hasselblad 4/3 CMOS főkamera + 70mm és 166mm telekamerák. Apple ProRes opció.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "dji_mavic_3_classic",
            modelName = "DJI Mavic 3 Classic",
            brand = "DJI",
            category = "Hasselblad C1 Profi",
            newRetailPriceHuf = 520000,
            baseUsedPriceHuf = 295000,
            flyMoreComboBonusHuf = 75000,
            smartControllerBonusHuf = 45000,
            popularity = "🟢 Keresett professzionális gép",
            averageTurnoverDays = "4-8 nap",
            description = "4/3-os Hasselblad kamera egyetlen objektívvel, kiváló 5.1K színmélységgel és 46 perc repülési idővel.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "dji_mavic_3",
            modelName = "DJI Mavic 3 (Standard / Cine)",
            brand = "DJI",
            category = "Dupla kamerás Hasselblad",
            newRetailPriceHuf = 600000,
            baseUsedPriceHuf = 330000,
            flyMoreComboBonusHuf = 80000,
            smartControllerBonusHuf = 50000,
            popularity = "🟢 Stabil prémium kategória",
            averageTurnoverDays = "5-10 nap",
            description = "Az eredeti Mavic 3 Hasselblad 4/3 kamerával és másodlagos hibrid zoom kamerával.",
            releaseYear = 2021
        ),
        DroneModelSpec(
            id = "dji_mavic_2_pro",
            modelName = "DJI Mavic 2 Pro",
            brand = "DJI",
            category = "1 colos Hasselblad Klasszikus",
            newRetailPriceHuf = 380000,
            baseUsedPriceHuf = 125000,
            flyMoreComboBonusHuf = 35000,
            smartControllerBonusHuf = 30000,
            popularity = "⚖️ Időtálló legenda",
            averageTurnoverDays = "6-12 nap",
            description = "Legendás 1 colos Hasselblad színvilág, masszív felépítés, jó szélállóság.",
            releaseYear = 2018
        ),
        DroneModelSpec(
            id = "dji_mavic_2_zoom",
            modelName = "DJI Mavic 2 Zoom",
            brand = "DJI",
            category = "Optikai Zoom Klasszikus",
            newRetailPriceHuf = 340000,
            baseUsedPriceHuf = 110000,
            flyMoreComboBonusHuf = 30000,
            smartControllerBonusHuf = 25000,
            popularity = "⚖️ Speciális optikai zoom",
            averageTurnoverDays = "7-14 nap",
            description = "2x optikai és 2x digitális zoom (24-48mm), Dolly Zoom effektus.",
            releaseYear = 2018
        ),
        DroneModelSpec(
            id = "dji_mavic_pro",
            modelName = "DJI Mavic Pro / Platinum (1. gen)",
            brand = "DJI",
            category = "Klasszikus első Mavic Pro",
            newRetailPriceHuf = 260000,
            baseUsedPriceHuf = 75000,
            flyMoreComboBonusHuf = 22000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Régebbi profi",
            averageTurnoverDays = "8-18 nap",
            description = "Az első ikonikus összecsukható DJI drón 4K kamerával és OcuSync rendszerrel.",
            releaseYear = 2016
        ),

        // DJI FPV & Avata & Spark
        DroneModelSpec(
            id = "dji_avata_2",
            modelName = "DJI Avata 2",
            brand = "DJI",
            category = "FPV Cinewhoop Új generáció",
            newRetailPriceHuf = 390000,
            baseUsedPriceHuf = 205000,
            flyMoreComboBonusHuf = 65000,
            smartControllerBonusHuf = 0,
            popularity = "🔥 Nagyon forró újdonság",
            averageTurnoverDays = "1-4 nap",
            description = "DJI Goggles 3 és RC Motion 3 támogatás, 1/1.3 colos HDR szenzor, csendesebb propellerek és Easy ACRO.",
            releaseYear = 2024
        ),
        DroneModelSpec(
            id = "dji_avata_1",
            modelName = "DJI Avata (1. generáció)",
            brand = "DJI",
            category = "FPV Cinewhoop",
            newRetailPriceHuf = 270000,
            baseUsedPriceHuf = 135000,
            flyMoreComboBonusHuf = 45000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Jó ár-értékű FPV belépő",
            averageTurnoverDays = "3-7 nap",
            description = "Beépített propeller védőkeret, 4K/60fps stabilizált kamera és Goggles 2 / Integra támogatás.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "dji_fpv_combo",
            modelName = "DJI FPV Combo",
            brand = "DJI",
            category = "Nagysebességű FPV (140 km/h)",
            newRetailPriceHuf = 320000,
            baseUsedPriceHuf = 135000,
            flyMoreComboBonusHuf = 40000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Speciális élménygép",
            averageTurnoverDays = "6-14 nap",
            description = "140 km/h végsebesség, V2 Goggles szemüveg, manuális akrobatikus mód és vészfék funkció.",
            releaseYear = 2021
        ),
        DroneModelSpec(
            id = "dji_spark",
            modelName = "DJI Spark",
            brand = "DJI",
            category = "Ultrakompakt Gesztusvezérelt",
            newRetailPriceHuf = 150000,
            baseUsedPriceHuf = 40000,
            flyMoreComboBonusHuf = 15000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Gyűjtői / Retro mini",
            averageTurnoverDays = "8-20 nap",
            description = "Tenyérből indítható gesztusvezérelt mini drón, 1080p felbontással és 2-tengelyes gimballal.",
            releaseYear = 2017
        ),

        // DJI Phantom & Inspire Series
        DroneModelSpec(
            id = "dji_phantom_4_pro",
            modelName = "DJI Phantom 4 Pro / Pro V2.0",
            brand = "DJI",
            category = "1\" Mechanikus záras Ikon",
            newRetailPriceHuf = 550000,
            baseUsedPriceHuf = 160000,
            flyMoreComboBonusHuf = 40000,
            smartControllerBonusHuf = 35000,
            popularity = "⚖️ Térképészek és fotósok kedvence",
            averageTurnoverDays = "6-14 nap",
            description = "1 hüvelykes mechanikus záras kamera térképészeti és professzionális fotós munkákhoz.",
            releaseYear = 2018
        ),
        DroneModelSpec(
            id = "dji_phantom_3_pro",
            modelName = "DJI Phantom 3 Professional / Adv",
            brand = "DJI",
            category = "Klasszikus Nagytestű Drón",
            newRetailPriceHuf = 280000,
            baseUsedPriceHuf = 55000,
            flyMoreComboBonusHuf = 15000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Belépő retro",
            averageTurnoverDays = "10-25 nap",
            description = "Nagy testű klasszikus drón 4K/2.7K felbontással, Lightbridge jeltovábbítással.",
            releaseYear = 2015
        ),
        DroneModelSpec(
            id = "dji_inspire_2",
            modelName = "DJI Inspire 2 (X5S / X7)",
            brand = "DJI",
            category = "Filmes Moziipari Csúcsmodell",
            newRetailPriceHuf = 1400000,
            baseUsedPriceHuf = 550000,
            flyMoreComboBonusHuf = 120000,
            smartControllerBonusHuf = 60000,
            popularity = "⚖️ Moziipari és forgatási kategória",
            averageTurnoverDays = "10-30 nap",
            description = "Kétszemélyes professzionális filmes drón cserélhető objektíves Zenmuse X5S/X7 kamerával, ProRes/CinemaDNG támogatással.",
            releaseYear = 2017
        ),

        // Autel Robotics Series
        DroneModelSpec(
            id = "autel_evo_lite_plus",
            modelName = "Autel EVO Lite+",
            brand = "Autel",
            category = "1 colos 6K No-Geofence",
            newRetailPriceHuf = 380000,
            baseUsedPriceHuf = 185000,
            flyMoreComboBonusHuf = 50000,
            smartControllerBonusHuf = 35000,
            popularity = "🟢 Geofence-mentes alternatíva",
            averageTurnoverDays = "5-10 nap",
            description = "1 hüvelykes 6K kamera állítható rekeszértékkel (f/2.8-f/11), beépített szoftveres geofence korlátozás nélkül.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "autel_evo_nano_plus",
            modelName = "Autel EVO Nano+",
            brand = "Autel",
            category = "Sub-249g RYYB Szenzor",
            newRetailPriceHuf = 230000,
            baseUsedPriceHuf = 110000,
            flyMoreComboBonusHuf = 35000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Kiváló éjszakai képességek",
            averageTurnoverDays = "6-12 nap",
            description = "1/1.28 colos RYYB f/1.9 fényerejű kamera, 249 gramm alatti súly, 3 irányú akadályelkerülés.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "autel_evo_2_pro",
            modelName = "Autel EVO II Pro (V2 / V3 6K)",
            brand = "Autel",
            category = "Profi 6K No-Geofence",
            newRetailPriceHuf = 550000,
            baseUsedPriceHuf = 270000,
            flyMoreComboBonusHuf = 60000,
            smartControllerBonusHuf = 45000,
            popularity = "🟢 Ipari & filmes munkaeszköz",
            averageTurnoverDays = "5-12 nap",
            description = "1 colos 6K kamera, 40 perces repülési idő, 360 fokos akadályérzékelés, NFZ korlátozások nélkül.",
            releaseYear = 2021
        ),

        // Custom FPV & Versenydrónok
        DroneModelSpec(
            id = "custom_fpv_5_o3",
            modelName = "Épített 5\" Freestyle FPV (DJI O3 Air Unit)",
            brand = "FPV",
            category = "Digitális HD Verseny/Freestyle",
            newRetailPriceHuf = 220000,
            baseUsedPriceHuf = 120000,
            flyMoreComboBonusHuf = 30000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Keresett a pilóták között",
            averageTurnoverDays = "4-8 nap",
            description = "DJI O3 digitális videoadóval szerelt karbonvázas 6S akkus freestyle vagy long-range drón.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "custom_fpv_5_analog",
            modelName = "Épített 5\" Freestyle FPV (Analóg rendszer)",
            brand = "FPV",
            category = "Analóg Karbon Freestyle",
            newRetailPriceHuf = 120000,
            baseUsedPriceHuf = 55000,
            flyMoreComboBonusHuf = 20000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Belépő FPV kategória",
            averageTurnoverDays = "5-12 nap",
            description = "Hagyományos analóg 5.8 GHz-es videórendszerrel ellátott tartós karbon quadkopter.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "iflight_nazgul5",
            modelName = "iFlight Nazgul5 V3 HD / Analog",
            brand = "FPV",
            category = "Gyári 5\" Freestyle BNF",
            newRetailPriceHuf = 190000,
            baseUsedPriceHuf = 105000,
            flyMoreComboBonusHuf = 25000,
            smartControllerBonusHuf = 0,
            popularity = "🔥 A legnépszerűbb gyári FPV drón",
            averageTurnoverDays = "3-6 nap",
            description = "Masszív XING-E motorok, Blitz vezérlő, HD vagy analóg átvitel. Kiemelkedően tartós freestyle gép.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "geprc_cinelog35",
            modelName = "GEPRC CineLog 35 / 25 HD",
            brand = "FPV",
            category = "Kamerás Cinewhoop",
            newRetailPriceHuf = 210000,
            baseUsedPriceHuf = 115000,
            flyMoreComboBonusHuf = 25000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Népszerű filmes whoop",
            averageTurnoverDays = "3-7 nap",
            description = "Védett propellerek GoPro cipeléséhez, zárt téri és beltéri forgatásokhoz.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "betafpv_cetus_pro_x",
            modelName = "BetaFPV Cetus X / Pro FPV RTF Kit",
            brand = "FPV",
            category = "Szobai / Kerti Tinywhoop RTF",
            newRetailPriceHuf = 95000,
            baseUsedPriceHuf = 48000,
            flyMoreComboBonusHuf = 12000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Kezdő lakás-FPV készlet",
            averageTurnoverDays = "3-6 nap",
            description = "Komplett 'Ready to Fly' készlet szemüveggel és távirányítóval szimulátorhoz és gyakorláshoz.",
            releaseYear = 2022
        ),

        // Egyéb népszerű gyártók
        DroneModelSpec(
            id = "fimi_x8_se",
            modelName = "Fimi X8 SE 2022 V2 / 2020",
            brand = "Egyéb",
            category = "4K HDR Megfizethető Utazódrón",
            newRetailPriceHuf = 190000,
            baseUsedPriceHuf = 85000,
            flyMoreComboBonusHuf = 25000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Jó ár-érték arányú 4K",
            averageTurnoverDays = "6-12 nap",
            description = "1/2 colos Sony CMOS szenzor, 4K/30fps, 35 perc repülés, hangszóró / csomagledobó kiegészítő port.",
            releaseYear = 2022
        ),
        DroneModelSpec(
            id = "fimi_x8_mini",
            modelName = "Fimi X8 Mini V2",
            brand = "Egyéb",
            category = "Sub-249g Kedvező árú 4K",
            newRetailPriceHuf = 130000,
            baseUsedPriceHuf = 65000,
            flyMoreComboBonusHuf = 20000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Kompakt alternatíva",
            averageTurnoverDays = "5-10 nap",
            description = "249 gramm alatti 4K kamera, USB-C gyorstöltés és 30 perc repülési idő.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "potesic_atom_4k",
            modelName = "Potensic Atom 4K (3-tengelyes gimbal)",
            brand = "Egyéb",
            category = "Sub-249g Pénztárcabarát 4K",
            newRetailPriceHuf = 135000,
            baseUsedPriceHuf = 68000,
            flyMoreComboBonusHuf = 22000,
            smartControllerBonusHuf = 0,
            popularity = "🟢 Kedvező árú alternatíva",
            averageTurnoverDays = "4-8 nap",
            description = "3-tengelyes mechanikus gimballal és 4K kamerával rendelkező olcsóbb 249 gramm alatti alternatíva.",
            releaseYear = 2023
        ),
        DroneModelSpec(
            id = "hubsan_zino_mini",
            modelName = "Hubsan Zino Mini Pro / SE",
            brand = "Egyéb",
            category = "Sub-249g Akadályérzékelős",
            newRetailPriceHuf = 170000,
            baseUsedPriceHuf = 75000,
            flyMoreComboBonusHuf = 22000,
            smartControllerBonusHuf = 0,
            popularity = "⚖️ Ritkább alternatíva",
            averageTurnoverDays = "7-15 nap",
            description = "4K 30fps kamera, 3-irányú akadályelkerülő rendszer 249 gramm alatti vázban.",
            releaseYear = 2021
        )
    )

    /**
     * Calculate realistic second-hand drone market price in Hungary based on user parameters
     */
    fun calculateDroneValue(
        model: DroneModelSpec,
        bundleType: String, // "Alap (1 akku)", "Fly More Combo (3 akku + hub + táska)", "Fly More Plus (Kijelzős RC2 / Smart)"
        condition: String, // "Újszerű / Karcmentes (Dobozos)", "Megkímélt / Normál használat", "Használt / Kisebb esztétikai hiba", "Sérült / Javított"
        batteryCycles: String, // "1-15 ciklus (Alig használt)", "15-40 ciklus (Normál)", "40+ ciklus (Fáradt akkuk)"
        hasValidWarranty: Boolean,
        extraAccessories: List<String> // e.g. "ND szűrőkészlet", "Vízálló kemény bőrönd", "Pótpropellerek", "Gyors memóriakártya (128GB+)"
    ): PriceEstimationResult {
        val breakdown = mutableListOf<PriceFactor>()

        // 1. Base price of drone
        var basePrice = model.baseUsedPriceHuf
        breakdown.add(PriceFactor("Alap használt piaci érték (${model.modelName})", basePrice, true))

        // 2. Bundle and Controller adjustment
        var bundleAddition = 0
        val isFlyMore = bundleType.contains("Fly More", ignoreCase = true) || bundleType.contains("Combo", ignoreCase = true)
        val isSmartRc = bundleType.contains("Smart", ignoreCase = true) || bundleType.contains("Kijelzős", ignoreCase = true) || bundleType.contains("RC2", ignoreCase = true) || bundleType.contains("RC 2", ignoreCase = true) || bundleType.contains("RC Pro", ignoreCase = true) || bundleType.contains("DJI RC", ignoreCase = true)
        val isGoggles = bundleType.contains("Goggles", ignoreCase = true) || bundleType.contains("Szemüveg", ignoreCase = true)
        val isNoController = bundleType.contains("Távirányító nélkül", ignoreCase = true)
        val isOnlyDrone = bundleType.contains("Csak dróntest", ignoreCase = true) || bundleType.contains("Csak a gép", ignoreCase = true)

        if (isFlyMore) {
            val comboBonus = if (model.flyMoreComboBonusHuf > 0) model.flyMoreComboBonusHuf else 35000
            bundleAddition += comboBonus
            breakdown.add(PriceFactor("Fly More Combo csomag (3 akku + hub + táska)", comboBonus, true))
        }
        if (isSmartRc) {
            val smartBonus = if (model.smartControllerBonusHuf > 0) model.smartControllerBonusHuf else 30000
            bundleAddition += smartBonus
            breakdown.add(PriceFactor("Beépített kijelzős Smart / RC2 távirányító", smartBonus, true))
        } else if (isGoggles) {
            val fpvBonus = if (model.smartControllerBonusHuf > 0) model.smartControllerBonusHuf + 20000 else 40000
            bundleAddition += fpvBonus
            breakdown.add(PriceFactor("FPV Szemüveg (Goggles) + Vezérlő készlet", fpvBonus, true))
        }
        if (isNoController) {
            bundleAddition -= 20000
            breakdown.add(PriceFactor("Távirányító nélküli levonás", -20000, false))
        }
        if (isOnlyDrone) {
            bundleAddition -= 15000
            breakdown.add(PriceFactor("Akku / töltő nélküli test", -15000, false))
        }

        // 3. Condition factor
        var subtotal = basePrice + bundleAddition
        var conditionAdjustment = 0
        when (condition) {
            "Újszerű / Karcmentes (Dobozos)" -> {
                conditionAdjustment = (subtotal * 0.08f).toInt()
                breakdown.add(PriceFactor("Kifogástalan, karcmentes gyári dobozos állapot", conditionAdjustment, true))
            }
            "Megkímélt / Normál használat" -> {
                // 0 change
            }
            "Használt / Kisebb esztétikai hiba" -> {
                conditionAdjustment = -(subtotal * 0.12f).toInt()
                breakdown.add(PriceFactor("Esztétikai karcok / kopások levonása", conditionAdjustment, false))
            }
            "Sérült / Javított" -> {
                conditionAdjustment = -(subtotal * 0.28f).toInt()
                breakdown.add(PriceFactor("Korábbi törés / javítás értékcsökkenése", conditionAdjustment, false))
            }
        }

        // 4. Battery cycle adjustment
        var batteryAdjustment = 0
        when (batteryCycles) {
            "1-15 ciklus (Alig használt)" -> {
                batteryAdjustment = 8000
                breakdown.add(PriceFactor("Friss, minimális ciklusszámú akkumulátorok", batteryAdjustment, true))
            }
            "15-40 ciklus (Normál)" -> {
                // 0 change
            }
            "40+ ciklus (Fáradt akkuk)" -> {
                batteryAdjustment = -12000
                breakdown.add(PriceFactor("Magas akku ciklusszám (kapacitásvesztés)", batteryAdjustment, false))
            }
        }

        // 5. Warranty / DJI Care
        var warrantyAdjustment = 0
        if (hasValidWarranty) {
            warrantyAdjustment = (subtotal * 0.07f).toInt().coerceAtLeast(10000).coerceAtMost(35000)
            breakdown.add(PriceFactor("Érvényes hivatalos garancia / DJI Care Refresh", warrantyAdjustment, true))
        }

        // 6. Extra accessories
        var extraAccessoriesTotal = 0
        extraAccessories.forEach { acc ->
            val value = when {
                acc.contains("ND szűrő", ignoreCase = true) -> 12000
                acc.contains("Kemény bőrönd", ignoreCase = true) || acc.contains("Vízálló", ignoreCase = true) -> 14000
                acc.contains("Memóriakártya", ignoreCase = true) -> 6000
                acc.contains("Pótpropeller", ignoreCase = true) -> 4000
                acc.contains("Landolópad", ignoreCase = true) -> 3000
                else -> 5000
            }
            extraAccessoriesTotal += value
            breakdown.add(PriceFactor(acc, value, true))
        }

        val estimatedAvg = (subtotal + conditionAdjustment + batteryAdjustment + warrantyAdjustment + extraAccessoriesTotal)
            // Round to neat 1,000s
            .let { (it / 1000) * 1000 }

        val minRec = ((estimatedAvg * 0.93f) / 1000).toInt() * 1000
        val maxRec = ((estimatedAvg * 1.07f) / 1000).toInt() * 1000
        val quickSale = ((estimatedAvg * 0.88f) / 1000).toInt() * 1000
        val patientSale = ((estimatedAvg * 1.12f) / 1000).toInt() * 1000

        val notes = when {
            model.popularity.contains("Top 1") || model.popularity.contains("Kiemelten") ->
                "Ez a modell az egyik leglikvidebb a magyar piacon. Reális árazással 1-3 napon belül eladható, nagy a kereslet a kezdő és haladó pilóták körében."
            model.brand == "DJI" && model.category.contains("Sub-249g") ->
                "A 249 gramm alatti kategória a legnépszerűbb Magyarországon a laza EU-s szabályozás (A1 nyílt alkategória) miatt. Gyorsan gazdára talál."
            model.category.contains("Hasselblad") || model.category.contains("Profi") ->
                "Professzionális filmes kategória. Kifejezetten a számlaképes vagy garanciás darabokat keresik a videósok."
            else ->
                "Stabil piaci modell. Az ár-érték arány és a részletes leírás döntő a gyors eladásban."
        }

        val tips = listOf(
            "📷 Készíts éles fotót a drónról, a propellerekről és a gimbal védőkupakról!",
            "🔋 Fotózd le a DJI Fly appban az akkumulátorok pontos ciklusszámát, ez növeli a vásárlók bizalmát.",
            "📦 Ha megvan a gyári doboz és a számla/garancialevél, mindenképp fotózd be a csomagba.",
            "🤝 Kínálj személyes kipróbálási lehetőséget felszállással, így sokkal hamarabb döntenek a vevők.",
            "⚖️ A hirdetésben tüntesd fel, hogy az ár minimálisan alkuképes-e vagy fix."
        )

        return PriceEstimationResult(
            modelName = model.modelName,
            brand = model.brand,
            condition = condition,
            bundleType = bundleType,
            batteryCycleRange = batteryCycles,
            hasWarranty = hasValidWarranty,
            extraAccessories = extraAccessories,
            estimatedAveragePriceHuf = estimatedAvg,
            recommendedMinPriceHuf = minRec,
            recommendedMaxPriceHuf = maxRec,
            quickSalePriceHuf = quickSale,
            patientSalePriceHuf = patientSale,
            marketPopularity = model.popularity,
            turnoverSpeed = model.averageTurnoverDays,
            marketNotes = notes,
            breakdown = breakdown,
            tipsForSeller = tips
        )
    }

    /**
     * Finds a matching model from the database by user input string or creates a calculated custom model spec.
     */
    fun findModelOrEstimate(
        rawDroneInput: String,
        customNewPriceHuf: Int? = null
    ): DroneModelSpec {
        val trimmed = rawDroneInput.trim()
        if (trimmed.isBlank()) {
            return droneModels.first()
        }

        // 1. Direct exact or substring match
        val matched = droneModels.firstOrNull {
            it.modelName.equals(trimmed, ignoreCase = true)
        } ?: droneModels.firstOrNull {
            trimmed.lowercase().contains(it.modelName.lowercase()) ||
            it.modelName.lowercase().contains(trimmed.lowercase())
        } ?: droneModels.firstOrNull {
            val qParts = trimmed.lowercase().split(" ", "-", "/").filter { it.length >= 3 }
            qParts.any { part -> it.modelName.lowercase().contains(part) }
        }

        if (matched != null) {
            return matched
        }

        // 2. Custom/Unknown model with provided or estimated price
        val newPrice = customNewPriceHuf ?: 200000
        val baseUsed = (newPrice * 0.60f).toInt()
        val detectedBrand = when {
            trimmed.contains("DJI", ignoreCase = true) -> "DJI"
            trimmed.contains("Autel", ignoreCase = true) -> "Autel"
            trimmed.contains("FPV", ignoreCase = true) || trimmed.contains("Nazgul", ignoreCase = true) -> "FPV"
            else -> "Egyéb"
        }

        return DroneModelSpec(
            id = "custom_${System.currentTimeMillis()}",
            modelName = trimmed,
            brand = detectedBrand,
            category = "Egyedi drón modell",
            newRetailPriceHuf = newPrice,
            baseUsedPriceHuf = baseUsed,
            flyMoreComboBonusHuf = (newPrice * 0.20f).toInt(),
            smartControllerBonusHuf = (newPrice * 0.15f).toInt(),
            popularity = "⚖️ Egyedi piaci modell",
            averageTurnoverDays = "5-10 nap",
            description = "Egyedi / ritkább drón típus a megadott műszaki paraméterekkel és piaci avulási rátával.",
            releaseYear = 2023
        )
    }
}
