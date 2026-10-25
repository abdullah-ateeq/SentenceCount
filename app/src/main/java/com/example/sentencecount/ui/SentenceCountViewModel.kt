package com.example.sentencecount.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentencecount.data.HistoryRepository
import com.example.sentencecount.data.PreferencesManager
import com.example.sentencecount.engine.CharacterCounter
import com.example.sentencecount.engine.SentenceDetector
import com.example.sentencecount.engine.StatisticsCalculator
import com.example.sentencecount.engine.WordCounter
import com.example.sentencecount.model.AppTheme
import com.example.sentencecount.model.BehaviorSettings
import com.example.sentencecount.model.DisplaySettings
import com.example.sentencecount.model.HyphenMode
import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.LengthUnit
import com.example.sentencecount.model.Sentence
import com.example.sentencecount.model.SentenceCountHistory
import com.example.sentencecount.model.SentenceCountUiState
import com.example.sentencecount.model.SentenceDetectionSettings
import com.example.sentencecount.model.SentenceSortOrder
import com.example.sentencecount.model.ThemeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class SentenceCountViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val historyRepository = HistoryRepository(application)
    private val sentenceDetector = SentenceDetector()

    private val _uiState = MutableStateFlow(SentenceCountUiState())
    val uiState: StateFlow<SentenceCountUiState> = _uiState.asStateFlow()

    // Settings flows
    private val _detectionSettings = MutableStateFlow(preferencesManager.getDetectionSettings())
    val detectionSettings: StateFlow<SentenceDetectionSettings> = _detectionSettings.asStateFlow()

    private val _lengthSettings = MutableStateFlow(preferencesManager.getLengthSettings())
    val lengthSettings: StateFlow<LengthSettings> = _lengthSettings.asStateFlow()

    private val _displaySettings = MutableStateFlow(preferencesManager.getDisplaySettings())
    val displaySettings: StateFlow<DisplaySettings> = _displaySettings.asStateFlow()

    private val _behaviorSettings = MutableStateFlow(preferencesManager.getBehaviorSettings())
    val behaviorSettings: StateFlow<BehaviorSettings> = _behaviorSettings.asStateFlow()

    private val _themeSettings = MutableStateFlow(preferencesManager.getThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private val _historyList = MutableStateFlow<List<SentenceCountHistory>>(emptyList())
    val historyList: StateFlow<List<SentenceCountHistory>> = _historyList.asStateFlow()

    // Status message for snakbars or toasts
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var analysisJob: Job? = null

    init {
        loadHistory()
        if (_behaviorSettings.value.restoreLastText) {
            val lastText = preferencesManager.getLastText()
            if (lastText.isNotBlank()) {
                onTextChanged(lastText, triggerLiveAnalysis = true)
            }
        }
    }

    fun loadHistory() {
        _historyList.value = historyRepository.getHistory()
    }

    fun onTextChanged(newText: String, triggerLiveAnalysis: Boolean = true) {
        val lineCount = if (newText.isEmpty()) 0 else newText.lines().size
        _uiState.update {
            it.copy(
                text = newText,
                totalLines = lineCount,
                errorMessage = null
            )
        }

        if (_behaviorSettings.value.restoreLastText) {
            preferencesManager.setLastText(newText)
        }

        if (newText.isBlank()) {
            analysisJob?.cancel()
            _uiState.update {
                SentenceCountUiState(
                    text = newText,
                    totalLines = lineCount
                )
            }
            return
        }

        if (triggerLiveAnalysis && _behaviorSettings.value.liveAnalysis) {
            scheduleDebouncedAnalysis()
        }
    }

    fun analyzeText() {
        val currentText = _uiState.value.text
        if (currentText.isBlank()) {
            _uiState.update {
                it.copy(
                    sentences = emptyList(),
                    totalSentences = 0,
                    errorMessage = "Enter some text to analyze."
                )
            }
            return
        }

        runAnalysis(currentText)
    }

    private fun scheduleDebouncedAnalysis() {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            runAnalysis(_uiState.value.text)
        }
    }

    private fun runAnalysis(textToAnalyze: String) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }

            val detSettings = _detectionSettings.value
            val lenSettings = _lengthSettings.value
            val dispSettings = _displaySettings.value

            val result = withContext(Dispatchers.Default) {
                // 1. Detect sentences
                val sentences = sentenceDetector.detectSentences(
                    text = textToAnalyze,
                    settings = detSettings,
                    lengthSettings = lenSettings
                )

                // 2. Statistics
                val stats = StatisticsCalculator.calculateStatistics(sentences, lenSettings)

                // 3. Distribution
                val dist = StatisticsCalculator.calculateDistribution(sentences, lenSettings)

                // 4. Shortest / Longest sentences
                val shortestSentences = if (stats.shortestLength != null) {
                    sentences.filter { StatisticsCalculator.getSentenceLength(it, lenSettings) == stats.shortestLength }
                } else emptyList()

                val longestSentences = if (stats.longestLength != null) {
                    sentences.filter { StatisticsCalculator.getSentenceLength(it, lenSettings) == stats.longestLength }
                } else emptyList()

                // 5. Short / Medium / Long classification
                val groups = StatisticsCalculator.calculateGroups(sentences, lenSettings, dispSettings)

                object {
                    val sentences = sentences
                    val stats = stats
                    val dist = dist
                    val shortest = shortestSentences
                    val longest = longestSentences
                    val groups = groups
                }
            }

            _uiState.update { state ->
                state.copy(
                    sentences = result.sentences,
                    totalSentences = result.stats.totalSentences,
                    totalWords = result.stats.totalWords,
                    totalCharacters = result.stats.totalCharacters,
                    shortestLength = result.stats.shortestLength,
                    longestLength = result.stats.longestLength,
                    averageLength = result.stats.averageLength,
                    medianLength = result.stats.medianLength,
                    modeLengths = result.stats.modeLengths,
                    range = result.stats.range,
                    averageCharactersPerSentence = result.stats.averageCharactersPerSentence,
                    standardDeviation = result.stats.standardDeviation,
                    distribution = result.dist,
                    shortestSentences = result.shortest,
                    longestSentences = result.longest,
                    shortCount = result.groups.shortCount,
                    shortPercentage = result.groups.shortPercentage,
                    mediumCount = result.groups.mediumCount,
                    mediumPercentage = result.groups.mediumPercentage,
                    longCount = result.groups.longCount,
                    longPercentage = result.groups.longPercentage,
                    isAnalyzing = false,
                    errorMessage = null
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSortOrderChanged(sortOrder: SentenceSortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
    }

    fun clearText() {
        onTextChanged("", triggerLiveAnalysis = false)
        preferencesManager.setLastText("")
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // History
    fun saveCurrentToHistory(): Boolean {
        val state = _uiState.value
        if (state.text.isBlank() || state.totalSentences == 0) {
            showMessage("No analysis to save")
            return false
        }

        val det = _detectionSettings.value
        val len = _lengthSettings.value

        val historyItem = SentenceCountHistory(
            id = UUID.randomUUID().toString(),
            text = state.text,
            analyzedAt = System.currentTimeMillis(),
            lengthUnit = len.lengthUnit.name,
            includeSpaces = len.includeSpaces,
            includePunctuation = len.includePunctuation,
            countNumbers = len.countNumbers,
            hyphenMode = len.hyphenMode.name,
            paragraphBoundaryMode = det.paragraphBoundaryMode,
            countPunctuationOnly = det.countPunctuationOnly,
            customAbbreviations = det.customAbbreviations,
            customTerminators = det.customTerminators
        )

        val saved = historyRepository.saveAnalysis(historyItem)
        if (saved) {
            loadHistory()
            showMessage("Analysis saved to history")
        } else {
            showMessage("Analysis already saved in history")
        }
        return saved
    }

    fun restoreHistory(item: SentenceCountHistory) {
        val lenUnit = runCatching { LengthUnit.valueOf(item.lengthUnit) }.getOrDefault(LengthUnit.WORDS)
        val hypMode = runCatching { HyphenMode.valueOf(item.hyphenMode) }.getOrDefault(HyphenMode.KEEP)

        val newLenSettings = _lengthSettings.value.copy(
            lengthUnit = lenUnit,
            includeSpaces = item.includeSpaces,
            includePunctuation = item.includePunctuation,
            countNumbers = item.countNumbers,
            hyphenMode = hypMode
        )
        val newDetSettings = _detectionSettings.value.copy(
            paragraphBoundaryMode = item.paragraphBoundaryMode,
            countPunctuationOnly = item.countPunctuationOnly,
            customAbbreviations = item.customAbbreviations,
            customTerminators = item.customTerminators
        )

        updateLengthSettings(newLenSettings)
        updateDetectionSettings(newDetSettings)

        onTextChanged(item.text, triggerLiveAnalysis = false)
        runAnalysis(item.text)
        showMessage("Restored analysis from history")
    }

    fun deleteHistoryItem(id: String) {
        historyRepository.deleteItem(id)
        loadHistory()
        showMessage("History entry deleted")
    }

    fun clearAllHistory() {
        historyRepository.clearHistory()
        loadHistory()
        showMessage("History cleared")
    }

    // Settings Updates
    fun updateDetectionSettings(settings: SentenceDetectionSettings) {
        _detectionSettings.value = settings
        preferencesManager.saveDetectionSettings(settings)
        if (_uiState.value.text.isNotBlank()) {
            runAnalysis(_uiState.value.text)
        }
    }

    fun updateLengthSettings(settings: LengthSettings) {
        _lengthSettings.value = settings
        preferencesManager.saveLengthSettings(settings)
        if (_uiState.value.text.isNotBlank()) {
            runAnalysis(_uiState.value.text)
        }
    }

    fun updateDisplaySettings(settings: DisplaySettings) {
        _displaySettings.value = settings
        preferencesManager.saveDisplaySettings(settings)
        if (_uiState.value.text.isNotBlank()) {
            runAnalysis(_uiState.value.text)
        }
    }

    fun updateBehaviorSettings(settings: BehaviorSettings) {
        _behaviorSettings.value = settings
        preferencesManager.saveBehaviorSettings(settings)
    }

    fun updateThemeSettings(settings: ThemeSettings) {
        _themeSettings.value = settings
        preferencesManager.saveThemeSettings(settings)
    }

    fun resetSettingsToDefaults() {
        preferencesManager.resetToDefaults()
        _detectionSettings.value = preferencesManager.getDetectionSettings()
        _lengthSettings.value = preferencesManager.getLengthSettings()
        _displaySettings.value = preferencesManager.getDisplaySettings()
        _behaviorSettings.value = preferencesManager.getBehaviorSettings()
        _themeSettings.value = preferencesManager.getThemeSettings()
        if (_uiState.value.text.isNotBlank()) {
            runAnalysis(_uiState.value.text)
        }
        showMessage("Settings reset to defaults")
    }

    // Export Helpers
    fun getSummaryText(): String {
        val s = _uiState.value
        val lenUnit = _lengthSettings.value.lengthUnit.label.lowercase(Locale.ROOT)
        val avgWords = String.format(Locale.US, "%.2f", s.averageLength ?: 0.0)
        val medianStr = if (s.medianLength != null) {
            if (s.medianLength % 1.0 == 0.0) s.medianLength.toInt().toString() else String.format(Locale.US, "%.2f", s.medianLength)
        } else "N/A"
        val modeStr = if (s.modeLengths.isNotEmpty()) s.modeLengths.joinToString(", ") + " $lenUnit" else "N/A"
        val avgChars = String.format(Locale.US, "%.2f", s.averageCharactersPerSentence ?: 0.0)
        val sdStr = String.format(Locale.US, "%.2f", s.standardDeviation ?: 0.0)

        return buildString {
            appendLine("SentenceCount")
            appendLine("Count sentences and analyze sentence lengths.")
            appendLine()
            appendLine("SENTENCE ANALYSIS")
            appendLine("Total sentences: ${s.totalSentences}")
            appendLine("Total words: ${s.totalWords}")
            appendLine("Total characters: ${s.totalCharacters}")
            appendLine()
            appendLine("Shortest sentence: ${s.shortestLength ?: 0} $lenUnit")
            appendLine("Longest sentence: ${s.longestLength ?: 0} $lenUnit")
            appendLine()
            appendLine("Average: $avgWords $lenUnit")
            appendLine("Median: $medianStr $lenUnit")
            appendLine("Most common length: $modeStr")
            appendLine("Range: ${s.range ?: 0} $lenUnit")
            appendLine("Average characters per sentence: $avgChars")
            appendLine("Standard deviation: $sdStr")
        }
    }

    fun getAllSentencesText(): String {
        val s = _uiState.value
        return buildString {
            s.sentences.forEach {
                appendLine("${it.index}. ${it.text}")
            }
        }
    }

    fun getSentenceCsvString(): String {
        val s = _uiState.value
        val totalWords = s.totalWords.toDouble().coerceAtLeast(1.0)
        return buildString {
            appendLine("Sentence Number,Sentence,Word Count,Character Count,Percentage of Total Words")
            s.sentences.forEach { sentence ->
                val pct = String.format(Locale.US, "%.2f", (sentence.wordCount / totalWords) * 100.0)
                val escapedSentence = "\"" + sentence.text.replace("\"", "\"\"") + "\""
                appendLine("${sentence.index},$escapedSentence,${sentence.wordCount},${sentence.characterCount},$pct")
            }
        }
    }

    fun getDistributionCsvString(): String {
        val s = _uiState.value
        return buildString {
            appendLine("Sentence Length,Sentence Count,Percentage")
            s.distribution.forEach { item ->
                val pct = String.format(Locale.US, "%.2f", item.percentage)
                appendLine("${item.length},${item.sentenceCount},$pct")
            }
        }
    }

    fun getFilteredAndSortedSentences(): List<Sentence> {
        val s = _uiState.value
        var list = s.sentences
        if (s.searchQuery.isNotBlank()) {
            list = list.filter { it.text.contains(s.searchQuery, ignoreCase = true) }
        }

        return when (s.sortOrder) {
            SentenceSortOrder.ORIGINAL -> list
            SentenceSortOrder.SHORTEST_FIRST -> list.sortedBy { StatisticsCalculator.getSentenceLength(it, _lengthSettings.value) }
            SentenceSortOrder.LONGEST_FIRST -> list.sortedByDescending { StatisticsCalculator.getSentenceLength(it, _lengthSettings.value) }
            SentenceSortOrder.ALPHABETICAL -> list.sortedBy { it.text.lowercase(Locale.ROOT) }
            SentenceSortOrder.REVERSE_ALPHABETICAL -> list.sortedByDescending { it.text.lowercase(Locale.ROOT) }
        }
    }
}
