package sparespark.stock.management.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PeriodicDataBackupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val serviceIntent = Intent(it, DataBackupsService::class.java)
            serviceIntent.action = START_ACTION_BACKUPS
            it.startService(serviceIntent)
        }
    }
}
