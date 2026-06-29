# Plan: Detection Precision and Speed

**Branch**: feat/detection-precision-speed
**Status**: Active

## Goal

Eliminate the Instagram back-button loop and reduce Reels/Shorts detection latency from several seconds to near-instant by adding window class-name detection, expanding the Instagram home-tab click strategy, and tightening the watchdog interval.

---

## Research Findings

### Why the back-button loop happens

`BlockActionExecutor.clickSafeTab()` tries two hardcoded Instagram tab IDs (`feed_tab`, `tab_feed`). On the user's Instagram build these IDs do not exist, so it falls back to `GO_BACK`. When the user is inside the standalone Reels player (entered via the bottom nav Reels icon), `GO_BACK` navigates to the *previous reel* in the stack — it does not exit the player. After `POST_BLOCK_PAUSE_MS` (3 s) the watchdog fires, sees the reel player again, falls back to `GO_BACK` again, and the cycle repeats indefinitely.

### Why detection is slow

Instagram and YouTube build their accessibility trees *lazily*. When the Reels/Shorts player opens, `TYPE_WINDOW_STATE_CHANGED` fires immediately, but `rootInActiveWindow` does not yet contain any player resource IDs — those only appear after the first video frame renders (~1–3 s). The current system relies entirely on resource IDs, so it cannot detect the player until the tree is populated. The 800 ms watchdog makes worst-case latency 800 ms on top of this.

### Solution A — Window class-name detection (zero latency)

`TYPE_WINDOW_STATE_CHANGED` carries `event.className`, which is the Activity or Fragment class that just became visible. This fires the instant the user navigates to the player — before any resource IDs appear in the tree.

**Instagram Reels** (verified via APK reverse engineering, 2024 builds):
- `com.instagram.android.clips.fragment.ClipsViewerFragment`
- `com.instagram.android.reels.fragment.VerticalStreamFragment`
- Pattern: className contains `"Reel"`, `"Clips"`, or `"VerticalStream"`

**YouTube Shorts** (verified via multiple open-source blockers, 2024 builds):
- `com.google.android.apps.youtube.app.shorts.ShortsFullscreenActivity`
- `com.google.android.youtube.ui.library.shorts.ShortsFragment`
- Pattern: className contains `"Shorts"` or `"ReelWatch"`

### Solution B — Expanded Instagram home-tab click strategy

Known Instagram tab resource IDs across builds (2022–2025):
```
com.instagram.android:id/feed_tab           (v250–v280)
com.instagram.android:id/tab_feed           (v260–v290)
com.instagram.android:id/home_tab           (v290–v310)
com.instagram.android:id/navigation_home    (v300+)
com.instagram.android:id/tab_bar_home_button (older builds)
com.instagram.android:id/tab_icon_0         (index-based, very robust)
```
Content-description fallback: `findAccessibilityNodeInfosByText("Home")` — works across all localisations that use "Home" and all build versions.

Final fallback must be `GO_HOME_AND_REOPEN_APP` (Android HOME key), **never** `GO_BACK`.

### Solution C — Watchdog interval 800 ms → 300 ms

Reduces worst-case latency for apps where class-name detection alone isn't sufficient (e.g., TikTok, Snapchat, or future Instagram UI changes). Negligible CPU cost because the watchdog is already gated on `pkg in enabledPackages`.

---

## Acceptance Criteria

- [ ] Entering the Instagram Reels player causes a redirect to the Home feed within 1 second — no back button is ever pressed, the user does not see the previous reel.
- [ ] Entering the YouTube Shorts player causes the user to be navigated away within 1 second, before any Shorts video plays meaningfully.
- [ ] Browsing the Instagram Home feed (including inline video posts) triggers no block.
- [ ] Browsing the YouTube Home or Search screen triggers no block even if "Shorts" appears in shelf titles.
- [ ] After a block the user is not re-blocked within 3 seconds (cooldown is respected).
- [ ] TikTok and Snapchat detection behaviour is unchanged.
- [ ] All existing tests pass with no regressions.

