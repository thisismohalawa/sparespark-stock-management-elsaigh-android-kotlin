package sparespark.stock.management.data.reminder

import sparespark.stock.management.core.result.DataResult

interface ReminderAPI {

    fun setUpAlarmForAutoBackup(): DataResult<Exception, Unit>

}
