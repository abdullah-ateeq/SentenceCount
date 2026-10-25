package com.example.sentencecount

import com.example.sentencecount.engine.CharacterCounter
import com.example.sentencecount.engine.SentenceDetector
import com.example.sentencecount.engine.StatisticsCalculator
import com.example.sentencecount.engine.WordCounter
import com.example.sentencecount.model.HyphenMode
import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.Sentence
import com.example.sentencecount.model.SentenceDetectionSettings
import com.example.sentencecount.model.SentenceSortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SentenceDetectorAndStatisticsTest {

    private lateinit var detector: SentenceDetector

    @Before
    fun setUp() {
        detector = SentenceDetector()
    }

    @Test
    fun testBasic() {
        val input = "Hello world. How are you? I am fine!"
        val result = detector.detectSentences(input)
        assertEquals(3, result.size)
    }

    @Test
    fun testPeriod() {
        val input = "This is one. This is two."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testQuestion() {
        val input = "How are you? Are you okay?"
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testExclamation() {
        val input = "Amazing! Fantastic!"
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testMultiplePunctuation() {
        val input = "Really?! What!!! Why?!"
        val result = detector.detectSentences(input)
        assertEquals(3, result.size)
    }

    @Test
    fun testDecimal() {
        val input = "The price is 12.50 dollars."
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
    }

    @Test
    fun testVersion() {
        val input = "Install version 2.5.1 now."
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
    }

    @Test
    fun testAbbreviation() {
        val input = "Dr. Smith arrived early. He waited outside."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testInitials() {
        val input = "J. K. Rowling wrote a book."
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
    }

    @Test
    fun testInitialsConsecutive() {
        val input = "J.R.R. Tolkien wrote the book."
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
    }

    @Test
    fun testUrl() {
        val input = "Visit https://example.com/about. It has more information."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testEmail() {
        val input = "Email support@example.com for help."
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
    }

    @Test
    fun testEllipsis() {
        val input = "Wait... Are you serious?"
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testQuotes() {
        val input = "He said, \"Hello.\" Then he left."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
        assertEquals("He said, \"Hello.\"", result[0].text)
        assertEquals("Then he left.", result[1].text)
    }

    @Test
    fun testQuestionInQuotes() {
        val input = "He asked, \"Are you coming?\" Then he waited."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testParentheses() {
        val input = "He arrived (very late). Then he left."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testNewlineDefault() {
        val input = "Hello\nWorld"
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
    }

    @Test
    fun testParagraphBreakMode() {
        val input = "Hello\n\nWorld"
        val settings = SentenceDetectionSettings(paragraphBoundaryMode = true)
        val result = detector.detectSentences(input, settings)
        assertEquals(2, result.size)
    }

    @Test
    fun testEmptySegments() {
        val input = "Hello... ... World."
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testTrailingWhitespace() {
        val input = "Hello.    "
        val result = detector.detectSentences(input)
        assertEquals(1, result.size)
        assertEquals("Hello.", result[0].text)
    }

    @Test
    fun testUnicodeFrench() {
        val input = "Bonjour ! Comment allez-vous ?"
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testCJK() {
        val input = "你好世界。今天好吗？"
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testArabic() {
        val input = "مرحبا بالعالم. كيف حالك؟"
        val result = detector.detectSentences(input)
        assertEquals(2, result.size)
    }

    @Test
    fun testAverage() {
        val sentences = listOf(
            Sentence(1, "s1", 5, 20, 0, 10),
            Sentence(2, "s2", 7, 30, 11, 20),
            Sentence(3, "s3", 9, 40, 21, 30)
        )
        val stats = StatisticsCalculator.calculateStatistics(sentences)
        assertEquals(7.0, stats.averageLength!!, 0.001)
    }

    @Test
    fun testMedianOdd() {
        val lengths = listOf(3, 5, 7, 9, 11)
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val stats = StatisticsCalculator.calculateStatistics(sentences)
        assertEquals(7.0, stats.medianLength!!, 0.001)
    }

    @Test
    fun testMedianEven() {
        val lengths = listOf(3, 5, 7, 9)
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val stats = StatisticsCalculator.calculateStatistics(sentences)
        assertEquals(6.0, stats.medianLength!!, 0.001)
    }

    @Test
    fun testMode() {
        val lengths = listOf(4, 5, 5, 6, 5, 7)
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val stats = StatisticsCalculator.calculateStatistics(sentences)
        assertEquals(listOf(5), stats.modeLengths)
    }

    @Test
    fun testMultipleModes() {
        val lengths = listOf(4, 4, 5, 5, 6)
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val stats = StatisticsCalculator.calculateStatistics(sentences)
        assertEquals(listOf(4, 5), stats.modeLengths)
    }

    @Test
    fun testRange() {
        val lengths = listOf(2, 5, 9)
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val stats = StatisticsCalculator.calculateStatistics(sentences)
        assertEquals(7, stats.range)
    }

    @Test
    fun testDistribution() {
        val lengths = listOf(3, 3, 4, 5, 5, 5)
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val dist = StatisticsCalculator.calculateDistribution(sentences)
        assertEquals(3, dist.size)
        assertEquals(3, dist[0].length)
        assertEquals(2, dist[0].sentenceCount)
        assertEquals(4, dist[1].length)
        assertEquals(1, dist[1].sentenceCount)
        assertEquals(5, dist[2].length)
        assertEquals(3, dist[2].sentenceCount)
    }

    @Test
    fun testPercentage() {
        val lengths = List(3) { 5 } + List(7) { 10 }
        val sentences = lengths.mapIndexed { idx, len ->
            Sentence(idx + 1, "s$idx", len, len * 5, 0, 10)
        }
        val dist = StatisticsCalculator.calculateDistribution(sentences)
        val dist5 = dist.first { it.length == 5 }
        assertEquals(30.00, dist5.percentage, 0.01)
    }

    @Test
    fun testPunctuationExclusion() {
        val input = "Hello!"
        val countExcluded = CharacterCounter.countCharacters(
            input,
            LengthSettings(includePunctuation = false, includeSpaces = false)
        )
        val countIncluded = CharacterCounter.countCharacters(
            input,
            LengthSettings(includePunctuation = true, includeSpaces = false)
        )
        assertEquals(5, countExcluded)
        assertEquals(6, countIncluded)
    }

    @Test
    fun testSpaces() {
        val input = "Hello world."
        val countExcluded = CharacterCounter.countCharacters(
            input,
            LengthSettings(includeSpaces = false, includePunctuation = false)
        )
        val countIncluded = CharacterCounter.countCharacters(
            input,
            LengthSettings(includeSpaces = true, includePunctuation = false)
        )
        assertEquals(10, countExcluded)
        assertEquals(11, countIncluded)
    }

    @Test
    fun testApostrophe() {
        val input = "I don't know."
        val count = WordCounter.countWords(input, LengthSettings())
        assertEquals(3, count)
    }

    @Test
    fun testHyphen() {
        val input = "This is well-known."
        val keepCount = WordCounter.countWords(input, LengthSettings(hyphenMode = HyphenMode.KEEP))
        val splitCount = WordCounter.countWords(input, LengthSettings(hyphenMode = HyphenMode.SPLIT))
        assertEquals(3, keepCount)
        assertEquals(4, splitCount)
    }

    @Test
    fun testNumbers() {
        val input = "There are 3 apples."
        val numbersOn = WordCounter.countWords(input, LengthSettings(countNumbers = true))
        val numbersOff = WordCounter.countWords(input, LengthSettings(countNumbers = false))
        assertEquals(4, numbersOn)
        assertEquals(3, numbersOff)
    }

    @Test
    fun testSearch() {
        val sentences = listOf(
            Sentence(1, "The quick brown fox jumps.", 5, 25, 0, 26),
            Sentence(2, "It runs across the field.", 5, 25, 27, 52),
            Sentence(3, "Another fox appeared.", 3, 20, 53, 74)
        )
        val filtered = sentences.filter { it.text.contains("fox", ignoreCase = true) }
        assertEquals(2, filtered.size)
        assertEquals("The quick brown fox jumps.", filtered[0].text)
        assertEquals("Another fox appeared.", filtered[1].text)
    }

    @Test
    fun testSorting() {
        val sentences = listOf(
            Sentence(1, "Banana.", 1, 7, 0, 7),
            Sentence(2, "Apple is great.", 3, 15, 8, 23),
            Sentence(3, "Cherry.", 1, 7, 24, 31)
        )

        val shortest = sentences.sortedBy { it.wordCount }
        assertEquals(1, shortest[0].wordCount)

        val longest = sentences.sortedByDescending { it.wordCount }
        assertEquals(3, longest[0].wordCount)

        val alpha = sentences.sortedBy { it.text }
        assertEquals("Apple is great.", alpha[0].text)

        val revAlpha = sentences.sortedByDescending { it.text }
        assertEquals("Cherry.", revAlpha[0].text)
    }

    @Test
    fun testLargeText() {
        val baseSentence = "The quick brown fox jumps over the lazy dog. "
        val repeatCount = 3000 // ~135,000 characters
        val largeText = StringBuilder()
        for (i in 0 until repeatCount) {
            largeText.append(baseSentence)
        }

        assertTrue(largeText.length > 100000)
        val result = detector.detectSentences(largeText.toString())
        assertEquals(repeatCount, result.size)
    }
}
