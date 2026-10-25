package com.example.sentencecount.engine

import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.Sentence
import com.example.sentencecount.model.SentenceDetectionSettings
import java.util.Locale

class SentenceDetector {

    companion object {
        // Common standard abbreviations
        val DEFAULT_ABBREVIATIONS = setOf(
            "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "st", "vs", "etc",
            "eg", "e.g", "ie", "i.e", "us", "u.s", "uk", "u.k", "approx", "app",
            "apt", "dept", "est", "fig", "gen", "gov", "inc", "ltd", "corp",
            "co", "mt", "no", "oz", "rev", "sgt", "vol", "v", "al", "jan",
            "feb", "mar", "apr", "jun", "jul", "aug", "sep", "sept", "oct",
            "nov", "dec", "col", "capt", "lt", "maj", "gen", "sen", "rep"
        )

        // CJK terminators that do not require trailing whitespace
        val CJK_TERMINATORS = setOf('。', '！', '？', '﹗', '﹖')

        // Quotation closing marks
        val CLOSING_QUOTES = setOf('"', '\'', '”', '’', '»', '›', '」', '』', '”')

        // Brackets closing marks
        val CLOSING_BRACKETS = setOf(')', ']', '}', '〉', '》', '】', '〕')
    }

    fun detectSentences(
        text: String,
        settings: SentenceDetectionSettings = SentenceDetectionSettings(),
        lengthSettings: LengthSettings = LengthSettings()
    ): List<Sentence> {
        if (text.isBlank()) return emptyList()

        val abbreviations = buildAbbreviationSet(settings.customAbbreviations)
        val sentences = mutableListOf<Sentence>()

        val len = text.length
        var sentenceStart = -1

        // Helper to find the start of next sentence (skipping leading whitespace)
        fun findNextContentStart(fromIndex: Int): Int {
            var idx = fromIndex
            while (idx < len && Character.isWhitespace(text[idx])) {
                idx++
            }
            return if (idx < len) idx else -1
        }

        sentenceStart = findNextContentStart(0)
        if (sentenceStart == -1) return emptyList()

        var i = sentenceStart

        while (i < len) {
            // Check for Paragraph Break if enabled
            if (settings.paragraphBoundaryMode && isParagraphBreak(text, i)) {
                val sentenceEnd = i
                val candidateText = text.substring(sentenceStart, sentenceEnd).trim()
                if (hasLexicalContent(candidateText) || (settings.countPunctuationOnly && candidateText.isNotEmpty())) {
                    val actualEnd = sentenceStart + text.substring(sentenceStart, sentenceEnd).trimEnd().length
                    val sText = text.substring(sentenceStart, actualEnd)
                    sentences.add(
                        createSentence(
                            index = sentences.size + 1,
                            text = sText,
                            startOffset = sentenceStart,
                            endOffset = actualEnd,
                            lengthSettings = lengthSettings
                        )
                    )
                }
                // Skip newline block
                while (i < len && (text[i] == '\r' || text[i] == '\n' || Character.isWhitespace(text[i]))) {
                    i++
                }
                sentenceStart = i
                continue
            }

            // Check if current position is terminal punctuation
            val termLength = getTerminalPunctuationLength(text, i, settings)
            if (termLength > 0) {
                val punctStart = i
                val punctEnd = i + termLength

                // Check if this punctuation is NOT a sentence boundary
                if (isNonBoundaryPunctuation(text, punctStart, punctEnd, abbreviations)) {
                    i = punctEnd
                    continue
                }

                // Boundary confirmed! Now expand past any closing quotes or brackets
                var boundaryEnd = punctEnd
                while (boundaryEnd < len && (text[boundaryEnd] in CLOSING_QUOTES || text[boundaryEnd] in CLOSING_BRACKETS)) {
                    boundaryEnd++
                }

                // Check dialogue attribution: e.g. "Are you coming?" he asked.
                // If closing quote exists and is followed by whitespace + lowercase word, sentence continues!
                if (boundaryEnd > punctEnd && boundaryEnd < len && isDialogueAttributionFollows(text, boundaryEnd)) {
                    i = boundaryEnd
                    continue
                }

                // Extract sentence
                val candidateText = text.substring(sentenceStart, boundaryEnd).trim()
                if (hasLexicalContent(candidateText) || (settings.countPunctuationOnly && candidateText.isNotEmpty())) {
                    val sText = text.substring(sentenceStart, boundaryEnd)
                    sentences.add(
                        createSentence(
                            index = sentences.size + 1,
                            text = sText,
                            startOffset = sentenceStart,
                            endOffset = boundaryEnd,
                            lengthSettings = lengthSettings
                        )
                    )
                }

                // Find next sentence start
                sentenceStart = findNextContentStart(boundaryEnd)
                if (sentenceStart == -1) break
                i = sentenceStart
                continue
            }

            i++
        }

        // Add any remaining text as a sentence if valid
        if (sentenceStart != -1 && sentenceStart < len) {
            val remaining = text.substring(sentenceStart).trimEnd()
            if (hasLexicalContent(remaining) || (settings.countPunctuationOnly && remaining.isNotEmpty())) {
                val endOffset = sentenceStart + remaining.length
                val sText = text.substring(sentenceStart, endOffset)
                sentences.add(
                    createSentence(
                        index = sentences.size + 1,
                        text = sText,
                        startOffset = sentenceStart,
                        endOffset = endOffset,
                        lengthSettings = lengthSettings
                    )
                )
            }
        }

        return sentences
    }

