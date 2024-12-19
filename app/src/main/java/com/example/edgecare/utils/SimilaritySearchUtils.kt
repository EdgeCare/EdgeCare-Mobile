package com.example.edgecare.utils

import com.example.edgecare.models.HealthReport
import io.objectbox.Box
import io.objectbox.kotlin.query

object SimilaritySearchUtils {

    /**
     * Calculate cosine similarity between two FloatArrays.
     *
     * @param vector1 The first vector.
     * @param vector2 The second vector.
     * @return A similarity score between -1 and 1.
     */
    fun cosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        if (vector1.size != vector2.size) throw IllegalArgumentException("Vectors must be the same size")

        val dotProduct = vector1.zip(vector2).map { (a, b) -> a * b }.sum()
        val magnitude1 = kotlin.math.sqrt(vector1.map { it * it }.sum())
        val magnitude2 = kotlin.math.sqrt(vector2.map { it * it }.sum())

        return if (magnitude1 > 0 && magnitude2 > 0) dotProduct / (magnitude1 * magnitude2) else 0f
    }

    /**
     * Load stored embeddings from the ObjectBox database.
     *
     * @param healthReportBox The ObjectBox box for HealthReport entities.
     * @return A list of pairs of health report IDs and their embeddings.
     */
    fun loadStoredEmbeddings(healthReportBox: Box<HealthReport>): List<Pair<Long, FloatArray>> {
        return healthReportBox.query { }.find()
            .filter { it.embedding != null } // Only include reports with non-null embeddings
            .map { it.id to it.embedding!! }
    }

    /**
     * Find the most similar reports based on embeddings.
     *
     * @param queryEmbedding The embedding of the query health report.
     * @param storedEmbeddings List of pairs of health report IDs and their embeddings.
     * @param topK The number of top results to return.
     * @return A list of IDs of the most similar reports.
     */
    fun findMostSimilarReports(
        queryEmbedding: FloatArray,
        healthReportBox: Box<HealthReport>,
        topK: Int = 5
    ): List<Pair<Long, Float>> {
        // Calculate similarity for each stored embedding
        val storedEmbeddings = loadStoredEmbeddings(healthReportBox)
        val similarityScores = storedEmbeddings.map { (id, storedEmbedding) ->
            val similarity = cosineSimilarity(queryEmbedding, storedEmbedding)
            id to similarity
        }

        // Sort by similarity and return top-k results
        val topSimilarReports = similarityScores.sortedByDescending { it.second }
            .take(topK)

        // Print the results
        topSimilarReports.forEach { (id, similarity) ->
            println("HealthReport ID: $id, Similarity: $similarity")
        }

        return topSimilarReports
    }
}
