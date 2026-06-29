package com.mindguard.accessibility

object InstagramHomeTabStrategy {

    // Priority-ordered list of known Instagram home-tab resource IDs across builds (2022–2025).
    // Earlier entries are preferred; the list is exhaustive enough to cover most builds.
    val ORDERED_IDS = listOf(
        "com.instagram.android:id/feed_tab",
        "com.instagram.android:id/tab_feed",
        "com.instagram.android:id/home_tab",
        "com.instagram.android:id/navigation_home",
        "com.instagram.android:id/tab_bar_home_button",
        "com.instagram.android:id/tab_icon_0"
    )

    fun selectId(availableIds: Set<String>): String? =
        ORDERED_IDS.firstOrNull { it in availableIds }
}