    private fun isParagraphBreak(text: String, idx: Int): Boolean {
        if (text[idx] == '\n') {
            // Check if followed by optional whitespace and another \n
            var j = idx + 1
            while (j < text.length && text[j] == ' ') j++
            if (j < text.length && text[j] == '\n') {
                return true
            }
        }
        return false
    }

    private fun getTerminalPunctuationLength(text: String, idx: Int, settings: SentenceDetectionSettings): Int {
        val ch = text[idx]

        // Custom terminators check
        for (term in settings.customTerminators) {
            if (term.isNotEmpty() && text.startsWith(term, idx)) {
                return term.length
            }
        }

        var isTerminal = false
        if (settings.enablePeriod && ch == '.') isTerminal = true
        if (settings.enableQuestion && ch == '?') isTerminal = true
        if (settings.enableExclamation && ch == '!') isTerminal = true

        if (settings.enableUnicodeEquivalents) {
            if (ch in CJK_TERMINATORS) isTerminal = true
            if (ch == '؟' || ch == '۔' || ch == '…' || ch == '᠃' || ch == '᠉' || ch == '।' || ch == '॥') isTerminal = true
        }

        if (!isTerminal) return 0

        // Consume consecutive terminal punctuation marks (e.g. "?!", "...", "!!!")
        var end = idx + 1
        while (end < text.length) {
            val nextCh = text[end]
            val nextIsTerminal = (settings.enablePeriod && nextCh == '.') ||
                    (settings.enableQuestion && nextCh == '?') ||
                    (settings.enableExclamation && nextCh == '!') ||
                    (settings.enableUnicodeEquivalents && (nextCh in CJK_TERMINATORS || nextCh == '؟' || nextCh == '۔' || nextCh == '…'))

            if (nextIsTerminal) {
                end++
            } else {
                break
            }
        }

        return end - idx
    }

    private fun isNonBoundaryPunctuation(
        text: String,
        start: Int,
        end: Int,
        abbreviations: Set<String>
    ): Boolean {
        val ch = text[start]
        val isPeriodOrDots = (ch == '.' || ch == '…')

        // 1. CJK terminators are always boundaries
        if (ch in CJK_TERMINATORS) {
            return false
        }

        // 2. Check if decimal number or version number: e.g. "12.50" or "2.5.1"
        if (ch == '.' && (end - start == 1)) {
            val prevChar = if (start > 0) text[start - 1] else null
            val nextChar = if (end < text.length) text[end] else null
            if (prevChar != null && prevChar.isDigit() && nextChar != null && nextChar.isDigit()) {
                return true
            }
        }

        // 3. Check if inside URL or Email
        // If period is preceded by letter/digit and followed immediately by letter/digit (e.g. example.com, support@test.org)
        if (ch == '.' && (end - start == 1)) {
            val prevChar = if (start > 0) text[start - 1] else null
            val nextChar = if (end < text.length) text[end] else null
            if (prevChar != null && (prevChar.isLetterOrDigit() || prevChar == '/' || prevChar == '_') &&
                nextChar != null && nextChar.isLetterOrDigit()
            ) {
                return true
            }
        }

        // 4. Check if Abbreviation or Initial
        if (isPeriodOrDots && (end - start == 1)) {
            // Check preceding word token
            val prevWord = getPrecedingWordToken(text, start)
            if (prevWord.isNotEmpty()) {
                val cleanWord = prevWord.lowercase(Locale.ROOT).trimEnd('.')
                if (abbreviations.contains(cleanWord)) {
                    // It's an abbreviation like Dr., Mr., etc.
                    return true
                }

                // Initials handling: e.g. "J. K. Rowling" or "J.R.R. Tolkien"
                if (isInitial(prevWord, text, start, end)) {
                    return true
                }
            }
        }

        // 5. Normal period / exclamation / question must be followed by whitespace, newline, closing quote, closing bracket, or end of text
        // If it is immediately followed by a normal letter without spaces (like in code or malformed text), it's not a boundary
        if (end < text.length) {
            val nextChar = text[end]
            val isFollowedByBoundaryMarker = Character.isWhitespace(nextChar) ||
                    nextChar in CLOSING_QUOTES ||
                    nextChar in CLOSING_BRACKETS ||
                    nextChar in CJK_TERMINATORS

            if (!isFollowedByBoundaryMarker) {
                return true
            }
        }

        return false
    }

