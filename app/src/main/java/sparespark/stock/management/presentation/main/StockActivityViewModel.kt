package sparespark.stock.management.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sparespark.stock.management.R
import sparespark.stock.management.core.NO_INTERNET_CONNECTION
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.model.other.SettingItem
import sparespark.stock.management.data.reminder.ReminderAPI
import sparespark.stock.management.data.repository.UserRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import kotlin.coroutines.CoroutineContext

class StockActivityViewModel(
    private val userRepo: UserRepository,
    reminderAPI: ReminderAPI,
    uiContext: CoroutineContext
) : BaseViewModel<StockActivityEvent>(uiContext) {

    internal val signAttempt = MutableLiveData<Unit>()
    internal val backupActionDialogAttempt = SingleLiveData<Unit>()
    internal val navigateToTeamAttempt = SingleLiveData<Unit>()
    internal val navigateTempDBAttempt = SingleLiveData<Unit>()

    private val userRoleState = SingleLiveData<Int?>()
    val userRole: LiveData<Int?> get() = userRoleState

    private val userState = SingleLiveData<User?>()
    val user: LiveData<User?> get() = userState

    private val settingsListState = MutableLiveData<List<SettingItem>?>()
    val settingsList: MutableLiveData<List<SettingItem>?> get() = settingsListState

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    init {
        if (reminderAPI.setUpAlarmForAutoBackup() is DataResult.Error)
            showError(R.string.error_set_auto_backup)
    }

    override fun handleEvent(event: StockActivityEvent) {
        when (event) {
            is StockActivityEvent.OnStart -> getLocalUser(checkRemoteUserNeeded = true)
            is StockActivityEvent.GetProfileUserInfo -> getLocalUser(checkRemoteUserNeeded = false)
            is StockActivityEvent.ClearCache -> clearStoredCache()
            is StockActivityEvent.GetProfileSettingsList -> getProfileSettingsMenu()
            is StockActivityEvent.Logout -> moveToSignView()
            is StockActivityEvent.OnTeamSettingClick -> moveToTeamView()
            is StockActivityEvent.OnTempDBSettingClick -> moveToTempDBView()
            is StockActivityEvent.OnBackupsActionSettingClick -> startOptionBackupsDialog()
        }
    }

    override fun onCleared() {
        super.onCleared()
        jobTracker.cancel()
    }

    private fun moveToSignView() {
        signAttempt.value = Unit
    }

    private fun moveToTeamView() {
        navigateToTeamAttempt.value = Unit
    }

    private fun moveToTempDBView() {
        navigateTempDBAttempt.value = Unit
    }

    private fun startOptionBackupsDialog() {
        backupActionDialogAttempt.value = Unit
    }

    private suspend fun userSyncedSuccessfully() {
        updateActionStatusText(R.string.synced_successfully)
        delay(500)
        hideActionEventText()
    }

    private fun bindUserNavUI(userRoleId: Int) {
        if (userRoleState.value != userRoleId) userRoleState.value = userRoleId
    }

    private fun bindUser(user: User) {
        userState.value = user
    }

    private fun getLocalUser(checkRemoteUserNeeded: Boolean) = launch {
        updateActionStatusText(R.string.check_signed_user)
        when (val result: DataResult<Exception, User?> = userRepo.getLocalUser()) {
            is DataResult.Error -> moveToSignView()
            is DataResult.Value -> if (result.value != null) {
                bindUserNavUI(userRoleId = result.value.roleId)

                if (!checkRemoteUserNeeded) {
                    bindUser(result.value)
                    userSyncedSuccessfully()
                } else getRemoteUser()

            } else moveToSignView()
        }
    }

    private fun getRemoteUser(): Job = launch {
        updateActionStatusText(R.string.get_remote_user)
        when (val result: DataResult<Exception, User?> = userRepo.getRemoteUser()) {
            is DataResult.Error -> when (result.error.message) {
                NO_INTERNET_CONNECTION -> updateActionStatusText(
                    R.string.no_internet, isError = true
                )

                else -> moveToSignView()
            }

            is DataResult.Value -> if (result.value != null) {
                bindUserNavUI(userRoleId = result.value.roleId)
                updateLocalUserEntries(remoteUser = result.value)
            } else {
                updateActionStatusText(R.string.cant_find_remote_user, isError = true)
                delay(3000)
                moveToSignView()
            }
        }
    }

    private fun updateLocalUserEntries(remoteUser: User): Job = launch {
        updateActionStatusText(R.string.update_user_data)
        when (userRepo.updateLocalUserEntry(user = remoteUser)) {
            is DataResult.Error -> updateActionStatusText(R.string.error_update_entries)
            is DataResult.Value -> userSyncedSuccessfully()
        }
    }

    private fun getProfileSettingsMenu(): Job = launch {
        when (val result: DataResult<Exception, List<SettingItem>> =
            userRepo.getSettingListByRoleId(userRoleId = userRoleState.value)) {
            is DataResult.Value -> settingsListState.value = result.value
            is DataResult.Error -> updateActionStatusText(R.string.error_get_data)
        }
    }

    private fun clearStoredCache(): Job = launch {
        when (userRepo.clearCacheTimePreference()) {
            is DataResult.Error -> updateActionStatusText(R.string.error_update_entries)
            is DataResult.Value -> updatedState.value = true
        }
    }

}
