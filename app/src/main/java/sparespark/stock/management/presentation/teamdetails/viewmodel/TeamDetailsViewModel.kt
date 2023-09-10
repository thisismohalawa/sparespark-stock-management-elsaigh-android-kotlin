package sparespark.stock.management.presentation.teamdetails.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.repository.TeamRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.teamdetails.TeamDetailsViewEvent
import kotlin.coroutines.CoroutineContext

class TeamDetailsViewModel(
    private val teamRepo: TeamRepository,
    uiContext: CoroutineContext
) : BaseViewModel<TeamDetailsViewEvent>(uiContext) {

    internal val bottomSheetViewState = MutableLiveData<Int>()

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private val userState = MutableLiveData<User?>()
    val user: MutableLiveData<User?> get() = userState

    override fun handleEvent(event: TeamDetailsViewEvent) {
        when (event) {
            is TeamDetailsViewEvent.OnDestroy -> jobTracker.cancel()
            is TeamDetailsViewEvent.UpdateBottomSheetToHideState -> updateBottomSheetToHideState()
            is TeamDetailsViewEvent.OnTeamDetailsStartGetUser -> setUser(event.user)
            is TeamDetailsViewEvent.UpdateUserAdminStatus -> updateUserAdmin(event.isAdmin)
            is TeamDetailsViewEvent.UpdateUserPmStatus -> updateUserPm(event.isPm)
        }
    }

    private fun setUser(user: User) = launch {
        userState.value = user
        delay(300)
        updateBottomSheetToExpandState()
    }

    fun isBottomSheetAtExpandingState(): Boolean =
        bottomSheetViewState.value != BottomSheetBehavior.STATE_HIDDEN

    private fun updateBottomSheetToExpandState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateBottomSheetToHideState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun updateUserAdmin(isAdmin: Boolean): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit>? =
            userState.value?.let { teamRepo.updateUserAdminStatus(it.uid, isAdmin) }) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true
            else -> Unit
        }
        hideLoading()
    }

    private fun updateUserPm(isPm: Boolean): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit>? =
            userState.value?.uid?.let { teamRepo.updateUserPmStatus(it, isPm) }) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true
            else -> Unit
        }
        hideLoading()
    }
}
