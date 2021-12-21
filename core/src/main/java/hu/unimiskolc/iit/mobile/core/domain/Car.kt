package hu.unimiskolc.iit.mobile.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Car(
    val id: Int,
    var type: String,
    var image: String,
    var owner: User?,
    var lastInspection: Date,
    var licensePlate: String,
    var cylinderCapacity: Int,
    var enginePower: Int,
    var horsepower: Int,
    var totalMass: Int,
    var ownMass: Int,
    var propellant: Propellant
) : Parcelable