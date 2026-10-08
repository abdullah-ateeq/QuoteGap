package com.example.quotegap.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.data.model.QuoteStatus
import com.example.quotegap.data.model.SavedCalculation
import com.example.quotegap.domain.MoneyFormatter
import com.example.quotegap.domain.QuoteGapCalculator
import com.example.quotegap.ui.components.ConfirmationDialog
import com.example.quotegap.ui.theme.getStatusColors
import com.example.quotegap.ui.viewmodel.HistoryFilter
import com.example.quotegap.ui.viewmodel.QuoteGapViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: QuoteGapViewModel,
    onOpenCalculation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.historyList.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val activeFilter by viewModel.historyFilter.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var calculationToDelete by remember { mutableStateOf<SavedCalculation?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (calculationToDelete != null) {
        if (settings.confirmDelete) {
            ConfirmationDialog(
                title = "Delete Calculation",
                message = "Are you sure you want to delete '${calculationToDelete!!.projectName}'?",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deleteCalculation(calculationToDelete!!.id)
                    calculationToDelete = null
                },
                onDismiss = { calculationToDelete = null }
            )
        } else {
            viewModel.deleteCalculation(calculationToDelete!!.id)
            calculationToDelete = null
        }
    }

    if (showClearAllDialog) {
        ConfirmationDialog(
            title = "Clear All History",
            message = "Are you sure you want to delete all saved calculations? This cannot be undone.",
            confirmText = "Clear All",
            onConfirm = {
                viewModel.clearHistory()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Row with Clear All button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HISTORY",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "${historyList.size} of 50 saved calculations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = {
                        if (settings.confirmDelete) {
                            showClearAllDialog = true
                        } else {
                            viewModel.clearHistory()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear All",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onHistorySearchChange,
            placeholder = { Text("Search by project name or notes...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onHistorySearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips (All, Under Budget, On Budget, Over Budget)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = activeFilter == HistoryFilter.ALL,
                onClick = { viewModel.onHistoryFilterChange(HistoryFilter.ALL) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = activeFilter == HistoryFilter.UNDER_BUDGET,
                onClick = { viewModel.onHistoryFilterChange(HistoryFilter.UNDER_BUDGET) },
                label = { Text("Under Budget") },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = activeFilter == HistoryFilter.ON_BUDGET,
                onClick = { viewModel.onHistoryFilterChange(HistoryFilter.ON_BUDGET) },
                label = { Text("On Budget") },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = activeFilter == HistoryFilter.OVER_BUDGET,
                onClick = { viewModel.onHistoryFilterChange(HistoryFilter.OVER_BUDGET) },
                label = { Text("Over Budget") },
                colors = FilterChipDefaults.filterChipColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History List or Empty View
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotBlank() || activeFilter != HistoryFilter.ALL)
                            "No matching calculations found"
                        else
                            "No saved calculations yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || activeFilter != HistoryFilter.ALL)
                            "Try adjusting your search query or filter."
                        else
                            "Perform a calculation on the Calculator screen and tap 'Save'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        targetLabel = settings.comparisonLabel.label,
                        decimalPlaces = settings.decimalPlaces,
                        onOpen = {
                            viewModel.restoreCalculation(item)
                            onOpenCalculation()
                        },
                        onDuplicate = { viewModel.duplicateCalculation(item.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                        onDelete = { calculationToDelete = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: SavedCalculation,
    targetLabel: String,
    decimalPlaces: Int,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColors = getStatusColors(item.status)
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    val budgetBd = QuoteGapCalculator.parseBigDecimalOrNull(item.targetBudget)
    val quoteTotalBd = QuoteGapCalculator.parseBigDecimalOrNull(item.quotedTotal)
    val absDiffBd = QuoteGapCalculator.parseBigDecimalOrNull(item.absoluteDifference)
    val percentBd = QuoteGapCalculator.parseBigDecimalOrNull(item.percentageDifference)

    val formattedTarget = MoneyFormatter.formatMoney(budgetBd, item.currency, decimalPlaces)
    val formattedQuote = MoneyFormatter.formatMoney(quoteTotalBd, item.currency, decimalPlaces)
    val formattedDiff = MoneyFormatter.formatMoney(absDiffBd, item.currency, decimalPlaces)
    val formattedPercent = MoneyFormatter.formatPercentage(percentBd, decimalPlaces)

    val diffSubtitle = when (item.status) {
        QuoteStatus.UNDER_BUDGET -> "$formattedDiff under • $formattedPercent below budget"
        QuoteStatus.ON_BUDGET -> "Matches target budget"
        QuoteStatus.OVER_BUDGET -> "$formattedDiff over • $formattedPercent above budget"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Project Name & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.projectName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Favorite Icon Button
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (item.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Pill & Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColors.statusBackground,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColors.statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (item.mode == ComparisonMode.PER_UNIT && item.quantity != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Unit × ${item.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Numbers: Budget vs Quote
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "QUOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedQuote,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Difference and percentage
            Text(
                text = diffSubtitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = statusColors.statusText
            )

            // Notes if present
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${item.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(4.dp))

            // Actions: Open, Duplicate, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpen) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open")
                }

                TextButton(onClick = onDuplicate) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate")
                }

                TextButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
