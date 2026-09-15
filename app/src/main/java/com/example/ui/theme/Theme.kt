package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.AppTheme

val LocalAppTheme = staticCompositionLocalOf { AppTheme.CYBER_DARK }

@Composable
fun MyApplicationTheme(
  appTheme: AppTheme = AppTheme.CYBER_DARK,
  darkTheme: Boolean = appTheme.isDark,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    appTheme.isDark -> darkColorScheme(
      primary = appTheme.primaryColor,
      secondary = appTheme.primaryColor,
      tertiary = appTheme.primaryColor,
      background = appTheme.backgroundColor,
      surface = appTheme.surfaceColor,
      onPrimary = Color.Black,
      onSecondary = Color.Black,
      onTertiary = Color.Black,
      onBackground = Color.White,
      onSurface = Color.White,
      surfaceVariant = appTheme.surfaceColor,
      onSurfaceVariant = Color(0xFFCBD5E1),
      outline = appTheme.cardBorderColor
    )
    else -> lightColorScheme(
      primary = appTheme.primaryColor,
      secondary = appTheme.primaryColor,
      tertiary = appTheme.primaryColor,
      background = appTheme.backgroundColor,
      surface = appTheme.surfaceColor,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = Color(0xFF0F172A),
      onSurface = Color(0xFF0F172A),
      surfaceVariant = Color(0xFFE2E8F0),
      onSurfaceVariant = Color(0xFF475569),
      outline = appTheme.cardBorderColor
    )
  }

  val themeShapes = Shapes(
    extraSmall = RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.4f).coerceAtLeast(2f).dp),
    small = RoundedCornerShape((appTheme.cardCornerRadiusDp * 0.65f).coerceAtLeast(3f).dp),
    medium = RoundedCornerShape(appTheme.cardCornerRadiusDp.dp),
    large = RoundedCornerShape((appTheme.cardCornerRadiusDp * 1.3f).dp),
    extraLarge = RoundedCornerShape((appTheme.cardCornerRadiusDp * 1.6f).dp)
  )

  CompositionLocalProvider(LocalAppTheme provides appTheme) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = themeShapes,
      content = content
    )
  }
}
