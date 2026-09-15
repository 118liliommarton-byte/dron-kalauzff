package com.example.data

enum class ZoneType(
    val title: String,
    val shortName: String,
    val icon: String,
    val colorHex: String,
    val badgeText: String,
    val defaultMaxAltitude: String
) {
    CTR_AIRPORT(
        title = "Repülőtér Ellenőrzött Zóna (CTR / TIZ)",
        shortName = "Repülőtér CTR",
        icon = "✈️",
        colorHex = "#EF4444",
        badgeText = "SZIGORÚ CTR ENGEDÉLY",
        defaultMaxAltitude = "0 m (Engedély nélkül tilos)"
    ),
    PROHIBITED_NO_FLY(
        title = "Tiltott Légtér (P-Zóna / No-Fly)",
        shortName = "Tiltott P-Zóna",
        icon = "🛑",
        colorHex = "#991B1B",
        badgeText = "SZIGORÚAN TILOS",
        defaultMaxAltitude = "0 m (Tilos a berepülés)"
    ),
    RESTRICTED(
        title = "Korlátozott Légtér (R-Zóna / Védett Létesítmény)",
        shortName = "Korlátozott R-Zóna",
        icon = "⚠️",
        colorHex = "#F59E0B",
        badgeText = "KORLÁTOZOTT LÉGTÉR",
        defaultMaxAltitude = "Csak eseti engedéllyel"
    ),
    MILITARY_DANGER(
        title = "Katonai Gyakorló & Veszélyes Zóna (D-Zóna / TSA)",
        shortName = "Katonai D-Zóna",
        icon = "🛡️",
        colorHex = "#8B5CF6",
        badgeText = "KATONAI VESZÉLYZÓNA",
        defaultMaxAltitude = "0 m aktív időszakban"
    ),
    NATIONAL_PARK(
        title = "Nemzeti Park & Természetvédelmi Körzet",
        shortName = "Nemzeti Park",
        icon = "🌲",
        colorHex = "#10B981",
        badgeText = "TERMÉSZETVÉDELEM",
        defaultMaxAltitude = "Természetvédelmi engedéllyel"
    ),
    TEMPORARY_RESERVED(
        title = "Eseti Légtér (TRA / Rendezvény / Belterület)",
        shortName = "Eseti Légtér (TRA)",
        icon = "⏱️",
        colorHex = "#3B82F6",
        badgeText = "ESETI LÉGTÉR KÖTELES",
        defaultMaxAltitude = "Engedélyezett plafonig"
    ),
    HOSPITAL_HEMS(
        title = "Kórház & Mentőhelikopter Bázis (HEMS / Sürgősségi)",
        shortName = "Kórház HEMS",
        icon = "🏥",
        colorHex = "#EC4899",
        badgeText = "HEMS KÓRHÁZI LÉGTÉR",
        defaultMaxAltitude = "0 m (Életmentés miatt tilos!)"
    ),
    DYNAMIC_NOTAM(
        title = "Élő NOTAM Korlátozás (Dinamikus / Időleges Zóna)",
        shortName = "Élő NOTAM",
        icon = "📡",
        colorHex = "#F97316",
        badgeText = "IDŐLEGES NOTAM KORLÁTOZÁS",
        defaultMaxAltitude = "NOTAM felhívás szerinti magasság"
    ),
    OPEN_CATEGORY(
        title = "Nyílt Kategória (Open Geocage 120m)",
        shortName = "Nyílt Légtér",
        icon = "🟢",
        colorHex = "#06B6D4",
        badgeText = "SZABADON REPÜLHETŐ (MAX 120M)",
        defaultMaxAltitude = "120 m AGL"
    )
}

data class AirspaceZone(
    val id: String,
    val countryName: String,
    val name: String,
    val type: ZoneType,
    val lat: Double,
    val lng: Double,
    val radiusMeters: Int,
    val lowerLimit: String,
    val upperLimit: String,
    val maxDroneAltitude: String,
    val activityHours: String,
    val description: String,
    val permitRequired: String,
    val contactAuthority: String,
    val penalties: String,
    val nearbyCities: String
)

data class AirspaceGuideItem(
    val title: String,
    val shortCode: String,
    val icon: String,
    val colorHex: String,
    val subtitle: String,
    val description: String,
    val requirements: List<String>,
    val rulesForDrones: List<String>,
    val penaltyInfo: String
)

object AirspaceDataRepository {

