package sparespark.stock.management.core

import sparespark.stock.management.R
import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.model.other.SettingItem
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.data.model.team.MainTeamItem

fun getDefSettingMenuList(): DataResult<Exception, List<SettingItem>> = DataResult.build {
    val items: MutableList<SettingItem> = mutableListOf()
    items.add(
        SettingItem(
            id = SETTINGS_CLEAR_CACHE,
            title = R.string.invalid_cache,
            subTitle = R.string.invalid_cache_info
        )
    )
    items.add(
        SettingItem(
            id = SETTINGS_LOGOUT, title = R.string.logout, subTitle = R.string.logout_
        )
    )
    return@build items
}

fun getAdminSettingMenuList() = DataResult.build {
    val items: MutableList<SettingItem> = mutableListOf()
    items.add(
        SettingItem(
            id = SETTINGS_TEAM, title = R.string.team, subTitle = R.string.team_info
        )
    )
    items.add(
        SettingItem(
            id = SETTINGS_CLEAR_CACHE,
            title = R.string.invalid_cache,
            subTitle = R.string.invalid_cache_info
        )
    )
    items.add(
        SettingItem(
            id = SETTING_TEMP, title = R.string.temp_database, subTitle = R.string.temp_database
        )
    )
    items.add(
        SettingItem(
            id = SETTING_DELETE_COMPLETED_ITEMS, title = R.string.delete_completed_item,
            subTitle = R.string.delete_permanently
        )
    )
    items.add(
        SettingItem(
            id = SETTINGS_LOGOUT, title = R.string.logout, subTitle = R.string.logout_
        )
    )
    return@build items
}

fun getMutableTeamFilteredList(users: List<User>?): List<MainTeamItem> = try {
    val team: MutableList<MainTeamItem> = mutableListOf()
    val admins: MutableList<User> = mutableListOf()
    val pms: MutableList<User> = mutableListOf()
    val others: MutableList<User> = mutableListOf()
    users?.let {
        if (it.isNotEmpty())
            it.forEach { user ->
                when (user.roleId) {
                    ADMIN_ROLE_ID, OWNER_ROLE_ID -> admins.add(user)
                    PM_ROLE_ID -> pms.add(user)
                    else -> others.add(user)
                }
            }
    }
    team.add(
        MainTeamItem(
            id = 1, title = R.string.admins, team = admins
        )
    )
    team.add(
        MainTeamItem(
            id = 2, title = R.string.pms, team = pms
        )
    )
    team.add(
        MainTeamItem(
            id = 3, title = R.string.team, team = others
        )
    )
    team
} catch (ex: Exception) {
    ex.printStackTrace()
    emptyList()
}

fun newStock() = Stock(
    city = "",
    client = "",
    creationDate = "",
    creationDateCustom = "",
    createdBy = "",
    operationType = false,
    assetQuantity = 0.0,
    assetGramPrice = 0.0,
    lastUpdateDate = "",
    lastUpdateBy = "",
    details = "",
    active = true,
)
