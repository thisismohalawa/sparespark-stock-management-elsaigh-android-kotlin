package sparespark.stock.management.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: RoomUser): Long

    @Query("select * from user_table where uid = :userId")
    suspend fun getSignedUser(userId: String): RoomUser

    @Query("DELETE FROM user_table where uid = :userId")
    suspend fun clearSignedUser(userId: String)

    @Query("select activated from user_table where uid = :userId")
    suspend fun isSignedUserActive(userId: String): Boolean?

    @Query("select roleId from user_table where uid = :userId")
    suspend fun getSignedRoleId(userId: String): Int?

    @Query("SELECT * FROM user_table")
    suspend fun getTeamList(): List<RoomUser>

    @Query("DELETE FROM user_table where uid!=:userId")
    suspend fun clearTeamList(userId: String)

    @Query("select name from user_table where uid = :userId")
    suspend fun getUserNameById(userId: String): String?
}
