package com.example.quotegap.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.quotegap.data.model.AppSettings
import com.example.quotegap.data.model.AppThemeMode
import com.example.quotegap.data.model.ComparisonLabel
import com.example.quotegap.data.model.ComparisonMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "quotegap_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val CUSTOM_CURRENCY = stringPreferencesKey("custom_currency")
        val COMPARISON_LABEL = stringPreferencesKey("comparison_label")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val DEFAULT_BUDGET = stringPreferencesKey("default_budget")
        val DEFAULT_QUOTE = stringPreferencesKey("default_quote")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val SHOW_BUDGET_BAR = booleanPreferencesKey("show_budget_bar")
        val SHOW_PERCENTAGE = booleanPreferencesKey("show_percentage")
        val SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val SHOW_NOTES = booleanPreferencesKey("show_notes")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }

        val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
        val defaultCurrency = preferences[PreferencesKeys.DEFAULT_CURRENCY] ?: "PKR"
        val customCurrency = preferences[PreferencesKeys.CUSTOM_CURRENCY] ?: ""

        val labelStr = preferences[PreferencesKeys.COMPARISON_LABEL] ?: ComparisonLabel.TARGET_BUDGET.name
        val comparisonLabel = try {
            ComparisonLabel.valueOf(labelStr)
        } catch (_: Exception) {
            ComparisonLabel.TARGET_BUDGET
        }

        val modeStr = preferences[PreferencesKeys.DEFAULT_MODE] ?: ComparisonMode.TOTAL.name
        val defaultMode = try {
            ComparisonMode.valueOf(modeStr)
        } catch (_: Exception) {
            ComparisonMode.TOTAL
        }

        val defaultBudget = preferences[PreferencesKeys.DEFAULT_BUDGET] ?: "100000"
        val defaultQuote = preferences[PreferencesKeys.DEFAULT_QUOTE] ?: "92000"
        val decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: 2
        val showBudgetBar = preferences[PreferencesKeys.SHOW_BUDGET_BAR] ?: true
        val showPercentage = preferences[PreferencesKeys.SHOW_PERCENTAGE] ?: true
        val showFormula = preferences[PreferencesKeys.SHOW_FORMULA] ?: true
        val showNotes = preferences[PreferencesKeys.SHOW_NOTES] ?: true
        val hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
        val confirmDelete = preferences[PreferencesKeys.CONFIRM_DELETE] ?: true

        AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            defaultCurrency = defaultCurrency,
            customCurrency = customCurrency,
            comparisonLabel = comparisonLabel,
            defaultMode = defaultMode,
            defaultBudget = defaultBudget,
            defaultQuote = defaultQuote,
            decimalPlaces = decimalPlaces,
            showBudgetBar = showBudgetBar,
            showPercentage = showPercentage,
            showFormula = showFormula,
            showNotes = showNotes,
            hapticFeedback = hapticFeedback,
            confirmDelete = confirmDelete
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = settings.themeMode.name
            preferences[PreferencesKeys.DYNAMIC_COLOR] = settings.dynamicColor
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = settings.defaultCurrency
            preferences[PreferencesKeys.CUSTOM_CURRENCY] = settings.customCurrency
            preferences[PreferencesKeys.COMPARISON_LABEL] = settings.comparisonLabel.name
            preferences[PreferencesKeys.DEFAULT_MODE] = settings.defaultMode.name
            preferences[PreferencesKeys.DEFAULT_BUDGET] = settings.defaultBudget
            preferences[PreferencesKeys.DEFAULT_QUOTE] = settings.defaultQuote
            preferences[PreferencesKeys.DECIMAL_PLACES] = settings.decimalPlaces
            preferences[PreferencesKeys.SHOW_BUDGET_BAR] = settings.showBudgetBar
            preferences[PreferencesKeys.SHOW_PERCENTAGE] = settings.showPercentage
            preferences[PreferencesKeys.SHOW_FORMULA] = settings.showFormula
            preferences[PreferencesKeys.SHOW_NOTES] = settings.showNotes
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = settings.hapticFeedback
            preferences[PreferencesKeys.CONFIRM_DELETE] = settings.confirmDelete
        }
    }

    suspend fun resetSettings() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
