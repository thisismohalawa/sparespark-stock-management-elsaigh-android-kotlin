package sparespark.stock.management.data.model.login

import java.io.Serializable

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val roleId: Int,
    val activated: Boolean
) : Serializable
