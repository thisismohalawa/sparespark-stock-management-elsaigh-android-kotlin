package sparespark.stock.management.presentation.clientlist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sparespark.stock.management.core.getCalendarDateTime
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.clientlist.ClientListEvent
import kotlin.coroutines.CoroutineContext

class ClientListViewModel(
    private val clientRepo: ClientRepository,
    private val cityRepo: CityRepository,
    uiContext: CoroutineContext
) : BaseViewModel<ClientListEvent>(uiContext) {

    internal val bottomSheetViewState = MutableLiveData<Int>()

    private val clientState = MutableLiveData<Client>()
    val client: MutableLiveData<Client> get() = clientState

    private val clientListState = MutableLiveData<List<Client>?>()
    val clientList: MutableLiveData<List<Client>?> get() = clientListState

    private val cityListState = MutableLiveData<List<City>?>()
    val cityList: MutableLiveData<List<City>?> get() = cityListState

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private val deletedState = SingleLiveData<Boolean>()
    val deleted: LiveData<Boolean> get() = deletedState


    override fun handleEvent(event: ClientListEvent) {
        when (event) {
            is ClientListEvent.OnDestroy -> jobTracker.cancel()
            is ClientListEvent.OnClientViewStart -> setupCurrentClientView(Client("", "", "", ""))
            is ClientListEvent.OnClientItemClicked -> setupCurrentClientView(event.client)
            is ClientListEvent.GetClientList -> getClientList()
            is ClientListEvent.GetCityList -> getCityList()
            is ClientListEvent.UpdateClient -> updateClient(event.clientName, event.phoneNum)
            is ClientListEvent.DeleteClient -> deleteClient(event.creationDate)
            is ClientListEvent.UpdateBottomSheetToHideState -> updateBottomSheetToHideState()
        }
    }

    fun isBottomSheetAtExpandingState(): Boolean =
        bottomSheetViewState.value != BottomSheetBehavior.STATE_HIDDEN

    fun updateSelectedCity(city: String) {
        clientState.value?.cityName = city
    }

    private fun isANewClient(): Boolean = clientState.value?.creationDate.isNullOrEmpty()

    private fun updateBottomSheetToExpandState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateBottomSheetToHideState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupCurrentClientView(client: Client): Job = launch {
        clientState.value = client
        delay(300)
        updateBottomSheetToExpandState()
    }

    private fun getClientList(): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, List<Client>> = clientRepo.getClientList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> clientListState.value = result.value
        }
        hideLoading()
    }

    private fun getCityList(): Job = launch {
        when (val result: DataResult<Exception, List<City>> = cityRepo.getCityList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> cityListState.value = result.value
        }
    }

    private fun updateClient(
        clientName: String, phoneNum: String
    ): Job = launch {
        showLoading()
        if (isANewClient()) clientState.value?.creationDate = getCalendarDateTime()
        when (val result: DataResult<Exception, Unit> = clientRepo.updateClient(
            clientState.value!!.copy(
                name = clientName, phoneNum = phoneNum
            )
        )) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true
        }
        hideLoading()
    }

    private fun deleteClient(creationDate: String): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit> =
            clientRepo.deleteClient(creationDate = creationDate)) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> deletedState.value = true
        }
        hideLoading()
    }
}
