# MindGuard

**Reclaim your attention.** MindGuard is a privacy-first Android app that automatically detects and blocks short-form video feeds — Instagram Reels, YouTube Shorts, TikTok, and Snapchat Spotlight — entirely on-device.

---

## Features

| Feature | Detail |
|---|---|
| **Reel / Shorts / Spotlight blocking** | Instagram, YouTube, TikTok, Snapchat |
| **Per-app toggles** | Enable or disable blocking per app |
| **Focus schedule** | Block only during configurable hours (supports overnight ranges) |
| **Pause focus mode** | 30-minute pause from the app or directly from the notification shade |
| **Quick Settings tile** | Tap "Focus Mode" tile to pause/resume without opening the app |
| **Daily stats** | Blocks today, attempts today |
| **All-time stats** | Total blocks, total attempts |
| **Streak tracking** | Current clean-day streak and all-time best |
| **On-device only** | No accounts, no cloud, no tracking |
| **Play Protect compliant** | Passes Google Play Protect review |

---

## How it works

MindGuard uses Android's Accessibility Service to inspect UI events from the four target apps only. When the short-video player is detected on screen, the service navigates away before you can get drawn in.

```
AccessibilityEvent (from Instagram / YouTube / TikTok / Snapchat)
        │
        ▼
AccessibilityEventConverter
  └─ BFS node traversal (depth 15) + rootInActiveWindow augment
  └─ Extracts resource IDs (requires flagReportViewIds)
        │
        ▼
ScreenSnapshot { packageName, resourceIds, screenText, contentDescriptions }
        │
        ▼
RuleEngine
  ├─ InstagramReelRule   → clips_viewer_view_pager, reel_pager, …
  ├─ YouTubeShortsRule   → reel_watch_fragment_root, reel_progress_bar, …
  ├─ TikTokRule          → TikTok video-feed resource IDs
  └─ SnapchatSpotlightRule → spotlight_container
        │
        ▼ shouldBlock = true
BlockCooldown (500 ms)
        │
        ▼
BlockActionExecutor
  ├─ Instagram → CLICK_SAFE_TAB  (clicks Home feed tab, stays in app)
  └─ Others   → GO_BACK         (system back press)
        │
        ▼
800 ms watchdog rescans rootInActiveWindow while blocked app is foreground
```

### Detection design

Detection is **resource-ID only** — no text or navigation-tab signals. This means:
- Opening YouTube does not trigger a block (Shorts nav tab visible but not the player)
- Opening Instagram does not trigger a block (Reels nav tab visible but not the player)
- Only confirmed player-specific view IDs trigger a block

| App | Blocked when these resource IDs appear |
|---|---|
| Instagram | `clips_viewer_view_pager`, `reel_pager`, `reel_play_button`, `reel_component`, `clips_swipe_container`, `reels_viewer`, `reel_feed_recycler_view`, `ig_reels_player_container` |
| YouTube | `reel_watch_fragment_root`, `reel_progress_bar`, `shorts_video_header`, `shorts_container`, `shorts_vertical_feed_container`, `reel_player_page_container` |
| TikTok | TikTok video-feed container IDs |
| Snapchat | `spotlight_container` |

Instagram's home feed, profile pages, and Explore are never blocked — the service uses an explicit feed-screen guard (`feed_pager`, `feed_container`, `stories_container`) that short-circuits detection.

---

## Architecture

```
mindguard/
├── shared/                        # Kotlin Multiplatform — pure business logic
│   └── commonMain/
│       ├── models/                # ScreenSnapshot, DetectionResult, BlockAction
│       ├── rules/                 # InstagramReelRule, YouTubeShortsRule, TikTokRule,
│       │                          #   SnapchatSpotlightRule, RuleEngine, BlockingRule
│       └── usecases/              # DetectBlockedContentUseCase, BlockCooldown,
│                                  #   FocusPauseLogic (isPausedAt pure function)
│
└── androidApp/                    # Android-specific
    ├── accessibility/             # MindGuardAccessibilityService, AccessibilityEventConverter,
    │                              #   BlockActionExecutor
    ├── storage/                   # SettingsDataStore (DataStore Preferences)
    ├── tile/                      # FocusTileService (Quick Settings tile)
    ├── ui/screens/                # Compose screens: Onboarding, Permissions, Home,
    │                              #   Stats, Settings
    └── di/                        # Koin modules
```

The shared module has zero Android dependencies — all rules and use cases are plain Kotlin and fully unit-testable on the JVM.

---

## Screens

**Onboarding** — explains what the service does before asking for permission.

**Permissions** — guides setup:
- Android 13+ sideloaded builds: two-step guide (allow restricted settings → enable service)
- Play Store / older builds: single-step (enable service)
- Advances automatically within 500 ms of permission being granted

**Home** — focus mode toggle, today's block count, streak, pause button (when active).

**Stats** — today + all-time blocks and attempts.

**Settings** — per-app toggles, focus schedule (start/end hour).

---

## Privacy

- All detection runs entirely on-device
- No network requests
- No user accounts or sign-in
- Accessibility service scope restricted to 6 package names via `android:packageNames`
- Service description is explicit: reads view IDs and visible text only within Instagram, YouTube, TikTok, and Snapchat; no data is collected, stored remotely, or transmitted
- `BIND_ACCESSIBILITY_SERVICE` is a service-binding permission only — not declared in `<uses-permission>` (Play Protect requirement)

---

## Requirements

- Android 7.0+ (minSdk 24)
- Accessibility Service permission (granted manually in system settings)

---

## Installation

**From source:**

```bash
git clone https://github.com/Ashish-CodeJourney/MindGuard.git
cd MindGuard
./gradlew :androidApp:assembleDebug
# Install the APK at androidApp/build/outputs/apk/debug/
```

After installing a sideloaded APK on Android 13+:
1. Go to **Settings → Apps → MindGuard**
2. Tap ⋮ → **Allow restricted settings**
3. Go to **Settings → Accessibility → MindGuard** → enable the toggle

**Adding the Quick Settings tile:** long-press the notification shade → tap "Edit tiles" → drag **Focus Mode** into your active tiles.

---

## Development

### Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin (Multiplatform) |
| UI | Jetpack Compose + Material 3 |
| Async | Coroutines + Flow |
| Storage | DataStore Preferences |
| DI | Koin |
| Testing | Kotlin Test (TDD) |
| Build | Gradle Kotlin DSL |

### Running tests

```bash
./gradlew :shared:testDebugUnitTest
```

All production code in the shared module is written test-first. Tests live in `shared/src/commonTest/` and run on the JVM with no Android dependencies.

### Adding a new blocking rule

1. Implement `BlockingRule` in the shared module:

```kotlin
class NewAppRule : BlockingRule {
    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != "com.example.app") return noBlock()
        val hasPlayerResourceId = snapshot.resourceIds.any { id ->
            PLAYER_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) }
        }
        return if (hasPlayerResourceId)
            DetectionResult(shouldBlock = true, action = BlockAction.GO_BACK, reason = "...")
        else noBlock()
    }
}
```

2. Register it in `MindGuardAccessibilityService`:

```kotlin
RuleEngine(listOf(InstagramReelRule(), YouTubeShortsRule(), TikTokRule(), SnapchatSpotlightRule(), NewAppRule()))
```

3. Add the package name to `accessibility_config.xml`'s `android:packageNames` and to `SettingsDataStore`.

---

## Contributing

- Follow TDD: write a failing test before any production code
- Use conventional commits (`feat:`, `fix:`, `refactor:`, `test:`)
- Keep the shared module free of Android dependencies
- PRs welcome via GitHub Issues / Discussions

---

## License

MIT
