import SwiftUI
import Foundation
import WebKit
import UserNotifications

// MARK: - iOS Themes
enum iOSAppTheme: String, CaseIterable, Identifiable {
    case cyberDark = "cyber_dark"
    case lightSky = "light_sky"
    case oledBlack = "oled_black"
    case balatonNavy = "balaton_navy"
    case sunsetAmber = "sunset_amber"
    
    var id: String { rawValue }
    
    var displayName: String {
        switch self {
        case .cyberDark: return "Kiber Sötét (HUD)"
        case .lightSky: return "Tiszta Égbolt"
        case .oledBlack: return "OLED Éjfél"
        case .balatonNavy: return "Balatoni Tengerkék"
        case .sunsetAmber: return "Arany Naplemente"
        }
    }
    
    var iconEmoji: String {
        switch self {
        case .cyberDark: return "⚡"
        case .lightSky: return "☀️"
        case .oledBlack: return "🖤"
        case .balatonNavy: return "🌊"
        case .sunsetAmber: return "🌅"
        }
    }
    
    var primaryColor: Color {
        switch self {
        case .cyberDark: return Color(red: 0.0, green: 0.94, blue: 1.0)
        case .lightSky: return Color(red: 0.01, green: 0.52, blue: 0.78)
        case .oledBlack: return Color(red: 0.06, green: 0.73, blue: 0.51)
        case .balatonNavy: return Color(red: 0.22, green: 0.74, blue: 0.97)
        case .sunsetAmber: return Color(red: 0.96, green: 0.62, blue: 0.04)
        }
    }
    
    var backgroundColor: Color {
        switch self {
        case .cyberDark: return Color(red: 0.03, green: 0.04, blue: 0.08)
        case .lightSky: return Color(red: 0.94, green: 0.98, blue: 1.0)
        case .oledBlack: return Color.black
        case .balatonNavy: return Color(red: 0.02, green: 0.05, blue: 0.10)
        case .sunsetAmber: return Color(red: 0.08, green: 0.04, blue: 0.11)
        }
    }
    
    var surfaceColor: Color {
        switch self {
        case .cyberDark: return Color(red: 0.06, green: 0.09, blue: 0.16)
        case .lightSky: return Color.white
        case .oledBlack: return Color(red: 0.05, green: 0.06, blue: 0.07)
        case .balatonNavy: return Color(red: 0.05, green: 0.13, blue: 0.22)
        case .sunsetAmber: return Color(red: 0.14, green: 0.07, blue: 0.18)
        }
    }
    
    var cardBorderColor: Color {
        switch self {
        case .cyberDark: return Color(red: 0.0, green: 0.94, blue: 1.0).opacity(0.35)
        case .lightSky: return Color(red: 0.73, green: 0.90, blue: 0.99)
        case .oledBlack: return Color(red: 0.06, green: 0.73, blue: 0.51).opacity(0.4)
        case .balatonNavy: return Color(red: 0.22, green: 0.74, blue: 0.97).opacity(0.4)
        case .sunsetAmber: return Color(red: 0.96, green: 0.62, blue: 0.04).opacity(0.4)
        }
    }
    
    var isDark: Bool {
        return self != .lightSky
    }
    
    var cornerRadius: CGFloat {
        switch self {
        case .cyberDark: return 8
        case .lightSky: return 24
        case .oledBlack: return 3
        case .balatonNavy: return 18
        case .sunsetAmber: return 14
        }
    }
}

// MARK: - Models
struct SpotterLocation: Identifiable {
    let id = UUID()
    let name: String
    let region: String
    let category: String
    let description: String
    let coordinates: String
    let maxAltitude: String
    let rules: String
    let imageEmoji: String
}

struct DroneMarketItem: Identifiable {
    let id = UUID()
    let title: String
    let category: String
    let priceHuf: Int
    let location: String
    let condition: String
    let sellerName: String
    let contactPhone: String
    let description: String
}

struct ExamQuestion: Identifiable {
    let id = UUID()
    let question: String
    let options: [String]
    let correctIndex: Int
    let explanation: String
}

struct ChatMessage: Identifiable {
    let id = UUID()
    let sender: String
    let text: String
    let timestamp: String
    let isPilotPro: Bool
    let category: String
}

struct DroneVehicle: Identifiable {
    let id = UUID()
    let modelName: String
    let serialNumber: String
    let weightGrams: Int
    let batteryCycles: Int
    let lastFlightDate: String
}

// MARK: - WebView Component
struct WebView: UIViewRepresentable {
    let urlString: String
    
    func makeUIView(context: Context) -> WKWebView {
        let prefs = WKWebpagePreferences()
        prefs.allowsContentJavaScript = true
        let config = WKWebViewConfiguration()
        config.defaultWebpagePreferences = prefs
        let webView = WKWebView(frame: .zero, configuration: config)
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        if let url = URL(string: urlString) {
            let request = URLRequest(url: url)
            uiView.load(request)
        }
    }
}

