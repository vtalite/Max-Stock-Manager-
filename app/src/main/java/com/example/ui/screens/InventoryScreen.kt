package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.data.ProductOnlineInfo
import com.example.data.WineItem
import com.example.data.containsNormalized
import com.example.ui.SortOrder
import com.example.ui.components.ExportPdfDialog
import com.example.ui.components.OnlineProductSearchDialog
import com.example.ui.components.WineItemCard
import com.example.ui.theme.WineBurgundyPrimary

@Composable
fun InventoryScreen(
    wines: List<WineItem>,
    allDbWines: List<WineItem> = emptyList(),
    searchQuery: String,
    selectedCategoryFilter: String,
    selectedTypeFilter: String,
    selectedSortOrder: SortOrder,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterSelected: (String) -> Unit,
    onTypeFilterSelected: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onWineClick: (WineItem) -> Unit,
    onIncrement: (WineItem) -> Unit,
    onDecrement: (WineItem) -> Unit,
    onAddNewWineClick: () -> Unit,
    onClearAllProducts: () -> Unit = {},
    companyName: String = "Max Bebidas e Alimentos",
    companyCnpj: String = "",
    databaseName: String = "Estoque Central",
    operatorName: String = "Operador",
    onSelectOnlineProduct: ((ProductOnlineInfo) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Todos",
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
        "Produtos em Alteração"
    )
    val wineTypes = listOf("Todos", "Padrão", "Tinto", "Branco", "Rosé", "Espumante", "Pilsen/Lager", "IPA", "Whisky", "Gin", "Vodka")
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showExportPdfDialog by remember { mutableStateOf(false) }
    var showOnlineSearchDialog by remember { mutableStateOf(false) }

    // Source pool for complete database search
    val dbPool = if (allDbWines.isNotEmpty()) allDbWines else wines

    // Breakdown search query into tokens/parts
    val searchParts = remember(searchQuery) {
        searchQuery.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    // Match all products in database table containing all or any of the typed tokens
    val matchingDbProducts = remember(searchParts, dbPool) {
        if (searchParts.isNotEmpty()) {
            dbPool.filter { wine ->
                searchParts.all { token ->
                    wine.name.containsNormalized(token) ||
                    wine.category.containsNormalized(token) ||
                    wine.producer.containsNormalized(token) ||
                    wine.barcode.contains(token)
                }
            }
        } else emptyList()
    }

    val matchingNames = remember(matchingDbProducts) {
        matchingDbProducts.map { it.name }.distinct()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & Sort Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar por produto, marca, código...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WineBurgundyPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inventory_search_input")
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Botão de Pesquisa Online em Lojas e Adegas
                IconButton(
                    onClick = { showOnlineSearchDialog = true },
                    modifier = Modifier.testTag("inventory_online_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Buscar em Lojas Online",
                        tint = WineBurgundyPrimary
                    )
                }

                // Sort Order Menu Button
                Box {
                    IconButton(
                        onClick = { isSortMenuExpanded = true },
                        modifier = Modifier.testTag("sort_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Ordenar",
                            tint = WineBurgundyPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.label,
                                        fontWeight = if (order == selectedSortOrder) FontWeight.Bold else FontWeight.Normal,
                                        color = if (order == selectedSortOrder) WineBurgundyPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSortOrderChange(order)
                                    isSortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Filtros de busca solicitados: Nome A-Z, Validade, Menor, Maior, Troca
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Filtro:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = WineBurgundyPrimary,
                    modifier = Modifier.padding(end = 2.dp)
                )

                SortOrder.entries.forEach { order ->
                    val isSelected = order == selectedSortOrder
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSortOrderChange(order) },
                        label = {
                            Text(
                                text = order.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            when (order) {
                                SortOrder.NAME -> Icon(imageVector = Icons.Default.SortByAlpha, contentDescription = null, modifier = Modifier.size(13.dp))
                                SortOrder.EXPIRATION -> Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(13.dp))
                                SortOrder.QUANTITY_ASC -> Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(13.dp))
                                SortOrder.QUANTITY_DESC -> Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(13.dp))
                                SortOrder.TROCA -> Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(13.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WineBurgundyPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_${order.name}")
                    )
                }
            }

            // Seleção de texto por partes no campo de pesquisa
            if (searchParts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Partes da busca:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WineBurgundyPrimary,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    searchParts.forEach { part ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = WineBurgundyPrimary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WineBurgundyPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = part,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WineBurgundyPrimary,
                                    modifier = Modifier.clickable {
                                        // Isolar e pesquisar apenas por esta parte selecionada
                                        onSearchQueryChange(part)
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover esta parte",
                                    tint = WineBurgundyPrimary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            // Remove apenas esta parte da busca
                                            val remaining = searchParts.filter { it != part }.joinToString(" ")
                                            onSearchQueryChange(remaining)
                                        }
                                )
                            }
                        }
                    }

                    if (searchParts.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSearchQueryChange("") }
                        ) {
                            Text(
                                text = "Limpar tudo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Exibição de todos os nomes possíveis encontrados na tabela de dados
            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WineBurgundyPrimary.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = WineBurgundyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (matchingNames.isNotEmpty()) {
                                        "Nomes na tabela (${matchingNames.size} encontrados):"
                                    } else {
                                        "Pesquisa na tabela de dados:"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WineBurgundyPrimary
                                )
                            }

                            if (matchingNames.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = WineBurgundyPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "${matchingNames.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Botão para pesquisar nas lojas online de supermercados e adegas
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WineBurgundyPrimary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WineBurgundyPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showOnlineSearchDialog = true }
                                .testTag("search_online_supermarkets_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = WineBurgundyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pesquisar \"$searchQuery\" em Lojas Online de Supermercados & Adegas",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WineBurgundyPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = WineBurgundyPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (matchingNames.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            // Chips com todos os nomes possíveis que contêm as letras digitadas
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchingNames.forEach { prodName ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onSearchQueryChange(prodName)
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Label,
                                                contentDescription = null,
                                                tint = WineBurgundyPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = prodName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Se o usuário estiver com filtro de seção ativo e houver itens em outras seções
                            val outsideCategoryCount = matchingDbProducts.count { 
                                selectedCategoryFilter != "Todos" && !it.category.equals(selectedCategoryFilter, ignoreCase = true)
                            }
                            if (outsideCategoryCount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WineBurgundyPrimary.copy(alpha = 0.12f))
                                        .clickable { onCategoryFilterSelected("Todos") }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Existem $outsideCategoryCount produto(s) em outras seções.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WineBurgundyPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Ver Todas ➔",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WineBurgundyPrimary
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nenhum produto na tabela de dados contém as letras \"$searchQuery\".",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category / Section Dropdown Menu Button
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { isCategoryMenuExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WineBurgundyPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_menu_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Seção",
                                tint = WineBurgundyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Seção / Categoria: ${if (selectedCategoryFilter == "Todos") "Todas as Seções" else selectedCategoryFilter}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expandir Seções",
                            tint = WineBurgundyPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = { isCategoryMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategoryFilter
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (cat == "Todos") "Todas as Seções (Geral)" else cat,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) WineBurgundyPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = WineBurgundyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                onCategoryFilterSelected(cat)
                                isCategoryMenuExpanded = false
                            },
                            modifier = Modifier.testTag("category_option_$cat")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wine Count Header & Action Buttons (Exportar PDF & Limpar Tudo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${wines.size} produto(s)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { showExportPdfDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WineBurgundyPrimary
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Exportar PDF",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (wines.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("clear_all_products_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpar Tudo",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Limpar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (showClearConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showClearConfirmDialog = false },
                    title = { Text("Apagar Todos os Produtos?") },
                    text = { Text("Esta ação irá remover permanentemente todos os registros de produtos do estoque. Deseja continuar?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onClearAllProducts()
                                showClearConfirmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Apagar Tudo")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearConfirmDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Wine List or Empty State
            if (wines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WineBar,
                            contentDescription = "Nenhum vinho",
                            modifier = Modifier.size(64.dp),
                            tint = WineBurgundyPrimary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum produto encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Nenhum produto corresponde a \"$searchQuery\" com os filtros atuais."
                            } else {
                                "Tente alterar a busca, os filtros ou cadastre um novo produto no estoque."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("inventory_wine_list")
                ) {
                    items(wines, key = { it.id }) { wine ->
                        WineItemCard(
                            wine = wine,
                            onWineClick = { onWineClick(wine) },
                            onIncrement = { onIncrement(wine) },
                            onDecrement = { onDecrement(wine) }
                        )
                    }
                }
            }
        }

        // FAB to add new product
        FloatingActionButton(
            onClick = onAddNewWineClick,
            containerColor = WineBurgundyPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_wine_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Cadastrar Novo Produto")
        }
    }

    if (showExportPdfDialog) {
        val filterDesc = when {
            selectedCategoryFilter != "Todos" -> "Seção: $selectedCategoryFilter | Filtro: ${selectedSortOrder.label}"
            searchQuery.isNotBlank() -> "Busca: \"$searchQuery\" | Filtro: ${selectedSortOrder.label}"
            else -> "Estoque Geral | Filtro: ${selectedSortOrder.label}"
        }
        ExportPdfDialog(
            companyName = companyName,
            companyCnpj = companyCnpj,
            databaseName = databaseName,
            operatorName = operatorName,
            filterDescription = filterDesc,
            wines = wines,
            onDismiss = { showExportPdfDialog = false }
        )
    }

    if (showOnlineSearchDialog) {
        OnlineProductSearchDialog(
            initialQuery = searchQuery,
            onDismiss = { showOnlineSearchDialog = false },
            onSelectProduct = { selected ->
                if (onSelectOnlineProduct != null) {
                    onSelectOnlineProduct(selected)
                } else {
                    onAddNewWineClick()
                }
            }
        )
    }
}
