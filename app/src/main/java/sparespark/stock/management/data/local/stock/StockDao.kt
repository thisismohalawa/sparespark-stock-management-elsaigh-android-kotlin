package sparespark.stock.management.data.local.stock

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: RoomStock): Long

    @Query("SELECT * FROM stock_table where temp_item=:isTemp ORDER BY creation_date DESC")
    suspend fun getStockList(isTemp: Boolean): List<RoomStock>

    @Query("SELECT * FROM stock_table where temp_item=:isTemp ORDER BY creation_date DESC")
    suspend fun getTempStockList(isTemp: Boolean): List<RoomStock>

    @Query("DELETE FROM stock_table where temp_item=:isTemp")
    suspend fun clearData(isTemp: Boolean)

    @Query("DELETE FROM stock_table WHERE creation_date = :payId")
    suspend fun deleteTempPay(payId: String)

}
