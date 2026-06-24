# AutoClicker - Android Auto-Click Application

A full-featured Android auto-clicker application built with **Kotlin** and **Jetpack Compose**.

## Features

- 🖱️ **Auto Clicking** - Configurable tap points with adjustable intervals
- 👆 **Swipe Gestures** - Automated swipe from point A to point B
- 👆 **Long Press** - Automated long press gestures
- 🎯 **Multi-Point Sequences** - Chain multiple gestures in sequence
- 🔁 **Loop Modes** - Once, Infinite, or Custom loop count
- 🪟 **Floating Overlay** - Control panel that stays on top of all apps
- 💾 **Macro System** - Save and load gesture configurations (Room database)
- ⚡ **Speed Control** - 0.1x to 5.0x speed multiplier
- 🎨 **Material 3 UI** - Modern dark/light theme with dynamic colors

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.53.1 |
| Database | Room 2.6.1 |
| Preferences | DataStore |
| Navigation | Navigation Compose 2.8.5 |
| Core Engine | AccessibilityService.dispatchGesture() |
| Min SDK | 29 (Android 10) |
| Target SDK | 35 |

## Architecture

```
com.imut.autoclicker/
├── AutoClickerApp.kt          # Application class (Hilt + notification channel)
├── MainActivity.kt             # Single activity with Compose navigation
├── accessibility/
│   └── AutoClickerService.kt   # AccessibilityService for gesture dispatch
├── gesture/
│   └── GestureEngine.kt        # Coroutine-based gesture sequence orchestrator
├── overlay/
│   └── FloatingControlService.kt # Floating overlay control panel
├── data/
│   ├── SettingsRepository.kt   # DataStore preferences
│   ├── MacroEntity.kt          # Room entity + type converters
│   ├── MacroDao.kt             # Room DAO
│   └── MacroRepository.kt      # Room database + repository
├── di/
│   └── AppModule.kt            # Hilt dependency injection module
├── viewmodel/
│   ├── HomeViewModel.kt        # Home screen state management
│   ├── GestureConfigViewModel.kt # Gesture configuration logic
│   └── OtherViewModels.kt      # Settings + Macro list ViewModels
└── ui/
    ├── theme/Theme.kt          # Material 3 color scheme
    └── screens/
        ├── HomeScreen.kt       # Main control screen
        ├── GestureConfigScreen.kt # Gesture point editor
        ├── SettingsScreen.kt   # App settings
        └── MacroListScreen.kt  # Saved macros list
```

## How It Works

1. **AccessibilityService** - Uses Android's `AccessibilityService.dispatchGesture()` API to perform taps, swipes, and long presses without requiring root access
2. **GestureEngine** - Orchestrates gesture sequences using Kotlin coroutines with pause/resume/stop support
3. **FloatingControlService** - Foreground service that displays a draggable overlay control panel above all apps
4. **DataLayer** - Room for macro persistence, DataStore for user preferences

## Setup

1. Open the project in Android Studio
2. Sync Gradle
3. Run on device (API 29+)
4. Grant Overlay permission
5. Enable Accessibility Service
6. Configure click points and start!

## Permissions

- `SYSTEM_ALERT_WINDOW` - Floating overlay control panel
- `FOREGROUND_SERVICE` - Background service for overlay
- `POST_NOTIFICATIONS` - Service status notifications
- `VIBRATE` - Haptic feedback on taps
- `WAKE_LOCK` - Keep screen active during operation

## Build

```bash
./gradlew assembleDebug
```

## License

MIT
