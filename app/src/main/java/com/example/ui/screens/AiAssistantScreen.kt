package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassCard
import com.example.ui.components.MicrophoneInputDialog
import com.example.ui.components.VoiceFab
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.VoiceManager

enum class AiMode {
    COPILOT_MAPS,
    HIGH_THINKING,
    CREATIVE_STUDIO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val voiceManager = remember { VoiceManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    var showMicDialog by remember { mutableStateOf(false) }

    val aiChats by viewModel.aiChats.collectAsState()
    val isLoading by viewModel.aiLoading.collectAsState()
    val isVoiceListening by viewModel.isVoiceListening.collectAsState()
    val isSpeakingTts by voiceManager.isSpeakingTts.collectAsState()

    val isListening by voiceManager.isListening.collectAsState()
    val speechTranscript by voiceManager.speechTranscript.collectAsState()
    val voiceStatus by voiceManager.voiceStatus.collectAsState()

    var hasAudioPermission by remember {
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
        hasAudioPermission = isGranted
        if (isGranted) {
            voiceManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice-to-text", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedMode by remember { mutableStateOf(AiMode.COPILOT_MAPS) }
    var textInput by remember { mutableStateOf("") }

    // Auto-transcribe spoken speech into text input field
    LaunchedEffect(speechTranscript) {
        if (speechTranscript.isNotBlank()) {
            textInput = speechTranscript
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    if (showMicDialog) {
        MicrophoneInputDialog(
            viewModel = viewModel,
            voiceManager = voiceManager,
            onDismiss = { showMicDialog = false }
        )
    }

    // Creative Studio State
    var studioMediaType by remember { mutableStateOf("IMAGE") } // "IMAGE" or "VIDEO"
    var selectedImageSize by remember { mutableStateOf("2K") } // "1K", "2K", "4K"
    var selectedVideoAspectRatio by remember { mutableStateOf("16:9") } // "16:9", "9:16"
    var photoAttached by remember { mutableStateOf(false) }

    val copilotPrompts = listOf(
        "📍 Find nearby fertilizer & electronics wholesale suppliers",
        "📊 Analyze profit margin & cash velocity",
        "📄 Generate invoice for KES 48.5k",
        "📦 Reorder level check for retail stock"
    )

    val thinkingPrompts = listOf(
        "🧠 Run multi-scenario cash flow forecast for next 6 months",
        "⚖️ Calculate tax compliance & VAT 16% buffer requirement",
        "📉 Stress-test inventory against fuel price hikes"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Uncluttered Mode Switcher Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedMode == AiMode.COPILOT_MAPS,
                        onClick = { selectedMode = AiMode.COPILOT_MAPS },
                        label = { Text("📍 Copilot & Maps", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedMode == AiMode.HIGH_THINKING,
                        onClick = { selectedMode = AiMode.HIGH_THINKING },
                        label = { Text("🧠 High Thinking", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryAmber,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedMode == AiMode.CREATIVE_STUDIO,
                        onClick = { selectedMode = AiMode.CREATIVE_STUDIO },
                        label = { Text("🎨 Creative Studio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TelegramBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Mode Badge Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (selectedMode) {
                        AiMode.COPILOT_MAPS -> MaterialTheme.colorScheme.primaryContainer
                        AiMode.HIGH_THINKING -> SecondaryAmber.copy(alpha = 0.2f)
                        AiMode.CREATIVE_STUDIO -> TelegramBlue.copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedMode) {
                                AiMode.COPILOT_MAPS -> Icons.Default.Explore
                                AiMode.HIGH_THINKING -> Icons.Default.AccountTree
                                AiMode.CREATIVE_STUDIO -> Icons.Default.MovieFilter
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (selectedMode) {
                                AiMode.COPILOT_MAPS -> "Model: gemini-3.5-flash (Google Maps Grounded)"
                                AiMode.HIGH_THINKING -> "Model: gemini-3.1-pro-preview (ThinkingLevel = HIGH)"
                                AiMode.CREATIVE_STUDIO -> if (studioMediaType == "IMAGE") "Model: gemini-3-pro-image-preview" else "Model: veo-3.1-fast-generate-preview"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                IconButton(onClick = {
                    viewModel.scanReceiptSimulated { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "OCR Scan Receipt", tint = SecondaryAmber)
                }
            }

            // Studio Control Panel (Only visible in Creative Studio mode)
            if (selectedMode == AiMode.CREATIVE_STUDIO) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Studio Target:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = studioMediaType == "IMAGE",
                                    onClick = { studioMediaType = "IMAGE" },
                                    label = { Text("🖼️ High-Res Image", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = studioMediaType == "VIDEO",
                                    onClick = { studioMediaType = "VIDEO" },
                                    label = { Text("🎬 Veo Video Anim", fontSize = 11.sp) }
                                )
                            }
                        }

                        if (studioMediaType == "IMAGE") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Resolution:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("1K", "2K", "4K").forEach { size ->
                                        SuggestionChip(
                                            onClick = { selectedImageSize = size },
                                            label = { Text(size, fontSize = 11.sp, fontWeight = if (selectedImageSize == size) FontWeight.Bold else FontWeight.Normal) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (selectedImageSize == size) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Veo Aspect Ratio:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("16:9", "9:16").forEach { ratio ->
                                        SuggestionChip(
                                            onClick = { selectedVideoAspectRatio = ratio },
                                            label = { Text(if (ratio == "16:9") "16:9 Landscape" else "9:16 Portrait", fontSize = 11.sp, fontWeight = if (selectedVideoAspectRatio == ratio) FontWeight.Bold else FontWeight.Normal) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (selectedVideoAspectRatio == ratio) TelegramBlue.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Animate Photo:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                OutlinedButton(
                                    onClick = {
                                        photoAttached = !photoAttached
                                        Toast.makeText(context, if (photoAttached) "Photo attached for Veo animation!" else "Photo unattached", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (photoAttached) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (photoAttached) "Product Photo Attached" else "Upload Photo", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Prompt Pills
            if (selectedMode != AiMode.CREATIVE_STUDIO) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val promptList = if (selectedMode == AiMode.COPILOT_MAPS) copilotPrompts else thinkingPrompts
                    items(promptList) { prompt ->
                        SuggestionChip(
                            onClick = {
                                if (selectedMode == AiMode.COPILOT_MAPS) {
                                    viewModel.sendAiPrompt(prompt)
                                } else {
                                    viewModel.sendHighThinkingPrompt(prompt)
                                }
                            },
                            label = { Text(prompt, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Voice Assistant Listening Banner
            if (isVoiceListening) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = "Listening Audio Wave", tint = MaterialTheme.colorScheme.primary)
                            Text("Listening to voice command...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { viewModel.toggleVoiceListening() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Send Voice", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Chat & Creative Output Feed
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(aiChats) { chat ->
                    val isUser = chat.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isUser) "You" else "BizPilot AI CEO",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUser) Color.White.copy(alpha = 0.8f) else SecondaryAmber
                                    )

                                    if (!isUser) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Speak / Stop Read Aloud Button
                                            IconButton(
                                                onClick = {
                                                    if (isSpeakingTts) {
                                                        voiceManager.stopSpeaking()
                                                    } else {
                                                        voiceManager.speakAiCeoResponse(chat.message)
                                                        Toast.makeText(context, "🔊 Reading response aloud...", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSpeakingTts) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                    contentDescription = "Read Response Aloud",
                                                    tint = if (isSpeakingTts) Color(0xFFEF4444) else MpesaGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            // Copy Response Button
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(chat.message))
                                                    Toast.makeText(context, "📋 Response copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Text",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = chat.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(
                                text = when (selectedMode) {
                                    AiMode.COPILOT_MAPS -> "Gemini 3.5 Flash querying Google Maps location data..."
                                    AiMode.HIGH_THINKING -> "Gemini 3.1 Pro performing HIGH level step-by-step thinking..."
                                    AiMode.CREATIVE_STUDIO -> if (studioMediaType == "IMAGE") "Gemini 3 Pro generating $selectedImageSize high-res image..." else "Veo 3.1 Fast generating $selectedVideoAspectRatio animated video..."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Live Voice-to-Text Speech Recognition Status Banner
            AnimatedVisibility(
                visible = isListening,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MpesaGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444),
                                modifier = Modifier
                                    .size(10.dp)
                                    .scale(micScale)
                            ) {}
                            Text(
                                text = if (speechTranscript.isNotBlank()) "🎙️ Listening: \"$speechTranscript\"" else "🎙️ $voiceStatus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showMicDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Full Mic Mode", fontSize = 11.sp, color = SecondaryAmber, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { voiceManager.stopListening() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Done", fontSize = 11.sp, color = MpesaGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = if (isListening) "🎙️ Listening... Speak your query to AI CEO" else when (selectedMode) {
                                    AiMode.COPILOT_MAPS -> "Ask AI CEO or speak your query..."
                                    AiMode.HIGH_THINKING -> "Describe complex strategy or speak prompt..."
                                    AiMode.CREATIVE_STUDIO -> if (studioMediaType == "IMAGE") "Describe image or speak prompt ($selectedImageSize)..." else "Describe video promo or speak prompt ($selectedVideoAspectRatio)..."
                                }
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Voice-to-Text Microphone Input Button
                    Surface(
                        shape = CircleShape,
                        color = if (isListening) MpesaGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isListening) BorderStroke(1.5.dp, Color.White) else null,
                        modifier = Modifier
                            .scale(if (isListening) micScale else 1.0f)
                            .testTag("voice_to_text_input_button")
                            .clickable {
                                if (isListening) {
                                    voiceManager.stopListening()
                                } else {
                                    if (hasAudioPermission) {
                                        voiceManager.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicNone else Icons.Default.Mic,
                                contentDescription = "Voice-to-Text Speech Input",
                                tint = if (isListening) Color.White else MpesaGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                voiceManager.stopListening()
                                when (selectedMode) {
                                    AiMode.COPILOT_MAPS -> viewModel.sendAiPrompt(textInput)
                                    AiMode.HIGH_THINKING -> viewModel.sendHighThinkingPrompt(textInput)
                                    AiMode.CREATIVE_STUDIO -> {
                                        if (studioMediaType == "IMAGE") {
                                            viewModel.generateStudioImage(textInput, selectedImageSize) {
                                                Toast.makeText(context, "Image generated successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            viewModel.generateStudioVideo(textInput, selectedVideoAspectRatio) {
                                                Toast.makeText(context, "Veo Video generated successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                                textInput = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Floating Voice Assistant Mic
        VoiceFab(
            isListening = isVoiceListening || showMicDialog,
            onClick = { showMicDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
        )
    }
}

