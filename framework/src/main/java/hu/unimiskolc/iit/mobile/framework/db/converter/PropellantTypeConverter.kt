package hu.unimiskolc.iit.mobile.framework.db.converter

import androidx.room.TypeConverter
import hu.unimiskolc.iit.mobile.core.domain.Propellant

class PropellantTypeConverter {
    @TypeConverter
    fun toPropellant(value: Int) : Propellant = Propellant.values().first { it.ordinal == value }

    @TypeConverter
    fun toIntValue(propellant: Propellant) : Int = propellant.ordinal
}