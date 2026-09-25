package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CategoryConfig
import com.example.ui.theme.WineBurgundyPrimary

@Composable
fun CategoryConfigTableDialog(
    categoryConfigs: List<CategoryConfig>,
    onSaveConfig: (categoryName: String, newMinQuantity: Int, updateExistingProducts: Boolean) -> Unit,
    onAddNewCategory: (categoryName: String, minQuantity: Int, unitDescription: String, notes: String) -> Unit,
    onDeleteCategory: ((categoryName: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    // Local editable map of minQuantities as Strings so user can edit and see zero when cleared
    val editableMinMap = remember(categoryConfigs) {
        mutableStateMapOf<String, String>().apply {
            categoryConfigs.forEach { config ->
                put(config.categoryName, config.minQuantity.toString())
            }
        }
    }

    var updateExistingProducts by remember { mutableStateOf(false) }

    // State for creating new item in standard table
    var isCreatingNewItem by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newUnitDescription by remember { mutableStateOf("unidades") }
    var newMinQuantityText by remember { mutableStateOf("2") }
    var newNotes by remember { mutableStateOf("") }
    var newCategoryError by remember { mutableStateOf(false) }

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
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("category_config_table_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(18.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(WineBurgundyPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = WineBurgundyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tabela Padrão de Reposição",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${categoryConfigs.size} itens e categorias com índices mínimos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action to Add New Item in Standard Table
                Button(
                    onClick = { isCreatingNewItem = !isCreatingNewItem },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCreatingNewItem) MaterialTheme.colorScheme.secondary else WineBurgundyPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_to_standard_table_button")
                ) {
                    Icon(
                        imageVector = if (isCreatingNewItem) Icons.Default.Close else Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCreatingNewItem) "Cancelar Novo Item" else "➕ Adicionar Novo Item na Tabela Padrão",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Expandable Card for Creating New Category/Item
                AnimatedVisibility(visible = isCreatingNewItem) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Cadastrar Novo Item na Tabela Padrão",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WineBurgundyPrimary
                            )

                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = {
                                    newCategoryName = it
                                    newCategoryError = false
                                },
                                label = { Text("Nome da Categoria / Item") },
                                placeholder = { Text("Ex: Destilados Especiais, Queijos, Embalagens") },
                                isError = newCategoryError,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_category_name_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newUnitDescription,
                                    onValueChange = { newUnitDescription = it },
                                    label = { Text("Tipo de Embalagem / Unidade") },
                                    placeholder = { Text("Ex: garrafas, latas, caixas, kg") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.5f)
                                )

                                OutlinedTextField(
                                    value = newMinQuantityText,
                                    onValueChange = { input ->
                                        newMinQuantityText = input.filter { it.isDigit() }
                                    },
                                    label = { Text("Estoque Mín.") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_category_min_stock_input")
                                )
                            }

                            OutlinedTextField(
                                value = newNotes,
                                onValueChange = { newNotes = it },
                                label = { Text("Observação / Descrição (Opcional)") },
                                placeholder = { Text("Ex: Reposição semanal obrigatória") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    isCreatingNewItem = false
                                    newCategoryName = ""
                                }) {
                                    Text("Cancelar")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        if (newCategoryName.isBlank()) {
                                            newCategoryError = true
                                            return@Button
                                        }
                                        val minQty = newMinQuantityText.toIntOrNull() ?: 2
                                        onAddNewCategory(
                                            newCategoryName.trim(),
                                            minQty,
                                            newUnitDescription.trim().ifBlank { "unidades" },
                                            newNotes.trim()
                                        )
                                        editableMinMap[newCategoryName.trim()] = minQty.toString()
                                        isCreatingNewItem = false
                                        newCategoryName = ""
                                        newNotes = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("confirm_add_category_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salvar na Tabela", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description Box & Checkbox to Apply to Existing Products
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Quando você cadastrar um novo produto nesta categoria, o estoque mínimo será preenchido automaticamente com o valor desta tabela.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateExistingProducts = !updateExistingProducts },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = updateExistingProducts,
                                onCheckedChange = { updateExistingProducts = it },
                                colors = CheckboxDefaults.colors(checkedColor = WineBurgundyPrimary),
                                modifier = Modifier.testTag("checkbox_update_existing_products")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Atualizar também o estoque mínimo dos produtos já cadastrados",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Table of Category Configs
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("category_configs_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryConfigs, key = { it.categoryName }) { config ->
                        val currentValueStr = editableMinMap[config.categoryName] ?: config.minQuantity.toString()

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = config.categoryName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (onDeleteCategory != null && isUserCustomCategory(config.categoryName)) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { onDeleteCategory(config.categoryName) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Excluir Categoria",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Unidade: ${config.unitDescription}${if (config.notes.isNotBlank()) " • ${config.notes}" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Stock Min Control
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Decrement Button
                                    IconButton(
                                        onClick = {
                                            val current = currentValueStr.toIntOrNull() ?: 0
                                            val next = (current - 1).coerceAtLeast(0)
                                            editableMinMap[config.categoryName] = next.toString()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Diminuir",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Numeric Text Field (Clearing leaves it 0!)
                                    OutlinedTextField(
                                        value = currentValueStr,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }
                                            editableMinMap[config.categoryName] = if (digits.isEmpty()) {
                                                "0"
                                            } else {
                                                if (digits.length > 1 && digits.startsWith("0")) {
                                                    digits.trimStart('0').ifEmpty { "0" }
                                                } else {
                                                    digits
                                                }
                                            }
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .width(58.dp)
                                            .height(48.dp)
                                            .testTag("input_min_stock_${config.categoryName}")
                                    )

                                    // Increment Button
                                    IconButton(
                                        onClick = {
                                            val current = currentValueStr.toIntOrNull() ?: 0
                                            val next = current + 1
                                            editableMinMap[config.categoryName] = next.toString()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Aumentar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fechar")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            categoryConfigs.forEach { config ->
                                val updatedVal = editableMinMap[config.categoryName]?.toIntOrNull() ?: config.minQuantity
                                onSaveConfig(config.categoryName, updatedVal, updateExistingProducts)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_category_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar Tabela Padrão", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun isUserCustomCategory(categoryName: String): Boolean {
    val defaults = listOf("Vinhos", "Cerveja", "Destilados", "Sucos e Agua", "Mercearia", "Congelados", "Gelos")
    return !defaults.any { it.equals(categoryName, ignoreCase = true) }
}
