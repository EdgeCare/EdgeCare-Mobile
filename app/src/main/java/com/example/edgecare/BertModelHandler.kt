package com.example.edgecare

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.edgecare.utils.BertTokenizerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.LongBuffer

data class InputFeatures(
    val inputIds: LongArray,
    val attentionMask: LongArray,
    val tokenTypeIds: LongArray,
    val tokens: List<String>
)

class BertModelHandler(private val context: Context) {

    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null
    private var modelFile: String?  = null
    private val labelMap: Map<Int, String>

    init {
        // Load the label map
        val labelsInputStream = context.assets.open("edgeCare-de-id-labels.json")
        val labelsContent = labelsInputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(labelsContent)
            labelMap = jsonObject.keys().asSequence().map { key ->
                key.toInt() to jsonObject.getString(key)
            }.toMap()

        //Load the ONNX model
        // [ToDo] - Need to find a better way to load the model. Currently, it is copied from assets to files directory.
//        modelFile = copyAssetToFile(context, "edgeCare-de-identifier.onnx", "edgeCare-de-identifier.onnx").absolutePath.toString()
        modelFile = copyAssetToFile(context, "edgeCare-de-identifier.onnx", "edgeCare-de-identifier.onnx").absolutePath

        // Create the session using the model file path
        ortSession = ortEnvironment.createSession(modelFile)
    }

    // [ToDo] - Works for now, but need to find a better way to load the model
    private fun copyAssetToFile(context: Context, assetFileName: String, outputFileName: String): File {
        val file = File(context.filesDir, outputFileName)
        if (!file.exists()) {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return file
    }

    fun prepareInputs(text: String): InputFeatures {
        // Generate tokens and input IDs
        val (tokenList, tokenIdList) = BertTokenizerUtils.generateTokenListForDeIdentifier(context, text)

        // Create attention mask (1 for each valid token)
        val attentionMask = LongArray(tokenIdList.size) { 1 }

        // For single-sequence tasks, token_type_ids are typically all zeros - [ToDo]- check this
        val tokenTypeIds = LongArray(tokenIdList.size) { 0 }

        return InputFeatures(
            inputIds = tokenIdList,
            attentionMask = attentionMask,
            tokenTypeIds = tokenTypeIds,
            tokens = tokenList
        )
    }

    suspend fun runInference(features: InputFeatures): List<Pair<String, String>> = withContext(Dispatchers.IO) {

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

        // [Note] - we don't need token_type_ids for stanferd model
        val tokenTypeIdsTensor = OnnxTensor.createTensor(
            ortEnvironment,
            LongBuffer.wrap(features.tokenTypeIds),
            longArrayOf(1, features.tokenTypeIds.size.toLong())
        )

        val inputs = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to attentionMaskTensor,
            "token_type_ids" to tokenTypeIdsTensor
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

        // Clean up resources
        inputIdsTensor.close()
        attentionMaskTensor.close()
        tokenTypeIdsTensor.close()
        results.close()

        labeledTokens
    }
}
