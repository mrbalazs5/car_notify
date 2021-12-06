package hu.unimiskolc.iit.mobile.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Car(
    val id: Int,
    val type: String,
    val image: String,
    val owner: User?,
    val lastInspection: Date,
    val licensePlate: String,
    val cylinderCapacity: Int,
    val enginePower: Int,
    val horsepower: Int,
    val totalMass: Int,
    val ownMass: Int,
    val propellant: Propellant
) : Parcelable