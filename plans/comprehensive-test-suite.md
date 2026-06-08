# Plan: Comprehensive Test Suite

**Branch**: feat/comprehensive-test-suite
**Status**: Active

## Goal

Achieve 100% behavioural coverage across unit, integration, and acceptance (Gherkin) test levels for every testable component in the shared module.

## Context

### What already exists (shared/commonTest)
| File | Status |
|---|---|
| `rules/InstagramReelRuleTest` | Good — but `CLICK_SAFE_TAB` not yet asserted |
| `rules/YouTubeShortsRuleTest` | Good |
| `rules/TikTokRuleTest` | Good |
| `rules/SnapchatSpotlightRuleTest` | Good |
| `rules/RuleEngineTest` | Good |
| `usecases/BlockCooldownTest` | Good |
| `usecases/DetectBlockedContentUseCaseTest` | Good |
| `usecases/FocusPauseLogicTest` | Good |
| `models/BlockActionTest` | **Stale** — missing `CLICK_SAFE_TAB` |
| `models/DetectionResultTest` | Structural only |
| `models/ScreenSnapshotTest` | Good |
| `models/SupportedAppTest` | **Stale** — only tests INSTAGRAM, misses YOUTUBE/TIKTOK/SNAPCHAT |
| `integration/DetectionIntegrationTest` | **Stale** — Instagram asserts `GO_BACK` (should be `CLICK_SAFE_TAB`) |

### What is missing
- Gherkin-style acceptance tests — none exist
- Integration tests for YouTube, TikTok, Snapchat paths through full pipeline
- Focus-schedule + pause-mode interaction integration
- Gherkin DSL helper (no external dependency needed — lightweight inline DSL)

### What is NOT testable in shared/commonTest
The following are Android-specific (AccessibilityService, DataStore, Compose) and require
instrumented tests or manual verification. They are out of scope for this plan:
- `MindGuardAccessibilityService` (accessibility events, rootInActiveWindow, global actions)
- `BlockActionExecutor` (performGlobalAction, findAccessibilityNodeInfosByViewId)
- `SettingsDataStore` (DataStore Preferences — requires Android Context)
- `FocusTileService` (TileService lifecycle)
- All Compose screens (Onboarding, Home, Permissions, Settings, Stats)

---

## Acceptance Criteria

- [ ] Every test file compiles and passes with `./gradlew :shared:testDebugUnitTest`
- [ ] `BlockActionTest` asserts all 4 actions including `CLICK_SAFE_TAB`
- [ ] `SupportedAppTest` asserts all 4 platforms (Instagram, YouTube, TikTok, Snapchat)
- [ ] `DetectionIntegrationTest` asserts `CLICK_SAFE_TAB` for Instagram, `GO_BACK` for others
- [ ] Integration tests cover YouTube, TikTok, and Snapchat full detection pipelines
- [ ] Integration tests cover focus-schedule and pause-mode scenarios
- [ ] A Gherkin DSL (`GherkinDsl.kt`) exists in commonTest with `feature/scenario/given/when/then/and`
- [ ] Acceptance feature files exist for: Instagram Reels, YouTube Shorts, TikTok, Snapchat Spotlight, Focus Schedule, Pause Mode, Multi-app, Per-app toggle
- [ ] No existing passing test is broken

---

## Steps

Every step follows RED → GREEN → MUTATE → KILL MUTANTS → REFACTOR.

---

### Step 1: Fix stale model tests (`BlockActionTest`, `SupportedAppTest`)

**RED**: Add a test `clickSafeTabActionExists()` in `BlockActionTest` asserting `CLICK_SAFE_TAB`
exists and `definesAllBlockActions()` now enumerates 4 values. In `SupportedAppTest`, add tests
`youtubeAppExists()`, `tiktokAppExists()`, `snapchatAppExists()`.

**GREEN**: These tests pass immediately because the production enum already has the values —
the stale tests just weren't asserting them.

**MUTATE**: Run mutation testing against `BlockAction.kt` and `SupportedApp.kt`.

**KILL MUTANTS**: Add any assertions needed to kill surviving enum-removal mutants.

**REFACTOR**: Remove `futureAppsCanBeAdded()` — it tests implementation count (fragile),
replace with individual behavioural assertions.

**Done when**: All 4 `BlockAction` values and all 4 `SupportedApp` values are individually asserted.

---

