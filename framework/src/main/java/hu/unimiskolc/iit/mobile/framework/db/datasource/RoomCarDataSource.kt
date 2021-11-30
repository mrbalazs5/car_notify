package hu.unimiskolc.iit.mobile.framework.db.datasource

import hu.unimiskolc.iit.mobile.core.data.datasource.CarDataSource
import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.dao.CarDao
import hu.unimiskolc.iit.mobile.framework.db.mapper.CarMapper

class RoomCarDataSource(private val dao: CarDao, private val mapper: CarMapper): CarDataSource {
    override suspend fun add(car: Car) = dao.insert(mapper.mapToEntity(car))

    override suspend fun update(car: Car) = dao.update(mapper.mapToEntity(car))

    override suspend fun remove(car: Car) = dao.delete(mapper.mapToEntity(car))

    override suspend fun fetchById(id: Int): Car? = dao.fetchById(id)?.let { mapper.mapFromEntity(it) }

    override suspend fun fetchByOwner(owner: User): List<Car> = dao.fetchByOwner(owner.id).map { mapper.mapFromEntity(it) }

    override suspend fun fetchAll(): List<Car> = dao.fetchAll().map { mapper.mapFromEntity(it) }
}