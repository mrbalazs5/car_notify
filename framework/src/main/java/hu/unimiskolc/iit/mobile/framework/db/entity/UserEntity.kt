package hu.unimiskolc.iit.mobile.framework.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.*

@Entity(
    tableName = "users"
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val birthDate: Date
)

data class UserInfo(
    @Embedded
    val entity: UserEntity,
    @Relation(
        entity = CarEntity::class,
        entityColumn = "ownerId",
        parentColumn = "id"
    )
    val cars: List<CarEntity>

)
