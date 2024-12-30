package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Persona(
    @Id var id: Long = 0,
    var name: String? = null,
    var age: Int? = null,
    var birthday: String? = null,
    var gender: String? = null,
    var weight: Double? = null,
    var height: Double? = null,
//    var medicalHistory: String?,
//    var currentMedications: String?,
    var allergies: String? = null,
//    var dietaryPreferences: String?,
//    var activityLevel: String?,
//    var healthGoals: String?,
    var diabetes: Boolean? = null,
    var highBloodPressure: Boolean? = null,
    var smoking: Boolean? = null,
    var alcoholConsumption: Boolean? = null,
    // Optional fields can be nullable
//    var familyMedicalHistory: String?,
    var sleepHours: Int? = null,
//    var stressLevel: String?,
//    var specificSymptoms: String?
)