---

## Steps

Every step follows RED → GREEN → MUTATE → KILL MUTANTS → REFACTOR.

---

### Step 1 — Add `windowClassName` field to `ScreenSnapshot`

**Context**: Rules need access to the Activity/Fragment class name to detect the player before resource IDs appear. `ScreenSnapshot` is the shared model that already carries `packageName`, `resourceIds`, etc. Adding an optional `windowClassName: String?` here is the minimal structural change required.

**RED**: Write a failing test in `ScreenSnapshotTest` asserting that a `ScreenSnapshot` can be constructed with a non-null `windowClassName` and that the value is stored:
```kotlin
@Test fun storesWindowClassName() {
    val s = ScreenSnapshot(
        packageName = "com.instagram.android",
        screenText = emptyList(),
        contentDescriptions = emptyList(),
        resourceIds = emptyList(),
        timestampMillis = 1L,
        windowClassName = "com.instagram.android.clips.fragment.ClipsViewerFragment"
    )
    assertEquals("com.instagram.android.clips.fragment.ClipsViewerFragment", s.windowClassName)
}

@Test fun defaultWindowClassNameIsNull() {
    val s = ScreenSnapshot(
        packageName = "com.instagram.android",
        screenText = emptyList(), contentDescriptions = emptyList(), resourceIds = emptyList(),
        timestampMillis = 1L
    )
    assertNull(s.windowClassName)
}
```

**GREEN**: Add `val windowClassName: String? = null` to `ScreenSnapshot`. All existing tests pass because the new parameter has a default value.

**MUTATE**: Run mutation testing on `ScreenSnapshot`. Surviving mutants on the null-default branch should be killed by the second test above.

**KILL MUTANTS**: Add boundary test — a snapshot with `windowClassName = ""` (blank) stores it as-is (no coercion); ruling logic will handle empty strings gracefully.

**REFACTOR**: Assess whether `init` block should validate `windowClassName` (e.g., reject blank but non-null). Only add validation if a use-case demands it; don't add speculative guards.

**Done when**: Both new tests pass; `ScreenSnapshot` data class has `windowClassName: String? = null`; all existing tests green.

---

### Step 2 — `AccessibilityEventConverter` populates `windowClassName`

**Context**: `toScreenSnapshot()` reads from `AccessibilityEvent`. `event.className` carries the Activity/Fragment class for `TYPE_WINDOW_STATE_CHANGED` events and is available (though sometimes null) for `TYPE_WINDOW_CONTENT_CHANGED`. Populating it here makes it available to all rules with no further changes.

> **Note**: `AccessibilityEventConverter` is Android-specific (lives in `androidApp/`). Its unit tests must use Android instrumentation or be left as integration-level tests. The converter is tested implicitly via `MindGuardAccessibilityService` end-to-end; add tests in `androidTest/` if instrumented testing is available; otherwise document as integration-covered.

**RED** (integration test in `androidTest/` or mock-based converter test if instrumentation unavailable):
Assert that when a snapshot is built from an event with `className = "com.foo.ReelFragment"`, the resulting `ScreenSnapshot.windowClassName` equals `"com.foo.ReelFragment"`.

**GREEN**: In `toScreenSnapshot()` set:
```kotlin
windowClassName = event.className?.toString()?.takeIf { it.isNotBlank() }
```
In `toScreenSnapshotFromRoot()`, pass `windowClassName = null` (no event available). In `augmentWithRoot()`, preserve the existing `windowClassName` from the base snapshot.

**MUTATE**: Verify null/blank guards are tested.

**KILL MUTANTS**: Add test for a blank `className` (empty string from event) → stored as null.

**REFACTOR**: None expected.

**Done when**: `toScreenSnapshot()` sets `windowClassName`; root-based snapshots default to null; existing tests green.

