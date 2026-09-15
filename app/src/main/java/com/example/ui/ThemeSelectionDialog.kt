package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val isDarkMode = currentTheme.isDark
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = currentTheme.primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Válassz Témát & Kinézetet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "${currentTheme.uiStyleName} • Ablak: ${currentTheme.dialogWindowTag}",
                        fontSize = 10.sp,
                        color = currentTheme.primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "A kiválasztott téma megváltoztatja a teljes kezelőfelületet: a színeket, a kártyák és ablakok formavilágát, valamint a főoldali drónképet.",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(AppTheme.values()) { theme ->
                        val isSelected = theme == currentTheme
                        Surface(
                            onClick = {
                                onSelectTheme(theme)
                            },
                            shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                            color = theme.surfaceColor,
                            border = BorderStroke(
                                if (isSelected) (theme.borderWidthDp + 1f).dp else theme.borderWidthDp.dp,
                                if (isSelected) theme.primaryColor else theme.cardBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Hero image preview thumbnail
                                    Card(
                                        shape = RoundedCornerShape((theme.cardCornerRadiusDp * 0.4f).coerceAtLeast(3f).dp),
                                        modifier = Modifier
                                            .size(width = 50.dp, height = 36.dp),
                                        border = BorderStroke(0.8.dp, theme.cardBorderColor)
                                    ) {
                                        Image(
                                            painter = painterResource(id = theme.heroImageRes),
                                            contentDescription = theme.heroImageTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = theme.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = if (theme.isDark) Color.White else Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape((theme.cardCornerRadiusDp * 0.4f).dp))
                                                    .background(theme.primaryColor.copy(alpha = 0.15f))
                                                    .border(0.5.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape((theme.cardCornerRadiusDp * 0.4f).dp))
                                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            ) {
                                                Text(
                                                    text = theme.uiStyleName,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.primaryColor
                                                )
                                            }
                                        }
                                        Text(
                                            text = theme.subtitle,
                                            fontSize = 10.5.sp,
                                            maxLines = 1,
                                            color = if (theme.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.primaryColor)
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.backgroundColor)
                                                    .border(0.5.dp, theme.cardBorderColor, CircleShape)
                                            )
                                            Text(
                                                text = "Ablak: ${theme.dialogCornerRadiusDp.toInt()}dp • Keret: ${theme.dialogBorderWidthDp}dp",
                                                fontSize = 9.sp,
                                                color = if (theme.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = theme.primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (theme.isDark && theme != AppTheme.OLED_BLACK) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(currentTheme.buttonCornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor)
            ) {
                Text("Kész", color = if (currentTheme.isDark && currentTheme != AppTheme.OLED_BLACK) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = currentTheme.surfaceColor,
        shape = RoundedCornerShape(currentTheme.dialogCornerRadiusDp.dp)
    )
}

@Composable
fun AppThemeSelectionCard(
    currentTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit
) {
    val isDarkMode = currentTheme.isDark
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(currentTheme.cardCornerRadiusDp.dp),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.surfaceColor
        ),
        border = BorderStroke(
            currentTheme.borderWidthDp.dp,
            currentTheme.cardBorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            currentTheme.primaryColor.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = currentTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "🎨 Téma & Kinézet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Válassz megjelenési stílust, geometriát és színharmóniát",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppTheme.values().forEach { theme ->
                    val isSelected = theme == currentTheme
                    Surface(
                        onClick = { onSelectTheme(theme) },
                        shape = RoundedCornerShape(theme.cardCornerRadiusDp.dp),
                        color = theme.surfaceColor,
                        border = BorderStroke(
                            if (isSelected) (theme.borderWidthDp + 1f).dp else theme.borderWidthDp.dp,
                            if (isSelected) theme.primaryColor else theme.cardBorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(theme.iconEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = theme.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = if (theme.isDark) Color.White else Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape((theme.cardCornerRadiusDp * 0.4f).dp))
                                                .background(theme.primaryColor.copy(alpha = 0.15f))
                                                .border(0.5.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape((theme.cardCornerRadiusDp * 0.4f).dp))
                                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = theme.uiStyleName,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.primaryColor
                                            )
                                        }
                                    }
                                    Text(
                                        text = theme.subtitle,
                                        fontSize = 10.5.sp,
                                        color = if (theme.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(theme.primaryColor)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(theme.backgroundColor)
                                                .border(0.5.dp, theme.cardBorderColor, CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(theme.surfaceColor)
                                                .border(0.5.dp, theme.cardBorderColor, CircleShape)
                                        )
                                        Text(
                                            text = "${theme.cardCornerRadiusDp.toInt()}dp sarok · ${theme.borderWidthDp}dp keret",
                                            fontSize = 9.sp,
                                            color = if (theme.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = theme.primaryColor,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (theme.isDark && theme != AppTheme.OLED_BLACK) Color.Black else Color.White,
                                            modifier = Modifier.size(14.dp)
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
}
