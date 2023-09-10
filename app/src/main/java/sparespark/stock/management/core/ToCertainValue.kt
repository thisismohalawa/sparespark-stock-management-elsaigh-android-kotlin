package sparespark.stock.management.core

import android.text.Editable
import sparespark.stock.management.core.view.getDividedRequiredAveValue
import sparespark.stock.management.core.view.toStringFullNumberFormat
import sparespark.stock.management.data.model.other.StockListDetails
import sparespark.stock.management.data.model.stock.Stock
import kotlin.math.roundToInt

internal fun Int?.isAdmin(): Boolean = this == OWNER_ROLE_ID || this == ADMIN_ROLE_ID
internal fun Int?.isPM(): Boolean = this == PM_ROLE_ID
internal fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
internal fun Boolean?.getOperationTypeStringValue(): String =
    if (isDeviceLanguageArabic()) if (this == true) "شراء" else "بيع"
    else if (this == true) "Buy" else "Sell"

internal fun Boolean?.getActiveTypeStringValue(): String =
    if (isDeviceLanguageArabic()) if (this == true) "قيد التنفيذ" else "مكتمل"
    else if (this == true) "Pending" else "Completed"

internal fun Double.toDoubleLimitation(): Double = try {
    (this * 100.0).roundToInt() / 100.0
} catch (e: Exception) {
    this
}

internal fun getStockDetailsSharedTitle(
    title: String,
    date: String,
    type: String,
    client: String,
    city: String,
    gramPrice: Double,
    quantity: Double
): String =
    "$title:\nDate: $date\nType: $type\nClient: $client\nCity: $city\nGram Price: ${gramPrice.toStringFullNumberFormat()} LE\n" +
            "Quantity: ${quantity.toStringFullNumberFormat()} G\n" + "Total: ${
        (gramPrice * quantity).toDoubleLimitation().toStringFullNumberFormat()
    } LE"

suspend fun List<Stock>.filterStockListByOperationType(isBuying: Boolean): List<Stock> =
    launchAWithContextScope {
        val filteredList: MutableList<Stock> = mutableListOf()
        this.forEach { stock ->
            if (stock.operationType == isBuying) filteredList.add(stock)
        }
        return@launchAWithContextScope filteredList
    }

suspend fun List<Stock>.filterStockListByActiveType(isActive: Boolean): List<Stock> =
    launchAWithContextScope {
        val filteredList: MutableList<Stock> = mutableListOf()
        this.forEach { stock ->
            if (stock.active == isActive) filteredList.add(stock)
        }
        return@launchAWithContextScope filteredList
    }

suspend fun List<Stock>?.getListDetails(): StockListDetails = launchAWithContextScope {
    val stockListDetails = StockListDetails()
    this?.forEach {
        if (it.operationType) {
            stockListDetails.totalCostAmount += (it.assetGramPrice * it.assetQuantity).toDoubleLimitation()
            stockListDetails.totalAssetQuantity += it.assetQuantity
        } else {
            stockListDetails.totalCostAmount -= (it.assetGramPrice * it.assetQuantity).toDoubleLimitation()
            stockListDetails.totalAssetQuantity -= it.assetQuantity
        }
        stockListDetails.average = getDividedRequiredAveValue(
            stockListDetails.totalCostAmount, stockListDetails.totalAssetQuantity
        )
    }
    return@launchAWithContextScope stockListDetails
}
