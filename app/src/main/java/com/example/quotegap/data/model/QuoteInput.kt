package com.example.quotegap.data.model

import java.math.BigDecimal

data class QuoteInput(
    val projectName: String = "",
    val notes: String = "",
    val currency: String = "PKR",
    val mode: ComparisonMode = ComparisonMode.TOTAL,
    val targetBudget: BigDecimal = BigDecimal.ZERO,
    val quotedPrice: BigDecimal = BigDecimal.ZERO,
    val unitPrice: BigDecimal? = null,
    val quantity: Int? = null
)
