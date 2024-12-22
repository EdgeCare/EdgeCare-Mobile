package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class ChatMessage2(
    @Id var id: Long = 0,
    var chatId: Long = 0, // Foreign key reference to the Chat
    var message: String = "",
    var timestamp: Date = Date(),
    var isSentByUser: Boolean = true
)