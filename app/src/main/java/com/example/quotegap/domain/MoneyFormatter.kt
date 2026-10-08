package com.example.quotegap.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyFormatter {

    fun formatMoney(
        amount: BigDecimal?,
        currency: String = "PKR",
        decimalPlaces: Int = 2
    ): String {
        if (amount == null) return "$currency 0"
        val formattedNumber = formatNumber(amount, decimalPlaces)
        val cleanCurrency = currency.trim()
        return when (cleanCurrency) {
            "$", "€", "£", "¥", "₹" -> "$cleanCurrency$formattedNumber"
            else -> "$cleanCurrency $formattedNumber"
        }
    }

    fun formatPercentage(
        percent: BigDecimal?,
        decimalPlaces: Int = 2,
        includeSymbol: Boolean = true
    ): String {
        if (percent == null) return if (includeSymbol) "0%" else "0"
        val formattedNumber = formatNumber(percent, decimalPlaces)
        return if (includeSymbol) "$formattedNumber%" else formattedNumber
    }

    fun formatNumber(
        value: BigDecimal?,
        decimalPlaces: Int = 2
    ): String {
        if (value == null) return "0"
        val safeDecimals = decimalPlaces.coerceIn(0, 4)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        val rounded = value.setScale(safeDecimals, RoundingMode.HALF_UP)
        val pattern = if (safeDecimals == 0) {
            "#,##0"
        } else {
            "#,##0." + "#".repeat(safeDecimals)
        }

        val df = DecimalFormat(pattern, symbols).apply {
            isGroupingUsed = true
            groupingSize = 3
            maximumFractionDigits = safeDecimals
            minimumFractionDigits = 0
        }

        return df.format(rounded)
    }

    fun formatNumberFixedDecimals(
        value: BigDecimal?,
        decimalPlaces: Int = 2
    ): String {
        if (value == null) return "0"
        val safeDecimals = decimalPlaces.coerceIn(0, 4)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        val rounded = value.setScale(safeDecimals, RoundingMode.HALF_UP)
        val pattern = if (safeDecimals == 0) {
            "#,##0"
        } else {
            "#,##0." + "0".repeat(safeDecimals)
        }

        val df = DecimalFormat(pattern, symbols).apply {
            isGroupingUsed = true
            groupingSize = 3
            maximumFractionDigits = safeDecimals
            minimumFractionDigits = safeDecimals
        }

        return df.format(rounded)
    }
}
