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
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormatSymbols

object PdfReportGenerator {

    fun generateAndSharePdf(context: Context, state: ReportUiState) {
        val pdfDocument = PdfDocument()

        val pageWidth = 595

        val topWastedList = state.topWastedItems
        val reasonList = state.reasonBreakdowns
        val consumedList = state.topConsumedItems

        val topItemsCount = topWastedList.size
        val reasonsCount = reasonList.size
        val consumedCount = consumedList.size

        val wastedItemsCardHeight = 36f + (topItemsCount * 22f).coerceAtLeast(25f)
        val reasonsCardHeight = 36f + (reasonsCount * 22f).coerceAtLeast(25f)
        val consumedCardHeight = 36f + (consumedCount * 22f).coerceAtLeast(25f)
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

        paint.color = primaryGreen
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Food Waste & Consumption Report", startX, currentY + 16f, paint)

        val monthName = DateFormatSymbols().months.getOrElse(state.selectedMonth) { "" }
        val dateDisplay = "$monthName ${state.selectedYear}"

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

        // Card 1: Total Items Wasted
        drawRoundedRect(canvas, startX, currentY, metricCardWidth, metricCardHeight, 10f, bgCard, borderCard)
        paint.color = textMuted
        paint.textSize = 10f
        canvas.drawText("Total Items Wasted", startX + 14f, currentY + 18f, paint)
        paint.color = textDark
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText("${state.totalWastedItems} items", startX + 14f, currentY + 40f, paint)

        // Card 2: Most Wasted Item
        val card2X = startX + metricCardWidth + cardGap
        drawRoundedRect(canvas, card2X, currentY, metricCardWidth, metricCardHeight, 10f, bgCard, borderCard)
        paint.color = textMuted
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Most Wasted", card2X + 14f, currentY + 18f, paint)
        paint.color = textDark
        paint.textSize = 15f
        paint.isFakeBoldText = true
        val mostWastedName = state.mostWastedName.ifEmpty { "-" }
        canvas.drawText(mostWastedName, card2X + 14f, currentY + 40f, paint)

        currentY += metricCardHeight + 14f

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
                    canvas.drawText("${item.count} (${item.percentage.toInt()}%)", posX + 105f, posY, paint)
                }
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No waste data for this month", startX + 14f, currentY + 75f, paint)
        }

        currentY += breakdownCardHeight + 14f

        drawRoundedRect(canvas, startX, currentY, contentWidth, wastedItemsCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = textDark
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("All Wasted Items (${topWastedList.size})", startX + 14f, currentY + 24f, paint)

        var itemY = currentY + 45f
        if (topWastedList.isNotEmpty()) {
            topWastedList.forEach { item ->
                paint.color = textDark
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText(item.name, startX + 14f, itemY, paint)

                val barStartX = startX + 130f
                val barMaxWidth = 260f
                val barHeight = 5f
                paint.color = bgCard
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(barStartX, itemY - 6f, barStartX + barMaxWidth, itemY - 6f + barHeight), 2.5f, 2.5f, paint)

                val fillWidth = (item.percentage / 100f).coerceIn(0f, 1f) * barMaxWidth
                paint.color = primaryGreen
                canvas.drawRoundRect(RectF(barStartX, itemY - 6f, barStartX + fillWidth, itemY - 6f + barHeight), 2.5f, 2.5f, paint)

                paint.color = textMuted
                paint.textSize = 9f
                canvas.drawText("${item.count} items (${item.percentage.toInt()}%)", barStartX + barMaxWidth + 12f, itemY, paint)

                itemY += 22f
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No wasted items found", startX + 14f, itemY, paint)
        }

        currentY += wastedItemsCardHeight + 14f

        drawRoundedRect(canvas, startX, currentY, contentWidth, reasonsCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = textDark
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Waste Reasons", startX + 14f, currentY + 24f, paint)

        var reasonY = currentY + 45f
        if (reasonList.isNotEmpty()) {
            reasonList.forEach { reason ->
                paint.color = textDark
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText(reason.reason, startX + 14f, reasonY, paint)

                val barStartX = startX + 130f
                val barMaxWidth = 260f
                val barHeight = 5f
                paint.color = bgCard
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(barStartX, reasonY - 6f, barStartX + barMaxWidth, reasonY - 6f + barHeight), 2.5f, 2.5f, paint)

                val fillWidth = (reason.percentage / 100f).coerceIn(0f, 1f) * barMaxWidth
                paint.color = primaryGreen
                canvas.drawRoundRect(RectF(barStartX, reasonY - 6f, barStartX + fillWidth, reasonY - 6f + barHeight), 2.5f, 2.5f, paint)

                paint.color = textMuted
                paint.textSize = 9f
                canvas.drawText("${reason.count} (${reason.percentage.toInt()}%)", barStartX + barMaxWidth + 12f, reasonY, paint)

                reasonY += 22f
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No reasons recorded", startX + 14f, reasonY, paint)
        }

        currentY += reasonsCardHeight + 14f

        drawRoundedRect(canvas, startX, currentY, contentWidth, consumedCardHeight, 12f, Color.WHITE, borderCard)

        paint.color = primaryGreen
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("All Consumed Items (${consumedList.size})", startX + 14f, currentY + 24f, paint)

        var consumedY = currentY + 45f
        if (consumedList.isNotEmpty()) {
            consumedList.forEach { item ->
                paint.color = textDark
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText(item.name, startX + 14f, consumedY, paint)

                val barStartX = startX + 130f
                val barMaxWidth = 260f
                val barHeight = 5f
                paint.color = bgCard
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(barStartX, consumedY - 6f, barStartX + barMaxWidth, consumedY - 6f + barHeight), 2.5f, 2.5f, paint)

                val fillWidth = (item.percentage / 100f).coerceIn(0f, 1f) * barMaxWidth
                paint.color = primaryGreen
                canvas.drawRoundRect(RectF(barStartX, consumedY - 6f, barStartX + fillWidth, consumedY - 6f + barHeight), 2.5f, 2.5f, paint)

                paint.color = textMuted
                paint.textSize = 9f
                canvas.drawText("${item.count} items (${item.percentage.toInt()}%)", barStartX + barMaxWidth + 12f, consumedY, paint)

                consumedY += 22f
            }
        } else {
            paint.color = textMuted
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No consumed items recorded for this month", startX + 14f, consumedY, paint)
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
        val fileName = "Food_Waste_Report_${dateTitle.replace(" ", "_")}.pdf"
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