### Step 2: Fix stale integration test (`DetectionIntegrationTest`)

**RED**: Change the assertion in `fullDetectionFlowForReel()` from `assertEquals(BlockAction.GO_BACK, ...)` to
`assertEquals(BlockAction.CLICK_SAFE_TAB, ...)` — this test currently passes incorrectly because
`InstagramReelRule` now returns `CLICK_SAFE_TAB`.

Wait — actually the current test will **fail** if the existing integration test still asserts `GO_BACK`
and InstagramReelRule now returns `CLICK_SAFE_TAB`. Confirm it fails, then fix.

**GREEN**: Update all Instagram assertions in `DetectionIntegrationTest` to use `CLICK_SAFE_TAB`.

**MUTATE**: Run mutation testing against `DetectionIntegrationTest`.

**KILL MUTANTS**: Strengthen assertions where mutants survive.

**REFACTOR**: Extract a `instagramReelSnapshot()` factory function to reduce duplication.

**Done when**: `DetectionIntegrationTest` passes with correct actions.

---

### Step 3: Add integration tests for YouTube, TikTok, Snapchat pipelines

**RED**: In `DetectionIntegrationTest`, add:
- `fullDetectionFlowForYoutubeShorts()` — snapshot with `reel_watch_fragment_root` → `shouldBlock=true`, `action=GO_BACK`
- `fullDetectionFlowForTikTok()` — snapshot with "For You" text → `shouldBlock=true`, `action=GO_BACK`
- `fullDetectionFlowForSnapchatSpotlight()` — snapshot with "Spotlight" text → `shouldBlock=true`, `action=GO_BACK`
- `doesNotBlockUnknownApp()` — snapshot from `com.twitter.android` → `shouldBlock=false`
- `engineEvaluatesOnlyMatchingRule()` — Instagram snapshot only triggers `InstagramReelRule`, not YouTube/TikTok/Snapchat

These fail because the tests don't exist.

**GREEN**: Write the test bodies. No production code changes needed.

**MUTATE**: Run mutation testing against the integration test module.

**KILL MUTANTS**: Tighten assertions; add reason-string checks where mutants survive on `reason` field.

**REFACTOR**: Consolidate snapshot factory functions into a `SnapshotFactory` object shared across the integration test.

**Done when**: All 4 detection pipelines have integration coverage.

---

### Step 4: Add focus-schedule and pause-mode integration tests

**RED**: Add a new file `FocusModeIntegrationTest.kt` in `integration/` with:
- `isPausedBlockingDelegatesToPauseLogic()` — verifies `isPausedAt(pauseUntil, now)` returns the correct value for each boundary (0, before, at, after)
- `pauseAndDetectionAreIndependent()` — pause logic does not affect `RuleEngine.evaluate()` (the rule engine has no knowledge of pause state)
- `cooldownRemainsActiveAcrossMultipleDetectionCalls()` — 10 rapid calls to `DetectBlockedContentUseCase.execute()` with a shared `BlockCooldown(500)` produce exactly 1 block in the first 500ms window

These fail because the file doesn't exist.

**GREEN**: Write the test bodies using existing shared-module classes.

**MUTATE**: Run mutation testing.

**KILL MUTANTS**: Address surviving mutants on boundary conditions.

**REFACTOR**: Assess whether `FocusModeIntegrationTest` should merge into `DetectionIntegrationTest` or stay separate (keep separate — distinct concern).

**Done when**: All focus/pause integration scenarios pass.

---

### Step 5: Introduce the Gherkin DSL helper

**RED**: Create `GherkinDslTest.kt` in `commonTest` that uses a `feature { scenario { given {} when {} then {} } }` DSL and contains one trivial passing assertion. The file fails to compile until the DSL is created.

**GREEN**: Create `GherkinDsl.kt` in `commonTest/kotlin/com/mindguard/shared/acceptance/` with:

```kotlin
fun feature(description: String, block: FeatureContext.() -> Unit) =
    FeatureContext(description).block()

class FeatureContext(val name: String) {
    fun scenario(description: String, block: ScenarioContext.() -> Unit) =
        ScenarioContext(name, description).block()
}

class ScenarioContext(val feature: String, val name: String) {
    fun given(description: String, block: () -> Unit) = block()
    fun `when`(description: String, block: () -> Unit) = block()
    fun then(description: String, block: () -> Unit) = block()
    fun and(description: String, block: () -> Unit) = block()
    fun but(description: String, block: () -> Unit) = block()
}
```

