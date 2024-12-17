package com.example.edgecare.utils

import android.content.Context
import com.example.edgecare.models.NormalizerConfig
import com.example.edgecare.models.PostProcessorConfig
import com.example.edgecare.models.PreTokenizerConfig
import com.example.edgecare.models.Tokenizer
import com.google.gson.Gson

object TokenizerUtils {
    fun loadTokenizer(context: Context): Tokenizer {
        context.assets.open("tokenizer.json").use { inputStream ->
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            return Gson().fromJson(jsonString, Tokenizer::class.java)
        }
    }

    fun tokenize(text: String, tokenizer: Tokenizer): List<Int> {
        println("Embedding Model | Tokenizing..")
        // Normalize text
        val normalizedText = normalizeText(text, tokenizer.normalizer)

        // Pre-tokenization logic (based on the type specified in your tokenizer settings)
        val preTokenizedText = preTokenizeText(normalizedText, tokenizer.preTokenizer)

        // Convert words to tokens using the vocabulary
        val tokens = preTokenizedText.flatMap { word ->
            wordToTokens(word, tokenizer.model.vocab, tokenizer.model.continuingSubWordPrefix, tokenizer)
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

    private fun wordToTokens(word: String, vocab: Map<String, Int>, subWordPrefix: String, tokenizer: Tokenizer): List<Int> {
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
}