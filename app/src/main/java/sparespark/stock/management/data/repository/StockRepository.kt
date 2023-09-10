package sparespark.stock.management.data.repository

import android.content.Context
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.model.other.StockListDetails
import sparespark.stock.management.data.model.stock.Stock

interface StockRepository {
    suspend fun getStockList(): DataResult<Exception, List<Stock>>
    suspend fun getTempStockList(): DataResult<Exception, List<Stock>>
    suspend fun getStockListFilteredByQuery(query: String): DataResult<Exception, List<Stock>>
    suspend fun getBaseStockListTotalDetails(): DataResult<Exception, StockListDetails>

    suspend fun getStockListFilteredByStatus(
        isPending: Boolean?,
        isBuying: Boolean?
    ): DataResult<Exception, List<Stock>>

    suspend fun updateStockStatus(
        singleItemId: String?,
        selectedItemsIds: List<String>?,
        isActive: Boolean
    ): DataResult<Exception, Unit>

    suspend fun updateStock(
        stock: Stock,
        isTemp: Boolean
    ): DataResult<Exception, Unit>


    suspend fun deleteStock(
        singleItemId: String?,
        selectedItemsIds: List<String>?,
        isTemp: Boolean
    ): DataResult<Exception, Unit>

    suspend fun pushTempDataToRemoteServer(
        tempStockList: List<Stock>
    ): DataResult<Exception, Unit>

    suspend fun downloadDataBackupsAsExcel(context: Context?): DataResult<Exception, Unit>

    suspend fun deleteAllCompletedItems(context: Context?): DataResult<Exception, Unit>

}
