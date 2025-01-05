package com.example.edgecare.models

data class QueryRequest(
    val input_value: String,
    val input_type: String = "chat",
    val output_type: String = "chat",
    val tweaks: Map<String, Any> = emptyMap()
)

data class QueryResponse(
    val outputs: List<FlowOutput>?
)

data class FlowOutput(
    val outputs: List<ComponentOutput>
)

data class ComponentOutput(
    val outputs: OutputMessage
)

data class OutputMessage(
    val message: MessageContent
)

data class MessageContent(
    val text: String
)