// MARK: - Main ContentView
struct ContentView: View {
    @State private var selectedTab = 0
    @State private var activeTheme: iOSAppTheme = .cyberDark
    @State private var showThemePicker = false
    
    var body: some View {
        ZStack {
            activeTheme.backgroundColor.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top App Bar
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        HStack(spacing: 6) {
                            Text("🛸")
                                .font(.title3)
                            Text("DRÓN KALAUZ")
                                .font(.system(size: 18, weight: .black, design: .monospaced))
                                .foregroundColor(activeTheme.primaryColor)
                        }
                        Text("Magyar Drónpilóták Hivatalos Segédlete (iOS)")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(activeTheme.isDark ? Color.gray : Color.black.opacity(0.6))
                    }
                    
                    Spacer()
                    
                    Button(action: { showThemePicker = true }) {
                        HStack(spacing: 4) {
                            Text(activeTheme.iconEmoji)
                            Text("Téma")
                                .font(.caption.bold())
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(activeTheme.surfaceColor)
                        .cornerRadius(activeTheme.cornerRadius)
                        .overlay(
                            RoundedRectangle(cornerRadius: activeTheme.cornerRadius)
                                .stroke(activeTheme.cardBorderColor, lineWidth: 1)
                        )
                        .foregroundColor(activeTheme.primaryColor)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(activeTheme.surfaceColor)
                .overlay(
                    Rectangle()
                        .frame(height: 1)
                        .foregroundColor(activeTheme.cardBorderColor),
                    alignment: .bottom
                )
                
                // Tab Content Switcher
                TabView(selection: $selectedTab) {
                    WeatherView(theme: activeTheme)
                        .tag(0)
                    
                    RegulationsView(theme: activeTheme)
                        .tag(1)
                    
                    SpotterView(theme: activeTheme)
                        .tag(2)
                    
                    MarketplaceView(theme: activeTheme)
                        .tag(3)
                    
                    ExamAndInsuranceView(theme: activeTheme)
                        .tag(4)
                    
                    PilotProfileView(theme: activeTheme)
                        .tag(5)
                    
                    ChatView(theme: activeTheme)
                        .tag(6)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                
                // Custom Tab Bar Navigation
                CustomTabBar(selectedTab: $selectedTab, theme: activeTheme)
            }
        }
        .sheet(isPresented: $showThemePicker) {
            ThemePickerSheet(activeTheme: $activeTheme, isPresented: $showThemePicker)
        }
    }
}

// MARK: - Custom Tab Bar Navigation
struct CustomTabBar: View {
    @Binding var selectedTab: Int
    let theme: iOSAppTheme
    
    let tabs: [(icon: String, title: String)] = [
        ("cloud.sun.fill", "Időkép"),
        ("doc.text.fill", "Szabályzat"),
        ("map.fill", "Spotter"),
        ("cart.fill", "Piac"),
        ("checkmark.seal.fill", "Vizsga"),
        ("person.crop.square.fill", "Profil"),
        ("bubble.left.and.bubble.right.fill", "Chat")
    ]
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(0..<tabs.count, id: \.self) { index in
                    Button(action: { selectedTab = index }) {
                        VStack(spacing: 3) {
                            Image(systemName: tabs[index].icon)
                                .font(.system(size: 15))
                            Text(tabs[index].title)
                                .font(.system(size: 10, weight: selectedTab == index ? .bold : .medium))
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .foregroundColor(selectedTab == index ? (theme.isDark ? .black : .white) : theme.primaryColor)
                        .background(
                            selectedTab == index ? theme.primaryColor : theme.surfaceColor
                        )
                        .cornerRadius(theme.cornerRadius)
                        .overlay(
                            RoundedRectangle(cornerRadius: theme.cornerRadius)
                                .stroke(theme.cardBorderColor, lineWidth: 1)
                        )
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
        }
        .background(theme.surfaceColor)
        .overlay(
            Rectangle()
                .frame(height: 1)
                .foregroundColor(theme.cardBorderColor),
            alignment: .top
        )
    }
}

// MARK: - 1. Weather & Flight Conditions View
struct WeatherView: View {
    let theme: iOSAppTheme
    @State private var selectedCity = "Budapest"
    @State private var showWindyMap = false
    @State private var notificationScheduled = false
    
    let cities = ["Budapest", "Balatonvilágos", "Debrecen", "Szeged", "Győr", "Pécs", "Miskolc"]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Location Selector Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Text("📍 VÁLASZTOTT HELYSZÍN")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        Spacer()
                        Picker("Helyszín", selection: $selectedCity) {
                            ForEach(cities, id: \.self) { city in
                                Text(city).tag(city)
                            }
                        }
                        .pickerStyle(.menu)
                        .accentColor(theme.primaryColor)
                    }
                    
