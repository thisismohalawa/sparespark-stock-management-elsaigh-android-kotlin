package sparespark.stock.management.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import sparespark.stock.management.core.TAG
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.receiver.PeriodicDataBackupReceiver
import java.util.Calendar

private const val RC = 0

class ReminderApiImpl(
    private val utilPref: UtilPreference,
    private val context: Context
) : ReminderAPI {


    override fun setUpAlarmForAutoBackup(): DataResult<Exception, Unit> = DataResult.build {
        if (utilPref.isUsingAutoBackup()) {
            if (setReminder() is DataResult.Value) Unit
            else throw Exception()
        } else {
            if (cancelReminder() is DataResult.Value) Unit
            else throw Exception()
        }
    }

    private fun Intent.isScheduled(): Boolean = PendingIntent.getBroadcast(
        context, RC,
        this,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    ) != null


    private fun setReminder(): DataResult<Exception, Unit> = DataResult.build {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PeriodicDataBackupReceiver::class.java)
        if (!intent.isScheduled()) {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            val startUpTime = calendar.timeInMillis
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                RC,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startUpTime, AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }

    private fun cancelReminder(): DataResult<Exception, Unit> = DataResult.build {
        val intent = Intent(context, PeriodicDataBackupReceiver::class.java)
        if (intent.isScheduled()) {
            val pendingIntent = PendingIntent.getBroadcast(
                context, RC,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.cancel()
        }
    }
}
