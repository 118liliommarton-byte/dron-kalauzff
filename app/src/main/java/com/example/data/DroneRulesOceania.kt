package com.example.data

object DroneRulesOceania {
    val countries = listOf(
        CountryRule(
            name = "Ausztrália",
            flag = "🇦🇺",
            maxAltitude = "120 m",
            registrationRequired = false,
            registrationDetail = "250g alatt hobbi célra nem kötelező, de az OpenSky alkalmazás használata erősen ajánlott.",
            summary = "Szigorú szabályok a tengerpartokon a helikopteres cápafigyelők és a Sydney Operaház körzetében.",
            detailedRules = listOf(
                "Hobbi célú repülés esetén 250g alatti drónhoz nem kell CASA regisztráció.",
                "A maximális megengedett magasság 120 méter (400 láb).",
                "A Sydney Harbour Bridge és a Sydney Operaház felett állandó repülési tilalom van.",
                "Strandokon legalább 30 méter távolságot kell tartani az emberektől.",
                "Nemzeti parkokban (pl. Kakadu, Uluru-Kata Tjuta) az őslakosok szent helyei miatt tilos a repülés."
            ),
            lat = -25.2744,
            lng = 133.7751,
            mapZoom = 4,
            officialMapName = "CASA OpenSky Australia",
            officialMapUrl = "https://www.casa.gov.au/drones",
            restrictedZonesSummary = "Sydney Kingsford Smith (SYD), Melbourne (MEL), Brisbane (BNE), Sydney Kikötő, Uluru."
        ),
        CountryRule(
            name = "Új-Zéland",
            flag = "🇳🇿",
            maxAltitude = "120 m",
            registrationRequired = false,
            registrationDetail = "Airshare regisztráció és a földtulajdonos hozzájárulása minden felszálláshoz kötelező!",
            summary = "Minden felszállás előtt kötelező a földtulajdonos (pl. DOC természetvédelmi hivatal) engedélyét beszerezni.",
            detailedRules = listOf(
                "Új-Zélandon a CAA Rule Part 101 szerint minden felszálláshoz kell a terület tulajdonosának engedélye.",
                "A DOC (Department of Conservation) területein (pl. Milford Sound, Mount Cook) tilos repülni előzetes DOC engedély nélkül.",
                "A maximális magasság 120 méter (400 láb).",
                "Minden repülést javasolt bejelenteni az Airshare.co.nz hivatalos térképen.",
                "Repülőterektől és helikopter-leszállóktól 4 km-es védőtávolságot kell tartani."
            ),
            lat = -40.9006,
            lng = 174.886,
            mapZoom = 5,
            officialMapName = "Airshare New Zealand (CAA)",
            officialMapUrl = "https://www.airshare.co.nz",
            restrictedZonesSummary = "Auckland (AKL), Christchurch (CHC), Queenstown (ZQN), Milford Sound, Aoraki Mount Cook."
        ),
        CountryRule(
            name = "Fidzsi-szigetek",
            flag = "🇫🇯",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "CAAF (Civil Aviation Authority of Fiji) regisztráció.",
            summary = "Mamanuca és Yasawa szigeteinek lagúnái csodálatosak, de a szállodák és hidroplánok miatt engedély szükséges.",
            detailedRules = listOf(
                "Minden külföldi drónt kötelező regisztrálni a CAAF hivatalos felületén a beutazás előtt.",
                "Nadi Nemzetközi Repülőtér (NAN) és Nausori (SUV) 5 km-es körzetében tilos repülni.",
                "A privát szigeti üdülőhelyek felett a szállodaigazgatóság írásos beleegyezése szükséges.",
                "A maximális megengedett repülési magasság 120 méter.",
                "A hidroplánok és hajók biztonsági sávjait szigorúan be kell tartani."
            ),
            lat = -17.7134,
            lng = 178.065,
            mapZoom = 7,
            officialMapName = "CAAF Fiji Drone Registration",
            officialMapUrl = "https://www.caaf.org.fj",
            restrictedZonesSummary = "Nadi Nemzetközi Repülőtér (NAN) CTR, Suva Nausori (SUV), Port Denarau kikötő."
        ),
        CountryRule(
            name = "Pápua Új-Guinea",
            flag = "🇵🇬",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "CASA PNG regisztráció.",
            summary = "A Kokoda-ösvény hegyei, a Rabaul aktív Tavurvur vulkánja és a törzsi területek.",
            detailedRules = listOf(
                "A drónokat regisztrálni kell a CASA PNG felületén a felszállás előtt.",
                "Port Moresby Jacksons repülőtér (POM) 5 km-es körzetében tilos a repülés.",
                "A helyi törzsi közösségek falvai felett a törzsi vezetők engedélye szükséges a konfliktusok elkerülésére.",
                "A megengedett legnagyobb magasság 120 méter.",
                "A bányászati telepek (Ok Tedi, Porgera) felett szigorúan tilos repülni."
            ),
            lat = -6.3149,
            lng = 143.9555,
            mapZoom = 6,
            officialMapName = "CASA Papua New Guinea",
            officialMapUrl = "https://www.casapng.gov.pg",
            restrictedZonesSummary = "Port Moresby Jacksons (POM) CTR, Rabaul Tavurvur vulkán, bányászati övezetek."
        ),
        CountryRule(
            name = "Vanuatu",
            flag = "🇻🇺",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "CAAV (Civil Aviation Authority of Vanuatu) regisztráció.",
            summary = "A Yasur aktív vulkán (Tanna-sziget) és Espiritu Santo Kék Lyukai (Blue Holes).",
            detailedRules = listOf(
                "Minden drónt be kell jelenteni a CAAV hatóságnál a beutazás előtt.",
                "Port Vila Bauerfield repülőtér (VLI) és Santo Pekoa (SON) 4 km-es körzetében tilos repülni.",
                "A Mount Yasur vulkánnál a felcsapó lávadarabok és gázkitörések közvetlen veszélyt jelentenek a drónra.",
                "A megengedett maximális repülési magasság 120 méter.",
                "Tilos a szent helyek (tabu helyek) felett a falusi közösség engedélye nélkül repülni."
            ),
            lat = -15.3767,
            lng = 166.9592,
            mapZoom = 7,
            officialMapName = "Civil Aviation Authority Vanuatu",
            officialMapUrl = "https://caav.gov.vu",
            restrictedZonesSummary = "Port Vila Bauerfield (VLI) CTR, Santo Pekoa (SON), Mount Yasur vulkáni övezet."
        ),
        CountryRule(
            name = "Szamoa",
            flag = "🇼🇸",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "Samoa Airport Authority / MWTI engedély.",
            summary = "To Sua Ocean Trench óriási sziklaürege és Lalomanu tengerpartja.",
            detailedRules = listOf(
                "Minden drónhoz a Közlekedési Minisztérium (MWTI) engedélye és bejelentése kötelező.",
                "Apia Faleolo Nemzetközi Repülőtér (APW) és Fagali'i (FGI) 5 km-es körzetében tilos repülni.",
                "A To Sua Ocean Trench-nél a fürdőzők magánszféráját tiszteletben kell tartani.",
                "A maximális megengedett magasság 120 méter.",
                "Vasárnaponként a templomi istentiszteletek idején tilos a falvak felett repülni."
            ),
            lat = -13.759,
            lng = -172.1046,
            mapZoom = 9,
            officialMapName = "MWTI Samoa Civil Aviation",
            officialMapUrl = "https://www.mwti.gov.ws",
            restrictedZonesSummary = "Faleolo Nemzetközi Repülőtér (APW), Apia Fagali'i, To Sua Ocean Trench."
        ),
        CountryRule(
            name = "Tonga",
            flag = "🇹🇴",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "CAD Tonga (Civil Aviation Division) regisztráció.",
            summary = "Hosszúszárnyú bálnákkal való úszás Vava'u-n és Nuku'alofa királyi palotája.",
            detailedRules = listOf(
                "Minden drónt regisztrálni kell a Tongai Polgári Légügyi Hatóságnál.",
                "A bálnák védelmében szigorúan tilos 100 méternél közelebb repülni a vándorló bálnákhoz.",
                "Fuaʻamotu Nemzetközi Repülőtér (TBU) és Vava'u (VAV) 5 km-es körzetében tilos a repülés.",
                "A Királyi Palota és a királyi rezidenciák felett szigorú repülési tilalom van.",
                "A megengedett maximális repülési magasság 120 méter."
            ),
            lat = -21.1789,
            lng = -175.1982,
            mapZoom = 8,
            officialMapName = "Civil Aviation Division Tonga",
            officialMapUrl = "https://www.infrastructure.gov.to",
            restrictedZonesSummary = "Fuaʻamotu Nemzetközi Repülőtér (TBU), Királyi Palota Nuku'alofa, bálnavédelmi zónák."
        ),
        CountryRule(
            name = "Salamon-szigetek",
            flag = "🇸🇧",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "CAASI (Civil Aviation Authority of Solomon Islands) engedély.",
            summary = "A Marovo-lagúna, Guadalcanal II. világháborús roncsai és Kennedy-sziget.",
            detailedRules = listOf(
                "A drónokat az érkezés előtt regisztrálni kell a CAASI rendszerében.",
                "Honiara Henderson Nemzetközi Repülőtér (HIR) 5 km-es körzetében tilos a repülés.",
                "A Marovo-lagúna UNESCO jelölt területén a tengeri élővilág védelme érvényesül.",
                "A megengedett legnagyobb magasság 120 méter.",
                "Tilos helyi törzsi szentélyek (tamboo helyek) felett engedély nélkül repülni."
            ),
            lat = -9.6457,
            lng = 160.1562,
            mapZoom = 7,
            officialMapName = "CAASI Solomon Islands",
            officialMapUrl = "http://www.caasi.gov.sb",
            restrictedZonesSummary = "Honiara Henderson (HIR) CTR, Iron Bottom Sound vizei, Marovo-lagúna."
        ),
        CountryRule(
            name = "Mikronézia",
            flag = "🇫🇲",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "DTC&I Civil Aviation engedély.",
            summary = "Nan Madol ősi bazaltváros romjai és Chuuk-lagúna elsüllyedt hajóroncsai.",
            detailedRules = listOf(
                "Minden drónhoz a Közlekedési és Hírközlési Minisztérium engedélye szükséges.",
                "Nan Madol UNESCO régészeti terület felett a régészeti előírások érvényesek.",
                "Pohnpei (PNI), Chuuk (TKK) és Yap (YAP) repülőterek 4 km-es körzetében tilos repülni.",
                "A megengedett maximális magasság 120 méter.",
                "A roncsmerülő búvárok felett biztonsági távolságot kell tartani."
            ),
            lat = 7.4256,
            lng = 150.5508,
            mapZoom = 6,
            officialMapName = "Civil Aviation Micronesia",
            officialMapUrl = "https://tci.gov.fm",
            restrictedZonesSummary = "Pohnpei (PNI), Chuuk (TKK), Yap (YAP), Nan Madol régészeti romok."
        ),
        CountryRule(
            name = "Marshall-szigetek",
            flag = "🇲🇭",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "DCA Marshall Islands és Kwajalein amerikai bázis egyeztetés.",
            summary = "Majuro és Bikini-atoll nukleáris teszthelyszíne, Kwajalein rakétabázis zárt légterével.",
            detailedRules = listOf(
                "A Kwajalein-atoll (Ronald Reagan Rakétavédelmi Bázis - USA) felett szigorúan tilos bármilyen repülés.",
                "Majuro Amata Kabua nemzetközi repülőtér (MAJ) 5 km-es körzetében tilos repülni.",
                "A Bikini- és Enewetak-atollok felett kutatási engedély kötelező.",
                "A megengedett maximális repülési magasság 120 méter.",
                "Minden drónt regisztrálni kell a Polgári Légügyi Hivatalnál."
            ),
            lat = 7.1315,
            lng = 171.1845,
            mapZoom = 8,
            officialMapName = "Marshall Islands DCA",
            officialMapUrl = "https://www.infomarshallislands.com",
            restrictedZonesSummary = "Kwajalein amerikai rakétateszt bázis, Majuro Amata Kabua (MAJ), Bikini-atoll."
        ),
        CountryRule(
            name = "Palau",
            flag = "🇵🇼",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "Palau Bureau of Aviation regisztráció és Koror engedély.",
            summary = "A Szikla-szigetek (Rock Islands Southern Lagoon) és a Medúza-tó (Jellyfish Lake).",
            detailedRules = listOf(
                "Minden látogatónak regisztrálnia kell a drónt a Palaui Légügyi Hivatalnál.",
                "A Rock Islands déli lagúnájában és a Medúza-tónál a helyi Koror állami környezetvédelmi engedély szükséges.",
                "Roman Tmetuchl Nemzetközi Repülőtér (ROR) 5 km-es körzetében tilos repülni.",
                "A megengedett maximális magasság 120 méter.",
                "A cápamenekélyként szolgáló vizekben az állatok közvetlen zaklatása büntetendő."
            ),
            lat = 7.515,
            lng = 134.5825,
            mapZoom = 9,
            officialMapName = "Bureau of Aviation Palau",
            officialMapUrl = "https://www.palaugov.pw",
            restrictedZonesSummary = "Roman Tmetuchl repülőtér (ROR) CTR, Rock Islands Southern Lagoon, Jellyfish Lake."
        ),
        CountryRule(
            name = "Kiribati",
            flag = "🇰🇮",
            maxAltitude = "120 m",
            registrationRequired = true,
            registrationDetail = "Civil Aviation Kiribati engedély.",
            summary = "Kiritimati (Karácsony-sziget) és a Phoenix-szigetek Védett Terület (PIPA).",
            detailedRules = listOf(
                "A drónhasználathoz a Polgári Légügyi Hivatal előzetes jóváhagyása szükséges.",
                "Bonriki Nemzetközi Repülőtér (TRW) és Cassidy repülőtér (CXI) körzetében tilos repülni.",
                "A Phoenix-szigetek védett tengeri rezervátumában a madárkolóniák zavarása tilos.",
                "A megengedett legnagyobb magasság 120 méter.",
                "A végtelen atollok felett a tájékozódásra és akkumulátor-időre fokozottan ügyelni kell."
            ),
            lat = -3.3704,
            lng = -168.734,
            mapZoom = 5,
            officialMapName = "Civil Aviation Kiribati",
            officialMapUrl = "http://www.micttd.gov.ki",
            restrictedZonesSummary = "Tarawa Bonriki (TRW), Kiritimati Cassidy (CXI), Phoenix Islands Marine Reserve."
        ),
        CountryRule(
            name = "Nauru",
            flag = "🇳🇷",
            maxAltitude = "100 m",
            registrationRequired = true,
            registrationDetail = "NCAI (Nauru Civil Aviation Authority) engedély.",
            summary = "A világ legkisebb független köztársasága: foszfátbányászat és körgyűrűs tengerpart.",
            detailedRules = listOf(
                "Minden drónhasználathoz a Naurui Polgári Légügyi Hatóság engedélye kötelező.",
                "Nauru Nemzetközi Repülőtér (INU) a kis sziget mérete miatt a terület nagy részét érinti, ezért koordináció szükséges.",
                "A belső foszfátbányászati meddőhányók felett a repülés szabályokhoz kötött.",
                "A megengedett maximális magasság 100 méter.",
                "Tilos a kormányzati elnöki hivatal és parlament felett repülni."
            ),
            lat = -0.5228,
            lng = 166.9315,
            mapZoom = 12,
            officialMapName = "NCAI Nauru Civil Aviation",
            officialMapUrl = "http://naurugov.nr",
            restrictedZonesSummary = "Nauru Nemzetközi Repülőtér (INU) CTR, kormányzati negyed, foszfátbányák."
        ),
        CountryRule(
            name = "Tuvalu",
            flag = "🇹🇻",
            maxAltitude = "100 m",
            registrationRequired = true,
            registrationDetail = "Department of Civil Aviation Tuvalu engedély.",
            summary = "Funafuti keskeny atollja és a Funafuti Természetvédelmi Terület csodás lagúnája.",
            detailedRules = listOf(
                "Minden drónt be kell jelenteni a Tuvalui Polgári Légügyi Hivatalnál.",
                "Funafuti Nemzetközi Repülőtér (FUN) kifutópályája és körzete érkezéskor/induláskor zárt.",
                "A Funafuti Marine Conservation Area tengeri teknősei és koralljai védettek.",
                "A megengedett maximális repülési magasság 100 méter.",
                "A lakóházak felett tiszteletben kell tartani a helyi családok magánéletét."
            ),
            lat = -7.1095,
            lng = 177.6493,
            mapZoom = 10,
            officialMapName = "Tuvalu Civil Aviation",
            officialMapUrl = "https://www.gov.tv",
            restrictedZonesSummary = "Funafuti Nemzetközi Repülőtér (FUN), Funafuti Conservation Area lagúna."
        )
    )
}
