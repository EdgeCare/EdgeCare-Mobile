package com.example.edgecare

import io.objectbox.converter.PropertyConverter

class FloatArrayConverter : PropertyConverter<FloatArray, String> {
    override fun convertToDatabaseValue(entityProperty: FloatArray?): String {
        return entityProperty?.joinToString(",") ?: ""
    }

    override fun convertToEntityProperty(databaseValue: String?): FloatArray {
        return databaseValue?.split(",")?.mapNotNull { it.toFloatOrNull() }?.toFloatArray()
            ?: FloatArray(0)
    }
}
