package sparespark.stock.management.presentation.stocklist

import sparespark.stock.management.data.model.stock.Stock

sealed class StockListEvent {
    object OnDestroy : StockListEvent()
    object GetStockList : StockListEvent()
    object GetTempStockList : StockListEvent()
    object GetCityList : StockListEvent()
    object GetClientList : StockListEvent()
    object PushTempDataToServer : StockListEvent()
    data class UpdateStockStatus(
        var itemId: String?,
        var selectedItems: List<String>?,
        val isActive: Boolean
    ) : StockListEvent()

    data class DeleteStock(
        var itemId: String?,
        var selectedItems: List<String>?,
        val isTemp: Boolean
    ) : StockListEvent()

    data class OnStockItemClicked(val stock: Stock) : StockListEvent()
    data class FilterPayListByQuery(val query: String) : StockListEvent()
    data class UpdatePayPendingType(val isPending: Boolean) : StockListEvent()
    data class UpdatePayOperationType(val isBuying: Boolean) : StockListEvent()
}
