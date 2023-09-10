package sparespark.stock.management.data.model.stock

import java.io.Serializable

data class Stock(
    var creationDate: String,
    var creationDateCustom: String,
    var assetGramPrice: Double,
    var assetQuantity: Double,
    var operationType: Boolean,
    var createdBy: String,
    var lastUpdateBy: String,
    var lastUpdateDate: String,
    var client: String,
    var city: String,
    val details: String,
    var active: Boolean,
) : Serializable
