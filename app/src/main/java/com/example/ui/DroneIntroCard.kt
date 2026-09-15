package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Session-level flag: persists while the app is running in memory, resets when the app is restarted
internal var isIntroDismissedForSession by mutableStateOf(false)

/**
 * Animated Introduction Card delivered and carried away by a flying Quadcopter Drone.
 * On entrance / initial load, the drone descends from above holding the card.
 * When the user taps the dismiss button, the drone carries the card away into the sky.
 * Once dismissed, it will not reappear until the app is restarted.
 */
@Composable
fun DroneDeliveredIntroCard(
    modifier: Modifier = Modifier
) {
    if (isIntroDismissedForSession) {
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val appTheme = com.example.ui.theme.LocalAppTheme.current

    // 0f = high in the sky / carried away, 1f = landed smoothly in dashboard place
    val deliveryProgress = remember { Animatable(1f) }
    var isDelivered by remember { mutableStateOf(true) }
    var isFlyingAnimationRunning by remember { mutableStateOf(false) }

    fun dismissIntro() {
        if (isFlyingAnimationRunning) return
        coroutineScope.launch {
            // Fly away / Carry window away into the sky
            isFlyingAnimationRunning = true
            deliveryProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 650,
                    easing = FastOutSlowInEasing
                )
            )
            isDelivered = false
            isFlyingAnimationRunning = false
            isIntroDismissedForSession = true
        }
    }

    // Infinite propeller rotation (spins faster when lifting/carrying)
    val infiniteTransition = rememberInfiniteTransition(label = "drone_intro_propellers")
    val propSpeed = if (isFlyingAnimationRunning) 40 else 80
    val propellerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(propSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "prop_spin"
    )

    // Gentle hovering oscillation
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_bob"
    )

    val currentProgress = deliveryProgress.value
    val isVisibleOnScreen = currentProgress > 0.05f

    // Calculate vertical travel offset of card during delivery / departure
    val cardYOffset = (1f - currentProgress) * -110f
    val cardAlpha = (currentProgress * 1.3f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("welcome_intro_card_drone_wrapper"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isVisibleOnScreen) {
            // Interactive Drone Rig & Status Badge centered above the intro card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
                    .offset(y = hoverOffset.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        dismissIntro()
                    }
            ) {
                // Quadcopter Drone graphic centered at horizontal midpoint
                DroneGraphic(
                    propellerAngle = propellerAngle,
                    isPullingDown = isFlyingAnimationRunning && isDelivered,
                    isPullingUp = isFlyingAnimationRunning && !isDelivered
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Status Button centered directly underneath the drone with high contrast
                Surface(
                    shape = RoundedCornerShape(appTheme.buttonCornerRadiusDp.dp),
                    color = appTheme.surfaceColor,
                    border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.primaryColor.copy(alpha = 0.85f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier.clickable { dismissIntro() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFlyingAnimationRunning) "Drón elszállítja az ablakot..." else "Érintsd meg az üdvözlő ablak eltüntetéséhez",
                            color = if (appTheme.isDark) Color(0xFFE0F2FE) else Color(0xFF0F172A),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // The Delivered Intro Card (translates smoothly up/down with the drone tow cables)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = cardYOffset.dp)
                    .alpha(cardAlpha)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome_intro_card"),
                    colors = CardDefaults.cardColors(containerColor = appTheme.surfaceColor),
                    border = BorderStroke(appTheme.borderWidthDp.dp, appTheme.cardBorderColor),
                    shape = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                                Color(0xFF38BDF8).copy(alpha = 0.15f)
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlightTakeoff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Üdvözöl a Drón Kalauz!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "A Te digitális másodpilótád és szabályzati kalauzod",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Text(
                            text = "Ez az alkalmazás a hobbi- és professzionális drónpilótáknak nyújt átfogó, naprakész segítséget a biztonságos és szabályos repüléshez. Akár Magyarországon, akár külföldön repülsz, minden fontos információt azonnal elérsz egy helyen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 20.sp
                        )

                        // Főbb funkciók kiemelése
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appTheme.backgroundColor, RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.5f).dp))
                                .border(appTheme.borderWidthDp.dp, appTheme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.5f).dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📜", fontSize = 14.sp)
                                Text(
                                    text = "Országos & Globális Szabályzatok: EASA nyílt kategóriák (A1/A2/A3), regisztráció, biztosítási limitek és eseti légtér követelmények.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🛰️", fontSize = 14.sp)
                                Text(
                                    text = "Élő Telemetria & Időjárás: Valós GPS adatok, szélsebesség, széllökések, Kp napvihar index és polgári szürkület kalkulátor.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📍", fontSize = 14.sp)
                                Text(
                                    text = "Spotter Kalauz & Légtértérkép: Fedezz fel biztonságos fotós helyszíneket, ellenőrizd a tiltott és korlátozott légtereket.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

