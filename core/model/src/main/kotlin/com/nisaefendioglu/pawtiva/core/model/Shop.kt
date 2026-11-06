package com.nisaefendioglu.pawtiva.core.model

enum class ShopCategory { HATS, GLASSES, HEADPHONES, CLOTHES, NECK, SPECIAL }

/** A purchasable cosmetic. Prices are in PawCoins; there is no real-money purchase. */
data class ShopItem(
    val accessory: AccessoryId,
    val category: ShopCategory,
    val price: Int,
    /** Player level needed before the item can be bought (1 = no requirement). */
    val requiredLevel: Int = 1,
) {
    val compatiblePets: Set<PetType> get() = accessory.compatiblePets
}

/** A shop item together with what this user can do with it right now. */
data class ShopItemState(
    val item: ShopItem,
    val owned: Boolean,
    val equipped: Boolean,
    val fitsCurrentPet: Boolean,
    val affordable: Boolean,
    val levelUnlocked: Boolean,
) {
    val canBuy: Boolean get() = !owned && affordable && levelUnlocked
}

sealed interface PurchaseOutcome {
    data class Purchased(val item: ShopItem, val balanceAfter: Int, val unlockedAchievements: List<AchievementId>) : PurchaseOutcome
    data object AlreadyOwned : PurchaseOutcome
    data class InsufficientBalance(val missing: Int) : PurchaseOutcome
    data class LevelTooLow(val requiredLevel: Int) : PurchaseOutcome
    data object UnknownItem : PurchaseOutcome
}

enum class DateFormatPreference { SYSTEM, DAY_MONTH_YEAR, MONTH_DAY_YEAR, ISO }
