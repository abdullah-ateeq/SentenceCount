package com.example.sentencecount.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sentencecount.model.ApostropheMode
import com.example.sentencecount.model.AppTheme
import com.example.sentencecount.model.BehaviorSettings
import com.example.sentencecount.model.CharacterUnit
import com.example.sentencecount.model.DisplaySettings
import com.example.sentencecount.model.HyphenMode
import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.LengthUnit
import com.example.sentencecount.model.SentenceDetectionSettings
import com.example.sentencecount.model.ThemeSettings

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sentence_count_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_TEXT = "last_text"

        // Detection
        private const val KEY_ENABLE_PERIOD = "enable_period"
        private const val KEY_ENABLE_QUESTION = "enable_question"
        private const val KEY_ENABLE_EXCLAMATION = "enable_exclamation"
        private const val KEY_ENABLE_UNICODE = "enable_unicode"
        private const val KEY_PARAGRAPH_BOUNDARY = "paragraph_boundary"
        private const val KEY_COUNT_PUNCTUATION_ONLY = "count_punct_only"
        private const val KEY_CUSTOM_ABBREVIATIONS = "custom_abbreviations"
        private const val KEY_CUSTOM_TERMINATORS = "custom_terminators"

        // Length
        private const val KEY_LENGTH_UNIT = "length_unit"
        private const val KEY_CHAR_UNIT = "char_unit"
        private const val KEY_INCLUDE_SPACES = "include_spaces"
        private const val KEY_INCLUDE_PUNCTUATION = "include_punctuation"
        private const val KEY_COUNT_NUMBERS = "count_numbers"
        private const val KEY_HYPHEN_MODE = "hyphen_mode"
        private const val KEY_APOSTROPHE_MODE = "apostrophe_mode"
        private const val KEY_URL_EMAIL_SINGLE = "url_email_single"

        // Display
        private const val KEY_SHOW_MEDIAN = "show_median"
        private const val KEY_SHOW_MODE = "show_mode"
        private const val KEY_SHOW_DISTRIBUTION = "show_distribution"
        private const val KEY_SHOW_CHART = "show_chart"
        private const val KEY_SHOW_VARIABILITY = "show_variability"
        private const val KEY_SHOW_SHORT_MED_LONG = "show_short_med_long"
        private const val KEY_SHORT_THRESHOLD = "short_threshold"
        private const val KEY_MEDIUM_THRESHOLD = "medium_threshold"

        // Behavior
        private const val KEY_LIVE_ANALYSIS = "live_analysis"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_RESTORE_LAST_TEXT = "restore_last_text"

        // Theme
        private const val KEY_THEME = "app_theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }

    fun getLastText(): String {
        return prefs.getString(KEY_LAST_TEXT, "") ?: ""
    }

    fun setLastText(text: String) {
        prefs.edit().putString(KEY_LAST_TEXT, text).apply()
    }

    fun getDetectionSettings(): SentenceDetectionSettings {
        val customAbbr = prefs.getString(KEY_CUSTOM_ABBREVIATIONS, "") ?: ""
        val customTerm = prefs.getString(KEY_CUSTOM_TERMINATORS, "") ?: ""

        val abbrList = if (customAbbr.isBlank()) emptyList() else customAbbr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val termList = if (customTerm.isBlank()) emptyList() else customTerm.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return SentenceDetectionSettings(
            enablePeriod = prefs.getBoolean(KEY_ENABLE_PERIOD, true),
            enableQuestion = prefs.getBoolean(KEY_ENABLE_QUESTION, true),
            enableExclamation = prefs.getBoolean(KEY_ENABLE_EXCLAMATION, true),
            enableUnicodeEquivalents = prefs.getBoolean(KEY_ENABLE_UNICODE, true),
            paragraphBoundaryMode = prefs.getBoolean(KEY_PARAGRAPH_BOUNDARY, false),
            countPunctuationOnly = prefs.getBoolean(KEY_COUNT_PUNCTUATION_ONLY, false),
            customAbbreviations = abbrList,
            customTerminators = termList
        )
    }

    fun saveDetectionSettings(settings: SentenceDetectionSettings) {
        prefs.edit()
            .putBoolean(KEY_ENABLE_PERIOD, settings.enablePeriod)
            .putBoolean(KEY_ENABLE_QUESTION, settings.enableQuestion)
            .putBoolean(KEY_ENABLE_EXCLAMATION, settings.enableExclamation)
            .putBoolean(KEY_ENABLE_UNICODE, settings.enableUnicodeEquivalents)
            .putBoolean(KEY_PARAGRAPH_BOUNDARY, settings.paragraphBoundaryMode)
            .putBoolean(KEY_COUNT_PUNCTUATION_ONLY, settings.countPunctuationOnly)
            .putString(KEY_CUSTOM_ABBREVIATIONS, settings.customAbbreviations.joinToString(","))
            .putString(KEY_CUSTOM_TERMINATORS, settings.customTerminators.joinToString(","))
            .apply()
    }

    fun getLengthSettings(): LengthSettings {
        val lengthUnitStr = prefs.getString(KEY_LENGTH_UNIT, LengthUnit.WORDS.name) ?: LengthUnit.WORDS.name
        val charUnitStr = prefs.getString(KEY_CHAR_UNIT, CharacterUnit.GRAPHEME.name) ?: CharacterUnit.GRAPHEME.name
        val hyphenStr = prefs.getString(KEY_HYPHEN_MODE, HyphenMode.KEEP.name) ?: HyphenMode.KEEP.name
        val aposStr = prefs.getString(KEY_APOSTROPHE_MODE, ApostropheMode.KEEP.name) ?: ApostropheMode.KEEP.name

        return LengthSettings(
            lengthUnit = runCatching { LengthUnit.valueOf(lengthUnitStr) }.getOrDefault(LengthUnit.WORDS),
            characterUnit = runCatching { CharacterUnit.valueOf(charUnitStr) }.getOrDefault(CharacterUnit.GRAPHEME),
            includeSpaces = prefs.getBoolean(KEY_INCLUDE_SPACES, false),
            includePunctuation = prefs.getBoolean(KEY_INCLUDE_PUNCTUATION, false),
            countNumbers = prefs.getBoolean(KEY_COUNT_NUMBERS, true),
            hyphenMode = runCatching { HyphenMode.valueOf(hyphenStr) }.getOrDefault(HyphenMode.KEEP),
            apostropheMode = runCatching { ApostropheMode.valueOf(aposStr) }.getOrDefault(ApostropheMode.KEEP),
            urlEmailAsSingleToken = prefs.getBoolean(KEY_URL_EMAIL_SINGLE, true)
        )
    }

    fun saveLengthSettings(settings: LengthSettings) {
        prefs.edit()
            .putString(KEY_LENGTH_UNIT, settings.lengthUnit.name)
            .putString(KEY_CHAR_UNIT, settings.characterUnit.name)
            .putBoolean(KEY_INCLUDE_SPACES, settings.includeSpaces)
            .putBoolean(KEY_INCLUDE_PUNCTUATION, settings.includePunctuation)
            .putBoolean(KEY_COUNT_NUMBERS, settings.countNumbers)
            .putString(KEY_HYPHEN_MODE, settings.hyphenMode.name)
            .putString(KEY_APOSTROPHE_MODE, settings.apostropheMode.name)
            .putBoolean(KEY_URL_EMAIL_SINGLE, settings.urlEmailAsSingleToken)
            .apply()
    }

    fun getDisplaySettings(): DisplaySettings {
        return DisplaySettings(
            showMedian = prefs.getBoolean(KEY_SHOW_MEDIAN, true),
            showMode = prefs.getBoolean(KEY_SHOW_MODE, true),
            showDistribution = prefs.getBoolean(KEY_SHOW_DISTRIBUTION, true),
            showChart = prefs.getBoolean(KEY_SHOW_CHART, true),
            showVariability = prefs.getBoolean(KEY_SHOW_VARIABILITY, true),
            showShortMediumLong = prefs.getBoolean(KEY_SHOW_SHORT_MED_LONG, true),
            shortThreshold = prefs.getInt(KEY_SHORT_THRESHOLD, 7),
            mediumThreshold = prefs.getInt(KEY_MEDIUM_THRESHOLD, 14)
        )
    }

    fun saveDisplaySettings(settings: DisplaySettings) {
        prefs.edit()
            .putBoolean(KEY_SHOW_MEDIAN, settings.showMedian)
            .putBoolean(KEY_SHOW_MODE, settings.showMode)
            .putBoolean(KEY_SHOW_DISTRIBUTION, settings.showDistribution)
            .putBoolean(KEY_SHOW_CHART, settings.showChart)
            .putBoolean(KEY_SHOW_VARIABILITY, settings.showVariability)
            .putBoolean(KEY_SHOW_SHORT_MED_LONG, settings.showShortMediumLong)
            .putInt(KEY_SHORT_THRESHOLD, settings.shortThreshold)
            .putInt(KEY_MEDIUM_THRESHOLD, settings.mediumThreshold)
            .apply()
    }

    fun getBehaviorSettings(): BehaviorSettings {
        return BehaviorSettings(
            liveAnalysis = prefs.getBoolean(KEY_LIVE_ANALYSIS, true),
            haptics = prefs.getBoolean(KEY_HAPTICS, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            restoreLastText = prefs.getBoolean(KEY_RESTORE_LAST_TEXT, true)
        )
    }

    fun saveBehaviorSettings(settings: BehaviorSettings) {
        prefs.edit()
            .putBoolean(KEY_LIVE_ANALYSIS, settings.liveAnalysis)
            .putBoolean(KEY_HAPTICS, settings.haptics)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_RESTORE_LAST_TEXT, settings.restoreLastText)
            .apply()
    }

    fun getThemeSettings(): ThemeSettings {
        val themeStr = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return ThemeSettings(
            theme = runCatching { AppTheme.valueOf(themeStr) }.getOrDefault(AppTheme.SYSTEM),
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        )
    }

    fun saveThemeSettings(settings: ThemeSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .apply()
    }

    fun resetToDefaults() {
        saveDetectionSettings(SentenceDetectionSettings())
        saveLengthSettings(LengthSettings())
        saveDisplaySettings(DisplaySettings())
        saveBehaviorSettings(BehaviorSettings())
        saveThemeSettings(ThemeSettings())
    }
}
