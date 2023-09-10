package sparespark.stock.management.presentation.teamlist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.core.view.SingleLiveData
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.repository.TeamRepository
import sparespark.stock.management.presentation.base.BaseViewModel
import sparespark.stock.management.presentation.teamlist.TeamListViewEvent
import kotlin.coroutines.CoroutineContext

class TeamListViewModel(
    private val teamRepo: TeamRepository,
    uiContext: CoroutineContext
) : BaseViewModel<TeamListViewEvent>(uiContext) {

    internal val teamItemClickedNavigateAttempt = SingleLiveData<User>()

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private val userListState = MutableLiveData<List<User>?>()
    val userList: MutableLiveData<List<User>?> get() = userListState

    override fun handleEvent(event: TeamListViewEvent) {
        when (event) {
            is TeamListViewEvent.OnDestroy -> jobTracker.cancel()
            is TeamListViewEvent.GetTeamList -> getTeamList()
            is TeamListViewEvent.UpdateUserActiveStatus -> updateUserActiveStatus(
                event.uid, event.isActive
            )

            is TeamListViewEvent.OnTeamMemberClick -> startTeamDetailsView(event.user)
        }
    }


    private fun getTeamList(): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, List<User>> = teamRepo.getTeamList()) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> userListState.value = result.value
        }
        hideLoading()
    }


    private fun updateUserActiveStatus(uid: String, isActive: Boolean): Job = launch {
        showLoading()
        when (val result: DataResult<Exception, Unit> =
            teamRepo.updateUserActiveStatus(uid, isActive)) {
            is DataResult.Error -> result.error.message.resultErrorMsgTrigger()
            is DataResult.Value -> updatedState.value = true
        }
        hideLoading()
    }

    private fun startTeamDetailsView(user: User) {
        teamItemClickedNavigateAttempt.value = user
    }
}
