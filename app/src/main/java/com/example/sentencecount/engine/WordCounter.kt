package com.example.sentencecount.engine

import com.example.sentencecount.model.ApostropheMode
import com.example.sentencecount.model.HyphenMode
import com.example.sentencecount.model.LengthSettings
import java.util.regex.Pattern

object WordCounter {

    // Regex for URLs and Emails
    private val URL_OR_EMAIL_PATTERN = Pattern.compile(
        """(?i)\b(?:https?://|ftp://|www\.)[^\s<>"'{}|\\^`]+|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}|(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(?:/[^\s<>"'{}|\\^`]*)?"""
    )

    // Number pattern (integers, decimals, formatted numbers)
    private val NUMBER_PATTERN = Pattern.compile(
        """\b\d+(?:[.,]\d+)*\b"""
    )

    fun countWords(text: String, settings: LengthSettings): Int {
        return extractWords(text, settings).size
    }

    fun extractWords(text: String, settings: LengthSettings): List<String> {
        if (text.isBlank()) return emptyList()

        val words = mutableListOf<String>()
        val consumedIndices = BooleanArray(text.length)

        // 1. Extract URLs and Emails if enabled
        if (settings.urlEmailAsSingleToken) {
            val matcher = URL_OR_EMAIL_PATTERN.matcher(text)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                // Trim trailing sentence punctuation from URL if any (e.g. "https://example.com/about.")
                var trimmedEnd = end
                while (trimmedEnd > start && (text[trimmedEnd - 1] in ".,!?;:\"'()[]{}")) {
                    trimmedEnd--
                }
                if (trimmedEnd > start) {
                    val token = text.substring(start, trimmedEnd)
                    words.add(token)
                    for (i in start until trimmedEnd) {
                        consumedIndices[i] = true
                    }
                }
            }
        }

        // 2. Extract Numbers if handled
        val numberMatcher = NUMBER_PATTERN.matcher(text)
        while (numberMatcher.find()) {
            val start = numberMatcher.start()
            val end = numberMatcher.end()
            if (!consumedIndices[start]) {
                if (settings.countNumbers) {
                    words.add(text.substring(start, end))
                }
                for (i in start until end) {
                    consumedIndices[i] = true
                }
            }
        }

        // 3. Tokenize remaining characters
        var i = 0
        val len = text.length
        while (i < len) {
            if (consumedIndices[i]) {
                i++
                continue
            }

            val cp = text.codePointAt(i)
            val charCount = Character.charCount(cp)

            // Check if CJK ideograph
            if (isCjk(cp)) {
                words.add(String(Character.toChars(cp)))
                i += charCount
                continue
            }

            // Check if Letter
            if (Character.isLetter(cp)) {
                val start = i
                var curr = i + charCount

                while (curr < len && !consumedIndices[curr]) {
                    val nextCp = text.codePointAt(curr)
                    val nextCharCount = Character.charCount(nextCp)

                    if (Character.isLetter(nextCp)) {
                        curr += nextCharCount
                    } else if (isApostrophe(nextCp) && settings.apostropheMode == ApostropheMode.KEEP) {
                        // Check if followed by letter (e.g. don't)
                        val afterApos = curr + nextCharCount
                        if (afterApos < len && Character.isLetter(text.codePointAt(afterApos))) {
                            curr = afterApos + Character.charCount(text.codePointAt(afterApos))
                        } else {
                            break
                        }
                    } else if (isHyphen(nextCp) && settings.hyphenMode == HyphenMode.KEEP) {
                        // Check if followed by letter or digit (e.g. well-known)
                        val afterHyphen = curr + nextCharCount
                        if (afterHyphen < len && (Character.isLetter(text.codePointAt(afterHyphen)) || Character.isDigit(text.codePointAt(afterHyphen)))) {
                            curr = afterHyphen + Character.charCount(text.codePointAt(afterHyphen))
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }

                val word = text.substring(start, curr)
                words.add(word)
                i = curr
            } else {
                i += charCount
            }
        }

        return words
    }

    private fun isApostrophe(cp: Int): Boolean {
        return cp == '\''.code || cp == '’'.code || cp == '‘'.code || cp == 'ʼ'.code
    }

    private fun isHyphen(cp: Int): Boolean {
        return cp == '-'.code || cp == '‐'.code || cp == '‑'.code || cp == '–'.code
    }

    private fun isCjk(cp: Int): Boolean {
        val script = Character.UnicodeScript.of(cp)
        return script == Character.UnicodeScript.HAN ||
                script == Character.UnicodeScript.HIRAGANA ||
                script == Character.UnicodeScript.KATAKANA ||
                script == Character.UnicodeScript.HANGUL
    }
}
