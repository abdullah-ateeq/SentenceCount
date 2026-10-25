package com.example.sentencecount.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentencecount.model.SentenceLengthDistribution

@Composable
fun SentenceDistributionChart(
    distribution: List<SentenceLengthDistribution>,
    lengthUnitLabel: String,
    modifier: Modifier = Modifier
) {
    if (distribution.isEmpty()) return

    val maxCount = distribution.maxOfOrNull { it.sentenceCount } ?: 1
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
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
                    text = "SENTENCE LENGTH HISTOGRAM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Max: $maxCount sent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 8.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val leftPadding = 36f
                val bottomPadding = 32f
                val topPadding = 16f
                val rightPadding = 16f

                val chartWidth = canvasWidth - leftPadding - rightPadding
                val chartHeight = canvasHeight - bottomPadding - topPadding

                // Draw Y-axis reference lines (0, 50%, 100%)
                val yLevels = listOf(0f, 0.5f, 1f)
                for (lvl in yLevels) {
                    val yPos = topPadding + chartHeight * (1f - lvl)
                    drawLine(
                        color = outlineColor.copy(alpha = 0.6f),
                        start = Offset(leftPadding, yPos),
                        end = Offset(canvasWidth - rightPadding, yPos),
                        strokeWidth = 1f
                    )

                    val labelVal = (maxCount * lvl).toInt().toString()
                    val textLayout = textMeasurer.measure(
                        text = labelVal,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = textColor,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            leftPadding - textLayout.size.width - 6f,
                            yPos - textLayout.size.height / 2f
                        )
                    )
                }

                // Draw Bars
                val barCount = distribution.size
                val slotWidth = chartWidth / barCount
                val barWidth = (slotWidth * 0.65f).coerceIn(8f, 32f)

                distribution.forEachIndexed { index, item ->
                    val barHeight = (item.sentenceCount.toFloat() / maxCount) * chartHeight
                    val barX = leftPadding + (index * slotWidth) + (slotWidth - barWidth) / 2f
                    val barY = topPadding + chartHeight - barHeight

                    // Draw bar
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(barX, barY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    // Draw X label (Length)
                    val xLabel = item.length.toString()
                    val xLayout = textMeasurer.measure(
                        text = xLabel,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = xLayout,
                        topLeft = Offset(
                            barX + (barWidth - xLayout.size.width) / 2f,
                            canvasHeight - bottomPadding + 6f
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Length in $lengthUnitLabel →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
