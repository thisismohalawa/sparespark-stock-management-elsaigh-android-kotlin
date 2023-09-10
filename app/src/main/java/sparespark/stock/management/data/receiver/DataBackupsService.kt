package sparespark.stock.management.data.receiver

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import sparespark.stock.management.core.TAG
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.notification.NotificationManager
import sparespark.stock.management.data.repository.StockRepository

const val START_ACTION_BACKUPS = "START_ACTION_BACKUPS"
const val START_ACTION_CLEAR_DATA = "START_ACTION_CLEAR_DATA"
const val STOP_ACTION_SERVICE = "STOP_ACTION_SERVICE"

class DataBackupsService : Service(), KodeinAware {
    override lateinit var kodein: Kodein

    private val jobTracker = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val stockRepo: StockRepository by instance()

    override fun onCreate() {
        super.onCreate()
        kodein = (this.applicationContext as KodeinAware).kodein

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForeground(
            2,
            NotificationManager.startForegroundServiceNotification(this)
        )
        else startForeground(1, Notification())
    }

    override fun onDestroy() {
        jobTracker.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fun stopService() {
            stopForeground(true)
            stopSelfResult(startId)
        }
        when (intent?.action) {
            START_ACTION_BACKUPS -> {
                coroutineScope.launch {
                    when (stockRepo.downloadDataBackupsAsExcel(this@DataBackupsService)) {
                        is DataResult.Error ->
                            stopService()

                        is DataResult.Value ->
                            Log.d(TAG, "onStartCommand Done..")
                    }

                }
            }

            START_ACTION_CLEAR_DATA -> {
                coroutineScope.launch {
                    when (stockRepo.deleteAllCompletedItems(this@DataBackupsService)) {
                        is DataResult.Error ->
                            stopService()

                        is DataResult.Value ->
                            Log.d(TAG, "onStartCommand Done..")
                    }
                }
            }

            STOP_ACTION_SERVICE -> {
                stopService()
            }
        }
        return START_STICKY
    }
}
