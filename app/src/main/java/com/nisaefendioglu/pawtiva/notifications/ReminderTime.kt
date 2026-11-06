package com.nisaefendioglu.pawtiva.notifications

import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Next moment the daily reminder should fire: today at [time] if that is still ahead of [now],
 * otherwise tomorrow at [time], always in [now]'s zone. A local time that does not exist on a
 * DST "spring forward" day is moved forward by the gap (java.time's ZonedDateTime rules).
 */
internal fun nextReminderTrigger(now: ZonedDateTime, time: LocalTime): ZonedDateTime {
    val today = ZonedDateTime.of(now.toLocalDate(), time, now.zone)
    return if (today.isAfter(now)) today else ZonedDateTime.of(now.toLocalDate().plusDays(1), time, now.zone)
}
