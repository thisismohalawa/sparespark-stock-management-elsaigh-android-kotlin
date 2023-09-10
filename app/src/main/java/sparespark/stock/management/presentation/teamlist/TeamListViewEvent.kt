package sparespark.stock.management.presentation.teamlist

import sparespark.stock.management.data.model.login.User

sealed class TeamListViewEvent {
    object OnDestroy : TeamListViewEvent()
    object GetTeamList : TeamListViewEvent()
    data class UpdateUserActiveStatus(val uid: String, val isActive: Boolean) : TeamListViewEvent()
    data class OnTeamMemberClick(val user: User) : TeamListViewEvent()
}
