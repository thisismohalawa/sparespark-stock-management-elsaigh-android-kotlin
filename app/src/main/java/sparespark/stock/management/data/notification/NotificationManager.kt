package sparespark.stock.management.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import sparespark.stock.management.R
import sparespark.stock.management.presentation.main.StockActivity

object NotificationManager {

    // FOREGROUND
    private const val FORE_NOTIFICATION_CHANNEL_ID = "ElSaigh.services"
    private const val FORE_CHANNEL_NAME = "ElSaigh Services"

    @RequiresApi(Build.VERSION_CODES.O)
    fun startForegroundServiceNotification(context: Context): Notification {
        val notificationChannel = NotificationChannel(
            FORE_NOTIFICATION_CHANNEL_ID,
            FORE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_NONE
        )
        val manager =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(notificationChannel)
        val notificationBuilder =
            NotificationCompat.Builder(context, FORE_NOTIFICATION_CHANNEL_ID)
        return notificationBuilder.setOngoing(true)
            .setContentTitle(context.getString(R.string.auto_data_backup))
            .setContentText(context.getString(R.string.start_backup))
            .setContentIntent(getPendingIntentWithStack(context, StockActivity::class.java))
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setSmallIcon(R.drawable.ic_buy)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun <T> getPendingIntentWithStack(
        context: Context, javaClass: Class<T>
    ): PendingIntent {
        val resultIntent = Intent(context, javaClass)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(javaClass)
        stackBuilder.addNextIntent(resultIntent)

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