                    // Flight Condition Status
                    HStack(spacing: 12) {
                        Text("🟢")
                            .font(.title)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("REPÜLÉSRE ALKALMAS ILLETMÉNY")
                                .font(.system(size: 12, weight: .black, design: .monospaced))
                                .foregroundColor(.green)
                            Text("Szél: 12 km/h • Látástáv: >10 km • KP Index: 2 (Nyugodt)")
                                .font(.system(size: 11))
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
                        }
                    }
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.green.opacity(0.12))
                    .cornerRadius(theme.cornerRadius)
                    .overlay(
                        RoundedRectangle(cornerRadius: theme.cornerRadius)
                            .stroke(Color.green.opacity(0.4), lineWidth: 1)
                    )
                }
                .padding(16)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Metrics Grid
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    MetricCard(title: "SZÉLSEBESSÉG", value: "12 km/h", subtitle: "Lökés: 18 km/h", icon: "wind", color: .cyan, theme: theme)
                    MetricCard(title: "HŐMÉRSÉKLET", value: "22°C", subtitle: "UV: 4 (Mérsékelt)", icon: "sun.max.fill", color: .orange, theme: theme)
                    MetricCard(title: "GEOMAGNETIKUS", value: "KP 2", subtitle: "GPS: 18 Műhold (Kiváló)", icon: "location.north.circle.fill", color: .purple, theme: theme)
                    MetricCard(title: "FELHŐALAP", value: "1200m", subtitle: "Köd kockázat: Alacsony", icon: "cloud.fill", color: .blue, theme: theme)
                }
                
                // Golden Hour & Twilight Alarm Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("🌅 ARANYÓRA & SZÜRKÜLET RIASZTÁS")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        Spacer()
                    }
                    
                    HStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Polgári szürkület vége:")
                                .font(.caption)
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            Text("19:42")
                                .font(.system(size: 22, weight: .black, design: .monospaced))
                                .foregroundColor(theme.primaryColor)
                        }
                        
                        Divider().frame(height: 35)
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Hátralévő vizuális idő:")
                                .font(.caption)
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            Text("02ó 14p 30sp")
                                .font(.system(size: 18, weight: .bold, design: .monospaced))
                                .foregroundColor(.orange)
                        }
                    }
                    
                    Button(action: {
                        GoldenHourNotificationHandler.scheduleSunsetWarningNotification(sunsetTimeString: "19:42")
                        notificationScheduled = true
                    }) {
                        HStack {
                            Image(systemName: notificationScheduled ? "bell.badge.fill" : "bell.fill")
                            Text(notificationScheduled ? "Riasztás Beállítva (19:27-kor)" : "Riasztás Kérése 15 Perccei Szürkület Előtt")
                                .font(.system(size: 12, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(notificationScheduled ? Color.green : theme.primaryColor)
                        .foregroundColor(theme.isDark ? .black : .white)
                        .cornerRadius(theme.cornerRadius)
                    }
                }
                .padding(16)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Embedded Interactive Windy Radar
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Text("🗺️ ÉLŐ SZÉLTÉRKÉP & RADAR (WINDY)")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        Spacer()
                        Button(action: { showWindyMap.toggle() }) {
                            Text(showWindyMap ? "Bezárás" : "Interaktív Megnyitása")
                                .font(.caption2.bold())
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(theme.primaryColor.opacity(0.2))
                                .cornerRadius(6)
                                .foregroundColor(theme.primaryColor)
                        }
                    }
                    
                    if showWindyMap {
                        WebView(urlString: "https://embed.windy.com/embed2.html?lat=47.1625&lon=19.5033&detailLat=47.4979&detailLon=19.0402&width=650&height=450&zoom=7&level=surface&overlay=wind&product=ecmwf&metricWind=km%2Fh&metricTemp=%C2%B0C")
                            .frame(height: 320)
                            .cornerRadius(theme.cornerRadius)
                    } else {
                        HStack {
                            Image(systemName: "wind")
                                .font(.largeTitle)
                                .foregroundColor(theme.primaryColor)
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Dinamikus Globális ECMWF Szélmodell")
                                    .font(.subheadline.bold())
                                Text("Kattints a fenti gombra az élő interaktív széltérkép betöltéséhez.")
                                    .font(.caption)
                                    .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            }
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(theme.backgroundColor.opacity(0.5))
                        .cornerRadius(theme.cornerRadius)
                    }
                }
                .padding(16)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
            }
            .padding(16)
        }
    }
}

struct MetricCard: View {
    let title: String
    let value: String
    let subtitle: String
    let icon: String
    let color: Color
    let theme: iOSAppTheme
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                Spacer()
                Text(title)
                    .font(.system(size: 9, weight: .bold, design: .monospaced))
                    .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
            }
            Text(value)
                .font(.system(size: 20, weight: .black, design: .monospaced))
                .foregroundColor(theme.isDark ? .white : .black)
            Text(subtitle)
                .font(.system(size: 10))
                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
        }
        .padding(12)
        .background(theme.surfaceColor)
        .cornerRadius(theme.cornerRadius)
        .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
    }
}

