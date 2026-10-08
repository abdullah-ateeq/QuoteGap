package com.example.quotegap.data.model

enum class ComparisonLabel(val label: String) {
    TARGET_BUDGET("Target Budget"),
    TARGET("Target"),
    LIMIT("Limit"),
    MAXIMUM("Maximum");

    override fun toString(): String = label
}
