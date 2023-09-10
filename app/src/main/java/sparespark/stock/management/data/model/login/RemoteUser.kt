package sparespark.stock.management.data.model.login

data class RemoteUser(
    val uid: String? = "",
    val name: String? = "",
    val email: String? = "",
    val roleId: Int? = 3,
    val activated: Boolean? = false
)
