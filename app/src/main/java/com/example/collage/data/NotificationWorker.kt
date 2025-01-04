package com.example.collage.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.collage.ui.MainViewModel
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Random

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    ) : Worker(context, workerParams) {

        override fun doWork(): Result {

            val calendar = Calendar.getInstance()

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationChannelID = "1"

            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    notificationChannelID,
                    "Daily Photo Reminder",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(applicationContext, notificationChannelID)
                .setContentTitle("Time to freeze the moment!")
                .setContentText("Today is ${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1 }/${calendar.get(Calendar.YEAR)}")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()



            notificationManager.notify(Random().nextInt(), notification)


            return Result.success()
        }
}