package com.nisaefendioglu.pawtiva.feature.shop

import androidx.annotation.StringRes
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.ShopCategory

@StringRes
internal fun AccessoryId.nameRes(): Int = when (this) {
    AccessoryId.BASIC_CAP -> R.string.shop_item_basic_cap
    AccessoryId.ROUND_GLASSES -> R.string.shop_item_round_glasses
    AccessoryId.WIRELESS_HEADPHONES -> R.string.shop_item_wireless_headphones
    AccessoryId.ROYAL_CROWN -> R.string.shop_item_royal_crown
    AccessoryId.WINTER_SCARF -> R.string.shop_item_winter_scarf
    AccessoryId.FLOWER_CLIP -> R.string.shop_item_flower_clip
}

@StringRes
internal fun AccessoryId.descriptionRes(): Int = when (this) {
    AccessoryId.BASIC_CAP -> R.string.shop_item_basic_cap_description
    AccessoryId.ROUND_GLASSES -> R.string.shop_item_round_glasses_description
    AccessoryId.WIRELESS_HEADPHONES -> R.string.shop_item_wireless_headphones_description
    AccessoryId.ROYAL_CROWN -> R.string.shop_item_royal_crown_description
    AccessoryId.WINTER_SCARF -> R.string.shop_item_winter_scarf_description
    AccessoryId.FLOWER_CLIP -> R.string.shop_item_flower_clip_description
}

@StringRes
internal fun ShopCategory.nameRes(): Int = when (this) {
    ShopCategory.HATS -> R.string.shop_category_hats
    ShopCategory.GLASSES -> R.string.shop_category_glasses
    ShopCategory.HEADPHONES -> R.string.shop_category_headphones
    ShopCategory.CLOTHES -> R.string.shop_category_clothes
    ShopCategory.NECK -> R.string.shop_category_neck
    ShopCategory.SPECIAL -> R.string.shop_category_special
}
