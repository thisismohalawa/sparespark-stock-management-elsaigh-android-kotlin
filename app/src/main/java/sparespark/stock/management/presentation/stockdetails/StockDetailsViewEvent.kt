package sparespark.stock.management.presentation.stockdetails

import sparespark.stock.management.data.model.stock.Stock


sealed class StockDetailsViewEvent {
    object OnDestroy : StockDetailsViewEvent()
    data class OnStartGetStock(val stock: Stock) : StockDetailsViewEvent()
    object GetClientList : StockDetailsViewEvent()
    data class UpdateStockOperationType(val isBuying: Boolean) : StockDetailsViewEvent()
    data class UpdateStockClientDetails(
        val clientName: String
    ) : StockDetailsViewEvent()

    data class UpdateStock(
        val isTemp: Boolean,
        val gramPrice: Double,
        val quantity: Double,
        val details: String
    ) : StockDetailsViewEvent()
}
