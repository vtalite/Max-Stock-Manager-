package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VintageStatus
import com.example.data.WineItem
import com.example.ui.theme.PortWineColor
import com.example.ui.theme.RedWineColor
import com.example.ui.theme.RoseWineColor
import com.example.ui.theme.SparklingWineColor
import com.example.ui.theme.StockAmber
import com.example.ui.theme.StockAmberContainer
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenContainer
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.WineBurgundyPrimary
import com.example.ui.theme.WhiteWineColor

@Composable
fun WineItemCard(
    wine: WineItem,
    onWineClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maturityStatus = wine.getVintageMaturityStatus()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onWineClick() }
            .testTag("wine_card_${wine.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Category Tag, Type Tag, Expiration Badge
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

                if (wine.hasExpirationDate) {
                    ExpirationBadge(wine = wine)
                } else if (wine.category == "Vinhos") {
                    VintageMaturityBadge(status = maturityStatus)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wine Title, Price & Producer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (wine.price != null && wine.price > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "R$ %.2f".format(wine.price),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (wine.producer.isNotBlank() || wine.region.isNotBlank()) {
                val subtitle = listOfNotNull(
                    wine.producer.ifBlank { null },
                    wine.region.ifBlank { null },
                    wine.grape.ifBlank { null }
                ).joinToString(" • ")

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info row: Location, Rating, Barcode badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (wine.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Localização",
                                modifier = Modifier.size(16.dp),
                                tint = WineBurgundyPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = wine.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (wine.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Avaliação",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFB300)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(wine.rating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (wine.barcode.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode,
                            contentDescription = "EAN",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EAN: ${wine.barcode}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Stock status & Quick adjustment controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Status Pill with informative packaging unit
                StockStatusPill(
                    quantity = wine.quantity,
                    minQuantity = wine.minQuantity,
                    unitName = wine.getEffectivePackagingUnit(plural = wine.quantity != 1),
                    productType = wine.getPackagingTypeLabel()
                )

                // - / + Quantity Adjuster
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = wine.quantity > 0,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("decrement_button_${wine.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = WineBurgundyPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir estoque",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${wine.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("increment_button_${wine.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = WineBurgundyPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar estoque",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val (bgColor, textColor) = when (category) {
        "Vinhos" -> Color(0xFF722F37) to Color.White
        "Cerveja" -> Color(0xFFD97706) to Color.White
        "Destilados" -> Color(0xFF7C2D12) to Color.White
        "Sucos e Agua" -> Color(0xFF0284C7) to Color.White
        "Mercearia" -> Color(0xFF0D9488) to Color.White
        "Congelados" -> Color(0xFF2563EB) to Color.White
        "Gelos" -> Color(0xFF0284C7) to Color.White
        else -> WineBurgundyPrimary to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = category,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ExpirationBadge(wine: WineItem) {
    val days = wine.getDaysUntilExpiration() ?: return
    val (bgColor, textColor, label) = when {
        days < 0 -> Triple(StockRedContainer, StockRed, "Vencido (${-days}d atrás)")
        days <= 7 -> Triple(StockRedContainer, StockRed, "Vence em ${days}d!")
        days <= 30 -> Triple(StockAmberContainer, StockAmber, "Vence em ${days}d")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, wine.getFormattedExpirationDate())
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            if (days <= 30) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Aviso Validade",
                    tint = textColor,
                    modifier = Modifier.size(12.dp).padding(end = 2.dp)
                )
            }
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WineTypeBadge(type: String) {
    val (bgColor, textColor) = when (type.lowercase()) {
        "tinto" -> RedWineColor to Color.White
        "branco" -> WhiteWineColor to Color.Black
        "rosé", "rose" -> RoseWineColor to Color.White
        "espumante" -> SparklingWineColor to Color.Black
        "porto / fortificado", "porto", "fortificado" -> PortWineColor to Color.White
        else -> WineBurgundyPrimary to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = type,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun VintageMaturityBadge(status: VintageStatus) {
    val (bgColor, textColor) = when (status) {
        VintageStatus.PEAK_DRINKING -> Color(0xFF2E7D32).copy(alpha = 0.15f) to Color(0xFF2E7D32)
        VintageStatus.IN_CELLAR_GUARD -> Color(0xFF1976D2).copy(alpha = 0.15f) to Color(0xFF1976D2)
        VintageStatus.PAST_PEAK -> Color(0xFFD32F2F).copy(alpha = 0.15f) to Color(0xFFD32F2F)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StockStatusPill(
    quantity: Int,
    minQuantity: Int,
    unitName: String = "",
    productType: String = ""
) {
    val unitSuffix = if (unitName.isNotBlank()) " $unitName" else ""
    val (bgColor, textColor, label, showWarning) = when {
        quantity == 0 -> Quadruple(StockRedContainer, StockRed, "Fora de Estoque (0$unitSuffix)", true)
        quantity <= minQuantity -> Quadruple(StockAmberContainer, StockAmber, "Estoque Baixo ($quantity$unitSuffix)", true)
        else -> {
            val displayUnit = if (unitName.isNotBlank()) {
                unitName.replaceFirstChar { it.uppercase() }
            } else {
                "Unidades"
            }
            Quadruple(StockGreenContainer, StockGreen, "$quantity $displayUnit", false)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        if (showWarning) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Aviso de estoque",
                tint = textColor,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 3.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }

        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
