package com.nisaefendioglu.pawtiva.feature.statistics

import androidx.annotation.StringRes
import com.nisaefendioglu.pawtiva.core.model.AchievementId

@StringRes
internal fun AchievementId.titleRes(): Int = when (this) {
    AchievementId.FIRST_STEP -> R.string.achievement_first_step
    AchievementId.BEGINNER -> R.string.achievement_beginner
    AchievementId.CONSISTENCY -> R.string.achievement_consistency
    AchievementId.DETERMINED -> R.string.achievement_determined
    AchievementId.GROWING_TOGETHER -> R.string.achievement_growing_together
    AchievementId.FIRST_PURCHASE -> R.string.achievement_first_purchase
    AchievementId.PERFECT_DAY -> R.string.achievement_perfect_day
}

@StringRes
internal fun AchievementId.descriptionRes(): Int = when (this) {
    AchievementId.FIRST_STEP -> R.string.achievement_first_step_description
    AchievementId.BEGINNER -> R.string.achievement_beginner_description
    AchievementId.CONSISTENCY -> R.string.achievement_consistency_description
    AchievementId.DETERMINED -> R.string.achievement_determined_description
    AchievementId.GROWING_TOGETHER -> R.string.achievement_growing_together_description
    AchievementId.FIRST_PURCHASE -> R.string.achievement_first_purchase_description
    AchievementId.PERFECT_DAY -> R.string.achievement_perfect_day_description
}
