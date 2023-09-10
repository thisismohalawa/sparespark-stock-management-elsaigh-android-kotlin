package sparespark.stock.management.core

import com.google.firebase.auth.FirebaseUser
import sparespark.stock.management.data.local.city.RoomCity
import sparespark.stock.management.data.local.client.RoomClient
import sparespark.stock.management.data.local.stock.RoomStock
import sparespark.stock.management.data.local.user.RoomUser
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.data.model.city.RemoteCity
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.data.model.client.RemoteClient
import sparespark.stock.management.data.model.login.RemoteUser
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.data.model.stock.RemoteStock
import sparespark.stock.management.data.model.stock.Stock

private fun isOwner(signedEmail: String?): Boolean {
    return signedEmail == "m7md7lwa@gmail.com" ||
            signedEmail == "eslamelsaigh@gmail.com"
}

internal val RemoteUser.toUser: User
    get() = User(
        uid = this.uid ?: "",
        name = this.name ?: "",
        email = this.email ?: "",
        roleId = this.roleId ?: if (isOwner(this.email)) OWNER_ROLE_ID else
            EMPLOYEE_ROLE_ID,
        activated = this.activated ?: if (isOwner(this.email)) ACTIVE else
            !ACTIVE
    )
internal val FirebaseUser.toUser: User
    get() = User(
        uid = this.uid,
        name = this.displayName ?: "",
        email = this.email ?: "",
        roleId = if (isOwner(this.email)) OWNER_ROLE_ID else
            EMPLOYEE_ROLE_ID,
        activated = isOwner(this.email)
    )
internal val RoomUser.toUser: User
    get() = User(
        uid = this.uid,
        name = this.name,
        email = this.email,
        roleId = this.roleId,
        activated = this.activated
    )
internal val RoomStock.toStock: Stock
    get() = Stock(
        creationDate = this.creationDate,
        creationDateCustom = this.creationDateCustom,
        operationType = this.operationType,
        assetGramPrice = this.assetGramPrice,
        assetQuantity = this.assetQuantity,
        createdBy = this.createdBy,
        lastUpdateBy = this.lastUpdateBy,
        lastUpdateDate = this.lastUpdateDate,
        city = this.city,
        client = this.client,
        details = this.details,
        active = this.active
    )
internal val RemoteStock.toStock: Stock
    get() = Stock(
        creationDate = this.creationDate ?: "",
        creationDateCustom = this.creationDateCustom ?: "",
        assetGramPrice = this.assetGramPrice ?: 0.0,
        assetQuantity = this.assetQuantity ?: 0.0,
        operationType = this.operationType ?: true,
        createdBy = this.createdBy ?: "",
        lastUpdateBy = this.lastUpdateBy ?: "",
        lastUpdateDate = this.lastUpdateDate ?: "",
        city = this.city ?: "",
        client = this.client ?: "",
        details = this.details ?: "",
        active = this.active ?: true
    )

internal val User.toRemoteUser: RemoteUser
    get() = RemoteUser(
        uid = this.uid,
        name = this.name,
        email = this.email,
        roleId = this.roleId,
        activated = this.activated
    )

internal val RemoteCity.toCity: City
    get() = City(
        name = this.name ?: "", creationDate = this.creationDate ?: ""
    )
internal val RemoteClient.toClient: Client
    get() = Client(
        name = this.name ?: "",
        creationDate = this.creationDate ?: "",
        phoneNum = this.phoneNum ?: "",
        cityName = this.cityName ?: ""
    )
internal val City.toRoomCity: RoomCity
    get() = RoomCity(
        name = this.name, creationDate = this.creationDate
    )
internal val Client.toRoomClient: RoomClient
    get() = RoomClient(
        name = this.name,
        creationDate = this.creationDate,
        phoneNum = this.phoneNum,
        cityName = this.cityName,
    )
internal val User.toRoomUser: RoomUser
    get() = RoomUser(
        uid = this.uid,
        name = this.name,
        email = this.email,
        roleId = this.roleId,
        activated = this.activated
    )
internal val Stock.toRoomStock: RoomStock
    get() = RoomStock(
        creationDate = this.creationDate,
        creationDateCustom = this.creationDateCustom,
        assetGramPrice = this.assetGramPrice,
        assetQuantity = this.assetQuantity,
        operationType = this.operationType,
        createdBy = this.createdBy,
        lastUpdateBy = this.lastUpdateBy,
        lastUpdateDate = this.lastUpdateDate,
        city = this.city,
        client = this.client,
        active = this.active,
        details = this.details,
        tempItem = false
    )
internal val Stock.toRoomTempStock: RoomStock
    get() = RoomStock(
        city = this.city,
        client = this.client,
        creationDate = this.creationDate,
        creationDateCustom = this.creationDateCustom,
        createdBy = this.createdBy,
        operationType = this.operationType,
        assetGramPrice = this.assetGramPrice,
        assetQuantity = this.assetQuantity,
        lastUpdateDate = this.lastUpdateDate,
        lastUpdateBy = this.lastUpdateBy,
        details = this.details,
        active = this.active,
        tempItem = true
    )
internal val Stock.toRemoteStock: RemoteStock
    get() = RemoteStock(
        city = this.city,
        client = this.client,
        creationDate = this.creationDate,
        creationDateCustom = this.creationDateCustom,
        createdBy = this.createdBy,
        operationType = this.operationType,
        assetGramPrice = this.assetGramPrice,
        assetQuantity = this.assetQuantity,
        lastUpdateDate = this.lastUpdateDate,
        lastUpdateBy = this.lastUpdateBy,
        details = this.details,
        active = this.active
    )
internal val RoomCity.toCity: City
    get() = City(
        name = this.name, creationDate = this.creationDate
    )
internal val RoomClient.toClient: Client
    get() = Client(
        name = this.name,
        creationDate = this.creationDate,
        phoneNum = this.phoneNum,
        cityName = this.cityName
    )
internal val City.toRemoteCity: RemoteCity
    get() = RemoteCity(
        name = this.name, creationDate = this.creationDate
    )
internal val Client.toRemoteClient: RemoteClient
    get() = RemoteClient(
        name = this.name,
        creationDate = this.creationDate,
        phoneNum = this.phoneNum,
        cityName = this.cityName,
    )

internal fun List<RoomCity>.toCityListFromRoomCity(): List<City> = this.flatMap {
    listOf(it.toCity)
}

internal fun List<RoomClient>.toClientListFromRoomClient(): List<Client> = this.flatMap {
    listOf(it.toClient)
}

internal fun List<RoomUser>.toUserListFromRoomUser(): List<User> = this.flatMap {
    listOf(it.toUser)
}

internal fun List<RoomStock>.toStockListFromRoomStock(): List<Stock> = this.flatMap {
    listOf(it.toStock)
}
