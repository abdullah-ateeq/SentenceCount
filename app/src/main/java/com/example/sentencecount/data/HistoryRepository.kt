package com.example.sentencecount.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sentencecount.model.SentenceCountHistory
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HistoryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sentence_count_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY_ITEMS = "history_items"
        const val MAX_HISTORY_ITEMS = 10
    }

    fun getHistory(): List<SentenceCountHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY_ITEMS, null) ?: return emptyList()
        val list = mutableListOf<SentenceCountHistory>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val abbrArray = obj.optJSONArray("customAbbreviations") ?: JSONArray()
                val termArray = obj.optJSONArray("customTerminators") ?: JSONArray()

                val abbrList = mutableListOf<String>()
                for (j in 0 until abbrArray.length()) {
                    abbrList.add(abbrArray.getString(j))
                }

                val termList = mutableListOf<String>()
                for (j in 0 until termArray.length()) {
                    termList.add(termArray.getString(j))
                }

                list.add(
                    SentenceCountHistory(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        analyzedAt = obj.getLong("analyzedAt"),
                        lengthUnit = obj.getString("lengthUnit"),
                        includeSpaces = obj.getBoolean("includeSpaces"),
                        includePunctuation = obj.getBoolean("includePunctuation"),
                        countNumbers = obj.getBoolean("countNumbers"),
                        hyphenMode = obj.getString("hyphenMode"),
                        paragraphBoundaryMode = obj.getBoolean("paragraphBoundaryMode"),
                        countPunctuationOnly = obj.getBoolean("countPunctuationOnly"),
                        customAbbreviations = abbrList,
                        customTerminators = termList
                    )
                )
            }
        } catch (_: Exception) {
            // Return empty list on parse error
        }
        return list
    }

    /**
     * Saves analysis to history.
     * Returns true if saved, false if skipped as duplicate.
     */
    fun saveAnalysis(history: SentenceCountHistory): Boolean {
        val currentList = getHistory().toMutableList()

        // Check for immediate duplicate (text and settings identical)
        val first = currentList.firstOrNull()
        if (first != null && isDuplicate(first, history)) {
            return false
        }

        // Add to front
        currentList.add(0, history)

        // Enforce max 10
        while (currentList.size > MAX_HISTORY_ITEMS) {
            currentList.removeAt(currentList.lastIndex)
        }

        persistList(currentList)
        return true
    }

    fun deleteItem(id: String) {
        val currentList = getHistory().toMutableList()
        currentList.removeAll { it.id == id }
        persistList(currentList)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY_ITEMS).apply()
    }

    private fun isDuplicate(a: SentenceCountHistory, b: SentenceCountHistory): Boolean {
        return a.text == b.text &&
                a.lengthUnit == b.lengthUnit &&
                a.includeSpaces == b.includeSpaces &&
                a.includePunctuation == b.includePunctuation &&
                a.countNumbers == b.countNumbers &&
                a.hyphenMode == b.hyphenMode &&
                a.paragraphBoundaryMode == b.paragraphBoundaryMode &&
                a.countPunctuationOnly == b.countPunctuationOnly &&
                a.customAbbreviations == b.customAbbreviations &&
                a.customTerminators == b.customTerminators
    }

    private fun persistList(list: List<SentenceCountHistory>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("analyzedAt", item.analyzedAt)
            obj.put("lengthUnit", item.lengthUnit)
            obj.put("includeSpaces", item.includeSpaces)
            obj.put("includePunctuation", item.includePunctuation)
            obj.put("countNumbers", item.countNumbers)
            obj.put("hyphenMode", item.hyphenMode)
            obj.put("paragraphBoundaryMode", item.paragraphBoundaryMode)
            obj.put("countPunctuationOnly", item.countPunctuationOnly)

            val abbrArray = JSONArray()
            item.customAbbreviations.forEach { abbrArray.put(it) }
            obj.put("customAbbreviations", abbrArray)

            val termArray = JSONArray()
            item.customTerminators.forEach { termArray.put(it) }
            obj.put("customTerminators", termArray)

            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY_ITEMS, jsonArray.toString()).apply()
    }
}
