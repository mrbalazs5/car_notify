package hu.unimiskolc.iit.mobile.core.data.repository

import hu.unimiskolc.iit.mobile.core.data.datasource.UserDataSource
import hu.unimiskolc.iit.mobile.core.domain.User

class UserRepository(private val dataSource: UserDataSource) {
    suspend fun add(user: User) = dataSource.add(user)
    suspend fun update(user: User) = dataSource.update(user)
    suspend fun remove(user: User) = dataSource.remove(user)

    suspend fun fetchById(id: Int): User? = dataSource.fetchById(id)
    suspend fun fetchByEmail(email: String): User? = dataSource.fetchByEmail(email)
    suspend fun fetchAll(): List<User> = dataSource.fetchAll()
}