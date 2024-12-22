package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
data class Chat(
    @Id var id: Long = 0,
    var chatName: String = "New Chat",
){
    lateinit var chatMessageList: ToMany<ChatMessage>
}