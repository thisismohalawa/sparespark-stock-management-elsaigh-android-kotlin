package sparespark.stock.management.data.implementation

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import sparespark.stock.management.core.CITY_REF_NAME
import sparespark.stock.management.core.DATABASE_REF_NAME
import sparespark.stock.management.core.DATABASE_URL
import sparespark.stock.management.core.DeactivatedException
import sparespark.stock.management.core.NoConnectivityException
import sparespark.stock.management.core.NotPermittedException
import sparespark.stock.management.core.awaitTaskCompletable
import sparespark.stock.management.core.awaitTaskResult
import sparespark.stock.management.core.launchAWithContextScope
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.toCity
import sparespark.stock.management.core.toCityListFromRoomCity
import sparespark.stock.management.core.toRemoteCity
import sparespark.stock.management.core.toRoomCity
import sparespark.stock.management.data.local.city.CityDao
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.local.user.UserDao
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.data.model.city.RemoteCity
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.presentation.base.BaseRepositoryImpl

class CityRepositoryImpl(
    private val cityDao: CityDao,
    private val utilPref: UtilPreference,
    userDao: UserDao,
    connectivityInterceptor: ConnectivityInterceptor
) : BaseRepositoryImpl(userDao, connectivityInterceptor), CityRepository {

    private val userId: String?
        get() = utilPref.getSignedUserId()

    private val dataCacheTime: Int
        get() = utilPref.getDataCacheTime()

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance(DATABASE_URL).getReference(DATABASE_REF_NAME)
            .child(CITY_REF_NAME)

    private fun clearCityListCache() = utilPref.clearCityListCacheTime()

    override suspend fun getCityList(): DataResult<Exception, List<City>> =
        if (utilPref.isCityListUpdateNeeded()) getRemoteCityList()
        else getLocalCityList()


    /*
    * Remote
    * 
    * 
    * */
    private suspend fun getRemoteCityList(): DataResult<Exception, List<City>> = DataResult.build {
        if (!isUserActive(userId)) throw DeactivatedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val task: DataSnapshot = awaitTaskResult(databaseReference.get())
        val result: DataResult<Exception, List<City>> = resultToCityList(task)
        if (result is DataResult.Value) {
            result.value.updateLocalCityEntries()
            return@build result.value
        } else throw Exception()
    }

    private suspend fun resultToCityList(result: DataSnapshot?): DataResult<Exception, List<City>> =
        launchAWithContextScope {
            DataResult.build {
                if (result == null) return@build emptyList()
                val cities: MutableList<City> = mutableListOf()
                result.children.forEach { data ->
                    data.getValue(RemoteCity::class.java)?.let { cities.add(it.toCity) }
                }
                return@build cities
            }
        }

    override suspend fun updateCity(city: City): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActive(userId)) throw DeactivatedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            awaitTaskCompletable(
                databaseReference.child(city.creationDate)
                    .setValue(city.toRemoteCity)
            )
            clearCityListCache()
        }

    override suspend fun deleteCity(creationDate: String): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActiveAdmin(userId)) throw NotPermittedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            awaitTaskCompletable(
                databaseReference.child(creationDate).removeValue()
            )
            clearCityListCache()
        }

    /*
    * Local
    * 
    * */
    private suspend fun getLocalCityList(): DataResult<Exception, List<City>> = DataResult.build {
        cityDao.getCityList().toCityListFromRoomCity()
    }

    private suspend fun List<City>.updateLocalCityEntries(): DataResult<Exception, Unit> =
        DataResult.build {
            launchAWithContextScope {
                this.let { remoteCities ->
                    if (remoteCities.isNotEmpty() && dataCacheTime != 0) {
                        cityDao.clearData()
                        remoteCities.forEach { cityDao.insertOrUpdateCity(it.toRoomCity) }
                        utilPref.updateCityListCacheTimeToNow()
                    }
                }
            }
        }
}
