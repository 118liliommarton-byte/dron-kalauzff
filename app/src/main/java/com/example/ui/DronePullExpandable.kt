package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Drone-powered Animated Accordion Container for Flight Rules.
 * When opening: A high-tech drone attaches tow cables and visibly pulls the panel down.
 * When closing: The drone engages reverse thrusters and pulls the panel back up into place.
 */
@Composable
fun DronePullExpandable(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Smooth animated transition from 0f (closed) to 1f (fully open)
    val expandProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "drone_expand_progress"
    )

    // Infinite propeller rotation for active flight
    val infiniteTransition = rememberInfiniteTransition(label = "propeller_anim")
    val propellerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propeller_spin"
    )

    // Drone gentle hovering wobble
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drone_hover"
    )

    // Determine drone vertical tilt based on moving state
    var previousTarget by remember { mutableStateOf(isExpanded) }
    var isMovingDown by remember { mutableStateOf(false) }
    var isMovingUp by remember { mutableStateOf(false) }

    LaunchedEffect(isExpanded) {
        if (isExpanded != previousTarget) {
            if (isExpanded) {
                isMovingDown = true
                isMovingUp = false
            } else {
                isMovingDown = false
                isMovingUp = true
            }
            previousTarget = isExpanded
        }
    }

    // Reset moving state once animation settles
    LaunchedEffect(expandProgress) {
        if (expandProgress >= 0.99f || expandProgress <= 0.01f) {
            isMovingDown = false
            isMovingUp = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Unfolding panel content whose height is controlled by the drone
        if (expandProgress > 0.001f) {
            DroneAnimatedHeightLayout(progress = expandProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        .alpha(expandProgress.coerceIn(0.2f, 1f))
                ) {
                    content()
                }
            }

            // The Towing Cable & Animated Drone Rig positioned right at the bottom edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                // Tow ropes connecting the card edges to the drone hitch
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val droneCenter = Offset(w / 2f, h / 2f + hoverOffset)

                    val leftAnchor = Offset(w * 0.12f, 0f)
                    val rightAnchor = Offset(w * 0.88f, 0f)

                    // Tension cables glow
                    val cableColor = if (isMovingDown) {
                        Color(0xFF00F0FF) // Cyan down-thrust
                    } else if (isMovingUp) {
                        Color(0xFFF59E0B) // Amber up-pull
                    } else {
                        Color(0xFF00F0FF).copy(alpha = 0.6f)
                    }

                    // Left rope
                    drawLine(
                        color = cableColor,
                        start = leftAnchor,
                        end = Offset(droneCenter.x - 10.dp.toPx(), droneCenter.y),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Right rope
                    drawLine(
                        color = cableColor,
                        start = rightAnchor,
                        end = Offset(droneCenter.x + 10.dp.toPx(), droneCenter.y),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Anchor hooks on left/right
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 2.5.dp.toPx(),
                        center = leftAnchor
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 2.5.dp.toPx(),
                        center = rightAnchor
                    )
                }

                // Interactive Drone Widget (Clicking it also collapses the menu!)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .offset(y = hoverOffset.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle() }
                ) {
                    // Drone Graphic
                    DroneGraphic(
                        propellerAngle = propellerAngle,
                        isPullingDown = isMovingDown,
                        isPullingUp = isMovingUp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Drone status label pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0B132B).copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isMovingUp) Color(0xFFF59E0B).copy(alpha = 0.8f) else Color(0xFF00F0FF).copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = if (isMovingUp) Color(0xFFF59E0B) else Color(0xFF00F0FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isMovingDown) "▼ Drón lehúzva..." else if (isMovingUp) "▲ Drón felhúzva..." else "▲ Szabályzat felhúzása (Bezárás)",
                                color = if (isMovingUp) Color(0xFFFDE68A) else Color(0xFFE0F2FE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Layout that animates its measured and laid out height according to progress (0f..1f).
 */
@Composable
private fun DroneAnimatedHeightLayout(
    progress: Float,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, constraints ->
        val placeable = measurables.firstOrNull()?.measure(
            constraints.copy(minHeight = 0)
        )
        val fullHeight = placeable?.height ?: 0
        val currentHeight = (fullHeight * progress).roundToInt()

        layout(
            width = placeable?.width ?: constraints.minWidth,
            height = currentHeight
        ) {
            placeable?.placeRelative(0, 0)
        }
    }
}

/**
 * Mini High-Tech Quadcopter Drone with rotating rotors and navigation LEDs.
 */
@Composable
fun DroneGraphic(
    propellerAngle: Float,
    isPullingDown: Boolean,
    isPullingUp: Boolean,
    modifier: Modifier = Modifier
) {
    val tiltAngle = when {
        isPullingDown -> 12f
        isPullingUp -> -12f
        else -> 0f
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .rotate(tiltAngle),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cX = size.width / 2f
            val cY = size.height / 2f

            // Carbon fiber arm cross
            val armPadding = 4.dp.toPx()
            drawLine(
                color = Color(0xFF06B6D4),
                start = Offset(armPadding, armPadding),
                end = Offset(size.width - armPadding, size.height - armPadding),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF06B6D4),
                start = Offset(armPadding, size.height - armPadding),
                end = Offset(size.width - armPadding, armPadding),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Drone Center Core Body
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 6.5.dp.toPx(),
                center = Offset(cX, cY)
            )
            drawCircle(
                color = if (isPullingUp) Color(0xFFF59E0B) else Color(0xFF00F0FF),
                radius = 6.5.dp.toPx(),
                center = Offset(cX, cY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Center status LED
            drawCircle(
                color = if (isPullingUp) Color(0xFFEF4444) else Color(0xFF10B981),
                radius = 2.2.dp.toPx(),
                center = Offset(cX, cY)
            )

            // Tow hitch hook on bottom of the drone
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 2.dp.toPx(),
                center = Offset(cX, cY + 5.5.dp.toPx())
            )

            // 4 Rotors
            val rotorCenters = listOf(
                Offset(armPadding, armPadding),
                Offset(size.width - armPadding, armPadding),
                Offset(armPadding, size.height - armPadding),
                Offset(size.width - armPadding, size.height - armPadding)
            )

            rotorCenters.forEachIndexed { idx, pos ->
                // Propeller glow disc
                drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = 0.25f),
                    radius = 5.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = if (idx < 2) Color(0xFF10B981) else Color(0xFFEF4444),
                    radius = 5.dp.toPx(),
                    center = pos,
                    style = Stroke(width = 0.8.dp.toPx())
                )

                // Spinning blade line
                val currentPropAngle = propellerAngle + (idx * 90f)
                rotate(degrees = currentPropAngle, pivot = pos) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.9f),
                        start = Offset(pos.x - 4.5.dp.toPx(), pos.y),
                        end = Offset(pos.x + 4.5.dp.toPx(), pos.y),
                        strokeWidth = 1.3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