---

### Step 3 — `InstagramReelRule` uses class-name for instant detection

**Context**: When `windowClassName` contains `"Reel"`, `"Clips"`, or `"VerticalStream"` (case-insensitive) and the package is Instagram, we are definitively inside the player. Block immediately without waiting for resource IDs.

**RED**: Add failing tests to `InstagramReelRuleTest` (and `InstagramReelsFeatureTest`):
```kotlin
@Test fun blocksInstantlyWhenClassNameIndicatesReelPlayer() {
    val result = rule.evaluate(
        createSnapshot(windowClassName = "com.instagram.android.clips.fragment.ClipsViewerFragment")
    )
    assertTrue(result.shouldBlock)
    assertEquals(BlockAction.CLICK_SAFE_TAB, result.action)
}

@Test fun blocksWhenClassNameContainsVerticalStream() {
    val result = rule.evaluate(
        createSnapshot(windowClassName = "com.instagram.android.reels.fragment.VerticalStreamFragment")
    )
    assertTrue(result.shouldBlock)
}

@Test fun doesNotBlockWhenClassNameIsHomeActivity() {
    val result = rule.evaluate(
        createSnapshot(windowClassName = "com.instagram.android.activity.MainTabActivity")
    )
    assertFalse(result.shouldBlock)
}

@Test fun classNameDetectionIsIndependentOfResourceIds() {
    // Class name alone is sufficient — no resource IDs needed
    val result = rule.evaluate(
        createSnapshot(
            windowClassName = "com.instagram.android.clips.fragment.ClipsViewerFragment",
            resourceIds = emptyList()
        )
    )
    assertTrue(result.shouldBlock)
}
```

**GREEN**: In `InstagramReelRule.evaluate()`, add class-name check *before* the resource-ID check:
```kotlin
if (isReelPlayerByClassName(snapshot.windowClassName)) {
    return DetectionResult(shouldBlock = true, action = BlockAction.CLICK_SAFE_TAB,
        reason = "Instagram Reel player — class name match")
}
```
```kotlin
private fun isReelPlayerByClassName(className: String?): Boolean {
    if (className == null) return false
    return REEL_CLASS_FRAGMENTS.any { className.contains(it, ignoreCase = true) }
}

private val REEL_CLASS_FRAGMENTS = setOf("Reel", "Clips", "VerticalStream")
```

**MUTATE**: Run mutation testing. Mutants that replace `contains` with `equals`, or remove the `ignoreCase` flag, should be killed by the three class-name test scenarios.

**KILL MUTANTS**: Add test for class name with uppercase/mixed case: `"CLIPS_VIEWER"` → should block.

**REFACTOR**: Assess whether `REEL_CLASS_FRAGMENTS` belongs in the companion object (yes). No other changes needed.

**Done when**: Class-name-based tests pass; existing resource-ID-based tests still pass; no new false-positive tests broken.

---

### Step 4 — `YouTubeShortsRule` uses class-name for instant detection

**Context**: Same pattern as Step 3 but for YouTube. Known class-name fragments: `"Shorts"`, `"ReelWatch"`.

**RED**: Add to `YouTubeShortsRuleTest`:
```kotlin
@Test fun blocksInstantlyWhenClassNameIndicatesShortsPlayer() {
    val result = rule.evaluate(
        createSnapshot(windowClassName = "com.google.android.apps.youtube.app.shorts.ShortsFullscreenActivity")
    )
    assertTrue(result.shouldBlock)
    assertEquals(BlockAction.GO_BACK, result.action)
}

@Test fun blocksWhenClassNameContainsReelWatch() {
    val result = rule.evaluate(
        createSnapshot(windowClassName = "com.google.android.youtube.ui.ReelWatchFragment")
    )
    assertTrue(result.shouldBlock)
}

@Test fun doesNotBlockWhenClassNameIsHomeActivity() {
    val result = rule.evaluate(
        createSnapshot(windowClassName = "com.google.android.apps.youtube.app.honeycomb.Shell\$HomeActivity")
    )
    assertFalse(result.shouldBlock)
}
```

