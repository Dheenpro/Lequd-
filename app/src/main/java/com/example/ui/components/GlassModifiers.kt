package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.GlassWhite

@Composable
fun Modifier.glassmorphic(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    intensity: Float = 1.0f // Slider value for blur intensity (0.5 to 2.0)
): Modifier {
    val glassColor = GlassWhite
    val glassBorder = GlassBorderWhite

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .drawBehind {
            // Renders translucent liquid glass effect background
            drawRect(
                color = glassColor
            )
        }
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    glassBorder,
                    Color(0x02FFFFFF)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

@Composable
fun Modifier.glassBackgroundDynamic(
    artworkColor: Color,
    intensity: Float = 1.0f
): Modifier {
    return this.drawBehind {
        // Draw real-time ambient art blur simulation using radial gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    artworkColor.copy(alpha = 0.25f * intensity),
                    Color.Transparent
                ),
                radius = size.width * 1.2f
            )
        )
        // Tint overlay
        drawRect(
            color = Color(0xBB0A0D15)
        )
    }
}
