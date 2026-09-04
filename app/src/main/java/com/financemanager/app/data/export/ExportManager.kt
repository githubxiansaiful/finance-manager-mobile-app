package com.financemanager.app.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.financemanager.app.data.model.AppCurrency
import com.financemanager.app.data.model.TransactionEntity
import com.financemanager.app.data.model.TransactionType
import com.financemanager.app.data.repository.FinancialStats
import com.financemanager.app.ui.components.Formatters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportManager {

    fun exportToCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        currency: AppCurrency
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "finance_transactions_$timeStamp.csv")

        FileOutputStream(file).bufferedWriter().use { writer ->
            // Header
            writer.write("ID,Date,Time,Title,Type,Category,Amount (${currency.code}),Payment Method,Note\n")
            for (tx in transactions) {
                val dateStr = Formatters.formatDate(tx.dateMillis).replace(",", "")
                val timeStr = Formatters.formatTime(tx.dateMillis)
                val titleEscaped = "\"" + tx.title.replace("\"", "\"\"") + "\""
                val noteEscaped = "\"" + tx.note.replace("\"", "\"\"") + "\""
                val amountFormatted = String.format(Locale.US, "%.2f", tx.amount)
                writer.write("${tx.id},$dateStr,$timeStr,$titleEscaped,${tx.type.name},${tx.category.displayName},$amountFormatted,${tx.paymentMethod.displayName},$noteEscaped\n")
            }
        }
        return file
    }

    fun exportToPdf(
        context: Context,
        userName: String,
        periodName: String,
        stats: FinancialStats,
        transactions: List<TransactionEntity>,
        currency: AppCurrency
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "financial_statement_$timeStamp.pdf")

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.rgb(27, 94, 32) // Forest green
            textSize = 20f
            isFakeBoldText = true
        }

        val subPaint = Paint().apply {
            color = AndroidColor.DKGRAY
            textSize = 10f
        }

        val textPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 10f
        }

        val boldPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 10f
            isFakeBoldText = true
        }

        val greenPaint = Paint().apply {
            color = AndroidColor.rgb(46, 125, 50)
            textSize = 10f
            isFakeBoldText = true
        }

        val redPaint = Paint().apply {
            color = AndroidColor.rgb(198, 40, 40)
            textSize = 10f
            isFakeBoldText = true
        }

        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(240, 244, 241)
        }

        val linePaint = Paint().apply {
            color = AndroidColor.LTGRAY
            strokeWidth = 1f
        }

        var y = 45f

        // Document Header
        canvas.drawText("FINANCE MANAGER - FINANCIAL STATEMENT", 40f, y, titlePaint)
        y += 18f
        canvas.drawText("User Account: $userName  |  Period: $periodName  |  Currency: ${currency.displayName}", 40f, y, subPaint)
        y += 14f
        canvas.drawText("Generated on: ${SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())}", 40f, y, subPaint)
        y += 18f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Summary Card Box
        canvas.drawRoundRect(40f, y, 555f, y + 65f, 8f, 8f, bgPaint)
        val cardY = y + 25f
        canvas.drawText("TOTAL INCOME", 60f, cardY, subPaint)
        canvas.drawText(Formatters.formatCurrency(stats.totalIncome, currency), 60f, cardY + 22f, greenPaint)

        canvas.drawText("TOTAL EXPENSE", 185f, cardY, subPaint)
        canvas.drawText(Formatters.formatCurrency(stats.totalExpense, currency), 185f, cardY + 22f, redPaint)

        canvas.drawText("NET BALANCE", 320f, cardY, subPaint)
        val balancePaint = if (stats.netBalance >= 0) greenPaint else redPaint
        canvas.drawText(Formatters.formatCurrency(stats.netBalance, currency), 320f, cardY + 22f, balancePaint)

        canvas.drawText("SAVINGS RATE", 450f, cardY, subPaint)
        canvas.drawText("${String.format(Locale.US, "%.1f", stats.savingsRate)}%", 450f, cardY + 22f, boldPaint)

        y += 85f

        // Table Header
        canvas.drawRect(40f, y, 555f, y + 22f, bgPaint)
        val headerY = y + 15f
        canvas.drawText("Date", 48f, headerY, boldPaint)
        canvas.drawText("Title", 130f, headerY, boldPaint)
        canvas.drawText("Category", 280f, headerY, boldPaint)
        canvas.drawText("Method", 400f, headerY, boldPaint)
        canvas.drawText("Amount", 490f, headerY, boldPaint)
        y += 24f

        // Table Rows
        val rowHeight = 20f
        var isOdd = false
        val maxRows = 26
        val displayList = transactions.take(maxRows)

        for (tx in displayList) {
            if (isOdd) {
                canvas.drawRect(40f, y, 555f, y + rowHeight, bgPaint)
            }
            isOdd = !isOdd

            val rowTextY = y + 14f
            canvas.drawText(Formatters.formatDate(tx.dateMillis), 48f, rowTextY, textPaint)
            val truncatedTitle = if (tx.title.length > 22) tx.title.take(20) + ".." else tx.title
            canvas.drawText(truncatedTitle, 130f, rowTextY, textPaint)
            canvas.drawText(tx.category.displayName, 280f, rowTextY, textPaint)
            canvas.drawText(tx.paymentMethod.displayName, 400f, rowTextY, textPaint)

            val amountPrefix = if (tx.type == TransactionType.INCOME) "+" else "-"
            val amountFormatted = "$amountPrefix${currency.symbol}${String.format(Locale.US, "%.2f", tx.amount)}"
            val amtPaint = if (tx.type == TransactionType.INCOME) greenPaint else redPaint
            canvas.drawText(amountFormatted, 490f, rowTextY, amtPaint)

            y += rowHeight
        }

        if (transactions.size > maxRows) {
            y += 14f
            canvas.drawText("... and ${transactions.size - maxRows} more transactions (full history exported in CSV)", 48f, y, subPaint)
        }

        // Footer
        canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
        canvas.drawText("Generated by Finance Manager Android App - Local & Cloud Synchronized", 40f, 815f, subPaint)

        pdfDoc.finishPage(page)
        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
