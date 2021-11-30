package hu.unimiskolc.iit.mobile.framework.db.mapper

import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.entity.UserEntity
import hu.unimiskolc.iit.mobile.framework.db.entity.UserInfo

class UserMapper {
    private val carMapper: CarMapper = CarMapper()

    fun mapToEntity(data: User) : UserEntity =
        UserEntity(
            data.id,
            data.name,
            data.email,
            data.password,
            data.birthDate
        )

    fun mapFromEntity(info: UserInfo) : User =
        User(
            info.entity.id,
            info.entity.name,
            info.entity.email,
            info.entity.password,
            info.entity.birthDate,
            info.cars.map { carMapper.mapFromEntity(it) }
        )
}