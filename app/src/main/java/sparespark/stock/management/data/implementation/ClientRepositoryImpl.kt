package sparespark.stock.management.data.implementation

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import sparespark.stock.management.core.CLIENT_REF_NAME
import sparespark.stock.management.core.DATABASE_REF_NAME
import sparespark.stock.management.core.DATABASE_URL
import sparespark.stock.management.core.DeactivatedException
import sparespark.stock.management.core.NoConnectivityException
import sparespark.stock.management.core.NotPermittedException
import sparespark.stock.management.core.awaitTaskCompletable
import sparespark.stock.management.core.awaitTaskResult
import sparespark.stock.management.core.launchAWithContextScope
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.toClient
import sparespark.stock.management.core.toClientListFromRoomClient
import sparespark.stock.management.core.toRemoteClient
import sparespark.stock.management.core.toRoomClient
import sparespark.stock.management.data.local.client.ClientDao
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.local.user.UserDao
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.data.model.client.RemoteClient
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.presentation.base.BaseRepositoryImpl

class ClientRepositoryImpl(
    private val clientDao: ClientDao,
    private val utilPref: UtilPreference,
    userDao: UserDao,
    connectivityInterceptor: ConnectivityInterceptor
) : BaseRepositoryImpl(userDao, connectivityInterceptor), ClientRepository {

    private val userId: String?
        get() = utilPref.getSignedUserId()

    private val dataCacheTime: Int
        get() = utilPref.getDataCacheTime()

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance(DATABASE_URL).getReference(DATABASE_REF_NAME)
            .child(CLIENT_REF_NAME)

    private fun clearClientCache() = utilPref.clearClientListCacheTime()

    override suspend fun getClientList(): DataResult<Exception, List<Client>> =
        if (utilPref.isClientListUpdateNeeded()) getRemoteClientList()
        else getLocalClientList()

    /*
    * Remote
    *
    *
    * */
    private suspend fun getRemoteClientList(): DataResult<Exception, List<Client>> =
        DataResult.build {
            if (!isUserActive(userId)) throw DeactivatedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            val task: DataSnapshot = awaitTaskResult(databaseReference.get())
            val result: DataResult<Exception, List<Client>> = resultToClientList(task)
            if (result is DataResult.Value) {
                result.value.updateLocalClientEntries()
                return@build result.value
            } else throw Exception()
        }

    private suspend fun resultToClientList(result: DataSnapshot?): DataResult<Exception, List<Client>> =
        launchAWithContextScope {
            DataResult.build {
                if (result == null) return@build emptyList()
                val clients: MutableList<Client> = mutableListOf()
                result.children.forEach { data ->
                    data.getValue(RemoteClient::class.java)?.let { clients.add(it.toClient) }
                }
                return@build clients
            }
        }

    override suspend fun updateClient(client: Client): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActive(userId)) throw DeactivatedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            awaitTaskCompletable(
                databaseReference.child(client.creationDate).setValue(client.toRemoteClient)
            )
            clearClientCache()
        }

    override suspend fun deleteClient(creationDate: String): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActiveAdmin(userId)) throw NotPermittedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            awaitTaskCompletable(
                databaseReference.child(creationDate).removeValue()
            )
            clearClientCache()
        }

    /*
    * Local
    *
    *
    * */
    override suspend fun getCityByClientName(client: String): DataResult<Exception, String?> =
        DataResult.build {
            clientDao.getCity(client)
        }

    private suspend fun getLocalClientList(): DataResult<Exception, List<Client>> =
        DataResult.build {
            clientDao.getClientList().toClientListFromRoomClient()
        }

    private suspend fun List<Client>.updateLocalClientEntries(): DataResult<Exception, Unit> =
        DataResult.build {
            launchAWithContextScope {
                this.let { remoteClients ->
                    if (remoteClients.isNotEmpty() && dataCacheTime != 0) {
                        clientDao.clearData()
                        remoteClients.forEach { clientDao.insertOrUpdateClient(it.toRoomClient) }
                        utilPref.updateClientListCacheTimeToNow()
                    }
                }
            }
        }
}
