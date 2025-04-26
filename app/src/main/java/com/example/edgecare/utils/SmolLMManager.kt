package com.example.edgecare.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.measureTime


class SmolLMManager(
    //private val messagesDB: com.osh.sml.data.MessagesDB,
) {
    private val instance = com.example.smollm.SmolLM()
    private var responseGenerationJob: Job? = null
    private var modelInitJob: Job? = null
    //private var chat: com.osh.sml.data.Chat? = null
    private var isInstanceLoaded = false
    var isInferenceOn = false

    data class SmolLMInitParams(
        //val chat: com.osh.sml.data.Chat,
        val modelPath: String,
        val minP: Float,
        val temperature: Float,
        val storeChats: Boolean,
        val contextSize: Long,
        val chatTemplate: String,
        val nThreads: Int,
        val useMmap: Boolean,
        val useMlock: Boolean,
    )

    data class SmolLMResponse(
        val response: String,
        val generationSpeed: Float,
        val generationTimeSecs: Int,
        val contextLengthUsed: Int,
    )

    fun create(
        initParams: SmolLMInitParams,
        onError: (Exception) -> Unit,
        onSuccess: () -> Unit,
    ) {
        try {
            modelInitJob =
                CoroutineScope(Dispatchers.Default).launch {
                    //chat = initParams.chat
                    if (isInstanceLoaded) {
                        close()
                    }
                    instance.create(
                        initParams.modelPath,
                        initParams.minP,
                        initParams.temperature,
                        initParams.storeChats,
                        initParams.contextSize,
                        initParams.chatTemplate,
                        initParams.nThreads,
                        initParams.useMmap,
                        initParams.useMlock,
                    )



                    withContext(Dispatchers.Main) {
                        isInstanceLoaded = true
                        onSuccess()
                    }
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun getResponse(
        query: String,
        responseTransform: (String) -> String,
        onPartialResponseGenerated: (String) -> Unit,
        onSuccess: (SmolLMResponse) -> Unit,
        onCancelled: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        try {
            //assert(initParams.chat != null) { "Please call SmolLMManager.create() first." }
            responseGenerationJob =
                CoroutineScope(Dispatchers.Default).launch {
                    isInferenceOn = true
                    var response = ""
                    val duration =
                        measureTime {
                            instance.getResponse(query).collect { piece ->
                                response += responseTransform(piece)
                                withContext(Dispatchers.Main) {
                                    onPartialResponseGenerated(response)
                                }
                            }
                        }
                    // once the response is generated
                    withContext(Dispatchers.Main) {
                        isInferenceOn = false
                        onSuccess(
                            SmolLMResponse(
                                response = response,
                                generationSpeed = instance.getResponseGenerationSpeed(),
                                generationTimeSecs = duration.inWholeSeconds.toInt(),
                                contextLengthUsed = instance.getContextLengthUsed(),
                            ),
                        )
                    }
                }
        } catch (e: CancellationException) {
            isInferenceOn = false
            onCancelled()
        } catch (e: Exception) {
            isInferenceOn = false
            onError(e)
        }
    }

    fun stopResponseGeneration() {
        responseGenerationJob?.let { cancelJobIfActive(it) }
    }

    fun close() {
        stopResponseGeneration()
        modelInitJob?.let { cancelJobIfActive(it) }
        instance.close()
        isInstanceLoaded = false
    }

    private fun cancelJobIfActive(job: Job) {
        if (job.isActive) {
            job.cancel()
        }
    }
}