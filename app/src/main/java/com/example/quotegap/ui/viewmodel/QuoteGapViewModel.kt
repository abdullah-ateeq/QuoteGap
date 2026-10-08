package com.example.quotegap.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotegap.data.model.AppSettings
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.data.model.QuoteInput
import com.example.quotegap.data.model.QuoteResult
import com.example.quotegap.data.model.QuoteStatus
import com.example.quotegap.data.model.SavedCalculation
import com.example.quotegap.data.repository.HistoryRepository
import com.example.quotegap.data.repository.SettingsRepository
import com.example.quotegap.domain.MoneyFormatter
import com.example.quotegap.domain.QuoteGapCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

enum class HistoryFilter {
    ALL,
    UNDER_BUDGET,
    ON_BUDGET,
    OVER_BUDGET
}

enum class HapticFeedbackType {
    STATUS_CHANGE,
    SAVE,
    FAVORITE,
    DELETE
}

class QuoteGapViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val historyRepository = HistoryRepository(application)

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    // Form Inputs
    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _currency = MutableStateFlow("PKR")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _comparisonMode = MutableStateFlow(ComparisonMode.TOTAL)
    val comparisonMode: StateFlow<ComparisonMode> = _comparisonMode.asStateFlow()

    private val _targetBudgetInput = MutableStateFlow("100000")
    val targetBudgetInput: StateFlow<String> = _targetBudgetInput.asStateFlow()

    private val _quotedPriceInput = MutableStateFlow("92000")
    val quotedPriceInput: StateFlow<String> = _quotedPriceInput.asStateFlow()

    private val _unitPriceInput = MutableStateFlow("")
    val unitPriceInput: StateFlow<String> = _unitPriceInput.asStateFlow()

    private val _quantityInput = MutableStateFlow("1")
    val quantityInput: StateFlow<String> = _quantityInput.asStateFlow()

    // Errors
    private val _budgetError = MutableStateFlow<String?>(null)
    val budgetError: StateFlow<String?> = _budgetError.asStateFlow()

    private val _quoteError = MutableStateFlow<String?>(null)
    val quoteError: StateFlow<String?> = _quoteError.asStateFlow()

    private val _unitPriceError = MutableStateFlow<String?>(null)
    val unitPriceError: StateFlow<String?> = _unitPriceError.asStateFlow()

    private val _quantityError = MutableStateFlow<String?>(null)
    val quantityError: StateFlow<String?> = _quantityError.asStateFlow()

    // Calculation Result
    private val _calculationResult = MutableStateFlow<QuoteResult?>(null)
    val calculationResult: StateFlow<QuoteResult?> = _calculationResult.asStateFlow()

    private var previousStatus: QuoteStatus? = null

    // History UI state
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    // Combined filtered history list
    val historyList: StateFlow<List<SavedCalculation>> = combine(
        historyRepository.historyFlow,
        _historySearchQuery,
        _historyFilter
    ) { allHistory, query, filter ->
        allHistory
            .filter { item ->
                when (filter) {
                    HistoryFilter.ALL -> true
                    HistoryFilter.UNDER_BUDGET -> item.status == QuoteStatus.UNDER_BUDGET
                    HistoryFilter.ON_BUDGET -> item.status == QuoteStatus.ON_BUDGET
                    HistoryFilter.OVER_BUDGET -> item.status == QuoteStatus.OVER_BUDGET
                }
            }
            .filter { item ->
                if (query.isBlank()) true
                else {
                    item.projectName.contains(query, ignoreCase = true) ||
                            item.notes.contains(query, ignoreCase = true)
                }
            }
            .sortedWith(
                compareByDescending<SavedCalculation> { it.isFavorite }
                    .thenByDescending { it.timestamp }
            )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Events (Snackbar, Haptics, Navigation)
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _hapticEvent = MutableSharedFlow<HapticFeedbackType>()
    val hapticEvent: SharedFlow<HapticFeedbackType> = _hapticEvent.asSharedFlow()

    private var isInitialized = false

    init {
        viewModelScope.launch {
            settings.collect { currentSettings ->
                if (!isInitialized) {
                    _currency.value = currentSettings.activeCurrency
                    _comparisonMode.value = currentSettings.defaultMode
                    _targetBudgetInput.value = currentSettings.defaultBudget
                    _quotedPriceInput.value = currentSettings.defaultQuote
                    isInitialized = true
                    recalculate()
                }
            }
        }
    }

    fun onProjectNameChange(name: String) {
        _projectName.value = name
    }

    fun onNotesChange(notesText: String) {
        _notes.value = notesText
    }

    fun onCurrencyChange(newCurrency: String) {
        _currency.value = newCurrency
    }

    fun onComparisonModeChange(mode: ComparisonMode) {
        _comparisonMode.value = mode
        recalculate()
    }

    fun onTargetBudgetChange(input: String) {
        _targetBudgetInput.value = input
        _budgetError.value = QuoteGapCalculator.validateBudget(input)
        recalculate()
    }

    fun onQuotedPriceChange(input: String) {
        _quotedPriceInput.value = input
        _quoteError.value = QuoteGapCalculator.validateQuotedPrice(input)
        recalculate()
    }

    fun onUnitPriceChange(input: String) {
        _unitPriceInput.value = input
        _unitPriceError.value = QuoteGapCalculator.validateUnitPrice(input)
        recalculate()
    }

    fun onQuantityChange(input: String) {
        _quantityInput.value = input
        _quantityError.value = QuoteGapCalculator.validateQuantity(input)
        recalculate()
    }

    fun applyExample(budget: String, quote: String) {
        _comparisonMode.value = ComparisonMode.TOTAL
        _targetBudgetInput.value = budget
        _quotedPriceInput.value = quote
        _budgetError.value = null
        _quoteError.value = null
        recalculate()
    }

    fun resetCalculator() {
        val currentSettings = settings.value
        _projectName.value = ""
        _notes.value = ""
        _currency.value = currentSettings.activeCurrency
        _comparisonMode.value = currentSettings.defaultMode
        _targetBudgetInput.value = currentSettings.defaultBudget
        _quotedPriceInput.value = currentSettings.defaultQuote
        _unitPriceInput.value = ""
        _quantityInput.value = "1"
        _budgetError.value = null
        _quoteError.value = null
        _unitPriceError.value = null
        _quantityError.value = null
        recalculate()
    }

    private fun recalculate() {
        val budgetVal = QuoteGapCalculator.parseBigDecimalOrNull(_targetBudgetInput.value)
        val budgetErr = if (_targetBudgetInput.value.isBlank()) null else QuoteGapCalculator.validateBudget(_targetBudgetInput.value)
        _budgetError.value = budgetErr

        if (budgetVal == null || budgetVal.compareTo(BigDecimal.ZERO) <= 0) {
            _calculationResult.value = null
            return
        }

        when (_comparisonMode.value) {
            ComparisonMode.TOTAL -> {
                val quoteVal = QuoteGapCalculator.parseBigDecimalOrNull(_quotedPriceInput.value)
                val quoteErr = if (_quotedPriceInput.value.isBlank()) null else QuoteGapCalculator.validateQuotedPrice(_quotedPriceInput.value)
                _quoteError.value = quoteErr

                if (quoteVal == null || quoteVal.compareTo(BigDecimal.ZERO) < 0) {
                    _calculationResult.value = null
                    return
                }

                val input = QuoteInput(
                    projectName = _projectName.value,
                    notes = _notes.value,
                    currency = _currency.value,
                    mode = ComparisonMode.TOTAL,
                    targetBudget = budgetVal,
                    quotedPrice = quoteVal
                )
                val result = QuoteGapCalculator.calculateResult(input)
                checkStatusChangeHaptic(result.status)
                _calculationResult.value = result
            }

            ComparisonMode.PER_UNIT -> {
                val unitPriceVal = QuoteGapCalculator.parseBigDecimalOrNull(_unitPriceInput.value)
                val unitErr = if (_unitPriceInput.value.isBlank()) null else QuoteGapCalculator.validateUnitPrice(_unitPriceInput.value)
                _unitPriceError.value = unitErr

                val qtyInt = _quantityInput.value.trim().toIntOrNull()
                val qtyErr = if (_quantityInput.value.isBlank()) null else QuoteGapCalculator.validateQuantity(_quantityInput.value)
                _quantityError.value = qtyErr

                if (unitPriceVal == null || unitPriceVal.compareTo(BigDecimal.ZERO) < 0 || qtyInt == null || qtyInt < 1 || qtyInt > 1_000_000) {
                    _calculationResult.value = null
                    return
                }

                val input = QuoteInput(
                    projectName = _projectName.value,
                    notes = _notes.value,
                    currency = _currency.value,
                    mode = ComparisonMode.PER_UNIT,
                    targetBudget = budgetVal,
                    quotedPrice = BigDecimal.ZERO,
                    unitPrice = unitPriceVal,
                    quantity = qtyInt
                )
                val result = QuoteGapCalculator.calculateResult(input)
                checkStatusChangeHaptic(result.status)
                _calculationResult.value = result
            }
        }
    }

    private fun checkStatusChangeHaptic(newStatus: QuoteStatus) {
        if (previousStatus != null && previousStatus != newStatus) {
            triggerHaptic(HapticFeedbackType.STATUS_CHANGE)
        }
        previousStatus = newStatus
    }

    private fun triggerHaptic(type: HapticFeedbackType) {
        if (settings.value.hapticFeedback) {
            viewModelScope.launch {
                _hapticEvent.emit(type)
            }
        }
    }

    fun saveCalculation() {
        val result = _calculationResult.value ?: run {
            viewModelScope.launch {
                _snackbarEvent.emit("Enter valid budget and quote to save.")
            }
            return
        }

        val name = _projectName.value.ifBlank { "Untitled Quote" }
        val saved = SavedCalculation(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            projectName = name,
            notes = _notes.value,
            currency = _currency.value,
            mode = result.mode,
            targetBudget = result.targetBudget.toPlainString(),
            quotedPrice = when (result.mode) {
                ComparisonMode.TOTAL -> result.quotedTotal.toPlainString()
                ComparisonMode.PER_UNIT -> result.unitPrice?.toPlainString() ?: "0"
            },
            unitPrice = result.unitPrice?.toPlainString(),
            quantity = result.quantity,
            quotedTotal = result.quotedTotal.toPlainString(),
            signedDifference = result.signedDifference.toPlainString(),
            absoluteDifference = result.absoluteDifference.toPlainString(),
            percentageDifference = result.percentageDifference.toPlainString(),
            budgetUsagePercent = result.budgetUsagePercent.toPlainString(),
            status = result.status,
            isFavorite = false
        )

        viewModelScope.launch {
            historyRepository.saveCalculation(saved)
            triggerHaptic(HapticFeedbackType.SAVE)
            _snackbarEvent.emit("Calculation saved to history")
        }
    }

    fun restoreCalculation(saved: SavedCalculation) {
        _projectName.value = saved.projectName
        _notes.value = saved.notes
        _currency.value = saved.currency
        _comparisonMode.value = saved.mode
        _targetBudgetInput.value = saved.targetBudget

        if (saved.mode == ComparisonMode.TOTAL) {
            _quotedPriceInput.value = saved.quotedPrice
            _unitPriceInput.value = ""
            _quantityInput.value = "1"
        } else {
            _quotedPriceInput.value = ""
            _unitPriceInput.value = saved.unitPrice ?: saved.quotedPrice
            _quantityInput.value = (saved.quantity ?: 1).toString()
        }

        _budgetError.value = null
        _quoteError.value = null
        _unitPriceError.value = null
        _quantityError.value = null

        recalculate()
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(id)
            triggerHaptic(HapticFeedbackType.FAVORITE)
        }
    }

    fun duplicateCalculation(id: String) {
        viewModelScope.launch {
            historyRepository.duplicateCalculation(id)
            triggerHaptic(HapticFeedbackType.SAVE)
            _snackbarEvent.emit("Calculation duplicated")
        }
    }

    fun deleteCalculation(id: String) {
        viewModelScope.launch {
            historyRepository.deleteCalculation(id)
            triggerHaptic(HapticFeedbackType.DELETE)
            _snackbarEvent.emit("Calculation deleted")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            triggerHaptic(HapticFeedbackType.DELETE)
            _snackbarEvent.emit("History cleared")
        }
    }

    fun onHistorySearchChange(query: String) {
        _historySearchQuery.value = query
    }

    fun onHistoryFilterChange(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
            if (_currency.value != newSettings.activeCurrency) {
                _currency.value = newSettings.activeCurrency
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetSettings()
            _snackbarEvent.emit("Settings reset to default")
        }
    }

    fun buildCopyShareText(): String {
        val result = _calculationResult.value ?: return ""
        val decimals = settings.value.decimalPlaces
        val currencySymbol = _currency.value
        val label = settings.value.comparisonLabel.label
        val name = _projectName.value.ifBlank { "Untitled Quote" }

        val statusText = when (result.status) {
            QuoteStatus.UNDER_BUDGET -> "under budget"
            QuoteStatus.ON_BUDGET -> "on budget"
            QuoteStatus.OVER_BUDGET -> "over budget"
        }

        val sb = StringBuilder()
        sb.appendLine("QuoteGap")
        sb.appendLine("Project: $name")
        sb.appendLine()
        sb.appendLine("$label: ${MoneyFormatter.formatMoney(result.targetBudget, currencySymbol, decimals)}")

        if (result.mode == ComparisonMode.PER_UNIT && result.unitPrice != null && result.quantity != null) {
            sb.appendLine("Unit Price: ${MoneyFormatter.formatMoney(result.unitPrice, currencySymbol, decimals)}")
            sb.appendLine("Quantity: ${result.quantity}")
            sb.appendLine("Quoted Total: ${MoneyFormatter.formatMoney(result.quotedTotal, currencySymbol, decimals)}")
        } else {
            sb.appendLine("Quoted Price: ${MoneyFormatter.formatMoney(result.quotedTotal, currencySymbol, decimals)}")
        }

        sb.appendLine()
        if (result.status == QuoteStatus.ON_BUDGET) {
            sb.appendLine("Difference: 0 difference")
            sb.appendLine("Variance: Matches target budget")
        } else {
            sb.appendLine("Difference: ${MoneyFormatter.formatMoney(result.absoluteDifference, currencySymbol, decimals)} $statusText")
            sb.appendLine("Variance: ${MoneyFormatter.formatPercentage(result.percentageDifference, decimals)}")
        }

        sb.appendLine("Budget Used: ${MoneyFormatter.formatPercentage(result.budgetUsagePercent, decimals)}")
        sb.appendLine()
        sb.appendLine("Status: ${result.status.displayName}")

        if (_notes.value.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("Notes: ${_notes.value}")
        }

        return sb.toString().trim()
    }
}
