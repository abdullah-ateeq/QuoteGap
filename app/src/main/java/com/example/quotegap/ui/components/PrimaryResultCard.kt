package com.example.quotegap.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.data.model.QuoteResult
import com.example.quotegap.data.model.QuoteStatus
import com.example.quotegap.domain.MoneyFormatter
import com.example.quotegap.ui.theme.getStatusColors

@Composable
fun PrimaryResultCard(
    result: QuoteResult,
    currency: String,
    targetLabel: String = "Target Budget",
    decimalPlaces: Int = 2,
    showPercentage: Boolean = true,
    modifier: Modifier = Modifier
) {
    val statusColors = getStatusColors(result.status)
    val formattedDiff = MoneyFormatter.formatMoney(result.absoluteDifference, currency, decimalPlaces)
    val formattedTarget = MoneyFormatter.formatMoney(result.targetBudget, currency, decimalPlaces)
    val formattedQuote = MoneyFormatter.formatMoney(result.quotedTotal, currency, decimalPlaces)
    val formattedPercent = MoneyFormatter.formatPercentage(result.percentageDifference, decimalPlaces)
    val formattedUsage = MoneyFormatter.formatPercentage(result.budgetUsagePercent, decimalPlaces)

    val varianceSubtitle = when (result.status) {
        QuoteStatus.UNDER_BUDGET -> "$formattedPercent below target"
        QuoteStatus.ON_BUDGET -> "Matches target budget"
        QuoteStatus.OVER_BUDGET -> "$formattedPercent above target"
    }

    val quickSummarySentence = when (result.status) {
        QuoteStatus.UNDER_BUDGET -> "Your quote is $formattedDiff below the target budget."
        QuoteStatus.ON_BUDGET -> "Your quote matches the target budget."
        QuoteStatus.OVER_BUDGET -> "Your quote is $formattedDiff above the target budget."
    }

    val accessibilityDesc = "${result.status.displayName}, $formattedDiff difference, $varianceSubtitle, Budget used $formattedUsage"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDesc },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColors.statusBackground
        ),
        border = BorderStroke(1.5.dp, statusColors.statusBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Dominant Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColors.statusText.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusIcon = when (result.status) {
                            QuoteStatus.UNDER_BUDGET -> Icons.Default.CheckCircle
                            QuoteStatus.ON_BUDGET -> Icons.Default.Info
                            QuoteStatus.OVER_BUDGET -> Icons.Default.Warning
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColors.statusText,
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = result.status.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = statusColors.statusText
                        )
                    }
                }

                // Budget Usage Pill
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "BUDGET USED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedUsage,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColors.statusText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Difference Amount
            Text(
                text = "DIFFERENCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (result.status == QuoteStatus.ON_BUDGET) "0 difference" else formattedDiff,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = statusColors.statusText
            )

            if (showPercentage) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = varianceSubtitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = statusColors.statusBorder.copy(alpha = 0.6f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Unit quote details if Per-Unit mode
            if (result.mode == ComparisonMode.PER_UNIT && result.unitPrice != null && result.quantity != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "UNIT PRICE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = MoneyFormatter.formatMoney(result.unitPrice, currency, decimalPlaces),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "QUANTITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = result.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Budget Comparison Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = targetLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedTarget,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "QUOTED TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedQuote,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic quick summary statement
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = quickSummarySentence,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