// MARK: - 2. Regulations & Laws View
struct RegulationsView: View {
    let theme: iOSAppTheme
    @State private var droneWeightGrams = 249
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header
                VStack(alignment: .leading, spacing: 6) {
                    Text("📜 MAGYAR DRÓN SZABÁLYZAT KISOKOS")
                        .font(.system(size: 12, weight: .black, design: .monospaced))
                        .foregroundColor(theme.primaryColor)
                    Text("EU 2019/947 rendelet & Magyar légügyi szabályozás összefoglaló")
                        .font(.caption)
                        .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Insurance Calculator Widget
                VStack(alignment: .leading, spacing: 12) {
                    Text("⚖️ KÖTELEZŐ FELELŐSSÉGBIZTOSÍTÁS KALKULÁTOR")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(theme.primaryColor)
                    
                    HStack {
                        Text("Drón felszállótömege (MTOM):")
                            .font(.caption)
                        Spacer()
                        Text("\(droneWeightGrams) gramm")
                            .font(.system(size: 14, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                    }
                    
                    Slider(value: Binding(
                        get: { Double(droneWeightGrams) },
                        set: { droneWeightGrams = Int($0) }
                    ), in: 100...10000, step: 50)
                    .accentColor(theme.primaryColor)
                    
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Kötelező biztosítási limit:")
                                .font(.caption2)
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            Text(insuranceLimitForWeight(droneWeightGrams))
                                .font(.system(size: 15, weight: .black, design: .monospaced))
                                .foregroundColor(theme.primaryColor)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Kategória:")
                                .font(.caption2)
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            Text(categoryForWeight(droneWeightGrams))
                                .font(.caption.bold())
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(theme.primaryColor.opacity(0.2))
                                .cornerRadius(6)
                                .foregroundColor(theme.primaryColor)
                        }
                    }
                    .padding(10)
                    .background(theme.backgroundColor.opacity(0.6))
                    .cornerRadius(theme.cornerRadius)
                }
                .padding(16)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Regulation Cards
                RegulationCard(
                    title: "A1/A3 Nyílt Kategória (Alapvizsga)",
                    badge: "< 250g - 25kg",
                    description: "Max 120m magasság, emberek felett tilos átrepülni (kivéve A1 <250g C0 drón). A1/A3 ingyenes online vizsga szükséges a KTI felületén.",
                    icon: "shield.checkerboard",
                    theme: theme
                )
                
                RegulationCard(
                    title: "A2 Alkategória (Emberközelben)",
                    badge: "C2 Osztály",
                    description: "Emberek közelében (min. 30m, kis sebességű üzemben 5m) repülhetsz C2 jelzésű drónnal. Elméleti A2 vizsga és gyakorlati önképzés szükséges.",
                    icon: "person.3.fill",
                    theme: theme
                )
                
                RegulationCard(
                    title: "MyDroneSpace Alkalmazás Használat",
                    badge: "Kötelező!",
                    description: "Repülés előtt kötelező a MyDroneSpace appban bejelentkezni és eseti/állandó légteret ellenőrizni és lefoglalni.",
                    icon: "iphone.radiowaves.left.and.right",
                    theme: theme
                )
            }
            .padding(16)
        }
    }
    
    func insuranceLimitForWeight(_ grams: Int) -> String {
        if grams < 250 { return "Nem kötelező (Ajánlott: 3M Ft)" }
        if grams <= 4000 { return "3.000.000 Ft / káresemény" }
        if grams <= 14000 { return "5.000.000 Ft / káresemény" }
        return "10.000.000 Ft / káresemény"
    }
    
    func categoryForWeight(_ grams: Int) -> String {
        if grams < 250 { return "C0 / A1 (Könnyű)" }
        if grams <= 900 { return "C1 / A1" }
        if grams <= 4000 { return "C2 / A2" }
        return "C3-C4 / A3"
    }
}

struct RegulationCard: View {
    let title: String
    let badge: String
    let description: String
    let icon: String
    let theme: iOSAppTheme
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(theme.primaryColor)
                Text(title)
                    .font(.subheadline.bold())
                Spacer()
                Text(badge)
                    .font(.caption2.bold())
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(theme.primaryColor.opacity(0.15))
                    .cornerRadius(4)
                    .foregroundColor(theme.primaryColor)
            }
            Text(description)
                .font(.caption)
                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
                .lineSpacing(3)
        }
        .padding(14)
        .background(theme.surfaceColor)
        .cornerRadius(theme.cornerRadius)
        .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
    }
}

// MARK: - 3. Spotter Locations View
struct SpotterView: View {
    let theme: iOSAppTheme
    
