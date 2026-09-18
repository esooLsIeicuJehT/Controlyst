package com.example.ui.mapper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MappingConfig
import com.example.model.MappingNode
import com.example.model.NodeType
import com.example.model.*
import com.example.ui.MainAppViewModel
import com.example.ui.theme.*
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotMapperScreen(
    viewModel: MainAppViewModel
) {
    val activeConfig by viewModel.activeConfig.collectAsState()
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var snapToGrid by remember { mutableStateOf(false) }
    var isLiveTestMode by remember { mutableStateOf(false) }
    var isDiffMode by remember { mutableStateOf(false) }
    var testTappedFeedback by remember { mutableStateOf<String?>(null) }

    var showAddNodeDialog by remember { mutableStateOf(false) }

    val selectedNode = activeConfig.buttons.firstOrNull { it.id == selectedNodeId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Toolbar
        Surface(
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = activeConfig.profileName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${activeConfig.buttons.size} Nodes • ${activeConfig.targetAspectRatio}",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Snap to Grid toggle
                    IconButton(
                        onClick = { snapToGrid = !snapToGrid },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (snapToGrid) CyberCyan.copy(alpha = 0.2f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = "Snap Grid",
                            tint = if (snapToGrid) CyberCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // AI Assistant Suggestion button
                    IconButton(
                        onClick = { viewModel.runAiAssistant() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElectricViolet.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "AI Assistant",
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Auto-detect HUD button
                    IconButton(
                        onClick = { viewModel.runAiHudScan() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = CyberCyan.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = "Auto Detect",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Diff Mode toggle
                    IconButton(
                        onClick = { viewModel.runConfigDiff() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isDiffMode) AccentAmber.copy(alpha = 0.2f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            Icons.Default.Compare,
                            contentDescription = "Diff HUD",
                            tint = if (isDiffMode) AccentAmber else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Emergency Panic Kill-Switch
                    IconButton(
                        onClick = { viewModel.triggerPanicKillSwitch() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AccentRose.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            Icons.Default.FlashOff,
                            contentDescription = "Panic Kill Switch",
                            tint = AccentRose,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Live Test Mode toggle
                    Button(
                        onClick = { isLiveTestMode = !isLiveTestMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiveTestMode) AccentGreen else DarkSurfaceBorder
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (isLiveTestMode) Icons.Default.TouchApp else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isLiveTestMode) Color(0xFF00363D) else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isLiveTestMode) "Testing" else "Live Test",
                            color = if (isLiveTestMode) Color(0xFF00363D) else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Test Mode Banner if active
        if (isLiveTestMode) {
            Surface(
                color = AccentGreen.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, AccentGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = testTappedFeedback ?: "Tap any mapped node on the screenshot to test synthetic injection feedback",
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Visual Mapping Canvas (Scaled HUD viewport)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D131F))
                .border(1.dp, if (isDiffMode) AccentAmber else DarkSurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isLiveTestMode, snapToGrid, activeConfig.buttons) {
                        if (isLiveTestMode) {
                            detectTapGestures { offset ->
                                val xNorm = offset.x / size.width
                                val yNorm = offset.y / size.height
                                val hit = activeConfig.buttons.firstOrNull { node ->
                                    val dist = hypot(xNorm - node.xNorm, yNorm - node.yNorm)
                                    dist <= (node.radiusNorm * 1.5f)
                                }
                                if (hit != null) {
                                    viewModel.currentInjector.injectTap(offset.x, offset.y)
                                    testTappedFeedback = "Triggered: ${hit.boundKey} (${hit.label}) @ (${(xNorm*100).toInt()}%, ${(yNorm*100).toInt()}%)"
                                }
                            }
                        } else {
                            detectTapGestures { offset ->
                                val xNorm = offset.x / size.width
                                val yNorm = offset.y / size.height
                                val hit = activeConfig.buttons.firstOrNull { node ->
                                    val dist = hypot(xNorm - node.xNorm, yNorm - node.yNorm)
                                    dist <= (node.radiusNorm * 1.8f)
                                }
                                selectedNodeId = hit?.id
                            }
                        }
                    }
                    .pointerInput(isLiveTestMode, snapToGrid, selectedNodeId) {
                        if (!isLiveTestMode) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                selectedNodeId?.let { id ->
                                    val target = activeConfig.buttons.firstOrNull { it.id == id }
                                    if (target != null) {
                                        var newX = (target.xNorm + dragAmount.x / size.width).coerceIn(0.02f, 0.98f)
                                        var newY = (target.yNorm + dragAmount.y / size.height).coerceIn(0.02f, 0.98f)
                                        if (snapToGrid) {
                                            newX = (Math.round(newX * 20) / 20f)
                                            newY = (Math.round(newY * 20) / 20f)
                                        }
                                        viewModel.updateNode(target.copy(xNorm = newX, yNorm = newY))
                                    }
                                }
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw Grid if enabled
                if (snapToGrid) {
                    val stepX = canvasW / 20f
                    val stepY = canvasH / 20f
                    for (i in 1..19) {
                        drawLine(
                            color = Color(0x1800F0FF),
                            start = Offset(i * stepX, 0f),
                            end = Offset(i * stepX, canvasH),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0x1800F0FF),
                            start = Offset(0f, i * stepY),
                            end = Offset(canvasW, i * stepY),
                            strokeWidth = 1f
                        )
                    }
                }

                // Simulated Game HUD background elements
                drawCircle(
                    color = Color(0x221E293B),
                    radius = canvasW * 0.12f,
                    center = Offset(canvasW * 0.18f, canvasH * 0.72f),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )

                // Draw Nodes
                activeConfig.buttons.forEach { node ->
                    val cx = node.xNorm * canvasW
                    val cy = node.yNorm * canvasH
                    val radius = node.radiusNorm * canvasW
                    val isSelected = node.id == selectedNodeId

                    val nodeColor = when (node.type) {
                        NodeType.BUTTON -> if (node.boundKey.contains("R") || node.boundKey == "A") AccentRose else CyberCyan
                        NodeType.JOYSTICK_ZONE -> CyberCyan
                        NodeType.CAMERA_DRAG -> ElectricViolet
                        NodeType.TURBO -> AccentAmber
                        NodeType.MACRO -> AccentGreen
                    }

                    // Node Fill
                    drawCircle(
                        color = nodeColor.copy(alpha = if (isSelected) 0.45f else 0.22f),
                        radius = radius,
                        center = Offset(cx, cy)
                    )

                    // Node Border
                    drawCircle(
                        color = if (isSelected) Color.White else nodeColor,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = if (isSelected) 3f else 1.8f)
                    )

                    // Reticle center point
                    drawCircle(
                        color = if (isSelected) Color.White else nodeColor,
                        radius = 4f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        // Node Inspector Bottom Panel
        if (selectedNode != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, CyberCyan)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyberCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = selectedNode.boundKey,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = selectedNode.label.ifEmpty { "Node ${selectedNode.id}" },
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.removeNode(selectedNode.id)
                                selectedNodeId = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Radius slider & coordinates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Size", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(36.dp))
                        Slider(
                            value = selectedNode.radiusNorm,
                            onValueChange = { newRadius ->
                                viewModel.updateNode(selectedNode.copy(radiusNorm = newRadius))
                            },
                            valueRange = 0.02f..0.18f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan
                            )
                        )
                        Text(
                            text = "${(selectedNode.radiusNorm * 100).toInt()}%",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp)
                        )
                    }

                    // Key binding buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("A", "B", "X", "Y", "LT", "RT", "LB", "RB", "LS", "RS").forEach { key ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedNode.boundKey == key) CyberCyan else DarkSurfaceBorder,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.updateNode(selectedNode.copy(boundKey = key))
                                    }
                            ) {
                                Text(
                                    text = key,
                                    color = if (selectedNode.boundKey == key) Color(0xFF00363D) else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Add Node Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddNodeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color(0xFF00363D))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Button Node", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val joyNode = MappingNode(
                            id = "joy_${System.currentTimeMillis()}",
                            xNorm = 0.2f,
                            yNorm = 0.7f,
                            radiusNorm = 0.12f,
                            type = NodeType.JOYSTICK_ZONE,
                            boundKey = "LS",
                            label = "Thumbstick"
                        )
                        viewModel.addNode(joyNode)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = BorderStroke(1.dp, CyberCyan)
                ) {
                    Text("+ Thumbstick")
                }
            }
        }
    }

    // Add Node Dialog
    if (showAddNodeDialog) {
        var newBoundKey by remember { mutableStateOf("A") }
        var newLabel by remember { mutableStateOf("") }
        var newType by remember { mutableStateOf(NodeType.BUTTON) }

        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = { Text("Add Mapping Node", color = CyberCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newBoundKey,
                        onValueChange = { newBoundKey = it.uppercase() },
                        label = { Text("Bound Key / Button") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                    )
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("HUD Action Label (e.g. Fire, Jump)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val node = MappingNode(
                            id = "btn_${System.currentTimeMillis()}",
                            xNorm = 0.5f,
                            yNorm = 0.5f,
                            radiusNorm = 0.055f,
                            type = newType,
                            boundKey = newBoundKey,
                            label = newLabel
                        )
                        viewModel.addNode(node)
                        showAddNodeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Place Node", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNodeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // AI HUD Candidates Review Dialog
    val aiCandidates by viewModel.aiHudCandidates.collectAsState()
    if (aiCandidates.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAiHudCandidates() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = CyberCyan)
                    Spacer(Modifier.width(8.dp))
                    Text("AI HUD Review (${aiCandidates.size} Elements)", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "AI Vision analyzed the HUD layout and detected the following controls. Confirm or reject bindings:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    aiCandidates.forEach { candidate ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(candidate.predictedAction, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    Text("Confidence ${(candidate.confidence * 100).toInt()}% • Pos: (${(candidate.xNorm * 100).toInt()}%, ${(candidate.yNorm * 100).toInt()}%)", fontSize = 10.sp, color = TextSecondary)
                                }
                                Surface(
                                    color = CyberCyan,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        candidate.recommendedKey,
                                        color = Color(0xFF00363D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAiHudCandidates(aiCandidates) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Apply All Detected", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAiHudCandidates() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // AI Mapping Assistant Suggestion Dialog
    val aiSuggestionState = viewModel.aiMappingSuggestion.collectAsState()
    val s = aiSuggestionState.value
    if (s != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAiAssistant() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = ElectricViolet)
                    Spacer(Modifier.width(8.dp))
                    Text("AI Mapping Assistant", color = ElectricViolet, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Optimized layout for ${s.gameTitle} using ${s.targetController.displayName}:",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(s.rationale, fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Est. Injection Latency:", fontSize = 11.sp, color = TextSecondary)
                        Text("${s.estimatedLatencyMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Mapped Nodes:", fontSize = 11.sp, color = TextSecondary)
                        Text("${s.nodes.size} controls", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.applyAiAssistantSuggestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Apply Suggestion", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAiAssistant() }) {
                    Text("Dismiss", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Config Diff Repair Dialog
    val diffResultState = viewModel.diffResult.collectAsState()
    val diff = diffResultState.value
    if (diff != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDiff() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Compare, contentDescription = null, tint = AccentAmber)
                    Spacer(Modifier.width(8.dp))
                    Text("HUD Diff Analysis", color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Compared existing config against new HUD screenshot:",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text("• Unchanged Controls: ${diff.unchangedCount}", fontSize = 11.sp, color = AccentGreen)
                    Text("• Moved Controls: ${diff.movedNodes.size}", fontSize = 11.sp, color = AccentAmber)
                    Text("• Missing/Relocated: ${diff.missingNodes.size}", fontSize = 11.sp, color = AccentRose)
                    Text("• Newly Detected: ${diff.newDetectedNodes.size}", fontSize = 11.sp, color = CyberCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tap 'Repair Config' to automatically shift moved controls to their new positions without losing your existing key bindings.",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.applyDiffRepair() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                ) {
                    Text("Repair Config", color = Color(0xFF2A1C0A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDiff() }) {
                    Text("Dismiss", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
