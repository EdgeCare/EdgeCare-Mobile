package com.example.edgecare.utils

import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object AnonymizationUtils {
    fun calculateAge(birthDateString: String): Int? {
        return try {
            // Define the date format
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Parse the input date string
            val birthDate = LocalDate.parse(birthDateString, formatter)

            // Get the current date
            val currentDate = LocalDate.now()

            // Calculate the age
            Period.between(birthDate, currentDate).years
        } catch (e: Exception) {
            null // Return null if the date format is incorrect
        }
    }

    fun calculateAgeFromYear(birthDateString: String): Int? {
        val possibleFormats = listOf(
            "yyyy-MM-dd", "dd/MM/yyyy", "MM-dd-yyyy", "dd-MM-yyyy",
            "yyyy/MM/dd", "MMMM d, yyyy", "d MMM yyyy", "yyyy"
        )

        for (format in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(format)
                val birthYear = when (format) {
                    "yyyy" -> Year.parse(birthDateString).value // Directly extract year
                    else -> LocalDate.parse(birthDateString, formatter).year // Extract year from date
                }

                val currentYear = LocalDate.now().year
                return currentYear - birthYear
            } catch (e: DateTimeParseException) {
                // Ignore and try the next format
            }
        }
        return null // Return null if no valid format is found
    }

    fun anonymizeAge(age: Int): String {
        return when {
            age < 3 -> "1-2"
            age in 3..5 -> "3-5"
            age in 6..8 -> "6-8"
            age in 9..12 -> "9-12"
            age in 13..19 -> "13-19"
            age in 20..29 -> "20-30"
            age in 30..39 -> "30-40"
            age in 40..49 -> "40-50"
            age in 50..59 -> "50-60"
            age in 60..69 -> "60-70"
            age in 70..79 -> "70-80"
            else -> "80+"
        }
    }
}