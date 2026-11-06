package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.ShopCategory
import com.nisaefendioglu.pawtiva.core.model.ShopItem

/**
 * The shop catalog lives in code (not in the database), so changing prices or adding items
 * never needs a migration. Only ownership is stored. Prices assume 5 PawCoins per habit.
 */
object ShopCatalog {
    val items: List<ShopItem> = listOf(
        ShopItem(AccessoryId.BASIC_CAP, ShopCategory.HATS, price = 40),
        ShopItem(AccessoryId.FLOWER_CLIP, ShopCategory.SPECIAL, price = 50),
        ShopItem(AccessoryId.ROUND_GLASSES, ShopCategory.GLASSES, price = 60),
        ShopItem(AccessoryId.WINTER_SCARF, ShopCategory.NECK, price = 70),
        ShopItem(AccessoryId.WIRELESS_HEADPHONES, ShopCategory.HEADPHONES, price = 90),
        ShopItem(AccessoryId.ROYAL_CROWN, ShopCategory.HATS, price = 200, requiredLevel = 5),
    )

    fun find(id: AccessoryId): ShopItem? = items.firstOrNull { it.accessory == id }

    /** Categories that currently have at least one item, in display order. */
    val categories: List<ShopCategory>
        get() = ShopCategory.entries.filter { category -> items.any { it.category == category } }
}
