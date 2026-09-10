package com.example.common.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Interface defining authentication token management and session storage.
 * Easily injectable and platform-agnostic for Compose Multiplatform.
 */
interface AuthRepository {
    val accessToken: String?
    val refreshToken: String?
    val idToken: String?
    val isLoggedIn: Boolean

    fun saveTokens(accessToken: String, refreshToken: String? = null, idToken: String? = null)
    fun clearSession()
    fun getBearerHeader(): String?
}

/**
 * Thread-safe In-Memory implementation of AuthRepository.
 * Keeps tokens accessible across the app for REST and GraphQL queries.
 */
class InMemoryAuthRepository : AuthRepository {

    override var accessToken: String? by mutableStateOf(null)
        private set

    override var refreshToken: String? by mutableStateOf(null)
        private set

    override var idToken: String? by mutableStateOf(null)
        private set

    override val isLoggedIn: Boolean
        get() = !accessToken.isNullOrBlank()

    override fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) {
        this.accessToken = accessToken
        if (refreshToken != null) this.refreshToken = refreshToken
        if (idToken != null) this.idToken = idToken
    }

    override fun clearSession() {
        this.accessToken = null
        this.refreshToken = null
        this.idToken = null
    }

    override fun getBearerHeader(): String? {
        val token = accessToken
        return if (!token.isNullOrBlank()) "Bearer $token" else null
    }

    companion object {
        val shared: InMemoryAuthRepository by lazy { InMemoryAuthRepository() }
    }
}
