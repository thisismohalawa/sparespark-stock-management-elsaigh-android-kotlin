package sparespark.stock.management.data.repository

import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.model.other.SettingItem

interface UserRepository {
    suspend fun getAuthUser(): DataResult<Exception, User?>
    suspend fun getLocalUser(): DataResult<Exception, User?>
    suspend fun getRemoteUser(): DataResult<Exception, User?>
    suspend fun createARemoteFirebaseUser(user: User): DataResult<Exception, Boolean>
    suspend fun updateLocalUserEntry(user: User): DataResult<Exception, Unit>
    suspend fun signOutCurrentUser(): DataResult<Exception, Unit>
    suspend fun signInGoogleUser(idToken: String): DataResult<Exception, Unit>
    suspend fun getSettingListByRoleId(userRoleId: Int?): DataResult<Exception, List<SettingItem>>
    suspend fun clearCacheTimePreference(): DataResult<Exception, Unit>
}
