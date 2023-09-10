package sparespark.stock.management.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sparespark.stock.management.data.local.city.CityDao
import sparespark.stock.management.data.local.city.RoomCity
import sparespark.stock.management.data.local.client.ClientDao
import sparespark.stock.management.data.local.client.RoomClient
import sparespark.stock.management.data.local.stock.RoomStock
import sparespark.stock.management.data.local.stock.StockDao
import sparespark.stock.management.data.local.user.RoomUser
import sparespark.stock.management.data.local.user.UserDao

private const val DATABASE = "stock_db"

@Database(
    entities = [
        RoomUser::class,
        RoomCity::class,
        RoomClient::class,
        RoomStock::class
    ], version = 1, exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cityDao(): CityDao
    abstract fun clientDao(): ClientDao
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var instance: StockDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                StockDatabase::class.java, DATABASE
            ).build()
    }
}
