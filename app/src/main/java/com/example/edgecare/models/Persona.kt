package com.example.edgecare.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Persona(
    @Id var id: Long = 0,
    var name: String,
    var age: Int,
    var gender: String,
    var weight: Double,
    var height: Double,
//    var medicalHistory: String?,
//    var currentMedications: String?,
//    var allergies: String?,
//    var dietaryPreferences: String?,
//    var activityLevel: String?,
//    var healthGoals: String?,
    var smoking: Boolean,
    var alcoholConsumption: Boolean,
    // Optional fields can be nullable
//    var familyMedicalHistory: String?,
//    var sleepHabits: String?,
//    var stressLevel: String?,
//    var specificSymptoms: String?
)