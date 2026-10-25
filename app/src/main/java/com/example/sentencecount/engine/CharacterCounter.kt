package com.example.sentencecount.engine

import com.example.sentencecount.model.CharacterUnit
import com.example.sentencecount.model.LengthSettings
import java.text.BreakIterator

object CharacterCounter {

    /**
     * Determines whether a Unicode code point is considered punctuation or a symbol.
     */
    fun isPunctuation(codePoint: Int): Boolean {
        val type = Character.getType(codePoint).toByte()
        return when (type) {
            Character.CONNECTOR_PUNCTUATION,
            Character.DASH_PUNCTUATION,
            Character.START_PUNCTUATION,
            Character.END_PUNCTUATION,
            Character.INITIAL_QUOTE_PUNCTUATION,
            Character.FINAL_QUOTE_PUNCTUATION,
            Character.OTHER_PUNCTUATION,
            Character.MATH_SYMBOL,
            Character.CURRENCY_SYMBOL,
            Character.MODIFIER_SYMBOL,
            Character.OTHER_SYMBOL -> true
            else -> false
        }
    }

    /**
     * Counts characters in [text] according to [settings].
     */
    fun countCharacters(text: String, settings: LengthSettings): Int {
        if (text.isEmpty()) return 0

        return when (settings.characterUnit) {
            CharacterUnit.GRAPHEME -> countGraphemes(text, settings.includeSpaces, settings.includePunctuation)
            CharacterUnit.CODE_POINT -> countCodePoints(text, settings.includeSpaces, settings.includePunctuation)
        }
    }

    private fun countGraphemes(text: String, includeSpaces: Boolean, includePunctuation: Boolean): Int {
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(text)
        var count = 0
        var start = boundary.first()
        var end = boundary.next()

        while (end != BreakIterator.DONE) {
            val cluster = text.substring(start, end)
            val firstCodePoint = cluster.codePointAt(0)
            val isWhitespace = cluster.all { Character.isWhitespace(it) }

            if (!includeSpaces && isWhitespace) {
                // Skip whitespace
            } else if (!includePunctuation && isPunctuation(firstCodePoint)) {
                // Skip punctuation
            } else {
                count++
            }

            start = end
            end = boundary.next()
        }
        return count
    }

    private fun countCodePoints(text: String, includeSpaces: Boolean, includePunctuation: Boolean): Int {
        var count = 0
        var i = 0
        val length = text.length
        while (i < length) {
            val cp = text.codePointAt(i)
            val isWhitespace = Character.isWhitespace(cp)

            if (!includeSpaces && isWhitespace) {
                // Skip
            } else if (!includePunctuation && isPunctuation(cp)) {
                // Skip
            } else {
                count++
            }
            i += Character.charCount(cp)
        }
        return count
    }
}