    val hungaryZones: List<AirspaceZone> = listOf(
        AirspaceZone(
            id = "LHBP_CTR",
            countryName = "Magyarország",
            name = "Budapest Liszt Ferenc Nemzetközi Repülőtér (LHBP CTR)",
            type = ZoneType.CTR_AIRPORT,
            lat = 47.4369,
            lng = 19.2556,
            radiusMeters = 15000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3500 ft (1066 m) AMSL",
            maxDroneAltitude = "0 m (Engedély nélkül szigorúan tilos!)",
            activityHours = "H-V: 00:00 - 24:00 (Folyamatos 24/7)",
            description = "Ferihegy 1 és 2 terminálok, 13L/31R és 13R/31L aktív futópálya megközelítési tengelyek. Repülőtéri nagygépes utasszállító forgalom miatt a drónozás életveszélyes és bűncselekmény!",
            permitRequired = "Katonai Légügyi Hatóság eseti légtér kijelölés (30 nappal előre) + HungaroControl koordináció + MyDroneSpace élő bejelentkezés + TWR rádiókapcsolat.",
            contactAuthority = "HungaroControl Zrt. / Légiforgalmi Irányítás (TWR)",
            penalties = "Engedély nélküli berepülés esetén 500 000 - 3 000 000 Ft közigazgatási bírság és drón lefoglalás, légiközlekedés veszélyeztetése miatt büntetőeljárás!",
            nearbyCities = "Budapest XVIII., XVII., XIX., Vecsés, Üllő, Ecser, Gyál, Monor"
        ),
        AirspaceZone(
            id = "LHBS_TIZ",
            countryName = "Magyarország",
            name = "Budaörs Repülőtér (LHBS) & Sportrepülőtér Körzet",
            type = ZoneType.CTR_AIRPORT,
            lat = 47.4503,
            lng = 18.9872,
            radiusMeters = 5500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2500 ft (762 m) AMSL",
            maxDroneAltitude = "0 m (LHBS AFIS engedély nélkül tilos)",
            activityHours = "SR-SS (Napkelte - Napnyugta)",
            description = "Kisgépes oktató, vitorlázó és helikopteres repülések központja. Alacsonyan repülő kisgépek és ejtőernyős ugrások miatt veszélyes.",
            permitRequired = "LHBS repülőtér üzembentartói hozzájárulás + MyDroneSpace egyeztetés.",
            contactAuthority = "Budaörs Repülőtér Üzembentartó (AFIS)",
            penalties = "100 000 - 1 000 000 Ft bírság.",
            nearbyCities = "Budaörs, Budapest XI., XXII., Törökbálint"
        ),
        AirspaceZone(
            id = "LHDX_TIZ",
            countryName = "Magyarország",
            name = "Dunakeszi Vitorlázórepülőtér (LHDX)",
            type = ZoneType.CTR_AIRPORT,
            lat = 47.6167,
            lng = 19.1417,
            radiusMeters = 4000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "0 m (Üzemidőben engedélyköteles)",
            activityHours = "Hétvégén és jó időben intenzív",
            description = "Csörléses és vontatásos vitorlázórepülés, sárkányrepülők. A csörlőkötél akár 1000 méter magasságig érhet!",
            permitRequired = "Dunakeszi repülőtérvezetői egyeztetés és MyDroneSpace.",
            contactAuthority = "Malév Repülőklub / Opitz Nándor RK",
            penalties = "100 000 - 800 000 Ft bírság.",
            nearbyCities = "Dunakeszi, Fót, Budapest IV., Göd"
        ),
        AirspaceZone(
            id = "LHDC_CTR",
            countryName = "Magyarország",
            name = "Debrecen Nemzetközi Repülőtér (LHDC CTR)",
            type = ZoneType.CTR_AIRPORT,
            lat = 47.4889,
            lng = 21.6156,
            radiusMeters = 12000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3000 ft (914 m) AMSL",
            maxDroneAltitude = "0 m (Engedély nélkül tilos)",
            activityHours = "H-V: 00:00 - 24:00",
            description = "Nemzetközi menetrend szerinti utasszállító és charter forgalom (Wizz Air, Lufthansa).",
            permitRequired = "Katonai Légügyi Hatóság eseti légtér + Debrecen TWR torony koordináció.",
            contactAuthority = "Debrecen Airport TWR / HungaroControl",
            penalties = "300 000 - 2 500 000 Ft bírság.",
            nearbyCities = "Debrecen, Mikepércs, Sáránd, Derecske, Ebes, Hajdúszovát"
        ),
        AirspaceZone(
            id = "LHSK_CTR",
            countryName = "Magyarország",
            name = "Hévíz-Balaton (Sármellék) Nemzetközi Repülőtér (LHSK CTR)",
            type = ZoneType.CTR_AIRPORT,
            lat = 46.6864,
            lng = 17.1592,
            radiusMeters = 12000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3000 ft AMSL",
            maxDroneAltitude = "0 m (Engedély nélkül tilos)",
            activityHours = "H-V: Üzemidőben",
            description = "Nemzetközi charter és üzleti forgalom a Nyugat-Balaton térségében.",
            permitRequired = "Katonai Légügyi Hatóság eseti légtér + Sármellék TWR.",
            contactAuthority = "Hévíz-Balaton Airport Kft.",
            penalties = "250 000 - 2 000 000 Ft bírság.",
            nearbyCities = "Sármellék, Keszthely, Hévíz, Zalavár, Balatonszentgyörgy"
        ),
        AirspaceZone(
            id = "LHPR_CTR",
            countryName = "Magyarország",
            name = "Győr-Pér Nemzetközi Repülőtér (LHPR CTR)",
            type = ZoneType.CTR_AIRPORT,
            lat = 47.6294,
            lng = 17.8117,
            radiusMeters = 9000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2500 ft AMSL",
            maxDroneAltitude = "0 m (Engedély nélkül tilos)",
            activityHours = "H-V: 07:00 - 19:00 LT",
            description = "Audi Hungaria üzleti járatai és nemzetközi kisgépes forgalom.",
            permitRequired = "LHPR repülőtér és HungaroControl jóváhagyás.",
            contactAuthority = "Győr-Pér Airport Zrt.",
            penalties = "200 000 - 1 500 000 Ft bírság.",
            nearbyCities = "Győr, Pér, Töltéstava, Mezőörs, Pázmándfalu"
        ),
        AirspaceZone(
            id = "LHNY_TIZ",
            countryName = "Magyarország",
            name = "Nyíregyháza Repülőtér (LHNY)",
            type = ZoneType.CTR_AIRPORT,
            lat = 47.9822,
            lng = 21.6928,
            radiusMeters = 6000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2500 ft AMSL",
            maxDroneAltitude = "0 m (AFIS engedélyköteles)",
            activityHours = "Napkelte - Napnyugta (intenzív növendékképzés)",
            description = "Tréner Kft. pilótaképző központ (Wizz Air Akadémia) intenzív többgépes iskolakörökkel.",
            permitRequired = "Nyíregyháza AFIS és MyDroneSpace koordináció.",
            contactAuthority = "Tréner Kft. / Nyíregyháza Repülőtér",
            penalties = "150 000 - 1 000 000 Ft bírság.",
            nearbyCities = "Nyíregyháza, Kótaj, Nagykálló, Oros"
        ),
        AirspaceZone(
            id = "LHKE_MCTR",
            countryName = "Magyarország",
            name = "Kecskemét MH Kiss József Repülőbázis (LHKE MCTR)",
            type = ZoneType.MILITARY_DANGER,
            lat = 46.9172,
            lng = 19.7492,
            radiusMeters = 16000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "4000 ft AMSL",
            maxDroneAltitude = "0 m (Szigorúan tilos katonai légtér!)",
            activityHours = "H-P: 00:00 - 24:00 (és riasztási ügyelet)",
            description = "JAS-39 Gripen vadászgépek és KC-390 szállítógépek bázisa. A NATO QRA (Quick Reaction Alert) készültség miatt bármilyen illetéktelen drón azonnali katonai fenyegetésnek minősül!",
            permitRequired = "Katonai Légügyi Hatóság (KLH) és MH Légierő Parancsnokság írásos miniszteri engedélye.",
            contactAuthority = "Magyar Honvédség Légierő Parancsnokság",
            penalties = "Azonnali fegyveres lefoglalás, 1 000 000 - 5 000 000 Ft bírság és nemzetbiztonsági eljárás!",
            nearbyCities = "Kecskemét, Nagykőrös, Városföld, Nyárlőrinc, Helvécia"
        ),
        AirspaceZone(
            id = "LHPA_MCTR",
            countryName = "Magyarország",
            name = "Pápa MH 47. Bázisrepülőtér (LHPA MCTR)",
            type = ZoneType.MILITARY_DANGER,
            lat = 47.3639,
            lng = 17.4981,
            radiusMeters = 14000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3500 ft AMSL",
            maxDroneAltitude = "0 m (Szigorúan védett katonai légtér)",
            activityHours = "H-V: 24 órás NATO bázis",
            description = "A NATO SAC (Strategic Airlift Capability) C-17 Globemaster III nehéz teherszállító repülőgépek európai otthona.",
            permitRequired = "KLH és NATO HAW Parancsnokság jóváhagyás.",
            contactAuthority = "MH 47. Bázisrepülőtér / NATO SAC",
            penalties = "Katonai feljelentés és azonnali drónlefoglalás!",
            nearbyCities = "Pápa, Nemesgörzsöny, Vaszar, Marcaltő"
        ),
        AirspaceZone(
            id = "LHTL_MCTR",
            countryName = "Magyarország",
            name = "Szolnok MH Kiss József 86. Helikopterdandár (LHTL MCTR)",
            type = ZoneType.MILITARY_DANGER,
            lat = 47.1219,
            lng = 20.2344,
            radiusMeters = 12000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3000 ft AMSL",
            maxDroneAltitude = "0 m (Szigorúan tilos katonai légtér)",
            activityHours = "H-V: 00:00 - 24:00",
            description = "H145M és H225M katonai helikopterek, valamint ejtőernyős különleges műveleti erők bázisa.",
            permitRequired = "KLH és Dandárparancsnoki engedély.",
            contactAuthority = "MH 86. Helikopterdandár Szolnok",
            penalties = "Katonai eljárás és 1 000 000 Ft feletti bírság.",
            nearbyCities = "Szolnok, Szandaszőlős, Rákóczifalva, Tószeg"
        ),
        AirspaceZone(
            id = "LH_P1_PAKS",
            countryName = "Magyarország",
            name = "Paksi Atomerőmű (LH-P1 Tiltott Zóna)",
            type = ZoneType.PROHIBITED_NO_FLY,
            lat = 46.5728,
            lng = 18.8544,
            radiusMeters = 5000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "UNLIMITED (Korlátlan magasság)",
            maxDroneAltitude = "0 m (SZIGORÚAN TILOS / NO-FLY)",
            activityHours = "Folyamatos 24/7/365",
            description = "Kritikus energetikai infrastruktúra és nukleáris létesítmény. A zónába repülő drónokat a Készenléti Rendőrség és a fegyveres biztonsági őrség drónelhárító rendszerrel azonnal semlegesíti és megsemmisíti!",
            permitRequired = "Nincs magáncélú engedély! Csak kormányzati és nukleáris biztonsági felhatalmazással.",
            contactAuthority = "Országos Atomenergia Hivatal (OAH) / MVM Paksi Atomerőmű Zrt.",
            penalties = "Nemzetbiztonsági őrizetbevétel, 5 000 000 Ft-ig terjedő bírság és börtönbüntetés!",
            nearbyCities = "Paks, Dunaszentgyörgy, Gerjen, Dunaföldvár"
        ),
        AirspaceZone(
            id = "LH_P2_KFKI",
            countryName = "Magyarország",
            name = "KFKI Atomkutató Intézet (LH-P2 Csillebérc)",
            type = ZoneType.PROHIBITED_NO_FLY,
            lat = 47.4878,
            lng = 18.9556,
            radiusMeters = 2000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3000 ft AMSL",
            maxDroneAltitude = "0 m (SZIGORÚAN TILOS)",
            activityHours = "Folyamatos 24/7",
            description = "Budapesti Kutatóreaktor területe Csillebércen.",
            permitRequired = "Kormányzati nukleáris engedély.",
            contactAuthority = "Energiatudományi Kutatóközpont",
            penalties = "Azonnali feljelentés és eljárás.",
            nearbyCities = "Budapest XII. kerület, Budaörs"
        ),
        AirspaceZone(
            id = "LH_R1_PARLAMENT",
            countryName = "Magyarország",
            name = "Országház & Belvárosi Kormányzati Negyed (LH-R1A/B)",
            type = ZoneType.RESTRICTED,
            lat = 47.5072,
            lng = 19.0456,
            radiusMeters = 2500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2500 ft AMSL",
            maxDroneAltitude = "0 m (Kizárólag eseti légtérrel és TEK engedéllyel)",
            activityHours = "Folyamatos 24/7",
            description = "Magyar Országgyűlés, Miniszterelnökség, minisztériumok és védett középületek légtere a Duna mentén.",
            permitRequired = "Terrorelhárítási Központ (TEK) + Készenléti Rendőrség + KLH eseti légtér 30 nappal előre.",
            contactAuthority = "Országgyűlési Őrség / TEK / Légügyi Hatóság",
            penalties = "500 000 - 3 000 000 Ft bírság, személyi igazoltatás és eszközlefoglalás!",
            nearbyCities = "Budapest V., I., VI., VII., II., XIII. kerületek"
        ),
        AirspaceZone(
            id = "LH_R2_VAR",
            countryName = "Magyarország",
            name = "Budavári Palotanegyed & Sándor-palota (LH-R2)",
            type = ZoneType.RESTRICTED,
            lat = 47.4962,
            lng = 19.0396,
            radiusMeters = 2000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "0 m (Engedély nélkül tilos)",
            activityHours = "Folyamatos 24/7",
            description = "Köztársasági Elnöki Hivatal, Karmelita Kolostor és a Várnegyed védett épületei.",
            permitRequired = "Köztársasági Elnöki Testőrség (KR) hozzájárulás + eseti légtér.",
            contactAuthority = "Készenléti Rendőrség Személy- és Objektumvédelmi Igazgatóság",
            penalties = "500 000 - 2 000 000 Ft bírság.",
            nearbyCities = "Budapest I. kerület (Várnegyed, Krisztinaváros, Tabán)"
        ),
        AirspaceZone(
            id = "LH_R3_VISEGRAD",
            countryName = "Magyarország",
            name = "Visegrádi Fellegvár & Dunakanyar Történelmi Zóna",
            type = ZoneType.RESTRICTED,
            lat = 47.7928,
            lng = 18.9806,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "Csak engedéllyel",
            activityHours = "Műemlékvédelmi nyitvatartás alatt szigorított",
            description = "Nemzeti műemlék és fokozottan védett történelmi és turisztikai terület.",
            permitRequired = "Pilisi Parkerdő Zrt. és MNM Mátyás Király Múzeum hozzájárulás.",
            contactAuthority = "Magyar Nemzeti Múzeum Mátyás Király Múzeum",
            penalties = "100 000 - 500 000 Ft bírság.",
            nearbyCities = "Visegrád, Nagymaros, Dömös, Kismaros"
        ),
        AirspaceZone(
            id = "LH_D13_BAKONY",
            countryName = "Magyarország",
            name = "Bakony Harckiképző és Tüzérségi Lőtér (LH-D13)",
            type = ZoneType.MILITARY_DANGER,
            lat = 47.1942,
            lng = 18.0667,
            radiusMeters = 18000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "FL 200 (6000 m)",
            maxDroneAltitude = "0 m (Éleslövészet idején szigorúan tilos!)",
            activityHours = "NOTAM és lőtérparancsnoki hirdetmény szerint",
            description = "Közép-Európa egyik legnagyobb katonai lőtere. Éles tüzérségi, páncélos és helikopteres rakétalövészetek helyszíne!",
            permitRequired = "MH Bakony Harckiképző Központ Parancsnokság engedélye.",
            contactAuthority = "MH Bakony Harckiképző Központ (Várpalota)",
            penalties = "Életveszélyes zóna! Katonai őrizetbe vétel és szabálysértési eljárás.",
            nearbyCities = "Várpalota, Hajmáskér, Öskü, Veszprém, Zirc"
        ),
        AirspaceZone(
            id = "NP_HORTOBAGY",
            countryName = "Magyarország",
            name = "Hortobágyi Nemzeti Park (UNESCO Világörökség)",
            type = ZoneType.NATIONAL_PARK,
            lat = 47.5817,
            lng = 21.1483,
            radiusMeters = 25000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3000 ft AMSL",
            maxDroneAltitude = "Csak természetvédelmi engedéllyel",
            activityHours = "Folyamatos természetvédelmi oltalom",
            description = "Európa legnagyobb szikes pusztája, a daruvonulás és ritka ragadozó madarak fészkelőhelye. A drónzaj pánikot okoz a fészkelő madarakban!",
            permitRequired = "Hortobágyi Nemzeti Park Igazgatóság (HNPI) kutatási/filmezési engedélye.",
            contactAuthority = "Hortobágyi Nemzeti Park Igazgatóság (Debrecen)",
            penalties = "Természetvédelmi bírság 200 000 - 1 500 000 Ft, fészekpusztulás esetén büntetőjogi felelősségre vonás!",
            nearbyCities = "Hortobágy, Balmazújváros, Nádudvar, Tiszafüred, Egyek"
        ),
        AirspaceZone(
            id = "NP_BALATON",
            countryName = "Magyarország",
            name = "Balaton-felvidéki Nemzeti Park & Tihanyi Belső-tó",
            type = ZoneType.NATIONAL_PARK,
            lat = 46.9114,
            lng = 17.8864,
            radiusMeters = 15000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "Csak BFNP hozzájárulással",
            activityHours = "Minden évszakban védett",
            description = "Tihanyi Belső-tó, Káli-medence, Badacsony bazaltorgonái és a Kis-Balaton Ramsari vízimadár-élőhelye.",
            permitRequired = "Balaton-felvidéki Nemzeti Park Igazgatóság (Csopak) engedélye.",
            contactAuthority = "Balaton-felvidéki Nemzeti Park Igazgatóság (Csopak)",
            penalties = "150 000 - 1 000 000 Ft természetvédelmi bírság.",
            nearbyCities = "Tihany, Balatonfüred, Csopak, Káptalantóti, Badacsonytomaj, Keszthely"
        ),
        AirspaceZone(
            id = "NP_BUKK",
            countryName = "Magyarország",
            name = "Bükki Nemzeti Park & Szalajka-völgy",
            type = ZoneType.NATIONAL_PARK,
            lat = 48.0667,
            lng = 20.5333,
            radiusMeters = 16000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2500 ft AMSL",
            maxDroneAltitude = "Természetvédelmi engedéllyel",
            activityHours = "Folyamatosan védett",
            description = "Bükk-fennsík őserdő, Szilvásvárad, lillafüredi vízesés és ritka denevérkolóniák barlangjai.",
            permitRequired = "Bükki Nemzeti Park Igazgatóság (Eger) hozzájárulása.",
            contactAuthority = "Bükki Nemzeti Park Igazgatóság (Eger)",
            penalties = "100 000 - 800 000 Ft bírság.",
            nearbyCities = "Eger, Szilvásvárad, Miskolc-Lillafüred, Bélapátfalva, Répáshuta"
        ),
        AirspaceZone(
            id = "NP_AGGTELEK",
            countryName = "Magyarország",
            name = "Aggteleki Nemzeti Park & Baradla-barlang",
            type = ZoneType.NATIONAL_PARK,
            lat = 48.4714,
            lng = 20.4925,
            radiusMeters = 12000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "Csak ANPI engedéllyel",
            activityHours = "Folyamatos védelem",
            description = "UNESCO Világörökség karsztvidék, hucul ménes és denevérrezervátum.",
            permitRequired = "Aggteleki Nemzeti Park Igazgatóság engedélye.",
            contactAuthority = "Aggteleki Nemzeti Park Igazgatóság (Jósvafő)",
            penalties = "100 000 - 700 000 Ft bírság.",
            nearbyCities = "Jósvafő, Aggtelek, Szögliget, Perkupa"
        ),
        AirspaceZone(
            id = "NP_FERTO",
            countryName = "Magyarország",
            name = "Fertő-Hanság Nemzeti Park & Fertő-tó nádasai",
            type = ZoneType.NATIONAL_PARK,
            lat = 47.6667,
            lng = 16.8333,
            radiusMeters = 14000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "Csak FHNP engedéllyel",
            activityHours = "Nemzetközi határ és madárrezervátum",
            description = "Kócsagok, kanalasgémek és vándormadarak védett fészkelőhelye az osztrák-magyar határon.",
            permitRequired = "Fertő-Hanság Nemzeti Park Igazgatóság (Sarród) engedélye.",
            contactAuthority = "Fertő-Hanság Nemzeti Park Igazgatóság (Sarród)",
            penalties = "150 000 - 1 200 000 Ft bírság.",
            nearbyCities = "Fertőd, Sopron, Fertőrákos, Sarród, Kapuvár"
        ),
        AirspaceZone(
            id = "NP_DUNADRAVA",
            countryName = "Magyarország",
            name = "Duna-Dráva Nemzeti Park & Gemenci Ártéri Erdő",
            type = ZoneType.NATIONAL_PARK,
            lat = 46.2500,
            lng = 18.8833,
            radiusMeters = 16000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "Csak DDNPI engedéllyel",
            activityHours = "Fészkelési időben (március-július) szigorított",
            description = "Közép-Európa legnagyobb egybefüggő ártéri erdeje, a fekete gólya és a rétisas kiemelt fészkelőhelye.",
            permitRequired = "Duna-Dráva Nemzeti Park Igazgatóság (Pécs) engedélye.",
            contactAuthority = "Duna-Dráva Nemzeti Park Igazgatóság (Pécs)",
            penalties = "200 000 - 1 500 000 Ft bírság.",
            nearbyCities = "Baja, Szekszárd, Mohács, Bátaszék, Pörböly"
        ),
        AirspaceZone(
            id = "HEMS_HONVED_BP",
            countryName = "Magyarország",
            name = "Budapest Észak-Pesti Centrumkórház Honvédkórház HEMS Helikopter Leszálló",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.5303,
            lng = 19.0768,
            radiusMeters = 2500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Életmentő helikopterek miatt szigorúan tilos!)",
            activityHours = "24/7/365 Folyamatos sürgősségi légimentés",
            description = "Közép-Magyarország legforgalmasabb felnőtt sürgősségi és politraumatológiai HEMS központja. A tetőleszállóra és udvari leszállóra érkező mentőhelikopterek váratlanul, alacsonyan és nagy sebességgel érkeznek!",
            permitRequired = "Országos Mentőszolgálat (OMSZ) és Magyar Légimentő Nonprofit Kft. hozzájárulása + KLH eseti légtér.",
            contactAuthority = "Magyar Légimentő Bázis / Honvédkórház Sürgősségi Igazgatóság",
            penalties = "Közvetlen életveszély okozása miatt 1 000 000 - 3 000 000 Ft bírság és büntetőeljárás!",
            nearbyCities = "Budapest XIII., XIV., IV. kerületek (Angyalföld, Zugló)"
        ),
        AirspaceZone(
            id = "HEMS_SEMMELWEIS_BP",
            countryName = "Magyarország",
            name = "Semmelweis Egyetem Klinikai Központ & Bókay Gyermekklinika HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.4878,
            lng = 19.0736,
            radiusMeters = 2500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Életmentő gyermeksürgősségi légifolyosó)",
            activityHours = "24/7 Folyamatos készenlét",
            description = "Kiemelt újszülött-, gyermekintenzív és szívsebészeti HEMS fogadóbázis. A drónok által keltett légörvény és ütközésveszély a leszállás legkritikusabb másodperceiben végzetes katasztrófát okozhat.",
            permitRequired = "Semmelweis Egyetem és Légimentő Parancsnokság engedélye.",
            contactAuthority = "Semmelweis Egyetem Biztonságtechnikai Igazgatóság / OMSZ",
            penalties = "Azonnali rendőri intézkedés és súlyos közigazgatási bírság.",
            nearbyCities = "Budapest VIII., IX., Ferencváros, Józsefváros, Corvin-negyed"
        ),
        AirspaceZone(
            id = "HEMS_BALESETI_FIUMEI",
            countryName = "Magyarország",
            name = "Országos Baleseti és Mozgásszervi Intézet (Fiumei úti Traumatológia HEMS)",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.4983,
            lng = 19.0831,
            radiusMeters = 2000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Tilos a berepülés)",
            activityHours = "24/7 Folyamatos traumatológiai ügyelet",
            description = "Országos súlyos baleseti és gerincsérült ellátó központ tetőleszállóval. A belvárosi sűrű beépítés miatt a megközelítési folyosó rendkívül szűk.",
            permitRequired = "Országos Traumatológiai Intézet és Légimentők engedélye.",
            contactAuthority = "OBMI Igazgatóság / Légimentés",
            penalties = "Büntetőjogi felelősségre vonás és eszközlefoglalás.",
            nearbyCities = "Budapest VIII., VII., Keleti pályaudvar térsége"
        ),
        AirspaceZone(
            id = "HEMS_SZENT_LASZLO_DELPEST",
            countryName = "Magyarország",
            name = "Dél-pesti Centrumkórház (Szent László / Szent István Kórház HEMS)",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.4739,
            lng = 19.0945,
            radiusMeters = 2000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Légimentő folyosó)",
            activityHours = "24/7 Sürgősségi légimentés",
            description = "Kiemelt infektológiai, hematológiai és égési sérült centrum helikopter leszállója.",
            permitRequired = "Dél-pesti Centrumkórház és Légimentő koordináció.",
            contactAuthority = "Dél-pesti Centrumkórház Üzemeltetés",
            penalties = "500 000 - 2 000 000 Ft bírság.",
            nearbyCities = "Budapest IX., X., XIX. kerületek (Népliget, Kispest)"
        ),
        AirspaceZone(
            id = "HEMS_SZENT_JANOS_BUDA",
            countryName = "Magyarország",
            name = "Észak-Közép-budai Centrum Új Szent János Kórház HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.5112,
            lng = 18.9972,
            radiusMeters = 2000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Buda-hegyvidéki HEMS folyosó)",
            activityHours = "24/7 Folyamatos",
            description = "Észak- és Közép-Buda, valamint a Pilis-Dunakanyar térség traumatológiai és stroke centrumának mentőhelikopter leszállója.",
            permitRequired = "Szent János Kórház és Légimentők engedélye.",
            contactAuthority = "ÉKBC Új Szent János Kórház Igazgatóság",
            penalties = "Közigazgatási eljárás és szabálysértési bírság.",
            nearbyCities = "Budapest XII., II. kerületek (Pasarét, Svábhegy, Városmajor)"
        ),
        AirspaceZone(
            id = "HEMS_DEBRECEN_AUGUSZTA",
            countryName = "Magyarország",
            name = "Debreceni Egyetem Klinikai Központ HEMS Bázis & Traumatológia",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.5583,
            lng = 21.6281,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Kelet-Magyarországi HEMS csomópont)",
            activityHours = "24/7/365 Kiemelt készenlét",
            description = "Az Északkelet-Magyarországi régió legnagyobb egyetemi sürgősségi, neurotrauma és égési központja az Auguszta campuson. A debreceni légimentő helikopter és más bázisok gépei rendszeresen szállítanak ide súlyos sérülteket.",
            permitRequired = "Debreceni Egyetem Klinikai Központ és OMSZ Légimentés engedélye.",
            contactAuthority = "DE Klinikai Központ / Légimentő Bázis",
            penalties = "500 000 - 2 500 000 Ft bírság és drónelkobzás.",
            nearbyCities = "Debrecen (Nagyerdő, Egyetemváros), Pallag, Bocskaikert"
        ),
        AirspaceZone(
            id = "HEMS_SZEGED_KLINIKA",
            countryName = "Magyarország",
            name = "Szegedi Tudományegyetem Szent-Györgyi Albert Klinikai Központ HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 46.2464,
            lng = 20.1472,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Dél-Alföldi mentőhelikopter folyosó)",
            activityHours = "24/7 Sürgősségi készenlét",
            description = "A Dél-Alföldi régió legfontosabb politraumatológiai és kardiológiai egyetemi klinikája. A Tisza menti megközelítési tengely alacsony berepülést igényel a mentőhelikopterektől.",
            permitRequired = "SZTE Klinikai Központ és Légimentők engedélye.",
            contactAuthority = "SZTE Sürgősségi Betegellátó Önálló Osztály / OMSZ",
            penalties = "1 000 000 Ft feletti bírság és feljelentés.",
            nearbyCities = "Szeged belváros, Újszeged, Tiszasziget, Deszk"
        ),
        AirspaceZone(
            id = "HEMS_PECS_400AGYAS",
            countryName = "Magyarország",
            name = "Pécsi Tudományegyetem Klinikai Központ 400 Ágyas Klinika HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 46.0711,
            lng = 18.2056,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Dél-Dunántúli HEMS csomópont)",
            activityHours = "24/7 Folyamatos készenlét",
            description = "Dél-Dunántúl legfontosabb polytrauma, stroke és szívcentruma. A Mecsek lábánál fekvő helikopter leszállóra érkező gépek azonnali életmentést végeznek.",
            permitRequired = "PTE Klinikai Központ és Légimentők hozzájárulása.",
            contactAuthority = "PTE KK / Pécsi Légimentő Bázis (Pogány)",
            penalties = "500 000 - 2 000 000 Ft bírság.",
            nearbyCities = "Pécs (Egyetemváros, Uránváros), Pellérd, Keszü"
        ),
        AirspaceZone(
            id = "HEMS_GYOR_PETZ",
            countryName = "Magyarország",
            name = "Győr Petz Aladár Egyetemi Oktató Kórház Sürgősségi HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.6681,
            lng = 17.6439,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Észak-Dunántúli mentőhelikopter folyosó)",
            activityHours = "24/7 Kiemelt traumatológiai ügyelet",
            description = "Az M1-es autópálya és Észak-Dunántúl súlyos közlekedési baleseteit és ipari sérültjeit fogadó regionális traumaközpont tetőleszállója.",
            permitRequired = "Petz Aladár Kórház és Győri Légimentő Bázis engedélye.",
            contactAuthority = "Petz Aladár Egyetemi Oktató Kórház / OMSZ",
            penalties = "Légiközlekedés és életmentés veszélyeztetése miatti eljárás.",
            nearbyCities = "Győr (Nádorváros, Marcalváros, Adyváros), Győrújbarát"
        ),
        AirspaceZone(
            id = "HEMS_MISKOLC_MEGYEI",
            countryName = "Magyarország",
            name = "Borsod-Abaúj-Zemplén Vármegyei Központi Kórház HEMS (Miskolc)",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 48.1250,
            lng = 20.7889,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Északkelet-Magyarországi traumaközpont)",
            activityHours = "24/7 Sürgősségi készenlét",
            description = "A Szentpéteri kapui megyei központi kórház helikopter leszállója. Észak-Magyarország és a hegyvidéki barlangi/síbalesetek sérültjeit ide szállítja a miskolci légimentő gép.",
            permitRequired = "BAZ Megyei Központi Kórház és Miskolci Légimentő Bázis engedélye.",
            contactAuthority = "BAZ Vármegyei Központi Kórház Igazgatóság",
            penalties = "500 000 - 2 000 000 Ft bírság.",
            nearbyCities = "Miskolc (Szentpéteri kapu, Hejőcsaba), Szirmabesenyő, Felsőzsolca"
        ),
        AirspaceZone(
            id = "HEMS_FEHERVAR_SZENTGYORGY",
            countryName = "Magyarország",
            name = "Székesfehérvár Fejér Vármegyei Szent György Kórház HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 47.1811,
            lng = 18.4319,
            radiusMeters = 2500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (M7 autópálya sürgősségi mentőfolyosó)",
            activityHours = "24/7 Folyamatos",
            description = "Kiemelt regionális traumaközpont az M7/M6 autópályák és a Velencei-tó térségének súlyos sérültjeinek ellátására.",
            permitRequired = "Szent György Kórház és Légimentő koordináció.",
            contactAuthority = "Fejér Vármegyei Szent György Egyetemi Oktató Kórház",
            penalties = "Szabálysértési és büntetőjogi feljelentés.",
            nearbyCities = "Székesfehérvár, Szabadbattyán, Seregélyes, Pákozd"
        ),
        AirspaceZone(
            id = "HEMS_KECSKEMET_MEGYEI",
            countryName = "Magyarország",
            name = "Kecskemét Bács-Kiskun Vármegyei Oktatókórház Sürgősségi HEMS",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 46.9089,
            lng = 19.6644,
            radiusMeters = 2500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "1500 ft AMSL",
            maxDroneAltitude = "0 m (Duna-Tisza közi HEMS leszálló)",
            activityHours = "24/7 Folyamatos",
            description = "Az M5 autópálya és a Duna-Tisza köze sürgősségi traumatológiai és szívcentrumának helikopter leszállója a Nyíri úton.",
            permitRequired = "Bács-Kiskun Megyei Oktatókórház engedélye.",
            contactAuthority = "Bács-Kiskun Vármegyei Oktatókórház",
            penalties = "500 000 - 1 500 000 Ft bírság.",
            nearbyCities = "Kecskemét (Széchenyiváros, Belváros), Hetényegyháza"
        ),
        AirspaceZone(
            id = "HEMS_SZENTES_BAZIS",
            countryName = "Magyarország",
            name = "Szentes Magyar Légimentő Bázis (Csongrád-Csanád HEMS Központ)",
            type = ZoneType.HOSPITAL_HEMS,
            lat = 46.6500,
            lng = 20.2500,
            radiusMeters = 3500,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "2000 ft AMSL",
            maxDroneAltitude = "0 m (Sárga EC-135 mentőhelikopter riasztási zóna)",
            activityHours = "Napkelte - Napnyugta (Azonnali 3 perces riasztási idővel)",
            description = "A Magyar Légimentő Nonprofit Kft. aktív mentőhelikopter bázisa. A mentőhelikopter riasztás esetén másodpercek alatt száll fel alacsony magasságban vészhelyzeti kivonulásra!",
            permitRequired = "Magyar Légimentő Nonprofit Kft. parancsnokság engedélye.",
            contactAuthority = "Magyar Légimentő Bázis Szentes / OMSZ",
            penalties = "1 000 000 - 3 000 000 Ft bírság, légiforgalmi vészhelyzet okozása miatti büntetőeljárás.",
            nearbyCities = "Szentes, Csongrád, Mindszent, Szegvár"
        ),
        AirspaceZone(
            id = "NOTAM_HU_0142_26",
            countryName = "Magyarország",
            name = "A0142/26 NOTAMN – Bakony & Veszprém Katonai Hadgyakorlati Légtérzár",
            type = ZoneType.DYNAMIC_NOTAM,
            lat = 47.1667,
            lng = 17.8500,
            radiusMeters = 12000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "FL150 (4500 m)",
            maxDroneAltitude = "0 m (Élő NOTAM légtérzár)",
            activityHours = "Érvényes: 2026.08.20 06:00 UTC - 2026-08-30 20:00 UTC",
            description = "[ÉLŐ NOTAM ÁRNYÉKZÓNA] MH Bakony Harcikiképző Központ kis- és közepes magasságú légikötelék taktikai lőgyakorlat. Tilos bármilyen drón (UAS) üzemeltetés az érvényességi idő alatt!",
            permitRequired = "MH Veszprém Légivezetési és Irányítási Központ eseti feloldási engedélye.",
            contactAuthority = "HungaroControl AIS / MH Veszprém TWR",
            penalties = "Katonai légitérszegés miatti azonnali elfogás és 2 000 000 Ft büntetés.",
            nearbyCities = "Veszprém, Hajmáskér, Várpalota, Úrkút"
        ),
        AirspaceZone(
            id = "NOTAM_HU_0209_26",
            countryName = "Magyarország",
            name = "A0209/26 NOTAMN – Budapest Duna-szakasz Állami Kíséret VIP Korlátozás",
            type = ZoneType.DYNAMIC_NOTAM,
            lat = 47.5000,
            lng = 19.0400,
            radiusMeters = 3000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "3500 ft AMSL",
            maxDroneAltitude = "0 m (Ideiglenes VIP Védelmi Zóna)",
            activityHours = "Érvényes: Napi 08:00 - 22:00 UTC (Dinamikus NOTAM)",
            description = "[ÉLŐ NOTAM RESTRICITON] Diplomáciai delegáció és állami delegáció védelmében kiadott időleges korlátozott légtér a Duna belvárosi szakasza mentén.",
            permitRequired = "Készenléti Rendőrség / Terrorelhárítási Központ (TEK) felhatalmazás.",
            contactAuthority = "HungaroControl AIS / TEK Légtérbiztosítás",
            penalties = "Azonnali légvédelmi/rendőri intézkedés és szigorú büntetőfeljelentés.",
            nearbyCities = "Budapest I., V., II. kerületek (Parlament, Várkert Bazár, Lánchíd)"
        ),
        AirspaceZone(
            id = "NOTAM_HU_0318_26",
            countryName = "Magyarország",
            name = "A0318/26 NOTAMN – Balatonfüred & Tihany Légi Térképészeti Munkálatok",
            type = ZoneType.DYNAMIC_NOTAM,
            lat = 46.9167,
            lng = 17.8833,
            radiusMeters = 5000,
            lowerLimit = "GND",
            upperLimit = "1000 ft AMSL",
            maxDroneAltitude = "Kizárólag a NOTAM-ban bejelentett ipari drónnak max 150m",
            activityHours = "Érvényes: 2026.08.25 - 2026.08.28 07:00-17:00 UTC",
            description = "[ÉLŐ NOTAM LÉGIMUNKÁLAT] Hivatalos lézeres LiDAR és ortofotó térképező merevszárnyú UAS mérések. A zónában repülő egyéb hobbi drónok súlyos ütközésveszélyt jelentenek!",
            permitRequired = "KKLH Eseti Légtér Engedély & NOTAM Koordináció.",
            contactAuthority = "Légügyi Hatóság / HungaroControl NOTAM Desk",
            penalties = "Engedély nélküli berepülés esetén 500 000 Ft bírság.",
            nearbyCities = "Balatonfüred, Tihany, Zamárdi, Csopak"
        ),
        AirspaceZone(
            id = "OPEN_HUNGARY_GENERAL",
            countryName = "Magyarország",
            name = "Magyarország Nyílt Külterületi Légtér (Open A1/A2/A3)",
            type = ZoneType.OPEN_CATEGORY,
            lat = 47.1625,
            lng = 19.5033,
            radiusMeters = 35000,
            lowerLimit = "GND (Talajszint)",
            upperLimit = "120 m AGL (Talaj felett)",
            maxDroneAltitude = "120 m AGL",
            activityHours = "VLOS (Látótávolságon belül, nappal)",
            description = "Külterületen, korlátozott légtereken kívül 120 méter magasságig szabadon repülhető EASA és magyar szabályok szerint.",
            permitRequired = "MyDroneSpace regisztráció és repülés előtti aktiválás + UAS Üzemeltetői regisztráció (KAV) + Felelősségbiztosítás.",
            contactAuthority = "Közlekedési Hatóság Légügyi Hivatal / MyDroneSpace",
            penalties = "120 m feletti repülés esetén 50 000 - 300 000 Ft bírság.",
            nearbyCities = "Minden lakott területen kívüli nyílt mezőgazdasági és külterület"
        )
    )

    val guideItems: List<AirspaceGuideItem> = listOf(
        AirspaceGuideItem(
            title = "Repülőtéri Ellenőrzött Körzet (CTR)",
            shortCode = "CTR",
            icon = "✈️",
            colorHex = "#EF4444",
            subtitle = "Repülőterek 8-15 km-es közvetlen körzete a fel- és leszálló repülőgépek védelmére",
            description = "A Control Zone (CTR) a polgári vagy katonai repülőterek körül kijelölt ellenőrzött légtér a földfelszíntől (GND) egy meghatározott magasságig. Ebben a légtérben a repülőtéri irányító torony (TWR) irányítja az érkező és induló légi járműveket.",
            requirements = listOf(
                "Katonai Légügyi Hatóságtól (KLH) igényelt eseti légtér (TRA) legalább 30 nappal előre",
                "Repülőtér üzembentartójának és a HungaroControl légiforgalmi irányításának írásos hozzájárulása",
                "MyDroneSpace alkalmazásban történő eseménykezelés és repülésindítás",
                "Kétoldalú rádiókapcsolat a toronnyal (TWR) vagy előre egyeztetett telefonos készenlét"
            ),
            rulesForDrones = listOf(
                "Engedély és eseti légtér nélkül a belépés SZIGORÚAN TILOS és bűncselekmény!",
                "Futópálya tengelyek közvetlen közelében még engedéllyel is minimális a jóváhagyás esélye.",
                "Azonnali leszállási kötelezettség mentőhelikopter vagy váratlan légi forgalom észlelésekor."
            ),
            penaltyInfo = "Engedély nélküli CTR berepülés: 500 000 - 3 000 000 Ft közigazgatási bírság + büntetőeljárás légi közlekedés veszélyeztetése miatt."
        ),
        AirspaceGuideItem(
            title = "Tiltott Légtér (P-Zóna - Prohibited Area)",
            shortCode = "LH-P",
            icon = "🛑",
            colorHex = "#991B1B",
            subtitle = "Állandóan tiltott légtér a földfelszíntől korlátlan magasságig (pl. Paksi Atomerőmű)",
            description = "A Prohibited (LH-P) légtérben a polgári légi járművek és drónok repülése TELJESEN ÉS ÁLLANDÓAN TILOS az állami és nukleáris biztonság védelmében.",
            requirements = listOf(
                "Polgári drónosoknak és hobbi pilótáknak NEM adható felmentés",
                "Kizárólag kormányzati szervek és miniszteri határozattal felhatalmazott katonai/rendőri műveletek engedélyezettek"
            ),
            rulesForDrones = listOf(
                "A zóna határát megközelíteni tilos, a drónelhárító rendszerek (RF zavarók és hálókilövők) azonnal kényszerleszállítják vagy megsemmisítik a drónt.",
                "Paksi Atomerőmű (LH-P1) és Csillebérci Atomkutató (LH-P2) körzetében 24 órás fegyveres és radaros védelem üzemel."
            ),
            penaltyInfo = "Nemzetbiztonsági és rendőrségi őrizetbevétel, 5 000 000 Ft-ig terjedő büntetés, büntetőjogi feljelentés."
        ),
        AirspaceGuideItem(
            title = "Korlátozott Légtér (R-Zóna - Restricted Area)",
            shortCode = "LH-R",
            icon = "⚠️",
            colorHex = "#F59E0B",
            subtitle = "Védett állami és kormányzati épületek (Parlament, Várnegyed, Visegrádi Vár)",
            description = "A Restricted (LH-R) légterekben a repülés csak előzetesen meghatározott speciális feltételek és biztonsági felülvizsgálat teljesülése esetén lehetséges.",
            requirements = listOf(
                "Eseti légtér kijelölése a Katonai Légügyi Hatóságnál (KLH)",
                "Terrorelhárítási Központ (TEK) vagy Országgyűlési Őrség / Készenléti Rendőrség jóváhagyása",
                "Kamerás felvételek esetén a nemzetbiztonsági és személyiségi jogi garanciák betartása"
            ),
            rulesForDrones = listOf(
                "A Parlament (LH-R1) és Budavári Palotanegyed (LH-R2) felett engedély nélküli repülés azonnali rendőri intézkedést von maga után.",
                "Kordonok és védett személyek közvetlen közelében a repülés még eseti légtérben is korlátozható."
            ),
            penaltyInfo = "300 000 - 2 000 000 Ft bírság és a drón lefoglalása a helyszínen."
        ),
        AirspaceGuideItem(
            title = "Katonai Gyakorló & Veszélyes Zóna (D-Zóna / TSA)",
            shortCode = "LH-D / TSA",
            icon = "🛡️",
            colorHex = "#8B5CF6",
            subtitle = "Katonai lőterek, vadászgép gyakorló és helikopter műveleti légterek",
            description = "A Danger (LH-D) és Katonai Időszakos Légterek (TSA/TRA) aktív időszakban éleslövészetek, légiharc-gyakorlatok és katonai műveletek színhelyei (pl. Bakony lőtér, Kecskemét Gripen bázis).",
            requirements = listOf(
                "Honvédelmi Minisztérium és a kijelölt katonai alakulat parancsnokságának engedélye",
                "NOTAM (Notice to Airmen) közlemények és lőtéri lőidőpontok kötelező ellenőrzése"
            ),
            rulesForDrones = listOf(
                "Éleslövészet és lőgyakorlatok idején a légtér ÉLETVESZÉLYES!",
                "Alacsonyan száguldó vadászgépek (akár 30-50 méteren) pillanatok alatt elüthetik a drónt."
            ),
            penaltyInfo = "Katonai objektum megsértése miatt büntetőfeljelentés és több milliós bírság."
        ),
        AirspaceGuideItem(
            title = "Nemzeti Parkok & Védett Természeti Területek",
            shortCode = "NP / Natura 2000",
            icon = "🌲",
            colorHex = "#10B981",
            subtitle = "Védett élővilág, madárvonulási folyosók és ökológiai rezervátumok védelme",
            description = "A nemzeti parkok és Natura 2000 természetvédelmi területek a madarak, vadállatok és ritka élőhelyek megóvására szolgálnak. A drónok rotorzaja és ragadozómadár-szerű sziluettje pánikot kelt az állatokban.",
            requirements = listOf(
                "Az illetékes Nemzeti Park Igazgatóság (pl. HNPI, BFNP, BNPI) előzetes írásos természetvédelmi engedélye",
                "Kormányhivatali környezetvédelmi és természetvédelmi főosztály jóváhagyása filmezés esetén"
            ),
            rulesForDrones = listOf(
                "Fészkelési időszakban (március 1. - július 31.) ritka madarak fészkei közelében szigorú tilalom.",
                "Vízimadarak, darucsapatok és vadállományok közvetlen üldözése, zavarása szigorúan tilos!",
                "Természetvédelmi őr felhívására a repülést azonnal be kell fejezni."
            ),
            penaltyInfo = "200 000 - 1 500 000 Ft természetvédelmi bírság; védett madár elpusztulása esetén természetkárosítás bűntette (akár 3 év szabadságvesztés)!"
        ),
        AirspaceGuideItem(
            title = "Eseti Légtér (TRA) & Belterületi Repülés",
            shortCode = "TRA / Belterület",
            icon = "⏱️",
            colorHex = "#3B82F6",
            subtitle = "Lakott terület feletti repülés és speciális rendezvények légtérbiztosítása",
            description = "Magyarországon a hatályos légiközlekedési törvény szerint lakott terület (belterület) felett bármilyen kamerás vagy 120g feletti drónnal repülni CSAK eseti légtérben szabad!",
            requirements = listOf(
                "Kérelmet legalább 30 nappal a repülés előtt be kell nyújtani a Katonai Légügyi Hatósághoz (KLH)",
                "Illetékbélyeg és a HungaroControl biztonsági felülvizsgálati díjának befizetése (9000 Ft illeték + eljárási díj)",
                "Érvényes felelősségbiztosítás a drónra",
                "A kijelölt eseti légtér aktiválása a repülés megkezdése előtt 30 perccel a MyDroneSpace appban"
            ),
            rulesForDrones = listOf(
                "A kijelölt eseti légtér határait (horizontális és vertikális) szigorúan be kell tartani.",
                "Embertömeg (koncertek, fesztiválok, tüntetések) felett közvetlenül repülni tilos!",
                "A lakók magánszféráját (GDPR) tiszteletben kell tartani, ablakokon betekinteni tilos."
            ),
            penaltyInfo = "Belterületen eseti légtér nélküli drónozás: 100 000 - 1 000 000 Ft bírság."
        ),
        AirspaceGuideItem(
            title = "Kórházak & Mentőhelikopter Bázisok (HEMS)",
            shortCode = "HEMS",
            icon = "🏥",
            colorHex = "#EC4899",
            subtitle = "Sürgősségi kórházi leszállók és azonnali riasztású légimentő folyosók",
            description = "A Helicopter Emergency Medical Services (HEMS) zónák a sürgősségi kórházak tető- és talajszinti helikopter leszállói, valamint a légimentő bázisok körüli 2-3 km-es védett légterek. A mentőhelikopterek (pl. sárga Eurocopter EC135 / H145) életmentő riasztás esetén váratlanul, alacsonyan (akár 30-100 méteren) és nagy sebességgel érkeznek vagy szállnak fel.",
            requirements = listOf(
                "Szigorú és állandó no-drone zóna a leszállópálya közvetlen környezetében",
                "Országos Mentőszolgálat (OMSZ) és a Légimentő Bázis előzetes írásos engedélye",
                "Eseti légtér kijelölése a Katonai Légügyi Hatóságnál filmes/ipari munkákhoz"
            ),
            rulesForDrones = listOf(
                "Bármilyen helikopterhang vagy közeledő mentőhelikopter észlelésekor a drónt AZONNAL le kell szállítani a földre!",
                "Kórházak felett vagy közvetlen közelében hobbi célú repülés SZIGORÚAN TILOS!",
                "A drón által okozott legkisebb légörvény vagy optikai zavarás is a helikopter lezuhanásához vezethet."
            ),
            penaltyInfo = "Életmentés és légi közlekedés közvetlen veszélyeztetése miatt azonnali rendőri előállítás, 1 000 000 - 3 000 000 Ft bírság és börtönbüntetés!"
        ),
        AirspaceGuideItem(
            title = "Dinamikus & Élő NOTAM Korlátozások",
            shortCode = "Élő NOTAM",
            icon = "📡",
            colorHex = "#F97316",
            subtitle = "Valós idejű, időleges légtérzárak és katonai/diplomáciai NOTAM jelzések",
            description = "A NOTAM (Notice to Airmen) olyan időleges légtérkorlátozás vagy veszélyjelzés, amelyet a légiforgalmi irányítás (pl. HungaroControl NOTAM Desk, EUROCONTROL, FAA) ad ki. Ezek lehetnek 1-2 órás VIP védelmi sávok, katonai lőgyakorlatok, mentőakciók, vagy hivatalos légi térképezési munkák.",
            requirements = listOf(
                "Repülés előtt közvetlenül ellenőrizni kell az aktív NOTAM táviratokat",
                "Érvényességi időablak (UTC-ben megadva, pl. 06:00 - 18:00 UTC) szigorú betartása",
                "Az NOTAM-ban megadott vertikális és horizontális koordináták elkerülése"
            ),
            rulesForDrones = listOf(
                "Ha NOTAM lép életbe az adott zónában, a drónozást a megadott idősávban fel kell függeszteni!",
                "A NOTAM-ban szereplő felhívások felülírják a nyílt A1/A2/A3 kategóriás alapértelmezett jogokat.",
                "Eseti feloldást kizárólag az adott NOTAM kiadó hatósága (pl. Honvédelmi Minisztérium, TEK) adhat."
            ),
            penaltyInfo = "Katonai vagy állami VIP NOTAM megsértése esetén 1 000 000 - 3 000 000 Ft bírság és büntetőeljárás!"
        ),
        AirspaceGuideItem(
            title = "Nyílt Kategória (Open A1 / A2 / A3) Szabályok",
            shortCode = "Open 120m",
            icon = "🟢",
            colorHex = "#06B6D4",
            subtitle = "Külterületi szabad repülés az EASA és magyar előírások betartásával",
            description = "Külterületen, korlátozott légtereken kívül a nyílt kategóriában engedélyezett a repülés maximum 120 méter magasságig.",
            requirements = listOf(
                "UAS Üzembentartói regisztráció a Közlekedési Hatóságnál (minden kamerás vagy 250g+ drónhoz)",
                "A1/A3 vagy A2 Távpilóta Kompetencia Tanúsítvány (KAV vizsga)",
                "MyDroneSpace alkalmazás használata és aktív repülés indítása",
                "Felelősségbiztosítás megléte"
            ),
            rulesForDrones = listOf(
                "Maximális megengedett repülési magasság a talaj felett: 120 méter (AGL).",
                "Közvetlen látótávolságban (VLOS) kell tartani a drónt (FPV esetén megfigyelő személy kötelező).",
                "Nyílt kategóriában éjszakai repüléshez zöld villogó fényjelzés szükséges a drónon."
            ),
            penaltyInfo = "Regisztráció vagy MyDroneSpace nélküli repülés: 50 000 - 300 000 Ft bírság."
        )
    )

    /**
     * Generates or retrieves a comprehensive, highly realistic set of airspace restriction zones
     * for every single country in the world database.
     */
    fun getZonesForCountry(country: CountryRule): List<AirspaceZone> {
        val cName = country.name
        val baseList: List<AirspaceZone> = when {
            cName.contains("Magyarország", ignoreCase = true) -> hungaryZones
            cName.contains("Ausztria", ignoreCase = true) -> AirspaceZonesEurope.austriaZones
            cName.contains("Németország", ignoreCase = true) -> AirspaceZonesEurope.germanyZones
            cName.contains("Horvátország", ignoreCase = true) -> AirspaceZonesEurope.croatiaZones
            cName.contains("Olaszország", ignoreCase = true) -> AirspaceZonesEurope.italyZones
            cName.contains("Szlovákia", ignoreCase = true) -> AirspaceZonesEurope.slovakiaZones
            cName.contains("Románia", ignoreCase = true) -> AirspaceZonesEurope.romaniaZones
            cName.contains("Svájc", ignoreCase = true) -> AirspaceZonesWorld.switzerlandZones
            cName.contains("Egyesült Királyság", ignoreCase = true) -> AirspaceZonesWorld.ukZones
            cName.contains("Franciaország", ignoreCase = true) -> AirspaceZonesWorld.franceZones
            cName.contains("Spanyolország", ignoreCase = true) -> AirspaceZonesWorld.spainZones
            cName.contains("Amerikai Egyesült Államok", ignoreCase = true) -> AirspaceZonesWorld.usaZones
            else -> generateGenericZonesForCountry(country)
        }

        val matchingLiveNotams = LiveAviationNetworkManager.liveNotams.filter {
            it.countryName.contains(cName, ignoreCase = true) || it.countryName == "Európai Unió"
        }

        return if (matchingLiveNotams.isNotEmpty()) {
            baseList + matchingLiveNotams
        } else {
            baseList
        }
    }

    private fun generateGenericZonesForCountry(country: CountryRule): List<AirspaceZone> {
        val cName = country.name
        val result = mutableListOf<AirspaceZone>()
        val baseLat = country.lat
        val baseLng = country.lng
        val isNoFly = country.maxAltitude == "0 m"
        val cHash = Math.abs(cName.hashCode())

        // 1. Primary International Hub Airport CTR
        result.add(
            AirspaceZone(
                id = "${cHash}_CTR_HUB",
                countryName = cName,
                name = "$cName Fővárosi Nemzetközi Repülőtér (Primary CTR / ICAO Cat A)",
                type = ZoneType.CTR_AIRPORT,
                lat = baseLat + 0.085,
                lng = baseLng + 0.105,
                radiusMeters = 16000,
                lowerLimit = "GND (Talajszint)",
                upperLimit = "3500 ft AMSL",
                maxDroneAltitude = "0 m (Szigorúan tilos / TWR engedély nélkül bűncselekmény)",
                activityHours = "H-V: 00:00 - 24:00 (Folyamatos nemzetközi légiforgalom)",
                description = "Központi nemzetközi légi csomópont és futópálya megközelítési folyosók. A drónok jelenléte súlyos nemzetközi katasztrófaveszélyt jelent!",
                permitRequired = "${country.officialMapName} és a helyi Polgári Légügyi Hatóság előzetes eseti légtér jóváhagyása.",
                contactAuthority = country.officialMapName,
                penalties = "Azonnali nemzetközi hatósági eljárás, drónelkobzás és 1000 - 10 000 EUR pénzbírság.",
                nearbyCities = "$cName Fővárosi körzet & nemzetközi terminálok"
            )
        )

        // 2. Secondary Regional Airport / Cargo & Charter Hub (TIZ)
        result.add(
            AirspaceZone(
                id = "${cHash}_CTR_REGIONAL",
                countryName = cName,
                name = "$cName 2. Számú Nemzetközi Repülőtér & Cargo Terminál (TIZ)",
                type = ZoneType.CTR_AIRPORT,
                lat = baseLat - 0.22,
                lng = baseLng + 0.18,
                radiusMeters = 11000,
                lowerLimit = "GND",
                upperLimit = "2500 ft AMSL",
                maxDroneAltitude = "0 m (Üzemidőben engedélyköteles)",
                activityHours = "06:00 - 23:00 (Éjszakai teherszállító járatokkal)",
                description = "Belföldi, charter és regionális légi mentő repülőgépek ellenőrzött légtere.",
                permitRequired = "Repülőtér üzembentartói engedély és ${country.officialMapName} bejelentés.",
                contactAuthority = "$cName Regionális Légiforgalmi Irányítás",
                penalties = "Közigazgatási eljárás és 500 - 3000 EUR bírság.",
                nearbyCities = "$cName 2. legnagyobb városa és ipari agglomerációja"
            )
        )

        // 3. Sport & General Aviation Aerodrome (AFIS / Vitorlázó Körzet)
        result.add(
            AirspaceZone(
                id = "${cHash}_AIRFIELD_GA",
                countryName = cName,
                name = "$cName Sportrepülőtér & Ejtőernyős Körzet (AFIS)",
                type = ZoneType.CTR_AIRPORT,
                lat = baseLat + 0.19,
                lng = baseLng - 0.14,
                radiusMeters = 5500,
                lowerLimit = "GND",
                upperLimit = "2000 ft AMSL",
                maxDroneAltitude = "0 m (Üzemidőben és hétvégén tilos)",
                activityHours = "Napkelte - Napnyugta (intenzív hétvégi sportrepülés)",
                description = "Kisgépes oktatórepülés, vitorlázó és helikopteres kiképzés, ejtőernyős ugrások.",
                permitRequired = "Repülőtérvezetői hozzájárulás és helyi rádiófrekvenciás figyelés.",
                contactAuthority = "$cName Nemzeti Repülőklub",
                penalties = "300 - 1500 EUR bírság.",
                nearbyCities = "$cName Elővárosi sportövezet"
            )
        )

        // 4. National Government / Presidential Palace & Diplomatic Core (P/R Zone)
        result.add(
            AirspaceZone(
                id = "${cHash}_PROHIBITED_GOV",
                countryName = cName,
                name = "$cName Elnöki Palota, Parlament & Kormányzati Negyed (P/R Zóna)",
                type = if (isNoFly) ZoneType.PROHIBITED_NO_FLY else ZoneType.RESTRICTED,
                lat = baseLat,
                lng = baseLng,
                radiusMeters = 4500,
                lowerLimit = "GND",
                upperLimit = "3000 ft AMSL",
                maxDroneAltitude = if (isNoFly) "0 m (SZIGORÚAN TILOS NO-FLY)" else "0 m (Csak miniszteri különengedéllyel)",
                activityHours = "24/7/365 Állandó állami védelem",
                description = "Parlament, köztársasági elnöki rezidencia, külképviseletek, diplomáciai negyed és kiemelt nemzeti emlékhelyek feletti védett légtér.",
                permitRequired = "Belügyminisztérium, Nemzetbiztonsági Szolgálat és Elnöki Testőrség írásos felhatalmazása.",
                contactAuthority = "Nemzeti Rendőrség és Kormányőrség",
                penalties = "Azonnali fegyveres előállítás, elektronikus drónzavarás és büntetőbírósági eljárás!",
                nearbyCities = "$cName Főváros Belváros"
            )
        )

        // 5. Critical Energy, Nuclear or Maritime Infrastructure (R-Zone)
        result.add(
            AirspaceZone(
                id = "${cHash}_RESTRICTED_ENERGY",
                countryName = cName,
                name = "$cName Stratégiai Energetikai & Ipari Létesítmény (R-Zóna)",
                type = ZoneType.RESTRICTED,
                lat = baseLat + 0.18,
                lng = baseLng - 0.24,
                radiusMeters = 6500,
                lowerLimit = "GND",
                upperLimit = "UNLIMITED",
                maxDroneAltitude = "0 m (Szigorúan zárt biztonsági zóna)",
                activityHours = "Folyamatos 24 órás védelem",
                description = "Erőművek, kőolaj-finomítók, stratégiai gáztárolók és tengeri kikötői biztonsági terminálok.",
                permitRequired = "Energiahivatal és Nemzetbiztonsági Szolgálat engedélye.",
                contactAuthority = "Országos Katasztrófavédelem & Ipari Biztonsági Hatóság",
                penalties = "Súlyos terrorveszélyeztetési eljárás és azonnali drónmegsemmisítés.",
                nearbyCities = "$cName Ipari és Kikötői Régió"
            )
        )

        // 6. Military Air Base & Tactical Combat Squadron (MCTR / D-Zone)
        result.add(
            AirspaceZone(
                id = "${cHash}_MIL_AIRBASE",
                countryName = cName,
                name = "$cName Harcászati Légibázis & Vadászgép Központ (MCTR / D-Zóna)",
                type = ZoneType.MILITARY_DANGER,
                lat = baseLat - 0.15,
                lng = baseLng - 0.16,
                radiusMeters = 16000,
                lowerLimit = "GND",
                upperLimit = "FL 250 (7500 m)",
                maxDroneAltitude = "0 m (Életveszélyes katonai légtér)",
                activityHours = "24 órás légvédelmi készültség / QRA riasztások",
                description = "Vadászgép bázis, katonai helikopterek, radarállomások és alacsony magasságú szuperszonikus gyakorló folyosók.",
                permitRequired = "Katonai Légügyi Hatóság és Védelmi Minisztérium jóváhagyása.",
                contactAuthority = "Katonai Légierő Parancsnokság",
                penalties = "Katonai törvényszéki feljelentés, lefoglalás és börtönbüntetés!",
                nearbyCities = "$cName Katonai Védelmi Körzet"
            )
        )

        // 7. Military Artillery, Tank & Missile Proving Ground (TSA / Danger Area)
        result.add(
            AirspaceZone(
                id = "${cHash}_MIL_ARTILLERY",
                countryName = cName,
                name = "$cName Katonai Nehézfegyverzeti & Tüzérségi Lőtér (TSA)",
                type = ZoneType.MILITARY_DANGER,
                lat = baseLat + 0.26,
                lng = baseLng + 0.22,
                radiusMeters = 18000,
                lowerLimit = "GND",
                upperLimit = "FL 200 (6000 m)",
                maxDroneAltitude = "0 m (Éleslövészet idején szigorúan tilos!)",
                activityHours = "NOTAM és hadgyakorlati menetrend szerint",
                description = "Nehéztüzérségi, páncélozott harcjármű és rakétaéleslövészeti gyakorlótér.",
                permitRequired = "Gyakorlótér Parancsnokság írásos engedélye.",
                contactAuthority = "Fegyveres Erők Gyakorlótér Parancsnokság",
                penalties = "Azonnali katonai őrizetbevétel és súlyos bírság.",
                nearbyCities = "$cName Katonai Lőtér Körzet"
            )
        )

        // 8. Primary National Park & UNESCO Biosphere Reserve
        result.add(
            AirspaceZone(
                id = "${cHash}_NAT_PARK_1",
                countryName = cName,
                name = "$cName Kiemelt Nemzeti Park & UNESCO Bioszféra Rezervátum",
                type = ZoneType.NATIONAL_PARK,
                lat = baseLat + 0.32,
                lng = baseLng + 0.05,
                radiusMeters = 24000,
                lowerLimit = "GND",
                upperLimit = "3000 ft AMSL",
                maxDroneAltitude = "0 m (Csak természetvédelmi engedéllyel)",
                activityHours = "Egész évben védett természeti övezet",
                description = "Védett hegyvidéki vagy tengerparti ökoszisztéma, madárvonulási folyosók és ritka ragadozók fészkelőhelye.",
                permitRequired = "Nemzeti Park Igazgatóság és Környezetvédelmi Főhatóság kutatási/filmezési engedélye.",
                contactAuthority = "Nemzeti Természetvédelmi Őrszolgálat",
                penalties = "Környezetkárosítási bírság (500 - 5000 EUR), fészkelés megzavarása esetén büntetőeljárás.",
                nearbyCities = "$cName Természetvédelmi és Turisztikai Régió"
            )
        )

        // 9. Secondary Mountain / Coastal Wildlife Sanctuary
        result.add(
            AirspaceZone(
                id = "${cHash}_NAT_PARK_2",
                countryName = cName,
                name = "$cName Vadvédelmi Körzet & Hegyvidéki Rezervátum",
                type = ZoneType.NATIONAL_PARK,
                lat = baseLat - 0.34,
                lng = baseLng - 0.20,
                radiusMeters = 18000,
                lowerLimit = "GND",
                upperLimit = "2500 ft AMSL",
                maxDroneAltitude = "Természetvédelmi engedéllyel",
                activityHours = "Fészkelési időben (tavasz-nyár) szigorított",
                description = "Védett növény- és állattársulások, vízesések és kanyonok területe.",
                permitRequired = "Környezetvédelmi Hatóság engedélye.",
                contactAuthority = "Regionális Természetvédelmi Igazgatóság",
                penalties = "300 - 2000 EUR bírság.",
                nearbyCities = "$cName Hegyvidéki és Üdülő Régió"
            )
        )

        // 10. Border Security Buffer Strip / No-Fly Corridor
        result.add(
            AirspaceZone(
                id = "${cHash}_BORDER_STRIP",
                countryName = cName,
                name = "$cName Államhatári Biztonsági Zóna (5 km-es Határsáv)",
                type = ZoneType.RESTRICTED,
                lat = baseLat - 0.28,
                lng = baseLng - 0.08,
                radiusMeters = 12000,
                lowerLimit = "GND",
                upperLimit = "2000 ft AMSL",
                maxDroneAltitude = "0 m (Határőrségi engedély nélkül tilos)",
                activityHours = "24 órás határőrizeti monitoring",
                description = "Nemzetközi államhatár melletti 5 km-es biztonsági sáv az illegális határátlépések és csempészet megelőzésére.",
                permitRequired = "Országos Határrendészeti Igazgatóság engedélye.",
                contactAuthority = "Határőrség & Nemzeti Rendőrség",
                penalties = "Határsértési gyanú miatti azonnali őrizetbe vétel.",
                nearbyCities = "$cName Határmenti települések"
            )
        )

        // 11. Historic Old Town Fortress & Cultural Heritage Core
        result.add(
            AirspaceZone(
                id = "${cHash}_HISTORIC_CORE",
                countryName = cName,
                name = "$cName Történelmi Óváros, Vár & Műemléki Negyed (R-Zóna)",
                type = ZoneType.RESTRICTED,
                lat = baseLat - 0.04,
                lng = baseLng - 0.06,
                radiusMeters = 3500,
                lowerLimit = "GND",
                upperLimit = "1500 ft AMSL",
                maxDroneAltitude = "0 m (Önkormányzati & Műemlékvédelmi engedély nélkül tilos)",
                activityHours = "Nappal és turisztikai csúcsidőben",
                description = "Középkori belváros, műemlékek, zsúfolt sétálóutcák és kulturális emlékhelyek.",
                permitRequired = "Helyi Önkormányzat és Kulturális Örökségvédelmi Hivatal engedélye.",
                contactAuthority = "Városi Polgármesteri Hivatal / Helyi Rendőrség",
                penalties = "Műemlékvédelmi bírság és drónlefoglalás.",
                nearbyCities = "$cName Óvárosi Mag"
            )
        )

        // 12. Central University Hospital & Trauma Center Heliport Corridor (HEMS)
        result.add(
            AirspaceZone(
                id = "${cHash}_HEMS_CENTRAL_HOSPITAL",
                countryName = cName,
                name = "$cName Fővárosi Egyetemi Kórház & Sürgősségi Traumatológia HEMS",
                type = ZoneType.HOSPITAL_HEMS,
                lat = baseLat + 0.05,
                lng = baseLng - 0.09,
                radiusMeters = 3000,
                lowerLimit = "GND",
                upperLimit = "1500 ft AMSL",
                maxDroneAltitude = "0 m (Életmentő helikopterek miatt szigorúan tilos!)",
                activityHours = "24/7/365 Folyamatos sürgősségi légimentés",
                description = "Központi politraumatológiai, stroke és gyermeksürgősségi kórház helikopter leszállója (HEMS). A fel- és leszálló mentőhelikopterek közvetlen életveszélynek vannak kitéve a drónok miatt.",
                permitRequired = "Országos Mentőszolgálat és Légimentő Parancsnokság engedélye.",
                contactAuthority = "Országos Légimentő Bázis / Központi Kórház",
                penalties = "Életveszély okozása és légiközlekedés veszélyeztetése miatt azonnali büntetőeljárás!",
                nearbyCities = "$cName Kórházi & Egyetemi Negyed"
            )
        )

        // 13. Regional Emergency Rescue & Mountain Air Ambulance Base (HEMS Base)
        result.add(
            AirspaceZone(
                id = "${cHash}_HEMS_REGIONAL_BASE",
                countryName = cName,
                name = "$cName Regionális Légimentő Bázis & Katasztrófavédelmi Helikopter Leszálló",
                type = ZoneType.HOSPITAL_HEMS,
                lat = baseLat - 0.12,
                lng = baseLng + 0.14,
                radiusMeters = 3500,
                lowerLimit = "GND",
                upperLimit = "2000 ft AMSL",
                maxDroneAltitude = "0 m (Mentőhelikopter riasztási sáv)",
                activityHours = "Napkelte - Napnyugta (3 perces azonnali riasztási idővel)",
                description = "Regionális légimentő helikopter állomás. Riasztás esetén a mentőhelikopter azonnal, alacsonyan startol.",
                permitRequired = "Regionális Légimentő Parancsnokság engedélye.",
                contactAuthority = "Nemzeti Légimentő Szolgálat",
                penalties = "1 000 000 Ft feletti közigazgatási bírság és feljelentés.",
                nearbyCities = "$cName Regionális Mentőbázis Körzet"
            )
        )

        // 14. Open Category Flight Zone (if country is not completely prohibited)
        if (!isNoFly) {
            result.add(
                AirspaceZone(
                    id = "${cHash}_OPEN_SAFE",
                    countryName = cName,
                    name = "$cName Nyílt Kategória Repülési Zóna (Max ${country.maxAltitude})",
                    type = ZoneType.OPEN_CATEGORY,
                    lat = baseLat - 0.06,
                    lng = baseLng + 0.22,
                    radiusMeters = 42000,
                    lowerLimit = "GND",
                    upperLimit = country.maxAltitude,
                    maxDroneAltitude = country.maxAltitude,
                    activityHours = "VLOS (Közvetlen látótávolságban, nappal)",
                    description = "A fenti korlátozott légtereken, repülőtereken és lakott belterületeken kívüli szabad repülési zóna a helyi szabályzat betartásával.",
                    permitRequired = if (country.registrationRequired) "Kötelező távpilóta regisztráció: ${country.registrationDetail}" else "Szabad repülés alapszabályok betartásával.",
                    contactAuthority = country.officialMapName,
                    penalties = "Magassági és biztonsági túllépés esetén helyi pénzbírság.",
                    nearbyCities = "$cName Külterületek & Nyílt Vidék"
                )
            )
        }

        return result
    }
}
