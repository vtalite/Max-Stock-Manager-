package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WineItem
import com.example.ui.ScanFeedback
import com.example.ui.ScanMode
import com.example.ui.components.BarcodeScannerView
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenContainer
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.WineBurgundyPrimary

@Composable
fun BarcodeScannerScreen(
    scanMode: ScanMode,
    scanFeedback: ScanFeedback?,
    sampleWines: List<WineItem>,
    onModeChanged: (ScanMode) -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onClearFeedback: () -> Unit,
    onRegisterNewWithBarcode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("barcode_scanner_screen")
    ) {
        // Top Title
        Text(
            text = "Leitor de Código de Barras",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = WineBurgundyPrimary
        )

        Text(
            text = "Escaneie a garrafa para dar baixa rápida por falta ou cadastrar novos produtos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Mode Selector: [ Dar Baixa ] vs [ Cadastrar Produto ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_mode_selector"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isConsume = scanMode == ScanMode.CONSUME
            Card(
                onClick = { onModeChanged(ScanMode.CONSUME) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConsume) StockRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isConsume) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isConsume) 4.dp else 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("scan_mode_consume_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dar Baixa / Falta",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val isRegister = scanMode == ScanMode.REGISTER
            Card(
                onClick = { onModeChanged(ScanMode.REGISTER) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRegister) WineBurgundyPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isRegister) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isRegister) 4.dp else 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("scan_mode_register_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cadastrar Produto",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Result Feedback Card
        AnimatedVisibility(visible = scanFeedback != null) {
            if (scanFeedback != null) {
                val containerColor = if (scanFeedback.isSuccess) StockGreenContainer else StockRedContainer
                val contentColor = if (scanFeedback.isSuccess) StockGreen else StockRed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("scan_feedback_card"),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (scanFeedback.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = scanFeedback.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }

                            IconButton(
                                onClick = onClearFeedback,
                                modifier = Modifier.testTag("clear_feedback_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar aviso",
                                    tint = contentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = scanFeedback.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (scanFeedback.wineItem == null) {
                            val codeToRegister = scanFeedback.scannedCode ?: scanFeedback.message.filter { it.isDigit() }
                            if (codeToRegister.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onRegisterNewWithBarcode(codeToRegister)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_new_from_feedback_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cadastrar Produto Escaneado ($codeToRegister)",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Camera Scanner View & Barcode Inputs
        BarcodeScannerView(
            onBarcodeDetected = onBarcodeDetected,
            sampleWines = sampleWines
        )
    }
}
