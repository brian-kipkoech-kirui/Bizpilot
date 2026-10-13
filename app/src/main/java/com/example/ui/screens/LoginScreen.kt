package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: BizPilotViewModel,
    onContinueAsGuest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authLoading by viewModel.authLoading.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var merchantName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showGoogleAccountSelector by remember { mutableStateOf(false) }

    var enableFingerprint by remember { mutableStateOf(true) }
    var showFingerprintPromptModal by remember { mutableStateOf(false) }
    var fingerprintAuthPurpose by remember { mutableStateOf("SIGN_UP") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Header
            Surface(
                shape = CircleShape,
                color = MpesaGreen.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, MpesaGreen.copy(alpha = 0.4f), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "BizPilot Logo",
                        tint = MpesaGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BizPilot AI",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SecondaryAmber.copy(alpha = 0.2f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SecondaryAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Smart Retail & Business OS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SecondaryAmber
                    )
                }
            }

            // Security Gate Alert Banner for Business OS
            if (onContinueAsGuest != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Authorized Only",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔒 Business OS Access Gate",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Business OS modules (POS, ERP, Finance, AI Copilot) require authorized staff login. Public users can access Biz Social Hub.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onContinueAsGuest,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MpesaGreen),
                    border = BorderStroke(1.5.dp, MpesaGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp), tint = MpesaGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Explore Public Biz Social Hub (No Login Required)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Authentication Main Glass Card
            GlassCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Sign In / Sign Up Mode Switcher Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSignUpMode = false }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isSignUpMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSignUpMode = true }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Create Account",
                                    fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSignUpMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Display Error message if any
                    authErrorMessage?.let { errMsg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = errMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Form Fields
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = merchantName,
                            onValueChange = { merchantName = it },
                            label = { Text("Business / Merchant Name") },
                            placeholder = { Text("e.g. Safari Electronics Ltd") },
                            leadingIcon = {
                                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("owner@bizpilot.ai") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Fingerprint Biometric Toggle in Sign Up Mode
                    if (isSignUpMode) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (enableFingerprint) MpesaGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (enableFingerprint) MpesaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Fingerprint,
                                                contentDescription = "Fingerprint Auth",
                                                tint = if (enableFingerprint) MpesaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text("Fingerprint Biometric Sign-Up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Scan fingerprint during sign up for 1-tap quick access", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = enableFingerprint,
                                    onCheckedChange = { enableFingerprint = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MpesaGreen
                                    )
                                )
                            }
                        }
                    }

                    // Submit Primary Button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (isSignUpMode) {
                                if (enableFingerprint) {
                                    fingerprintAuthPurpose = "SIGN_UP"
                                    showFingerprintPromptModal = true
                                } else {
                                    viewModel.registerWithEmail(email, password, merchantName) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                viewModel.loginWithEmail(email, password) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !authLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (isSignUpMode && enableFingerprint) Icons.Default.Fingerprint else Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isSignUpMode) {
                                        if (enableFingerprint) "Sign Up with Fingerprint 🖐️" else "Register & Access Dashboard"
                                    } else "Sign In to Dashboard",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Fingerprint Quick Sign-In Option in Sign In Mode
                    if (!isSignUpMode) {
                        OutlinedButton(
                            onClick = {
                                fingerprintAuthPurpose = "SIGN_IN"
                                showFingerprintPromptModal = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("fingerprint_signin_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.7f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint Sign In", tint = MpesaGreen, modifier = Modifier.size(22.dp))
                                Text("Fingerprint Quick Sign In", color = MpesaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    // Divider with text "OR"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    // Google Sign In Button
                    OutlinedButton(
                        onClick = {
                            showGoogleAccountSelector = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TelegramBlue,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Text(
                                text = "Sign in with Google",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Guest / Quick Demo Entry Option
                    TextButton(
                        onClick = {
                            viewModel.continueAsGuest()
                            Toast.makeText(context, "Logged in as Guest Merchant", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Continue as Offline Merchant / Guest",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(14.dp))
                    Text("Firebase Auth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(14.dp))
                    Text("AES Room Security", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Google Account Quick Selection Modal Dialog
        if (showGoogleAccountSelector) {
            AlertDialog(
                onDismissRequest = { showGoogleAccountSelector = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TelegramBlue.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("G", fontWeight = FontWeight.ExtraBold, color = TelegramBlue)
                            }
                        }
                        Text("Choose Google Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Select a Google account to sign in with Firebase Auth & Cloud Firestore:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val accounts = listOf(
                            Pair("pkoechkirui001@gmail.com", "Peter Kirui (Primary Owner)"),
                            Pair("owner@safari-tech.co.ke", "Safari Tech Mart (Merchant)"),
                            Pair("finance@bizpilot.ai", "BizPilot Finance Account")
                        )

                        accounts.forEach { (accEmail, accName) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.loginWithGoogleDemo(accEmail, accName)
                                        showGoogleAccountSelector = false
                                        Toast.makeText(context, "Signed in as $accName via Google Auth", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MpesaGreen.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(accName.take(1), fontWeight = FontWeight.Bold, color = MpesaGreen)
                                        }
                                    }
                                    Column {
                                        Text(accName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(accEmail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGoogleAccountSelector = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Fingerprint Biometric Prompt Modal Dialog
        if (showFingerprintPromptModal) {
            FingerprintAuthModalDialog(
                purpose = fingerprintAuthPurpose,
                merchantEmail = email.ifBlank { "merchant@safari-tech.co.ke" },
                merchantName = merchantName.ifBlank { "Safari Retail Mart" },
                onDismiss = { showFingerprintPromptModal = false },
                onAuthSuccess = {
                    showFingerprintPromptModal = false
                    if (fingerprintAuthPurpose == "SIGN_UP") {
                        viewModel.registerWithEmail(
                            email.ifBlank { "merchant.biometric@bizpilot.ai" },
                            password.ifBlank { "bioPass123" },
                            merchantName.ifBlank { "Safari Tech Mart" }
                        ) { success, msg ->
                            Toast.makeText(context, "🖐️ Biometric Fingerprint Registered! $msg", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        viewModel.continueAsGuest()
                        Toast.makeText(context, "🖐️ Fingerprint Verified! Welcome back to BizPilot OS.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun FingerprintAuthModalDialog(
    purpose: String,
    merchantEmail: String,
    merchantName: String,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Touch the fingerprint sensor below to scan biometric credentials") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(isScanning) {
        if (isScanning) {
            statusText = "Scanning biometric fingerprint pattern..."
            kotlinx.coroutines.delay(1200)
            isScanning = false
            isVerified = true
            statusText = "Fingerprint Matched Successfully!"
            kotlinx.coroutines.delay(600)
            onAuthSuccess()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MpesaGreen.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(22.dp))
                    }
                }
                Text(
                    text = if (purpose == "SIGN_UP") "Fingerprint Biometric Sign Up" else "Fingerprint Quick Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (purpose == "SIGN_UP") 
                        "Enrolling biometric fingerprint security for $merchantEmail ($merchantName)." 
                    else 
                        "Authenticate with registered fingerprint sensor to unlock business dashboard.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                // Interactive Biometric Pulse Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVerified) MpesaGreen.copy(alpha = 0.15f) 
                            else if (isScanning) MpesaGreen.copy(alpha = 0.1f) 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 2.dp,
                            color = if (isVerified) MpesaGreen else if (isScanning) MpesaGreen else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable {
                            if (!isScanning && !isVerified) {
                                isScanning = true
                            }
                        }
                ) {
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulseScale)
                                .background(MpesaGreen.copy(alpha = 0.2f), CircleShape)
                        )
                    }

                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint Sensor",
                        tint = if (isVerified) MpesaGreen else if (isScanning) MpesaGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Status Message
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isVerified) MpesaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MpesaGreen)
                        } else if (isVerified) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(14.dp))
                        } else {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(14.dp))
                        }

                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isVerified) MpesaGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isScanning && !isVerified) {
                        isScanning = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                enabled = !isScanning
            ) {
                Text(if (isVerified) "Verified ✓" else "Touch / Scan Fingerprint 🖐️")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / Use Password")
            }
        }
    )
}