    let spots: [SpotterLocation] = [
        SpotterLocation(name: "Hármashatár-hegy (Guckler Kilátó)", region: "Budapest, II. kerület", category: "Panoráma", description: "Lenyűgöző budapesti panoráma és naplemente. Széltől védett völgyek.", coordinates: "47.5562, 18.9981", maxAltitude: "120m (Eseti MydroneSpace)", rules: "Hétvégén sok a túrázó, tarts 30m távolságot tőlük!", imageEmoji: "🏔️"),
        SpotterLocation(name: "Balatonvilágos Magaspart", region: "Balaton, Veszprém megye", category: "Vízpart", description: "Ikonikus balatoni kilátás a Kisfaludy kilátótól. Kristálytiszta türkizkék víz.", coordinates: "46.9742, 18.1633", maxAltitude: "120m", rules: "Erős tóparti szélre számíts!", imageEmoji: "🌊"),
        SpotterLocation(name: "Bokodi-tó (Lbegő Falu)", region: "Komárom-Esztergom", category: "Különleges", description: "Cölöpökre épült horgászházak és fa pallók feletti drónvideózás.", coordinates: "47.4920, 18.2580", maxAltitude: "100m", rules: "Magánterületek felett ne lebegj alacsonyan!", imageEmoji: "🏚️"),
        SpotterLocation(name: "Visegrádi Fellegvár & Dunakanyar", region: "Pest megye", category: "Történelmi", description: "Dunakanyar kanyarulat és középkori vár panoráma.", coordinates: "47.7936, 18.9806", maxAltitude: "120m", rules: "Műemlék védelmi övezet közelében fokozott figyelem!", imageEmoji: "🏰"),
        SpotterLocation(name: "Tihanyi Belső-tó & Levendulás", region: "Balaton", category: "Természet", description: "Vulkáni eredetű Belső-tó és tihanyi apátsági sziluett.", coordinates: "46.9110, 17.8890", maxAltitude: "120m", rules: "Nemzeti Park területe: természetvédelmi engedély szükséges lehet!", imageEmoji: "🪻")
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("📍 MAGYAR DRÓN SPOTTER TÉRKÉP")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        Text("Tesztelt, biztonságos és látványos legális drónozó helyszínek")
                            .font(.caption2)
                            .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                    }
                    Spacer()
                }
                .padding(14)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Spots List
                ForEach(spots) { spot in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack(spacing: 10) {
                            Text(spot.imageEmoji)
                                .font(.title)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(spot.name)
                                    .font(.subheadline.bold())
                                    .foregroundColor(theme.isDark ? .white : .black)
                                Text(spot.region)
                                    .font(.caption)
                                    .foregroundColor(theme.primaryColor)
                            }
                            Spacer()
                            Text(spot.category)
                                .font(.caption2.bold())
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(theme.primaryColor.opacity(0.15))
                                .cornerRadius(6)
                                .foregroundColor(theme.primaryColor)
                        }
                        
                        Text(spot.description)
                            .font(.caption)
                            .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
                        
                        HStack(spacing: 12) {
                            HStack(spacing: 4) {
                                Image(systemName: "arrow.up.and.down")
                                Text(spot.maxAltitude)
                            }
                            .font(.system(size: 10, weight: .medium, design: .monospaced))
                            .foregroundColor(.orange)
                            
                            HStack(spacing: 4) {
                                Image(systemName: "location.fill")
                                Text(spot.coordinates)
                            }
                            .font(.system(size: 10, weight: .medium, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        }
                        
                        Text("⚠️ Szabályok: \(spot.rules)")
                            .font(.system(size: 10))
                            .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            .padding(8)
                            .background(theme.backgroundColor.opacity(0.5))
                            .cornerRadius(6)
                    }
                    .padding(14)
                    .background(theme.surfaceColor)
                    .cornerRadius(theme.cornerRadius)
                    .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                }
            }
            .padding(16)
        }
    }
}