No production code. DSL lives entirely in `commonTest`.

**MUTATE**: N/A — pure test infrastructure, no branching logic to mutate.

**REFACTOR**: Ensure DSL is purely a readability wrapper; it must not swallow assertion exceptions.

**Done when**: `GherkinDslTest.kt` compiles and the one trivial scenario passes.

---

### Step 6: Acceptance tests — Feature: Instagram Reels blocking

**RED**: Create `acceptance/InstagramReelsFeatureTest.kt` with 5 `@Test` functions, each wrapping a `feature { scenario { ... } }` block:

1. `reelPlayerIsBlockedImmediately` — player resource ID present → `shouldBlock=true`, `action=CLICK_SAFE_TAB`
2. `homeFeedIsNeverBlocked` — `feed_pager` resource ID present → `shouldBlock=false`
3. `reelsNavTabAloneDoesNotBlock` — only `reels_tab` resource ID → `shouldBlock=false`
4. `differentAppIsNeverBlocked` — same resource IDs, package `com.twitter.android` → `shouldBlock=false`
5. `multiplePlayerResourceIdsBlockImmediately` — `reel_pager` + `clips_viewer_view_pager` → `shouldBlock=true`

All fail because the file doesn't exist.

**GREEN**: Write the test bodies using `InstagramReelRule` directly.

**MUTATE**: Run mutation testing against `InstagramReelRule.kt`.

**KILL MUTANTS**: Add any missing assertions.

**REFACTOR**: Ensure scenario descriptions read as product requirements, not code descriptions.

**Done when**: All 5 Instagram acceptance scenarios pass.

---

### Step 7: Acceptance tests — Feature: YouTube Shorts blocking

**RED**: Create `acceptance/YouTubeShortsFeatureTest.kt` with 5 scenarios:

1. `shortsPlayerIsBlockedImmediately` — `reel_watch_fragment_root` → block
2. `progressBarAloneTriggersBlock` — `reel_progress_bar` → block (unique to Shorts player)
3. `homeScreenWithShortsTabIsNotBlocked` — `shorts_pivot_tab_label` alone → no block
4. `regularVideoWatchScreenIsNotBlocked` — `watch_player` resource ID → no block
5. `resourceIdMatchIsCaseInsensitive` — `REEL_WATCH_FRAGMENT_ROOT` (uppercase) → block

All fail because the file doesn't exist.

**GREEN**: Write test bodies using `YouTubeShortsRule`.

**MUTATE**: Run mutation testing against `YouTubeShortsRule.kt`.

**KILL MUTANTS**: Address surviving mutants.

**REFACTOR**: Assess.

**Done when**: All 5 YouTube acceptance scenarios pass.

---

### Step 8: Acceptance tests — Feature: TikTok and Snapchat blocking

**RED**: Create `acceptance/TikTokFeatureTest.kt` with 4 scenarios and `acceptance/SnapchatSpotlightFeatureTest.kt` with 4 scenarios:

TikTok:
1. `forYouFeedIsBlocked` — "For You" text in global package → block
2. `allThreePackageVariantsAreMonitored` — same text in trill/aweme packages → block
3. `settingsScreenIsNotBlocked` — settings resource IDs → no block
4. `feedResourceIdAloneTriggersBlock` — `feed_video_container` resource ID → block

Snapchat:
1. `spotlightFeedIsBlocked` — `spotlight_feed` resource ID → block
2. `spotlightTextTriggersBlock` — "Spotlight" text → block
3. `cameraScreenIsNotBlocked` — camera resource IDs → no block
4. `otherAppsNotBlocked` — Snapchat text in Instagram package → no block

**GREEN**: Write test bodies.

**MUTATE**: Run against `TikTokRule.kt` and `SnapchatSpotlightRule.kt`.

**KILL MUTANTS**: Address surviving mutants, particularly on package name checks.

**REFACTOR**: Assess.

**Done when**: All 8 scenarios across TikTok and Snapchat pass.

---

### Step 9: Acceptance tests — Feature: Focus schedule and pause mode

**RED**: Create `acceptance/FocusModeFeatureTest.kt` with 6 scenarios:

