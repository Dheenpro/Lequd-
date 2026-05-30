package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSwitch
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val blurIntensity by viewModel.blurIntensity.collectAsState()

    val isSkipSilenceEnabled by viewModel.isSkipSilenceEnabled.collectAsState()
    val isCrossfadeEnabled by viewModel.isCrossfadeEnabled.collectAsState()
    val isVolumeNormalizationEnabled by viewModel.isVolumeNormalizationEnabled.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "APPLICATION VISUAL THEME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Premium Liquid Theme select cards
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("LIGHT", "DARK", "GLASS")
                    modes.forEach { mode ->
                        val isSelected = themeMode == mode
                        val bg = if (isSelected) Color(0x44FFFFFF) else Color(0x0EFFFFFF)
                        val borderCol = if (isSelected) Color(0xFF00E5FF) else Color(0x1BFFFFFF)

                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (mode == "LIGHT") "☀️" else if (mode == "DARK") "🌑" else "🔮",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0x80FFFFFF)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Blur Intensity Config
                Text(
                    text = "BLUR LEVEL DEVIATIONS: ${(blurIntensity * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = blurIntensity,
                    onValueChange = { viewModel.setBlurIntensity(it) },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF00E5FF)
                    )
                )
            }
        }

        // Processing Sound Engine Variables section
        item {
            Text(
                text = "PLAYBACK ENGINE CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Crossfade Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Crossfade Transitions", color = Color.White, fontSize = 14.sp)
                            Text("Fade between overlapping track paths", color = Color(0x80FFFFFF), fontSize = 11.sp)
                        }
                        GlassSwitch(
                            checked = isCrossfadeEnabled,
                            onCheckedChange = { viewModel.toggleCrossfade(it) }
                        )
                    }

                    // Volume Normalizations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Volume Normalization", color = Color.White, fontSize = 14.sp)
                            Text("ReplayGain peaks safety capping", color = Color(0x80FFFFFF), fontSize = 11.sp)
                        }
                        GlassSwitch(
                            checked = isVolumeNormalizationEnabled,
                            onCheckedChange = { viewModel.toggleVolumeNormalization(it) }
                        )
                    }

                    // Skip Silence Thresholds
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Skip Silence Triggers", color = Color.White, fontSize = 14.sp)
                            Text("Auto skip trailing decibel quiet zones", color = Color(0x80FFFFFF), fontSize = 11.sp)
                        }
                        GlassSwitch(
                            checked = isSkipSilenceEnabled,
                            onCheckedChange = { viewModel.toggleSkipSilence(it) }
                        )
                    }
                }
            }
        }

        // Backups & Maintenance triggers
        item {
            Text(
                text = "BACKUP, RESTORE & DATA MAINTENANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Database Backups", color = Color.White, fontSize = 14.sp)
                            Text("Save playlists and metadata schema offline", color = Color(0x66FFFFFF), fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassButton(
                                onClick = { viewModel.runBackup() },
                                cornerRadius = 8.dp,
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Backup 💾", fontSize = 11.sp, color = Color.White)
                            }
                            GlassButton(
                                onClick = { viewModel.runRestore() },
                                cornerRadius = 8.dp,
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Restore 🔄", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    Divider(color = Color(0x13FFFFFF))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Engine Temp Cache", color = Color.White, fontSize = 14.sp)
                            Text("Remove images, indexing lists and sync caches", color = Color(0x66FFFFFF), fontSize = 11.sp)
                        }
                        GlassButton(
                            onClick = { viewModel.clearCache() },
                            cornerRadius = 8.dp,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Flush Cache", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
