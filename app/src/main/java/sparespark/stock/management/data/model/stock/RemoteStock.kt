package sparespark.stock.management.data.model.stock

import java.io.Serializable

data class RemoteStock(
    val creationDate: String? = "",
    val creationDateCustom: String? = "",
    var assetGramPrice: Double? = 0.0,
    var assetQuantity: Double? = 0.0,
    var operationType: Boolean? = true,
    var createdBy: String? = "",
    var lastUpdateBy: String? = "",
    var lastUpdateDate: String? = "",
    val client: String? = "",
    val city: String? = "",
    val details: String? = "",
    var active: Boolean? = true
) : Serializable
