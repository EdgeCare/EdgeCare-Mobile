package com.example.edgecare.utils

import android.content.Context
import com.example.edgecare.ObjectBox
import com.example.edgecare.models.HealthReportChunk
import com.example.edgecare.models.HealthReportChunk_
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
    private fun cosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        //Todo
        if (vector1.size != vector2.size) return 0f

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
    private fun loadStoredEmbeddings(healthReportChunksBox: Box<HealthReportChunk>): List<Pair<Long, FloatArray>> {
        return healthReportChunksBox.query { }.find()
            .filter { it.embedding != null } // Only include reports with non-null embeddings
            .map { it.id to it.embedding!! }
    }

    /**
     * Find the most similar reports based on embeddings.
     *
     * @param queryEmbedding The embedding of the query health report.
     * @param topK The number of top results to return.
     * @return A list of IDs of the most similar reports.
     */
    fun findMostSimilarReports(
        queryEmbedding: FloatArray,
        healthReportChunkBox: Box<HealthReportChunk>,
        topK: Int = 5
    ): List<Pair<Long, Float>> {
        // Calculate similarity for each stored embedding
        val storedEmbeddings = loadStoredEmbeddings(healthReportChunkBox)
        val similarityScores = storedEmbeddings.map { (id, storedEmbedding) ->
            val similarity = cosineSimilarity(queryEmbedding, storedEmbedding)
            id to similarity
        }

        // Sort by similarity and return top-k results
        val topSimilarReports = similarityScores.sortedByDescending { it.second }
            .take(topK)

        // Print the results
        topSimilarReports.forEach { (id, similarity) ->
            println("Chunk ID: $id, Similarity: $similarity")
        }

        return topSimilarReports
    }

    // returns the top health reports as a string list.
    fun getTopSimilarHealthReports(text: String, context: Context):List<String>{
        //Similarity Search for health reports
        val embeddings = EmbeddingUtils.computeEmbedding(text,context)
        val nonNullableEmbeddings: FloatArray = embeddings ?: FloatArray(0) // Default to empty array
        val healthReportChunk = ObjectBox.store.boxFor(HealthReportChunk::class.java)
        val similarReports = findMostSimilarReports(nonNullableEmbeddings, healthReportChunk,2 )
        val output =mutableListOf<String>()
        similarReports.forEach { (id, similarity) ->
            val chunk = healthReportChunk.get(id)
            output.add(chunk.text)
        }

        if(output.isEmpty()){
            output.add("No similar health reports")
        }
        return output
    }

    // Used Object box nearestNeighbors search. Does not work when the health reports are dynamically updates.
    fun findSimilarReports(text:String, context: Context ){
        val healthReportChunkBox = ObjectBox.store.boxFor(HealthReportChunk::class.java)

        val embeddings = EmbeddingUtils.computeEmbedding(text, context)
        val nonNullableEmbeddings: FloatArray = embeddings ?: FloatArray(0)
        val query = healthReportChunkBox
            .query(HealthReportChunk_.embedding.nearestNeighbors(nonNullableEmbeddings, 2))
            .build()
        val results = query.findIdsWithScores()
        query.close()
        for (result in results) {
            println("Report ID: ${result.id}, distance: ${result.score}")
        }
    }
}
