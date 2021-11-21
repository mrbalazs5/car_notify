package hu.unimiskolc.iit.mobile.core.domain

import java.util.*

data class User(val id: Int, val name: String, val email: String, val password: String, val birthDate: Date)