package com.financemanager.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity

@Entity(
    tableName = "custom_categories",
    primaryKeys = ["userId", "name"]
)
data class CustomCategoryEntity(
    val userId: String,
    val name: String,
    val iconKey: String = "Category",
    val colorHex: Long = 0xFF4CAF50,
    val isExpense: Boolean = true
)

object CategoryIconCatalog {
    val icons: Map<String, ImageVector> = mapOf(
        "Fastfood" to Icons.Default.Fastfood,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "Home" to Icons.Default.Home,
        "Movie" to Icons.Default.Movie,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "School" to Icons.Default.School,
        "Flight" to Icons.Default.Flight,
        "Pets" to Icons.Default.Pets,
        "AttachMoney" to Icons.Default.AttachMoney,
        "Work" to Icons.Default.Work,
        "Computer" to Icons.Default.Computer,
        "LocalHospital" to Icons.Default.LocalHospital,
        "CardGiftcard" to Icons.Default.CardGiftcard,
        "Build" to Icons.Default.Build,
        "PhoneIphone" to Icons.Default.PhoneIphone,
        "Coffee" to Icons.Default.Coffee,
        "Category" to Icons.Default.Category
    )

    val presetColors: List<Color> = listOf(
        Color(0xFFE53935), // Red
        Color(0xFFD81B60), // Pink
        Color(0xFF8E24AA), // Purple
        Color(0xFF5E35B1), // Deep Purple
        Color(0xFF3949AB), // Indigo
        Color(0xFF1E88E5), // Blue
        Color(0xFF039BE5), // Light Blue
        Color(0xFF00ACC1), // Cyan
        Color(0xFF00897B), // Teal
        Color(0xFF43A047), // Green
        Color(0xFF7CB342), // Light Green
        Color(0xFFFDD835), // Yellow
        Color(0xFFFFB300), // Amber
        Color(0xFFFB8C00), // Orange
        Color(0xFFF4511E), // Deep Orange
        Color(0xFF6D4C41), // Brown
        Color(0xFF546E7A)  // Blue Grey
    )

    fun getIcon(key: String): ImageVector {
        return icons[key] ?: Icons.Default.Category
    }
}
