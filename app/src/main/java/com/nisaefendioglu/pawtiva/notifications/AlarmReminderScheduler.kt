package com.nisaefendioglu.pawtiva.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.nisaefendioglu.pawtiva.core.data.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules one inexact alarm for the next reminder. A 15-minute window is plenty for a daily
 * nudge and needs no exact-alarm permission. Each delivery schedules the following day.
 */
@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {

    override fun schedule(time: LocalTime) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val trigger = nextReminderTrigger(ZonedDateTime.now(), time)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            trigger.toInstant().toEpochMilli(),
            WINDOW_MILLIS,
            pendingIntent(),
        )
    }

    override fun cancel() {
        context.getSystemService<AlarmManager>()?.cancel(pendingIntent())
    }

    private fun pendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_SHOW_REMINDER),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val REQUEST_CODE = 4_201
        const val WINDOW_MILLIS = 15 * 60 * 1_000L
    }
}
