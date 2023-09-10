package sparespark.stock.management.data.local.stock

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(
    tableName = "stock_table",
    indices = [Index("creation_date")]
)
data class RoomStock(

    @PrimaryKey
    @ColumnInfo(name = "creation_date")
    val creationDate: String,

    @ColumnInfo(name = "creation_date_custom")
    val creationDateCustom: String,

    @ColumnInfo(name = "asset_gram_price")
    var assetGramPrice: Double,

    @ColumnInfo(name = "asset_quantity")
    var assetQuantity: Double,

    @ColumnInfo(name = "operation_type")
    var operationType: Boolean,

    @ColumnInfo(name = "created_by")
    var createdBy: String,

    @ColumnInfo(name = "last_update_by")
    var lastUpdateBy: String,

    @ColumnInfo(name = "last_update_date")
    val lastUpdateDate: String,

    @ColumnInfo(name = "city")
    var city: String,

    @ColumnInfo(name = "client")
    var client: String,

    @ColumnInfo(name = "details")
    var details: String,

    @ColumnInfo(name = "active")
    var active: Boolean,

    @ColumnInfo(name = "temp_item")
    var tempItem: Boolean

) : Serializable
