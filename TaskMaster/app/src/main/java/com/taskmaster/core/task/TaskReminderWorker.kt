package com.taskmaster.core.task

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.taskmaster.R
import java.util.concurrent.TimeUnit

class TaskReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val KEY_TASK_NAME = "task_name"
        const val KEY_TASK_ID = "task_id"

        fun scheduleReminder(
            context: Context,
            taskId: Int,
            taskName: String,
            delayMinutes: Long
        ): String {
            val workId = "reminder_$taskId"
            val data = workDataOf(
                KEY_TASK_NAME to taskName,
                KEY_TASK_ID to taskId
            )
            val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(workId)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(workId, ExistingWorkPolicy.REPLACE, request)
            return workId
        }

        fun cancelReminder(context: Context, taskId: Int) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$taskId")
        }
    }

    override suspend fun doWork(): Result {
        val taskName = inputData.getString(KEY_TASK_NAME) ?: return Result.failure()
        createNotificationChannel()
        showNotification(taskName)
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de tareas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de tareas pendientes"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(taskName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("¡Tarea pendiente!")
            .setContentText(taskName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}