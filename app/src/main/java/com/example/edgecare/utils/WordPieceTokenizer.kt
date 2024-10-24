package com.example.edgecare.utils


class WordPieceTokenizer(
    private val vocab: Map<String, Int>,
    val unkToken: String = "[UNK]",
    private val maxInputCharsPerWord: Int = 100,
    private val lowercase: Boolean = true,
    private val subWordPrefix: String = "##"
) {
    fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()

        // Normalize text
        val normalizedText = if (lowercase) text.lowercase() else text

        // Split text into words (basic tokenizer)
        val words = normalizedText.trim().split("\\s+".toRegex())

        for (word in words) {
            if (word.length > maxInputCharsPerWord) {
                tokens.add(unkToken)
                continue
            }

            var isBad = false
            var start = 0
            val subTokens = mutableListOf<String>()

            while (start < word.length) {
                var end = word.length
                var curSubStr: String? = null

                while (start < end) {
                    var substr = word.substring(start, end)
                    if (start > 0) {
                        substr = subWordPrefix + substr
                    }
                    if (vocab.containsKey(substr)) {
                        curSubStr = substr
                        break
                    }
                    end -= 1
                }

                if (curSubStr == null) {
                    isBad = true
                    break
                }

                subTokens.add(curSubStr)
                start = end
            }

            if (isBad) {
                tokens.add(unkToken)
            } else {
                tokens.addAll(subTokens)
            }
        }

        return tokens
    }
}