package hu.unimiskolc.iit.mobile.core.data.datasource

import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.core.domain.User

interface CarDataSource {
    suspend fun add(car: Car)
    suspend fun update(car: Car)
    suspend fun remove(car: Car)

    suspend fun fetchById(id: Int): Car?
    suspend fun fetchByOwner(owner: User): List<Car>
    suspend fun fetchAll(): List<Car>
}