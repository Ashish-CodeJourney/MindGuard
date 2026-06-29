package com.mindguard.accessibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InstagramHomeTabStrategyTest {

    @Test
    fun prefersHigherPriorityIdWhenMultiplePresent() {
        val ids = setOf(
            "com.instagram.android:id/tab_icon_0",
            "com.instagram.android:id/feed_tab"
        )
        assertEquals("com.instagram.android:id/feed_tab", InstagramHomeTabStrategy.selectId(ids))
    }

    @Test
    fun selectsFeedTabWhenOnlyFeedTabPresent() {
        val ids = setOf("com.instagram.android:id/feed_tab")
        assertEquals("com.instagram.android:id/feed_tab", InstagramHomeTabStrategy.selectId(ids))
    }

    @Test
    fun selectsTabFeedAsFallback() {
        val ids = setOf("com.instagram.android:id/tab_feed")
        assertEquals("com.instagram.android:id/tab_feed", InstagramHomeTabStrategy.selectId(ids))
    }

    @Test
    fun selectsTabIcon0AsLastResort() {
        val ids = setOf("com.instagram.android:id/tab_icon_0")
        assertEquals("com.instagram.android:id/tab_icon_0", InstagramHomeTabStrategy.selectId(ids))
    }

    @Test
    fun returnsNullWhenNoKnownIdPresent() {
        val ids = setOf("com.instagram.android:id/profile_tab", "com.instagram.android:id/reels_tab")
        assertNull(InstagramHomeTabStrategy.selectId(ids))
    }

    @Test
    fun returnsNullForEmptySet() {
        assertNull(InstagramHomeTabStrategy.selectId(emptySet()))
    }

    @Test
    fun orderedListContainsAllKnownIds() {
        val known = setOf(
            "com.instagram.android:id/feed_tab",
            "com.instagram.android:id/tab_feed",
            "com.instagram.android:id/home_tab",
            "com.instagram.android:id/navigation_home",
            "com.instagram.android:id/tab_bar_home_button",
            "com.instagram.android:id/tab_icon_0"
        )
        assertEquals(6, InstagramHomeTabStrategy.ORDERED_IDS.size)
        assertEquals(known, InstagramHomeTabStrategy.ORDERED_IDS.toSet())
    }
}
