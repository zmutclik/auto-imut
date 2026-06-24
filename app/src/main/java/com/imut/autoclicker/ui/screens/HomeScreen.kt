package com.imut.autoclicker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imut.autoclicker.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGestureConfig: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val isRunning by viewModel.isExecuting.collectAsState()
    val isPaused by viewModel.isServicePaused.collectAsState()
    val clickInterval by viewModel.clickInterval.collectAsState()
    val repeatCount by viewModel.repeatCount.collectAsState()
    val points by viewModel.points.collectAsState()
    val executionCount by viewModel.executionCount.collectAsState()

    val isAccessibilityEnabled = viewModel.isAccessibilityEnabled()
    val isOverlayEnabled = viewModel.isOverlayEnabled()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("AutoClicker") },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Status Card
            StatusCard(
                isRunning = isRunning,
                isPaused = isPaused,
                currentPoint = points.firstOrNull(),
                executionCount = executionCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Permissions Section
            Text(
                text = "PERMISSIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PermissionRow(
                icon = "🪟",
                label = "Overlay Permission",
                isGranted = isOverlayEnabled,
                onClick = { viewModel.openOverlaySettings() }
            )

            PermissionRow(
                icon = "♿",
                label = "Accessibility Service",
                isGranted = isAccessibilityEnabled,
                onClick = { viewModel.openAccessibilitySettings() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Config Section
            Text(
                text = "QUICK CONFIG",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            StepperRow(
                label = "Click Interval",
                value = "${clickInterval}ms",
                onDecrement = { viewModel.decrementInterval() },
                onIncrement = { viewModel.incrementInterval() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StepperRow(
                label = "Repeat Count",
                value = if (repeatCount == 0) "∞ Infinite" else "$repeatCount times",
                onDecrement = { viewModel.decrementRepeat() },
                onIncrement = { viewModel.incrementRepeat() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            OutlinedButton(
                onClick = onNavigateToGestureConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("📍 Select Click Points", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.stopAutoClicker()
                    } else {
                        viewModel.startAutoClicker()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        Color(0xFF238636)
                )
            ) {
                Text(
                    text = if (isRunning) "⏹ STOP AUTO-CLICKER" else "▶ START AUTO-CLICKER",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    isRunning: Boolean,
    isPaused: Boolean,
    currentPoint: com.imut.autoclicker.gesture.GesturePoint?,
    executionCount: Int
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            isRunning && !isPaused -> Color(0xFF3FB950)
            isPaused -> Color(0xFFD29922)
            else -> Color(0xFFF85149)
        },
        label = "statusColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            androidx.compose.foundation.Canvas(modifier = Modifier.padding(end = 12.dp)) {
                drawCircle(
                    color = statusColor,
                    radius = 8.dp.toPx()
                )
            }

            Column {
                Text(
                    text = when {
                        isRunning && !isPaused -> "Service: ACTIVE"
                        isPaused -> "Service: PAUSED"
                        else -> "Service: INACTIVE"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when {
                        isRunning && currentPoint != null -> "Mengklik di (${currentPoint.x.toInt()}, ${currentPoint.y.toInt()}) · Count: $executionCount"
                        isPaused -> "Dijeda"
                        else -> "Tap start to begin"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: String,
    label: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            if (isGranted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = Color(0xFF3FB950)
                )
            } else {
                FilledTonalButton(onClick = onClick) {
                    Text("Enable →", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDecrement,
                modifier = Modifier.weight(1f)
            ) {
                Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Card(
                modifier = Modifier.weight(3f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedButton(
                onClick = onIncrement,
                modifier = Modifier.weight(1f)
            ) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
