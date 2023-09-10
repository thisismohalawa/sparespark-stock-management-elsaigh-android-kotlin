package sparespark.stock.management.data.implementation

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import sparespark.stock.management.core.DATABASE_REF_NAME
import sparespark.stock.management.core.DATABASE_URL
import sparespark.stock.management.core.DeactivatedException
import sparespark.stock.management.core.NoConnectivityException
import sparespark.stock.management.core.NotPermittedException
import sparespark.stock.management.core.PAY_ACTIVE_REF_NAME
import sparespark.stock.management.core.PAY_REF_NAME
import sparespark.stock.management.core.UPDATED_DATE_REF_NAME
import sparespark.stock.management.core.UPDATED_REF_NAME
import sparespark.stock.management.core.awaitTaskCompletable
import sparespark.stock.management.core.awaitTaskResult
import sparespark.stock.management.core.exportListToExcel
import sparespark.stock.management.core.getCalendarDateTime
import sparespark.stock.management.core.launchAWithContextScope
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.toDoubleLimitation
import sparespark.stock.management.core.toRemoteStock
import sparespark.stock.management.core.toRoomStock
import sparespark.stock.management.core.toRoomTempStock
import sparespark.stock.management.core.toStock
import sparespark.stock.management.core.toStockListFromRoomStock
import sparespark.stock.management.core.view.getDividedRequiredAveValue
import sparespark.stock.management.data.local.preference.util.UtilPreference
import sparespark.stock.management.data.local.stock.StockDao
import sparespark.stock.management.data.local.user.UserDao
import sparespark.stock.management.data.model.other.StockListDetails
import sparespark.stock.management.data.model.stock.RemoteStock
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.data.network.connectivity.ConnectivityInterceptor
import sparespark.stock.management.data.receiver.DataBackupsService
import sparespark.stock.management.data.receiver.STOP_ACTION_SERVICE
import sparespark.stock.management.data.repository.StockRepository
import sparespark.stock.management.presentation.base.BaseRepositoryImpl
import java.util.Locale

