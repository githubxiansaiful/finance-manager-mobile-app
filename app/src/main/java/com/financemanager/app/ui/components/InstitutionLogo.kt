package com.financemanager.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember
import com.financemanager.app.R
import com.financemanager.app.data.model.FinancialInstitution

@Composable
fun InstitutionLogo(
    institution: FinancialInstitution,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val sizeDp = size
    val cornerRadius = sizeDp * 0.28f
    val shape = RoundedCornerShape(cornerRadius)
    val context = LocalContext.current

    val directResId: Int? = when (institution) {
        FinancialInstitution.BKASH -> R.drawable.ic_bkash
        FinancialInstitution.DUTCH_BANGLA -> R.drawable.ic_dbbl
        FinancialInstitution.NAGAD -> R.drawable.ic_nagad
        FinancialInstitution.ROCKET -> R.drawable.ic_rocket
        else -> null
    }

    val candidateNames = remember(institution) {
        when (institution) {
            FinancialInstitution.BKASH -> listOf("ic_bkash", "bkash")
            FinancialInstitution.NAGAD -> listOf("ic_nagad", "nagad")
            FinancialInstitution.ROCKET -> listOf("ic_rocket", "rocket")
            FinancialInstitution.DUTCH_BANGLA -> listOf("ic_dbbl", "ic_dutch_bangla", "dbbl")
            FinancialInstitution.PUBALI_BANK -> listOf("ic_pubali", "ic_pubali_bank", "pubali")
            FinancialInstitution.ISLAMI_BANK -> listOf("ic_ibbl", "ic_islami_bank", "ibbl")
            FinancialInstitution.DHAKA_BANK -> listOf("ic_dhaka_bank", "dhaka_bank")
            FinancialInstitution.BANK_ASIA -> listOf("ic_bank_asia", "bank_asia")
            FinancialInstitution.CASH -> listOf("ic_cash", "cash")
            FinancialInstitution.OTHER -> listOf("ic_bank_generic", "ic_other", "other")
        }
    }

    val resId = directResId ?: remember(candidateNames) {
        candidateNames.firstNotNullOfOrNull { name ->
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (id != 0) id else null
        } ?: 0
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .shadow(elevation = 2.dp, shape = shape)
            .clip(shape)
            .background(if (resId != 0) Color.White else institution.brandColor),
        contentAlignment = Alignment.Center
    ) {
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = institution.displayName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(sizeDp * 0.12f),
                contentScale = ContentScale.Fit
            )
        } else {
            when (institution) {
                FinancialInstitution.BKASH -> BkashLogo(sizeDp)
                FinancialInstitution.NAGAD -> NagadLogo(sizeDp)
                FinancialInstitution.ROCKET -> RocketLogo(sizeDp)
                FinancialInstitution.DUTCH_BANGLA -> DutchBanglaLogo(sizeDp)
                FinancialInstitution.PUBALI_BANK -> PubaliBankLogo(sizeDp)
                FinancialInstitution.ISLAMI_BANK -> IslamiBankLogo(sizeDp)
                FinancialInstitution.DHAKA_BANK -> DhakaBankLogo(sizeDp)
                FinancialInstitution.BANK_ASIA -> BankAsiaLogo(sizeDp)
                FinancialInstitution.CASH -> {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Cash",
                        tint = Color.White,
                        modifier = Modifier.size(sizeDp * 0.58f)
                    )
                }
                FinancialInstitution.OTHER -> {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Bank",
                        tint = Color.White,
                        modifier = Modifier.size(sizeDp * 0.58f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BkashLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // Origami bird wing silhouette
        val bodyPath = Path().apply {
            moveTo(w * 0.15f, h * 0.70f)
            lineTo(w * 0.50f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.55f)
            lineTo(w * 0.55f, h * 0.50f)
            close()
        }
        drawPath(bodyPath, Color.White)

        val wingPath = Path().apply {
            moveTo(w * 0.50f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.25f)
            lineTo(w * 0.65f, h * 0.45f)
            close()
        }
        drawPath(wingPath, Color.White.copy(alpha = 0.9f))

        val tailPath = Path().apply {
            moveTo(w * 0.15f, h * 0.70f)
            lineTo(w * 0.45f, h * 0.85f)
            lineTo(w * 0.40f, h * 0.65f)
            close()
        }
        drawPath(tailPath, Color.White.copy(alpha = 0.85f))
    }
}

@Composable
private fun NagadLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // Nagad flame/curve emblem
        val flamePath = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            cubicTo(w * 0.65f, h * 0.25f, w * 0.85f, h * 0.45f, w * 0.80f, h * 0.68f)
            cubicTo(w * 0.75f, h * 0.88f, w * 0.55f, h * 0.90f, w * 0.45f, h * 0.88f)
            cubicTo(w * 0.25f, h * 0.85f, w * 0.15f, h * 0.65f, w * 0.25f, h * 0.45f)
            cubicTo(w * 0.32f, h * 0.32f, w * 0.42f, h * 0.22f, w * 0.50f, h * 0.12f)
            close()
        }
        drawPath(flamePath, Color.White)

        // Inner negative space swirl
        val innerSwirl = Path().apply {
            moveTo(w * 0.48f, h * 0.45f)
            cubicTo(w * 0.60f, h * 0.52f, w * 0.65f, h * 0.68f, w * 0.50f, h * 0.78f)
            cubicTo(w * 0.40f, h * 0.72f, w * 0.38f, h * 0.58f, w * 0.48f, h * 0.45f)
            close()
        }
        drawPath(innerSwirl, Color(0xFFF7941D))
    }
}

