package com.example.quotegap.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotegap.data.model.AppThemeMode
import com.example.quotegap.data.model.ComparisonLabel
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.ui.components.ConfirmationDialog
import com.example.quotegap.ui.components.CurrencySelectorDialog
import com.example.quotegap.ui.viewmodel.QuoteGapViewModel

@Composable
fun SettingsScreen(
    viewModel: QuoteGapViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        CurrencySelectorDialog(
            currentCurrency = settings.defaultCurrency,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { selected ->
                viewModel.updateSettings(
                    settings.copy(
                        defaultCurrency = selected,
                        customCurrency = if (selected !in listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR")) selected else settings.customCurrency
                    )
                )
                showCurrencyDialog = false
            }
        )
    }

    if (showResetSettingsDialog) {
        ConfirmationDialog(
            title = "Reset All Settings",
            message = "Are you sure you want to restore all settings to their default values?",
            confirmText = "Reset",
            onConfirm = {
                viewModel.resetSettings()
                showResetSettingsDialog = false
            },
            onDismiss = { showResetSettingsDialog = false }
        )
    }

    if (showClearHistoryDialog) {
        ConfirmationDialog(
            title = "Clear History",
            message = "Are you sure you want to delete all saved calculations?",
            confirmText = "Clear All",
            onConfirm = {
                viewModel.clearHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Configure display, calculation defaults, and preferences.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Section
        SettingsSectionTitle("APPEARANCE")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        onClick = { viewModel.updateSettings(settings.copy(themeMode = AppThemeMode.SYSTEM)) },
                        selected = settings.themeMode == AppThemeMode.SYSTEM
                    ) {
                        Text("System")
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        onClick = { viewModel.updateSettings(settings.copy(themeMode = AppThemeMode.LIGHT)) },
                        selected = settings.themeMode == AppThemeMode.LIGHT
                    ) {
                        Text("Light")
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        onClick = { viewModel.updateSettings(settings.copy(themeMode = AppThemeMode.DARK)) },
                        selected = settings.themeMode == AppThemeMode.DARK
                    ) {
                        Text("Dark")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                SettingsSwitchRow(
                    title = "Dynamic Colors",
                    subtitle = "Use Material You wallpaper colors (Android 12+)",
                    checked = settings.dynamicColor,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculation & Defaults Section
        SettingsSectionTitle("DEFAULTS & DISPLAY")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Default Currency
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyDialog = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Default Currency",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = settings.activeCurrency,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Change Currency"
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Comparison Label Dropdown
                var labelMenuExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { labelMenuExpanded = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Comparison Label",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = settings.comparisonLabel.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Label"
                        )
                        DropdownMenu(
                            expanded = labelMenuExpanded,
                            onDismissRequest = { labelMenuExpanded = false }
                        ) {
                            ComparisonLabel.values().forEach { labelEnum ->
                                DropdownMenuItem(
                                    text = { Text(labelEnum.label) },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(comparisonLabel = labelEnum))
                                        labelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Default Mode
                Text(
                    text = "Default Comparison Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        onClick = { viewModel.updateSettings(settings.copy(defaultMode = ComparisonMode.TOTAL)) },
                        selected = settings.defaultMode == ComparisonMode.TOTAL
                    ) {
                        Text("Total Quote")
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = { viewModel.updateSettings(settings.copy(defaultMode = ComparisonMode.PER_UNIT)) },
                        selected = settings.defaultMode == ComparisonMode.PER_UNIT
                    ) {
                        Text("Per Unit")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Decimal Places (0, 1, 2, 3, 4)
                Text(
                    text = "Decimal Places (${settings.decimalPlaces})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    (0..4).forEach { decimals ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = decimals, count = 5),
                            onClick = { viewModel.updateSettings(settings.copy(decimalPlaces = decimals)) },
                            selected = settings.decimalPlaces == decimals
                        ) {
                            Text(decimals.toString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Default initial budget & quote values
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = settings.defaultBudget,
                        onValueChange = { viewModel.updateSettings(settings.copy(defaultBudget = it)) },
                        label = { Text("Default Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = settings.defaultQuote,
                        onValueChange = { viewModel.updateSettings(settings.copy(defaultQuote = it)) },
                        label = { Text("Default Quote") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Features & Toggles
        SettingsSectionTitle("INTERFACE FEATURES")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Show Budget Comparison Bar",
                    subtitle = "Visual progress bar showing quote vs target marker",
                    checked = settings.showBudgetBar,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showBudgetBar = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SettingsSwitchRow(
                    title = "Show Percentage Variance",
                    subtitle = "Display percentage above or below target budget",
                    checked = settings.showPercentage,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showPercentage = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SettingsSwitchRow(
                    title = "Show Calculation Formula",
                    subtitle = "Collapsible formula breakdown card",
                    checked = settings.showFormula,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showFormula = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SettingsSwitchRow(
                    title = "Show Notes Field",
                    subtitle = "Optional multiline notes in calculator",
                    checked = settings.showNotes,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showNotes = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SettingsSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Vibration on status change, saving, and deletion",
                    checked = settings.hapticFeedback,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SettingsSwitchRow(
                    title = "Confirm Delete",
                    subtitle = "Require confirmation before deleting history items",
                    checked = settings.confirmDelete,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data & Maintenance
        SettingsSectionTitle("DATA & MAINTENANCE")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = { showResetSettingsDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset All Settings to Default")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showClearHistoryDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Saved History")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
