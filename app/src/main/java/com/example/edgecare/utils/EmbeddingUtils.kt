package com.example.edgecare.utils

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
            println("Embedding Model | Initialize the model")
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
            println("Embedding Model | Loading model...")
            val modelInputStream = context.assets.open("all-MiniLM-L6-V2.onnx")
            val byteArray = modelInputStream.readBytes()
            val opts = OrtSession.SessionOptions()
            env.createSession(byteArray, opts)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Compute the embedding for the given text
    fun computeEmbedding(text: String, context: Context): FloatArray? {
        try {
            println("Embedding Model | Computing embeddings")

            val tokenFromBertTokenizer = BertTokenizerUtils.generateTokenIdList(context, text)
            println("Generated Tokens: $tokenFromBertTokenizer")

            if (tokenFromBertTokenizer.isEmpty()) {
                return null
            }
            val inputs = prepareInput(tokenFromBertTokenizer, ortEnvironment) ?: return null

            return runModel(ortSession!!, inputs)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
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
    // [ToDo] - return full output embedding array. Currently returning only the first element for testing purposes
    private fun runModel(session: OrtSession, inputs: HashMap<String, OnnxTensor>): FloatArray {
        return try {
            val result = session.run(inputs)
            val outputTensor = result[0].value as Array<Array<FloatArray>> // Correcting to 3D array //results array of size [1][number of words][384]
            val outputTensor2 = result[1].value as Array<FloatArray> // Correcting to 2D array // results array of size [1][384]
            outputTensor2[0] // Pooled embedding
        } catch (e: Exception) {
            e.printStackTrace()
            floatArrayOf()
        }
    }
}

