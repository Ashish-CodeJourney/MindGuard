# MindGuard - Digital Wellbeing Android MVP

MindGuard is a privacy-first Android app that detects and blocks addictive short-form content (Instagram Reels, YouTube Shorts, etc.) to help users reclaim their attention.

## Features (MVP)

- **Instagram Reel Detection**: Automatically detects when you open Instagram Reels
- **Instant Redirect**: Returns you to Instagram Home feed within ~1 second
- **Privacy-First**: No accounts, no cloud dependency, no tracking
- **Battery Efficient**: Minimal background processing with 2-second cooldown
- **In-Device Processing**: All detection happens locally on device

## Architecture

### Project Structure

```
mindguard/
├── shared/                    # Kotlin Multiplatform - Pure business logic
│   ├── commonMain/
│   │   ├── domain/            # Core domain models
│   │   ├── models/            # Data classes (ScreenSnapshot, etc)
│   │   ├── rules/             # Detection rules (InstagramReelRule, RuleEngine)
│   │   ├── usecases/          # Orchestration (DetectBlockedContentUseCase, BlockCooldown)
│   │   ├── data/              # Repository interfaces
│   │   └── di/                # Koin shared modules
│   └── commonTest/            # Unit tests (TDD-first)
│
└── androidApp/                # Android-specific implementation
    ├── accessibility/         # AccessibilityService integration
    │   ├── MindGuardAccessibilityService
    │   ├── AccessibilityEventConverter
    │   ├── AccessibilityEventDebouncer
    │   └── BlockActionExecutor
    ├── storage/               # DataStore preferences
    ├── ui/                    # Jetpack Compose screens
    └── di/                    # Android DI modules
```

### Layered Architecture

```
UI Layer (Compose)
    ↓
Dependency Injection (Koin)
    ↓
Use Case Layer (Orchestration)
    ↓
Domain Layer (Pure Business Logic)
    ├── RuleEngine (Strategy pattern)
    └── BlockingRule Interface (Extensible)
    ↓
Android-Specific (AccessibilityService)
    └── BlockActionExecutor
```

## Detection Strategy

MindGuard uses **multi-signal heuristics** to reliably detect Reels without hardcoded IDs:

1. **Text Signals**: "Reels" label detection (case-insensitive)
2. **Resource ID Signals**: reel_pager, reels_tab, reel_play_button
3. **Description Signals**: "reel", "video reel" keywords
4. **Blocklist Check**: Excludes feed_pager, feed_container, stories_container

**Requires 2+ signals** for high-confidence detection, avoiding false positives on feed/stories.

## Block Flow

```
AccessibilityEvent
    ↓
AccessibilityEventConverter → ScreenSnapshot
    ↓
AccessibilityEventDebouncer (100ms window)
    ↓
DetectBlockedContentUseCase
    ↓
RuleEngine (evaluates rules in order)
    ↓
InstagramReelRule (multi-signal detection)
    ↓
BlockCooldown (2-second debounce)
    ↓
BlockActionExecutor
    ├─ GO_BACK: performGlobalAction()
    └─ GO_HOME_AND_REOPEN_APP: fallback
```

## Extensibility

Architecture designed for future blockers:

- **Add new rule**: Implement `BlockingRule` interface
- **Add to RuleEngine**: `RuleEngine(listOf(instagramRule, youtubeRule, ...))`
- **No code changes**: Just pluggable rule implementations

Supported future apps:
- YouTube Shorts
- Snapchat Spotlight
- TikTok
- Facebook Reels
- Reddit infinite scrolling

## Development

### Technologies

- **Language**: Kotlin (Multiplatform)
- **Build**: Gradle Kotlin DSL
- **Async**: Coroutines + Flow
- **UI**: Jetpack Compose
- **Storage**: DataStore (preferences), SQLDelight (analytics)
- **DI**: Koin
- **Testing**: Kotlin Test, Turbine

### Testing (TDD-First)

Run tests:
```bash
./gradlew test
```

All production code is written in response to failing tests. Test coverage >90% on shared module.

### Key Classes

#### Domain
- `ScreenSnapshot`: Accessibility event data (package, text, descriptions, resource IDs)
- `BlockAction`: NONE, GO_BACK, GO_HOME_AND_REOPEN_APP
- `DetectionResult`: shouldBlock, action, reason

#### Rules
- `BlockingRule`: Interface for detection rules
- `RuleEngine`: Evaluates rules in order, short-circuits on match
- `InstagramReelRule`: Multi-signal Instagram Reel detection

#### Use Cases
- `DetectBlockedContentUseCase`: Orchestrates rule engine with logging
- `BlockCooldown`: In-memory debounce (2 seconds)

#### Android
- `MindGuardAccessibilityService`: Listens to events, coordinates detection
- `AccessibilityEventConverter`: Converts events to ScreenSnapshot
- `AccessibilityEventDebouncer`: 100ms event debouncing
- `BlockActionExecutor`: Performs global actions (back, home)

#### Storage
- `SettingsDataStore`: Protection toggle preference
- `BlockHistoryRepository`: Analytics (SQLDelight)

### Building

```bash
./gradlew clean build
```

### Permissions Required

```xml
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
```

User must manually enable accessibility service in Settings.

## Performance

- **Event Processing**: <100ms (debounced)
- **Detection**: <50ms (rule evaluation)
- **Block Action**: <500ms (back navigation)
- **Battery Impact**: Minimal (cooldown prevents excessive processing)
- **Memory**: ~10MB baseline

## Privacy

- ✅ No accounts required
- ✅ No cloud dependency
- ✅ No tracking or analytics sent
- ✅ All processing local on device
- ✅ No Instagram content stored
- ⚠️ Accessibility service has broad permissions (required by Android)

## Limitations

- Requires Android 8.0+ (minSdk 24)
- Detection based on UI patterns (fragile to Instagram updates)
- Works only when app is active
- No custom rule configuration in MVP

## Future Roadmap

1. **Support more apps**: YouTube Shorts, Snapchat, TikTok
2. **Focus schedules**: Time windows for protection
3. **Analytics**: Detailed usage reports
4. **Remote rule updates**: Keep detection current without app updates
5. **AI scoring**: Distraction confidence scoring
6. **Cross-platform**: iOS support via SwiftUI

## Contributing

Pull requests welcome. Please follow:
- TDD (red-green-refactor)
- No production code without tests
- Conventional commits
- 100% coverage on shared module

## License

MIT

## Support

- **Issues**: GitHub Issues
- **Questions**: GitHub Discussions

---

**Made with ❤️ for attention reclamation.**
