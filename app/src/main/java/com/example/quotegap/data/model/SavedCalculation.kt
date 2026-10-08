package com.example.quotegap.data.model

import java.util.UUID

data class SavedCalculation(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val projectName: String = "Untitled Quote",
    val notes: String = "",
    val currency: String = "PKR",
    val mode: ComparisonMode = ComparisonMode.TOTAL,
    val targetBudget: String = "0",
    val quotedPrice: String = "0",
    val unitPrice: String? = null,
    val quantity: Int? = null,
    val quotedTotal: String = "0",
    val signedDifference: String = "0",
    val absoluteDifference: String = "0",
    val percentageDifference: String = "0",
    val budgetUsagePercent: String = "0",
    val status: QuoteStatus = QuoteStatus.ON_BUDGET,
    val isFavorite: Boolean = false
)
