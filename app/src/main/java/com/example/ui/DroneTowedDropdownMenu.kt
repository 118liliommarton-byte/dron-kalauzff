package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.auth.PilotUser
import kotlinx.coroutines.launch

/**
 * Custom Animated Dropdown Menu towed down & lifted up by a hovering Quadcopter Drone.
 */
@Composable
fun DroneTowedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    isDarkMode: Boolean,
    currentPilotUser: PilotUser?,
    isAdminUnlocked: Boolean,
    selectedTheme: AppTheme = AppTheme.CYBER_DARK,
    onNavigate: (AppScreen) -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenThemeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val animProgress = remember { Animatable(if (expanded) 1f else 0f) }
    var isPopupVisible by remember { mutableStateOf(expanded) }
    var menuHeightPx by remember { mutableFloatStateOf(0f) }

    // Handle smooth opening and closing animations
    LaunchedEffect(expanded) {
        if (expanded) {
            isPopupVisible = true
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        } else {
            animProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            isPopupVisible = false
        }
    }

    if (!isPopupVisible && animProgress.value <= 0.01f) return

    // High speed propeller rotation animation
    val propellerRotation = rememberInfiniteTransition(label = "drone_menu_propeller").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propeller_angle"
    )

    // Subtle hovering vertical sine wave oscillation
    val hoverBobbing = rememberInfiniteTransition(label = "drone_menu_hover").animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drone_bobbing"
    )

    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Invisible backdrop overlay to catch taps outside and smoothly retract menu
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismissRequest()
                    }
            )

            // Drone + Menu container positioned at top right under the 3-dots top bar button
            Box(
                modifier = modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 54.dp, end = 12.dp)
                    .width(280.dp)
            ) {
                val progress = animProgress.value
                val tiltAngle = if (progress < 0.99f && expanded) (16f * (1f - progress)) else if (!expanded) (-16f * (1f - progress)) else 0f

                // Use measured height or fallback to 350dp in px
                val fallbackHeightPx = with(LocalDensity.current) { 350.dp.toPx() }
                val effectiveHeightPx = if (menuHeightPx > 0f) menuHeightPx else fallbackHeightPx
                val currentBottomPx = effectiveHeightPx * progress
                val droneCenterYPx = currentBottomPx + with(LocalDensity.current) { 22.dp.toPx() } + with(LocalDensity.current) { hoverBobbing.value.dp.toPx() }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. THE TOWED DROPDOWN MENU CARD (Expands/Collapses vertically from top)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { size ->
                                if (size.height > 0) {
                                    menuHeightPx = size.height.toFloat()
                                }
                            }
                            .graphicsLayer {
                                scaleY = progress
                                alpha = progress.coerceIn(0f, 1f)
                                transformOrigin = TransformOrigin(0.5f, 0f)
                            }
                            .shadow((16 * progress).dp, RoundedCornerShape(14.dp))
                            .border(
                                width = 1.5.dp,
                                color = if (isDarkMode) Color(0xFF00F0FF).copy(alpha = 0.6f) else Color(0xFF0284C7).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            // Menu Item 1: Főoldal
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Főoldal", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    onNavigate(AppScreen.HOME)
                                    onDismissRequest()
                                },
                                modifier = Modifier.testTag("menu_home_item")
                            )

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Menu Item 2: Repülési szabályzatok
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Repülési szabályzatok", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    onNavigate(AppScreen.RULES)
                                    onDismissRequest()
                                },
                                modifier = Modifier.testTag("menu_rules_item")
                            )

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Menu Item 3: Spotter & Fotós Térkép
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = Color(0xFF00F0FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Drónos Spotter & Térkép", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    onNavigate(AppScreen.SPOTTER)
                                    onDismissRequest()
                                },
                                modifier = Modifier.testTag("menu_spotter_item")
                            )

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Menu Item 4: Drón Piac
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Drónok eladása és vétele", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    onNavigate(AppScreen.MARKETPLACE)
                                    onDismissRequest()
                                },
                                modifier = Modifier.testTag("menu_marketplace_item")
                            )

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Menu Item 5: Vizsgák & Biztosítások
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Drónvizsga & Biztosítás", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    onNavigate(AppScreen.EXAM_AND_INSURANCE)
                                    onDismissRequest()
                                },
                                modifier = Modifier.testTag("menu_exam_insurance_item")
                            )

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Menu Item 6: Profil / Bejelentkezés
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = if (currentPilotUser != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            if (currentPilotUser != null) "Pilóta Profil (${currentPilotUser.pilotName})" else "Pilótafiók & Bejelentkezés",
                                            color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    onNavigate(AppScreen.PROFILE)
                                    onDismissRequest()
                                },
                                modifier = Modifier.testTag("menu_profile_item")
                            )

                            // Admin MenuItem (if unlocked)
                            if (isAdminUnlocked) {
                                Divider(color = Color(0xFFEAB308).copy(alpha = 0.6f))
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = Color(0xFFEAB308),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                "Admin Vezérlőpult",
                                                color = Color(0xFFEAB308),
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    },
                                    onClick = {
                                        onNavigate(AppScreen.ADMIN)
                                        onDismissRequest()
                                    },
                                    modifier = Modifier.testTag("menu_admin_item")
                                )
                            }

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Theme Selector Dialog Item
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = selectedTheme.primaryColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Téma & Kinézet",
                                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${selectedTheme.iconEmoji} ${selectedTheme.displayName}",
                                                    color = selectedTheme.primaryColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    onDismissRequest()
                                    onOpenThemeDialog()
                                },
                                modifier = Modifier.testTag("menu_theme_picker_item")
                            )

                            Divider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))

                            // Dark Mode Quick Toggle Item
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                contentDescription = null,
                                                tint = if (isDarkMode) Color(0xFFF59E0B) else Color(0xFFFFB703),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (isDarkMode) "Sötét mód" else "Világos mód",
                                                color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Switch(
                                            checked = isDarkMode,
                                            onCheckedChange = { onToggleDarkMode() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = selectedTheme.primaryColor,
                                                checkedTrackColor = selectedTheme.primaryColor.copy(alpha = 0.3f),
                                                uncheckedThumbColor = Color(0xFF0284C7),
                                                uncheckedTrackColor = Color(0xFFBAE6FD)
                                            ),
                                            modifier = Modifier.scale(0.8f)
                                        )
                                    }
                                },
                                onClick = {
                                    onToggleDarkMode()
                                },
                                modifier = Modifier.testTag("menu_theme_toggle_item")
                            )
                        }
                    }

                    // 2. TOW CABLES (Connecting bottom edge of menu to drone top hitch)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(LocalDensity.current) { (effectiveHeightPx + 80.dp.toPx()).toDp() })
                            .alpha(progress.coerceIn(0f, 1f))
                    ) {
                        val droneCenterX = size.width / 2f
                        val droneCenterY = droneCenterYPx
                        val menuBottomY = currentBottomPx

                        if (progress > 0.05f) {
                            val leftAnchorX = 36.dp.toPx()
                            val rightAnchorX = size.width - 36.dp.toPx()

                            // Left tow cable
                            drawLine(
                                color = Color(0xFF00F0FF).copy(alpha = 0.85f),
                                start = Offset(leftAnchorX, menuBottomY),
                                end = Offset(droneCenterX - 8.dp.toPx(), droneCenterY - 12.dp.toPx()),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Butt
                            )

                            // Right tow cable
                            drawLine(
                                color = Color(0xFFEAB308).copy(alpha = 0.85f),
                                start = Offset(rightAnchorX, menuBottomY),
                                end = Offset(droneCenterX + 8.dp.toPx(), droneCenterY - 12.dp.toPx()),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Butt
                            )
                        }
                    }

                    // 3. QUADCOPTER DRONE (Hovers directly at the bottom edge of expanding menu)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                translationY = droneCenterYPx - density.run { 24.dp.toPx() }
                            }
                            .size(48.dp)
                            .rotate(tiltAngle)
                            .alpha(progress.coerceIn(0.1f, 1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cX = size.width / 2
                            val cY = size.height / 2

                            // Carbon arms
                            drawLine(
                                color = Color(0xFF0284C7),
                                start = Offset(6.dp.toPx(), 6.dp.toPx()),
                                end = Offset(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFF0284C7),
                                start = Offset(6.dp.toPx(), size.height - 6.dp.toPx()),
                                end = Offset(size.width - 6.dp.toPx(), 6.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Main Body Core
                            drawCircle(
                                color = Color(0xFF0F172A),
                                radius = 9.dp.toPx(),
                                center = Offset(cX, cY)
                            )
                            drawCircle(
                                color = Color(0xFF00F0FF),
                                radius = 9.dp.toPx(),
                                center = Offset(cX, cY),
                                style = Stroke(width = 1.8.dp.toPx())
                            )

                            // Search Light / Camera Eye
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 3.5.dp.toPx(),
                                center = Offset(cX, cY - 4.dp.toPx())
                            )

                            // 4 Propeller Disc Spinners
                            val rotorCenters = listOf(
                                Offset(6.dp.toPx(), 6.dp.toPx()),
                                Offset(size.width - 6.dp.toPx(), 6.dp.toPx()),
                                Offset(6.dp.toPx(), size.height - 6.dp.toPx()),
                                Offset(size.width - 6.dp.toPx(), size.height - 6.dp.toPx())
                            )

                            rotorCenters.forEachIndexed { index, rCenter ->
                                drawCircle(
                                    color = Color(0xFF00F0FF).copy(alpha = 0.35f),
                                    radius = 7.dp.toPx(),
                                    center = rCenter
                                )
                                drawCircle(
                                    color = if (index < 2) Color(0xFF10B981) else Color(0xFFEF4444),
                                    radius = 7.dp.toPx(),
                                    center = rCenter,
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                val angle = propellerRotation.value + index * 90f
                                rotate(degrees = angle, pivot = rCenter) {
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(rCenter.x - 6.dp.toPx(), rCenter.y),
                                        end = Offset(rCenter.x + 6.dp.toPx(), rCenter.y),
                                        strokeWidth = 1.8.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