@Composable
private fun RocketLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // Rocket body
        val rocketPath = Path().apply {
            moveTo(w * 0.50f, h * 0.10f)
            cubicTo(w * 0.62f, h * 0.25f, w * 0.65f, h * 0.55f, w * 0.62f, h * 0.72f)
            lineTo(w * 0.38f, h * 0.72f)
            cubicTo(w * 0.35f, h * 0.55f, w * 0.38f, h * 0.25f, w * 0.50f, h * 0.10f)
            close()
        }
        drawPath(rocketPath, Color.White)

        // Rocket wings
        val leftWing = Path().apply {
            moveTo(w * 0.38f, h * 0.58f)
            lineTo(w * 0.18f, h * 0.76f)
            lineTo(w * 0.38f, h * 0.72f)
            close()
        }
        drawPath(leftWing, Color.White)

        val rightWing = Path().apply {
            moveTo(w * 0.62f, h * 0.58f)
            lineTo(w * 0.82f, h * 0.76f)
            lineTo(w * 0.62f, h * 0.72f)
            close()
        }
        drawPath(rightWing, Color.White)

        // Rocket exhaust plume
        drawCircle(
            color = Color(0xFFFFB300),
            radius = w * 0.08f,
            center = Offset(w * 0.50f, h * 0.84f)
        )
    }
}

@Composable
private fun DutchBanglaLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // DBBL Banyan tree leaf motif
        val stemPath = Path().apply {
            moveTo(w * 0.50f, h * 0.88f)
            lineTo(w * 0.50f, h * 0.35f)
        }
        drawLine(
            color = Color.White,
            start = Offset(w * 0.50f, h * 0.88f),
            end = Offset(w * 0.50f, h * 0.35f),
            strokeWidth = w * 0.10f
        )

        // Tree crown arcs / leaves
        drawCircle(
            color = Color(0xFFFF9800),
            radius = w * 0.20f,
            center = Offset(w * 0.50f, h * 0.32f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.16f,
            center = Offset(w * 0.32f, h * 0.45f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.16f,
            center = Offset(w * 0.68f, h * 0.45f)
        )
    }
}

@Composable
private fun PubaliBankLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // Pubali Bank Stylized geometric emblem
        val path1 = Path().apply {
            moveTo(w * 0.20f, h * 0.25f)
            lineTo(w * 0.50f, h * 0.55f)
            lineTo(w * 0.20f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.55f)
            lineTo(w * 0.35f, h * 0.25f)
            close()
        }
        drawPath(path1, Color.White)

        val path2 = Path().apply {
            moveTo(w * 0.50f, h * 0.25f)
            lineTo(w * 0.80f, h * 0.55f)
            lineTo(w * 0.50f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.95f, h * 0.55f)
            lineTo(w * 0.65f, h * 0.25f)
            close()
        }
        drawPath(path2, Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun IslamiBankLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // IBBL Arch and Rising Sun
        // Golden Sun
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = w * 0.26f,
            center = Offset(w * 0.50f, h * 0.48f)
        )

        // White Minaret Arch
        val archPath = Path().apply {
            moveTo(w * 0.22f, h * 0.85f)
            lineTo(w * 0.22f, h * 0.50f)
            cubicTo(w * 0.22f, h * 0.20f, w * 0.50f, h * 0.12f, w * 0.50f, h * 0.12f)
            cubicTo(w * 0.50f, h * 0.12f, w * 0.78f, h * 0.20f, w * 0.78f, h * 0.50f)
            lineTo(w * 0.78f, h * 0.85f)
            lineTo(w * 0.64f, h * 0.85f)
            lineTo(w * 0.64f, h * 0.52f)
            cubicTo(w * 0.64f, h * 0.35f, w * 0.50f, h * 0.28f, w * 0.50f, h * 0.28f)
            cubicTo(w * 0.50f, h * 0.28f, w * 0.36f, h * 0.35f, w * 0.36f, h * 0.52f)
            lineTo(w * 0.36f, h * 0.85f)
            close()
        }
        drawPath(archPath, Color.White)
    }
}

@Composable
private fun DhakaBankLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // Dhaka Bank dual curved ribbon
        val ribbonLeft = Path().apply {
            moveTo(w * 0.15f, h * 0.20f)
            cubicTo(w * 0.55f, h * 0.20f, w * 0.45f, h * 0.80f, w * 0.85f, h * 0.80f)
            lineTo(w * 0.70f, h * 0.80f)
            cubicTo(w * 0.30f, h * 0.80f, w * 0.40f, h * 0.20f, w * 0.15f, h * 0.20f)
            close()
        }
        drawPath(ribbonLeft, Color.White)

        drawCircle(
            color = Color(0xFF212121),
            radius = w * 0.14f,
            center = Offset(w * 0.65f, h * 0.35f)
        )
    }
}

@Composable
private fun BankAsiaLogo(sizeDp: Dp) {
    Canvas(modifier = Modifier.size(sizeDp * 0.65f)) {
        val w = size.width
        val h = size.height

        // Bank Asia Outer white ring and star emblem
        drawCircle(
            color = Color.White,
            radius = w * 0.42f,
            style = Stroke(width = w * 0.09f)
        )

        // Internal stylised triangle
        val triangle = Path().apply {
            moveTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.75f, h * 0.72f)
            lineTo(w * 0.25f, h * 0.72f)
            close()
        }
        drawPath(triangle, Color.White)

        drawCircle(
            color = Color(0xFF0054A6),
            radius = w * 0.10f,
            center = Offset(w * 0.50f, h * 0.52f)
        )
    }
}
