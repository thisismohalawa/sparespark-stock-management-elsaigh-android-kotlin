package sparespark.stock.management.presentation.filterstocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.data.repository.StockRepository

class FilterStockListViewModelFactory(
    private val stockRepo: StockRepository,
    private val cityRepo: CityRepository,
    private val clientRepo: ClientRepository,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FilterStockListViewModel(
            stockRepo,
            cityRepo,
            clientRepo,
            Dispatchers.Main
        ) as T
    }
}
