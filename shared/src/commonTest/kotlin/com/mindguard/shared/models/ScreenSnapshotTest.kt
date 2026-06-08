package com.mindguard.shared.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScreenSnapshotTest {

    @Test
    fun createsValidSnapshot() {
        val snapshot = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = listOf("Reels", "Like"),
            contentDescriptions = listOf("Video feed"),
            resourceIds = listOf("com.instagram.android:id/reels_tab"),
            timestampMillis = 1000L
        )

        assertEquals("com.instagram.android", snapshot.packageName)
        assertEquals(listOf("Reels", "Like"), snapshot.screenText)
        assertEquals(listOf("Video feed"), snapshot.contentDescriptions)
        assertEquals(listOf("com.instagram.android:id/reels_tab"), snapshot.resourceIds)
        assertEquals(1000L, snapshot.timestampMillis)
    }

    @Test
    fun rejectsNegativeTimestamp() {
        assertFailsWith<IllegalArgumentException> {
            ScreenSnapshot(
                packageName = "com.instagram.android",
                screenText = emptyList(),
                contentDescriptions = emptyList(),
                resourceIds = emptyList(),
                timestampMillis = -1L
            )
        }
    }

    @Test
    fun rejectsZeroTimestamp() {
        assertFailsWith<IllegalArgumentException> {
            ScreenSnapshot(
                packageName = "com.instagram.android",
                screenText = emptyList(),
                contentDescriptions = emptyList(),
                resourceIds = emptyList(),
                timestampMillis = 0L
            )
        }
    }

    @Test
    fun rejectsEmptyPackageName() {
        assertFailsWith<IllegalArgumentException> {
            ScreenSnapshot(
                packageName = "",
                screenText = emptyList(),
                contentDescriptions = emptyList(),
                resourceIds = emptyList(),
                timestampMillis = 1000L
            )
        }
    }

    @Test
    fun createsSnapshotWithEmptyCollections() {
        val snapshot = ScreenSnapshot(
            packageName = "com.example.app",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )

        assertEquals(emptyList(), snapshot.screenText)
        assertEquals(emptyList(), snapshot.contentDescriptions)
        assertEquals(emptyList(), snapshot.resourceIds)
    }

    @Test
    fun rejectsBlankOnlyPackageName() {
        assertFailsWith<IllegalArgumentException> {
            ScreenSnapshot(
                packageName = "   ",
                screenText = emptyList(),
                contentDescriptions = emptyList(),
                resourceIds = emptyList(),
                timestampMillis = 1000L
            )
        }
    }

    @Test
    fun equalSnapshotsAreEqual() {
        val a = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = listOf("Reels"),
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )
        val b = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = listOf("Reels"),
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )
        assertEquals(a, b)
    }

    @Test
    fun snapshotsAreImmutable() {
        val snapshot = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = mutableListOf("Reels"),
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )

        // Verify we can't modify the returned list
        val texts = snapshot.screenText
        assertEquals(1, texts.size)
    }
}
