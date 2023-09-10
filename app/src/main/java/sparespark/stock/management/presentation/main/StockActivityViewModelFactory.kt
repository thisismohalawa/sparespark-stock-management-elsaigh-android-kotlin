package sparespark.stock.management.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.stock.management.data.reminder.ReminderAPI
import sparespark.stock.management.data.repository.UserRepository

class StockActivityViewModelFactory(
    private val userRepo: UserRepository,
    private val reminderAPI: ReminderAPI
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockActivityViewModel(
            userRepo = userRepo,
            reminderAPI,
            Dispatchers.Main
        ) as T
    }
}
