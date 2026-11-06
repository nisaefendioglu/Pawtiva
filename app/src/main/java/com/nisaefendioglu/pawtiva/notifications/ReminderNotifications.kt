package com.nisaefendioglu.pawtiva.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.nisaefendioglu.pawtiva.MainActivity
import com.nisaefendioglu.pawtiva.R
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import java.time.LocalDate
import com.nisaefendioglu.pawtiva.core.designsystem.R as DesignR

/** Channel creation and the reminder notification itself. */
object ReminderNotifications {

    const val CHANNEL_ID = "daily_reminders"
    private const val NOTIFICATION_ID = 1_001

    fun createChannel(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.reminder_channel_description) }
        manager.createNotificationChannel(channel)
    }

    /** Posts the reminder if, and only if, the user turned reminders on and allows notifications. */
    fun showIfAllowed(context: Context, preferences: UserPreferences, today: LocalDate = LocalDate.now()) {
        if (!preferences.remindersEnabled) return
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!permissionGranted || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val petName = preferences.petName ?: context.getString(
            if (preferences.petType == PetType.DOG) {
                DesignR.string.pawtiva_pet_default_name_dog
            } else {
                DesignR.string.pawtiva_pet_default_name_cat
            },
        )
        val messages = listOf(
            context.getString(R.string.reminder_message_adventure),
            context.getString(R.string.reminder_message_small_step, petName),
            context.getString(R.string.reminder_message_check_in),
        )
        val message = messages[today.dayOfYear % messages.size]

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title, petName))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        // Re-checked right before posting so the permission guard sits next to notify().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
