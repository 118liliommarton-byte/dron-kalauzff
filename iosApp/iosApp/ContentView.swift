import SwiftUI

struct ContentView: View {
    @State private var selectedTab: Int = 0
    @State private var isGoldenHourAlarmEnabled: Bool = false

    var body: some View {
        ZStack {
            Color(red: 0.07, green: 0.09, blue: 0.13)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Top App Bar
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("DRÓN KALAUSZ iOS")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                        Text("Magyarország Legfelső Légtér & Fotós Segéd")
                            .font(.system(size: 11))
                            .foregroundColor(Color(red: 0.23, green: 0.81, blue: 0.93))
                    }
                    Spacer()
                    Button(action: {
                        GoldenHourNotificationHandler.triggerTestNotificationIn5Seconds()
                    }) {
                        Image(systemName: "bell.badge.fill")
                            .font(.system(size: 18))
                            .foregroundColor(Color(red: 0.98, green: 0.75, blue: 0.14))
                            .padding(8)
                            .background(Color.white.opacity(0.1))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 12)
                .padding(.bottom, 12)
                .background(Color(red: 0.10, green: 0.13, blue: 0.18))

                // Main Body View
                ScrollView {
                    VStack(spacing: 16) {
                        // iOS Multiplatform Banner
                        HStack {
                            Image(systemName: "iphone.radiowaves.left.and.right")
                                .font(.system(size: 24))
                                .foregroundColor(Color(red: 0.23, green: 0.81, blue: 0.93))
                            VStack(alignment: .leading, spacing: 2) {
                                Text("iOS Multiplatform Kódalap")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.white)
                                Text("Compose Multiplatform keretrendszerre előkészített natív iOS kiadás.")
                                    .font(.system(size: 12))
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                        }
                        .padding(14)
                        .background(Color(red: 0.14, green: 0.18, blue: 0.25))
                        .cornerRadius(12)

                        // Golden hour alert trigger
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                Image(systemName: "sun.sunset.fill")
                                    .foregroundColor(.orange)
                                Text("Aranyóra & Szürkület Riasztás")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.white)
                                Spacer()
                                Toggle("", isOn: $isGoldenHourAlarmEnabled)
                                    .onChange(of: isGoldenHourAlarmEnabled) { newValue in
                                        if newValue {
                                            GoldenHourNotificationHandler.scheduleGoldenHourNotification(
                                                title: "📸 Aranyóra & Polgári Szürkület Riasztás",
                                                body: "Még 15 perc van hátra a törvényes repülési időből (polgári szürkület vége)! Készülj fel a leszállásra."
                                            )
                                        } else {
                                            GoldenHourNotificationHandler.cancelNotifications()
                                        }
                                    }
                            }
                            Text("A szürkület vége előtt 15 perccel automatikus hangjelzést ad az iPhone értesítési központjában.")
                                .font(.system(size: 12))
                                .foregroundColor(.gray)

                            Button(action: {
                                GoldenHourNotificationHandler.triggerTestNotificationIn5Seconds()
                            }) {
                                HStack {
                                    Image(systemName: "play.circle.fill")
                                    Text("Teszt Riasztás Készítése (5 mp)")
                                }
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(.black)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color(red: 0.98, green: 0.75, blue: 0.14))
                                .cornerRadius(8)
                            }
                        }
                        .padding(14)
                        .background(Color(red: 0.14, green: 0.18, blue: 0.25))
                        .cornerRadius(12)

                        // Airspace Quick Overview Card
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Legutóbbi Légtér Ellenőrzés")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.white)

                            HStack {
                                VStack(alignment: .leading) {
                                    Text("Status")
                                        .font(.system(size: 11))
                                        .foregroundColor(.gray)
                                    Text("Biztonságos Felszállás")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.green)
                                }
                                Spacer()
                                VStack(alignment: .trailing) {
                                    Text("KP Index")
                                        .font(.system(size: 11))
                                        .foregroundColor(.gray)
                                    Text("2 (Nyugodt)")
                                        .font(.system(size: 13, weight: .medium))
                                        .foregroundColor(.white)
                                }
                            }
                        }
                        .padding(14)
                        .background(Color(red: 0.14, green: 0.18, blue: 0.25))
                        .cornerRadius(12)
                    }
                    .padding(16)
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
