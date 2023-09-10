package sparespark.stock.management.presentation.main

import androidx.recyclerview.widget.RecyclerView
import sparespark.stock.management.core.result.UiResourceResult

interface StockActivityInteract {
    interface View {
        fun moveToLoginView()
        fun restartActivity()
        fun showLoadingProgress()
        fun hideLoadingProgress()
        fun updateMainActionStatusText(msg: UiResourceResult, isError: Boolean = false)
        fun actionOnSwipeLayoutRefreshed(rc: RecyclerView? = null, action: () -> Unit)
    }
    interface Action{
        fun downloadDataBackupAsExcelFile()
        fun deleteAllCompletedData()
    }
}

sealed class StockActivityEvent {
    object OnStart : StockActivityEvent()
    object GetProfileUserInfo : StockActivityEvent()
    object ClearCache : StockActivityEvent()
    object GetProfileSettingsList : StockActivityEvent()
    object Logout : StockActivityEvent()
    object OnTeamSettingClick : StockActivityEvent()
    object OnTempDBSettingClick : StockActivityEvent()
    object OnBackupsActionSettingClick : StockActivityEvent()
}
