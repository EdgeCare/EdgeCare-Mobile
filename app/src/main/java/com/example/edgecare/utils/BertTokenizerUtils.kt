package com.example.edgecare.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Kotlin implementation of the BertTokenizer with WordPiece tokenization.
 */
object BertTokenizerUtils {

    data class TokenizerConfig(
        val vocab: Map<String, Int>,
        val unkToken: String = "[UNK]",
        val sepToken: String = "[SEP]",
        val padToken: String = "[PAD]",
        val clsToken: String = "[CLS]",
        val maskToken: String = "[MASK]",
        val continuingSubWordPrefix: String = "##",
        val maxInputCharsPerWord: Int = 100
    )

    fun loadVocab(context: Context, assetFileName: String): Map<String, Int> {
        val vocab = mutableMapOf<String, Int>()
        context.assets.open(assetFileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                lines.forEachIndexed { index, token ->
                    vocab[token.trim()] = index
                }
            }
        }
        return vocab
    }

    fun whitespaceTokenize(text: String): List<String> {
        return text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    }

    fun convertTokensToIds(tokens: List<String>,vocab: Map<String, Int>): List<Int> {
        return tokens.map { token -> vocab[token] ?: vocab.getValue("[UNK]") }
    }



    /**
     * Basic Tokenizer: Handles basic text preprocessing like lowercasing and splitting on punctuation.
     */
    class BasicTokenizer(
        private val doLowerCase: Boolean = true,
        private val tokenizeChineseChars: Boolean = true
    ) {
        fun tokenize(text: String): List<String> {
            var normalizedText = text
            if (doLowerCase) {
                normalizedText = normalizedText.lowercase()
            }
            if (tokenizeChineseChars) {
                normalizedText = tokenizeChineseChars(normalizedText)
            }
            val tokens = whitespaceTokenize(normalizedText)
            return tokens.flatMap { splitOnPunctuation(it) }
        }

        private fun tokenizeChineseChars(text: String): String {
            val output = StringBuilder()
            for (char in text) {
                if (isChineseChar(char.code)) {
                    output.append(" ").append(char).append(" ")
                } else {
                    output.append(char)
                }
            }
            return output.toString()
        }

        private fun isChineseChar(codePoint: Int): Boolean {
            return (codePoint in 0x4E00..0x9FFF ||
                    codePoint in 0x3400..0x4DBF ||
                    codePoint in 0x20000..0x2A6DF ||
                    codePoint in 0x2A700..0x2B73F ||
                    codePoint in 0x2B740..0x2B81F ||
                    codePoint in 0x2B820..0x2CEAF ||
                    codePoint in 0xF900..0xFAFF ||
                    codePoint in 0x2F800..0x2FA1F)
        }

        private fun splitOnPunctuation(text: String): List<String> {
            if (text.isEmpty()) return listOf(text)
            val tokens = mutableListOf<String>()
            val currentToken = StringBuilder()
            for (char in text) {
                if (char.isLetterOrDigit()) {
                    currentToken.append(char)
                } else {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken.clear()
                    }
                    tokens.add(char.toString())
                }
            }
            if (currentToken.isNotEmpty()) {
                tokens.add(currentToken.toString())
            }
            return tokens
        }
    }

    /**
     * WordPiece Tokenizer: Splits words into subwords using a greedy longest-match-first algorithm.
     */
    class WordPieceTokenizer(
        private val vocab: Map<String, Int>,
        private val unkToken: String,
        private val continuingSubWordPrefix: String = "##",
        private val maxInputCharsPerWord: Int = 100
    ) {
        fun tokenize(text: String): List<String> {
            val outputTokens = mutableListOf<String>()
            for (token in whitespaceTokenize(text)) {
                if (token.length > maxInputCharsPerWord) {
                    outputTokens.add(unkToken)
                    continue
                }

                var start = 0
                val subTokens = mutableListOf<String>()
                while (start < token.length) {
                    var end = token.length
                    var curSubstr: String? = null
                    while (start < end) {
                        var substr = token.substring(start, end)
                        if (start > 0) {
                            substr = continuingSubWordPrefix + substr
                        }
                        if (vocab.containsKey(substr)) {
                            curSubstr = substr
                            break
                        }
                        end--
                    }
                    if (curSubstr == null) {
                        subTokens.clear()
                        subTokens.add(unkToken)
                        break
                    }
                    subTokens.add(curSubstr)
                    start = end
                }
                outputTokens.addAll(subTokens)
            }
            return outputTokens
        }
    }

    /**
     * Full Bert Tokenizer combining BasicTokenizer and WordPieceTokenizer.
     */
    class BertFullTokenizer(
        vocab: Map<String, Int>,
        doLowerCase: Boolean = true
    ) {
        private val basicTokenizer = BasicTokenizer(doLowerCase = doLowerCase)
        private val wordPieceTokenizer = WordPieceTokenizer(vocab, unkToken = "[UNK]")

        fun tokenize(text: String): List<String> {
            val basicTokens = basicTokenizer.tokenize(text)
            return basicTokens.flatMap { wordPieceTokenizer.tokenize(it) }
        }
    }

    fun generateTokens(context: Context, sentence: String): List<Int> {
        // Load vocabulary from the assets folder
        val vocab = loadVocab(context, "vocab.txt")

        // Initialize the tokenizer
        val tokenizer = BertFullTokenizer(vocab)


        // Tokenize the given sentence
        val tokenList =  tokenizer.tokenize(sentence)

        return convertTokensToIds(tokenList,vocab)
    }
}