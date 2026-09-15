package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Drone Takeoff Splash Screen.
 * Plays a startup animation where the drone from the app icon spools its rotors,
 * hovers, powers up thrust rings, and takes off smoothly into the sky before
 * transitioning to the home dashboard.
 */
@Composable
fun DroneTakeoffSplashScreen(
    onSplashFinished: () -> Unit
) {
    var animationStage by remember { mutableStateOf(0) } // 0: Spooling, 1: Hovering, 2: Takeoff, 3: Done

    // Infinite rotor spinning
    val infiniteTransition = rememberInfiniteTransition(label = "rotor_spin")
    val rotorAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotor_angle"
    )

    // Strobe beacon light
    val strobeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_alpha"
    )

    // Micro vibration for hover realism
    val hoverVibration by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_vibration"
    )

    // Main Takeoff Animation Timeline
    val takeoffOffset = remember { Animatable(0f) }
    val droneScale = remember { Animatable(0.8f) }
    val droneAlpha = remember { Animatable(0f) }
    val thrustRingScale = remember { Animatable(0.2f) }
    val thrustRingAlpha = remember { Animatable(0f) }
    val altitudeText = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Stage 0: Drone Fade In & Rotor Arming (0ms - 400ms)
        droneAlpha.animateTo(1f, animationSpec = tween(400))
        droneScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f))
        animationStage = 1

        // Stage 1: Rotor Spool-up & Systems Ready (400ms - 900ms)
        delay(500)
        animationStage = 2

        // Stage 2: Takeoff Thrust & Lift-off (900ms - 1900ms)
        thrustRingAlpha.snapTo(0.9f)
        coroutineScope {
            launch {
                thrustRingScale.animateTo(2.8f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            }
            launch {
                thrustRingAlpha.animateTo(0f, animationSpec = tween(900, easing = LinearEasing))
            }
            launch {
                altitudeText.animateTo(120f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            }
            launch {
                droneScale.animateTo(1.35f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            }
            launch {
                takeoffOffset.animateTo(
                    targetValue = -900f,
                    animationSpec = tween(durationMillis = 950, easing = FastOutLinearInEasing)
                )
            }
        }

        // Stage 3: Smooth Finish
        delay(100)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B), // Core deep slate
                        Color(0xFF0F172A), // Dark slate
                        Color(0xFF070B14)  // Luxury technical black
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            )
            .testTag("drone_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Tech Grid / Radar Circles in Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeColor = Color(0xFF06B6D4).copy(alpha = 0.08f)

            // Concentric Range Rings
            drawCircle(color = strokeColor, radius = size.width * 0.22f, center = center, style = Stroke(1.5f))
            drawCircle(color = strokeColor, radius = size.width * 0.40f, center = center, style = Stroke(1.5f))
            drawCircle(color = strokeColor, radius = size.width * 0.60f, center = center, style = Stroke(1.5f))

            // Crosshair lines
            drawLine(
                color = strokeColor,
                start = Offset(center.x - size.width * 0.45f, center.y),
                end = Offset(center.x + size.width * 0.45f, center.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = strokeColor,
                start = Offset(center.x, center.y - size.width * 0.45f),
                end = Offset(center.x, center.y + size.width * 0.45f),
                strokeWidth = 1.5f
            )
        }

        // Thrust Ring Wave beneath drone during takeoff
        if (thrustRingAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(thrustRingScale.value)
                    .alpha(thrustRingAlpha.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00F0FF).copy(alpha = 0.6f),
                                Color(0xFFF59E0B).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Animated Quadcopter Drone matching the App Launcher Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .offset(y = (takeoffOffset.value + if (animationStage < 2) hoverVibration else 0f).dp)
                .scale(droneScale.value)
                .alpha(droneAlpha.value)
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Vector Canvas Drone
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawDroneIcon(
                        rotorAngle = rotorAngle,
                        strobeAlpha = strobeAlpha,
                        isTakeoff = animationStage >= 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Flight HUD / System Status
            AnimatedVisibility(
                visible = animationStage < 2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color(0xFF06B6D4).copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (animationStage == 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (animationStage == 0) "MOTOROK ÉLESÍTÉSE..." else "FELSZÁLLÁS ENGEDÉLYEZVE",
                            color = if (animationStage == 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "DRÓN KALAUZ",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )

                    Text(
                        text = "Intelligens Magyar Repülési Térkép & Kísérő",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Altitude climb gauge during takeoff
            if (animationStage >= 2) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Color(0xFF00F0FF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FELSZÁLLÁS: ${altitudeText.value.toInt()} m",
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Bottom Tap to Skip Hint
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(0.6f)
        ) {
            Text(
                text = "Koppints a kihagyáshoz",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Custom Canvas drawing for the exact Drone Quadcopter model
 * matching the app's adaptive launcher icon with dynamic rotating propellers.
 */
private fun DrawScope.drawDroneIcon(
    rotorAngle: Float,
    strobeAlpha: Float,
    isTakeoff: Boolean
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val armLength = size.width * 0.32f
    val rotorRadius = size.width * 0.12f

    // 4 Arm Endpoints (45, 135, 225, 315 degrees)
    val angles = listOf(45.0, 135.0, 225.0, 315.0)
    val motorPositions = angles.map { deg ->
        val rad = Math.toRadians(deg)
        Offset(
            x = (center.x + armLength * cos(rad)).toFloat(),
            y = (center.y + armLength * sin(rad)).toFloat()
        )
    }

    // 1. Draw Quadcopter Carbon Fiber Arms (Cyan Accent #06B6D4)
    motorPositions.forEach { motorPos ->
        drawLine(
            color = Color(0xFF06B6D4),
            start = center,
            end = motorPos,
            strokeWidth = 9f,
            cap = StrokeCap.Round
        )
    }

    // 2. Draw Motor Mounts & Propeller Discs
    motorPositions.forEachIndexed { index, motorPos ->
        // Motor Pod Base
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 14f,
            center = motorPos
        )
        drawCircle(
            color = Color(0xFF06B6D4),
            radius = 14f,
            center = motorPos,
            style = Stroke(3f)
        )

        // Amber Glowing Propeller Disk Ring (#F59E0B)
        drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = if (isTakeoff) 0.85f else 0.5f),
            radius = rotorRadius,
            center = motorPos,
            style = Stroke(if (isTakeoff) 5f else 3.5f)
        )

        // Rotating Propeller Blades (High speed blur lines)
        val bladeAngle = rotorAngle + (index * 45f)
        val rad1 = Math.toRadians(bladeAngle.toDouble())
        val rad2 = Math.toRadians((bladeAngle + 180f).toDouble())

        drawLine(
            color = Color(0xFFF59E0B).copy(alpha = 0.9f),
            start = Offset(
                (motorPos.x + rotorRadius * 0.9f * cos(rad1)).toFloat(),
                (motorPos.y + rotorRadius * 0.9f * sin(rad1)).toFloat()
            ),
            end = Offset(
                (motorPos.x + rotorRadius * 0.9f * cos(rad2)).toFloat(),
                (motorPos.y + rotorRadius * 0.9f * sin(rad2)).toFloat()
            ),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Strobe Navigation LED (Front: Green/Cyan, Rear: Red/Amber)
        val ledColor = if (index < 2) Color(0xFF00F0FF) else Color(0xFFEF4444)
        drawCircle(
            color = ledColor.copy(alpha = strobeAlpha),
            radius = 6f,
            center = motorPos
        )
    }

    // 3. Central Drone Aerodynamic Fuselage / Body Pod
    val bodyRadius = size.width * 0.16f

    // Body shadow / glow
    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = 0.25f),
        radius = bodyRadius + 6f,
        center = center
    )

    // Main fuselage (#1E293B)
    drawCircle(
        color = Color(0xFF1E293B),
        radius = bodyRadius,
        center = center
    )
    drawCircle(
        color = Color(0xFF06B6D4),
        radius = bodyRadius,
        center = center,
        style = Stroke(5f)
    )

    // 4. Center 4K Gimbal Camera & Optical Radar Lens
    val cameraRadius = size.width * 0.07f
    drawCircle(
        color = Color(0xFF0F172A),
        radius = cameraRadius,
        center = center
    )
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = cameraRadius,
        center = center,
        style = Stroke(3f)
    )
    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = 0.8f),
        radius = cameraRadius * 0.45f,
        center = center
    )
}
