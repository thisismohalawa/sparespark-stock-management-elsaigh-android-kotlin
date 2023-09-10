package sparespark.stock.management.data.local.preference.user

interface UserPreference {
    fun getSignedUserId(): String?
    fun updateSignedUserId(userId: String)
}
