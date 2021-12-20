package hu.unimiskolc.iit.mobile.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class User(
    val id: Int,
    var name: String,
    var email: String,
    var password: String,
    var birthDate: Date,
    val cars: List<Car>
) : Parcelable