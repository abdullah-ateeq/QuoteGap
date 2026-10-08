package com.example.quotegap.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.ui.components.BudgetComparisonBar
import com.example.quotegap.ui.components.CalculationFormulaCard
import com.example.quotegap.ui.components.CurrencySelectorDialog
import com.example.quotegap.ui.components.PrimaryResultCard
import com.example.quotegap.ui.viewmodel.QuoteGapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("DEPRECATION")
@Composable
fun CalculatorScreen(
    viewModel: QuoteGapViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsState()
    val projectName by viewModel.projectName.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val comparisonMode by viewModel.comparisonMode.collectAsState()
    val targetBudget by viewModel.targetBudgetInput.collectAsState()
    val quotedPrice by viewModel.quotedPriceInput.collectAsState()
    val unitPrice by viewModel.unitPriceInput.collectAsState()
    val quantity by viewModel.quantityInput.collectAsState()

    val budgetError by viewModel.budgetError.collectAsState()
    val quoteError by viewModel.quoteError.collectAsState()
    val unitPriceError by viewModel.unitPriceError.collectAsState()
    val quantityError by viewModel.quantityError.collectAsState()

    val result by viewModel.calculationResult.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        CurrencySelectorDialog(
            currentCurrency = currency,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { selected ->
                viewModel.onCurrencyChange(selected)
                showCurrencyDialog = false
            }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTabletLandscape = maxWidth >= 680.dp

        if (isTabletLandscape) {
            // Two-column layout for tablets / wide landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Inputs & Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    CalculatorHeader()
                    Spacer(modifier = Modifier.height(16.dp))

                    QuickExampleChips(onSelectExample = { b, q -> viewModel.applyExample(b, q) })
                    Spacer(modifier = Modifier.height(16.dp))

                    CalculatorInputs(
                        projectName = projectName,
                        onProjectNameChange = viewModel::onProjectNameChange,
                        currency = currency,
                        onOpenCurrencyDialog = { showCurrencyDialog = true },
                        comparisonMode = comparisonMode,
                        onComparisonModeChange = viewModel::onComparisonModeChange,
                        targetLabel = settings.comparisonLabel.label,
                        targetBudget = targetBudget,
                        onTargetBudgetChange = viewModel::onTargetBudgetChange,
                        budgetError = budgetError,
                        quotedPrice = quotedPrice,
                        onQuotedPriceChange = viewModel::onQuotedPriceChange,
                        quoteError = quoteError,
                        unitPrice = unitPrice,
                        onUnitPriceChange = viewModel::onUnitPriceChange,
                        unitPriceError = unitPriceError,
                        quantity = quantity,
                        onQuantityChange = viewModel::onQuantityChange,
                        quantityError = quantityError,
                        notes = notes,
                        onNotesChange = viewModel::onNotesChange,
                        showNotes = settings.showNotes
                    )
                }

                // Right Column: Results, Chart, Actions & Disclaimer
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (result != null) {
                        PrimaryResultCard(
                            result = result!!,
                            currency = currency,
                            targetLabel = settings.comparisonLabel.label,
                            decimalPlaces = settings.decimalPlaces,
                            showPercentage = settings.showPercentage
                        )

                        if (settings.showBudgetBar) {
                            Spacer(modifier = Modifier.height(16.dp))
                            BudgetComparisonBar(
                                result = result!!,
                                targetLabel = settings.comparisonLabel.label,
                                decimalPlaces = settings.decimalPlaces
                            )
                        }

                        if (settings.showFormula) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CalculationFormulaCard(
                                result = result!!,
                                decimalPlaces = settings.decimalPlaces
                            )
                        }
                    } else {
                        EmptyInputPrompt()
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ActionButtonsRow(
                        hasResult = result != null,
                        onCopy = {
                            val text = viewModel.buildCopyShareText()
                            clipboardManager.setText(AnnotatedString(text))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Result copied to clipboard")
                            }
                        },
                        onShare = {
                            val text = viewModel.buildCopyShareText()
                            shareResult(context, text)
                        },
                        onSave = { viewModel.saveCalculation() },
                        onReset = { viewModel.resetCalculator() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    DisclaimerFooter()
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            // Single-column layout for phones portrait
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                CalculatorHeader()
                Spacer(modifier = Modifier.height(14.dp))

                QuickExampleChips(onSelectExample = { b, q -> viewModel.applyExample(b, q) })
                Spacer(modifier = Modifier.height(16.dp))

                CalculatorInputs(
                    projectName = projectName,
                    onProjectNameChange = viewModel::onProjectNameChange,
                    currency = currency,
                    onOpenCurrencyDialog = { showCurrencyDialog = true },
                    comparisonMode = comparisonMode,
                    onComparisonModeChange = viewModel::onComparisonModeChange,
                    targetLabel = settings.comparisonLabel.label,
                    targetBudget = targetBudget,
                    onTargetBudgetChange = viewModel::onTargetBudgetChange,
                    budgetError = budgetError,
                    quotedPrice = quotedPrice,
                    onQuotedPriceChange = viewModel::onQuotedPriceChange,
                    quoteError = quoteError,
                    unitPrice = unitPrice,
                    onUnitPriceChange = viewModel::onUnitPriceChange,
                    unitPriceError = unitPriceError,
                    quantity = quantity,
                    onQuantityChange = viewModel::onQuantityChange,
                    quantityError = quantityError,
                    notes = notes,
                    onNotesChange = viewModel::onNotesChange,
                    showNotes = settings.showNotes
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Results section
                if (result != null) {
                    PrimaryResultCard(
                        result = result!!,
                        currency = currency,
                        targetLabel = settings.comparisonLabel.label,
                        decimalPlaces = settings.decimalPlaces,
                        showPercentage = settings.showPercentage
                    )

                    if (settings.showBudgetBar) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BudgetComparisonBar(
                            result = result!!,
                            targetLabel = settings.comparisonLabel.label,
                            decimalPlaces = settings.decimalPlaces
                        )
                    }

                    if (settings.showFormula) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CalculationFormulaCard(
                            result = result!!,
                            decimalPlaces = settings.decimalPlaces
                        )
                    }
                } else {
                    EmptyInputPrompt()
                }

                Spacer(modifier = Modifier.height(20.dp))

                ActionButtonsRow(
                    hasResult = result != null,
                    onCopy = {
                        val text = viewModel.buildCopyShareText()
                        clipboardManager.setText(AnnotatedString(text))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Result copied to clipboard")
                        }
                    },
                    onShare = {
                        val text = viewModel.buildCopyShareText()
                        shareResult(context, text)
                    },
                    onSave = { viewModel.saveCalculation() },
                    onReset = { viewModel.resetCalculator() }
                )

                Spacer(modifier = Modifier.height(24.dp))
                DisclaimerFooter()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CalculatorHeader() {
    Column {
        Text(
            text = "QUOTEGAP",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Compare quoted prices with your target budget.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickExampleChips(
    onSelectExample: (String, String) -> Unit
) {
    Column {
        Text(
            text = "QUICK EXAMPLES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ElevatedFilterChip(
                selected = false,
                onClick = { onSelectExample("100000", "92000") },
                label = { Text("Under Budget (92k vs 100k)") },
                colors = FilterChipDefaults.elevatedFilterChipColors()
            )
            ElevatedFilterChip(
                selected = false,
                onClick = { onSelectExample("100000", "100000") },
                label = { Text("On Budget (100k)") },
                colors = FilterChipDefaults.elevatedFilterChipColors()
            )
            ElevatedFilterChip(
                selected = false,
                onClick = { onSelectExample("100000", "115000") },
                label = { Text("Over Budget (115k vs 100k)") },
                colors = FilterChipDefaults.elevatedFilterChipColors()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorInputs(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    currency: String,
    onOpenCurrencyDialog: () -> Unit,
    comparisonMode: ComparisonMode,
    onComparisonModeChange: (ComparisonMode) -> Unit,
    targetLabel: String,
    targetBudget: String,
    onTargetBudgetChange: (String) -> Unit,
    budgetError: String?,
    quotedPrice: String,
    onQuotedPriceChange: (String) -> Unit,
    quoteError: String?,
    unitPrice: String,
    onUnitPriceChange: (String) -> Unit,
    unitPriceError: String?,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    quantityError: String?,
    notes: String,
    onNotesChange: (String) -> Unit,
    showNotes: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Project Name & Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    label = { Text("Project / Item Name") },
                    placeholder = { Text("e.g. Office Chairs") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Currency selector box
                Surface(
                    onClick = onOpenCurrencyDialog,
                    shape = RoundedCornerShape(4.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENCY",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select currency"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector: Total vs Per Unit
            Text(
                text = "COMPARE TOTAL OR UNIT PRICE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    onClick = { onComparisonModeChange(ComparisonMode.TOTAL) },
                    selected = comparisonMode == ComparisonMode.TOTAL
                ) {
                    Text("Total Quote")
                }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    onClick = { onComparisonModeChange(ComparisonMode.PER_UNIT) },
                    selected = comparisonMode == ComparisonMode.PER_UNIT
                ) {
                    Text("Per Unit")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Budget
            OutlinedTextField(
                value = targetBudget,
                onValueChange = onTargetBudgetChange,
                label = { Text(targetLabel) },
                placeholder = { Text("e.g. 100000") },
                isError = budgetError != null,
                supportingText = {
                    if (budgetError != null) {
                        Text(budgetError, color = MaterialTheme.colorScheme.error)
                    }
                },
                prefix = { Text("$currency ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Quote Inputs based on Mode
            if (comparisonMode == ComparisonMode.TOTAL) {
                OutlinedTextField(
                    value = quotedPrice,
                    onValueChange = onQuotedPriceChange,
                    label = { Text("Quoted Total Price") },
                    placeholder = { Text("e.g. 92000") },
                    isError = quoteError != null,
                    supportingText = {
                        if (quoteError != null) {
                            Text(quoteError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    prefix = { Text("$currency ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Per Unit fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = onUnitPriceChange,
                        label = { Text("Quoted Unit Price") },
                        placeholder = { Text("e.g. 1200") },
                        isError = unitPriceError != null,
                        supportingText = {
                            if (unitPriceError != null) {
                                Text(unitPriceError, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        prefix = { Text("$currency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = onQuantityChange,
                        label = { Text("Quantity") },
                        placeholder = { Text("e.g. 40") },
                        isError = quantityError != null,
                        supportingText = {
                            if (quantityError != null) {
                                Text(quantityError, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }

            // Notes field if enabled in settings
            if (showNotes) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Supplier A, includes delivery") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EmptyInputPrompt() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter Budget & Quoted Price",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Provide valid numbers above to instantly see budget variance, usage, and status.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionButtonsRow(
    hasResult: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = hasResult,
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save")
        }

        OutlinedButton(
            onClick = onCopy,
            enabled = hasResult,
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy")
        }

        OutlinedButton(
            onClick = onShare,
            enabled = hasResult,
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
        }

        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset")
        }
    }
}

@Composable
private fun DisclaimerFooter() {
    Text(
        text = "QuoteGap is a simple comparison tool and does not provide financial, accounting, procurement, or investment advice.",
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        lineHeight = 15.sp
    )
}

private fun shareResult(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Quote Comparison")
    context.startActivity(shareIntent)
}
