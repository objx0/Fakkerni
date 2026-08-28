# Fakkerni

**Fakkerni (فكرني)** is an Arabic-first Android reminder app for the things that are easy to forget: car maintenance, subscriptions, hydration, medication, home tasks, and personal follow-ups.

## Features

- Material 3 light interface with a mint/teal visual system
- Arabic RTL interface and reminder-focused dashboard
- Custom reminder icons for car, subscriptions, health, home, money, and more
- Date and time picker when creating reminders
- Daily, weekly, and monthly recurring reminders
- Completion tracking and one-hour notification snooze
- Room persistence with automatic migration from the original `SharedPreferences` data
- Android notifications through `AlarmManager` and `BroadcastReceiver`, with snooze and complete actions
- Calendar-correct daily, weekly, and monthly recurrence
- Reminder restoration after reboot, app update, clock changes, and timezone changes
- Starter examples for oil changes, Netflix subscription review, and drinking water
- Swipe reminder cards right to complete, left to snooze, or farther left to delete
- Weekly completion statistics and health-habit streaks based on local completion history
- Arabic/English language switch and system, light, or dark theme choices

Open the folder in Android Studio and let Gradle sync. The package is `com.fakarni.app` and the app name is `Fakkerni`.

The UI is built with Jetpack Compose and split into screens, reusable components, and a lifecycle-aware `FakarniViewModel`. Run `./gradlew test` for the recurrence and persistence-converter unit tests.
