package sparespark.stock.management.presentation.teamlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.TeamRepository

class TeamListViewModelFactory(
    private val teamRepo: TeamRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TeamListViewModel(teamRepo, Dispatchers.Main) as T
    }
}
