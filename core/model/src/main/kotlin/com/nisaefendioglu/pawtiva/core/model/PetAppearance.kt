package com.nisaefendioglu.pawtiva.core.model

/** Where an accessory sits on the pet. One item per slot can be worn at a time. */
enum class AccessorySlot { HEAD, EYES, EARS, NECK, SPECIAL }

/**
 * Stable identifiers of cosmetic accessories. Ids are stored in the database, so they must
 * never be renamed; new items are only ever added.
 */
enum class AccessoryId(val storageId: String, val slot: AccessorySlot, val compatiblePets: Set<PetType>) {
    BASIC_CAP("basic_cap", AccessorySlot.HEAD, PetType.entries.toSet()),
    ROYAL_CROWN("royal_crown", AccessorySlot.HEAD, PetType.entries.toSet()),
    ROUND_GLASSES("round_glasses", AccessorySlot.EYES, PetType.entries.toSet()),
    WIRELESS_HEADPHONES("wireless_headphones", AccessorySlot.EARS, PetType.entries.toSet()),
    WINTER_SCARF("winter_scarf", AccessorySlot.NECK, PetType.entries.toSet()),
    FLOWER_CLIP("flower_clip", AccessorySlot.SPECIAL, PetType.entries.toSet()),
    ;

    fun fits(pet: PetType): Boolean = pet in compatiblePets

    companion object {
        fun fromStorageId(id: String): AccessoryId? = entries.firstOrNull { it.storageId == id }
    }
}

/** Everything needed to draw the pet. Renderers draw only [visibleAccessories]. */
data class PetAppearance(
    val type: PetType,
    val mood: PetMood,
    val equipped: Set<AccessoryId> = emptySet(),
) {
    /**
     * Items that fit this pet, one per slot. Items that do not fit stay owned and equipped in
     * storage; they simply are not drawn until the user switches back to a pet they fit.
     */
    val visibleAccessories: List<AccessoryId>
        get() = equipped
            .filter { it.fits(type) }
            .groupBy { it.slot }
            .map { (_, items) -> items.first() }
            .sortedBy { it.slot.ordinal }
}