// MARK: - 4. Drone Marketplace View
struct MarketplaceView: View {
    let theme: iOSAppTheme
    @State private var items: [DroneMarketItem] = [
        DroneMarketItem(title: "DJI Mini 3 Pro Fly More Combo (Karcmentes)", category: "Drónok", priceHuf: 245000, location: "Budapest, XI.", condition: "Újszerű (12 repülés)", sellerName: "Péter K.", contactPhone: "+36 30 123 4567", description: "3 darab akkumulátorral, RC kijelzős távirányítóval, táskával, érvényes felelősségbiztosítás átruházással."),
        DroneMarketItem(title: "DJI Avata 2 FPV Goggles 3 szett", category: "FPV", priceHuf: 380000, location: "Győr", condition: "Kiváló", sellerName: "Gábor M.", contactPhone: "+36 20 987 6543", description: "Bontatlan Motion Controller 3-mal és plusz akkuval."),
        DroneMarketItem(title: "DJI Mavic 3 Enterprise Hőkamera Akku", category: "Akkumulátorok", priceHuf: 55000, location: "Debrecen", condition: "Új (0 ciklus)", sellerName: "DrónTech Kft.", contactPhone: "+36 70 555 1212", description: "Bontatlan gyári csomagolásban, számlával.")
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("🛒 DRÓN PIAC & APRÓHIRDETÉSEK")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        Text("Ellenőrzött használt drónok, alkatrészek és kiegészítők")
                            .font(.caption2)
                            .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                    }
                    Spacer()
                }
                .padding(14)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Marketplace Items List
                ForEach(items) { item in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text(item.title)
                                .font(.subheadline.bold())
                                .foregroundColor(theme.isDark ? .white : .black)
                            Spacer()
                            Text("\(item.priceHuf) Ft")
                                .font(.system(size: 15, weight: .black, design: .monospaced))
                                .foregroundColor(theme.primaryColor)
                        }
                        
                        Text(item.description)
                            .font(.caption)
                            .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
                        
                        HStack {
                            HStack(spacing: 4) {
                                Image(systemName: "mappin.and.ellipse")
                                Text(item.location)
                            }
                            .font(.caption2)
                            .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            
                            Spacer()
                            
                            HStack(spacing: 4) {
                                Image(systemName: "person.fill")
                                Text("\(item.sellerName) (\(item.contactPhone))")
                            }
                            .font(.caption2.bold())
                            .foregroundColor(theme.primaryColor)
                        }
                    }
                    .padding(14)
                    .background(theme.surfaceColor)
                    .cornerRadius(theme.cornerRadius)
                    .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                }
            }
            .padding(16)
        }
    }
}

// MARK: - 5. Exam & Quiz View
struct ExamAndInsuranceView: View {
    let theme: iOSAppTheme
    @State private var currentQuestionIndex = 0
    @State private var selectedOptionIndex: Int? = nil
    @State private var score = 0
    
    let questions: [ExamQuestion] = [
        ExamQuestion(question: "Mekkora a maximális megengedett repülési magasság a földfelszíntől számítva Nyílt kategóriában?", options: ["50 méter", "100 méter", "120 méter", "300 méter"], correctIndex: 2, explanation: "Az EU 2019/947 rendelet szerint a földfelszíntől számított maximális magasság 120 méter."),
        ExamQuestion(question: "Milyen minimális életkor szükséges az A1/A3 drónpilóta tanúsítvány megszerzéséhez Magyarországon?", options: ["12 év", "16 év", "18 év", "Nincs életkori megkötés"], correctIndex: 1, explanation: "Magyarországon a minimális életkor 16 év az önálló drónpilóta regisztrációhoz és vizsgához."),
        ExamQuestion(question: "Kötelező-e a MyDroneSpace alkalmazás használata magyar légtérben végzett drónrepüléskor?", options: ["Nem, csak ajánlott", "Igen, a repülés teljes ideje alatt kötelező", "Csak éjszaka kötelező", "Csak 4kg feletti drónnál kötelező"], correctIndex: 1, explanation: "Magyarországon a jogszabály írja elő a MyDroneSpace mobilalkalmazás aktív használatát repülés közben.")
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Card
                VStack(alignment: .leading, spacing: 4) {
                    Text("🎓 A1/A3 DRÓNVIZSGA TESZTFELKÉSZÍTŐ")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(theme.primaryColor)
                    Text("Hivatalos KTI felkészítő vizsgakérdések és tesztek")
                        .font(.caption2)
                        .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Question Card
                if currentQuestionIndex < questions.count {
                    let q = questions[currentQuestionIndex]
                    
                    VStack(alignment: .leading, spacing: 14) {
                        HStack {
                            Text("Kérdés \(currentQuestionIndex + 1) / \(questions.count)")
                                .font(.caption.bold())
                                .foregroundColor(theme.primaryColor)
                            Spacer()
                            Text("Pontszám: \(score)")
                                .font(.caption.bold())
                        }
                        
                        Text(q.question)
                            .font(.subheadline.bold())
                            .foregroundColor(theme.isDark ? .white : .black)
                        
                        VStack(spacing: 8) {
                            ForEach(0..<q.options.count, id: \.self) { idx in
                                Button(action: {
                                    selectedOptionIndex = idx
                                    if idx == q.correctIndex {
                                        score += 1
                                    }
                                }) {
                                    HStack {
                                        Text("\(idx + 1). \(q.options[idx])")
                                            .font(.caption)
                                        Spacer()
                                        if selectedOptionIndex == idx {
                                            Image(systemName: idx == q.correctIndex ? "checkmark.circle.fill" : "xmark.circle.fill")
                                                .foregroundColor(idx == q.correctIndex ? .green : .red)
                                        }
                                    }
                                    .padding(12)
                                    .background(
                                        selectedOptionIndex == idx ?
                                        (idx == q.correctIndex ? Color.green.opacity(0.2) : Color.red.opacity(0.2)) :
                                        theme.backgroundColor.opacity(0.5)
                                    )
                                    .cornerRadius(theme.cornerRadius)
                                    .foregroundColor(theme.isDark ? .white : .black)
                                }
                                .disabled(selectedOptionIndex != nil)
                            }
                        }
                        
                        if selectedOptionIndex != nil {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("💡 Magyarázat:")
                                    .font(.caption2.bold())
                                    .foregroundColor(theme.primaryColor)
                                Text(q.explanation)
                                    .font(.caption2)
                                    .foregroundColor(theme.isDark ? .gray : .black.opacity(0.7))
                            }
                            .padding(10)
                            .background(theme.backgroundColor)
                            .cornerRadius(theme.cornerRadius)
                            
                            Button(action: {
                                selectedOptionIndex = nil
                                currentQuestionIndex += 1
                            }) {
                                Text("Következő Kérdés ➔")
                                    .font(.caption.bold())
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(theme.primaryColor)
                                    .foregroundColor(theme.isDark ? .black : .white)
                                    .cornerRadius(theme.cornerRadius)
                            }
                        }
                    }
                    .padding(16)
                    .background(theme.surfaceColor)
                    .cornerRadius(theme.cornerRadius)
                    .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                } else {
                    // Results Card
                    VStack(spacing: 12) {
                        Text("🎉 TESZT BEFEJEZVE!")
                            .font(.system(size: 16, weight: .black, design: .monospaced))
                            .foregroundColor(theme.primaryColor)
                        
                        Text("Elért eredményed: \(score) / \(questions.count) pont (\(Int(Double(score)/Double(questions.count)*100))%)")
                            .font(.subheadline.bold())
                        
                        Button(action: {
                            currentQuestionIndex = 0
                            score = 0
                            selectedOptionIndex = nil
                        }) {
                            Text("Teszt Újraindítása")
                                .font(.caption.bold())
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                                .background(theme.primaryColor)
                                .foregroundColor(theme.isDark ? .black : .white)
                                .cornerRadius(theme.cornerRadius)
                        }
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity)
                    .background(theme.surfaceColor)
                    .cornerRadius(theme.cornerRadius)
                    .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                }
            }
            .padding(16)
        }
    }
}

