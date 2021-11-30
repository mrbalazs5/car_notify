package hu.unimiskolc.iit.mobile.framework.db.datasource

import hu.unimiskolc.iit.mobile.core.data.datasource.UserDataSource
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.dao.UserDao
import hu.unimiskolc.iit.mobile.framework.db.mapper.UserMapper

class RoomUserDataSource (private val dao: UserDao, private val mapper: UserMapper): UserDataSource {
    override suspend fun add(user: User) = dao.insert(mapper.mapToEntity(user))

    override suspend fun update(user: User) = dao.update(mapper.mapToEntity(user))

    override suspend fun remove(user: User) = dao.delete(mapper.mapToEntity(user))

    override suspend fun fetchById(id: Int): User? = dao.fetchById(id)?.let { mapper.mapFromEntity(it) }

    override suspend fun fetchByEmail(email: String): User? = dao.fetchByEmail(email)?.let { mapper.mapFromEntity(it) }

    override suspend fun fetchAll(): List<User> = dao.fetchAll().map { mapper.mapFromEntity(it) }
}