package com.example.edgecare.utils

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.edgecare.models.NormalizerConfig
import com.example.edgecare.models.PostProcessorConfig
import com.example.edgecare.models.PreTokenizerConfig
import com.example.edgecare.models.Tokenizer
import com.google.gson.Gson
import java.nio.LongBuffer

object EmbeddingUtils {
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var tokenizer: Tokenizer
    private var ortSession: OrtSession? = null

    // Initialize the model
    fun initializeModel(context: Context): Boolean {
        return try {
            println("Embedding Model | Initialize the model")
            ortEnvironment = OrtEnvironment.getEnvironment()
            ortSession = loadModel(context, ortEnvironment)
            tokenizer = loadTokenizer(context)
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
    fun computeEmbedding(text: String): FloatArray? {
        try {
            println("Embedding Model | Computing embeddings")
            val tokens = tokenize(text, tokenizer)
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

    private fun loadTokenizer(context: Context): Tokenizer {
        context.assets.open("tokenizer.json").use { inputStream ->
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            return Gson().fromJson(jsonString, Tokenizer::class.java)
        }
    }

    private fun tokenize(text: String, tokenizer: Tokenizer): List<Int> {
        println("Embedding Model | Tokenizing..")
        // Normalize text
        val normalizedText = normalizeText(text, tokenizer.normalizer)

        // Pre-tokenization logic (based on the type specified in your tokenizer settings)
        val preTokenizedText = preTokenizeText(normalizedText, tokenizer.preTokenizer)

        // Convert words to tokens using the vocabulary
        val tokens = preTokenizedText.flatMap { word ->
            wordToTokens(word, tokenizer.model.vocab, tokenizer.model.continuingSubWordPrefix)
        }
        // Implement post-processing if required
        val processedTokens = postProcessTokens(tokens, tokenizer.postProcessor)
        return processedTokens
    }

    private fun normalizeText(text: String, config: NormalizerConfig): String {
        var result = text
//        if (config.cleanText) {
//            // Remove unwanted characters, etc.
//        }
//        if (config.handleChineseChars) {
//            // Implement logic for Chinese characters
//        }
//        if (config.stripAccents == true) {
//            // Implement logic to strip accents
//        }
        if (config.lowercase) {
            result = result.toLowerCase()   // [ToDo] - deprecated
        }
        return result
    }

    private fun preTokenizeText(text: String, config: PreTokenizerConfig): List<String> {
        // Split text based on spaces or other criteria based on the preTokenizer type
        return text.split(" ","\n")  // Customize this part based on your actual requirements
    }

    private fun wordToTokens(word: String, vocab: Map<String, Int>, subWordPrefix: String): List<Int> {
        val tokens = mutableListOf<Int>()

        // Check if the word is directly in the vocab
        vocab[word]?.let {
            tokens.add(it)
        } ?: run {
            var remaining = word
            loop@ while (remaining.isNotEmpty()) {
                var matchFound = false

                // Iterate from the longest subWord to the shortest
                for (i in remaining.length downTo 1) {
                    val subWord = if (i == remaining.length) remaining else subWordPrefix + remaining.substring(0, i)
                    vocab[subWord]?.let {
                        tokens.add(it)
                        remaining = remaining.substring(i)
                        matchFound = true
                    }
                }

                // If no subWord are found, add the unknown token if available, or break the loop
                if (!matchFound) {
                    vocab[tokenizer.model.unkToken]?.let {
                        tokens.add(it)
                    }
                    break
                }
            }
        }
        return tokens
    }

    private fun postProcessTokens(tokens: List<Int>, config: PostProcessorConfig): List<Int> {
        // Add special tokens, handle pairs of sequences, etc.
        return tokens
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
            val outputTensor = result[0].value as Array<Array<FloatArray>> // Correcting to 3D array
            outputTensor[0][3] // first element  // output is a 2D array where the first dimension corresponds to batches
        } catch (e: Exception) {
            e.printStackTrace()
            floatArrayOf(1.0f, 2.0f, 3.0f, 4.5f, 5.5f)
        }
    }
}

