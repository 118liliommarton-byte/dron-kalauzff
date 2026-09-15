package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val appTheme = com.example.ui.theme.LocalAppTheme.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .testTag("notification_dialog"),
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
            colors = CardDefaults.cardColors(
                containerColor = appTheme.surfaceColor
            ),
            border = BorderStroke(
                appTheme.dialogBorderWidthDp.dp,
                appTheme.cardBorderColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (unreadCount > 0) appTheme.primaryColor.copy(alpha = 0.2f)
                                    else Color(0xFF64748B).copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (unreadCount > 0) appTheme.primaryColor
                                    else Color(0xFF64748B).copy(alpha = 0.5f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (unreadCount > 0) appTheme.primaryColor else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Értesítések",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                )
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEF4444), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$unreadCount új",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${appTheme.dialogWindowTag} • Rendszerközlemények",
                                fontSize = 10.5.sp,
                                color = appTheme.primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Close Dialog Button (Drone Logo)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9), CircleShape)
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_drone_logo),
                            contentDescription = "Bezárás",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions Bar: Mark all read / Clear all
                if (notifications.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = { viewModel.markAllNotificationsAsRead() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mind olvasottnak jelölése",
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        TextButton(
                            onClick = { viewModel.clearAllNotifications() },
                            modifier = Modifier.testTag("clear_all_notifications_button"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Összes törlése",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Divider(
                        color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                // Notification Items List or Empty State
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(
                                        if (isDarkMode) Color(0xFF334155).copy(alpha = 0.4f)
                                        else Color(0xFFF1F5F9),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Nincsenek értesítéseid",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "Minden fontos repülési figyelmeztetésről, piactér hírről és rendszerközleményről itt kapsz majd tájékoztatást.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            NotificationItemCard(
                                notification = notif,
                                isDarkMode = isDarkMode,
                                onCardClick = {
                                    viewModel.markNotificationAsRead(notif.id)
                                    if (notif.targetScreen != null) {
                                        onNavigateToScreen(notif.targetScreen)
                                        onDismiss()
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteNotification(notif.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: AppNotification,
    isDarkMode: Boolean,
    onCardClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeIcon, typeColor, typeBadgeText) = when (notification.type) {
        NotificationType.ALERT -> Triple(Icons.Default.Warning, Color(0xFFEF4444), "FIGYELMEZTETÉS")
        NotificationType.WEATHER -> Triple(Icons.Default.Air, Color(0xFF0284C7), "IDŐJÁRÁS & RADAR")
        NotificationType.SYSTEM -> Triple(Icons.Default.AdminPanelSettings, Color(0xFFEAB308), "ADMIN KÖZLEMÉNY")
        NotificationType.MARKETPLACE -> Triple(Icons.Default.Storefront, Color(0xFF10B981), "PIACTÉR")
        NotificationType.INFO -> Triple(Icons.Default.Info, Color(0xFF38BDF8), "INFORMÁCIÓ")
    }

    val timeFormatted = remember(notification.timestamp) {
        val diff = System.currentTimeMillis() - notification.timestamp
        when {
            diff < 60 * 1000L -> "Épp most"
            diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)} perce"
            diff < 24 * 60 * 60 * 1000L -> "${diff / (60 * 60 * 1000L)} órája"
            else -> {
                val sdf = SimpleDateFormat("MMM d. HH:mm", Locale("hu"))
                sdf.format(Date(notification.timestamp))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !notification.isRead && isDarkMode -> Color(0xFF1E293B).copy(alpha = 0.9f)
                !notification.isRead && !isDarkMode -> Color(0xFFF0F9FF)
                isDarkMode -> Color(0xFF0F172A).copy(alpha = 0.6f)
                else -> Color(0xFFF8FAFC)
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                !notification.isRead -> typeColor.copy(alpha = 0.6f)
                isDarkMode -> Color(0xFF334155).copy(alpha = 0.6f)
                else -> Color(0xFFE2E8F0)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Type Icon badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(typeColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, typeColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = typeBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(typeColor, CircleShape)
                            )
                        }
                    }
                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = if (isDarkMode) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569),
                    lineHeight = 16.sp
                )

                // Optional Action Button (if target screen exists)
                if (notification.targetScreen != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Megtekintés",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Delete individual notification
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Értesítés törlése",
                    tint = if (isDarkMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
