package com.financemanager.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financemanager.app.ui.theme.PrimaryGreen
import com.financemanager.app.ui.theme.PrimaryGreenDark
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = w * 0.22f
            val radius = (w - strokeWidth) / 2f
            val center = Offset(w / 2f, h / 2f)

            // Red (top)
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 200f,
                sweepAngle = 105f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )

            // Yellow (left)
            drawArc(
                color = Color(0xFFFBBC05),
                startAngle = 125f,
                sweepAngle = 75f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )

            // Green (bottom)
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 35f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )

            // Blue (right & horizontal bar)
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = -35f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )

            // Blue crossbar
            drawLine(
                color = Color(0xFF4285F4),
                start = Offset(center.x, center.y),
                end = Offset(w - strokeWidth / 3f, center.y),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val errorMessage by viewModel.authErrorMessage.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showConfigDialog by remember { mutableStateOf(false) }
    var configuredClientId by remember { mutableStateOf(viewModel.getStoredWebClientId()) }

    val defaultResClientId = try {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (id != 0) context.getString(id) else ""
    } catch (_: Exception) {
        ""
    }

    val effectiveClientId = remember(configuredClientId, defaultResClientId) {
        if (configuredClientId.isNotBlank()) configuredClientId else defaultResClientId
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with discrete Firebase Config Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showConfigDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Firebase Configuration",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Production Hero Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 8.dp,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryGreen, PrimaryGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Finance App Logo",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Finance Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Smart Wealth, Budgets & Bangladesh Accounts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Main Auth Card (Production Polished)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Segmented Control: Sign In vs Create Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isSignUpMode = false
                                    viewModel.clearAuthError()
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Sign In",
                                    fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isSignUpMode = true
                                    viewModel.clearAuthError()
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Register",
                                    fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Google One-Tap Sign In Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isAuthenticating) {
                                viewModel.clearAuthError()
                                if (effectiveClientId.isBlank() || effectiveClientId.contains("dummy", ignoreCase = true)) {
                                    showConfigDialog = true
                                } else {
                                    viewModel.signInWithGoogle(effectiveClientId)
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Divider Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = "or email",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    // Name input (only for register)
                    AnimatedVisibility(visible = isSignUpMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g., Saiful Islam") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Email input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("you@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Password input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        placeholder = { Text("At least 6 characters") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Error Message
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Primary Submit Button
                    Button(
                        onClick = {
                            viewModel.clearAuthError()
                            if (isSignUpMode) {
                                viewModel.signUpWithEmail(emailInput, passwordInput, nameInput)
                            } else {
                                viewModel.signInWithEmail(emailInput, passwordInput)
                            }
                        },
                        enabled = !isAuthenticating && emailInput.isNotBlank() && passwordInput.length >= 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Connecting...")
                        } else {
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Sign In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trust & Security Badges
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(15.dp))
                    Text("256-bit Encrypted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(15.dp))
                    Text("Firebase Cloud", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Firebase Web Client ID Dialog
    if (showConfigDialog) {
        var tempInput by remember { mutableStateOf(configuredClientId) }
        val debugSha1 = "80:53:CF:68:D5:89:72:1F:C2:3D:A2:FF:63:EB:79:76:AC:F0:FC:DA"

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoogleLogoIcon(modifier = Modifier.size(20.dp))
                    Text("Firebase Google Auth Setup")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "For real Google Sign-In via Credential Manager, paste your Web Client ID from the Firebase Console:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it },
                        label = { Text("Web Client ID") },
                        placeholder = { Text("e.g. 12345-xxx.apps.googleusercontent.com") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    Text(
                        text = "Your App Registration Details:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Package Name:\ncom.financemanager.app",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Debug SHA-1 Fingerprint:\n$debugSha1",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SHA1", debugSha1))
                            Toast.makeText(context, "SHA-1 copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy SHA-1 Fingerprint")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = tempInput.trim()
                        configuredClientId = cleaned
                        viewModel.saveStoredWebClientId(cleaned)
                        showConfigDialog = false
                        if (cleaned.isNotBlank()) {
                            viewModel.signInWithGoogle(cleaned)
                        }
                    }
                ) {
                    Text("Save & Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
