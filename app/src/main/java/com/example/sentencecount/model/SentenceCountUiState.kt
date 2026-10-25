package com.example.sentencecount.model

data class SentenceCountUiState(
    val text: String = "",
    val sentences: List<Sentence> = emptyList(),
    val totalSentences: Int = 0,
    val totalWords: Int = 0,
    val totalCharacters: Int = 0,
    val shortestLength: Int? = null,
    val longestLength: Int? = null,
    val averageLength: Double? = null,
    val medianLength: Double? = null,
    val modeLengths: List<Int> = emptyList(),
    val range: Int? = null,
    val averageCharactersPerSentence: Double? = null,
    val standardDeviation: Double? = null,
    val distribution: List<SentenceLengthDistribution> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    // Extra helper state for rich display
    val sortOrder: SentenceSortOrder = SentenceSortOrder.ORIGINAL,
    val isAnalyzing: Boolean = false,
    val totalLines: Int = 0,
    val shortestSentences: List<Sentence> = emptyList(),
    val longestSentences: List<Sentence> = emptyList(),
    val shortCount: Int = 0,
    val shortPercentage: Double = 0.0,
    val mediumCount: Int = 0,
    val mediumPercentage: Double = 0.0,
    val longCount: Int = 0,
    val longPercentage: Double = 0.0
)
