package com.example.sentencecount.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentencecount.model.Sentence
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceDetailsBottomSheet(
    sentence: Sentence,
    totalWords: Int,
    isShortest: Boolean,
    isLongest: Boolean,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENTENCE DETAILS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sentence #${sentence.index}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${sentence.text}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metrics table
            val pctOfTotal = if (totalWords > 0) {
                String.format(Locale.US, "%.2f%%", (sentence.wordCount.toDouble() / totalWords) * 100.0)
            } else "0.00%"

            val avgCharsPerWord = if (sentence.wordCount > 0) {
                String.format(Locale.US, "%.2f", sentence.characterCount.toDouble() / sentence.wordCount)
            } else "0.00"

            DetailItemRow(label = "Words", value = "${sentence.wordCount}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItemRow(label = "Characters", value = "${sentence.characterCount}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItemRow(label = "Percentage of Total Words", value = pctOfTotal)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItemRow(label = "Avg Characters / Word", value = avgCharsPerWord)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItemRow(label = "Shortest?", value = if (isShortest) "Yes (tied)" else "No")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItemRow(label = "Longest?", value = if (isLongest) "Yes (tied)" else "No")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItemRow(
                label = "Offsets (Source Text)",
                value = "[${sentence.startOffset} .. ${sentence.endOffset}]"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(sentence.text))
                    onCopied()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.height(0.dp))
                Text(text = "  Copy Sentence", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
