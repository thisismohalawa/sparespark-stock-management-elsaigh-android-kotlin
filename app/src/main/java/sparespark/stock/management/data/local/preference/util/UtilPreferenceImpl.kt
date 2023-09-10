package sparespark.stock.management.data.local.preference.util

import android.content.Context
import org.threeten.bp.ZonedDateTime
import sparespark.stock.management.data.local.preference.PreferenceProvider

private const val USER_ID = "user_id"
private const val CITY_LAST_CACHE = "city_last_cache"
private const val CLIENT_LAST_CACHE = "client_last_cache"
private const val PAY_LAST_CACHE = "pay_last_cache"
private const val TEAM_LAST_CACHE = "team_last_cache"
private const val CACHE_TIME = "CACHE_TIME"
private const val EXCHANGE_CACHE_TIME = "EXCHANGE_CACHE_TIME"
private const val USE_AUTO_BACKUP = "USE_AUTO_BACKUP"


class UtilPreferenceImpl(
    context: Context
) : PreferenceProvider(context), UtilPreference {


    private fun getCityListLastCacheTime(): String? =
        preferences.getString(CITY_LAST_CACHE, null)

    private fun getClientListLastCacheTime(): String? =
        preferences.getString(CLIENT_LAST_CACHE, null)

    private fun getTeamListLastCacheTime(): String? =
        preferences.getString(TEAM_LAST_CACHE, null)

    private fun getStockListLastCacheTime(): String? =
        preferences.getString(PAY_LAST_CACHE, null)

    override fun getDataCacheTime(): Int = try {
        val target = preferences.getString(CACHE_TIME, "480")
        target?.toInt()!!
    } catch (ex: Exception) {
        480
    }

    override fun getDataExchangeCacheTime(): Int = try {
        val target = preferences.getString(EXCHANGE_CACHE_TIME, "0")
        target?.toInt()!!
    } catch (ex: Exception) {
        0
    }

    override fun isUsingAutoBackup(): Boolean = preferences.getBoolean(USE_AUTO_BACKUP, false)


    override fun getSignedUserId(): String? =
        preferences.getString(USER_ID, null)

    override fun updateSignedUserId(userId: String) =
        preferencesEditor.putString(USER_ID, userId).apply()

    override fun isCityListUpdateNeeded(): Boolean {
        if (getCityListLastCacheTime() == null) return true
        return try {
            val timeAgo = ZonedDateTime.now().minusMinutes(getDataCacheTime().toLong())
            val fetchedTime = ZonedDateTime.parse(getCityListLastCacheTime())
            fetchedTime.isBefore(timeAgo)
        } catch (ex: Exception) {
            true
        }
    }

    override fun isClientListUpdateNeeded(): Boolean {
        if (getClientListLastCacheTime() == null) return true
        return try {
            val timeAgo = ZonedDateTime.now().minusMinutes(getDataCacheTime().toLong())
            val fetchedTime = ZonedDateTime.parse(getClientListLastCacheTime())
            fetchedTime.isBefore(timeAgo)
        } catch (ex: Exception) {
            true
        }
    }

    override fun isTeamListUpdateNeeded(): Boolean {
        if (getTeamListLastCacheTime() == null) return true
        return try {
            val timeAgo = ZonedDateTime.now().minusMinutes(getDataCacheTime().toLong())
            val fetchedTime = ZonedDateTime.parse(getTeamListLastCacheTime())
            fetchedTime.isBefore(timeAgo)
        } catch (ex: Exception) {
            true
        }
    }

    override fun isStockListUpdateNeeded(): Boolean {
        if (getStockListLastCacheTime() == null) return true
        return try {
            val timeAgo = ZonedDateTime.now().minusMinutes(getDataExchangeCacheTime().toLong())
            val fetchedTime = ZonedDateTime.parse(getStockListLastCacheTime())
            fetchedTime.isBefore(timeAgo)
        } catch (ex: Exception) {
            true
        }
    }

    override fun updateCityListCacheTimeToNow() {
        preferencesEditor.putString(CITY_LAST_CACHE, ZonedDateTime.now().toString()).apply()
    }

    override fun updateClientListCacheTimeToNow() {
        preferencesEditor.putString(CLIENT_LAST_CACHE, ZonedDateTime.now().toString()).apply()
    }

    override fun updateStockListCacheTimeToNow() {
        preferencesEditor.putString(PAY_LAST_CACHE, ZonedDateTime.now().toString()).apply()
    }

    override fun updateTeamListCacheTimeToNow() {
        preferencesEditor.putString(TEAM_LAST_CACHE, ZonedDateTime.now().toString()).apply()
    }

    override fun clearPreferenceArgs() {
        preferencesEditor.clear().commit()
    }

    override fun clearCityListCacheTime() {
        preferencesEditor.remove(CITY_LAST_CACHE).commit()
    }

    override fun clearClientListCacheTime() {
        preferencesEditor.remove(CLIENT_LAST_CACHE).commit()
    }

    override fun clearStockListCacheTime() {
        preferencesEditor.remove(PAY_LAST_CACHE).commit()
    }

    override fun clearTeamListCacheTime() {
        preferencesEditor.remove(TEAM_LAST_CACHE).commit()
    }
}
