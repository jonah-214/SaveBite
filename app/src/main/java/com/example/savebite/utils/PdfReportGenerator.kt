package com.example.savebite.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.savebite.ui.viewmodel.ReportUiState
import com.example.savebite.ui.viewmodel.TimeFrame
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSharePdf(context: Context, state: ReportUiState) {
        val pdfDocument = PdfDocument()

        val pageWidth = 595

        val topWastedList = state.topWastedItems.take(5)
        val reasonList = state.reasonBreakdowns
        val consumedList = state.topConsumedItems.take(5)

        val topItemsCount = topWastedList.size
        val reasonsCount = reasonList.size
        val consumedCount = consumedList.size

        val wastedItemsCardHeight = 36f + (topItemsCount * 24f).coerceAtLeast(28f)
        val reasonsCardHeight = 36f + (reasonsCount * 24f).coerceAtLeast(28f)
        val consumedCardHeight = 36f + (consumedCount * 24f).coerceAtLeast(28f)
        val breakdownCardHeight = 150f

        val contentCalculatedHeight = 32f + 32f + 14f + 55f + 14f + breakdownCardHeight + 14f +
                wastedItemsCardHeight + 14f + reasonsCardHeight + 14f + consumedCardHeight + 40f

        val pageHeight = contentCalculatedHeight.toInt().coerceAtLeast(842)

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Color Palette
        val primaryGreen = Color.parseColor("#55823B")
        val bgCard = Color.parseColor("#F7F9F6")
        val borderCard = Color.parseColor("#E0E5DD")
        val textDark = Color.parseColor("#1F241E")
        val textMuted = Color.parseColor("#6C756B")

        val categoryColorsMap = mapOf(
            "Dairy & Eggs" to Color.parseColor("#55823B"),
            "Produce" to Color.parseColor("#ECA338"),
            "Meat & Seafood" to Color.parseColor("#D32F2F"),
            "Bakery & Bread" to Color.parseColor("#8D6E63"),
            "Beverages" to Color.parseColor("#0288D1"),
            "Pantry & Dry Goods" to Color.parseColor("#795548"),
            "Frozen Foods" to Color.parseColor("#3949AB"),
            "Snacks & Sweets" to Color.parseColor("#C2185B"),
            "Condiments & Sauces" to Color.parseColor("#00796B"),
            "Canned Goods" to Color.parseColor("#607D8B"),
            "Leftovers & Prepared" to Color.parseColor("#8BC34A"),
            "Spices & Baking" to Color.parseColor("#FF5722"),
            "Dairy" to Color.parseColor("#5C6BC0")
        )

        val categoryColorsList = listOf(
            primaryGreen, Color.parseColor("#ECA338"), Color.parseColor("#4A84C4"), Color.parseColor("#8E63B4"),
            Color.parseColor("#D32F2F"), Color.parseColor("#8D6E63"), Color.parseColor("#0288D1"),
            Color.parseColor("#795548"), Color.parseColor("#3949AB"), Color.parseColor("#C2185B"),
            Color.parseColor("#00796B"), Color.parseColor("#607D8B"), Color.parseColor("#8BC34A"),
            Color.parseColor("#FF5722")
        )

        fun getPdfCategoryColor(category: String, index: Int): Int {
            return categoryColorsMap[category] ?: categoryColorsList[index % categoryColorsList.size]
        }

        var currentY = 32f
        val startX = 36f
        val contentWidth = pageWidth - (startX * 2)

        // Title
        paint.color = primaryGreen
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Food Waste & Consumption Report", startX, currentY + 16f, paint)

        // 根据 selectedTimeFrame 组装 Date 显示字符串
        val dateDisplay = when (state.selectedTimeFrame) {
            TimeFrame.WEEKLY -> "Weekly Report: ${state.dateDisplay}"
            TimeFrame.MONTHLY -> "Monthly Report: ${state.dateDisplay}"
            TimeFrame.YEARLY -> "Yearly Report: ${state.dateDisplay}"
        }

        paint.color = textMuted
        paint.textSize = 11f
        paint.isFakeBoldText = false
        val dateWidth = paint.measureText(dateDisplay)
        canvas.drawText(dateDisplay, pageWidth - startX - dateWidth, currentY + 14f, paint)

        currentY += 32f

        paint.color = borderCard
        paint.strokeWidth = 1f
        canvas.drawLine(startX, currentY, pageWidth - startX, currentY, paint)

        currentY += 14f

        val cardGap = 12f
        val metricCardWidth = (contentWidth - cardGap) / 2f
        val metricCardHeight = 55f

        // Card 1: Waste Cost
        drawRoundedRect(canvas, startX, currentY, metricCardWidth, metricCardHeight, 10f, bgCard, borderCard)
        paint.color = textMuted
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Waste Cost", startX + 14f, currentY + 18f, paint)
        paint.color = textDark
        paint.textSize = 15f
        paint.isFakeBoldText = true
        val wastedCostText = Currency.format(state.totalWastedCost)
        canvas.drawText(wastedCostText, startX + 14f, currentY + 40f, paint)

        // Card 2: Saved Value
        val card2X = startX + metricCardWidth + cardGap
        drawRoundedRect(canvas, card2X, currentY, metricCardWidth, metricCardHeight, 10f, bgCard, borderCard)
        paint.color = textMuted
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Saved Value", card2X + 14f, currentY + 18f, paint)
        paint.color = primaryGreen
        paint.textSize = 15f
        paint.isFakeBoldText = true
        val savedText = Currency.format(state.totalSavedCost)
        canvas.drawText(savedText, card2X + 14f, currentY + 40f, paint)

        currentY += metricCardHeight + 14f

        // Waste Breakdown Card
        drawRoundedRect(canvas, startX, currentY, contentWidth, breakdownCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = textDark
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Waste Breakdown", startX + 14f, currentY + 24f, paint)

        if (state.totalWastedItems > 0 && state.wastedBreakdowns.isNotEmpty()) {
            val chartCenterX = startX + 70f
            val chartCenterY = currentY + 90f
            val chartRadius = 40f
            val rectF = RectF(
                chartCenterX - chartRadius,
                chartCenterY - chartRadius,
                chartCenterX + chartRadius,
                chartCenterY + chartRadius
            )

            var startAngle = -90f
            state.wastedBreakdowns.forEachIndexed { index, item ->
                if (item.percentage > 0f) {
                    val sweepAngle = (item.percentage / 100f) * 360f
                    paint.color = getPdfCategoryColor(item.category, index)
                    paint.style = Paint.Style.FILL
                    canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                    startAngle += sweepAngle
                }
            }

            paint.color = Color.WHITE
            canvas.drawCircle(chartCenterX, chartCenterY, 18f, paint)

            val legendStartX1 = startX + 145f
            val legendStartX2 = startX + 330f
            var legendY = currentY + 50f

            state.wastedBreakdowns.forEachIndexed { index, item ->
                val posX = if (index % 2 == 0) legendStartX1 else legendStartX2
                val posY = legendY + ((index / 2) * 20f)

                if (posY < currentY + breakdownCardHeight - 10f) {
                    paint.color = getPdfCategoryColor(item.category, index)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(posX, posY - 4f, 4f, paint)

                    paint.color = textDark
                    paint.textSize = 10f
                    paint.isFakeBoldText = true
                    canvas.drawText(item.category, posX + 10f, posY, paint)

                    paint.color = textMuted
                    paint.textSize = 9f
                    paint.isFakeBoldText = false
                    canvas.drawText("${item.count} items (${item.percentage.toInt()}%)", posX + 105f, posY, paint)
                }
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No waste data recorded", startX + 14f, currentY + 75f, paint)
        }

        currentY += breakdownCardHeight + 14f

        val colNameX = startX + 14f
        val colBarStartX = startX + 110f
        val barMaxWidth = 180f
        val cardRightPadding = 14f
        val colRightAlignX = startX + contentWidth - cardRightPadding

        // 1. Most Wasted Items Card (与 UI 标题对应)
        drawRoundedRect(canvas, startX, currentY, contentWidth, wastedItemsCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = textDark
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Most Wasted Items", colNameX, currentY + 24f, paint)

        var itemY = currentY + 46f
        if (topWastedList.isNotEmpty()) {
            topWastedList.forEach { item ->
                // 名称
                paint.color = textDark
                paint.textSize = 10f
                paint.isFakeBoldText = false
                val displayName = item.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                canvas.drawText(displayName, colNameX, itemY, paint)

                // 统一背景进度条
                paint.color = bgCard
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(colBarStartX, itemY - 6f, colBarStartX + barMaxWidth, itemY - 6f + 5f), 2.5f, 2.5f, paint)

                // 统一填充进度条
                val fillWidth = (item.percentage / 100f).coerceIn(0f, 1f) * barMaxWidth
                paint.color = primaryGreen
                canvas.drawRoundRect(RectF(colBarStartX, itemY - 6f, colBarStartX + fillWidth, itemY - 6f + 5f), 2.5f, 2.5f, paint)

                // 右侧文本（右对齐算法）
                paint.color = textMuted
                paint.textSize = 9f
                paint.isFakeBoldText = false

                val infoText = if (item.totalPrice > 0) {
                    Currency.formatWithCount(item.totalPrice, item.count)
                } else {
                    "${item.count} items (${item.percentage.toInt()}%)"
                }

                val textWidth = paint.measureText(infoText)
                canvas.drawText(infoText, colRightAlignX - textWidth, itemY, paint)

                itemY += 24f
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No items found", colNameX, itemY, paint)
        }

        currentY += wastedItemsCardHeight + 14f

        // 2. Waste Reasons Card
        drawRoundedRect(canvas, startX, currentY, contentWidth, reasonsCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = textDark
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Waste Reasons", colNameX, currentY + 24f, paint)

        var reasonY = currentY + 46f
        if (reasonList.isNotEmpty()) {
            reasonList.forEach { reason ->
                // 名称
                paint.color = textDark
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText(reason.reason, colNameX, reasonY, paint)

                // 统一背景进度条
                paint.color = bgCard
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(colBarStartX, reasonY - 6f, colBarStartX + barMaxWidth, reasonY - 6f + 5f), 2.5f, 2.5f, paint)

                // 统一填充进度条
                val fillWidth = (reason.percentage / 100f).coerceIn(0f, 1f) * barMaxWidth
                paint.color = primaryGreen
                canvas.drawRoundRect(RectF(colBarStartX, reasonY - 6f, colBarStartX + fillWidth, reasonY - 6f + 5f), 2.5f, 2.5f, paint)

                // 右侧文本（右对齐算法）
                paint.color = textMuted
                paint.textSize = 9f
                paint.isFakeBoldText = false

                val infoText = "${reason.count} items (${reason.percentage.toInt()}%)"
                val textWidth = paint.measureText(infoText)
                canvas.drawText(infoText, colRightAlignX - textWidth, reasonY, paint)

                reasonY += 24f
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No waste reasons recorded for this period", colNameX, reasonY, paint)
        }

        currentY += reasonsCardHeight + 14f

        // 3. Consumed Items Card
        drawRoundedRect(canvas, startX, currentY, contentWidth, consumedCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = primaryGreen
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Consumed Items", colNameX, currentY + 24f, paint)

        var consumedY = currentY + 46f
        if (consumedList.isNotEmpty()) {
            consumedList.forEach { item ->
                // 名称
                paint.color = textDark
                paint.textSize = 10f
                paint.isFakeBoldText = false
                val displayName = item.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                canvas.drawText(displayName, colNameX, consumedY, paint)

                // 统一背景进度条
                paint.color = bgCard
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(colBarStartX, consumedY - 6f, colBarStartX + barMaxWidth, consumedY - 6f + 5f), 2.5f, 2.5f, paint)

                // 统一填充进度条
                val fillWidth = (item.percentage / 100f).coerceIn(0f, 1f) * barMaxWidth
                paint.color = primaryGreen
                canvas.drawRoundRect(RectF(colBarStartX, consumedY - 6f, colBarStartX + fillWidth, consumedY - 6f + 5f), 2.5f, 2.5f, paint)

                // 右侧文本（右对齐算法）
                paint.color = textMuted
                paint.textSize = 9f
                paint.isFakeBoldText = false

                val infoText = if (item.totalPrice > 0) {
                    Currency.formatWithCount(item.totalPrice, item.count)
                } else {
                    "${item.count} items (${item.percentage.toInt()}%)"
                }

                val textWidth = paint.measureText(infoText)
                canvas.drawText(infoText, colRightAlignX - textWidth, consumedY, paint)

                consumedY += 24f
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No consumed items recorded", colNameX, consumedY, paint)
        }

        pdfDocument.finishPage(page)

        saveAndSharePdfFile(context, pdfDocument, dateDisplay)
    }

    private fun drawRoundedRect(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        radius: Float,
        fillColor: Int,
        strokeColor: Int
    ) {
        val rect = RectF(left, top, left + width, top + height)
        val paint = Paint().apply { isAntiAlias = true }

        // Draw Fill
        paint.color = fillColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, radius, radius, paint)

        // Draw Stroke Border
        paint.color = strokeColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    private fun saveAndSharePdfFile(context: Context, pdfDocument: PdfDocument, dateTitle: String) {
        val sanitizedTitle = dateTitle.replace(" ", "_").replace("(", "").replace(")", "")
        val fileName = "Food_Waste_Report_${sanitizedTitle}.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Food Waste Report"))

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}