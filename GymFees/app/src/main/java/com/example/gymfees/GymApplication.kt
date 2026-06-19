package com.example.gymfees

import android.app.Application
import androidx.work.*
import com.example.gymfees.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class GymApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupDailyNotifications()
    }

    private fun setupDailyNotifications() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_gym_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
