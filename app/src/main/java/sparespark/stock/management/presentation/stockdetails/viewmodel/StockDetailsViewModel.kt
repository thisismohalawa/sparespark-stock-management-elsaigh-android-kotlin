package sparespark.stock.management.presentation.stockdetails.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sparespark.stock.management.R
import sparespark.stock.management.core.getCalendarDateTime
import sparespark.stock.management.core.getOperationTypeStringValue
import sparespark.stock.management.core.getStockDetailsSharedTitle
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.result.UiResourceResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.data.repository.StockRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.stockdetails.StockDetailsViewEvent
import kotlin.coroutines.CoroutineContext

class StockDetailsViewModel(
    private val stockRepo: StockRepository,
    private val clientRepo: ClientRepository,
    uiContext: CoroutineContext
) : BaseViewModel<StockDetailsViewEvent>(uiContext) {

    internal val updateButtonTextStatus = MutableLiveData<UiResourceResult>()
    internal val updateTypeVisibleStatus = MutableLiveData<Boolean>()
    internal val actionSharingDialogStatus = MutableLiveData<String?>()

    private val stockState = MutableLiveData<Stock>()
    val stock: MutableLiveData<Stock> get() = stockState

    private val clientListState = MutableLiveData<List<Client>?>()
    val clientList: MutableLiveData<List<Client>?> get() = clientListState

    private val updateState = SingleLiveData<Boolean>()
    val update: LiveData<Boolean> get() = updateState

    private fun isANewStock(): Boolean = stockState.value?.creationDate.isNullOrEmpty()

    fun isClientNameValid(): Boolean = (!stockState.value?.client.isNullOrEmpty())

    private fun showAsNewPay() {
        updateTypeVisibleStatus.value = false
        updateButtonTextStatus.value = UiResourceResult.StringResource(R.string.add_item)
    }

    private fun showAsUpdatedPay() {
        updateTypeVisibleStatus.value = true
        updateButtonTextStatus.value = UiResourceResult.StringResource(R.string.update_item)
    }


    override fun handleEvent(event: StockDetailsViewEvent) {
        when (event) {
            is StockDetailsViewEvent.OnDestroy -> jobTracker.cancel()
            is StockDetailsViewEvent.OnStartGetStock -> getStock(
                stock = event.stock,
                firstBind = true
            )

            is StockDetailsViewEvent.GetClientList -> getClientList()
            is StockDetailsViewEvent.UpdateStockOperationType -> updateStockType(event.isBuying)
            is StockDetailsViewEvent.UpdateStockClientDetails -> updateStockClientDetails(event.clientName)
            is StockDetailsViewEvent.UpdateStock -> updateStock(
                event.isTemp, event.gramPrice, event.quantity, event.details
            )
        }
    }

    private fun getStock(stock: Stock, firstBind: Boolean = false): Job = launch {
        stockState.value = stock
        if (isANewStock() && firstBind)
            showAsNewPay()
        else if (firstBind) showAsUpdatedPay()
    }

    private fun getClientList(): Job = launch {
        when (val result: DataResult<Exception, List<Client>> = clientRepo.getClientList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> clientListState.value = result.value
        }
    }

    private fun updateStockType(isBuying: Boolean) {
        stockState.value?.let {
            it.operationType = isBuying
            getStock(stock = it)
        }
    }

    private fun updateStockClientDetails(clientName: String): Job = launch {
        val result = clientRepo.getCityByClientName(clientName)
        stockState.value?.let {
            it.client = clientName
            it.city = if (result is DataResult.Value) result.value.toString()
            else "Unknown"
        }
    }

    private fun updateStock(
        isTemp: Boolean, gramPrice: Double, quantity: Double, details: String
    ): Job = launch {
        showLoading()
        if (isANewStock()) {
            stockState.value?.apply {
                creationDate = System.currentTimeMillis().toString() // for sorting.
                creationDateCustom =
                    getCalendarDateTime("dd/MM/yyyy hh:mm:ss a") // string data format.
            }
        }

        val result: DataResult<Exception, Unit> =
            stockRepo.updateStock(
                stock = stockState.value!!.copy(
                    assetGramPrice = gramPrice, assetQuantity = quantity,
                    details = details
                ),
                isTemp = isTemp
            )
        when (result) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> if (!isTemp)
                actionSharingDialogStatus.value =
                    getStockDetailsSharedTitle(
                        title = "New Updates",
                        date = stockState.value?.creationDateCustom ?: "",
                        type = stockState.value?.operationType.getOperationTypeStringValue(),
                        client = stockState.value?.client ?: "",
                        city = stockState.value?.city ?: "",
                        gramPrice = gramPrice,
                        quantity = quantity
                    )
            else updateState.value = true
        }
        hideLoading()
    }
}
