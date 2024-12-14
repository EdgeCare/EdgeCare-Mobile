package com.example.edgecare.models

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female");

    companion object {
        fun fromDisplayName(name: String): Gender {
            return entries.find { it.displayName == name } ?: throw IllegalArgumentException("Invalid gender: $name")
        }
    }
}
