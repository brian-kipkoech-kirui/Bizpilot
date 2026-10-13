package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.VoiceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneInputDialog(
    viewModel: BizPilotViewModel,
    voiceManager: VoiceManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by voiceManager.isListening.collectAsState()
    val transcript by voiceManager.speechTranscript.collectAsState()
    val rmsDb by voiceManager.audioRmsDb.collectAsState()
    val voiceStatus by voiceManager.voiceStatus.collectAsState()
    val isSpeakingTts by voiceManager.isSpeakingTts.collectAsState()

    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiChats by viewModel.aiChats.collectAsState()

    var customQuestionText by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            voiceManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice questions", Toast.LENGTH_SHORT).show()
        }
    }

    // Update custom text if speech transcript changes
    LaunchedEffect(transcript) {
        if (transcript.isNotBlank()) {
            customQuestionText = transcript
        }
    }

    // Auto-start listening on launch if permission granted
    LaunchedEffect(Unit) {
        if (hasPermission) {
            voiceManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val sampleVoiceQuestions = listOf(
        "🎤 How is my overall business performance today?",
        "📊 What is my net profit and sales revenue?",
        "📦 Which products are low in stock & need reordering?",
        "💰 Analyze my cash flow and M-Pesa float liquidity",
        "📈 What are my top 3 best-selling products this week?"
    )

    // Get latest assistant response if available
    val latestAssistantChat = remember(aiChats) {
        aiChats.lastOrNull { it.sender == "assistant" }
    }

    Dialog(
        onDismissRequest = {
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MpesaGreen.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MpesaGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("AI CEO Voice Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Microphone Input • Gemini 3.5 Flash", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = {
                        voiceManager.stopListening()
                        voiceManager.stopSpeaking()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Microphone Pulsing Visualizer & Waveform Animation
                val transition = rememberInfiniteTransition(label = "audio_wave")
                val pulseScale by transition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (isListening) 1.25f else 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MpesaGreen.copy(alpha = 0.5f),
                                            SecondaryAmber.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isListening) MpesaGreen else MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .size(80.dp)
                            .testTag("ai_ceo_mic_button")
                            .clickable {
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (isListening) {
                                        voiceManager.stopListening()
                                    } else {
                                        voiceManager.startListening()
                                    }
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = "Toggle Microphone",
                                tint = if (isListening) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                // Audio Waveform Equalizer Bars (Dynamic visual feedback)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(28.dp)
                ) {
                    val barCount = 12
                    for (i in 0 until barCount) {
                        val baseHeight = if (isListening) (10..24).random().dp else 6.dp
                        val animatedBarHeight by animateDpAsState(
                            targetValue = if (isListening) (baseHeight.value * (1f + (rmsDb / 10f))).dp else 6.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "bar_height"
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(animatedBarHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isListening) MpesaGreen else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                // Voice Status Label
                Text(
                    text = voiceStatus,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) MpesaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Editable Speech Transcript Box
                OutlinedTextField(
                    value = customQuestionText,
                    onValueChange = { customQuestionText = it },
                    label = { Text("Your Voice Question to AI CEO") },
                    placeholder = { Text("Speech transcript will appear here...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        if (customQuestionText.isNotEmpty()) {
                            IconButton(onClick = { customQuestionText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                // Quick Voice Question Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Tap or Speak a Question:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sampleVoiceQuestions) { question ->
                            SuggestionChip(
                                onClick = {
                                    val cleanQ = question.substringAfter(" ").trim()
                                    customQuestionText = cleanQ
                                    voiceManager.setTranscript(cleanQ)
                                },
                                label = { Text(question, fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Send Question Button
                Button(
                    onClick = {
                        if (customQuestionText.isNotBlank()) {
                            voiceManager.stopListening()
                            viewModel.sendAiPrompt(customQuestionText)
                        } else {
                            Toast.makeText(context, "Please speak or enter a question first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = customQuestionText.isNotBlank() && !aiLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Gemini AI Processing Business Performance...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ask AI CEO Now", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // AI CEO Speech Response Section
                latestAssistantChat?.let { response ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(18.dp))
                                    Text("AI CEO Response", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SecondaryAmber)
                                }

                                // Text-To-Speech Read Aloud Button
                                IconButton(
                                    onClick = {
                                        if (isSpeakingTts) {
                                            voiceManager.stopSpeaking()
                                        } else {
                                            voiceManager.speakAiCeoResponse(response.message)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSpeakingTts) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                        contentDescription = "Read Aloud",
                                        tint = if (isSpeakingTts) MpesaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Text(
                                text = response.message,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
