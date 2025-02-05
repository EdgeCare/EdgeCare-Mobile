package com.example.edgecare.utils

import com.example.edgecare.ObjectBox
import com.example.edgecare.models.AuthToken
import io.objectbox.Box

class AuthUtils {
    private val authTokenBox: Box<AuthToken> = ObjectBox.store.boxFor(AuthToken::class.java)

    // Save or Update Token
    fun saveToken(userId:Long,accessToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000) // Convert to milliseconds
        val token = AuthToken( accessToken = accessToken, expiresAt = expiresAt, userId = userId)
        authTokenBox.put(token) // Store or update token
    }

    // Retrieve Token
    fun getToken(): AuthToken? {
        return authTokenBox.query().build().findFirst()
    }

    // Check if Token is Expired
    fun isTokenValid(): Boolean {
        val token = getToken() ?: return false
        if(token.expiresAt > System.currentTimeMillis()){
            return true
        }
        else{
            deleteToken()
            return false
        }
    }

    // Delete Token
    private fun deleteToken() {
        authTokenBox.removeAll()
    }
}
