package sparespark.stock.management.presentation.teamdetails

import sparespark.stock.management.data.model.login.User

sealed class TeamDetailsViewEvent {
    object OnDestroy : TeamDetailsViewEvent()
    object UpdateBottomSheetToHideState : TeamDetailsViewEvent()
    data class OnTeamDetailsStartGetUser(val user: User) : TeamDetailsViewEvent()
    data class UpdateUserAdminStatus(val isAdmin: Boolean) : TeamDetailsViewEvent()
    data class UpdateUserPmStatus(val isPm: Boolean) : TeamDetailsViewEvent()
}
