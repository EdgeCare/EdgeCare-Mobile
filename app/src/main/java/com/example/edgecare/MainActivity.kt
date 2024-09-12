package com.example.edgecare

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.nio.LongBuffer

class MainActivity : AppCompatActivity() {
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var embeddingsTextView: TextView
    private lateinit var mainVIewLoadModelText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ortEnvironment = OrtEnvironment.getEnvironment()
        embeddingsTextView = findViewById(R.id.mainViewText)
        mainVIewLoadModelText = findViewById(R.id.mainVIewLoadModelText)

        embeddingsTextView.text = "Loading model..."
        // Load model and handle its result within the onCreate method
        val session = loadModel(this, ortEnvironment)
        if (session == null) {
            embeddingsTextView.text = "Failed to load model"
            return
        }

        embeddingsTextView.text = "Model loaded, preparing input..."
        val tokens = listOf(101, 2023, 2003, 1037, 2742, 102) // Example token IDs for "[CLS/START] This is a test [END]"
        val inputTensor = prepareInput(tokens, ortEnvironment)
        if (inputTensor == null) {
            embeddingsTextView.text = "Failed to prepare input"
            return
        }

        embeddingsTextView.text = "Input prepared, running model..."
        val embeddings = runModel(session, inputTensor)
        if (embeddings != null) {
            val embeddingsString = "Embeddings: ${embeddings.joinToString(", ")}"
            runOnUiThread {
                embeddingsTextView.text = embeddingsString
            }
        } else {
            runOnUiThread {
                embeddingsTextView.text = "Failed to run model"
            }
        }
    }

    private fun loadModel(context: Context, env: OrtEnvironment): OrtSession? {
        return try {
            val assetManager = context.assets
            val modelInputStream = assetManager.open("all-MiniLM-L6-V2.onnx")
            val byteArray = modelInputStream.readBytes()
            val opts = OrtSession.SessionOptions()
            env.createSession(byteArray, opts)
        } catch (e: Exception) {
            mainVIewLoadModelText.text = "Model load error"
            e.printStackTrace()
            null
        }
    }

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

    private fun runModel(session: OrtSession, inputs: HashMap<String, OnnxTensor>): FloatArray? {
        return try {
            val result = session.run(inputs)
            val outputTensor = result[0].value as Array<Array<FloatArray>> // Correcting to 3D array
            outputTensor[0][3] // first element  // output is a 2D array where the first dimension corresponds to batches
        } catch (e: Exception) {
//            mainVIewLoadModelText.text = "Error running model: ${e.localizedMessage}"
            e.printStackTrace()
            null
        }
    }

}
