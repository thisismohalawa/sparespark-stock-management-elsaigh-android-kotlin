package sparespark.stock.management.data.local.client

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateClient(client: RoomClient): Long

    @Query("SELECT * FROM client_table")
    suspend fun getClientList(): List<RoomClient>

    @Query("SELECT city_name FROM client_table  where name = :client")
    suspend fun getCity(client: String): String?

    @Query("SELECT phone_name FROM client_table  where name = :client")
    suspend fun getPhoneNumber(client: String): String?

    @Query("DELETE FROM client_table")
    suspend fun clearData()
}
