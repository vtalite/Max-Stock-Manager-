package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VintageStatus
import com.example.data.WineItem
import com.example.ui.theme.WineBurgundyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WineDetailSheet(
    wine: WineItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustQuantity: (Int) -> Unit
) {
    // State for interactive icon resizing when clicking on it
    var isIconExpanded by remember { mutableStateOf(false) }

    // Dialog state for action confirmation
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val heroContainerSize by animateDpAsState(
        targetValue = if (isIconExpanded) 84.dp else 56.dp,
        animationSpec = spring(),
        label = "hero_container_size"
    )
    val heroIconSize by animateDpAsState(
        targetValue = if (isIconExpanded) 48.dp else 30.dp,
        animationSpec = spring(),
        label = "hero_icon_size"
    )

    val (categoryBgColor, categoryIconColor) = getCategoryContainerColor(wine.category)
    val categoryIcon = getCategoryIcon(wine.category)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .testTag("wine_detail_sheet")
        ) {
            // Header Row: Category Badge, Wine Type & Actions (Edit / Delete / Close)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryBadge(category = wine.category)
                    WineTypeBadge(type = wine.wineType)
                }

                // Resized and comfortably padded action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit action button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onEdit)
                            .testTag("edit_wine_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Produto",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Delete action button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDelete)
                            .testTag("delete_wine_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Produto",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Dismiss/Close button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss)
                            .testTag("close_wine_detail_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Product Hero Header with Click-to-Resize Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Product Category Hero Icon (Clicking toggles resize between 56dp and 84dp)
                Box(
                    modifier = Modifier
                        .size(heroContainerSize)
                        .clip(RoundedCornerShape(18.dp))
                        .background(categoryBgColor)
                        .border(1.5.dp, categoryIconColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .clickable { isIconExpanded = !isIconExpanded }
                        .testTag("product_hero_icon_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = "${wine.category} - Toque para redimensionar o ícone",
                        tint = categoryIconColor,
                        modifier = Modifier.size(heroIconSize)
                    )

                    // Zoom indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIconExpanded) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = categoryIconColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wine.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (wine.price != null && wine.price > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF2E7D32).copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "R$ %.2f".format(wine.price),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (wine.producer.isNotBlank()) {
                        Text(
                            text = wine.producer,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clickable hint showing current icon size state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isIconExpanded = !isIconExpanded }
                    ) {
                        Icon(
                            imageVector = if (isIconExpanded) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = categoryIconColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isIconExpanded) "Ícone ampliado (84dp) • Toque p/ reduzir" else "Toque no ícone p/ ampliar",
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryIconColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Resized & Highlighted Stock Control Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // "Estoque Atual" Section Header with full horizontal width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = WineBurgundyPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = "Estoque Atual",
                                        modifier = Modifier.size(20.dp),
                                        tint = WineBurgundyPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Estoque Atual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Saldo físico em depósito",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        StockStatusPill(
                            quantity = wine.quantity,
                            minQuantity = wine.minQuantity,
                            unitName = wine.getEffectivePackagingUnit(plural = wine.quantity != 1),
                            productType = wine.getPackagingTypeLabel()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo reposicionado que informa a quantidade de itens com alta legibilidade
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WineBurgundyPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Quantidade de Itens",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = WineBurgundyPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Tipo: ${wine.getPackagingTypeLabel()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = WineBurgundyPrimary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${wine.quantity}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (wine.quantity <= wine.minQuantity) MaterialTheme.colorScheme.error else WineBurgundyPrimary,
                                    fontSize = 34.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${wine.getEffectivePackagingUnit(plural = wine.quantity != 1)} disponíveis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons with prominent product type label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAdjustQuantity(-1) },
                            enabled = wine.quantity > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("detail_decrement_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Dar Baixa",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dar Baixa",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { onAdjustQuantity(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("detail_increment_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar ${wine.getPackagingTypeLabel()}",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Adicionar (${wine.getPackagingTypeLabel()})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resized Product Descriptions Section with Dedicated Section Header & Balanced Spacing
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = WineBurgundyPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = WineBurgundyPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Descrições do Produto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Details List with Redimensioned Spacing between Attribute Items (12dp)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo Estoque Atual em destaque nas descrições do produto
                DetailRow(
                    icon = Icons.Default.Inventory2,
                    label = "Estoque Atual",
                    value = "${wine.quantity} ${wine.getEffectivePackagingUnit(plural = wine.quantity != 1)} (Saldo Físico)",
                    iconTint = if (wine.quantity <= wine.minQuantity) MaterialTheme.colorScheme.error else WineBurgundyPrimary
                )

                DetailRow(
                    icon = Icons.Default.Category,
                    label = "Categoria",
                    value = wine.category,
                    iconTint = categoryIconColor
                )

                DetailRow(
                    icon = Icons.Default.Inventory2,
                    label = "Tipo do Produto / Embalagem",
                    value = wine.getPackagingTypeLabel(),
                    iconTint = categoryIconColor
                )

                DetailRow(
                    icon = Icons.Default.Warning,
                    label = "Limite Mínimo de Estoque",
                    value = "${wine.minQuantity} ${wine.getEffectivePackagingUnit(plural = wine.minQuantity != 1)}",
                    iconTint = if (wine.quantity <= wine.minQuantity) MaterialTheme.colorScheme.error else WineBurgundyPrimary
                )

                if (wine.hasExpirationDate) {
                    val days = wine.getDaysUntilExpiration() ?: 0
                    val expStatusStr = when {
                        days < 0 -> "Vencido (${-days} dias atrás)"
                        days == 0 -> "Vence Hoje!"
                        days <= 30 -> "Vence em $days dias"
                        else -> "Dentro do prazo"
                    }
                    val expColor = if (days <= 30) MaterialTheme.colorScheme.error else WineBurgundyPrimary
                    DetailRow(
                        icon = Icons.Default.Event,
                        label = "Data de Validade",
                        value = "${wine.getFormattedExpirationDate()} ($expStatusStr)",
                        iconTint = expColor
                    )
                }

                if (wine.region.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Default.Public,
                        label = "Região / Origem",
                        value = wine.region
                    )
                }

                if (wine.grape.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Default.Spa,
                        label = "Uvas / Castas",
                        value = wine.grape
                    )
                }

                if (wine.location.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Localização na Adega",
                        value = wine.location
                    )
                }

                if (wine.barcode.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Outlined.QrCode,
                        label = "EAN",
                        value = wine.barcode
                    )
                }

                if (wine.price != null && wine.price > 0) {
                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Preço Estimado",
                        value = "R$ %.2f".format(wine.price),
                        iconTint = Color(0xFF2E7D32)
                    )
                }

                if (wine.rating > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFB300).copy(alpha = 0.15f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Avaliação Pessoal",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFFFB300)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Avaliação Pessoal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f / 5.0".format(wine.rating),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (wine.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = WineBurgundyPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = WineBurgundyPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Notas de Degustação & Harmonização",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WineBurgundyPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = wine.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 44.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color = WineBurgundyPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(19.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Vinhos" -> Icons.Default.WineBar
        "Cerveja" -> Icons.Default.SportsBar
        "Destilados" -> Icons.Default.LocalBar
        "Sucos e Agua" -> Icons.Default.WaterDrop
        "Mercearia" -> Icons.Default.ShoppingBag
        "Congelados" -> Icons.Default.AcUnit
        "Gelos" -> Icons.Default.AcUnit
        else -> Icons.Default.Inventory2
    }
}

fun getCategoryContainerColor(category: String): Pair<Color, Color> {
    return when (category) {
        "Vinhos" -> Color(0xFF722F37).copy(alpha = 0.15f) to Color(0xFF722F37)
        "Cerveja" -> Color(0xFFD97706).copy(alpha = 0.15f) to Color(0xFFD97706)
        "Destilados" -> Color(0xFF7C2D12).copy(alpha = 0.15f) to Color(0xFF7C2D12)
        "Sucos e Agua" -> Color(0xFF0284C7).copy(alpha = 0.15f) to Color(0xFF0284C7)
        "Mercearia" -> Color(0xFF0D9488).copy(alpha = 0.15f) to Color(0xFF0D9488)
        "Congelados" -> Color(0xFF2563EB).copy(alpha = 0.15f) to Color(0xFF2563EB)
        "Gelos" -> Color(0xFF0284C7).copy(alpha = 0.15f) to Color(0xFF0284C7)
        else -> WineBurgundyPrimary.copy(alpha = 0.15f) to WineBurgundyPrimary
    }
}