// MARK: - 6. Pilot Profile View
struct PilotProfileView: View {
    let theme: iOSAppTheme
    @State private var pilotName = "Márton L."
    @State private var operatorId = "HUN-OP-8912345678"
    @State private var licenseId = "HUN-A1A3-2024-9981"
    
    @State private var fleet: [DroneVehicle] = [
        DroneVehicle(modelName: "DJI Mini 3 Pro", serialNumber: "1582F4X98213", weightGrams: 249, batteryCycles: 18, lastFlightDate: "2026-09-12"),
        DroneVehicle(modelName: "DJI Avata 2", serialNumber: "3710A8829102", weightGrams: 377, batteryCycles: 9, lastFlightDate: "2026-09-14")
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Pilot License Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack(spacing: 12) {
                        Text("👨‍✈️")
                            .font(.system(size: 36))
                        VStack(alignment: .leading, spacing: 2) {
                            Text(pilotName)
                                .font(.subheadline.bold())
                            Text("A1/A3 & A2 Minősített Drónpilóta")
                                .font(.caption)
                                .foregroundColor(theme.primaryColor)
                        }
                    }
                    
                    Divider()
                    
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text("UAS Üzemeltetői azonosító:")
                                .font(.caption2)
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            Spacer()
                            Text(operatorId)
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(theme.primaryColor)
                        }
                        HStack {
                            Text("A1/A3 Tanúsítvány száma:")
                                .font(.caption2)
                                .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            Spacer()
                            Text(licenseId)
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(theme.primaryColor)
                        }
                    }
                }
                .padding(16)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                // Fleet Management Card
                VStack(alignment: .leading, spacing: 12) {
                    Text("🛸 SAJÁT DRÓN FLOTTA (\(fleet.count) DRÓN)")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(theme.primaryColor)
                    
                    ForEach(fleet) { drone in
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(drone.modelName)
                                    .font(.subheadline.bold())
                                Text("S/N: \(drone.serialNumber) • \(drone.weightGrams)g")
                                    .font(.caption2)
                                    .foregroundColor(theme.isDark ? .gray : .black.opacity(0.6))
                            }
                            Spacer()
                            VStack(alignment: .trailing, spacing: 2) {
                                Text("\(drone.batteryCycles) akku ciklus")
                                    .font(.caption2.bold())
                                    .foregroundColor(theme.primaryColor)
                                Text("Utolsó: \(drone.lastFlightDate)")
                                    .font(.system(size: 9))
                                    .foregroundColor(theme.isDark ? .gray : .black.opacity(0.5))
                            }
                        }
                        .padding(10)
                        .background(theme.backgroundColor.opacity(0.5))
                        .cornerRadius(theme.cornerRadius)
                    }
                }
                .padding(16)
                .background(theme.surfaceColor)
                .cornerRadius(theme.cornerRadius)
                .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
            }
            .padding(16)
        }
    }
}

