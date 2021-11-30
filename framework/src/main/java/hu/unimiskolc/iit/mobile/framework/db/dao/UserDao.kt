package hu.unimiskolc.iit.mobile.framework.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hu.unimiskolc.iit.mobile.framework.db.entity.UserEntity
import hu.unimiskolc.iit.mobile.framework.db.entity.UserInfo

interface UserDao {
    @Insert
    suspend fun insert(entity: UserEntity)

    @Update
    suspend fun update(entity: UserEntity)

    @Delete
    suspend fun delete(entity: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun fetchById(id: Int): UserInfo?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun fetchByEmail(email: String): UserInfo?

    @Query("SELECT * from users")
    suspend fun fetchAll() : List<UserInfo>
}