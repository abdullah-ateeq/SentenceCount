package com.example.sentencecount.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.sentencecount.model.ApostropheMode
import com.example.sentencecount.model.AppTheme
import com.example.sentencecount.model.CharacterUnit
import com.example.sentencecount.model.HyphenMode
import com.example.sentencecount.model.LengthUnit
import com.example.sentencecount.ui.SentenceCountViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SentenceCountViewModel,
    onNavigateBack: () -> Unit
) {
    val detectionSettings by viewModel.detectionSettings.collectAsState()
    val lengthSettings by viewModel.lengthSettings.collectAsState()
    val displaySettings by viewModel.displaySettings.collectAsState()
    val behaviorSettings by viewModel.behaviorSettings.collectAsState()
    val themeSettings by viewModel.themeSettings.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showAddAbbrDialog by remember { mutableStateOf(false) }
    var showAddTermDialog by remember { mutableStateOf(false) }

    var newAbbrText by remember { mutableStateOf("") }
    var newTermText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance
            item {
                SectionHeader("APPEARANCE")
                SettingsCard {
                    Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AppTheme.values().forEach { theme ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.updateThemeSettings(themeSettings.copy(theme = theme))
                                }
                            ) {
                                RadioButton(
                                    selected = themeSettings.theme == theme,
                                    onClick = { viewModel.updateThemeSettings(themeSettings.copy(theme = theme)) }
                                )
                                Text(theme.label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ToggleRow(
                        title = "Dynamic Color",
                        subtitle = "Use system wallpaper colors on Android 12+",
                        checked = themeSettings.dynamicColor,
                        onCheckedChange = { viewModel.updateThemeSettings(themeSettings.copy(dynamicColor = it)) }
                    )
                }
            }

            // Sentence Detection Rules
            item {
                SectionHeader("SENTENCE DETECTION RULES")
                SettingsCard {
                    ToggleRow(
                        title = "Period ( . )",
                        subtitle = "Standard full stop terminator",
                        checked = detectionSettings.enablePeriod,
                        onCheckedChange = { viewModel.updateDetectionSettings(detectionSettings.copy(enablePeriod = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Question Mark ( ? )",
                        subtitle = "Standard interrogative terminator",
                        checked = detectionSettings.enableQuestion,
                        onCheckedChange = { viewModel.updateDetectionSettings(detectionSettings.copy(enableQuestion = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Exclamation Mark ( ! )",
                        subtitle = "Standard exclamatory terminator",
                        checked = detectionSettings.enableExclamation,
                        onCheckedChange = { viewModel.updateDetectionSettings(detectionSettings.copy(enableExclamation = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Unicode Equivalents",
                        subtitle = "Support CJK (。！？), Arabic (؟ ۔), and others",
                        checked = detectionSettings.enableUnicodeEquivalents,
                        onCheckedChange = { viewModel.updateDetectionSettings(detectionSettings.copy(enableUnicodeEquivalents = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Paragraph Breaks as Boundaries",
                        subtitle = "Treat double newline (\\n\\n) as sentence boundary",
                        checked = detectionSettings.paragraphBoundaryMode,
                        onCheckedChange = { viewModel.updateDetectionSettings(detectionSettings.copy(paragraphBoundaryMode = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Count Punctuation-Only Segments",
                        subtitle = "Treat segments with no letters/digits as sentences",
                        checked = detectionSettings.countPunctuationOnly,
                        onCheckedChange = { viewModel.updateDetectionSettings(detectionSettings.copy(countPunctuationOnly = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Custom Abbreviations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Custom Abbreviations", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Prevent splitting after these words", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showAddAbbrDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(14.dp).width(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (detectionSettings.customAbbreviations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            detectionSettings.customAbbreviations.forEach { abbr ->
                                InputChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(abbr) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier
                                                .height(16.dp)
                                                .width(16.dp)
                                                .clickable {
                                                    val updated = detectionSettings.customAbbreviations.filter { it != abbr }
                                                    viewModel.updateDetectionSettings(detectionSettings.copy(customAbbreviations = updated))
                                                }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Custom Terminators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Custom Terminators", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Additional punctuation marks to end sentences", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showAddTermDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(14.dp).width(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (detectionSettings.customTerminators.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            detectionSettings.customTerminators.forEach { term ->
                                InputChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(term) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier
                                                .height(16.dp)
                                                .width(16.dp)
                                                .clickable {
                                                    val updated = detectionSettings.customTerminators.filter { it != term }
                                                    viewModel.updateDetectionSettings(detectionSettings.copy(customTerminators = updated))
                                                }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Length Measure & Tokenization
            item {
                SectionHeader("LENGTH & CHARACTER MEASUREMENT")
                SettingsCard {
                    Text("Primary Length Unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LengthUnit.values().forEach { unit ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.updateLengthSettings(lengthSettings.copy(lengthUnit = unit))
                                }
                            ) {
                                RadioButton(
                                    selected = lengthSettings.lengthUnit == unit,
                                    onClick = { viewModel.updateLengthSettings(lengthSettings.copy(lengthUnit = unit)) }
                                )
                                Text(unit.label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Character Count Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column {
                        CharacterUnit.values().forEach { cUnit ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.updateLengthSettings(lengthSettings.copy(characterUnit = cUnit))
                                }
                            ) {
                                RadioButton(
                                    selected = lengthSettings.characterUnit == cUnit,
                                    onClick = { viewModel.updateLengthSettings(lengthSettings.copy(characterUnit = cUnit)) }
                                )
                                Text(cUnit.label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ToggleRow(
                        title = "Include Spaces in Character Count",
                        subtitle = "Count whitespace towards sentence character length",
                        checked = lengthSettings.includeSpaces,
                        onCheckedChange = { viewModel.updateLengthSettings(lengthSettings.copy(includeSpaces = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Include Punctuation in Character Count",
                        subtitle = "Count terminal and internal punctuation marks",
                        checked = lengthSettings.includePunctuation,
                        onCheckedChange = { viewModel.updateLengthSettings(lengthSettings.copy(includePunctuation = it)) }
                    )
                }
            }

            // Word Tokenizer Settings
            item {
                SectionHeader("WORD TOKENIZATION")
                SettingsCard {
                    ToggleRow(
                        title = "Count Numbers as Words",
                        subtitle = "Include numeric tokens (e.g. '3', '12.50') in word count",
                        checked = lengthSettings.countNumbers,
                        onCheckedChange = { viewModel.updateLengthSettings(lengthSettings.copy(countNumbers = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Hyphen Handling", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HyphenMode.values().forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.updateLengthSettings(lengthSettings.copy(hyphenMode = mode))
                                }
                            ) {
                                RadioButton(
                                    selected = lengthSettings.hyphenMode == mode,
                                    onClick = { viewModel.updateLengthSettings(lengthSettings.copy(hyphenMode = mode)) }
                                )
                                Text(mode.label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ToggleRow(
                        title = "URLs & Emails as One Token",
                        subtitle = "Treat web links and email addresses as single words",
                        checked = lengthSettings.urlEmailAsSingleToken,
                        onCheckedChange = { viewModel.updateLengthSettings(lengthSettings.copy(urlEmailAsSingleToken = it)) }
                    )
                }
            }

            // Display Results Options
            item {
                SectionHeader("RESULT DASHBOARD DISPLAY")
                SettingsCard {
                    ToggleRow(
                        title = "Show Median",
                        subtitle = "Display 50th percentile sentence length",
                        checked = displaySettings.showMedian,
                        onCheckedChange = { viewModel.updateDisplaySettings(displaySettings.copy(showMedian = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Show Mode (Most Common)",
                        subtitle = "Display the most frequent sentence length(s)",
                        checked = displaySettings.showMode,
                        onCheckedChange = { viewModel.updateDisplaySettings(displaySettings.copy(showMode = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Show Length Distribution",
                        subtitle = "Display length-by-length count and percentage table",
                        checked = displaySettings.showDistribution,
                        onCheckedChange = { viewModel.updateDisplaySettings(displaySettings.copy(showDistribution = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Show Length Histogram Chart",
                        subtitle = "Display expandable graphical distribution bar chart",
                        checked = displaySettings.showChart,
                        onCheckedChange = { viewModel.updateDisplaySettings(displaySettings.copy(showChart = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Show Sentence Length Variability",
                        subtitle = "Display population standard deviation card",
                        checked = displaySettings.showVariability,
                        onCheckedChange = { viewModel.updateDisplaySettings(displaySettings.copy(showVariability = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Show Short / Medium / Long Categories",
                        subtitle = "Display reading rhythm category breakdown",
                        checked = displaySettings.showShortMediumLong,
                        onCheckedChange = { viewModel.updateDisplaySettings(displaySettings.copy(showShortMediumLong = it)) }
                    )
                }
            }

            // Behavior Settings
            item {
                SectionHeader("APP BEHAVIOR")
                SettingsCard {
                    ToggleRow(
                        title = "Live Analysis",
                        subtitle = "Automatically analyze text while typing (debounced 300ms)",
                        checked = behaviorSettings.liveAnalysis,
                        onCheckedChange = { viewModel.updateBehaviorSettings(behaviorSettings.copy(liveAnalysis = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Haptics & Feedback",
                        subtitle = "Vibrate on key action buttons",
                        checked = behaviorSettings.haptics,
                        onCheckedChange = { viewModel.updateBehaviorSettings(behaviorSettings.copy(haptics = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Animations",
                        subtitle = "Show subtle card and list transitions",
                        checked = behaviorSettings.animations,
                        onCheckedChange = { viewModel.updateBehaviorSettings(behaviorSettings.copy(animations = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Confirm Delete",
                        subtitle = "Ask for confirmation before deleting history items",
                        checked = behaviorSettings.confirmDelete,
                        onCheckedChange = { viewModel.updateBehaviorSettings(behaviorSettings.copy(confirmDelete = it)) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRow(
                        title = "Restore Last Text",
                        subtitle = "Automatically reload your previous text when app restarts",
                        checked = behaviorSettings.restoreLastText,
                        onCheckedChange = { viewModel.updateBehaviorSettings(behaviorSettings.copy(restoreLastText = it)) }
                    )
                }
            }

            // Reset Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESET SETTINGS TO DEFAULTS")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Reset Confirm Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Settings?") },
            text = { Text("This will restore default settings. Your saved analysis history will not be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettingsToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Abbreviation Dialog
    if (showAddAbbrDialog) {
        AlertDialog(
            onDismissRequest = { showAddAbbrDialog = false },
            title = { Text("Add Custom Abbreviation") },
            text = {
                Column {
                    Text("Enter abbreviation without trailing dot (e.g. 'univ', 'dept'):", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAbbrText,
                        onValueChange = { newAbbrText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newAbbrText.trim().trimEnd('.')
                        if (clean.isNotEmpty() && !detectionSettings.customAbbreviations.contains(clean)) {
                            val updated = detectionSettings.customAbbreviations + clean
                            viewModel.updateDetectionSettings(detectionSettings.copy(customAbbreviations = updated))
                        }
                        newAbbrText = ""
                        showAddAbbrDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAbbrDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Terminator Dialog
    if (showAddTermDialog) {
        AlertDialog(
            onDismissRequest = { showAddTermDialog = false },
            title = { Text("Add Custom Terminator") },
            text = {
                Column {
                    Text("Enter punctuation mark (e.g. '؟' or '۔'):", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTermText,
                        onValueChange = { newTermText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newTermText.trim()
                        if (clean.isNotEmpty() && !detectionSettings.customTerminators.contains(clean)) {
                            val updated = detectionSettings.customTerminators + clean
                            viewModel.updateDetectionSettings(detectionSettings.copy(customTerminators = updated))
                        }
                        newTermText = ""
                        showAddTermDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTermDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
