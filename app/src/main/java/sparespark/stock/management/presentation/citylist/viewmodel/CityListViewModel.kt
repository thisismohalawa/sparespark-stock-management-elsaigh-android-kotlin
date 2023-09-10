package sparespark.stock.management.presentation.citylist.viewmodel

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
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.citylist.CityListEvent
import kotlin.coroutines.CoroutineContext

class CityListViewModel(
    private val cityRepo: CityRepository, uiContext: CoroutineContext
) : BaseViewModel<CityListEvent>(uiContext) {

    internal val bottomSheetViewState = MutableLiveData<Int>()

    private val cityState = MutableLiveData<City>()
    val city: MutableLiveData<City> get() = cityState

    private val cityListState = MutableLiveData<List<City>?>()
    val cityList: MutableLiveData<List<City>?> get() = cityListState

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private val deletedState = SingleLiveData<Boolean>()
    val deleted: LiveData<Boolean> get() = deletedState

    override fun handleEvent(event: CityListEvent) {
        when (event) {
            is CityListEvent.OnDestroy -> jobTracker.cancel()
            is CityListEvent.UpdateBottomSheetToHideState -> updateBottomSheetToHideState()
            is CityListEvent.OnCityViewStart -> setupCurrentCityView(City("", ""))
            is CityListEvent.OnCityItemClicked -> setupCurrentCityView(event.city)
            is CityListEvent.GetCityList -> getCityList()
            is CityListEvent.UpdateCity -> updateCity(event.cityName)
            is CityListEvent.DeleteCity -> deleteCity(event.creationDate)
        }
    }

    fun isBottomSheetAtExpandingState(): Boolean =
        bottomSheetViewState.value != BottomSheetBehavior.STATE_HIDDEN

    private fun isANewCity(): Boolean = cityState.value?.creationDate.isNullOrEmpty()

    private fun updateBottomSheetToExpandState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateBottomSheetToHideState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun getCityList(): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, List<City>> = cityRepo.getCityList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> cityListState.value = result.value
        }
        hideLoading()
    }

    private fun setupCurrentCityView(city: City): Job = launch {
        cityState.value = city
        delay(300)
        updateBottomSheetToExpandState()
    }

    private fun updateCity(cityName: String): Job = launch {
        showLoading()
        if (isANewCity()) cityState.value?.creationDate = getCalendarDateTime()
        if (cityName == cityState.value?.name) return@launch
        when (val result: DataResult<Exception, Unit> = cityRepo.updateCity(
            cityState.value!!.copy(
                name = cityName
            )
        )) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true

        }
        hideLoading()
    }

    private fun deleteCity(creationDate: String): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit> =
            cityRepo.deleteCity(creationDate = creationDate)) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value ->
                deletedState.value = true
        }
        hideLoading()
    }
}
