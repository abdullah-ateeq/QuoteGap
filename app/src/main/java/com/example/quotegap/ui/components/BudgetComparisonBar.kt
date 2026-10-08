package com.example.quotegap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotegap.data.model.QuoteResult
import com.example.quotegap.data.model.QuoteStatus
import com.example.quotegap.domain.MoneyFormatter
import com.example.quotegap.ui.theme.getStatusColors
import java.math.BigDecimal

@Composable
fun BudgetComparisonBar(
    result: QuoteResult,
    targetLabel: String = "Target Budget",
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val statusColors = getStatusColors(result.status)
    val quotePercentFloat = result.budgetUsagePercent.toFloat().coerceAtLeast(0f)
    val targetPercentFloat = 100f

    // Dynamic scale so if quote is > 100%, it doesn't clip
    val maxScale = maxOf(120f, quotePercentFloat * 1.15f)

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VISUAL COMPARISON",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${MoneyFormatter.formatPercentage(result.budgetUsagePercent, decimalPlaces)} used",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColors.statusText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comparison labels and values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .padding(end = 6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Text(
                        text = targetLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Chart Canvas
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            val targetBarColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            val quoteBarColor = statusColors.statusText
            val markerLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val width = size.width
                val barHeight = 14.dp.toPx()
                val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                val targetX = (targetPercentFloat / maxScale) * width
                val quoteX = (quotePercentFloat / maxScale).coerceAtMost(1f) * width

                // Row 1: Target Bar
                val yTarget = 8.dp.toPx()
                // Track
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(0f, yTarget),
                    size = Size(width, barHeight),
                    cornerRadius = cornerRadius
                )
                // Target Fill
                drawRoundRect(
                    color = targetBarColor,
                    topLeft = Offset(0f, yTarget),
                    size = Size(targetX, barHeight),
                    cornerRadius = cornerRadius
                )

                // Row 2: Quote Bar
                val yQuote = 36.dp.toPx()
                // Track
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(0f, yQuote),
                    size = Size(width, barHeight),
                    cornerRadius = cornerRadius
                )
                // Quote Fill
                if (quoteX > 0f) {
                    drawRoundRect(
                        color = quoteBarColor,
                        topLeft = Offset(0f, yQuote),
                        size = Size(quoteX, barHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // Vertical 100% Target Guide Line
                drawLine(
                    color = markerLineColor,
                    start = Offset(targetX, 0f),
                    end = Offset(targetX, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .padding(end = 6.dp)
                            .background(
                                color = statusColors.statusText,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Text(
                        text = "Quoted Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = MoneyFormatter.formatPercentage(result.budgetUsagePercent, decimalPlaces),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColors.statusText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom explanation
            Text(
                text = when (result.status) {
                    QuoteStatus.UNDER_BUDGET -> "Quote is within target budget limits (Target marker at 100%)."
                    QuoteStatus.ON_BUDGET -> "Quote exactly matches the target budget line."
                    QuoteStatus.OVER_BUDGET -> "Quote exceeds target budget marker by ${(quotePercentFloat - 100f).toInt()}%."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
