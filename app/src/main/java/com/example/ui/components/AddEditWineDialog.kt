package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CategoryConfig
import com.example.data.WineItem
import com.example.data.containsNormalized
import com.example.ui.theme.WineBurgundyPrimary
import com.example.util.SoundFeedbackHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWineDialog(
    initialWine: WineItem? = null,
    prefilledBarcode: String = "",
    existingWines: List<WineItem> = emptyList(),
    categoryConfigs: List<CategoryConfig> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (WineItem) -> Unit,
    onScanBarcodeClick: (() -> Unit)? = null,
    onOpenBatchImport: (() -> Unit)? = null
) {
    val initialCategory = initialWine?.category ?: "Vinhos"
    val defaultMinFromConfig = categoryConfigs.find { it.categoryName.equals(initialCategory, ignoreCase = true) }?.minQuantity ?: 2

    var name by remember { mutableStateOf(initialWine?.name ?: "") }
    var category by remember { mutableStateOf(initialCategory) }
    var wineType by remember { mutableStateOf(initialWine?.wineType ?: "Padrão") }
    var producer by remember { mutableStateOf(initialWine?.producer ?: "") }
    var region by remember { mutableStateOf(initialWine?.region ?: "") }
    var grape by remember { mutableStateOf(initialWine?.grape ?: "") }

    // Numeric fields where completely erasing leaves value as "0"
    var quantityText by remember { mutableStateOf((initialWine?.quantity ?: 1).toString()) }
    var minQuantityText by remember { mutableStateOf((initialWine?.minQuantity ?: defaultMinFromConfig).toString()) }
    var priceText by remember { mutableStateOf(initialWine?.price?.toString() ?: "0") }
    var packagingType by remember { mutableStateOf(initialWine?.packagingType ?: "") }

    var barcode by remember { mutableStateOf(initialWine?.barcode ?: prefilledBarcode) }
    var location by remember { mutableStateOf(initialWine?.location ?: "Adega Principal") }
    var rating by remember { mutableFloatStateOf(initialWine?.rating ?: 4.0f) }
    var drinkingStartText by remember { mutableStateOf(initialWine?.drinkingWindowStart?.toString() ?: "") }
    var drinkingEndText by remember { mutableStateOf(initialWine?.drinkingWindowEnd?.toString() ?: "") }
    var notes by remember { mutableStateOf(initialWine?.notes ?: "") }
    var expirationDateText by remember {
        mutableStateOf(
            initialWine?.expirationDateMillis?.let {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(it))
            } ?: ""
        )
    }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    val categories = remember(categoryConfigs) {
        val standard = listOf(
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
        val fromConfigs = categoryConfigs.map { it.categoryName }
        (fromConfigs + standard).distinct()
    }

    var nameError by remember { mutableStateOf(false) }
    var showOnlineSearchDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isSearchingOnline by remember { mutableStateOf(false) }
    var onlineSearchFeedback by remember { mutableStateOf<String?>(null) }

    fun performOnlineLookup(code: String) {
        val clean = code.trim()
        if (clean.length < 5) return
        coroutineScope.launch {
            isSearchingOnline = true
            onlineSearchFeedback = "🔍 Pesquisando EAN online em catálogos de supermercados..."
            val result = com.example.data.OnlineProductLookupService.lookupProductByBarcode(clean)
            isSearchingOnline = false
            if (result != null) {
                name = result.name
                nameError = false
                val autoCat = result.category ?: com.example.data.OnlineProductLookupService.inferCategoryFromName(result.name)
                if (!autoCat.isNullOrBlank()) {
                    category = autoCat
                    if (initialWine == null) {
                        val catMin = categoryConfigs.find { it.categoryName.equals(autoCat, ignoreCase = true) }?.minQuantity ?: 2
                        minQuantityText = catMin.toString()
                    }
                }
                if (!result.brand.isNullOrBlank() && producer.isBlank()) {
                    producer = result.brand
                }
                onlineSearchFeedback = "✅ Encontrado nos catálogos de supermercados: ${result.name} (${category})"
            } else {
                onlineSearchFeedback = "⚠️ Não encontrado nos catálogos de supermercados. Digite o nome do produto correspondente."
            }
        }
    }

    LaunchedEffect(prefilledBarcode) {
        if (prefilledBarcode.isNotBlank() && initialWine == null && name.isBlank()) {
            performOnlineLookup(prefilledBarcode)
        }
    }

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
                .fillMaxHeight(0.9f)
                .testTag("add_edit_wine_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialWine == null) "Cadastrar Novo Produto" else "Editar Produto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (onOpenBatchImport != null && initialWine == null) {
                    OutlinedButton(
                        onClick = onOpenBatchImport,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("open_batch_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = null,
                            tint = WineBurgundyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Lista de Produtos (Em Lote)",
                            fontWeight = FontWeight.Bold,
                            color = WineBurgundyPrimary
                        )
                    }
                }

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nome do Produto e Preço (lado a lado / próximo)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newName ->
                                name = newName
                                nameError = false
                                val inferred = com.example.data.OnlineProductLookupService.inferCategoryFromName(newName)
                                if (inferred != null) {
                                    category = inferred
                                    if (initialWine == null) {
                                        val catMin = categoryConfigs.find { it.categoryName.equals(inferred, ignoreCase = true) }?.minQuantity ?: 2
                                        minQuantityText = catMin.toString()
                                    }
                                }
                            },
                            label = { Text("Nome do Produto *") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showOnlineSearchDialog = true },
                                    modifier = Modifier.testTag("search_online_product_field_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Buscar em Lojas Online de Supermercados e Adegas",
                                        tint = WineBurgundyPrimary
                                    )
                                }
                            },
                            isError = nameError,
                            supportingText = if (nameError) { { Text("Obrigatório") } } else null,
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("wine_name_input")
                        )

                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { input ->
                                priceText = if (input.isEmpty()) {
                                    "0"
                                } else {
                                    if (priceText == "0" && input.length > 1 && !input.startsWith("0.")) {
                                        input.removePrefix("0")
                                    } else {
                                        input
                                    }
                                }
                            },
                            label = { Text("Preço (R$)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.85f)
                                .testTag("product_price_input")
                        )
                    }

                    // Atalho de pesquisa nas lojas online de supermercados e adegas
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WineBurgundyPrimary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WineBurgundyPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOnlineSearchDialog = true }
                            .testTag("open_online_search_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = WineBurgundyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (name.isNotBlank()) "🔍 Buscar \"$name\" em Lojas Online de Supermercados & Adegas" else "🔍 Buscar em Lojas Online de Supermercados & Adegas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WineBurgundyPrimary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Busca no Banco de Dados local ao digitar o nome do produto (exibe todos os nomes possíveis)
                    val matchingDbProducts = remember(name, existingWines) {
                        if (name.trim().isNotEmpty() && initialWine == null) {
                            existingWines.filter {
                                it.name.containsNormalized(name.trim()) ||
                                it.barcode.contains(name.trim())
                            }
                        } else emptyList()
                    }

                    if (matchingDbProducts.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WineBurgundyPrimary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = WineBurgundyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nomes possíveis na tabela (${matchingDbProducts.size} encontrados):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WineBurgundyPrimary
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                matchingDbProducts.forEach { dbProduct ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                            .clickable {
                                                name = dbProduct.name
                                                category = dbProduct.category
                                                barcode = dbProduct.barcode
                                                quantityText = dbProduct.quantity.toString()
                                                minQuantityText = dbProduct.minQuantity.toString()
                                                if (dbProduct.price != null && dbProduct.price > 0) priceText = dbProduct.price.toString()
                                                if (!dbProduct.notes.isNullOrBlank()) notes = dbProduct.notes
                                                if (dbProduct.expirationDateMillis != null) {
                                                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                                    expirationDateText = sdf.format(java.util.Date(dbProduct.expirationDateMillis))
                                                }
                                            }
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = dbProduct.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${dbProduct.category} • Qtd: ${dbProduct.quantity} em estoque • Mín: ${dbProduct.minQuantity}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "Preencher ➔",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = WineBurgundyPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Categoria Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isCategoryExpanded,
                        onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria do Produto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("product_category_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = isCategoryExpanded,
                            onDismissRequest = { isCategoryExpanded = false }
                        ) {
                            categories.forEach { catOption ->
                                DropdownMenuItem(
                                    text = { Text(catOption) },
                                    onClick = {
                                        category = catOption
                                        isCategoryExpanded = false
                                        // Update minQuantity based on category standard table if new item
                                        if (initialWine == null) {
                                            val catMin = categoryConfigs.find { it.categoryName.equals(catOption, ignoreCase = true) }?.minQuantity ?: 2
                                            minQuantityText = catMin.toString()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Data de Validade (DD/MM/AAAA)
                    OutlinedTextField(
                        value = expirationDateText,
                        onValueChange = { expirationDateText = applyDateMask(it) },
                        label = { Text("Data de Validade (DD/MM/AAAA)") },
                        placeholder = { Text("ex: 25/12/2026") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_expiration_input")
                    )

                    // EAN com botão do Leitor de Câmera em tempo real e Busca Online
                    var isInlineScannerOpen by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EAN",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { performOnlineLookup(barcode) },
                                    enabled = barcode.isNotBlank() && !isSearchingOnline,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("lookup_online_button")
                                ) {
                                    if (isSearchingOnline) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = WineBurgundyPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSearchingOnline) "Buscando nos catálogos..." else "Buscar nos Catálogos",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                OutlinedButton(
                                    onClick = { isInlineScannerOpen = !isInlineScannerOpen },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("toggle_inline_barcode_scanner")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = WineBurgundyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isInlineScannerOpen) "Fechar Câmera" else "Câmera",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        if (isInlineScannerOpen) {
                            Spacer(modifier = Modifier.height(8.dp))
                            BarcodeScannerView(
                                isCompactMode = true,
                                onBarcodeDetected = { scannedCode ->
                                    barcode = scannedCode
                                    isInlineScannerOpen = false
                                    performOnlineLookup(scannedCode)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = barcode,
                            onValueChange = {
                                barcode = it
                                onlineSearchFeedback = null
                            },
                            placeholder = { Text("Digite ou escaneie o código EAN") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isInlineScannerOpen = !isInlineScannerOpen },
                                    modifier = Modifier.testTag("scan_barcode_in_dialog_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Escanear EAN",
                                        tint = WineBurgundyPrimary
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wine_barcode_input")
                        )

                        if (!onlineSearchFeedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = onlineSearchFeedback!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (onlineSearchFeedback!!.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Localização no Estoque / Estabelecimento
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Localização no Estoque") },
                        placeholder = { Text("ex: Adega A, Prateleira 2, Geladeira 01") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tipo do Produto / Embalagem (ex: Garrafa, Caixa, Pacote, Lata, etc.)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tipo do Produto / Embalagem",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val packageSuggestions = listOf("Garrafa", "Caixa", "Pacote", "Lata", "Fardo", "Unidade")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            packageSuggestions.forEach { suggestion ->
                                val isSelected = packagingType.equals(suggestion, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        packagingType = if (isSelected) "" else suggestion
                                    },
                                    label = { Text(suggestion) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WineBurgundyPrimary.copy(alpha = 0.15f),
                                        selectedLabelColor = WineBurgundyPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = packagingType,
                            onValueChange = { packagingType = it },
                            label = { Text("Unidade / Embalagem do Produto") },
                            placeholder = { Text("ex: Caixa, Garrafa, Pacote, Lata...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_packaging_type_input")
                        )
                    }

                    // Estoque e Limite de Alerta de Reposição
                    // REGRA DE OURO: Ao apagar um campo por completo, deixe o valor do campo zerado ("0")!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }
                                quantityText = if (digitsOnly.isEmpty()) {
                                    "0"
                                } else {
                                    if (digitsOnly.length > 1 && digitsOnly.startsWith("0")) {
                                        digitsOnly.trimStart('0').ifEmpty { "0" }
                                    } else {
                                        digitsOnly
                                    }
                                }
                            },
                            label = { Text("Quantidade em Estoque") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wine_quantity_input")
                        )

                        OutlinedTextField(
                            value = minQuantityText,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }
                                minQuantityText = if (digitsOnly.isEmpty()) {
                                    "0"
                                } else {
                                    if (digitsOnly.length > 1 && digitsOnly.startsWith("0")) {
                                        digitsOnly.trimStart('0').ifEmpty { "0" }
                                    } else {
                                        digitsOnly
                                    }
                                }
                            },
                            label = { Text("Alerta Reposição (Mín.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wine_min_quantity_input")
                        )
                    }

                    // Dica informativa sobre o índice padrão da tabela configurada
                    val currentCatConfig = categoryConfigs.find { it.categoryName.equals(category, ignoreCase = true) }
                    if (currentCatConfig != null) {
                        Text(
                            text = "💡 Índice padrão da categoria: ${currentCatConfig.minQuantity} ${currentCatConfig.unitDescription}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WineBurgundyPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    // Notas de Degustação / Harmonização
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas de Degustação e Harmonização") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_wine_button")
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                SoundFeedbackHelper.playError()
                                return@Button
                            }

                            val vintageInt = initialWine?.vintage ?: 0
                            val dStart = drinkingStartText.toIntOrNull()
                            val dEnd = drinkingEndText.toIntOrNull()
                            val priceDouble = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val finalQuantity = quantityText.toIntOrNull() ?: 0
                            val finalMinQuantity = minQuantityText.toIntOrNull() ?: 0

                            val expMillis = try {
                                if (expirationDateText.isNotBlank()) {
                                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                    sdf.parse(expirationDateText.trim())?.time
                                } else null
                            } catch (e: Exception) {
                                null
                            }

                            val resultItem = (initialWine ?: WineItem(
                                name = name.trim(),
                                category = category,
                                wineType = wineType,
                                vintage = vintageInt,
                                packagingType = packagingType.trim()
                            )).copy(
                                name = name.trim(),
                                category = category,
                                wineType = wineType,
                                vintage = vintageInt,
                                packagingType = packagingType.trim(),
                                producer = producer.trim(),
                                region = region.trim(),
                                grape = grape.trim(),
                                quantity = finalQuantity,
                                minQuantity = finalMinQuantity,
                                barcode = barcode.trim(),
                                location = location.ifBlank { "Adega Principal" }.trim(),
                                rating = rating,
                                drinkingWindowStart = dStart,
                                drinkingWindowEnd = dEnd,
                                price = priceDouble,
                                notes = notes.trim(),
                                expirationDateMillis = expMillis
                            )

                            onSave(resultItem)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                        modifier = Modifier.testTag("save_wine_button")
                    ) {
                        Text("Salvar Produto")
                    }
                }
            }
        }
    }

    if (showOnlineSearchDialog) {
        OnlineProductSearchDialog(
            initialQuery = name,
            onDismiss = { showOnlineSearchDialog = false },
            onSelectProduct = { selected ->
                name = selected.name
                nameError = false
                val detected = selected.category ?: com.example.data.OnlineProductLookupService.inferCategoryFromName(selected.name)
                if (detected != null) {
                    category = detected
                    if (initialWine == null) {
                        val catMin = categoryConfigs.find { it.categoryName.equals(detected, ignoreCase = true) }?.minQuantity ?: 2
                        minQuantityText = catMin.toString()
                    }
                }
                if (!selected.brand.isNullOrBlank() && producer.isBlank()) {
                    producer = selected.brand
                }
                if (!selected.barcode.isNullOrBlank()) {
                    barcode = selected.barcode
                }
                onlineSearchFeedback = "✅ Selecionado de loja online: ${selected.name}"
            }
        )
    }
}
