package com.example.sentencecount.model

enum class LengthUnit(val label: String) {
    WORDS("Words"),
    CHARACTERS("Characters"),
    BOTH("Both")
}

enum class CharacterUnit(val label: String) {
    GRAPHEME("Grapheme Characters"),
    CODE_POINT("Unicode Code Points")
}

enum class HyphenMode(val label: String) {
    KEEP("Keep as one word"),
    SPLIT("Split into words")
}

enum class ApostropheMode(val label: String) {
    KEEP("Keep as one word"),
    SPLIT("Split into words")
}

enum class AppTheme(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class SentenceDetectionSettings(
    val enablePeriod: Boolean = true,
    val enableQuestion: Boolean = true,
    val enableExclamation: Boolean = true,
    val enableUnicodeEquivalents: Boolean = true,
    val paragraphBoundaryMode: Boolean = false,
    val countPunctuationOnly: Boolean = false,
    val customAbbreviations: List<String> = emptyList(),
    val customTerminators: List<String> = emptyList()
)

data class LengthSettings(
    val lengthUnit: LengthUnit = LengthUnit.WORDS,
    val characterUnit: CharacterUnit = CharacterUnit.GRAPHEME,
    val includeSpaces: Boolean = false,
    val includePunctuation: Boolean = false,
    val countNumbers: Boolean = true,
    val hyphenMode: HyphenMode = HyphenMode.KEEP,
    val apostropheMode: ApostropheMode = ApostropheMode.KEEP,
    val urlEmailAsSingleToken: Boolean = true
)

data class DisplaySettings(
    val showMedian: Boolean = true,
    val showMode: Boolean = true,
    val showDistribution: Boolean = true,
    val showChart: Boolean = true,
    val showVariability: Boolean = true,
    val showShortMediumLong: Boolean = true,
    val shortThreshold: Int = 7,
    val mediumThreshold: Int = 14
)

data class BehaviorSettings(
    val liveAnalysis: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastText: Boolean = true
)

data class ThemeSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true
)
