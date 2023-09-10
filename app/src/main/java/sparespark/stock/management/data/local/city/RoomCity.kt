package sparespark.stock.management.data.local.city

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(
    tableName = "city_table",
    indices = [Index("creation_date")]
)
data class RoomCity(

    @ColumnInfo(name = "name")
    var name: String,

    @PrimaryKey
    @ColumnInfo(name = "creation_date")
    val creationDate: String

) : Serializable
