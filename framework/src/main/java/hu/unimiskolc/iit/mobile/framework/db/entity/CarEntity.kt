package hu.unimiskolc.iit.mobile.framework.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import hu.unimiskolc.iit.mobile.core.domain.Propellant
import hu.unimiskolc.iit.mobile.core.domain.User
import java.util.*

@Entity(
    tableName = "cars",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("ownerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]

)
data class CarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val type: String,
    val image: String,
    val ownerId: Int,
    val lastInspection: Date,
    val licensePlate: String,
    val cylinderCapacity: Int,
    val enginePower: Int,
    val horsepower: Int,
    val totalMass: Int,
    val ownMass: Int,
    val propellant: Propellant
)
