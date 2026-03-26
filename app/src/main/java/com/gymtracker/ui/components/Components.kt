package com.gymtracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.ui.theme.*

// ═══════════════════════════════════════════════════════════════
// STAT CARD
// ═══════════════════════════════════════════════════════════════
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// GRADIENT BUTTON
// ═══════════════════════════════════════════════════════════════
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = listOf(OrangePrimary, OrangeDark)
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) Brush.horizontalGradient(colors)
                    else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EXERCISE CARD
// ═══════════════════════════════════════════════════════════════
@Composable
fun ExerciseCard(
    name: String,
    muscleGroup: String,
    equipment: String,
    difficulty: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Muscle group color indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getMuscleColor(muscleGroup).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getEquipmentIcon(equipment),
                    contentDescription = null,
                    tint = getMuscleColor(muscleGroup),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipLabel(muscleGroup, getMuscleColor(muscleGroup))
                    ChipLabel(equipment, MaterialTheme.colorScheme.secondary)
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChipLabel(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT SESSION CARD
// ═══════════════════════════════════════════════════════════════
@Composable
fun WorkoutSessionCard(
    name: String,
    date: String,
    duration: String,
    volume: String,
    splitType: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ChipLabel(splitType, MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat(Icons.Filled.Timer, duration, "Duration")
                MiniStat(Icons.Filled.FitnessCenter, volume, "Volume")
            }
        }
    }
}

@Composable
fun MiniStat(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════════
// NUMBER INPUT FIELD
// ═══════════════════════════════════════════════════════════════
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d*$"))) {
                onValueChange(new)
            }
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        singleLine = true,
        suffix = if (suffix.isNotEmpty()) {{ Text(suffix, style = MaterialTheme.typography.bodySmall) }} else null,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

// ═══════════════════════════════════════════════════════════════
// REST TIMER DISPLAY
// ═══════════════════════════════════════════════════════════════
@Composable
fun RestTimerDisplay(
    secondsRemaining: Int,
    totalSeconds: Int,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds else 0f
    val isLastFive = secondsRemaining in 1..5
    val timerColor = if (isLastFive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    timerColor.copy(alpha = 0.08f),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                tint = timerColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "REST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = timerColor,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    "Skip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
            color = timerColor,
            trackColor = timerColor.copy(alpha = 0.15f),
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// PR CELEBRATION ANIMATION
// ═══════════════════════════════════════════════════════════════
@Composable
fun PRCelebration(
    visible: Boolean,
    exerciseName: String,
    weight: String,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(300)) + fadeIn(),
        exit = scaleOut(tween(200)) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onDismiss),
            colors = CardDefaults.cardColors(containerColor = TealSuccess.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(TealSuccess, WarningOrange))
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "NEW PERSONAL RECORD!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TealSuccess
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    weight,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = WarningOrange
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION HEADER
// ═══════════════════════════════════════════════════════════════
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════════════════════════════
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(text = actionLabel, onClick = onAction)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════
fun getMuscleColor(muscle: String): Color {
    return when (muscle.lowercase()) {
        "chest"      -> Color(0xFFFF6B35)  // orange
        "back"       -> Color(0xFF4FC3F7)  // sky blue
        "shoulders"  -> Color(0xFFFFB300)  // amber
        "biceps"     -> Color(0xFF26C6A6)  // teal
        "triceps"    -> Periwinkle  // periwinkle
        "forearms"   -> Color(0xFF80CBC4)  // light teal
        "abs/core"   -> Color(0xFFFF8E5E)  // light orange
        "quads"      -> Color(0xFF81D4FA)  // light blue
        "hamstrings" -> Color(0xFF4DB6AC)  // teal-green
        "glutes"     -> Color(0xFFFFCC02)  // gold
        "calves"     -> Color(0xFF80DEEA)  // cyan
        else         -> Color(0xFF888899)
    }
}

fun getEquipmentIcon(equipment: String): ImageVector {
    return when (equipment.lowercase()) {
        "barbell" -> Icons.Filled.FitnessCenter
        "dumbbell" -> Icons.Filled.FitnessCenter
        "cable" -> Icons.Filled.Cable
        "machine" -> Icons.Filled.Settings
        "bodyweight" -> Icons.Filled.AccessibilityNew
        "kettlebell" -> Icons.Filled.FitnessCenter
        "smith machine" -> Icons.Filled.Straighten
        "ez bar" -> Icons.Filled.FitnessCenter
        else -> Icons.Filled.FitnessCenter
    }
}
