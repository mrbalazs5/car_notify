package hu.unimiskolc.iit.mobile.core.data.datasource

import hu.unimiskolc.iit.mobile.core.domain.User

interface UserDataSource {
    suspend fun add(user: User)
    suspend fun update(user: User)
    suspend fun remove(user: User)

    suspend fun fetchById(id: Int): User?
    suspend fun fetchByEmail(email: String): User?
    suspend fun fetchAll(): List<User>
}