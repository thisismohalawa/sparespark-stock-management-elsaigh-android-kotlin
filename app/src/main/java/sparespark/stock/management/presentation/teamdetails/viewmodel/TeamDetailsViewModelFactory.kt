package sparespark.stock.management.presentation.teamdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.repository.TeamRepository

class TeamDetailsViewModelFactory(
    private val teamRepo: TeamRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TeamDetailsViewModel(teamRepo, Dispatchers.Main) as T
    }
}
