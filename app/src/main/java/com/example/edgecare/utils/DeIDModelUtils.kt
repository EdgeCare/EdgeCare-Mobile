package com.example.edgecare.utils

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.edgecare.models.DeIdModelInputFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.LongBuffer

object DeIDModelUtils {
    private lateinit var ortEnvironmentDeId: OrtEnvironment
    private var ortSessionDeId: OrtSession? = null
    private lateinit var labelMap: Map<Int, String>

    // Initialize the model
    fun initializeModel(context: Context): Boolean {
        return try {

            // Load the label map
            labelMap = loadLabelMap(context)

            // load De-Id model
            println("DE-ID Model | Initialize the model")
            ortEnvironmentDeId = OrtEnvironment.getEnvironment()
            ortSessionDeId = loadDeIdModel(context, ortEnvironmentDeId)
            ortSessionDeId != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadLabelMap(context: Context): Map<Int, String>{
        val labelsInputStream = context.assets.open("edgeCare-de-id-labels.json")
        val labelsContent = labelsInputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(labelsContent)

        return jsonObject.keys().asSequence().map { key ->
            key.toInt() to jsonObject.getString(key)
        }.toMap()
    }

    // Load the ONNX model from assets
    private fun loadDeIdModel(context: Context, env: OrtEnvironment): OrtSession? {
        return try {
            println("De-ID Model | Loading model...")
            val modelInputStream = context.assets.open("edgeCare-de-identifier.onnx")
            val byteArray = modelInputStream.readBytes()
            val opts = OrtSession.SessionOptions()
            env.createSession(byteArray, opts)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Prepare the input tensors for the model
    fun prepareInputs(context: Context, text: String): DeIdModelInputFeatures {
        // Generate tokens and input IDs
        val (tokenList, tokenIdList) = BertTokenizerUtils.generateTokenListForDeIdentifier(context, text)

        // Create attention mask (1 for each valid token)
        val attentionMask = LongArray(tokenIdList.size) { 1 }

        // For single-sequence tasks, token_type_ids are typically all zeros - [ToDo]- check this
        val tokenTypeIds = LongArray(tokenIdList.size) { 0 }

        return DeIdModelInputFeatures(
            inputIds = tokenIdList,
            attentionMask = attentionMask,
            tokenTypeIds = tokenTypeIds,
            tokens = tokenList
        )
    }

    suspend fun runInference(features: DeIdModelInputFeatures): List<Pair<String, String>> = withContext(Dispatchers.IO) {

        // Prepare input tensors
        val inputIdsTensor = OnnxTensor.createTensor(
            ortEnvironmentDeId,
            LongBuffer.wrap(features.inputIds),
            longArrayOf(1, features.inputIds.size.toLong())
        )
        val attentionMaskTensor = OnnxTensor.createTensor(
            ortEnvironmentDeId,
            LongBuffer.wrap(features.attentionMask),
            longArrayOf(1, features.attentionMask.size.toLong())
        )

        val tokenTypeIdsTensor = OnnxTensor.createTensor(
            ortEnvironmentDeId,
            LongBuffer.wrap(features.tokenTypeIds),
            longArrayOf(1, features.tokenTypeIds.size.toLong())
        )

        val inputs = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to attentionMaskTensor,
            "token_type_ids" to tokenTypeIdsTensor
        )

        // Run the model
        val results = ortSessionDeId!!.run(inputs)

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