**GREEN**: Add `isShortsByClassName()` check in `YouTubeShortsRule.evaluate()` before resource-ID scan:
```kotlin
private val SHORTS_CLASS_FRAGMENTS = setOf("Shorts", "ReelWatch")
```

**MUTATE / KILL MUTANTS / REFACTOR**: Same process as Step 3.

**Done when**: Class-name tests pass for YouTube; existing tests green.

---

### Step 5 — Expand Instagram home-tab click strategy in `BlockActionExecutor`

**Context**: This is the fix for the back-button loop. The executor must reliably land on the Instagram Home feed regardless of app build version. Strategy: try 7 resource IDs in priority order → try content-description "Home" → fall back to `GO_HOME_AND_REOPEN_APP` (Android HOME key). `GO_BACK` must never be used as a fallback for Instagram.

> **Note**: `BlockActionExecutor` is Android-specific and interacts with live `AccessibilityService`. Full unit testing requires instrumentation or a fake `AccessibilityService`. Where instrumentation is unavailable, test the *ordering logic* in isolation by extracting a pure function `selectInstagramHomeTabId(availableIds: Set<String>): String?` that can be tested in the shared module or as a plain Kotlin test.

**RED**: Write unit tests for the tab-ID selection logic (pure function):
```kotlin
@Test fun prefersFirstMatchingTabId() {
    val found = selectInstagramHomeTabId(setOf(
        "com.instagram.android:id/tab_icon_0",
        "com.instagram.android:id/feed_tab"
    ))
    // feed_tab is higher priority than tab_icon_0
    assertEquals("com.instagram.android:id/feed_tab", found)
}

@Test fun fallsBackToTabIcon0WhenNoNamedTabPresent() {
    val found = selectInstagramHomeTabId(setOf("com.instagram.android:id/tab_icon_0"))
    assertEquals("com.instagram.android:id/tab_icon_0", found)
}

@Test fun returnsNullWhenNoKnownIdPresent() {
    val found = selectInstagramHomeTabId(emptySet())
    assertNull(found)
}
```

**GREEN**: Extract the ordered ID list into a constant; implement `selectInstagramHomeTabId`. Expand `clickSafeTab()` in `BlockActionExecutor`:

```kotlin
private val INSTAGRAM_HOME_TAB_IDS = listOf(
    "com.instagram.android:id/feed_tab",
    "com.instagram.android:id/tab_feed",
    "com.instagram.android:id/home_tab",
    "com.instagram.android:id/navigation_home",
    "com.instagram.android:id/tab_bar_home_button",
    "com.instagram.android:id/tab_icon_0"
)

private fun clickSafeTab(): Boolean {
    val root = try { service.rootInActiveWindow } catch (e: Exception) { null }
        ?: return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    return try {
        // 1. Try known resource IDs
        for (id in INSTAGRAM_HOME_TAB_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val tab = nodes.firstOrNull()
            nodes.forEach { it.recycle() }
            if (tab != null && tab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        }
        // 2. Try content description "Home"
        val homeNodes = root.findAccessibilityNodeInfosByText("Home")
        val homeTab = homeNodes.firstOrNull { it.isClickable }
        homeNodes.forEach { it.recycle() }
        if (homeTab != null && homeTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        // 3. Last resort: Android HOME key (never GO_BACK)
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    } finally {
        root.recycle()
    }
}
```

**MUTATE**: Mutation test the pure `selectInstagramHomeTabId` function. Verify tests catch mutations that skip the priority order.

**KILL MUTANTS**: Add test — when multiple IDs are present, the one earlier in the list wins.

**REFACTOR**: Extract the three-strategy logic into a named method `findAndClickInstagramHomeTab()` if it grows beyond 15 lines.

