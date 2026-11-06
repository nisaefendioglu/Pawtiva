package com.nisaefendioglu.pawtiva

import android.app.Application
import com.nisaefendioglu.pawtiva.notifications.ReminderNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PawtivaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Creating a channel shows nothing; reminders are posted only after the user opts in.
        ReminderNotifications.createChannel(this)
    }
}
