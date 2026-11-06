package com.nisaefendioglu.pawtiva.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NextReminderTriggerTest {

    private val istanbul = ZoneId.of("Europe/Istanbul")
    private val berlin = ZoneId.of("Europe/Berlin")

    private fun at(zone: ZoneId, y: Int, m: Int, d: Int, h: Int, min: Int) =
        ZonedDateTime.of(LocalDateTime.of(y, m, d, h, min), zone)

    @Test
    fun laterToday_firesToday() {
        val next = nextReminderTrigger(at(istanbul, 2026, 11, 5, 18, 0), LocalTime.of(20, 0))
        assertEquals(at(istanbul, 2026, 11, 5, 20, 0), next)
    }

    @Test
    fun alreadyPassedOrExactlyNow_firesTomorrow() {
        assertEquals(
            at(istanbul, 2026, 11, 6, 20, 0),
            nextReminderTrigger(at(istanbul, 2026, 11, 5, 21, 30), LocalTime.of(20, 0)),
        )
        assertEquals(
            at(istanbul, 2026, 11, 6, 20, 0),
            nextReminderTrigger(at(istanbul, 2026, 11, 5, 20, 0), LocalTime.of(20, 0)),
        )
    }

    @Test
    fun endOfMonth_rollsToNextMonth() {
        val next = nextReminderTrigger(at(istanbul, 2026, 12, 31, 23, 0), LocalTime.of(9, 0))
        assertEquals(LocalDate.of(2027, 1, 1), next.toLocalDate())
    }

    @Test
    fun dstSpringForwardGap_isShiftedForward() {
        // Berlin skips 02:00–03:00 on 2026-03-29.
        val next = nextReminderTrigger(at(berlin, 2026, 3, 29, 0, 30), LocalTime.of(2, 30))
        assertEquals(LocalDateTime.of(2026, 3, 29, 3, 30), next.toLocalDateTime())
    }

    @Test
    fun dstFallBack_keepsLocalTime() {
        // Berlin repeats 02:00–03:00 on 2026-10-25; the reminder stays at 20:00 local.
        val next = nextReminderTrigger(at(berlin, 2026, 10, 25, 12, 0), LocalTime.of(20, 0))
        assertEquals(LocalTime.of(20, 0), next.toLocalTime())
        assertEquals(LocalDate.of(2026, 10, 25), next.toLocalDate())
    }

    @Test
    fun zoneChange_usesTheNewZone() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        val next = nextReminderTrigger(at(tokyo, 2026, 11, 5, 7, 0), LocalTime.of(8, 0))
        assertEquals(tokyo, next.zone)
        assertEquals(LocalTime.of(8, 0), next.toLocalTime())
    }
}
