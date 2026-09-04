package com.financemanager.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financemanager.app.data.model.Category
import com.financemanager.app.data.model.CategoryIconCatalog
import com.financemanager.app.data.model.CustomCategoryEntity
import com.financemanager.app.data.model.TransactionType
import com.financemanager.app.ui.theme.ExpenseRed
import com.financemanager.app.ui.theme.IncomeGreen
import com.financemanager.app.ui.theme.PrimaryGreen
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@Composable
fun CategoriesScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customCategories by viewModel.customCategories.collectAsState()

    var categoryName by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedIconKey by remember { mutableStateOf("Category") }
    var selectedColor by remember { mutableStateOf(CategoryIconCatalog.presetColors.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var categoryToDelete by remember { mutableStateOf<CustomCategoryEntity?>(null) }

    val defaultCategories = remember(selectedTypeFilter) {
        when (selectedTypeFilter) {
            TransactionType.EXPENSE -> Category.expenseCategories()
            TransactionType.INCOME -> Category.incomeCategories()
            null -> Category.entries.toList()
        }
    }

    val filteredCustom = remember(customCategories, selectedTypeFilter) {
        when (selectedTypeFilter) {
            TransactionType.EXPENSE -> customCategories.filter { it.isExpense }
            TransactionType.INCOME -> customCategories.filter { !it.isExpense }
            null -> customCategories
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Overview Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Category Library",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${customCategories.size} custom • ${Category.entries.size} system categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = PrimaryGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Category Builder Card (Create New)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add New Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Live Preview Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = selectedColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, selectedColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = CategoryIconCatalog.getIcon(selectedIconKey),
                                    contentDescription = null,
                                    tint = selectedColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = categoryName.ifBlank { "Preview" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = selectedColor
                                )
                            }
                        }
                    }

                    // Expense vs Income Segmented Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isExpense) ExpenseRed else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isExpense = true }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Expense",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isExpense) IncomeGreen else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isExpense = false }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Income",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Category Name Field
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = {
                            categoryName = it
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text("Category Name") },
                        placeholder = { Text("e.g., Gym, Pet Care, Streaming") },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it) } },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Icon Picker
                    Text(
                        text = "Choose Icon",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryIconCatalog.icons.forEach { (key, vector) ->
                            val isSelected = key == selectedIconKey
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) selectedColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, selectedColor) else null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { selectedIconKey = key }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = key,
                                        tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Color Palette Picker
                    Text(
                        text = "Choose Color",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryIconCatalog.presetColors.forEach { color ->
                            val isSelected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = color }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Create Action Button
                    Button(
                        onClick = {
                            if (categoryName.isBlank()) {
                                errorMessage = "Please enter a category name"
                                return@Button
                            }
                            val existing = customCategories.any { it.name.equals(categoryName.trim(), ignoreCase = true) }
                            if (existing) {
                                errorMessage = "A category with this name already exists"
                                return@Button
                            }
                            val colorHex = (selectedColor.toArgb().toLong() and 0xFFFFFFFFL)
                            viewModel.addCustomCategory(
                                name = categoryName.trim(),
                                iconKey = selectedIconKey,
                                colorHex = colorHex,
                                isExpense = isExpense
                            )
                            Toast.makeText(context, "Category '${categoryName.trim()}' created!", Toast.LENGTH_SHORT).show()
                            categoryName = ""
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Category", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Filter Chips: All, Expenses, Income
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All Categories") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.EXPENSE,
                    onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                    label = { Text("Expense Only") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.INCOME,
                    onClick = { selectedTypeFilter = TransactionType.INCOME },
                    label = { Text("Income Only") }
                )
            }
        }

        // 4. Custom Categories Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Categories (${filteredCustom.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (filteredCustom.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (customCategories.isEmpty()) "No custom categories yet. Use the builder above to create one!" else "No custom categories match this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredCustom, key = { it.name }) { cat ->
                val catColor = Color(cat.colorHex)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = catColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CategoryIconCatalog.getIcon(cat.iconKey),
                                        contentDescription = cat.name,
                                        tint = catColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (cat.isExpense) "Expense Category" else "Income Category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cat.isExpense) ExpenseRed else IncomeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { categoryToDelete = cat }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. System Default Categories Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "System Default Categories (${defaultCategories.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(defaultCategories, key = { it.name }) { defaultCat ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = defaultCat.composeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = defaultCat.icon,
                                contentDescription = defaultCat.displayName,
                                tint = defaultCat.composeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = defaultCat.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (defaultCat.defaultType == TransactionType.EXPENSE) "Default Expense" else "Default Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "System",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Delete Confirmation Dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Category?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${cat.name}'? Existing transaction records with this category will not be lost.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomCategory(cat.name)
                        Toast.makeText(context, "Deleted '${cat.name}'", Toast.LENGTH_SHORT).show()
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
