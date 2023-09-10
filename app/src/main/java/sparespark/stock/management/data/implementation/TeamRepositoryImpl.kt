package sparespark.stock.management.data.implementation

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import sparespark.stock.management.core.ACTIVATED_CHILD_NAME
import sparespark.stock.management.core.ADMIN_ROLE_ID
import sparespark.stock.management.core.DATABASE_REF_NAME
import sparespark.stock.management.core.DATABASE_URL
import sparespark.stock.management.core.DeactivatedException
import sparespark.stock.management.core.EMPLOYEE_ROLE_ID
import sparespark.stock.management.core.NoConnectivityException
import sparespark.stock.management.core.NotPermittedException
import sparespark.stock.management.core.PM_ROLE_ID
import sparespark.stock.management.core.ROLE_ID_CHILD_NAME
import sparespark.stock.management.core.TEAM_REF_NAME
import sparespark.stock.management.core.awaitTaskCompletable
import sparespark.stock.management.core.awaitTaskResult
import sparespark.stock.management.core.launchAWithContextScope
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.toRoomUser
import sparespark.stock.management.core.toUser
import sparespark.stock.management.core.toUserListFromRoomUser
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.local.user.UserDao
import sparespark.stock.management.data.model.login.RemoteUser
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor
import sparespark.stock.management.data.repository.TeamRepository
import sparespark.stock.management.presentation.base.BaseRepositoryImpl

class TeamRepositoryImpl(
    private val utilPref: UtilPreference,
    private val userDao: UserDao,
    connectivityInterceptor: ConnectivityInterceptor
) : BaseRepositoryImpl(userDao, connectivityInterceptor), TeamRepository {

    private val userId: String?
        get() = utilPref.getSignedUserId()

    private val dataCacheTime: Int
        get() = utilPref.getDataCacheTime()

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance(DATABASE_URL).getReference(DATABASE_REF_NAME)
            .child(TEAM_REF_NAME)

    private fun clearTeamListCacheTime() = utilPref.clearTeamListCacheTime()

    override suspend fun getTeamList(): DataResult<Exception, List<User>> =
        if (utilPref.isTeamListUpdateNeeded()) getRemoteTeamList()
        else getLocalTeamList()

    /*
    * Remote
    *
    *
    * */
    private suspend fun getRemoteTeamList(): DataResult<Exception, List<User>> = DataResult.build {
        if (!isUserActiveAdmin(userId)) throw NotPermittedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val task: DataSnapshot = awaitTaskResult(databaseReference.get())
        val result: DataResult<Exception, List<User>> = resultToUserList(task)
        if (result is DataResult.Value) {
            result.value.updateLocalTeamEntries()
            return@build result.value
        } else throw Exception()
    }

    private suspend fun resultToUserList(result: DataSnapshot?): DataResult<Exception, List<User>> =
        launchAWithContextScope {
            DataResult.build {
                if (result == null) return@build emptyList()
                val users: MutableList<User> = mutableListOf()
                result.children.forEach { data ->
                    data.getValue(RemoteUser::class.java)?.toUser?.let { users.add(it) }
                }
                return@build users
            }
        }

    override suspend fun updateUserActiveStatus(
        uid: String,
        isActive: Boolean
    ): DataResult<Exception, Unit> = DataResult.build {
        if (!isUserActiveAdmin(userId)) throw NotPermittedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val result: HashMap<String, Any> = HashMap()
        result[ACTIVATED_CHILD_NAME] = isActive
        awaitTaskCompletable(
            databaseReference.child(uid).updateChildren(result)
        )
        clearTeamListCacheTime()
    }

    override suspend fun updateUserAdminStatus(
        uid: String,
        isAdmin: Boolean
    ): DataResult<Exception, Unit> = DataResult.build {
        if (!isUserActiveAdmin(userId)) throw NotPermittedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val result: HashMap<String, Any> = HashMap()
        if (isAdmin) result[ROLE_ID_CHILD_NAME] = ADMIN_ROLE_ID
        else result[ROLE_ID_CHILD_NAME] = EMPLOYEE_ROLE_ID
        awaitTaskCompletable(
            databaseReference.child(uid).updateChildren(result)
        )
        clearTeamListCacheTime()
    }

    override suspend fun updateUserPmStatus(
        uid: String,
        isPm: Boolean
    ): DataResult<Exception, Unit> = DataResult.build {
        if (!isUserActiveAdmin(userId)) throw NotPermittedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val result: HashMap<String, Any> = HashMap()
        if (isPm) result[ROLE_ID_CHILD_NAME] = PM_ROLE_ID
        else result[ROLE_ID_CHILD_NAME] = EMPLOYEE_ROLE_ID
        awaitTaskCompletable(
            databaseReference.child(uid).updateChildren(result)
        )
        clearTeamListCacheTime()
    }

    /*
    * Local
    *
    *
    *  */
    private suspend fun getLocalTeamList(): DataResult<Exception, List<User>> = DataResult.build {
        userDao.getTeamList().toUserListFromRoomUser()
    }

    private suspend fun List<User>.updateLocalTeamEntries(): DataResult<Exception, Unit> =
        DataResult.build {
            launchAWithContextScope {
                this.let { remoteTeam ->
                    if (remoteTeam.isNotEmpty() && dataCacheTime != 0) {
                        userId?.let { userDao.clearTeamList(userId = it) }
                        remoteTeam.forEach { userDao.upsert(it.toRoomUser) }
                        utilPref.updateTeamListCacheTimeToNow()
                    }
                }
            }
        }
}
