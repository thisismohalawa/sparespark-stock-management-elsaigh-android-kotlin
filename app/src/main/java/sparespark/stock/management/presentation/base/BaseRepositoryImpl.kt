package sparespark.stock.management.presentation.base

import sparespark.stock.management.core.ADMIN_ROLE_ID
import sparespark.stock.management.core.OWNER_ROLE_ID
import sparespark.stock.management.core.PM_ROLE_ID
import sparespark.stock.management.core.launchAWithContextScope
import sparespark.stock.management.data.local.user.UserDao
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor

abstract class BaseRepositoryImpl(
    private val userDao: UserDao,
    private val connectivityInterceptor: ConnectivityInterceptor
) {
    protected fun hasInternetConnection(): Boolean = connectivityInterceptor.isOnline()

    protected suspend fun isUserActiveAdmin(userId: String?): Boolean =
        isUserActive(userId) && isUserAdmin(userId)

    protected suspend fun isUserActivePM(userId: String?): Boolean =
        isUserActive(userId) && isUserPM(userId)

    protected suspend fun isUserActive(userId: String?): Boolean = launchAWithContextScope {
        when (userId?.let { userDao.isSignedUserActive(it) }) {
            true -> return@launchAWithContextScope true
            else -> return@launchAWithContextScope false
        }
    }

    protected suspend fun isUserAdmin(userId: String?): Boolean = launchAWithContextScope {
        when (userId?.let { userDao.getSignedRoleId(it) }) {
            OWNER_ROLE_ID, ADMIN_ROLE_ID -> return@launchAWithContextScope true
            else -> return@launchAWithContextScope false
        }
    }

    private suspend fun isUserPM(userId: String?): Boolean = launchAWithContextScope {
        when (userId?.let { userDao.getSignedRoleId(it) }) {
            PM_ROLE_ID -> return@launchAWithContextScope true
            else -> return@launchAWithContextScope false
        }
    }

    protected suspend fun getUserName(userId: String?): String? = launchAWithContextScope {
        if (userId == null) return@launchAWithContextScope null
        return@launchAWithContextScope userDao.getUserNameById(userId)
    }

}
