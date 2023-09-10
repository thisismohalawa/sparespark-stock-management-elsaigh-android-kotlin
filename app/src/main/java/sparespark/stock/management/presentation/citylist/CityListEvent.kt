package sparespark.stock.management.presentation.citylist

import sparespark.stock.management.data.model.city.City

sealed class CityListEvent {
    object OnDestroy : CityListEvent()
    object GetCityList : CityListEvent()
    object OnCityViewStart : CityListEvent()
    data class OnCityItemClicked(val city: City) : CityListEvent()
    data class UpdateCity(val cityName: String) : CityListEvent()
    data class DeleteCity(val creationDate: String) : CityListEvent()
    object UpdateBottomSheetToHideState : CityListEvent()
}
