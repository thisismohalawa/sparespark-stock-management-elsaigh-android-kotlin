package sparespark.stock.management.data.local.city

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCity(city: RoomCity): Long

    @Query("SELECT * FROM city_table")
    suspend fun getCityList(): List<RoomCity>

    @Query("DELETE FROM city_table")
    suspend fun clearData()
}
