package com.nisaefendioglu.pawtiva.core.model

/** Long-term growth (XP, level) and spendable currency (PawCoins), kept separate on purpose. */
data class Progress(
    val totalXp: Int = 0,
    val level: Int = 1,
    val xpIntoLevel: Int = 0,
    val xpPerLevel: Int = 100,
    val balance: Int = 0,
    val totalCoinsEarned: Int = 0,
) {
    val levelFraction: Float get() = xpIntoLevel.toFloat() / xpPerLevel
}

/** What one successful completion changed. */
data class CompletionReward(
    val xpGained: Int,
    val coinsGained: Int,
    val levelBefore: Int,
    val levelAfter: Int,
    val completedTheDay: Boolean,
    val unlockedAchievements: List<AchievementId>,
) {
    val leveledUp: Boolean get() = levelAfter > levelBefore
}

sealed interface CompleteOutcome {
    data class Completed(val reward: CompletionReward) : CompleteOutcome
    data object AlreadyCompleted : CompleteOutcome
    data object DateNotEditable : CompleteOutcome
    data object HabitNotFound : CompleteOutcome
}

sealed interface UndoOutcome {
    data class Undone(val xpRemoved: Int, val coinsRemoved: Int, val levelAfter: Int) : UndoOutcome
    data object NotCompleted : UndoOutcome
    data object DateNotEditable : UndoOutcome
}

/** Achievements; [storageId] is persisted and never renamed. */
enum class AchievementId(val storageId: String) {
    FIRST_STEP("first_step"),
    BEGINNER("beginner_10"),
    CONSISTENCY("consistency_7_days"),
    DETERMINED("determined_50"),
    GROWING_TOGETHER("level_5"),
    FIRST_PURCHASE("first_purchase"),
    PERFECT_DAY("perfect_day"),
    ;

    companion object {
        fun fromStorageId(id: String): AchievementId? = entries.firstOrNull { it.storageId == id }
    }
}

data class AchievementStatus(
    val id: AchievementId,
    val unlockedAtEpochMillis: Long?,
    /** 0..1 progress towards unlocking, for the progress bar. */
    val progress: Float,
) {
    val unlocked: Boolean get() = unlockedAtEpochMillis != null
}
