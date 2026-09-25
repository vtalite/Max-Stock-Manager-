package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.WineItem
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ExpirationBadge
import com.example.ui.theme.StockAmber
import com.example.ui.theme.StockAmberContainer
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.WineBurgundyPrimary

@Composable
fun AlertsScreen(
    lowStockWines: List<WineItem>,
    outOfStockWines: List<WineItem>,
    nearExpirationWines: List<WineItem> = emptyList(),
    onRestock: (WineItem, Int) -> Unit,
    onConsume: ((WineItem) -> Unit)? = null,
    onWineClick: (WineItem) -> Unit,
    onNavigateToExpirationTab: () -> Unit = {},
    onOpenCategoryConfigTable: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allAlerts = outOfStockWines + lowStockWines + nearExpirationWines

    // Grouping by category
    val outOfStockByCategory = remember(outOfStockWines) {
        outOfStockWines.groupBy { it.category }
    }
    val lowStockByCategory = remember(lowStockWines) {
        lowStockWines.groupBy { it.category }
    }

    // Expandable states for category cards in each alert section (default all expanded = true)
    val expandedOutOfStock = remember(outOfStockByCategory.keys) {
        mutableStateMapOf<String, Boolean>().apply {
            outOfStockByCategory.keys.forEach { put(it, true) }
        }
    }
    val expandedLowStock = remember(lowStockByCategory.keys) {
        mutableStateMapOf<String, Boolean>().apply {
            lowStockByCategory.keys.forEach { put(it, true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("alerts_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header Actions Row: Expandir/Recolher Todos + Compartilhar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val allLowExpandedGlobal = lowStockByCategory.keys.all { expandedLowStock[it] != false }
            OutlinedButton(
                onClick = {
                    val newState = !allLowExpandedGlobal
                    lowStockByCategory.keys.forEach { cat -> expandedLowStock[cat] = newState }
                    outOfStockByCategory.keys.forEach { cat -> expandedOutOfStock[cat] = newState }
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("toggle_expand_collapse_alerts")
            ) {
                Icon(
                    imageVector = if (allLowExpandedGlobal) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (allLowExpandedGlobal) "Recolher Categorias" else "Expandir Categorias",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (allAlerts.isNotEmpty()) {
                IconButtonWithLabel(
                    onClick = {
                        val shareText = buildString {
                            appendLine("📦 *Relatório de Controle de Estoque e Validade* 📦")
                            appendLine()
                            if (nearExpirationWines.isNotEmpty()) {
                                appendLine("⏰ *PRÓXIMOS À VALIDADE / VENCIDOS:*")
                                nearExpirationWines.forEach { w ->
                                    val days = w.getDaysUntilExpiration() ?: 0
                                    val statusStr = if (days < 0) "VENCIDO" else "Vence em ${days}d"
                                    appendLine("• ${w.name} (${w.category}) - $statusStr [Validade: ${w.getFormattedExpirationDate()}]")
                                }
                                appendLine()
                            }
                            if (outOfStockWines.isNotEmpty()) {
                                appendLine("🚨 *FORA DE ESTOQUE (0 un):*")
                                outOfStockByCategory.forEach { (cat, list) ->
                                    appendLine("📁 Categoria: $cat (${list.size} itens)")
                                    list.forEach { w ->
                                        appendLine("   • ${w.name} (EAN: ${w.barcode.ifBlank { "Sem EAN" }})")
                                    }
                                }
                                appendLine()
                            }
                            if (lowStockWines.isNotEmpty()) {
                                appendLine("⚠️ *ESTOQUE MÍNIMO / REPOSIÇÃO:*")
                                lowStockByCategory.forEach { (cat, list) ->
                                    appendLine("📁 Categoria: $cat (${list.size} itens)")
                                    list.forEach { w ->
                                        appendLine("   • ${w.name} - Atual: ${w.quantity} un (Mín: ${w.minQuantity})")
                                    }
                                }
                            }
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Alertas"))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (allAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Estoque e Validades em Dia!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nenhum produto fora de estoque, abaixo do mínimo ou vencendo em breve.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section 0: Near Expiration Link Banner
                if (nearExpirationWines.isNotEmpty()) {
                    item {
                        Card(
                            onClick = onNavigateToExpirationTab,
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("banner_link_near_expiration_tab")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Aba de Produtos Próximos ao Vencimento",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "Ver todos os ${nearExpirationWines.size} itens com busca por EAN e leitor ➔",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Acessar",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Section 0 List: Near Expiration
                    item {
                        AlertSectionHeader(
                            title = "⏰ Produtos Próximos à Validade",
                            count = nearExpirationWines.size,
                            color = Color(0xFFD97706),
                            containerColor = Color(0xFFFEF3C7),
                            actionText = "Ver todos",
                            onActionClick = onNavigateToExpirationTab
                        )
                    }

                    items(nearExpirationWines, key = { "exp_${it.id}" }) { wine ->
                        CompactAlertProductCard(
                            wine = wine,
                            alertType = AlertCardType.EXPIRATION,
                            onConsume = { onConsume?.invoke(wine) },
                            onRestock = { onRestock(wine, 1) },
                            onWineClick = { onWineClick(wine) }
                        )
                    }
                }

                // Section 1: Fora de Estoque (Agrupado por Categoria e Expansível)
                if (outOfStockWines.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        AlertSectionHeader(
                            title = "🚨 Fora de Estoque (0 unidades)",
                            count = outOfStockWines.size,
                            color = StockRed,
                            containerColor = StockRedContainer,
                            actionText = "Expandir/Recolher",
                            onActionClick = {
                                val allOpen = outOfStockByCategory.keys.all { expandedOutOfStock[it] == true }
                                outOfStockByCategory.keys.forEach { expandedOutOfStock[it] = !allOpen }
                            }
                        )
                    }

                    outOfStockByCategory.forEach { (categoryName, products) ->
                        item(key = "out_cat_header_$categoryName") {
                            val isExpanded = expandedOutOfStock[categoryName] ?: true
                            ExpandableCategoryHeader(
                                categoryName = categoryName,
                                count = products.size,
                                isExpanded = isExpanded,
                                color = StockRed,
                                onToggle = {
                                    expandedOutOfStock[categoryName] = !isExpanded
                                }
                            )
                        }

                        if (expandedOutOfStock[categoryName] != false) {
                            items(products, key = { "out_${it.id}" }) { wine ->
                                CompactAlertProductCard(
                                    wine = wine,
                                    alertType = AlertCardType.OUT_OF_STOCK,
                                    onConsume = null,
                                    onRestock = { onRestock(wine, 1) },
                                    onWineClick = { onWineClick(wine) }
                                )
                            }
                        }
                    }
                }

                // Section 2: Estoque Mínimo (Agrupado por Categoria e Expansível)
                if (lowStockWines.isNotEmpty()) {
                    item {
                        val allLowExpanded = lowStockByCategory.keys.all { expandedLowStock[it] != false }
                        Spacer(modifier = Modifier.height(4.dp))
                        AlertSectionHeader(
                            title = "⚠️ Estoque Mínimo / Reposição",
                            count = lowStockWines.size,
                            color = StockAmber,
                            containerColor = StockAmberContainer,
                            actionText = if (allLowExpanded) "Recolher Todos" else "Expandir Todos",
                            onActionClick = {
                                val newState = !allLowExpanded
                                lowStockByCategory.keys.forEach { cat -> expandedLowStock[cat] = newState }
                            }
                        )
                    }

                    lowStockByCategory.forEach { (categoryName, products) ->
                        item(key = "low_cat_header_$categoryName") {
                            val isExpanded = expandedLowStock[categoryName] ?: true
                            ExpandableCategoryHeader(
                                categoryName = categoryName,
                                count = products.size,
                                isExpanded = isExpanded,
                                color = StockAmber,
                                onToggle = {
                                    expandedLowStock[categoryName] = !isExpanded
                                }
                            )
                        }

                        if (expandedLowStock[categoryName] != false) {
                            items(products, key = { "low_${it.id}" }) { wine ->
                                CompactAlertProductCard(
                                    wine = wine,
                                    alertType = AlertCardType.LOW_STOCK,
                                    onConsume = { onConsume?.invoke(wine) },
                                    onRestock = { onRestock(wine, 1) },
                                    onWineClick = { onWineClick(wine) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableCategoryHeader(
    categoryName: String,
    count: Int,
    isExpanded: Boolean,
    color: Color,
    onToggle: () -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("category_alert_group_$categoryName")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(category = categoryName)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$categoryName ($count produto${if (count != 1) "s" else ""})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpanded) "Recolher" else "Expandir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = color,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationState)
                )
            }
        }
    }
}

enum class AlertCardType {
    EXPIRATION,
    OUT_OF_STOCK,
    LOW_STOCK
}

@Composable
private fun CompactAlertProductCard(
    wine: WineItem,
    alertType: AlertCardType,
    onConsume: (() -> Unit)?,
    onRestock: () -> Unit,
    onWineClick: () -> Unit
) {
    val days = wine.getDaysUntilExpiration() ?: 0
    val isExpired = days < 0

    val containerBg = when (alertType) {
        AlertCardType.EXPIRATION -> if (isExpired) StockRedContainer.copy(alpha = 0.45f) else StockAmberContainer.copy(alpha = 0.45f)
        AlertCardType.OUT_OF_STOCK -> StockRedContainer.copy(alpha = 0.35f)
        AlertCardType.LOW_STOCK -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }

    val borderColor = when (alertType) {
        AlertCardType.EXPIRATION -> if (isExpired) StockRed.copy(alpha = 0.35f) else StockAmber.copy(alpha = 0.35f)
        AlertCardType.OUT_OF_STOCK -> StockRed.copy(alpha = 0.35f)
        AlertCardType.LOW_STOCK -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWineClick() }
            .testTag("compact_alert_card_${wine.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Row: Category Badge + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "#${wine.id}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WineBurgundyPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    CategoryBadge(category = wine.category)
                }

                when (alertType) {
                    AlertCardType.EXPIRATION -> ExpirationBadge(wine = wine)
                    AlertCardType.OUT_OF_STOCK -> {
                        Surface(
                            color = StockRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ESGOTADO (0 un)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    AlertCardType.LOW_STOCK -> {
                        Surface(
                            color = StockAmber,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "REPOR ESTOQUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product Name
            Text(
                text = wine.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Estoque em destaque (Localização removida conforme solicitado)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📦", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Estoque Atual: ${wine.quantity} ${wine.getEffectivePackagingUnit(wine.quantity != 1)} (Mínimo: ${wine.minQuantity})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (wine.quantity <= wine.minQuantity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expiration / EAN Info & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (wine.hasExpirationDate) {
                    Text(
                        text = "Validade: ${wine.getFormattedExpirationDate()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (wine.barcode.isNotBlank()) {
                    Text(
                        text = "EAN: ${wine.barcode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "ID: #${wine.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onConsume != null && wine.quantity > 0) {
                        OutlinedButton(
                            onClick = onConsume,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("alert_consume_${wine.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Baixa", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Button(
                        onClick = onRestock,
                        colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("alert_restock_${wine.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+1 Repor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButtonWithLabel(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.testTag("share_shopping_list_button")
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Compartilhar",
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "Compartilhar",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AlertSectionHeader(
    title: String,
    count: Int,
    color: Color,
    containerColor: Color,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$title ($count)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            if (actionText != null && onActionClick != null) {
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "$actionText ➔",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}