// MARK: - 7. Chat View
struct ChatView: View {
    let theme: iOSAppTheme
    @State private var newMessageText = ""
    @State private var messages: [ChatMessage] = [
        ChatMessage(sender: "Péter_FPV", text: "Sziasztok! Hármashatár-hegyen ma tökéletesek a szélviszonyok, 10 km/h alatti lökés!", timestamp: "10:14", isPilotPro: true, category: "Spotok"),
        ChatMessage(sender: "László_Drone", text: "Köszi az infót! MyDroneSpace-ben lefoglaltad az eseti légteret?", timestamp: "10:18", isPilotPro: false, category: "Spotok"),
        ChatMessage(sender: "Péter_FPV", text: "Persze, 10:00 - 12:00 között aktív a légtér foglalásom.", timestamp: "10:20", isPilotPro: true, category: "Spotok")
    ]
    
    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 10) {
                    ForEach(messages) { msg in
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                HStack(spacing: 6) {
                                    Text(msg.sender)
                                        .font(.caption.bold())
                                        .foregroundColor(theme.primaryColor)
                                    if msg.isPilotPro {
                                        Text("PRO")
                                            .font(.system(size: 8, weight: .black))
                                            .padding(.horizontal, 4)
                                            .padding(.vertical, 1)
                                            .background(theme.primaryColor)
                                            .foregroundColor(.black)
                                            .cornerRadius(3)
                                    }
                                    Spacer()
                                    Text(msg.timestamp)
                                        .font(.system(size: 9))
                                        .foregroundColor(theme.isDark ? .gray : .black.opacity(0.5))
                                }
                                Text(msg.text)
                                    .font(.caption)
                                    .foregroundColor(theme.isDark ? .white : .black)
                            }
                            .padding(10)
                            .background(theme.surfaceColor)
                            .cornerRadius(theme.cornerRadius)
                            .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                        }
                    }
                }
                .padding(16)
            }
            
            // Input Bar
            HStack(spacing: 8) {
                TextField("Írj a pilóta chatekbe...", text: $newMessageText)
                    .font(.caption)
                    .padding(10)
                    .background(theme.surfaceColor)
                    .cornerRadius(theme.cornerRadius)
                    .overlay(RoundedRectangle(cornerRadius: theme.cornerRadius).stroke(theme.cardBorderColor, lineWidth: 1))
                
                Button(action: {
                    if !newMessageText.isEmpty {
                        messages.append(ChatMessage(sender: "Én", text: newMessageText, timestamp: "10:42", isPilotPro: true, category: "Általános"))
                        newMessageText = ""
                    }
                }) {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 14))
                        .padding(10)
                        .background(theme.primaryColor)
                        .foregroundColor(theme.isDark ? .black : .white)
                        .cornerRadius(theme.cornerRadius)
                }
            }
            .padding(12)
            .background(theme.surfaceColor)
        }
    }
}

// MARK: - Theme Picker Sheet
struct ThemePickerSheet: View {
    @Binding var activeTheme: iOSAppTheme
    @Binding var isPresented: Bool
    
    var body: some View {
        ZStack {
            activeTheme.backgroundColor.ignoresSafeArea()
            
            VStack(spacing: 16) {
                HStack {
                    Text("🎨 VÁLASSZ KÉPERNYŐ TÉMÁT")
                        .font(.system(size: 13, weight: .black, design: .monospaced))
                        .foregroundColor(activeTheme.primaryColor)
                    Spacer()
                    Button(action: { isPresented = false }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundColor(activeTheme.primaryColor)
                    }
                }
                .padding(.bottom, 8)
                
                ScrollView {
                    VStack(spacing: 10) {
                        ForEach(iOSAppTheme.allCases) { theme in
                            Button(action: {
                                activeTheme = theme
                                isPresented = false
                            }) {
                                HStack {
                                    Text(theme.iconEmoji)
                                        .font(.title2)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(theme.displayName)
                                            .font(.subheadline.bold())
                                            .foregroundColor(theme.isDark ? .white : .black)
                                        Text("Sarok: \(Int(theme.cornerRadius))pt")
                                            .font(.caption2)
                                            .foregroundColor(theme.primaryColor)
                                    }
                                    Spacer()
                                    if activeTheme == theme {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundColor(theme.primaryColor)
                                    }
                                }
                                .padding(14)
                                .background(theme.surfaceColor)
                                .cornerRadius(theme.cornerRadius)
                                .overlay(
                                    RoundedRectangle(cornerRadius: theme.cornerRadius)
                                        .stroke(activeTheme == theme ? theme.primaryColor : theme.cardBorderColor, lineWidth: activeTheme == theme ? 2 : 1)
                                )
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
    }
}
