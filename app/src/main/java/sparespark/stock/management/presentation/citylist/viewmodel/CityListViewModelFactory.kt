package sparespark.stock.management.presentation.citylist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.CityRepository

class CityListViewModelFactory(
    private val cityRep: CityRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CityListViewModel(cityRep, Dispatchers.Main) as T
    }
}
