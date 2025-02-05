package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class AuthToken(
    @Id var id: Long = 0,
    var userId:Long,
    var accessToken: String,
    var expiresAt: Long // Expiry time in milliseconds
)
