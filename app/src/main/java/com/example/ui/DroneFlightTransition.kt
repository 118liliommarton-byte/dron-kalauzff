package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Animated Flight Overlay where a drone flies across the top bar pulling a paper banner
 * displaying the destination menu name, and delivers/docks the paper banner directly
 * into the top menu bar title!
 */
@Composable
fun DroneFlightTransitionOverlay(
    currentScreen: AppScreen,
    modifier: Modifier = Modifier,
    onTitleDelivered: (AppScreen) -> Unit = {}
) {
    var previousScreen by remember { mutableStateOf(currentScreen) }
    var isFlying by remember { mutableStateOf(false) }
    var targetScreenName by remember { mutableStateOf("") }
    var flightDirection by remember { mutableIntStateOf(1) } // 1: left to right, -1: right to left

    val flightProgress = remember { Animatable(0f) }

    // Propeller high-speed rotation
    val propellerRotation = rememberInfiniteTransition(label = "propeller").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propeller_rotation"
    )

    // Gentle aerodynamic flutter wave for the paper banner
    val paperFlutter = rememberInfiniteTransition(label = "paper_flutter").animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paper_flutter_angle"
    )

    // Trigger drone fly-through animation with paper banner delivery whenever currentScreen changes
    LaunchedEffect(currentScreen) {
        if (currentScreen != previousScreen) {
            flightDirection = if (currentScreen.ordinal >= previousScreen.ordinal) 1 else -1
            targetScreenName = when (currentScreen) {
                AppScreen.HOME -> "FŐOLDAL"
                AppScreen.RADAR -> "IDŐKÉP RADAR & TÉRKÉP"
                AppScreen.RULES -> "REPÜLÉSI SZABÁLYZATOK"
                AppScreen.SPOTTER -> "SPOTTER & FOTÓS TÉRKÉP"
                AppScreen.MARKETPLACE -> "DRÓNOK ELADÁSA ÉS VÉTELE"
                AppScreen.EXAM_AND_INSURANCE -> "VIZSGÁK ÉS BIZTOSÍTÁSOK"
                AppScreen.CHAT -> "ÉLŐ CHAT"
                AppScreen.PROFILE -> "PILÓTAFIÓK & PROFIL"
                AppScreen.ADMIN -> "👑 ADMINISZTRÁCIÓS KÖZPONT"
            }
            val destinationScreen = currentScreen
            previousScreen = currentScreen
            isFlying = true

            var delivered = false
            flightProgress.snapTo(0f)
            flightProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1600,
                    easing = CubicBezierEasing(0.25f, 0.05f, 0.25f, 1.0f)
                )
            ) {
                if (!delivered && value >= 0.52f) {
                    delivered = true
                    onTitleDelivered(destinationScreen)
                }
            }
            if (!delivered) {
                onTitleDelivered(destinationScreen)
            }
            // Brief pause at the end before hiding overlay completely
            delay(150)
            isFlying = false
        }
    }

    if (isFlying) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            val progress = flightProgress.value
            val totalWidthPx = constraints.maxWidth.toFloat()
            val totalWidthDp = maxWidth

            // Drone trajectory calculations
            val droneStartX = if (flightDirection == 1) -70.dp else totalWidthDp + 70.dp
            val droneEndX = if (flightDirection == 1) totalWidthDp + 90.dp else -90.dp
            val droneX = droneStartX + (droneEndX - droneStartX) * progress

            // Aerodynamic altitude variation: slightly dips and pulls up as it delivers the banner
            val droneY = 24.dp + (kotlin.math.sin(progress.toDouble() * Math.PI) * -8).dp

            // Paper Banner positioning:
            // Towed behind the drone until progress reaches ~0.52 (center of TopAppBar),
            // then smoothly locks / settles into the center header title slot!
            val ropeLengthDp = 64.dp
            val targetCenterXDp = totalWidthDp / 2

            val naturalPaperX = if (flightDirection == 1) droneX - ropeLengthDp else droneX + ropeLengthDp

            val paperX = if (progress < 0.52f) {
                naturalPaperX
            } else {
                val dockT = ((progress - 0.52f) / 0.25f).coerceIn(0f, 1f)
                val easeDock = FastOutSlowInEasing.transform(dockT)
                naturalPaperX + (targetCenterXDp - naturalPaperX) * easeDock
            }

            val paperY = if (progress < 0.52f) {
                droneY + 8.dp + (paperFlutter.value).dp
            } else {
                val dockT = ((progress - 0.52f) / 0.25f).coerceIn(0f, 1f)
                (droneY + 8.dp) + (26.dp - (droneY + 8.dp)) * dockT
            }

            // Alpha fade: paper fades out gracefully as it dissolves into the new top bar title
            val droneAlpha = if (progress < 0.08f) (progress / 0.08f) else if (progress > 0.90f) ((1f - progress) / 0.10f) else 1f
            val paperAlpha = if (progress < 0.08f) {
                (progress / 0.08f)
            } else if (progress > 0.55f) {
                ((0.78f - progress) / 0.23f).coerceIn(0f, 1f)
            } else {
                1f
            }

            // Cable / Tow Rope Canvas connecting Drone rear to Paper Banner
            if (progress < 0.78f) {
                val cableAlpha = if (progress > 0.65f) ((0.78f - progress) / 0.13f) else 1f
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(cableAlpha * droneAlpha)
                ) {
                    val dXPx = if (flightDirection == 1) {
                        (-70.dp.toPx() + (totalWidthPx + 160.dp.toPx()) * progress)
                    } else {
                        ((totalWidthPx + 70.dp.toPx()) - (totalWidthPx + 160.dp.toPx()) * progress)
                    }
                    val dYPx = 28.dp.toPx() + (kotlin.math.sin(progress.toDouble() * Math.PI) * -8.dp.toPx()).toFloat()

                    val pXPx = if (flightDirection == 1) dXPx - ropeLengthDp.toPx() else dXPx + ropeLengthDp.toPx()
                    val pYPx = dYPx + 8.dp.toPx()

                    // Tow cable sag / tension curve
                    val midXPx = (dXPx + pXPx) / 2f
                    val midYPx = ((dYPx + pYPx) / 2f) + 4.dp.toPx()

                    val path = Path().apply {
                        moveTo(dXPx, dYPx)
                        quadraticTo(midXPx, midYPx, pXPx, pYPx)
                    }

                    // Draw tow cable
                    drawPath(
                        path = path,
                        color = Color(0xFF00F0FF).copy(alpha = 0.85f),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw cable hook / attachment ring
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 2.5.dp.toPx(),
                        center = Offset(pXPx, pYPx)
                    )
                }
            }

            // 1. PAPER BANNER (Papírszalag a menü nevével)
            Box(
                modifier = Modifier
                    .offset(x = paperX - 100.dp, y = paperY)
                    .width(200.dp)
                    .alpha(paperAlpha)
                    .rotate(if (progress < 0.65f) paperFlutter.value + (if (flightDirection == 1) 2f else -2f) else 0f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB), // Elegant parchment/paper off-white
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFF00F0FF), Color(0xFFF59E0B))
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFEF3C7),
                                        Color(0xFFFFFBEB),
                                        Color(0xFFFEF3C7)
                                    )
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Paper scroll icon / fold
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = targetScreenName,
                            color = Color(0xFF0F172A), // Deep high-contrast slate text on paper
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. THE TOWING DRONE (A repülő drón a vontató kötéllel)
            Box(
                modifier = Modifier
                    .offset(x = droneX - 22.dp, y = droneY)
                    .size(44.dp)
                    .alpha(droneAlpha)
                    .rotate(if (flightDirection == 1) 14f else -14f),
                contentAlignment = Alignment.Center
            ) {
                // Drone Graphic & Spinners
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cX = size.width / 2
                    val cY = size.height / 2

                    // Drone Carbon Struts / Arms
                    drawLine(
                        color = Color(0xFF06B6D4),
                        start = Offset(6.dp.toPx(), 6.dp.toPx()),
                        end = Offset(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF06B6D4),
                        start = Offset(6.dp.toPx(), size.height - 6.dp.toPx()),
                        end = Offset(size.width - 6.dp.toPx(), 6.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Drone Fuselage / Main Core
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 8.dp.toPx(),
                        center = Offset(cX, cY)
                    )
                    drawCircle(
                        color = Color(0xFF00F0FF),
                        radius = 8.dp.toPx(),
                        center = Offset(cX, cY),
                        style = Stroke(width = 1.8.dp.toPx())
                    )

                    // Camera Gimbal Eye
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 3.dp.toPx(),
                        center = Offset(if (flightDirection == 1) cX + 6.dp.toPx() else cX - 6.dp.toPx(), cY)
                    )

                    // Tow Hitch on rear
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 2.5.dp.toPx(),
                        center = Offset(if (flightDirection == 1) cX - 7.dp.toPx() else cX + 7.dp.toPx(), cY)
                    )

                    // 4 Spinning Rotor Discs
                    val rotorCenters = listOf(
                        Offset(6.dp.toPx(), 6.dp.toPx()),
                        Offset(size.width - 6.dp.toPx(), 6.dp.toPx()),
                        Offset(6.dp.toPx(), size.height - 6.dp.toPx()),
                        Offset(size.width - 6.dp.toPx(), size.height - 6.dp.toPx())
                    )

                    rotorCenters.forEachIndexed { index, rCenter ->
                        // Propeller blur disc
                        drawCircle(
                            color = Color(0xFF00F0FF).copy(alpha = 0.3f),
                            radius = 6.5.dp.toPx(),
                            center = rCenter
                        )
                        drawCircle(
                            color = if (index < 2) Color(0xFF10B981) else Color(0xFFEF4444),
                            radius = 6.5.dp.toPx(),
                            center = rCenter,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Spinning Blade line
                        val angle = (propellerRotation.value + index * 90f)
                        rotate(degrees = angle, pivot = rCenter) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.9f),
                                start = Offset(rCenter.x - 5.5.dp.toPx(), rCenter.y),
                                end = Offset(rCenter.x + 5.5.dp.toPx(), rCenter.y),
                                strokeWidth = 1.6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
