package hu.unimiskolc.iit.mobile.core.data.repository

import hu.unimiskolc.iit.mobile.core.data.datasource.CarDataSource
import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.core.domain.User

class CarRepository(private val dataSource: CarDataSource) {
    suspend fun add(car: Car) = dataSource.add(car)
    suspend fun update(car: Car) = dataSource.update(car)
    suspend fun remove(car: Car) = dataSource.remove(car)

    suspend fun fetchById(id: Int): Car? = dataSource.fetchById(id)
    suspend fun fetchByOwner(owner: User): List<Car> = dataSource.fetchByOwner(owner)
    suspend fun fetchAll(): List<Car> = dataSource.fetchAll()
}