package com.example.collage.data

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object NotificationScheduler {

    fun scheduleRandomNotification(context: Context, randomDelay: Long) {

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(randomDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }


}