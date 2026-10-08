package com.example.quotegap.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val SUPPORTED_CURRENCIES = listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR", "Other")

@Composable
fun CurrencySelectorDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    val isOther = currentCurrency !in listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR")
    var selectedOption by remember {
        mutableStateOf(if (isOther) "Other" else currentCurrency)
    }
    var customCurrencyText by remember {
        mutableStateOf(if (isOther) currentCurrency else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Display Currency",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                SUPPORTED_CURRENCIES.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = currency }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == currency,
                            onClick = { selectedOption = currency }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (selectedOption == "Other") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCurrencyText,
                        onValueChange = { customCurrencyText = it.take(8) },
                        label = { Text("Custom Currency Code/Symbol") },
                        placeholder = { Text("e.g. CAD, AUD, ¥") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalCurrency = if (selectedOption == "Other") {
                        customCurrencyText.trim().ifBlank { "Other" }
                    } else {
                        selectedOption
                    }
                    onCurrencySelected(finalCurrency)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
