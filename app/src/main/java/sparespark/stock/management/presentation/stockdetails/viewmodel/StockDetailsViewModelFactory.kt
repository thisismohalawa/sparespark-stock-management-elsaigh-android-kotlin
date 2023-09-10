package sparespark.stock.management.presentation.stockdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.ClientRepository
import sparespark.stock.management.data.repository.StockRepository

class StockDetailsViewModelFactory(
    private val stockRepo: StockRepository,
    private val clientRepo: ClientRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockDetailsViewModel(stockRepo, clientRepo, Dispatchers.Main) as T
    }
}
