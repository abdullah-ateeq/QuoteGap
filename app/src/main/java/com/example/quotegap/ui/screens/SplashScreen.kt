package com.example.quotegap.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val budgetLineProgress = remember { Animatable(0f) }
    val quoteLineProgress = remember { Animatable(0f) }
    val diffIndicatorAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animation timeline: 1.3 - 1.4 seconds total
        launch {
            budgetLineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        delay(200)
        launch {
            quoteLineProgress.animateTo(
                targetValue = 0.85f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        delay(400)
        launch {
            diffIndicatorAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }

        delay(1400)
        onSplashFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Animated Graphic: Budget Line vs Quote Line + Gap indicator
                val primaryColor = MaterialTheme.colorScheme.primary
                val tealColor = Color(0xFF0D9488)
                val diffBadgeColor = MaterialTheme.colorScheme.secondary

                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val lineHeight = 12.dp.toPx()
                        val corner = CornerRadius(lineHeight / 2, lineHeight / 2)

                        // Budget Line (100% target baseline)
                        val budgetW = width * budgetLineProgress.value
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.35f),
                            topLeft = Offset(0f, 15.dp.toPx()),
                            size = Size(budgetW, lineHeight),
                            cornerRadius = corner
                        )

                        // Quote Line (92% or 85% quote line)
                        val quoteW = width * quoteLineProgress.value
                        drawRoundRect(
                            color = tealColor,
                            topLeft = Offset(0f, 45.dp.toPx()),
                            size = Size(quoteW, lineHeight),
                            cornerRadius = corner
                        )

                        // Gap marker at quote endpoint
                        if (quoteLineProgress.value > 0.4f) {
                            val markerX = quoteW
                            val markerTop = 15.dp.toPx()
                            val markerBottom = 45.dp.toPx() + lineHeight
                            drawLine(
                                color = tealColor.copy(alpha = diffIndicatorAlpha.value),
                                start = Offset(markerX, markerTop),
                                end = Offset(markerX, markerBottom),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }

                    // Diff indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .alpha(diffIndicatorAlpha.value)
                            .background(tealColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-15%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tealColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Title and Subtitle fading in
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(titleAlpha.value)
                ) {
                    Text(
                        text = "QUOTEGAP",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Compare quotes with your budget",
                        style = MaterialTheme.typography.bodyMedium,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
