package com.example.sentencecount.model

data class SentenceCountHistory(
    val id: String,
    val text: String,
    val analyzedAt: Long,
    val lengthUnit: String,
    val includeSpaces: Boolean,
    val includePunctuation: Boolean,
    val countNumbers: Boolean,
    val hyphenMode: String,
    val paragraphBoundaryMode: Boolean,
    val countPunctuationOnly: Boolean,
    val customAbbreviations: List<String>,
    val customTerminators: List<String>
)
