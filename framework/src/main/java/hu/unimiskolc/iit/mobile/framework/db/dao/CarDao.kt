package hu.unimiskolc.iit.mobile.framework.db.dao

import androidx.room.*
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.entity.CarEntity

@Dao
interface CarDao {
    @Insert
    suspend fun insert(entity: CarEntity)

    @Update
    suspend fun update(entity: CarEntity)

    @Delete
    suspend fun delete(entity: CarEntity)

    @Query("SELECT * FROM cars WHERE id = :id")
    suspend fun fetchById(id: Int): CarEntity?

    @Query("SELECT * FROM cars WHERE ownerId = :ownerId")
    suspend fun fetchByOwner(ownerId: Int): List<CarEntity>

    @Query("SELECT * from cars")
    suspend fun fetchAll() : List<CarEntity>
}