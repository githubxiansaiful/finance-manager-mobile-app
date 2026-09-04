package com.financemanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financemanager.app.data.model.AppCurrency
import com.financemanager.app.data.model.PaymentMethod
import com.financemanager.app.data.model.TransactionEntity
import com.financemanager.app.data.model.TransactionType
import com.financemanager.app.ui.theme.ExpenseRed
import com.financemanager.app.ui.theme.IncomeGreen

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    currency: AppCurrency = AppCurrency.USD,
    onClick: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (onEdit != null) onEdit() else onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon in a modern soft squircle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(transaction.category.composeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.category.icon,
                contentDescription = transaction.category.displayName,
                tint = transaction.category.composeColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Subtitle (Category • Date)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${transaction.category.displayName} • ${Formatters.formatDate(transaction.dateMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Amount & Account under it
        val isIncome = transaction.type == TransactionType.INCOME
        val prefix = if (isIncome) "+ " else "- "
        val amountColor = if (isIncome) IncomeGreen else ExpenseRed

        val displayAccount = when {
            !transaction.accountName.isNullOrBlank() -> transaction.accountName
            transaction.paymentMethod == PaymentMethod.MOBILE_WALLET -> "bKash"
            transaction.paymentMethod == PaymentMethod.BANK_TRANSFER -> "Bank Account"
            transaction.paymentMethod == PaymentMethod.CASH -> "Cash"
            else -> transaction.paymentMethod.displayName
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$prefix${Formatters.formatCurrency(transaction.amount, currency)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                color = amountColor
            )

            Text(
                text = displayAccount,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}
