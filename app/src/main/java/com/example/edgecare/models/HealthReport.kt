package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class HealthReport(
    @Id var id: Long = 0,
    var isPDF:Boolean=false,
    var text: String = "",
    var summary:String ="",
    var pdfData: ByteArray? = null
)
