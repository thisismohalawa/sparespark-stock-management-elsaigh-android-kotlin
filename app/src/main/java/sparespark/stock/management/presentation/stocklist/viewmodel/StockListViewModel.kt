package sparespark.stock.management.presentation.stocklist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sparespark.stock.management.R
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.reminder.ReminderAPI
import sparespark.stock.management.data.model.other.StockListDetails
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.data.repository.StockRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.stocklist.StockListEvent
import kotlin.coroutines.CoroutineContext

class StockListViewModel(
    private val stockRepo: StockRepository,
    uiContext: CoroutineContext
) : BaseViewModel<StockListEvent>(uiContext) {

    internal val stockItemClickedNavigateAttempt = SingleLiveData<Stock>()

    private val stockListState = MutableLiveData<List<Stock>?>()
    val stocklist: MutableLiveData<List<Stock>?> get() = stockListState

    private val stockListDetailsState = MutableLiveData<StockListDetails?>()
    val stockListDetails: MutableLiveData<StockListDetails?> get() = stockListDetailsState

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private fun isStockListExists(): Boolean = stockListState.value?.isNotEmpty() == true



    override fun handleEvent(event: StockListEvent) {
        when (event) {
            is StockListEvent.OnDestroy -> jobTracker.cancel()
            is StockListEvent.GetStockList -> getStockList()
            is StockListEvent.UpdateStockStatus -> updateStockActivationStatus(
                event.itemId, event.selectedItems, event.isActive
            )

            is StockListEvent.GetTempStockList -> getStockList(displayTempItem = true)
            is StockListEvent.PushTempDataToServer -> pushTempListToServer()
            is StockListEvent.OnStockItemClicked -> stockItemClickedNavigateAttempt.value =
                event.stock

            is StockListEvent.DeleteStock -> deleteStock(
                event.itemId, event.selectedItems, event.isTemp
            )

            else -> Unit
        }
    }

    private fun getStockList(displayTempItem: Boolean = false): Job = launch {
        showLoading()
        val result: DataResult<Exception, List<Stock>> =
            if (displayTempItem) stockRepo.getTempStockList() else stockRepo.getStockList()

        when (result) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()

            is DataResult.Value -> if (result.value.isNotEmpty()) stockListState.value =
                result.value
            else showError(R.string.no_data)
        }
        getTotalStockListDetails()
        hideLoading()
    }

    private fun getTotalStockListDetails(): Job = launch {
        when (val result = stockRepo.getBaseStockListTotalDetails()) {
            is DataResult.Value -> stockListDetailsState.value = result.value
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
        }
    }

    private fun updateStockActivationStatus(
        itemId: String?, selectedItemsIds: List<String>?, active: Boolean
    ): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit> = stockRepo.updateStockStatus(
            singleItemId = itemId, selectedItemsIds = selectedItemsIds, isActive = active
        )) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true
        }
        hideLoading()
    }

    private fun deleteStock(
        itemId: String?, selectedItemsIds: List<String>?, isTemp: Boolean
    ): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit> = stockRepo.deleteStock(
            singleItemId = itemId, selectedItemsIds = selectedItemsIds, isTemp = isTemp
        )) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true
        }
        hideLoading()
    }

    private fun pushTempListToServer(): Job = launch {
        if (isStockListExists()) {
            showLoading()
            when (val result: DataResult<Exception, Unit>? = stocklist.value?.let {
                stockRepo.pushTempDataToRemoteServer(
                    tempStockList = it
                )
            }) {
                is DataResult.Value -> updatedState.value = true
                is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
                else -> {}
            }
            hideLoading()

        }
    }
}
