package sparespark.stock.management.presentation.filterstocklist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sparespark.stock.management.R
import sparespark.stock.management.core.filterStockListByActiveType
import sparespark.stock.management.core.filterStockListByOperationType
import sparespark.stock.management.core.getListDetails
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.data.model.other.StockListDetails
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.data.repository.StockRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.stocklist.StockListEvent
import kotlin.coroutines.CoroutineContext

class FilterStockListViewModel(
    private val stockRepo: StockRepository,
    private val cityRepo: CityRepository,
    private val clientRepo: ClientRepository,
    uiContext: CoroutineContext
) : BaseViewModel<StockListEvent>(uiContext) {

    private val payOperationTypeStatus = MutableLiveData<Boolean?>()
    private val payPendingTypeStatus = MutableLiveData<Boolean?>()
    internal val stockItemClickedNavigateAttempt = SingleLiveData<Stock>()

    private val filteredListState = MutableLiveData<List<Stock>?>()
    val filteredList: MutableLiveData<List<Stock>?> get() = filteredListState

    private val cityListState = MutableLiveData<List<City>?>()
    val cityList: MutableLiveData<List<City>?> get() = cityListState

    private val clientListState = MutableLiveData<List<Client>?>()
    val clientList: MutableLiveData<List<Client>?> get() = clientListState

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private val filteredListDetailsState = SingleLiveData<StockListDetails>()
    val filteredListDetails: LiveData<StockListDetails> get() = filteredListDetailsState

    fun isOperationTypeFiltered(): Boolean = payOperationTypeStatus.value != null

    fun isOperationPendingTypeFiltered(): Boolean = payPendingTypeStatus.value != null

    private fun isFilterDataListExists(): Boolean = filteredListState.value?.isNotEmpty() == true

    override fun handleEvent(event: StockListEvent) {
        when (event) {
            is StockListEvent.OnDestroy -> jobTracker.cancel()
            is StockListEvent.GetCityList -> getCityList()
            is StockListEvent.GetClientList -> getClientList()
            is StockListEvent.FilterPayListByQuery -> filterStockListByQuery(event.query)
            is StockListEvent.UpdatePayOperationType -> updatePayOperationType(event.isBuying)
            is StockListEvent.UpdatePayPendingType -> updatePayPendingType(event.isPending)
            is StockListEvent.OnStockItemClicked -> stockItemClickedNavigateAttempt.value =
                event.stock

            is StockListEvent.UpdateStockStatus -> updateStockActivationStatus(
                event.itemId, event.selectedItems, event.isActive
            )

            else -> Unit

        }
    }

    private suspend fun getTotalFilteredListDetails() {
        filteredListDetailsState.value = filteredListState.value.getListDetails()
    }

    private fun filterStockListByQuery(query: String) = launch {
        showLoading()
        filteredListState.value = emptyList()
        when (val result: DataResult<Exception, List<Stock>> =
            stockRepo.getStockListFilteredByQuery(query = query)) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()

            is DataResult.Value -> if (result.value.isNotEmpty()) {
                filteredListState.value = result.value
                getTotalFilteredListDetails()
                showError(R.string.nothing)
            } else showError(R.string.no_data)
        }
        hideLoading()
    }

    private fun filterStockListByStatus(
        isPending: Boolean?, isBuying: Boolean?
    ) = launch {
        showLoading()
        filteredListState.value = emptyList()
        when (val result: DataResult<Exception, List<Stock>> =
            stockRepo.getStockListFilteredByStatus(isPending = isPending, isBuying = isBuying)) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> if (result.value.isNotEmpty()) {
                filteredListState.value = result.value
                getTotalFilteredListDetails()
                showError(R.string.nothing)
            } else showError(R.string.no_data)
        }
        hideLoading()
    }

    private fun getCityList(): Job = launch {
        when (val result: DataResult<Exception, List<City>> = cityRepo.getCityList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> cityListState.value = result.value
        }
    }

    private fun getClientList(): Job = launch {
        when (val result: DataResult<Exception, List<Client>> = clientRepo.getClientList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> clientListState.value = result.value
        }
    }

    private fun updatePayPendingType(isPending: Boolean): Job = launch {
        payPendingTypeStatus.value = isPending
        if (isFilterDataListExists()) {
            filteredListState.value =
                filteredListState.value?.filterStockListByActiveType(
                    isActive = isPending
                )
            getTotalFilteredListDetails()
        } else filterStockListByStatus(isPending = isPending, isBuying = null)
    }

    private fun updatePayOperationType(isBuying: Boolean): Job = launch {
        payOperationTypeStatus.value = isBuying
        if (isFilterDataListExists()) {
            filteredListState.value = filteredListState.value?.filterStockListByOperationType(
                isBuying = isBuying
            )
            getTotalFilteredListDetails()
        } else filterStockListByStatus(isPending = null, isBuying = isBuying)
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

}
