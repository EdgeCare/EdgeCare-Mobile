package com.example.edgecare

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.edgecare.utils.Vocabulary
import com.example.edgecare.utils.WordPieceTokenizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.LongBuffer

data class InputFeatures(
    val inputIds: LongArray,
    val attentionMask: LongArray,
    val tokens: List<String>
)

class BertModelHandler(private val context: Context) {

    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null
    private var vocab: Vocabulary = Vocabulary(context)     // Load the vocabulary
    private var tokenizer:  WordPieceTokenizer = WordPieceTokenizer(vocab!!.tokenToId) // Initialize the tokenizer
    private var modelFile : String?  = null
    private val labelMap: Map<Int, String>

    init {

        // Load the label map
        val labelsInputStream = context.assets.open("labels.json")
        val labelsContent = labelsInputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(labelsContent)
            labelMap = jsonObject.keys().asSequence().map { key ->
                key.toInt() to jsonObject.getString(key)
            }.toMap()

        //Load the ONNX model
        // [ToDo] - Need to find a better way to load the model. Currently, it is copied from assets to files directory.
        modelFile = copyAssetToFile(context, "de-identifier.onnx", "de-identifier.onnx").absolutePath.toString()

        // Create the session using the model file path
        ortSession = ortEnvironment.createSession(modelFile)

    }

    //[ToDo] - Works for now, but need to find a better way to load model
    private fun copyAssetToFile(context: Context, assetFileName: String, outputFileName: String): File {
        val file = File(context.filesDir, outputFileName)
        if (!file.exists()) {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
        }
        return file
    }

    fun prepareInputs(text: String): InputFeatures {
        val tokens = mutableListOf<String>()

        // Add special token [CLS]
        tokens.add("[CLS]")

        // Tokenize the text and add to tokens list
        tokens.addAll(tokenizer.tokenize(text))

        // Add special token [SEP]
        tokens.add("[SEP]")

        // Convert tokens to input IDs
        val inputIds = tokens.map { token ->
            vocab.tokenToId[token] ?: vocab.tokenToId[tokenizer.unkToken]!!
        }.map { it.toLong() }.toLongArray()

        // Create attention mask (1 for all tokens)
        val attentionMask = LongArray(inputIds.size) { 1 }

        return InputFeatures(inputIds, attentionMask, tokens)
    }

    suspend fun runInference(features: InputFeatures): List<Pair<String, String>> = withContext(
        Dispatchers.IO) {


        // Prepare input tensors
        val inputIdsTensor = OnnxTensor.createTensor(
            ortEnvironment,
            LongBuffer.wrap(features.inputIds),
            longArrayOf(1, features.inputIds.size.toLong())
        )
        val attentionMaskTensor = OnnxTensor.createTensor(
            ortEnvironment,
            LongBuffer.wrap(features.attentionMask),
            longArrayOf(1, features.attentionMask.size.toLong())
        )

        val inputs = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to attentionMaskTensor
        )

        // Run the model
        val results = ortSession!!.run(inputs)

        // Get the logits output
        val logitsTensor = results[0].value as Array<Array<FloatArray>> // Shape: [batch_size, sequence_length, num_labels]

        // Process the logits to get predicted labels
        val predictedLabelIndices = logitsTensor[0].map { logits ->
            logits.indices.maxByOrNull { logits[it] } ?: -1
        }

        // Map tokens to labels
        val labeledTokens = features.tokens.zip(predictedLabelIndices).map { (token, labelIdx) ->
            val label = labelMap[labelIdx] ?: "O"
            token to label
        }

        println("labeledTokens")
        println(labeledTokens)

        // Clean up resources
        inputIdsTensor.close()
        attentionMaskTensor.close()
        results.close()

        labeledTokens
    }
}