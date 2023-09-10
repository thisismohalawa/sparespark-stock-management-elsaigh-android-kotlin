package sparespark.stock.management.presentation.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.StockRepository

class StockListViewModelFactory(
    private val stockRepo: StockRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockListViewModel(stockRepo, Dispatchers.Main) as T
    }
}
