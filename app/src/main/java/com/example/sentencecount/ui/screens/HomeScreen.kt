package com.example.sentencecount.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentencecount.model.LengthSettings
import com.example.sentencecount.model.Sentence
import com.example.sentencecount.model.SentenceSortOrder
import com.example.sentencecount.ui.SentenceCountViewModel
import com.example.sentencecount.ui.components.LengthDistributionTable
import com.example.sentencecount.ui.components.PrimaryStatisticsGrid
import com.example.sentencecount.ui.components.SampleTexts
import com.example.sentencecount.ui.components.SentenceDetailsBottomSheet
import com.example.sentencecount.ui.components.SentenceDistributionChart
import com.example.sentencecount.ui.components.SentenceRow
import com.example.sentencecount.ui.components.ShortMediumLongCard
import com.example.sentencecount.ui.components.VariabilityCard
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SentenceCountViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.uiState.collectAsState()
    val lengthSettings by viewModel.lengthSettings.collectAsState()
    val displaySettings by viewModel.displaySettings.collectAsState()
    val behaviorSettings by viewModel.behaviorSettings.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    var selectedSentenceForDetails by remember { mutableStateOf<Sentence?>(null) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSampleMenu by remember { mutableStateOf(false) }

    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SentenceCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Count sentences and analyze sentence lengths.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TEXT INPUT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Sample texts button
                                Box {
                                    TextButton(onClick = { showSampleMenu = true }) {
                                        Text(text = "Samples", style = MaterialTheme.typography.labelMedium)
                                    }
                                    DropdownMenu(
                                        expanded = showSampleMenu,
                                        onDismissRequest = { showSampleMenu = false }
                                    ) {
                                        SampleTexts.SAMPLES.forEach { sample ->
                                            DropdownMenuItem(
                                                text = { Text(sample.name) },
                                                onClick = {
                                                    viewModel.onTextChanged(sample.text)
                                                    showSampleMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Paste button
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrEmpty()) {
                                        viewModel.onTextChanged(clip)
                                    }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Clear button
                                if (state.text.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearText() }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.text,
                            onValueChange = { viewModel.onTextChanged(it) },
                            placeholder = {
                                Text(
                                    text = "Paste or type your text here...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live text statistics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiveStatBadge(label = "Characters", value = formatter.format(state.totalCharacters))
                            LiveStatBadge(label = "Words", value = formatter.format(state.totalWords))
                            LiveStatBadge(label = "Sentences", value = formatter.format(state.totalSentences))
                            LiveStatBadge(label = "Lines", value = formatter.format(state.totalLines))
                        }
                    }
                }
            }

            // Analyze Text Button
            item {
                Button(
                    onClick = { viewModel.analyzeText() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "ANALYZE TEXT",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Empty State
            if (state.text.isBlank() || (state.totalSentences == 0 && state.errorMessage != null)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.errorMessage ?: "Enter some text to analyze.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Analysis Dashboard (when sentences exist)
            if (state.totalSentences > 0) {
                // Header & Action Bar
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SENTENCE ANALYSIS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons: Copy, Save, Share, Export CSV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val summary = viewModel.getSummaryText()
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(summary))
                                    viewModel.showMessage("Summary copied to clipboard")
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(15.dp).width(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Copy", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { viewModel.saveCurrentToHistory() },
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.height(15.dp).width(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Save", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    val summary = viewModel.getSummaryText()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, summary)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Analysis"))
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.height(15.dp).width(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Share", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), maxLines = 1)
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showExportMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.height(15.dp).width(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "CSV", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), maxLines = 1)
                                }

                                DropdownMenu(
                                    expanded = showExportMenu,
                                    onDismissRequest = { showExportMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Export Sentence CSV") },
                                        onClick = {
                                            val csv = viewModel.getSentenceCsvString()
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, csv)
                                                type = "text/csv"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Export Sentence CSV"))
                                            showExportMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Export Distribution CSV") },
                                        onClick = {
                                            val csv = viewModel.getDistributionCsvString()
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, csv)
                                                type = "text/csv"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Export Distribution CSV"))
                                            showExportMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Primary Statistics Grid
                item {
                    PrimaryStatisticsGrid(
                        state = state,
                        lengthSettings = lengthSettings,
                        displaySettings = displaySettings
                    )
                }

                // Sentence Length Variability (Expandable)
                if (displaySettings.showVariability) {
                    item {
                        VariabilityCard(
                            state = state,
                            lengthSettings = lengthSettings
                        )
                    }
                }

                // Sentence Length Categories (Short / Medium / Long)
                if (displaySettings.showShortMediumLong) {
                    item {
                        ShortMediumLongCard(
                            state = state,
                            displaySettings = displaySettings
                        )
                    }
                }

                // Sentence Length Distribution Table
                if (displaySettings.showDistribution && state.distribution.isNotEmpty()) {
                    item {
                        LengthDistributionTable(
                            distribution = state.distribution,
                            lengthUnitLabel = lengthSettings.lengthUnit.label.lowercase(Locale.ROOT)
                        )
                    }
                }

                // Sentence Length Histogram Chart
                if (displaySettings.showChart && state.distribution.isNotEmpty()) {
                    item {
                        SentenceDistributionChart(
                            distribution = state.distribution,
                            lengthUnitLabel = lengthSettings.lengthUnit.label
                        )
                    }
                }

                // Sentences Search & Sort Header
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SENTENCES (${state.sentences.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Sort dropdown
                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.height(16.dp).width(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = state.sortOrder.displayName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SentenceSortOrder.values().forEach { order ->
                                        DropdownMenuItem(
                                            text = { Text(order.displayName) },
                                            onClick = {
                                                viewModel.onSortOrderChanged(order)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search bar
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search sentences by text...", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }

                // Filtered & Sorted Sentences List
                val displaySentences = viewModel.getFilteredAndSortedSentences()
                val maxLen = state.longestLength ?: 1

                items(displaySentences, key = { it.index }) { sentence ->
                    val isShort = sentence.wordCount == state.shortestLength
                    val isLong = sentence.wordCount == state.longestLength

                    SentenceRow(
                        sentence = sentence,
                        totalWords = state.totalWords,
                        maxSentenceLength = maxLen,
                        isShortest = isShort,
                        isLongest = isLong,
                        lengthSettings = lengthSettings,
                        onClick = { selectedSentenceForDetails = sentence }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Details BottomSheet
    selectedSentenceForDetails?.let { sentence ->
        val isShort = sentence.wordCount == state.shortestLength
        val isLong = sentence.wordCount == state.longestLength

        SentenceDetailsBottomSheet(
            sentence = sentence,
            totalWords = state.totalWords,
            isShortest = isShort,
            isLongest = isLong,
            onDismiss = { selectedSentenceForDetails = null },
            onCopied = { viewModel.showMessage("Sentence copied to clipboard") }
        )
    }
}

@Composable
private fun LiveStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
