package com.financemanager.app.ui.components

import com.financemanager.app.data.model.AppCurrency
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val numberFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatCurrency(amount: Double, currency: AppCurrency = AppCurrency.USD): String {
        val formatted = numberFormat.format(kotlin.math.abs(amount))
        return if (amount < 0) {
            "-${currency.symbol}$formatted"
        } else {
            "${currency.symbol}$formatted"
        }
    }

    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        return timeFormat.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        return monthYearFormat.format(Date(millis))
    }
}
