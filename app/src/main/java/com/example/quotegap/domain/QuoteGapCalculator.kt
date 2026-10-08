package com.example.quotegap.domain

import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.data.model.QuoteInput
import com.example.quotegap.data.model.QuoteResult
import com.example.quotegap.data.model.QuoteStatus
import java.math.BigDecimal
import java.math.RoundingMode

object QuoteGapCalculator {

    private const val CALCULATION_SCALE = 12

    fun calculateTotalQuote(quotedPrice: BigDecimal): BigDecimal = quotedPrice

    fun calculateQuotedTotal(quotedPrice: BigDecimal): BigDecimal = quotedPrice

    fun calculateTotalQuote(unitPrice: BigDecimal, quantity: Int): BigDecimal {
        return unitPrice.multiply(BigDecimal(quantity))
    }

    fun calculateQuotedTotal(unitPrice: BigDecimal, quantity: Int): BigDecimal {
        return unitPrice.multiply(BigDecimal(quantity))
    }

    fun calculateDifference(quotedTotal: BigDecimal, targetBudget: BigDecimal): BigDecimal {
        return quotedTotal.subtract(targetBudget)
    }

    fun calculateAbsoluteDifference(signedDifference: BigDecimal): BigDecimal {
        return signedDifference.abs()
    }

    fun calculatePercentageDifference(
        absoluteDifference: BigDecimal,
        targetBudget: BigDecimal
    ): BigDecimal {
        if (targetBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        return absoluteDifference
            .divide(targetBudget, CALCULATION_SCALE, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
    }

    fun calculateBudgetUsage(
        quotedTotal: BigDecimal,
        targetBudget: BigDecimal
    ): BigDecimal {
        if (targetBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        return quotedTotal
            .divide(targetBudget, CALCULATION_SCALE, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
    }

    fun determineStatus(signedDifference: BigDecimal): QuoteStatus {
        val comparison = signedDifference.compareTo(BigDecimal.ZERO)
        return when {
            comparison < 0 -> QuoteStatus.UNDER_BUDGET
            comparison == 0 -> QuoteStatus.ON_BUDGET
            else -> QuoteStatus.OVER_BUDGET
        }
    }

    fun calculateResult(input: QuoteInput): QuoteResult {
        val quotedTotal = when (input.mode) {
            ComparisonMode.TOTAL -> calculateQuotedTotal(input.quotedPrice)
            ComparisonMode.PER_UNIT -> {
                val unitPrice = input.unitPrice ?: BigDecimal.ZERO
                val quantity = input.quantity ?: 1
                calculateQuotedTotal(unitPrice, quantity)
            }
        }

        val signedDifference = calculateDifference(quotedTotal, input.targetBudget)
        val absoluteDifference = calculateAbsoluteDifference(signedDifference)
        val percentageDifference = calculatePercentageDifference(absoluteDifference, input.targetBudget)
        val budgetUsagePercent = calculateBudgetUsage(quotedTotal, input.targetBudget)
        val status = determineStatus(signedDifference)

        return QuoteResult(
            targetBudget = input.targetBudget,
            quotedTotal = quotedTotal,
            signedDifference = signedDifference,
            absoluteDifference = absoluteDifference,
            percentageDifference = percentageDifference,
            budgetUsagePercent = budgetUsagePercent,
            status = status,
            unitPrice = input.unitPrice,
            quantity = input.quantity,
            mode = input.mode
        )
    }

    fun validateBudget(budgetStr: String): String? {
        val trimmed = budgetStr.trim()
        if (trimmed.isEmpty()) return null // Incomplete entry while typing
        val value = parseBigDecimalOrNull(trimmed) ?: return "Budget must be a valid number."
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return "Budget must be greater than zero."
        }
        return null
    }

    fun validateQuotedPrice(quoteStr: String): String? {
        val trimmed = quoteStr.trim()
        if (trimmed.isEmpty()) return null
        val value = parseBigDecimalOrNull(trimmed) ?: return "Quoted price must be a valid number."
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return "Quoted price cannot be negative."
        }
        return null
    }

    fun validateUnitPrice(unitPriceStr: String): String? {
        val trimmed = unitPriceStr.trim()
        if (trimmed.isEmpty()) return null
        val value = parseBigDecimalOrNull(trimmed) ?: return "Unit price must be a valid number."
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return "Unit price cannot be negative."
        }
        return null
    }

    fun validateQuantity(quantityStr: String): String? {
        val trimmed = quantityStr.trim()
        if (trimmed.isEmpty()) return null
        val intValue = trimmed.toIntOrNull()
        if (intValue == null || intValue < 1 || intValue > 1_000_000) {
            return "Quantity must be a whole number from 1 to 1,000,000."
        }
        return null
    }

    fun parseBigDecimalOrNull(raw: String): BigDecimal? {
        val cleaned = raw.replace(",", "").trim()
        if (cleaned.isEmpty()) return null
        return try {
            BigDecimal(cleaned)
        } catch (_: Exception) {
            null
        }
    }
}
