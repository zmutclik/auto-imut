package com.imut.autoclicker.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imut.autoclicker.gesture.GesturePoint
import com.imut.autoclicker.gesture.GestureType
import com.imut.autoclicker.gesture.LoopMode
import com.imut.autoclicker.viewmodel.GestureConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: GestureConfigViewModel = hiltViewModel()
) {
    val points by viewModel.points.collectAsState()
    val gestureType by viewModel.gestureType.collectAsState()
    val loopMode by viewModel.loopMode.collectAsState()
    val loopCount by viewModel.loopCount.collectAsState()
    val delayBetweenPoints by viewModel.delayBetweenPoints.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Gesture Config") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = { /* Save macro dialog */ }) {
                    Text("💾 Save", color = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Gesture Type
            Text(
                text = "GESTURE TYPE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GestureType.entries.forEach { type ->
                    FilterChip(
                        selected = gestureType == type,
                        onClick = { viewModel.setGestureType(type) },
                        label = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (type) {
                                        GestureType.TAP -> "🖱️"
                                        GestureType.SWIPE -> "👆"
                                        GestureType.LONG_PRESS -> "👆"
                                    },
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = when (type) {
                                        GestureType.TAP -> "Tap"
                                        GestureType.SWIPE -> "Swipe"
                                        GestureType.LONG_PRESS -> "Long Press"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Click Points
            Text(
                text = if (gestureType == GestureType.SWIPE) "SWIPE PATH" else "CLICK POINTS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (points.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No points added yet.\nTap '+ Add New Point' to start.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            points.forEachIndexed { index, point ->
                PointRow(
                    index = index,
                    point = point,
                    isSwipe = gestureType == GestureType.SWIPE,
                    onDelete = { viewModel.removePoint(index) }
                )

                if (gestureType == GestureType.SWIPE && index < points.lastIndex) {
                    Text(
                        text = "↓",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }

            // Add point button
            OutlinedButton(
                onClick = {
                    // Add a demo point (in real app, this would open a point picker)
                    val demoX = (100..900).random().toFloat()
                    val demoY = (200..1600).random().toFloat()
                    viewModel.addPoint(demoX, demoY)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("+ Add New Point")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sequence Settings
            Text(
                text = "SEQUENCE SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Loop Mode
            Text(
                text = "Loop Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoopMode.entries.forEach { mode ->
                    FilterChip(
                        selected = loopMode == mode,
                        onClick = { viewModel.setLoopMode(mode) },
                        label = {
                            Text(
                                text = when (mode) {
                                    LoopMode.ONCE -> "Once"
                                    LoopMode.INFINITE -> "∞ Infinite"
                                    LoopMode.CUSTOM -> "Custom"
                                },
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            // Custom loop count (only when CUSTOM mode)
            if (loopMode == LoopMode.CUSTOM) {
                Spacer(modifier = Modifier.height(12.dp))
                StepperRow(
                    label = "Loop Count",
                    value = "$loopCount times",
                    onDecrement = { viewModel.decrementLoopCount() },
                    onIncrement = { viewModel.incrementLoopCount() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StepperRow(
                label = "Delay Between Points",
                value = "${delayBetweenPoints}ms",
                onDecrement = { viewModel.decrementDelay() },
                onIncrement = { viewModel.incrementDelay() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Test button
            Button(
                onClick = { viewModel.testSequence() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "▶ TEST SEQUENCE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PointRow(
    index: Int,
    point: GesturePoint,
    isSwipe: Boolean,
    onDelete: () -> Unit
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Point number
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "${index + 1}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Coordinates
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (isSwipe && point.type == GestureType.SWIPE) {
                    Text(
                        text = "Start (${point.x.toInt()}, ${point.y.toInt()})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "End (${point.endX.toInt()}, ${point.endY.toInt()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "(${point.x.toInt()}, ${point.y.toInt()})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Gesture type tag
            Text(
                text = when (point.type) {
                    GestureType.TAP -> "Tap · ${point.duration}ms"
                    GestureType.SWIPE -> "Swipe →"
                    GestureType.LONG_PRESS -> "Long Press"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Delete button
            IconButton(onClick = onDelete) {
                Text("✕", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
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
                    textAlign = TextAlign.Center,
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
