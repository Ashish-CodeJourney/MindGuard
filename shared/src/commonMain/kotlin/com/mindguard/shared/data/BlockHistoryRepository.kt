package com.mindguard.shared.data

import com.mindguard.shared.models.BlockAction

interface BlockHistoryRepository {
    suspend fun recordBlock(
        packageName: String,
        appName: String?,
        action: BlockAction,
        reason: String?,
        timestampMillis: Long
    )

    suspend fun getBlocksSince(timestampMillis: Long): List<BlockEvent>
    suspend fun getBlocksByPackage(packageName: String): List<BlockEvent>
    suspend fun countBlocksToday(todayStartMillis: Long): Long
    suspend fun deleteOldBlocks(beforeMillis: Long)
}

data class BlockEvent(
    val id: Long,
    val packageName: String,
    val appName: String?,
    val blockAction: String,
    val reason: String?,
    val timestampMillis: Long
)
