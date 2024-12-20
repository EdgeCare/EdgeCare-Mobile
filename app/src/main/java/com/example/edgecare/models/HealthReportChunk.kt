package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id

@Entity
data class HealthReportChunk(
    @Id var id: Long = 0,
    var reportId : Long ,
    var text: String = "",
    @HnswIndex(dimensions = 384) var embedding: FloatArray? = null,
)