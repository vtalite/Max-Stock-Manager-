package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.WineItem
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val TAG = "PdfReportGenerator"
    private const val PAGE_WIDTH = 595  // A4 portrait width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 portrait height in points (72 dpi)
    private const val MARGIN = 36f

    // Theme Colors
    private val COLOR_PRIMARY = Color.rgb(114, 9, 39)       // Wine Burgundy
    private val COLOR_PRIMARY_DARK = Color.rgb(74, 4, 23)   // Dark Burgundy
    private val COLOR_TEXT_MAIN = Color.rgb(33, 33, 33)
    private val COLOR_TEXT_MUTED = Color.rgb(117, 117, 117)
    private val COLOR_BG_LIGHT = Color.rgb(248, 249, 250)
    private val COLOR_ROW_ALT = Color.rgb(243, 244, 246)
    private val COLOR_BORDER = Color.rgb(222, 226, 230)
    private val COLOR_ALERT_RED = Color.rgb(198, 40, 40)
    private val COLOR_ALERT_AMBER = Color.rgb(230, 81, 0)
    private val COLOR_SUCCESS_GREEN = Color.rgb(46, 125, 50)

    /**
     * Gera o relatório oficial de estoque em PDF para impressão ou compartilhamento.
     */
    fun generateInventoryPdf(
        context: Context,
        companyName: String,
        companyCnpj: String,
        databaseName: String,
        operatorName: String,
        filterDescription: String,
        wines: List<WineItem>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

            val titlePaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 14f
                color = Color.WHITE
            }

            val subtitlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = Color.rgb(240, 240, 240)
            }

            val textBold = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8.5f
                color = COLOR_TEXT_MAIN
            }

            val textRegular = Paint().apply {
                isAntiAlias = true
                textSize = 8f
                color = COLOR_TEXT_MAIN
            }

            val textMuted = Paint().apply {
                isAntiAlias = true
                textSize = 7.5f
                color = COLOR_TEXT_MUTED
            }

            val textAlertRed = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8f
                color = COLOR_ALERT_RED
            }

            val textAlertAmber = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8f
                color = COLOR_ALERT_AMBER
            }

            val linePaint = Paint().apply {
                color = COLOR_BORDER
                strokeWidth = 0.5f
            }

            val bgPaint = Paint()

            // Calculations
            val totalSkus = wines.size
            val totalUnits = wines.sumOf { it.quantity }
            val totalEstimatedValue = wines.sumOf { (it.price ?: 0.0) * it.quantity }
            val lowStockCount = wines.count { it.isLowStock || it.isOutOfStock }
            val nearExpCount = wines.count { it.isNearExpiration(30) || it.isExpired() }

            // Pagination setup
            val usableWidth = PAGE_WIDTH - (MARGIN * 2)
            val headerHeightPage1 = 92f
            val headerHeightSubsequent = 45f
            val tableHeaderHeight = 22f
            val rowHeight = 24f
            val footerHeight = 35f
            val summaryBlockHeight = 70f

            // Column coordinates
            val colX1 = MARGIN                        // Item / Produto (width ~180)
            val colX2 = colX1 + 185f                  // Categoria (width ~65)
            val colX3 = colX2 + 65f                   // Embalagem (width ~55)
            val colX4 = colX3 + 55f                   // Quantidade (width ~45)
            val colX5 = colX4 + 45f                   // Validade (width ~60)
            val colX6 = colX5 + 60f                   // Local / Nota (width ~60)
            val colX7 = colX6 + 60f                   // Preço / Total (width ~54)
            val colEnd = MARGIN + usableWidth

            var currentItemIndex = 0
            var pageNumber = 1
            val estimatedPages = if (wines.isEmpty()) 1 else {
                val firstPageCapacity = ((PAGE_HEIGHT - headerHeightPage1 - tableHeaderHeight - footerHeight) / rowHeight).toInt()
                val remainingItems = wines.size - firstPageCapacity
                if (remainingItems <= 0) 1 else {
                    val otherPageCapacity = ((PAGE_HEIGHT - headerHeightSubsequent - tableHeaderHeight - footerHeight) / rowHeight).toInt()
                    1 + kotlin.math.ceil(remainingItems.toDouble() / otherPageCapacity).toInt()
                }
            }

            while (currentItemIndex < wines.size || (wines.isEmpty() && pageNumber == 1)) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                var currentY = MARGIN

                // --- HEADER ---
                if (pageNumber == 1) {
                    // Header Banner
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(MARGIN, currentY, colEnd, currentY + 68f), 8f, 8f, bgPaint)

                    canvas.drawText("MAX BEBIDAS • RELATÓRIO GERAL DE ESTOQUE", MARGIN + 12f, currentY + 22f, titlePaint)
                    canvas.drawText("Empresa: $companyName   |   CNPJ: $companyCnpj", MARGIN + 12f, currentY + 38f, subtitlePaint)
                    canvas.drawText("Banco de Dados: $databaseName   |   Emissão: $dateStr   |   Operador: $operatorName", MARGIN + 12f, currentY + 50f, subtitlePaint)
                    canvas.drawText("Filtro Aplicado: $filterDescription", MARGIN + 12f, currentY + 62f, subtitlePaint)

                    currentY += 76f

                    // Stats strip
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRoundRect(RectF(MARGIN, currentY, colEnd, currentY + 22f), 4f, 4f, bgPaint)
                    linePaint.color = COLOR_BORDER
                    canvas.drawRect(RectF(MARGIN, currentY, colEnd, currentY + 22f), linePaint.apply { style = Paint.Style.STROKE })

                    val statPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 8f
                        color = COLOR_TEXT_MAIN
                    }
                    val statBold = Paint().apply {
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 8f
                        color = COLOR_PRIMARY
                    }

                    val statY = currentY + 14f
                    canvas.drawText("SKUs Cadastrados: ", MARGIN + 8f, statY, statPaint)
                    canvas.drawText("$totalSkus", MARGIN + 82f, statY, statBold)

                    canvas.drawText("Unidades Físicas: ", MARGIN + 120f, statY, statPaint)
                    canvas.drawText("$totalUnits", MARGIN + 195f, statY, statBold)

                    canvas.drawText("Alertas Estoque: ", MARGIN + 235f, statY, statPaint)
                    canvas.drawText("$lowStockCount", MARGIN + 300f, statY, if (lowStockCount > 0) textAlertRed else statBold)

                    canvas.drawText("Próx. Validade: ", MARGIN + 335f, statY, statPaint)
                    canvas.drawText("$nearExpCount", MARGIN + 395f, statY, if (nearExpCount > 0) textAlertAmber else statBold)

                    canvas.drawText("Valor Total: ", MARGIN + 430f, statY, statPaint)
                    canvas.drawText(currencyFormat.format(totalEstimatedValue), MARGIN + 475f, statY, statBold)

                    currentY += 28f
                } else {
                    // Subsequent page compact header
                    bgPaint.color = COLOR_PRIMARY
                    canvas.drawRoundRect(RectF(MARGIN, currentY, colEnd, currentY + 30f), 4f, 4f, bgPaint)

                    val compactTitle = Paint().apply {
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 10f
                        color = Color.WHITE
                    }
                    canvas.drawText("MAX BEBIDAS • Relatório de Estoque (Continuação)", MARGIN + 10f, currentY + 14f, compactTitle)
                    canvas.drawText("$companyName  |  Emissão: $dateStr  |  Filtro: $filterDescription", MARGIN + 10f, currentY + 24f, subtitlePaint)

                    currentY += 36f
                }

                // --- TABLE HEADER ---
                bgPaint.color = COLOR_PRIMARY_DARK
                canvas.drawRect(RectF(MARGIN, currentY, colEnd, currentY + tableHeaderHeight), bgPaint)

                val colHeaderPaint = Paint().apply {
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 8f
                    color = Color.WHITE
                }

                val thY = currentY + 14f
                canvas.drawText("PRODUTO / ITEM", colX1 + 4f, thY, colHeaderPaint)
                canvas.drawText("CATEGORIA", colX2 + 4f, thY, colHeaderPaint)
                canvas.drawText("EMBAL.", colX3 + 4f, thY, colHeaderPaint)
                canvas.drawText("QTD", colX4 + 4f, thY, colHeaderPaint)
                canvas.drawText("VALIDADE", colX5 + 4f, thY, colHeaderPaint)
                canvas.drawText("LOCAL", colX6 + 4f, thY, colHeaderPaint)
                canvas.drawText("PREÇO UN.", colX7 + 4f, thY, colHeaderPaint)

                currentY += tableHeaderHeight

                // --- TABLE ROWS ---
                val maxY = PAGE_HEIGHT - footerHeight - 10f
                var rowIndex = 0

                if (wines.isEmpty()) {
                    currentY += 20f
                    canvas.drawText("Nenhum item encontrado no estoque com os filtros selecionados.", MARGIN + 20f, currentY, textRegular)
                }

                while (currentItemIndex < wines.size && currentY + rowHeight <= maxY) {
                    val wine = wines[currentItemIndex]

                    // Row background
                    bgPaint.color = if (rowIndex % 2 == 0) Color.WHITE else COLOR_ROW_ALT
                    canvas.drawRect(RectF(MARGIN, currentY, colEnd, currentY + rowHeight), bgPaint)

                    // Bottom border
                    linePaint.color = COLOR_BORDER
                    linePaint.style = Paint.Style.STROKE
                    canvas.drawLine(MARGIN, currentY + rowHeight, colEnd, currentY + rowHeight, linePaint)

                    val textY = currentY + 14f

                    // 1. Produto / Item (Trimmed to fit ~180 pt)
                    val truncatedName = if (wine.name.length > 34) wine.name.substring(0, 31) + "..." else wine.name
                    canvas.drawText(truncatedName, colX1 + 4f, textY - 2f, textBold)
                    val subInfo = buildString {
                        if (wine.barcode.isNotBlank()) append("EAN: ${wine.barcode} ")
                        if (wine.producer.isNotBlank()) append("| ${wine.producer}")
                    }
                    if (subInfo.isNotBlank()) {
                        val truncatedSub = if (subInfo.length > 40) subInfo.substring(0, 37) + "..." else subInfo
                        canvas.drawText(truncatedSub, colX1 + 4f, textY + 7f, textMuted)
                    }

                    // 2. Categoria
                    val catText = if (wine.category.length > 12) wine.category.substring(0, 10) + ".." else wine.category
                    canvas.drawText(catText, colX2 + 4f, textY, textRegular)

                    // 3. Embalagem
                    val embText = wine.getPackagingTypeLabel()
                    val truncatedEmb = if (embText.length > 9) embText.substring(0, 8) + "." else embText
                    canvas.drawText(truncatedEmb, colX3 + 4f, textY, textMuted)

                    // 4. Quantidade com alerta de estoque
                    val qtyStr = "${wine.quantity}"
                    val qtyPaint = when {
                        wine.isOutOfStock -> textAlertRed
                        wine.isLowStock -> textAlertAmber
                        else -> textBold
                    }
                    canvas.drawText(qtyStr, colX4 + 4f, textY, qtyPaint)

                    // 5. Validade
                    val expDate = wine.getFormattedExpirationDate()
                    val expPaint = when {
                        wine.isExpired() -> textAlertRed
                        wine.isNearExpiration(30) -> textAlertAmber
                        else -> textRegular
                    }
                    canvas.drawText(expDate, colX5 + 4f, textY, expPaint)

                    // 6. Localização
                    val locText = if (wine.location.length > 11) wine.location.substring(0, 9) + ".." else wine.location
                    canvas.drawText(locText, colX6 + 4f, textY, textRegular)

                    // 7. Preço Un.
                    val priceStr = wine.price?.let { currencyFormat.format(it) } ?: "—"
                    canvas.drawText(priceStr, colX7 + 4f, textY, textRegular)

                    currentY += rowHeight
                    rowIndex++
                    currentItemIndex++
                }

                // Check if this is the last page and we have room for summary block
                val isLastPage = currentItemIndex >= wines.size
                if (isLastPage && currentY + summaryBlockHeight <= maxY) {
                    currentY += 12f
                    bgPaint.color = COLOR_BG_LIGHT
                    canvas.drawRoundRect(RectF(MARGIN, currentY, colEnd, currentY + 46f), 4f, 4f, bgPaint)
                    linePaint.color = COLOR_BORDER
                    canvas.drawRoundRect(RectF(MARGIN, currentY, colEnd, currentY + 46f), 4f, 4f, linePaint.apply { style = Paint.Style.STROKE })

                    val sumTitlePaint = Paint().apply {
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 9f
                        color = COLOR_PRIMARY
                    }
                    canvas.drawText("RESUMO CONSOLIDADO DO ESTOQUE ATUAL", MARGIN + 10f, currentY + 14f, sumTitlePaint)

                    val summaryLine1 = "Total de SKUs: $totalSkus | Unidades Físicas: $totalUnits | Valor Total em Estoque: ${currencyFormat.format(totalEstimatedValue)}"
                    val summaryLine2 = "Itens em Alerta de Estoque: $lowStockCount | Itens Próximos do Vencimento/Vencidos: $nearExpCount"
                    canvas.drawText(summaryLine1, MARGIN + 10f, currentY + 28f, textRegular)
                    canvas.drawText(summaryLine2, MARGIN + 10f, currentY + 40f, textMuted)
                }

                // --- FOOTER ---
                val footerY = PAGE_HEIGHT - MARGIN + 14f
                linePaint.color = COLOR_BORDER
                canvas.drawLine(MARGIN, PAGE_HEIGHT - MARGIN, colEnd, PAGE_HEIGHT - MARGIN, linePaint)

                canvas.drawText("Max Bebidas & Estoque • Sistema Integrado de Controle de Adegas", MARGIN, footerY, textMuted)
                val pageStr = "Página $pageNumber"
                val pageStrWidth = textMuted.measureText(pageStr)
                canvas.drawText(pageStr, colEnd - pageStrWidth, footerY, textMuted)

                pdfDocument.finishPage(page)
                pageNumber++
            }

            // Save PDF File in app's cache directory
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val fileName = "Relatorio_Estoque_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val pdfFile = File(reportsDir, fileName)
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            Log.d(TAG, "PDF de estoque gerado com sucesso: ${pdfFile.absolutePath} (${pdfFile.length()} bytes)")
            return pdfFile
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar PDF de estoque", e)
            return null
        }
    }

    /**
     * Abre o arquivo PDF para visualização ou impressão direta.
     */
    fun openOrPrintPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            val chooser = Intent.createChooser(viewIntent, "Visualizar / Imprimir Relatório de Estoque")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir PDF", e)
            Toast.makeText(context, "Erro ao abrir PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Compartilha o arquivo PDF gerado via WhatsApp, E-mail, Drive, etc.
     */
    fun sharePdf(
        context: Context,
        pdfFile: File,
        companyName: String,
        itemCount: Int
    ) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório de Estoque - $companyName")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Segue anexo o Relatório de Estoque consolidado de $companyName com $itemCount produtos cadastrados."
                )
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Relatório de Estoque via:")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao compartilhar PDF", e)
            Toast.makeText(context, "Erro ao compartilhar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
