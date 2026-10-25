package com.example.sentencecount.model

data class SentenceLengthDistribution(
    val length: Int,
    val sentenceCount: Int,
    val percentage: Double
)
