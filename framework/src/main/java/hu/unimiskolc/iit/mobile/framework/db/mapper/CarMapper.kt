package hu.unimiskolc.iit.mobile.framework.db.mapper

import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.framework.db.entity.CarEntity

class CarMapper {
    fun mapToEntity(data: Car) : CarEntity =
        CarEntity(
            data.id,
            data.type,
            data.image,
            data.owner?.id ?: 0,
            data.lastInspection,
            data.licensePlate,
            data.cylinderCapacity,
            data.enginePower,
            data.horsepower,
            data.totalMass,
            data.ownMass,
            data.propellant
        )
    fun mapFromEntity(entity: CarEntity) : Car =
        Car(
            entity.id,
            entity.type,
            entity.image,
            null,
            entity.lastInspection,
            entity.licensePlate,
            entity.cylinderCapacity,
            entity.enginePower,
            entity.horsepower,
            entity.totalMass,
            entity.ownMass,
            entity.propellant
        )
}