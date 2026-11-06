package com.nisaefendioglu.pawtiva.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nisaefendioglu.pawtiva.core.data.ReminderScheduler
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Receivers get dependencies through an entry point (no constructor injection for receivers). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun preferences(): UserPreferencesRepository
    fun scheduler(): ReminderScheduler
}

private fun Context.reminderEntryPoint(): ReminderEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, ReminderEntryPoint::class.java)

/** Runs [block] off the main thread while keeping the broadcast alive until it finishes. */
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** Fired by the daily alarm: shows the reminder (if still wanted) and arms the next one. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_REMINDER) return
        val entryPoint = context.reminderEntryPoint()
        runAsync {
            val preferences = entryPoint.preferences().preferences.first()
            if (!preferences.remindersEnabled) return@runAsync
            // Arm tomorrow's alarm first, so a failure while posting can never stop future reminders.
            entryPoint.scheduler().schedule(preferences.reminderTime)
            runCatching { ReminderNotifications.showIfAllowed(context, preferences) }
        }
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.nisaefendioglu.pawtiva.action.SHOW_REMINDER"
    }
}

/** Re-arms the reminder after a reboot, an app update, or a clock / time-zone change. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        val entryPoint = context.reminderEntryPoint()
        runAsync {
            val preferences = entryPoint.preferences().preferences.first()
            val scheduler = entryPoint.scheduler()
            if (preferences.remindersEnabled) scheduler.schedule(preferences.reminderTime) else scheduler.cancel()
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
