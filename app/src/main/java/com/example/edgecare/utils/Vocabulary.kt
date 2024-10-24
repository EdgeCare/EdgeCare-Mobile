package com.example.edgecare.utils
import android.content.Context

class Vocabulary(context: Context) {
    val tokenToId: Map<String, Int>
    val idToToken: Map<Int, String>

    init {
        // Read the vocab.txt file from assets
        val vocabInputStream = context.assets.open("vocab.txt")
        val vocabContent = vocabInputStream.bufferedReader().use { it.readText() }
        val vocabList = vocabContent.split("\n")

        // Create token-to-ID and ID-to-token mappings
        tokenToId = vocabList.withIndex().associate { (index, token) -> token to index }
        idToToken = tokenToId.entries.associate { (token, id) -> id to token }
    }
}
