package com.example.sentencecount.model

enum class SentenceSortOrder(val displayName: String) {
    ORIGINAL("Original Order"),
    SHORTEST_FIRST("Shortest → Longest"),
    LONGEST_FIRST("Longest → Shortest"),
    ALPHABETICAL("Alphabetical (A → Z)"),
    REVERSE_ALPHABETICAL("Reverse Alphabetical (Z → A)")
}
