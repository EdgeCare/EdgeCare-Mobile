package com.example.edgecare.models

data class Tokenizer(
    val version: String,
    val truncation: TruncationConfig,
    val padding: PaddingConfig,
    val addedTokens: List<AddedToken>,
    val normalizer: NormalizerConfig,
    val preTokenizer: PreTokenizerConfig,
    val postProcessor: PostProcessorConfig,
    val decoder: DecoderConfig,
    val model: ModelConfig
)

data class TruncationConfig(
    val maxLength: Int,
    val strategy: String,
    val stride: Int
)

data class PaddingConfig(
    val strategy: PaddingStrategy,
    val direction: String,
    val padToMultipleOf: Int?,
    val padId: Int,
    val padTypeId: Int,
    val padToken: String
)

data class PaddingStrategy(
    val fixed: Int
)

data class AddedToken(
    val id: Int,
    val special: Boolean,
    val content: String,
    val singleWord: Boolean,
    val lstrip: Boolean,
    val rstrip: Boolean,
    val normalized: Boolean
)

data class NormalizerConfig(
    val type: String,
    val cleanText: Boolean,
    val handleChineseChars: Boolean,
    val stripAccents: Boolean?,
    val lowercase: Boolean
)

data class PreTokenizerConfig(
    val type: String
)

data class PostProcessorConfig(
    val type: String,
    val single: List<SpecialTokenConfig>,
    val pair: List<SpecialTokenConfig>,
    val specialTokens: Map<String, SpecialTokenDetail>
)

data class SpecialTokenConfig(
    val specialToken: SpecialTokenDetail?,
    val sequence: SequenceConfig?
)

data class SpecialTokenDetail(
    val id: String,
    val ids: List<Int>,
    val tokens: List<String>
)

data class SequenceConfig(
    val id: String,
    val typeId: Int
)

data class DecoderConfig(
    val type: String,
    val prefix: String,
    val cleanup: Boolean
)

data class ModelConfig(
    val type: String,
    val unkToken: String,
    val continuingSubWordPrefix: String,
    val maxInputCharsPerWord: Int,
    val vocab: Map<String, Int>
)


