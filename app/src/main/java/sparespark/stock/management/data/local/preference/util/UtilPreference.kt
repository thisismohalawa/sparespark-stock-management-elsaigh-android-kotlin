package sparespark.stock.management.data.local.preference.util

import sparespark.stock.management.data.local.preference.user.UserPreference

interface UtilPreference : UserPreference {

    fun getDataCacheTime(): Int
    fun getDataExchangeCacheTime(): Int
    fun isUsingAutoBackup(): Boolean

    fun isCityListUpdateNeeded(): Boolean
    fun isClientListUpdateNeeded(): Boolean
    fun isTeamListUpdateNeeded(): Boolean
    fun isStockListUpdateNeeded(): Boolean

    fun updateCityListCacheTimeToNow()
    fun updateClientListCacheTimeToNow()
    fun updateStockListCacheTimeToNow()
    fun updateTeamListCacheTimeToNow()

    fun clearPreferenceArgs()
    fun clearCityListCacheTime()
    fun clearClientListCacheTime()
    fun clearStockListCacheTime()
    fun clearTeamListCacheTime()
}
