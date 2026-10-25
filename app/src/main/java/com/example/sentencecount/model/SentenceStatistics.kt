package com.example.sentencecount.model

data class SentenceStatistics(
    val totalSentences: Int,
    val totalWords: Int,
    val totalCharacters: Int,
    val shortestLength: Int?,
    val longestLength: Int?,
    val averageLength: Double?,
    val medianLength: Double?,
    val modeLengths: List<Int>,
    val range: Int?,
    val averageCharactersPerSentence: Double?,
    val standardDeviation: Double?
)
