package sparespark.stock.management.data.local.client

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(
    tableName = "client_table",
    indices = [Index("creation_date")]
)
data class RoomClient(

    @ColumnInfo(name = "name")
    var name: String,

    @PrimaryKey
    @ColumnInfo(name = "creation_date")
    val creationDate: String,

    @ColumnInfo(name = "phone_name")
    var phoneNum: String,

    @ColumnInfo(name = "city_name")
    var cityName: String

) : Serializable
