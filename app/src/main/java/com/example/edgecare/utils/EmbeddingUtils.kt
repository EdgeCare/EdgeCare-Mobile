package com.example.edgecare.utils

object EmbeddingUtils {
    fun computeEmbedding(text: String): FloatArray {
        // Replace this with your actual embedding computation logic
        val embeddingSize = 128
        return FloatArray(embeddingSize) { Math.random().toFloat() }
    }

    fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in vecA.indices) {
            dotProduct += vecA[i] * vecB[i]
            normA += vecA[i] * vecA[i]
            normB += vecB[i] * vecB[i]
        }
        return dotProduct / ((Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat())
    }
}