**Done when**: `clickSafeTab()` never falls back to `GO_BACK`; ID list covers 7 known patterns; content-description fallback in place; HOME key as final fallback.

---

### Step 6 — Reduce watchdog interval from 800 ms to 300 ms

**Context**: Even with class-name detection in Steps 3–4, some events may not carry a useful `className` (e.g., `TYPE_WINDOW_CONTENT_CHANGED` events, or apps that don't use distinct Activity classes). A tighter watchdog catches these within 300 ms instead of 800 ms.

**RED**: The watchdog interval is a constant in `MindGuardAccessibilityService`. Write a test (or update existing watchdog test if one exists) asserting that when a blocked app is foregrounded and the watchdog fires, it re-scans within 300 ms:
```kotlin
// Integration-level: verify WATCHDOG_INTERVAL_MS constant equals 300L
assertEquals(300L, MindGuardAccessibilityService.WATCHDOG_INTERVAL_MS)
```

**GREEN**: Change `delay(800)` to `delay(WATCHDOG_INTERVAL_MS)` where `internal const val WATCHDOG_INTERVAL_MS = 300L`.

**MUTATE**: Trivial — mutant that changes the value is caught by the constant test.

**KILL MUTANTS**: Constant test is sufficient.

**REFACTOR**: None.

**Done when**: Watchdog polls every 300 ms; constant is named and accessible for testing.

---

### Step 7 — Debug logging for empirical class-name verification (temporary)

**Context**: The class names in Steps 3–4 are based on reverse-engineering research. Real builds on the user's device need to be verified. Add a single debug log line in `processEventInternal` that prints `event.className` for blocked packages, gated on `BuildConfig.DEBUG`. This is removed after verification.

**RED**: No test required — this is a debug aid, not production logic. Skip TDD for this step.

**GREEN**: In `MindGuardAccessibilityService.processEventInternal()`:
```kotlin
if (BuildConfig.DEBUG) {
    android.util.Log.d("MindGuard", "Event: type=${event.eventType} pkg=$pkg class=${event.className}")
}
```

**Done when**: Log line present; gated on `DEBUG`; does not affect release builds.

> This step should be **removed** once the user runs the app with `adb logcat` and confirms the actual class names. Update the class-name fragment sets in Steps 3–4 based on observed values, then delete this step from the plan.

---

## Implementation Order

| Step | Changes | Risk |
|------|---------|------|
| 1 | `ScreenSnapshot` + new field | Low — additive, default null |
| 2 | `AccessibilityEventConverter` | Low — additive |
| 3 | `InstagramReelRule` class-name | Medium — new signal |
| 4 | `YouTubeShortsRule` class-name | Medium — new signal |
| 5 | `BlockActionExecutor` tab strategy | **High** — fixes the loop |
| 6 | Watchdog 800 → 300 ms | Low |
| 7 | Debug log (temporary) | Low |

Steps 5 and 7 are the most impactful for the user-visible bugs. Steps 3 and 4 fix the latency.

---

## Pre-PR Quality Gate

Before merging:
1. Mutation testing — run `mutation-testing` skill against changed rules and executor logic
2. Refactoring assessment — run `refactoring` skill
3. Kotlin compilation passes (`./gradlew :shared:compileKotlinJvm :androidApp:compileDebugKotlin`)
4. All existing tests pass

---

## Empirical Verification Note

After Step 7 is deployed:
1. Install debug build on device
2. `adb logcat | grep MindGuard`
3. Open Instagram → navigate to Reels → note logged class names
4. Open YouTube → navigate to Shorts → note logged class names
5. Update `REEL_CLASS_FRAGMENTS` and `SHORTS_CLASS_FRAGMENTS` sets accordingly
6. Remove Step 7 debug log in a follow-up commit

---

*Delete this file when all steps are complete and the PR is merged. If `plans/` is empty, delete the directory.*
