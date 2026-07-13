package com.frei.app.domain.repository

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String
    )

    suspend fun register(
        email: String,
        password: String
    )

    fun logout()
}