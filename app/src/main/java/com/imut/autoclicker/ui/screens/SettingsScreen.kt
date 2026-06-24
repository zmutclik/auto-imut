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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imut.autoclicker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val defaultClickInterval by viewModel.defaultClickInterval.collectAsState()
    val defaultClickDuration by viewModel.defaultClickDuration.collectAsState()
    val overlaySize by viewModel.overlaySize.collectAsState()
    val overlayOpacity by viewModel.overlayOpacity.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val ignoreBatteryOpt by viewModel.ignoreBatteryOpt.collectAsState()
    val showNotification by viewModel.showNotification.collectAsState()
    val vibrateOnTap by viewModel.vibrateOnTap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("Settings") })

        Column(modifier = Modifier.padding(16.dp)) {
            // General Section
            SectionHeader("GENERAL")

            StepperRow(
                label = "Default Click Interval",
                value = "${defaultClickInterval}ms",
                onDecrement = {
                    val step = if (defaultClickInterval <= 100) 10L else if (defaultClickInterval <= 1000) 50L else 100L
                    viewModel.setDefaultClickInterval((defaultClickInterval - step).coerceAtLeast(50L))
                },
                onIncrement = {
                    val step = if (defaultClickInterval < 100) 10L else if (defaultClickInterval < 1000) 50L else 100L
                    viewModel.setDefaultClickInterval(defaultClickInterval + step)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StepperRow(
                label = "Default Click Duration",
                value = "${defaultClickDuration}ms",
                onDecrement = {
                    viewModel.setDefaultClickDuration((defaultClickDuration - 10L).coerceAtLeast(10L))
                },
                onIncrement = {
                    viewModel.setDefaultClickDuration(defaultClickDuration + 10L)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Overlay Section
            SectionHeader("OVERLAY")

            Text(
                text = "Overlay Size",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("small", "medium", "large").forEach { size ->
                    FilterChip(
                        selected = overlaySize == size,
                        onClick = { viewModel.setOverlaySize(size) },
                        label = {
                            Text(
                                text = size.replaceFirstChar { it.uppercase() },
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Opacity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("0%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = overlayOpacity,
                    onValueChange = { viewModel.setOverlayOpacity(it) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("100%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "${(overlayOpacity * 100).toInt()}%",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            SectionHeader("APPEARANCE")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("light" to "☀️ Light", "dark" to "🌙 Dark", "system" to "💻 System").forEach { (mode, label) ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Advanced Section
            SectionHeader("ADVANCED")

            ToggleRow(
                label = "🔋 Ignore Battery Optimization",
                checked = ignoreBatteryOpt,
                onCheckedChange = { viewModel.setIgnoreBatteryOpt(it) }
            )

            ToggleRow(
                label = "🔔 Show Notification",
                checked = showNotification,
                onCheckedChange = { viewModel.setShowNotification(it) }
            )

            ToggleRow(
                label = "📳 Vibrate on Tap",
                checked = vibrateOnTap,
                onCheckedChange = { viewModel.setVibrateOnTap(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SectionHeader("ABOUT")

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.resetAll() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
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

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            // Simple toggle indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (checked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    text = if (checked) "ON" else "OFF",
                    color = if (checked)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
