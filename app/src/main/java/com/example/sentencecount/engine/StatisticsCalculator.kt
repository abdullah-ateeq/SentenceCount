package com.example.sentencecount.engine

import com.example.sentencecount.model.DisplaySettings
import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.LengthUnit
import com.example.sentencecount.model.Sentence
import com.example.sentencecount.model.SentenceLengthDistribution
import com.example.sentencecount.model.SentenceStatistics
import kotlin.math.pow
import kotlin.math.sqrt

object StatisticsCalculator {

    fun getSentenceLength(sentence: Sentence, lengthSettings: LengthSettings): Int {
        return when (lengthSettings.lengthUnit) {
            LengthUnit.WORDS -> sentence.wordCount
            LengthUnit.CHARACTERS -> sentence.characterCount
            LengthUnit.BOTH -> sentence.wordCount
        }
    }

    fun calculateStatistics(
        sentences: List<Sentence>,
        lengthSettings: LengthSettings = LengthSettings()
    ): SentenceStatistics {
        if (sentences.isEmpty()) {
            return SentenceStatistics(
                totalSentences = 0,
                totalWords = 0,
                totalCharacters = 0,
                shortestLength = null,
                longestLength = null,
                averageLength = null,
                medianLength = null,
                modeLengths = emptyList(),
                range = null,
                averageCharactersPerSentence = null,
                standardDeviation = null
            )
        }

        val totalSentences = sentences.size
        val totalWords = sentences.sumOf { it.wordCount }
        val totalCharacters = sentences.sumOf { it.characterCount }

        val lengths = sentences.map { getSentenceLength(it, lengthSettings) }.sorted()

        val shortest = lengths.firstOrNull()
        val longest = lengths.lastOrNull()
        val range = if (shortest != null && longest != null) longest - shortest else null

        // Average length (Words / sentence by default)
        val averageLength = if (lengthSettings.lengthUnit == LengthUnit.CHARACTERS) {
            totalCharacters.toDouble() / totalSentences
        } else {
            totalWords.toDouble() / totalSentences
        }

        // Median calculation
        val medianLength: Double = if (lengths.size % 2 == 1) {
            lengths[lengths.size / 2].toDouble()
        } else {
            val mid1 = lengths[lengths.size / 2 - 1]
            val mid2 = lengths[lengths.size / 2]
            (mid1 + mid2).toDouble() / 2.0
        }

        // Mode calculation
        val frequencyMap = lengths.groupingBy { it }.eachCount()
        val maxFrequency = frequencyMap.values.maxOrNull() ?: 0
        val modeLengths = frequencyMap.filter { it.value == maxFrequency }.keys.sorted()

        // Average characters per sentence
        val avgCharsPerSentence = totalCharacters.toDouble() / totalSentences

        // Population Standard Deviation: sqrt( sum( (x - mean)^2 ) / N )
        val variance = lengths.sumOf { (it - averageLength).pow(2.0) } / totalSentences
        val standardDeviation = sqrt(variance)

        return SentenceStatistics(
            totalSentences = totalSentences,
            totalWords = totalWords,
            totalCharacters = totalCharacters,
            shortestLength = shortest,
            longestLength = longest,
            averageLength = averageLength,
            medianLength = medianLength,
            modeLengths = modeLengths,
            range = range,
            averageCharactersPerSentence = avgCharsPerSentence,
            standardDeviation = standardDeviation
        )
    }

    fun calculateDistribution(
        sentences: List<Sentence>,
        lengthSettings: LengthSettings = LengthSettings()
    ): List<SentenceLengthDistribution> {
        if (sentences.isEmpty()) return emptyList()

        val total = sentences.size.toDouble()
        val lengths = sentences.map { getSentenceLength(it, lengthSettings) }
        val counts = lengths.groupingBy { it }.eachCount()

        return counts.keys.sorted().map { len ->
            val count = counts[len] ?: 0
            val percentage = (count / total) * 100.0
            SentenceLengthDistribution(
                length = len,
                sentenceCount = count,
                percentage = percentage
            )
        }
    }

    data class SentenceGroups(
        val shortCount: Int,
        val shortPercentage: Double,
        val mediumCount: Int,
        val mediumPercentage: Double,
        val longCount: Int,
        val longPercentage: Double
    )

    fun calculateGroups(
        sentences: List<Sentence>,
        lengthSettings: LengthSettings,
        displaySettings: DisplaySettings
    ): SentenceGroups {
        if (sentences.isEmpty()) {
            return SentenceGroups(0, 0.0, 0, 0.0, 0, 0.0)
        }

        val total = sentences.size.toDouble()
        var short = 0
        var medium = 0
        var long = 0

        for (s in sentences) {
            val len = getSentenceLength(s, lengthSettings)
            when {
                len <= displaySettings.shortThreshold -> short++
                len <= displaySettings.mediumThreshold -> medium++
                else -> long++
            }
        }

        return SentenceGroups(
            shortCount = short,
            shortPercentage = (short / total) * 100.0,
            mediumCount = medium,
            mediumPercentage = (medium / total) * 100.0,
            longCount = long,
            longPercentage = (long / total) * 100.0
        )
    }
}