class StockRepositoryImpl(
    private val stockDao: StockDao,
    private val utilPref: UtilPreference,
    userDao: UserDao,
    connectivityInterceptor: ConnectivityInterceptor
) : BaseRepositoryImpl(userDao, connectivityInterceptor), StockRepository {

    private var stockListDetails = StockListDetails()

    private val userId: String?
        get() = utilPref.getSignedUserId()

    private val dataCacheTime: Int
        get() = utilPref.getDataExchangeCacheTime()

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance(DATABASE_URL).getReference(DATABASE_REF_NAME)
            .child(PAY_REF_NAME)

    private suspend fun isAllowToReadData(): Boolean = try {
        delay(200)
        isUserAdmin(userId)
    } catch (ex: Exception) {
        false
    }

    private suspend fun getSignUserName(): String = try {
        delay(200)
        getUserName(userId).toString()
    } catch (ex: Exception) {
        "Team Member"
    }

    private fun Stock.isNewStock(): Boolean = this.createdBy.isEmpty()

    private fun clearStockListCache() = utilPref.clearStockListCacheTime()

    override suspend fun getStockList(): DataResult<Exception, List<Stock>> =
        if (utilPref.isStockListUpdateNeeded()) getRemoteStockList(
            isUpdateLocalRequired = true, displayOnlyTodayItem = true
        ) else getLocalStockList(displayTempItems = false)

    override suspend fun getBaseStockListTotalDetails(): DataResult<Exception, StockListDetails> =
        DataResult.build {
            stockListDetails
        }

    override suspend fun updateStock(stock: Stock, isTemp: Boolean): DataResult<Exception, Unit> =
        if (isTemp) updateLocalStock(stock)
        else updateRemoteStock(stock)

    override suspend fun deleteStock(
        singleItemId: String?, selectedItemsIds: List<String>?, isTemp: Boolean
    ): DataResult<Exception, Unit> =
        if (isTemp) deleteTempLocalStock(singleItemId) // temp data item selection is disabled.
        else deleteRemoteStock(singleItemId, selectedItemsIds)

    override suspend fun updateStockStatus(
        singleItemId: String?, selectedItemsIds: List<String>?, isActive: Boolean
    ): DataResult<Exception, Unit> = DataResult.build {
        if (!isUserActive(userId)) throw DeactivatedException()
        // only admin can reactive item status.
        if (isActive && !isUserAdmin(userId)) throw NotPermittedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val result: HashMap<String, Any> = HashMap()
        result[PAY_ACTIVE_REF_NAME] = isActive
        if (isActive) {
            result[UPDATED_REF_NAME] = ""
            result[UPDATED_DATE_REF_NAME] = ""
        } else {
            result[UPDATED_REF_NAME] = getSignUserName()
            result[UPDATED_DATE_REF_NAME] = getCalendarDateTime("dd/MM/yyyy hh:mm:ss a")
        }
        singleItemId?.let { singleItem ->
            awaitTaskCompletable(
                databaseReference.child(singleItem).updateChildren(result)
            )
        }
        selectedItemsIds?.let { groupOfSelected ->
            groupOfSelected.forEach { singleItem ->
                awaitTaskCompletable(
                    databaseReference.child(singleItem).updateChildren(result)
                )
            }
        }
        clearStockListCache()
    }

    /*
    * Remote
    *
    *
    *
    *
    * */

    override suspend fun getStockListFilteredByQuery(query: String): DataResult<Exception, List<Stock>> =
        getRemoteStockList(queryFiltered = query)


    private suspend fun getRemoteStockList(
        isUpdateLocalRequired: Boolean = false,
        displayOnlyTodayItem: Boolean = false,
        queryFiltered: String? = null,
        isPending: Boolean? = null,
        isBuying: Boolean? = null
    ): DataResult<Exception, List<Stock>> = DataResult.build {
        if (!isUserActive(userId)) throw DeactivatedException()
        if (!hasInternetConnection()) throw NoConnectivityException()
        val task: DataSnapshot = awaitTaskResult(databaseReference.get())
        val result: DataResult<Exception, List<Stock>> =
            if (queryFiltered == null && isPending == null && isBuying == null) resultToStockList(
                task, displayOnlyTodayItem
            )
            else if (queryFiltered != null) resultToStockList(task, query = queryFiltered)
            else resultToStockList(
                task, query = null, isPending = isPending, isBuying = isBuying
            )/*
            * Result.
            *
            * */
        if (result is DataResult.Value) {
            if (isUpdateLocalRequired) result.value.updateLocalStockEntries()
            return@build result.value
        } else throw Exception()
    }

    override suspend fun getStockListFilteredByStatus(
        isPending: Boolean?, isBuying: Boolean?
    ): DataResult<Exception, List<Stock>> = getRemoteStockList(
        queryFiltered = null, isPending = isPending, isBuying = isBuying
    )

    private suspend fun resultToStockList(
        result: DataSnapshot?, displayOnlyTodayItem: Boolean
    ): DataResult<Exception, List<Stock>> = launchAWithContextScope {
        DataResult.build {
            stockListDetails = StockListDetails()
            if (result == null) return@build emptyList()
            val stockList: MutableList<Stock> = mutableListOf()
            val isAllowedToReadListData = isAllowToReadData()
            result.children.forEach { data ->
                data.getValue(RemoteStock::class.java)?.let {
                    if (!displayOnlyTodayItem) stockList.add(it.toStock)
                    else if (it.creationDateCustom?.startsWith(getCalendarDateTime("dd/MM")) == true) stockList.add(
                        it.toStock
                    )

                    /*
                    *
                    *
                    * */
                    if (isAllowedToReadListData) {
                        if (it.operationType == true) {
                            stockListDetails.totalCostAmount += (it.assetGramPrice!! * it.assetQuantity!!).toDoubleLimitation()
                            stockListDetails.totalAssetQuantity =
                                stockListDetails.totalAssetQuantity + it.assetQuantity!!

                        } else {
                            stockListDetails.totalCostAmount -= (it.assetGramPrice!! * it.assetQuantity!!).toDoubleLimitation()
                            stockListDetails.totalAssetQuantity =
                                stockListDetails.totalAssetQuantity - it.assetQuantity!!
                        }
                        stockListDetails.average = getDividedRequiredAveValue(
                            stockListDetails.totalCostAmount, stockListDetails.totalAssetQuantity
                        )
                    }
                }
            }
            return@build stockList.sortedByDescending { it.creationDate }
        }
    }

    private suspend fun resultToStockList(
        result: DataSnapshot?, query: String?, isPending: Boolean? = null, isBuying: Boolean? = null
    ): DataResult<Exception, List<Stock>> = launchAWithContextScope {
        DataResult.build {
            if (result == null) return@build emptyList()
            val stockList: MutableList<Stock> = mutableListOf()
            result.children.forEach { data ->
                data.getValue(RemoteStock::class.java)?.let {
                    if (query == null) {
                        if (it.active == isPending && isBuying == null) stockList.add(it.toStock)
                        else if (it.operationType == isBuying && isPending == null) stockList.add(it.toStock)

                    } else query.let { queryValue ->
                        if (it.creationDateCustom?.startsWith(queryValue) == true) stockList.add(it.toStock)
                        else if (it.city?.lowercase(Locale.ROOT)
                                ?.contains(queryValue.lowercase(Locale.ROOT)) == true
                        ) stockList.add(
                            it.toStock
                        )
                        else if (it.client?.lowercase(Locale.ROOT)
                                ?.contains(queryValue.lowercase(Locale.ROOT)) == true
                        ) stockList.add(
                            it.toStock
                        )
                        else if (it.lastUpdateBy?.lowercase(Locale.ROOT)
                                ?.contains(queryValue.lowercase(Locale.ROOT)) == true
                        ) stockList.add(it.toStock)
                        else Unit
                    }
                }
            }
            return@build stockList.sortedByDescending { it.creationDate }
        }
    }

    override suspend fun pushTempDataToRemoteServer(tempStockList: List<Stock>): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActiveAdmin(userId) && !isUserActivePM(userId)) throw NotPermittedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            tempStockList.forEach {
                awaitTaskCompletable(
                    databaseReference.child(it.creationDate).setValue(it.toRemoteStock)
                )
                stockDao.clearData(isTemp = true)
                clearStockListCache()
            }
        }

    private suspend fun updateRemoteStock(stock: Stock): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActive(userId)) throw DeactivatedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            if (stock.isNewStock()) stock.createdBy = getSignUserName()
            awaitTaskCompletable(
                databaseReference.child(stock.creationDate).setValue(stock.toRemoteStock)
            )
            clearStockListCache()
        }

    private suspend fun deleteRemoteStock(
        stockId: String?, selectedItemsIds: List<String>?
    ): DataResult<Exception, Unit> = DataResult.build {
        if (!isUserActiveAdmin(userId)) throw NotPermittedException()
        if (!hasInternetConnection()) throw NoConnectivityException()

        stockId?.let {
            awaitTaskCompletable(databaseReference.child(it).removeValue())
        }
        selectedItemsIds?.let {
            it.forEach { selectedId ->
                awaitTaskCompletable(databaseReference.child(selectedId).removeValue())
            }
        }
        clearStockListCache()
    }

    /*
    * Local
    *
    *
    *
    * */
    override suspend fun getTempStockList(): DataResult<Exception, List<Stock>> =
        getLocalStockList(displayTempItems = true)

    private suspend fun updateLocalStock(stock: Stock): DataResult<Exception, Unit> =
        DataResult.build {
            if (stock.isNewStock()) stock.createdBy = getSignUserName()
            stockDao.insertOrUpdateStock(stock = stock.toRoomTempStock)
        }

    private suspend fun getLocalStockList(displayTempItems: Boolean): DataResult<Exception, List<Stock>> =
        DataResult.build {
            if (displayTempItems) stockDao.getTempStockList(isTemp = true)
                .toStockListFromRoomStock()
            else stockDao.getStockList(isTemp = false).toStockListFromRoomStock()
        }


    private suspend fun deleteTempLocalStock(stockId: String?): DataResult<Exception, Unit> =
        DataResult.build {
            stockId?.let { stockDao.deleteTempPay(it) }
        }

    private suspend fun List<Stock>.updateLocalStockEntries(): DataResult<Exception, Unit> =
        DataResult.build {
            this.let { remoteList ->
                if (remoteList.isNotEmpty() && dataCacheTime != 0) {
                    stockDao.clearData(isTemp = false)
                    remoteList.forEach { stockDao.insertOrUpdateStock(it.toRoomStock) }
                    utilPref.updateStockListCacheTimeToNow()
                }
            }
        }

    override suspend fun downloadDataBackupsAsExcel(context: Context?): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActive(userId)) throw DeactivatedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            val task: DataSnapshot = awaitTaskResult(databaseReference.get())
            val result: DataResult<Exception, List<Stock>> =
                resultToStockList(task, displayOnlyTodayItem = false)

            if (result is DataResult.Value) {
                exportListToExcel(result.value)
                stopActionService(context)
            } else throw Exception()
        }

    override suspend fun deleteAllCompletedItems(context: Context?): DataResult<Exception, Unit> =
        DataResult.build {
            if (!isUserActiveAdmin(userId)) throw NotPermittedException()
            if (!hasInternetConnection()) throw NoConnectivityException()
            val task: DataSnapshot = awaitTaskResult(databaseReference.get())
            val result: DataResult<Exception, Unit> = resultToStockList(task)
            if (result is DataResult.Value) {
                utilPref.clearStockListCacheTime()
                stopActionService(context)
            } else throw Exception()
        }

    private suspend fun resultToStockList(
        result: DataSnapshot?,
    ): DataResult<Exception, Unit> = launchAWithContextScope {
        DataResult.build {
            if (result == null) return@build
            result.children.forEach { data ->
                data.getValue(RemoteStock::class.java)?.let {
                    if (it.active == false) awaitTaskCompletable(
                        databaseReference.child(it.creationDate.toString()).removeValue()
                    )
                }
            }
        }
    }

    private fun stopActionService(context: Context?) {
        context?.let {
            val serviceIntent = Intent(it, DataBackupsService::class.java)
            serviceIntent.action = STOP_ACTION_SERVICE
            it.startService(serviceIntent)
        }
    }
}