    private fun isInitial(prevWord: String, text: String, punctStart: Int, punctEnd: Int): Boolean {
        // A single uppercase letter: e.g. "J"
        if (prevWord.length == 1 && prevWord[0].isUpperCase()) {
            // Check what follows after the punctuation and whitespace
            var nextContentIdx = punctEnd
            while (nextContentIdx < text.length && Character.isWhitespace(text[nextContentIdx])) {
                nextContentIdx++
            }
            if (nextContentIdx < text.length) {
                val nextCh = text[nextContentIdx]
                // If followed by another uppercase letter, e.g. "K." or "Rowling"
                if (nextCh.isUpperCase()) {
                    return true
                }
            }
            // Or if preceded immediately by another initial: e.g. in "J.R.R."
            if (punctStart >= 2 && text[punctStart - 2] == '.') {
                return true
            }
        }

        // Multi-dot initials like "J.R.R"
        val parts = prevWord.split('.')
        if (parts.all { it.length == 1 && it[0].isUpperCase() }) {
            var nextContentIdx = punctEnd
            while (nextContentIdx < text.length && Character.isWhitespace(text[nextContentIdx])) {
                nextContentIdx++
            }
            if (nextContentIdx < text.length && text[nextContentIdx].isUpperCase()) {
                return true
            }
        }

        return false
    }

    private fun isDialogueAttributionFollows(text: String, boundaryEnd: Int): Boolean {
        // Find next word after boundaryEnd
        var idx = boundaryEnd
        while (idx < text.length && Character.isWhitespace(text[idx])) {
            idx++
        }
        if (idx < text.length) {
            val ch = text[idx]
            // If the following token starts with a lowercase letter, it's attribution (e.g. "he asked", "said she")
            if (ch.isLowerCase()) {
                return true
            }
        }
        return false
    }

    private fun getPrecedingWordToken(text: String, beforeIndex: Int): String {
        var start = beforeIndex - 1
        while (start >= 0 && (text[start].isLetterOrDigit() || text[start] == '.')) {
            start--
        }
        return text.substring(start + 1, beforeIndex)
    }

    private fun hasLexicalContent(s: String): Boolean {
        return s.any { Character.isLetterOrDigit(it) }
    }

    private fun buildAbbreviationSet(custom: List<String>): Set<String> {
        val set = HashSet(DEFAULT_ABBREVIATIONS)
        for (abbr in custom) {
            val clean = abbr.lowercase(Locale.ROOT).trim().trimEnd('.')
            if (clean.isNotEmpty()) {
                set.add(clean)
            }
        }
        return set
    }

    private fun createSentence(
        index: Int,
        text: String,
        startOffset: Int,
        endOffset: Int,
        lengthSettings: LengthSettings
    ): Sentence {
        val wordCount = WordCounter.countWords(text, lengthSettings)
        val charCount = CharacterCounter.countCharacters(text, lengthSettings)
        return Sentence(
            index = index,
            text = text,
            wordCount = wordCount,
            characterCount = charCount,
            startOffset = startOffset,
            endOffset = endOffset
        )
    }
}
