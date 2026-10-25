package com.example.sentencecount.model

data class Sentence(
    val index: Int,
    val text: String,
    val wordCount: Int,
    val characterCount: Int,
    val startOffset: Int,
    val endOffset: Int
)
