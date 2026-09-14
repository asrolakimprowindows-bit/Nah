package com.micplugin.rvc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun RvcScreen(
    viewModel: RvcConnectionViewModel = hiltViewModel(),
) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    var endpointInput by remember { mutableStateOf("") }
    var pitch by remember { mutableStateOf(settings.pitch.toString()) }
    var formant by remember { mutableStateOf(settings.formant.toString()) }
    var indexRate by remember { mutableStateOf(settings.indexRate.toString()) }
    var chunkSize by remember { mutableStateOf(settings.chunkSize.toString()) }
    var hopLength by remember { mutableStateOf(settings.hopLength.toString()) }
    var selectedF0Method by remember { mutableStateOf(settings.f0Method) }
    var speakerId by remember { mutableStateOf(settings.speakerId.toString()) }
    var protectConsonants by remember { mutableStateOf(settings.protectConsonants) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F7))  // iOS light gray
            .padding(16.dp),
    ) {
        // Header
        Text(
            "🎙️ RVC Voice Converter",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Connection Status Card
        ConnectionStatusCard(connectionStatus)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Endpoint Input
        GlassmorphicCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kaggle Endpoint", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = endpointInput,
                    onValueChange = { endpointInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    placeholder = { Text("https://kaggle-...com/api") },
                    singleLine = true,
                )
                Button(
                    onClick = { viewModel.testConnection(endpointInput) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Test Connection")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Voice Parameters
        GlassmorphicCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Voice Parameters", style = MaterialTheme.typography.labelMedium)
                
                // Speaker ID
                Spacer(modifier = Modifier.height(12.dp))
                ParameterRow("Speaker ID", speakerId) { speakerId = it }
                
                // Pitch
                Spacer(modifier = Modifier.height(12.dp))
                ParameterRow("Pitch (-12 to +12)", pitch) { pitch = it }
                
                // Formant
                Spacer(modifier = Modifier.height(12.dp))
                ParameterRow("Formant (-3.0 to +3.0)", formant) { formant = it }
                
                // Index Rate
                Spacer(modifier = Modifier.height(12.dp))
                ParameterRow("Index Rate (0.0 to 1.0)", indexRate) { indexRate = it }
                
                // Protect Consonants
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Protect Consonants")
                    Checkbox(
                        checked = protectConsonants,
                        onCheckedChange = { protectConsonants = it }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Processing Parameters
        GlassmorphicCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Processing", style = MaterialTheme.typography.labelMedium)
                
                // F0 Method
                Spacer(modifier = Modifier.height(12.dp))
                Text("F0 Method", style = MaterialTheme.typography.labelSmall)
                F0MethodDropdown(selectedF0Method) { selectedF0Method = it }
                
                // Chunk Size
                Spacer(modifier = Modifier.height(12.dp))
                ParameterRow("Chunk Size (1-1000)", chunkSize) { chunkSize = it }
                
                // Hop Length
                Spacer(modifier = Modifier.height(12.dp))
                ParameterRow("Hop Length (1-512)", hopLength) { hopLength = it }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* Start recording and processing */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text("🎤 START")
            }
            
            OutlinedButton(
                onClick = { /* Stop */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text("STOP")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
    ) {
        content()
    }
}

@Composable
fun ConnectionStatusCard(status: ConnectionStatus) {
    val (statusColor, statusText, latencyText) = when (status) {
        ConnectionStatus.Connecting -> Triple(Color(0xFFFFA500), "🟡 Connecting...", "")
        is ConnectionStatus.Connected -> Triple(
            Color(0xFF34C759),
            "🟢 Connected (Kaggle Ready)",
            "Latency: ${status.latencyMs}ms"
        )
        is ConnectionStatus.Disconnected -> Triple(
            Color(0xFFFF3B30),
            "🔴 Disconnected",
            status.reason ?: "No connection"
        )
        is ConnectionStatus.Error -> Triple(
            Color(0xFFFF3B30),
            "🔴 Error",
            status.message
        )
    }
    
    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                statusText,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor,
            )
            if (latencyText.isNotEmpty()) {
                Text(
                    latencyText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ParameterRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(120.dp)
                .height(40.dp),
            textStyle = MaterialTheme.typography.labelSmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
    }
}

@Composable
fun F0MethodDropdown(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val f0Methods = listOf("RMVPE", "CREPE", "HARVEST", "PARSELMOUTH")
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected)
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            f0Methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) },
                    onClick = {
                        onSelect(method)
                        expanded = false
                    }
                )
            }
        }
    }
}
