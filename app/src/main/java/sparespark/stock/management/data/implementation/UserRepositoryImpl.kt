package sparespark.stock.management.data.implementation

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import sparespark.stock.management.core.DATABASE_REF_NAME
import sparespark.stock.management.core.DATABASE_URL
import sparespark.stock.management.core.NoConnectivityException
import sparespark.stock.management.core.NotPermittedException
import sparespark.stock.management.core.PAY_REF_NAME
import sparespark.stock.management.core.TEAM_REF_NAME
import sparespark.stock.management.core.awaitTaskCompletable
import sparespark.stock.management.core.awaitTaskResult
import sparespark.stock.management.core.getAdminSettingMenuList
import sparespark.stock.management.core.getDefSettingMenuList
import sparespark.stock.management.core.isAdmin
import sparespark.stock.management.core.isPM
import sparespark.stock.management.core.launchAWithContextScope
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.toRemoteUser
import sparespark.stock.management.core.toRoomUser
import sparespark.stock.management.core.toUser
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.local.user.UserDao
import sparespark.stock.management.data.model.login.RemoteUser
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.model.other.SettingItem
import sparespark.stock.management.data.model.stock.RemoteStock
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor
import sparespark.stock.management.data.repository.UserRepository
import sparespark.stock.management.presentation.base.BaseRepositoryImpl

class UserRepositoryImpl(
    connectivityInterceptor: ConnectivityInterceptor,
    private val userDao: UserDao,
    private val utilPref: UtilPreference,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : BaseRepositoryImpl(userDao, connectivityInterceptor), UserRepository {

    private val userId: String?
        get() = utilPref.getSignedUserId()

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance(DATABASE_URL).getReference(DATABASE_REF_NAME)

    private fun isSignedIn(): Boolean = userId != null

    override suspend fun getAuthUser(): DataResult<Exception, User?> = DataResult.build {
        auth.currentUser?.toUser
    }

    override suspend fun getLocalUser(): DataResult<Exception, User?> = DataResult.build {
        if (!isSignedIn()) return@build null
        userDao.getSignedUser(userId!!).toUser
    }

    override suspend fun getRemoteUser(): DataResult<Exception, User?> = DataResult.build {
        if (!isSignedIn()) return@build null
        if (!hasInternetConnection()) throw NoConnectivityException()
        val task: DataSnapshot =
            awaitTaskResult(databaseReference.child(TEAM_REF_NAME).child(userId.toString()).get())
        task.getValue(RemoteUser::class.java)?.toUser
    }

    override suspend fun createARemoteFirebaseUser(user: User): DataResult<Exception, Boolean> =
        DataResult.build {
            if (userId != null) return@build false
            if (!hasInternetConnection()) throw NoConnectivityException()
            awaitTaskCompletable(
                databaseReference.child(TEAM_REF_NAME).child(user.uid).setValue(user.toRemoteUser)
            )
            return@build true
        }

    override suspend fun updateLocalUserEntry(user: User): DataResult<Exception, Unit> =
        DataResult.build {
            userId?.let { userDao.clearSignedUser(userId = it) }
            utilPref.updateSignedUserId(user.uid)
            userDao.upsert(user = user.toRoomUser)
        }

    override suspend fun signOutCurrentUser(): DataResult<Exception, Unit> = DataResult.build {
        auth.signOut()
        userId?.let { userDao.clearSignedUser(userId = it) }
        utilPref.clearPreferenceArgs()
    }

    override suspend fun signInGoogleUser(idToken: String): DataResult<Exception, Unit> =
        DataResult.build {
            /*
             * request credential from google, give it to firebase auth.
             *
             * */
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            awaitTaskCompletable(auth.signInWithCredential(credential))
        }

    override suspend fun getSettingListByRoleId(userRoleId: Int?): DataResult<Exception, List<SettingItem>> =
        if (userRoleId.isAdmin() || userRoleId.isPM()) getAdminSettingMenuList()
        else getDefSettingMenuList()

    override suspend fun clearCacheTimePreference(): DataResult<Exception, Unit> =
        DataResult.build {
            utilPref.clearCityListCacheTime()
            utilPref.clearClientListCacheTime()
            utilPref.clearStockListCacheTime()
            utilPref.clearTeamListCacheTime()
        }
}
