package com.nisaefendioglu.pawtiva.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PetAppearanceTest {

    @Test
    fun visibleAccessories_areSortedBySlot_andOnePerSlot() {
        val appearance = PetAppearance(
            type = PetType.CAT,
            mood = PetMood.NORMAL,
            equipped = setOf(AccessoryId.WINTER_SCARF, AccessoryId.BASIC_CAP, AccessoryId.ROYAL_CROWN, AccessoryId.ROUND_GLASSES),
        )
        val visible = appearance.visibleAccessories
        assertEquals(listOf(AccessorySlot.HEAD, AccessorySlot.EYES, AccessorySlot.NECK), visible.map { it.slot })
    }

    @Test
    fun storageIds_roundTrip_andUnknownIdsAreIgnored() {
        AccessoryId.entries.forEach { assertEquals(it, AccessoryId.fromStorageId(it.storageId)) }
        assertNull(AccessoryId.fromStorageId("removed_item"))
    }

    @Test
    fun storageIds_areUnique() {
        assertEquals(AccessoryId.entries.size, AccessoryId.entries.map { it.storageId }.toSet().size)
    }

    @Test
    fun switchingPet_keepsEquippedItems() {
        val cat = PetAppearance(PetType.CAT, PetMood.HAPPY, setOf(AccessoryId.FLOWER_CLIP))
        val dog = cat.copy(type = PetType.DOG)
        assertEquals(cat.equipped, dog.equipped)
    }
}
