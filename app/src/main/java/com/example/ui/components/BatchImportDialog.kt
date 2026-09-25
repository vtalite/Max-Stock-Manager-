package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OnlineProductLookupService
import com.example.data.WineItem
import com.example.ui.theme.WineBurgundyPrimary

fun applyDateMask(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)
    return when {
        digits.length <= 2 -> digits
        digits.length <= 4 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
        else -> "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4)}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchImportDialog(
    onDismiss: () -> Unit,
    onImportBatch: (List<WineItem>, String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mercearia") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var expirationDateText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categoryOptions = listOf(
        "Mercearia",
        "Vinhos",
        "Destilados",
        "Salgadinhos",
        "Bomboniere",
        "Congelados",
        "Gelos",
        "Cervejas",
        "Sucos e Diversos",
        "Tabaco",
        "Detectar Automaticamente (por nome)",
        "Sem Seção (Enviar para 'Produtos em Alteração')"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("batch_import_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Importar em Lote",
                        tint = WineBurgundyPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Enviar Lista de Produtos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WineBurgundyPrimary
                        )
                        Text(
                            text = "Cadastre múltiplos produtos de uma só vez",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Dropdown Selection
                Text(
                    text = "Escolher Categoria dos Produtos:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria / Seção Alvo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = WineBurgundyPrimary)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("batch_category_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false }
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCategory = option
                                    isCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Expiration Date Field with Automatic Date Mask (DD/MM/AAAA)
                Text(
                    text = "Data de Validade (Opcional):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = expirationDateText,
                    onValueChange = {
                        expirationDateText = applyDateMask(it)
                    },
                    label = { Text("Data de Validade (DD/MM/AAAA)") },
                    placeholder = { Text("ex: 25/12/2026") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = WineBurgundyPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_expiration_date_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Callout Card regarding "Produtos em Alteração"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = WineBurgundyPrimary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WineBurgundyPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Caso selecione 'Sem Seção' ou o produto não possua categoria definida, ele será adicionado na seção 'Produtos em Alteração'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-line Text Field for product list
                Text(
                    text = "Digite ou cole a lista de produtos (1 por linha):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "Exemplo:\nCerveja Heineken 600ml\nVinho Cabernet Sauvignon, 2\nÁgua Mineral 500ml\nSabão em Pó (Sem seção)"
                        )
                    },
                    minLines = 6,
                    maxLines = 10,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_import_text_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("cancel_batch_import_button")
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            if (lines.isEmpty()) {
                                errorMessage = "Insira pelo menos um produto na lista."
                                return@Button
                            }

                            val itemsToCreate = mutableListOf<WineItem>()
                            var fallbackCount = 0

                            val batchExpMillis = try {
                                if (expirationDateText.isNotBlank()) {
                                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                    sdf.parse(expirationDateText.trim())?.time
                                } else null
                            } catch (e: Exception) {
                                null
                            }

                            val finalType = "Padrão"

                            for (line in lines) {
                                var prodName = line
                                var qty = 1

                                // Extract trailing quantity e.g. "Cerveja, 5" or "Vinho - 2" or "3x Suco"
                                if (line.contains(Regex("[,\\-]\\s*\\d+$"))) {
                                    val parts = line.split(Regex("[,\\-]"))
                                    if (parts.size >= 2) {
                                        val potentialQty = parts.last().trim().toIntOrNull()
                                        if (potentialQty != null && potentialQty > 0) {
                                            qty = potentialQty
                                            prodName = parts.dropLast(1).joinToString("-").trim()
                                        }
                                    }
                                }

                                // Determine final category
                                val finalCategory = when {
                                    selectedCategory.startsWith("Detectar") || selectedCategory.startsWith("Sem Seção") -> {
                                        val inferred = OnlineProductLookupService.inferCategoryFromName(prodName)
                                        if (inferred != null) {
                                            inferred
                                        } else {
                                            fallbackCount++
                                            "Produtos em Alteração"
                                        }
                                    }
                                    else -> selectedCategory
                                }

                                itemsToCreate.add(
                                    WineItem(
                                        name = prodName,
                                        category = finalCategory,
                                        wineType = finalType,
                                        quantity = qty,
                                        expirationDateMillis = batchExpMillis
                                    )
                                )
                            }

                            val summaryMsg = if (fallbackCount > 0) {
                                "${itemsToCreate.size} produtos cadastrados ($fallbackCount na seção 'Produtos em Alteração')."
                            } else {
                                "${itemsToCreate.size} produtos cadastrados com sucesso na seção '$selectedCategory'."
                            }

                            onImportBatch(itemsToCreate, summaryMsg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_batch_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cadastrar Lista", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
