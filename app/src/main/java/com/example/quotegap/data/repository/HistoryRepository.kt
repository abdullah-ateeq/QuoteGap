package com.example.quotegap.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.quotegap.data.model.SavedCalculation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "quotegap_history")

class HistoryRepository(
    private val context: Context,
    private val gson: Gson = Gson()
) {

    companion object {
        const val MAX_HISTORY_SIZE = 50
        val KEY_HISTORY = stringPreferencesKey("history_list_json")
    }

    val historyFlow: Flow<List<SavedCalculation>> = context.historyDataStore.data.map { preferences ->
        val json = preferences[KEY_HISTORY] ?: "[]"
        deserializeHistory(json)
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        context.historyDataStore.edit { preferences ->
            val json = preferences[KEY_HISTORY] ?: "[]"
            val currentList = deserializeHistory(json).toMutableList()

            // Remove if duplicate ID already exists, then add at top
            currentList.removeAll { it.id == calculation.id }
            currentList.add(0, calculation)

            // Keep max 50 items (oldest gets dropped)
            val trimmedList = if (currentList.size > MAX_HISTORY_SIZE) {
                currentList.take(MAX_HISTORY_SIZE)
            } else {
                currentList
            }

            preferences[KEY_HISTORY] = gson.toJson(trimmedList)
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.historyDataStore.edit { preferences ->
            val json = preferences[KEY_HISTORY] ?: "[]"
            val currentList = deserializeHistory(json).toMutableList()
            currentList.removeAll { it.id == id }
            preferences[KEY_HISTORY] = gson.toJson(currentList)
        }
    }

    suspend fun clearHistory() {
        context.historyDataStore.edit { preferences ->
            preferences[KEY_HISTORY] = "[]"
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.historyDataStore.edit { preferences ->
            val json = preferences[KEY_HISTORY] ?: "[]"
            val currentList = deserializeHistory(json).map { item ->
                if (item.id == id) {
                    item.copy(isFavorite = !item.isFavorite)
                } else {
                    item
                }
            }
            preferences[KEY_HISTORY] = gson.toJson(currentList)
        }
    }

    suspend fun duplicateCalculation(id: String) {
        context.historyDataStore.edit { preferences ->
            val json = preferences[KEY_HISTORY] ?: "[]"
            val currentList = deserializeHistory(json).toMutableList()
            val original = currentList.find { it.id == id } ?: return@edit

            val duplicate = original.copy(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                projectName = "Copy of ${original.projectName.ifBlank { "Untitled Quote" }}"
            )

            currentList.add(0, duplicate)
            val trimmedList = if (currentList.size > MAX_HISTORY_SIZE) {
                currentList.take(MAX_HISTORY_SIZE)
            } else {
                currentList
            }

            preferences[KEY_HISTORY] = gson.toJson(trimmedList)
        }
    }

    fun deserializeHistory(json: String): List<SavedCalculation> {
        return try {
            val listType = object : TypeToken<List<SavedCalculation>>() {}.type
            gson.fromJson<List<SavedCalculation>>(json, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeHistory(list: List<SavedCalculation>): String {
        return gson.toJson(list)
    }
}
