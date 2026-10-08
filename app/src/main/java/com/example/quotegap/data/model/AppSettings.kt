package com.example.quotegap.data.model

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultCurrency: String = "PKR",
    val customCurrency: String = "",
    val comparisonLabel: ComparisonLabel = ComparisonLabel.TARGET_BUDGET,
    val defaultMode: ComparisonMode = ComparisonMode.TOTAL,
    val defaultBudget: String = "100000",
    val defaultQuote: String = "92000",
    val decimalPlaces: Int = 2,
    val showBudgetBar: Boolean = true,
    val showPercentage: Boolean = true,
    val showFormula: Boolean = true,
    val showNotes: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
) {
    val activeCurrency: String
        get() = if (defaultCurrency == "Other" && customCurrency.isNotBlank()) customCurrency.trim() else defaultCurrency
}
