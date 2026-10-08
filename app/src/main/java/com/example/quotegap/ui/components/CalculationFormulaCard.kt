package com.example.quotegap.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.data.model.QuoteResult
import com.example.quotegap.data.model.QuoteStatus
import com.example.quotegap.domain.MoneyFormatter

@Composable
fun CalculationFormulaCard(
    result: QuoteResult,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedBudget = MoneyFormatter.formatNumber(result.targetBudget, decimalPlaces)
    val formattedQuote = MoneyFormatter.formatNumber(result.quotedTotal, decimalPlaces)
    val formattedSignedDiff = MoneyFormatter.formatNumber(result.signedDifference, decimalPlaces)
    val formattedAbsDiff = MoneyFormatter.formatNumber(result.absoluteDifference, decimalPlaces)
    val formattedPercent = MoneyFormatter.formatNumber(result.percentageDifference, decimalPlaces)
    val formattedUsage = MoneyFormatter.formatNumber(result.budgetUsagePercent, decimalPlaces)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOW IT WAS CALCULATED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse formula" else "Expand formula",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (result.mode == ComparisonMode.PER_UNIT && result.unitPrice != null && result.quantity != null) {
                        val formattedUnit = MoneyFormatter.formatNumber(result.unitPrice, decimalPlaces)
                        FormulaStep(
                            label = "Quoted total",
                            formula = "$formattedUnit × ${result.quantity} = $formattedQuote"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 1. Signed Difference
                    FormulaStep(
                        label = "Difference",
                        formula = "$formattedQuote − $formattedBudget = $formattedSignedDiff"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Status
                    FormulaStep(
                        label = "Status",
                        formula = result.status.displayName
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Absolute Difference
                    FormulaStep(
                        label = "Absolute difference",
                        formula = formattedAbsDiff
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Percentage Difference
                    if (result.status == QuoteStatus.ON_BUDGET) {
                        FormulaStep(
                            label = "Percentage variance",
                            formula = "Matches target budget"
                        )
                    } else {
                        val direction = if (result.status == QuoteStatus.UNDER_BUDGET) "below target" else "above target"
                        FormulaStep(
                            label = "Percentage difference",
                            formula = "$formattedAbsDiff ÷ $formattedBudget × 100 = $formattedPercent% ($direction)"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Budget usage
                    FormulaStep(
                        label = "Budget usage",
                        formula = "$formattedQuote ÷ $formattedBudget × 100 = $formattedUsage%"
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaStep(
    label: String,
    formula: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formula,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