1. `protectionIsNotPausedByDefault` — `isPausedAt(0L, now)` → false
2. `protectionIsPausedDuringPauseWindow` — `isPausedAt(now + 30min, now)` → true
3. `protectionResumesAfterPauseExpires` — `isPausedAt(now - 1ms, now)` → false
4. `pauseExpiresAtExactDeadline` — `isPausedAt(now, now)` → false
5. `cooldownPreventsRapidReblocking` — 10 events in 400ms with 500ms cooldown → 1 block total
6. `cooldownResetsAfterWindow` — block at t=0, canBlock at t=501ms → true

**GREEN**: Write test bodies using `isPausedAt` and `BlockCooldown` directly.

**MUTATE**: Run against `FocusPauseLogic.kt` and `BlockCooldown.kt`.

**KILL MUTANTS**: Add boundary condition assertions.

**REFACTOR**: Assess.

**Done when**: All 6 focus-mode acceptance scenarios pass.

---

### Step 10: Acceptance tests — Feature: Multi-app and per-app toggle behaviour

**RED**: Create `acceptance/MultiAppFeatureTest.kt` with 5 scenarios:

1. `allAppsAreMonitoredByDefault` — Instagram, YouTube, TikTok, Snapchat snapshots each → all block
2. `ruleEngineShortCircuitsOnFirstMatch` — Instagram snapshot, 4 rules → only first matching rule executes
3. `unknownAppIsNeverBlocked` — `com.twitter.android` snapshot with any signals → no block from any rule
4. `rulesAreIndependent` — YouTube snapshot → only YouTubeShortsRule fires, not Instagram rule
5. `emptySnapshotBlocksNoApp` — empty resource IDs, all app packages → no blocks

**GREEN**: Write test bodies using `RuleEngine` with all 4 rules registered.

**MUTATE**: Run against `RuleEngine.kt`.

**KILL MUTANTS**: Address surviving mutants on short-circuit logic.

**REFACTOR**: Assess.

**Done when**: All 5 multi-app scenarios pass.

---

### Step 11: Coverage verification and gap-fill

**RED**: Run `./gradlew :shared:testDebugUnitTest` and review which production lines are uncovered.
For each uncovered path, write a minimal failing test targeting that behaviour.

Known gaps to check:
- `DetectionResult` equality / copy (data class behaviour)
- `ScreenSnapshot` — blank-only package name (spaces) rejected
- `BlockCooldown` — 0ms cooldown, Long.MAX_VALUE timestamp
- `RuleEngine` — snapshot with null-like edge data
- `DetectBlockedContentUseCase` — logger receives package name even when reason is null

**GREEN**: Write and pass each gap test.

**MUTATE**: Final mutation testing pass across entire shared module.

**KILL MUTANTS**: Address all surviving mutants. Ask if a mutant's value is ambiguous.

**REFACTOR**: Final pass — remove any test duplication introduced across steps, ensure all factory helpers are DRY.

**Done when**: No untested production lines remain in `shared/commonMain`.

---

## Pre-PR Quality Gate

Before each PR:
1. `./gradlew :shared:testDebugUnitTest` — all tests pass
2. `./gradlew :androidApp:assembleDebug` — Android build not broken
3. Mutation testing — run `mutation-testing` skill on shared module
4. No test tests implementation details (no spy on internal methods, no checking field names)
5. All scenario descriptions read as product requirements, not code descriptions

---

## File map — new files to create

```
shared/src/commonTest/kotlin/com/mindguard/shared/
├── acceptance/
│   ├── GherkinDsl.kt                    (Step 5)
│   ├── GherkinDslTest.kt                (Step 5)
│   ├── InstagramReelsFeatureTest.kt     (Step 6)
│   ├── YouTubeShortsFeatureTest.kt      (Step 7)
│   ├── TikTokFeatureTest.kt             (Step 8)
│   ├── SnapchatSpotlightFeatureTest.kt  (Step 8)
│   ├── FocusModeFeatureTest.kt          (Step 9)
│   └── MultiAppFeatureTest.kt           (Step 10)
└── integration/
    └── FocusModeIntegrationTest.kt      (Step 4)
```

## Files to modify

```
shared/src/commonTest/kotlin/com/mindguard/shared/
├── models/BlockActionTest.kt            (Step 1)
├── models/SupportedAppTest.kt           (Step 1)
└── integration/DetectionIntegrationTest.kt (Step 2 + 3)
```

---
*Delete this file when the plan is complete. If `plans/` is empty, delete the directory.*
