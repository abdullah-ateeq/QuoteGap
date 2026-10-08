package com.example.quotegap.data.model

import java.math.BigDecimal

data class QuoteResult(
    val targetBudget: BigDecimal,
    val quotedTotal: BigDecimal,
    val signedDifference: BigDecimal,
    val absoluteDifference: BigDecimal,
    val percentageDifference: BigDecimal,
    val budgetUsagePercent: BigDecimal,
    val status: QuoteStatus,
    val unitPrice: BigDecimal? = null,
    val quantity: Int? = null,
    val mode: ComparisonMode = ComparisonMode.TOTAL
)
