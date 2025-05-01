package com.example.edgecare.models


import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SmallModelinfo(
    @Id var id: Long = 0,
    var name: String = "",
    var url: String = "",
    var path: String = "",
    var contextSize: Int = 0,
    var chatTemplate: String = "",
)