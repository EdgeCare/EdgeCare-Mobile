package com.example.edgecare.utils

import android.content.ContentResolver
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object FileUtils {
    @Throws(Exception::class)
    fun readTextFile(contentResolver: ContentResolver, fileUri: Uri): String {
        val inputStream = contentResolver.openInputStream(fileUri)
            ?: throw Exception("Unable to open input stream")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append("\n")
        }
        reader.close()
        return stringBuilder.toString()
    }
}
