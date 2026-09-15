# 📱 Drón Kalauz - iOS Kódalap & Xcode Útmutató

Ez a mappa tartalmazza a **Drón Kalauz** natív iOS alkalmazás kódját és Xcode projektbeállításait.

---

## 🚀 Telepítési Lépések Mac-en (Xcode)

### 1. Előfeltételek
- **Mac számítógép** (macOS Monterey vagy újabb).
- **Xcode** (App Store-ból letölthető).
- **iPhone** és adatkábel (vagy Xcode iOS Szimulátor).

### 2. Projekt megnyitása
1. Töltsd le a teljes projektet ZIP-ben (vagy pushold GitHubra).
2. Nyisd meg az `iosApp` mappát.
3. Kattints duplán az `iosApp.xcodeproj` fájlra. Ez megnyitja a projektet Xcode-ban.

### 3. Aláírás és Fiók (Signing) Beállítása
1. Xcode-ban kattints a bal oldali sáv legtetején lévő kék kártyára (`iosApp`).
2. Válaszd a **Signing & Capabilities** fület.
3. Pipáld be az **Automatically manage signing** opciót.
4. A **Team** legördülő menüben válaszd ki a saját Apple fiókodat (Personal Team).
5. Ha szükséges, írj át egy egyedi Bundle Identifier-t (pl. `com.sajátnév.dronkalauz`).

### 4. Telepítés iPhone-ra
1. Csatlakoztasd az iPhone-t a Mac-hez.
2. Kapcsold be a Fejlesztői módot az iPhone-on: *Beállítások ➔ Adatvédelem és biztonság ➔ Fejlesztői mód*.
3. Az Xcode felső sávjában válaszd ki a csatlakoztatott iPhone-t célkészülékként.
4. Nyomj rá a **Play (▶️)** gombra!

---

## 🔔 Támogatott Funkciók az iOS Verzióban
- **Aranyóra & Polgári Szürkület Riasztások:** `UNUserNotificationCenter` segítségével natív iOS értesítések és hangjelzések a szürkület vége előtt.
- **Helymeghatározás:** iOS `CoreLocation` engedélyek és kérések felkészítve.
- **Dizájn & Téma:** Sötét drónos felület megegyező színekkel és ikonokkal.
