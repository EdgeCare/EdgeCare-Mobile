package com.example.edgecare.utils

//object EmbeddingUtils {
//    fun computeEmbedding(text: String): FloatArray {
//        // Replace this with your actual embedding computation logic
//        val embeddingSize = 128
//        return FloatArray(embeddingSize) { Math.random().toFloat() }
//    }
//
//    fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
//        var dotProduct = 0f
//        var normA = 0f
//        var normB = 0f
//        for (i in vecA.indices) {
//            dotProduct += vecA[i] * vecB[i]
//            normA += vecA[i] * vecA[i]
//            normB += vecB[i] * vecB[i]
//        }
//        return dotProduct / ((Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat())
//    }
//}

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.nio.LongBuffer

object EmbeddingUtils {
    private lateinit var ortEnvironment: OrtEnvironment
    private var ortSession: OrtSession? = null

    // Initialize the model
    fun initializeModel(context: Context): Boolean {
        return try {
            print("Initialize the model")
            ortEnvironment = OrtEnvironment.getEnvironment()
            ortSession = loadModel(context, ortEnvironment)
            ortSession != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Load the ONNX model from assets
    private fun loadModel(context: Context, env: OrtEnvironment): OrtSession? {
        return try {
            println("loading model...")
            val assetManager = context.assets
            val modelInputStream = assetManager.open("all-MiniLM-L6-V2.onnx")
            val byteArray = modelInputStream.readBytes()
            val opts = OrtSession.SessionOptions()
            println("model loaded, preparing input")
            env.createSession(byteArray, opts)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Compute the embedding for the given text
    fun computeEmbedding(text: String): FloatArray? {
        try {
            println("computing embeddings")
            val tokens = tokenize(text)
            if (tokens.isEmpty()) {
                return null
            }

            val inputs = prepareInput(tokens, ortEnvironment) ?: return null

            return runModel(ortSession!!, inputs)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Tokenize the input text
    private fun tokenize(text: String): List<Int> {
        // Implement tokenization logic here
        // For example, using a tokenizer that maps words to token IDs
        // Placeholder implementation:

        // You need to integrate a tokenizer compatible with your model
        // For this example, we'll simulate token IDs
        // In practice, use a real tokenizer (e.g., BertTokenizer)

        val tokens = mutableListOf<Int>()
        tokens.add(101) // [CLS] token ID

        // Split text into words (this is a simplification)
        val words = text.split(" ")
        for (word in words) {
            // Map each word to a token ID
            // Replace with actual token ID lookup
            val tokenId = word.hashCode() % 10000 // Placeholder
            tokens.add(tokenId)
        }

        tokens.add(102) // [SEP] token ID
        return listOf(101, 2023, 2003, 1037, 2742, 102)
    }

    // Prepare the input tensors for the model
    private fun prepareInput(tokens: List<Int>, env: OrtEnvironment): HashMap<String, OnnxTensor>? {
        try {
            val buffer: LongBuffer = LongBuffer.allocate(tokens.size)
            val maskBuffer: LongBuffer = LongBuffer.allocate(tokens.size)

            tokens.forEach { token ->
                buffer.put(token.toLong())
                maskBuffer.put(1L) // Assuming no padding, mask is all 1s
            }
            buffer.flip()
            maskBuffer.flip()

            val inputTensor = OnnxTensor.createTensor(env, buffer, longArrayOf(1, tokens.size.toLong()))
            val maskTensor = OnnxTensor.createTensor(env, maskBuffer, longArrayOf(1, tokens.size.toLong()))

            return hashMapOf("input_ids" to inputTensor, "attention_mask" to maskTensor)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    // Run the model and get the embedding
    private fun runModel(session: OrtSession, inputs: HashMap<String, OnnxTensor>): FloatArray {
        return try {
            val result = session.run(inputs)
            val outputTensor = result[0].value as Array<Array<FloatArray>> // Correcting to 3D array
            outputTensor[0][3] // first element  // output is a 2D array where the first dimension corresponds to batches
        } catch (e: Exception) {
//            mainVIewLoadModelText.text = "Error running model: ${e.localizedMessage}"
            e.printStackTrace()
            floatArrayOf(1.0f, 2.0f, 3.0f, 4.5f, 5.5f)
        }
    }
}

