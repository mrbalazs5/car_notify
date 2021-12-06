package hu.unimiskolc.iit.mobile.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val birthDate: Date,
    val cars: List<Car>
) : Parcelable