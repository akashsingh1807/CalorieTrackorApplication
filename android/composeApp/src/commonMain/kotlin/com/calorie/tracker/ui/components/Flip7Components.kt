package com.calorie.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorie.tracker.ui.theme.*

fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val transparentColor = color.copy(alpha = 0f).toArgb()
    val shadowColor = color.copy(alpha = alpha).toArgb()
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

enum class Flip7ButtonVariant { PRIMARY_GOLD, TEAL, CORAL, SKY_BLUE, MONOCHROME, GRAY }

@Composable
fun Flip7Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: Flip7ButtonVariant = Flip7ButtonVariant.PRIMARY_GOLD,
    enabled: Boolean = true
) {
    val (bgColor, shadowColor, textColor) = when (variant) {
        Flip7ButtonVariant.PRIMARY_GOLD -> Triple(AccentGold, AccentDark, PrimaryDark)
        Flip7ButtonVariant.TEAL -> Triple(PrimaryTeal, PrimaryDark, Color.White)
        Flip7ButtonVariant.CORAL -> Triple(CoralPrimary, CoralDark, Color.White)
        Flip7ButtonVariant.SKY_BLUE -> Triple(SkyBlue, Color(0xFF2980B9), Color.White)
        Flip7ButtonVariant.MONOCHROME -> Triple(Color.Black, Color.DarkGray, Color.White)
        Flip7ButtonVariant.GRAY -> Triple(Color.LightGray, Color.Gray, Color.DarkGray)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .coloredShadow(
                color = shadowColor,
                alpha = 0.4f,
                borderRadius = 50.dp,
                shadowRadius = 16.dp,
                offsetY = 6.dp
            ),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.DarkGray
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

enum class Flip7CardVariant { TEAL, GOLD, CORAL, MONOCHROME }

@Composable
fun Flip7Card(
    modifier: Modifier = Modifier,
    variant: Flip7CardVariant = Flip7CardVariant.TEAL,
    content: @Composable ColumnScope.() -> Unit
) {
    val accentColor = when (variant) {
        Flip7CardVariant.TEAL -> PrimaryTeal
        Flip7CardVariant.GOLD -> AccentGold
        Flip7CardVariant.CORAL -> CoralPrimary
        Flip7CardVariant.MONOCHROME -> Color.Black
    }
    
    val shadowColor = when (variant) {
        Flip7CardVariant.TEAL -> PrimaryTeal
        Flip7CardVariant.GOLD -> AccentGold
        Flip7CardVariant.CORAL -> CoralPrimary
        Flip7CardVariant.MONOCHROME -> Color.Black
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .coloredShadow(
                color = shadowColor,
                alpha = 0.15f,
                borderRadius = 24.dp,
                shadowRadius = 20.dp,
                offsetY = 4.dp
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left colored accent bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(accentColor)
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Flip7TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = PrimaryTeal,
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = PrimaryTeal,
            unfocusedLabelColor = Color.Gray
        ),
        keyboardOptions = keyboardOptions,
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
fun Flip7SectionTitle(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(CreamSurface, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 20.sp),
                color = PrimaryDark
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Dashed bottom border
        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
            drawLine(
                color = PrimaryTeal.copy(alpha = 0.3f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 3.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }
    }
}
