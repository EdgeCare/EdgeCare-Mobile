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
            print("Initialize the model")
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

//    private fun tokenize(text: String, tokenizer: Tokenizer): List<Int> {
//        return text.split(" ")  // Basic whitespace tokenizer, adjust as needed for your data
//            .map { word ->
//                tokenizer.vocab[word] ?: tokenizer.vocab[tokenizer.unkToken] ?: throw RuntimeException("Unknown token not found in tokenizer vocab")
//            }
//    }
//
//
//    // Tokenize the input text
//    private fun tokenize(text: String): List<Int> {
//        // Implement tokenization logic here
//        // For example, using a tokenizer that maps words to token IDs
//        // Placeholder implementation:
//
//        // You need to integrate a tokenizer compatible with your model
//        // For this example, we'll simulate token IDs
//        // In practice, use a real tokenizer (e.g., BertTokenizer)
//
//        val tokens = mutableListOf<Int>()
//        tokens.add(101) // [CLS] token ID
//
//        // Split text into words (this is a simplification)
//        val words = text.split(" ")
//        for (word in words) {
//            // Map each word to a token ID
//            // Replace with actual token ID lookup
//            val tokenId = word.hashCode() % 10000 // Placeholder
//            tokens.add(tokenId)
//        }
//
//        tokens.add(102) // [SEP] token ID
//        return listOf(101, 2023, 2003, 1037, 2742, 102)
//    }


    private fun tokenize(text: String, tokenizer: Tokenizer): List<Int> {
        println("tokernizing..")
        // Normalize text
        val normalizedText = normalizeText(text, tokenizer.normalizer)
        println(normalizedText)

        // Pre-tokenization logic (based on the type specified in your tokenizer settings)
        val preTokenizedText = preTokenizeText(normalizedText, tokenizer.preTokenizer)
        println(preTokenizedText)

        // Convert words to tokens using the vocabulary
        val tokens = preTokenizedText.flatMap { word ->
            wordToTokens(word, tokenizer.model.vocab, tokenizer.model.continuingSubwordPrefix)
        }
        println(tokens)
        // Implement post-processing if required
        val processedTokens = postProcessTokens(tokens, tokenizer.postProcessor)
        println(processedTokens)
        return processedTokens
    }

    private fun normalizeText(text: String, config: NormalizerConfig): String {
        var result = text
        if (config.cleanText) {
            // Remove unwanted characters, etc.
        }
        if (config.handleChineseChars) {
            // Implement logic for Chinese characters
        }
        if (config.stripAccents == true) {
            // Implement logic to strip accents
        }
        if (config.lowercase) {
            result = result.toLowerCase()
        }
        return result
    }

    private fun preTokenizeText(text: String, config: PreTokenizerConfig): List<String> {
        // Split text based on spaces or other criteria based on the preTokenizer type
        return text.split(" ","\n")  // Customize this part based on your actual requirements
    }

    private fun wordToTokens(word: String, vocab: Map<String, Int>, subwordPrefix: String): List<Int> {
        val tokens = mutableListOf<Int>()

        // Check if the word is directly in the vocab
        vocab[word]?.let {
            tokens.add(it)
        } ?: run {
            var remaining = word
            loop@ while (remaining.isNotEmpty()) {
                var matchFound = false

                // Iterate from the longest subword to the shortest
                for (i in remaining.length downTo 1) {
                    val subword = if (i == remaining.length) remaining else subwordPrefix + remaining.substring(0, i)
                    vocab[subword]?.let {
                        tokens.add(it)
                        remaining = remaining.substring(i)
                        matchFound = true
                    }
                }

                // If no subwords are found, add the unknown token if available, or break the loop
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

