package sparespark.stock.management.presentation.clientlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.CityRepository
import sparespark.stock.management.data.repository.ClientRepository

class ClientListViewModelFactory(
    private val clientRepo: ClientRepository,
    private val cityRepo: CityRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClientListViewModel(clientRepo, cityRepo, Dispatchers.Main) as T
    }
}
