package com.nisaefendioglu.pawtiva.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitIconsTest {

    @Test
    fun acceptsSingleEmoji_trimmed() {
        assertEquals("🐢", HabitIcons.normalizeCustom("  🐢 "))
        assertEquals("☕", HabitIcons.normalizeCustom("☕"))
        assertEquals("👍🏽", HabitIcons.normalizeCustom("👍🏽"))
    }

    @Test
    fun rejectsTextNumbersBlanksAndSeveralEmoji() {
        assertNull(HabitIcons.normalizeCustom(""))
        assertNull(HabitIcons.normalizeCustom("   "))
        assertNull(HabitIcons.normalizeCustom("a"))
        assertNull(HabitIcons.normalizeCustom("7"))
        assertNull(HabitIcons.normalizeCustom("su"))
        assertNull(HabitIcons.normalizeCustom("🐢🐇"))
        assertNull(HabitIcons.normalizeCustom("🐢 a"))
    }
}
