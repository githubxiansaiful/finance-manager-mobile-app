package com.financemanager.app.data.model

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar ($)"),
    EUR("EUR", "€", "Euro (€)"),
    GBP("GBP", "£", "British Pound (£)"),
    BDT("BDT", "৳", "Bangladeshi Taka (৳)"),
    INR("INR", "₹", "Indian Rupee (₹)"),
    CAD("CAD", "C$", "Canadian Dollar (C$)"),
    AUD("AUD", "A$", "Australian Dollar (A$)"),
    JPY("JPY", "¥", "Japanese Yen (¥)"),
    AED("AED", "AED ", "UAE Dirham (AED)"),
    SAR("SAR", "SAR ", "Saudi Riyal (SAR)");

    fun format(amount: Double): String {
        return String.format("%s%,.2f", symbol, amount)
    }

    companion object {
        fun fromCode(code: String?): AppCurrency {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}
