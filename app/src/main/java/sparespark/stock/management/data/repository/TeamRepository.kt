package sparespark.stock.management.data.repository

import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.model.login.User

interface TeamRepository {
    suspend fun getTeamList(): DataResult<Exception, List<User>>
    suspend fun updateUserActiveStatus(uid: String, isActive: Boolean): DataResult<Exception, Unit>
    suspend fun updateUserAdminStatus(uid: String, isAdmin: Boolean): DataResult<Exception, Unit>
    suspend fun updateUserPmStatus(uid: String, isPm: Boolean): DataResult<Exception, Unit>

}
