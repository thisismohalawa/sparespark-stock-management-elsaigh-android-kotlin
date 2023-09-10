package sparespark.stock.management.data.model.team

import sparespark.stock.management.data.model.login.User

data class MainTeamItem(
    val id: Int,
    val title: Int,
    var team: List<User> = emptyList()
)
