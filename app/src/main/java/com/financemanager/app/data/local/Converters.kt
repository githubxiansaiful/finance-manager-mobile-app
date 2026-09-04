package com.financemanager.app.data.local

import androidx.room.TypeConverter
import com.financemanager.app.data.model.Category
import com.financemanager.app.data.model.PaymentMethod
import com.financemanager.app.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)

    @TypeConverter
    fun fromCategory(value: Category): String = value.name

    @TypeConverter
    fun toCategory(value: String): Category =
        runCatching { Category.valueOf(value) }.getOrDefault(Category.OTHER)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod =
        runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.CASH)

    @TypeConverter
    fun fromAccountType(value: com.financemanager.app.data.model.AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): com.financemanager.app.data.model.AccountType =
        runCatching { com.financemanager.app.data.model.AccountType.valueOf(value) }
            .getOrDefault(com.financemanager.app.data.model.AccountType.CASH)

    @TypeConverter
    fun fromFinancialInstitution(value: com.financemanager.app.data.model.FinancialInstitution): String = value.name

    @TypeConverter
    fun toFinancialInstitution(value: String): com.financemanager.app.data.model.FinancialInstitution =
        runCatching { com.financemanager.app.data.model.FinancialInstitution.valueOf(value) }
            .getOrDefault(com.financemanager.app.data.model.FinancialInstitution.OTHER)
}
