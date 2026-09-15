import Foundation
import UserNotifications

struct GoldenHourNotificationHandler {
    static func requestNotificationPermissions() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let error = error {
                print("Error requesting notification permission on iOS: \(error.localizedDescription)")
            } else {
                print("Notification permission granted on iOS: \(granted)")
            }
        }
    }

    static func triggerTestNotificationIn5Seconds() {
        let content = UNMutableNotificationContent()
        content.title = "🔔 TESZT Riasztás: Aranyóra & Szürkület"
        content.body = "Az iOS értesítés és a háttérbeli szürkületi riasztás tökéletesen működik ezen a készüléken!"
        content.sound = UNNotificationSound.default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 5, repeats: false)
        let request = UNNotificationRequest(identifier: "test_golden_hour_alarm", content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Failed to schedule iOS test notification: \(error)")
            }
        }
    }

    static func scheduleGoldenHourNotification(title: String, body: String) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = UNNotificationSound.default

        // Schedule notification for 20:00 local time
        var dateComponents = DateComponents()
        dateComponents.hour = 20
        dateComponents.minute = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: "golden_hour_daily_alarm", content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Failed to schedule iOS notification: \(error)")
            }
        }
    }

    static func cancelNotifications() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
}
