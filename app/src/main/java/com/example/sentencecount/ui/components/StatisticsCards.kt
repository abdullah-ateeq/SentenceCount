package com.example.sentencecount.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentencecount.model.DisplaySettings
import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.SentenceCountUiState
import com.example.sentencecount.model.SentenceLengthDistribution
import com.example.sentencecount.ui.theme.GroupLongColor
import com.example.sentencecount.ui.theme.GroupMediumColor
import com.example.sentencecount.ui.theme.GroupShortColor
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PrimaryStatisticsGrid(
    state: SentenceCountUiState,
    lengthSettings: LengthSettings,
    displaySettings: DisplaySettings,
    modifier: Modifier = Modifier
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val lengthUnit = lengthSettings.lengthUnit.label.lowercase(Locale.ROOT)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1: Total Sentences & Total Words
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "TOTAL SENTENCES",
                value = formatter.format(state.totalSentences),
                subtitle = "detected segments",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "TOTAL WORDS",
                value = formatter.format(state.totalWords),
                subtitle = "valid tokens",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Total Characters & Avg Words / Sentence
        val charSubtitle = if (lengthSettings.includeSpaces) "including spaces" else "excluding spaces"
        val avgWordsStr = String.format(Locale.US, "%.2f", state.averageLength ?: 0.0)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "TOTAL CHARACTERS",
                value = formatter.format(state.totalCharacters),
                subtitle = charSubtitle,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "AVERAGE LENGTH",
                value = avgWordsStr,
                subtitle = "$lengthUnit / sentence",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Shortest & Longest
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "SHORTEST SENTENCE",
                value = "${state.shortestLength ?: 0}",
                subtitle = "$lengthUnit (min)",
                accentColor = GroupShortColor,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "LONGEST SENTENCE",
                value = "${state.longestLength ?: 0}",
                subtitle = "$lengthUnit (max)",
                accentColor = GroupLongColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Median & Mode (if enabled)
        if (displaySettings.showMedian || displaySettings.showMode) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (displaySettings.showMedian) {
                    val medianFormatted = if (state.medianLength != null) {
                        if (state.medianLength % 1.0 == 0.0) state.medianLength.toInt().toString()
                        else String.format(Locale.US, "%.1f", state.medianLength)
                    } else "0"

                    StatCard(
                        title = "MEDIAN LENGTH",
                        value = medianFormatted,
                        subtitle = "$lengthUnit (50th %ile)",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (displaySettings.showMode) {
                    val modeValue = if (state.modeLengths.isNotEmpty()) {
                        state.modeLengths.joinToString(", ")
                    } else "N/A"

                    val modeCount = state.distribution.firstOrNull { state.modeLengths.contains(it.length) }?.sentenceCount ?: 0
                    val modeSubtitle = if (modeCount > 0) "$modeCount sentences" else "most common"

                    StatCard(
                        title = "MOST COMMON (MODE)",
                        value = modeValue,
                        subtitle = modeSubtitle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Row 5: Range & Avg Chars per Sentence
        val avgCharsStr = String.format(Locale.US, "%.2f", state.averageCharactersPerSentence ?: 0.0)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "LENGTH RANGE",
                value = "${state.range ?: 0}",
                subtitle = "${state.shortestLength ?: 0} – ${state.longestLength ?: 0} $lengthUnit",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "AVG CHARS / SENTENCE",
                value = avgCharsStr,
                subtitle = "characters",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = accentColor ?: MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VariabilityCard(
    state: SentenceCountUiState,
    lengthSettings: LengthSettings,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")
    val lengthUnit = lengthSettings.lengthUnit.label.lowercase(Locale.ROOT)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENTENCE LENGTH VARIABILITY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Population standard deviation: ${String.format(Locale.US, "%.2f", state.standardDeviation ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    VariabilityRow(label = "Mean (Average)", value = "${String.format(Locale.US, "%.2f", state.averageLength ?: 0.0)} $lengthUnit")
                    VariabilityRow(label = "Minimum Length", value = "${state.shortestLength ?: 0} $lengthUnit")
                    VariabilityRow(label = "Maximum Length", value = "${state.longestLength ?: 0} $lengthUnit")
                    VariabilityRow(label = "Range", value = "${state.range ?: 0} $lengthUnit")
                    VariabilityRow(
                        label = "Std. Deviation (σ)",
                        value = String.format(Locale.US, "%.2f", state.standardDeviation ?: 0.0)
                    )
                }
            }
        }
    }
}

@Composable
private fun VariabilityRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ShortMediumLongCard(
    state: SentenceCountUiState,
    displaySettings: DisplaySettings,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "SENTENCE LENGTH CATEGORIES",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                if (state.shortPercentage > 0) {
                    Box(
                        modifier = Modifier
                            .weight(state.shortPercentage.toFloat().coerceAtLeast(0.01f))
                            .height(8.dp)
                            .background(GroupShortColor)
                    )
                }
                if (state.mediumPercentage > 0) {
                    Box(
                        modifier = Modifier
                            .weight(state.mediumPercentage.toFloat().coerceAtLeast(0.01f))
                            .height(8.dp)
                            .background(GroupMediumColor)
                    )
                }
                if (state.longPercentage > 0) {
                    Box(
                        modifier = Modifier
                            .weight(state.longPercentage.toFloat().coerceAtLeast(0.01f))
                            .height(8.dp)
                            .background(GroupLongColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CategoryPill(
                    label = "SHORT (1–${displaySettings.shortThreshold})",
                    count = state.shortCount,
                    pct = state.shortPercentage,
                    color = GroupShortColor
                )
                CategoryPill(
                    label = "MEDIUM (${displaySettings.shortThreshold + 1}–${displaySettings.mediumThreshold})",
                    count = state.mediumCount,
                    pct = state.mediumPercentage,
                    color = GroupMediumColor
                )
                CategoryPill(
                    label = "LONG (${displaySettings.mediumThreshold + 1}+)",
                    count = state.longCount,
                    pct = state.longPercentage,
                    color = GroupLongColor
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String, count: Int, pct: Double, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$count sent. (${String.format(Locale.US, "%.1f%%", pct)})",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LengthDistributionTable(
    distribution: List<SentenceLengthDistribution>,
    lengthUnitLabel: String,
    modifier: Modifier = Modifier
) {
    if (distribution.isEmpty()) return

    val maxSentences = distribution.maxOfOrNull { it.sentenceCount } ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENTENCE LENGTH DISTRIBUTION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${distribution.size} distinct lengths",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            for (item in distribution) {
                val fraction = (item.sentenceCount.toFloat() / maxSentences).coerceIn(0.05f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.length} $lengthUnitLabel",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.width(72.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Horizontal visual bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${item.sentenceCount} sent.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = String.format(Locale.US, "(%.1f%%)", item.percentage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
