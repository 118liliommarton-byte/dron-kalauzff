package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class ExamInsuranceTab(val title: String, val icon: ImageVector) {
    EXAMS("Drónvizsgák", Icons.Default.School),
    INSURANCE("Biztosítások", Icons.Default.Shield),
    GUIDE("Útmutató (5 lépés)", Icons.Default.Checklist),
    FAQ("Gyakori Kérdések", Icons.AutoMirrored.Filled.HelpOutline)
}

data class ExamInfo(
    val id: String,
    val title: String,
    val category: String,
    val badgeColor: Color,
    val shortSummary: String,
    val location: String,
    val requirements: String,
    val examFormat: String,
    val priceInfo: String,
    val validity: String,
    val targetAudience: String,
    val portalName: String,
    val portalUrl: String
)

data class InsuranceProvider(
    val name: String,
    val type: String,
    val badgeColor: Color,
    val description: String,
    val coverages: List<String>,
    val estimatedPrice: String,
    val features: List<String>,
    val portalUrl: String,
    val portalButtonText: String
)

data class FaqItem(
    val question: String,
    val answer: String,
    val tag: String
)

@Composable
fun ExamInsuranceScreen(viewModel: ChatViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ExamInsuranceTab.EXAMS) }

    // Drone weight slider state for Insurance Calculator
    var droneWeightGrams by remember { mutableFloatStateOf(249f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val cardBackground = if (isDarkMode) Color(0xFF161F30) else Color(0xFFFFFFFF)
    val cardBorderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top static header area
        Spacer(modifier = Modifier.height(16.dp))
        ExamInsuranceHeroCard(isDarkMode = isDarkMode)
        Spacer(modifier = Modifier.height(14.dp))

        // Tab Navigation Bar
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            contentColor = primaryColor,
            edgePadding = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
                .testTag("exam_insurance_tab_row")
        ) {
            ExamInsuranceTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) primaryColor else subtextColor
                            )
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) (if (isDarkMode) Color.White else Color(0xFF0F172A)) else subtextColor
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content with horizontal sliding animation
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val isForward = targetState.ordinal >= initialState.ordinal
                if (isForward) {
                    (
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                            initialOffsetX = { it }
                        ) + fadeIn(animationSpec = tween(260))
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it }
                        ) + fadeOut(animationSpec = tween(200))
                    )
                } else {
                    (
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                            initialOffsetX = { -it }
                        ) + fadeIn(animationSpec = tween(260))
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                            targetOffsetX = { it }
                        ) + fadeOut(animationSpec = tween(200))
                    )
                }
            },
            label = "exam_insurance_tab_switch",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                when (tab) {
                    ExamInsuranceTab.EXAMS -> {
                        item {
                            Text(
                                text = "HIVATALOS DRÓNVIZSGÁK MAGYARORSZÁGON ÉS AZ EU-BAN",
                                style = MaterialTheme.typography.labelLarge,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        items(getExamList()) { exam ->
                            ExamCard(
                                exam = exam,
                                isDarkMode = isDarkMode,
                                onOpenUrl = { url -> openWebUrl(context, url) }
                            )
                        }

                        item {
                            OfficialExamPartnersCard(
                                isDarkMode = isDarkMode,
                                onOpenUrl = { url -> openWebUrl(context, url) }
                            )
                        }
                    }

                    ExamInsuranceTab.INSURANCE -> {
                        item {
                            InsuranceLegalRequirementNotice(isDarkMode = isDarkMode)
                        }

                        item {
                            InsuranceCalculatorCard(
                                weightGrams = droneWeightGrams,
                                onWeightChange = { droneWeightGrams = it },
                                isDarkMode = isDarkMode
                            )
                        }

                        item {
                            Text(
                                text = "MEGBÍZHATÓ BIZTOSÍTÁSI PARTNEREK ÉS AJÁNLATOK",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        items(getInsuranceProviders()) { provider ->
                            InsuranceProviderCard(
                                provider = provider,
                                isDarkMode = isDarkMode,
                                onOpenUrl = { url -> openWebUrl(context, url) }
                            )
                        }

                        item {
                            CascoVsKgfbNoticeCard(isDarkMode = isDarkMode)
                        }
                    }

                    ExamInsuranceTab.GUIDE -> {
                        item {
                            StepByStepGuideContent(
                                isDarkMode = isDarkMode,
                                onOpenUrl = { url -> openWebUrl(context, url) }
                            )
                        }
                    }

                    ExamInsuranceTab.FAQ -> {
                        item {
                            Text(
                                text = "GYAKRAN ISMÉTELT KÉRDÉSEK A VIZSGÁRÓL ÉS BIZTOSÍTÁSRÓL",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        items(getFaqList()) { faq ->
                            FaqAccordionItem(
                                faq = faq,
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HERO HEADER
// -------------------------------------------------------------
@Composable
private fun ExamInsuranceHeroCard(isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF00F0FF).copy(alpha = 0.6f), Color(0xFFF59E0B).copy(alpha = 0.5f))
                ),
                RoundedCornerShape(18.dp)
            )
            .testTag("exam_insurance_hero_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF131D2F) else Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00F0FF).copy(alpha = 0.25f), Color(0xFFF59E0B).copy(alpha = 0.25f))
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "DRÓNVIZSGA & BIZTOSÍTÁS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Hivatalos vizsgaközpontok, EASA szabályok & KGFB kalkulátor",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            Text(
                text = "Itt megtalálsz minden hivatalos tudnivalót arról, hogyan tehetsz online vagy tantermi drónvizsgát (A1/A3, A2, Speciális), hol és milyen feltételekkel köthetsz kötelező felelősségbiztosítást a drónodra a magyar jogszabályoknak megfelelően.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                lineHeight = 18.sp
            )

            // Quick Info Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickBadge(
                    icon = Icons.Default.Public,
                    label = "EU / EASA 5 év",
                    tint = Color(0xFF00F0FF),
                    isDarkMode = isDarkMode
                )
                QuickBadge(
                    icon = Icons.Default.Shield,
                    label = "Kötelező KGFB",
                    tint = Color(0xFF10B981),
                    isDarkMode = isDarkMode
                )
                QuickBadge(
                    icon = Icons.Default.LaptopMac,
                    label = "Online KTI Teszt",
                    tint = Color(0xFFF59E0B),
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun RowScope.QuickBadge(
    icon: ImageVector,
    label: String,
    tint: Color,
    isDarkMode: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFE2E8F0),
        border = BorderStroke(0.5.dp, tint.copy(alpha = 0.5f)),
        modifier = Modifier.weight(1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                maxLines = 1
            )
        }
    }
}

// -------------------------------------------------------------
// EXAM CARDS
// -------------------------------------------------------------
@Composable
private fun ExamCard(
    exam: ExamInfo,
    isDarkMode: Boolean,
    onOpenUrl: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isExpanded) exam.badgeColor.copy(alpha = 0.8f)
                else (if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)),
                RoundedCornerShape(16.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .testTag("exam_card_${exam.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161F30) else Color(0xFFFFFFFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(exam.badgeColor.copy(alpha = 0.18f), CircleShape)
                            .border(1.dp, exam.badgeColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = exam.badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = exam.badgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, exam.badgeColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = exam.category,
                                color = exam.badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = exam.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Kevesebb részlet" else "Több részlet",
                        tint = exam.badgeColor
                    )
                }
            }

            Text(
                text = exam.shortSummary,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                lineHeight = 17.sp
            )

            // Expanded Detail Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )

                    DetailRow(
                        label = "Hol tehető le?",
                        value = exam.location,
                        icon = Icons.Default.LocationOn,
                        tint = exam.badgeColor,
                        isDarkMode = isDarkMode
                    )

                    DetailRow(
                        label = "Előfeltétel & Korhatár:",
                        value = exam.requirements,
                        icon = Icons.Default.Person,
                        tint = exam.badgeColor,
                        isDarkMode = isDarkMode
                    )

                    DetailRow(
                        label = "Vizsga formátuma:",
                        value = exam.examFormat,
                        icon = Icons.Default.Assignment,
                        tint = exam.badgeColor,
                        isDarkMode = isDarkMode
                    )

                    DetailRow(
                        label = "Díj & Érvényesség:",
                        value = "${exam.priceInfo} • ${exam.validity}",
                        icon = Icons.Default.AccessTime,
                        tint = exam.badgeColor,
                        isDarkMode = isDarkMode
                    )

                    DetailRow(
                        label = "Kinek ajánlott / Mire ad jogot?",
                        value = exam.targetAudience,
                        icon = Icons.Default.FlightTakeoff,
                        tint = exam.badgeColor,
                        isDarkMode = isDarkMode
                    )

                    Button(
                        onClick = { onOpenUrl(exam.portalUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .testTag("open_portal_${exam.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = exam.badgeColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exam.portalName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun OfficialExamPartnersCard(
    isDarkMode: Boolean,
    onOpenUrl: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Akkreditált Oktatóközpontok & Források",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }

            Text(
                text = "A hatósági KTI vizsgára való gyakorlati vagy tantermi felkészülést az alábbi elismert hazai drone akadémiák és portálok is segítik:",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenUrl("https://vizsgakozpont.kti.hu") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("KTI Portál", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = { onOpenUrl("https://legter.hu") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Légtér.hu", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = { onOpenUrl("https://duplitec.hu/dron-akademia") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Duplitec", fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// INSURANCE CONTENT
// -------------------------------------------------------------
@Composable
private fun InsuranceLegalRequirementNotice(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7F1D1D).copy(alpha = if (isDarkMode) 0.3f else 0.15f)
        ),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "KÖTELEZŐ JOGSZABÁLYI ELŐÍRÁS (KGFB)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp
                )
                Text(
                    text = "A 39/2001. (III. 5.) Korm. rendelet alapján Magyarország légterében minden nem játéknak minősülő drónra (amelyen van kamera vagy a felszállótömege > 120 g) KÖTELEZŐ felelősségbiztosítást kötni a repüléshez!",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color.White else Color(0xFF1E293B),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun InsuranceCalculatorCard(
    weightGrams: Float,
    onWeightChange: (Float) -> Unit,
    isDarkMode: Boolean
) {
    // Determine minimum liability limit according to Hungarian law
    val (requiredCoverageText, categoryLabel, isToyCategory) = when {
        weightGrams < 250f -> Triple("3.000.000 Ft", "Nyílt A1 / C0 Kategória (< 250 g, kamerás)", false)
        weightGrams <= 4000f -> Triple("3.000.000 Ft", "Nyílt A1/A2/A3 Kategória (250 g – 4 kg)", false)
        weightGrams <= 20000f -> Triple("5.000.000 Ft", "Nyílt A3 / Speciális Kategória (4 kg – 20 kg)", false)
        else -> Triple("10.000.000 Ft", "Nehéz / Ipari Kategória (> 20 kg)", false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF10B981).copy(alpha = 0.7f), Color(0xFF00F0FF).copy(alpha = 0.5f))
                ),
                RoundedCornerShape(16.dp)
            )
            .testTag("insurance_calculator_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF132228) else Color(0xFFF0FDF4)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "TÖRVÉNYI FELELŐSSÉGBIZTOSÍTÁS KALKULÁTOR",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }

            Text(
                text = "Állítsd be a drónod maximális felszállótömegét (MTOM), és a kalkulátor azonnal megmutatja a jogszabályban előírt minimális káronkénti fedezeti limitet:",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
            )

            // Weight Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Felszállósúly (MTOM):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Text(
                        text = if (weightGrams < 1000f) "${weightGrams.toInt()} g" else String.format("%.2f kg", weightGrams / 1000f),
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Slider(
                value = weightGrams,
                onValueChange = onWeightChange,
                valueRange = 100f..25000f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF10B981),
                    activeTrackColor = Color(0xFF10B981),
                    inactiveTrackColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.testTag("weight_slider")
            )

            // Result Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF0C191E) else Color(0xFFDCFCE7),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Kategória: $categoryLabel",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF166534),
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kötelező Minimális Fedezet:",
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                            fontSize = 13.sp
                        )
                        Text(
                            text = requiredCoverageText,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981),
                            fontSize = 17.sp
                        )
                    }

                    Text(
                        text = "💡 Tipp: A biztosítási kötvényen fel kell tüntetni a drón pontos gyártóját, modelljét és gyári sorozatszámát (Serial Number) vagy lajstromjelét!",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF1E293B),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InsuranceProviderCard(
    provider: InsuranceProvider,
    isDarkMode: Boolean,
    onOpenUrl: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
                RoundedCornerShape(16.dp)
            )
            .testTag("insurance_card_${provider.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161F30) else Color(0xFFFFFFFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(provider.badgeColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, provider.badgeColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = provider.badgeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = provider.type,
                            fontSize = 11.sp,
                            color = provider.badgeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = provider.badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, provider.badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = provider.estimatedPrice,
                        color = provider.badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = provider.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                lineHeight = 17.sp
            )

            // Features bullets
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                provider.features.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = provider.badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = feat,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                    }
                }
            }

            Button(
                onClick = { onOpenUrl(provider.portalUrl) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = provider.badgeColor,
                    contentColor = if (provider.badgeColor == Color(0xFF00F0FF)) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = provider.portalButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CascoVsKgfbNoticeCard(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KGFB vs. Casco vs. DJI Care Refresh",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }

            Text(
                text = "• Kötelező Felelősségbiztosítás (KGFB): Kizárólag másoknak okozott kárt (autó, ház, személy) térít meg. Törvényi kötelezettség!\n• Drón Casco: A saját gépedben esett kárt (lezuhanás, törés) téríti meg.\n• DJI Care / Autel Care: Gyártói cseregarancia saját gépre. FIGYELEM: Nem minősül felelősségbiztosításnak, önmagában nem elegendő a legális repüléshez!",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                lineHeight = 18.sp
            )
        }
    }
}

// -------------------------------------------------------------
// STEP-BY-STEP GUIDE CONTENT
// -------------------------------------------------------------
@Composable
private fun StepByStepGuideContent(
    isDarkMode: Boolean,
    onOpenUrl: (String) -> Unit
) {
    val steps = listOf(
        Triple(
            "1. Üzembentartói Regisztráció",
            "Mielőtt a levegőbe emelkednél, regisztrálnod kell magadat üzembentartóként az ÉKM Légiközlekedési Hatóságánál (e-Papír vagy kormanyhivatal.hu felületen). Kapsz egy egyedi HUN... azonosítót.",
            "https://magyarorszag.hu"
        ),
        Triple(
            "2. Kötelező Felelősségbiztosítás Megkötése",
            "Kösd meg a kötelező KGFB biztosítást a drónod pontos adataival (típus, gyári sorozatszám). A kötvényt vagy az igazolást tartsd magadnál repülés közben!",
            "https://generali.hu"
        ),
        Triple(
            "3. KTI Online Vizsga Letétele",
            "Regisztrálj a KTI e-learning rendszerébe (vizsgakozpont.kti.hu). Végezd el az ingyenes felkészülést és tedd le az A1/A3 online elméleti vizsgát.",
            "https://vizsgakozpont.kti.hu"
        ),
        Triple(
            "4. HUN Azonosító Matrica Felragasztása",
            "Nyomtasd ki vagy készíttess matricát a hatóságtól kapott HUN... üzembentartói azonosítóddal, és ragaszd fel a drón vázára jól látható helyre.",
            ""
        ),
        Triple(
            "5. MyDroneSpace Alkalmazás Használata",
            "Telepítsd a HungaroControl hivatalos MyDroneSpace appját. Minden repülés előtt jelöld be a repülési helyszínt és a repülési zónát a légtérvédelem érdekében.",
            "https://mydronespace.hu"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "LÉPÉSRŐL-LÉPÉSRE ÚTMUTATÓ ÚJ ÉS GYAKORLÓ DRÓNOZÓKNAK",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        steps.forEachIndexed { index, (title, desc, link) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF161F30) else Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                        lineHeight = 17.sp
                    )

                    if (link.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onOpenUrl(link) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Hivatalos oldal megnyitása", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FAQ ACCORDION ITEM
// -------------------------------------------------------------
@Composable
private fun FaqAccordionItem(
    faq: FaqItem,
    isDarkMode: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isExpanded) Color(0xFFF59E0B).copy(alpha = 0.7f)
                else (if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)),
                RoundedCornerShape(14.dp)
            )
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF161F30) else Color(0xFFFFFFFF)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = faq.question,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF334155),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER FUNCTIONS & DATA
// -------------------------------------------------------------
private fun openWebUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "A weboldal nem nyitható meg: $url", Toast.LENGTH_SHORT).show()
    }
}

private fun getExamList(): List<ExamInfo> = listOf(
    ExamInfo(
        id = "a1_a3",
        title = "A1/A3 Nyílt Kategóriás Alapvizsga",
        category = "EASA NYÍLT KATEGÓRIA (ALAP)",
        badgeColor = Color(0xFF00F0FF),
        shortSummary = "Minden drónpilótának kötelező alapvizsga, ha a drónján van kamera, vagy ha a felszállósúly meghaladja a 250 grammot.",
        location = "KTI (Közlekedéstudományi Intézet) Hivatalos e-learning rendszere (100% online)",
        requirements = "Betöltött 16. életév, ügyfélkapus / KTI regisztráció. Előzetes repülési tapasztalat nem szükséges.",
        examFormat = "40 kérdéses feleletválasztós online teszt. Sikeres vizsgához legalább 75% (30 helyes válasz) szükséges.",
        priceInfo = "Ingyenes felkészítő tananyag, kedvező hatósági vizsgadíj",
        validity = "5 évig érvényes az összes EU/EASA tagállamban",
        targetAudience = "Kamerás minidrónokhoz (DJI Mini 2/3/4 Pro stb.) és 25 kg alatti gépekhez lakott területen kívül (A3).",
        portalName = "KTI Vizsgaközpont (A1/A3 Megnyitása)",
        portalUrl = "https://vizsgakozpont.kti.hu"
    ),
    ExamInfo(
        id = "a2",
        title = "A2 Nyílt Kategóriás Kiegészítő Vizsga",
        category = "EASA NYÍLT KATEGÓRIA (KÖZELI REPÜLÉS)",
        badgeColor = Color(0xFFF59E0B),
        shortSummary = "Jogosítvány a C2 osztályú drónokkal (max. 4 kg) lakott területen, emberekhez közelebb (akár 30 m / 5 m) történő repüléshez.",
        location = "KTI Kijelölt Vizsgaközpontok vagy akkreditált drone akadémiák",
        requirements = "Meglévő, érvényes A1/A3 pilóta tanúsítvány + igazolt önálló gyakorlati felkészülés.",
        examFormat = "30 kérdéses elméleti vizsga: meteorológia, repülési teljesítmény és technikai kockázatcsökkentés témakörökben.",
        priceInfo = "Hatósági vizsgadíj + opcionális felkészítő tanfolyam",
        validity = "5 évig érvényes az Európai Unióban",
        targetAudience = "Városi videósoknak, ingatlanfotósoknak és eseményrögzítőknek C2 besorolású gépekkel (pl. Mavic 3).",
        portalName = "A2 Képzési Információk (Légtér.hu)",
        portalUrl = "https://legter.hu"
    ),
    ExamInfo(
        id = "specific",
        title = "Speciális Kategória & STS Jogosítások",
        category = "EASA SPECIFIC / STS-01 / STS-02",
        badgeColor = Color(0xFF8B5CF6),
        shortSummary = "Látótávolságon túli (BVLOS), lakott terület feletti zárt légterű vagy mezőgazdasági permetező műveletekhez szükséges engedélyek.",
        location = "Hatóságilag jóváhagyott képzőszervezetek (Légtér.hu, ABZ Drone, HungaroControl)",
        requirements = "A1/A3 + A2 megléte, orvosi alkalmasság és elméleti + gyakorlati képzés elvégzése.",
        examFormat = "Gyakorlati repülési vizsga hatósági vizsgabiztos előtt + elméleti hatósági záróvizsga.",
        priceInfo = "Tanfolyami díj alapján",
        validity = "EU STS / Nemzeti Műveleti Engedély alapján",
        targetAudience = "Ipari felmérésekhez, permetező drónokhoz, hőkamerás hálózatvizsgálathoz és professzionális filmeseknek.",
        portalName = "ÉKM Légügyi Hatóság Portál",
        portalUrl = "https://kormanyhivatalok.hu"
    )
)

private fun getInsuranceProviders(): List<InsuranceProvider> = listOf(
    InsuranceProvider(
        name = "Generali Drónbiztosítás",
        type = "Dedikált Drón KGFB & Opcionális Casco",
        badgeColor = Color(0xFF00F0FF),
        description = "A legelterjedtebb magyarországi felelősségbiztosítás magánszemélyeknek és cégeknek. Megfelel az összes hazai jogszabálynak és a hatósági regisztrációhoz is azonnal elfogadják.",
        coverages = listOf("3.000.000 Ft – 10.000.000 Ft KGFB fedezet", "Saját géptörés Casco kiegészítés kérhető"),
        estimatedPrice = "kb. 8.000 - 16.000 Ft / év",
        features = listOf(
            "Gyors online kötés & azonnali igazolás",
            "Minden DJI, Autel, FPV és egyedi gépre megköthető",
            "EU-s területi hatály választható"
        ),
        portalUrl = "https://www.generali.hu",
        portalButtonText = "Generali Drónbiztosítás Megnyitása"
    ),
    InsuranceProvider(
        name = "Allianz Drón Felelősségbiztosítás",
        type = "Lakossági & Vállalati Felelősségbiztosítás",
        badgeColor = Color(0xFF3B82F6),
        description = "Kifejezetten pilóta nélküli légi járművek által okozott harmadik személynek okozott dologi és személyi károk megtérítésére szabott konstrukció.",
        coverages = listOf("Törvényi minimumnak megfelelő fedezet", "Jogi védelem kiegészítés"),
        estimatedPrice = "kb. 9.000 - 18.000 Ft / év",
        features = listOf(
            "Megbízható kárrendezési háttér",
            "Hobbi és üzleti célú használatra is",
            "Magyar és külföldi repülésekre"
        ),
        portalUrl = "https://www.allianz.hu",
        portalButtonText = "Allianz Portál Megnyitása"
    ),
    InsuranceProvider(
        name = "Coverdrone (Nemzetközi EASA)",
        type = "Rugalmas Online Nemzetközi Drónbiztosítás",
        badgeColor = Color(0xFF10B981),
        description = "Európa egyik legnagyobb specializált drónbiztosítója. Akár 1 napra, 1 hétre vagy 1 évre is köthető online, azonnali többnyelvű kötvénykibocsátással.",
        coverages = listOf("EASA rendelet szerinti magas fedezet", "Felelősségbiztosítás + Komplett Casco"),
        estimatedPrice = "Napi díjas vagy éves előfizetés",
        features = listOf(
            "Külföldi utazásokhoz és EU-s repülésekhez ideális",
            "1 napos időszakra is aktiválható",
            "Azonnali digitális igazolás okostelefonra"
        ),
        portalUrl = "https://www.coverdrone.com",
        portalButtonText = "Coverdrone Weboldal Megnyitása"
    ),
    InsuranceProvider(
        name = "Groupama Biztosító",
        type = "Felelősségbiztosítási Megoldások",
        badgeColor = Color(0xFFF59E0B),
        description = "Egyes lakossági felelősségbiztosítási vagy lakásbiztosítási csomagok kiegészítőjeként is köthető hobbi célú, kis tömegű drónokra.",
        coverages = listOf("Általános felelősségbiztosítási limit"),
        estimatedPrice = "Csomagfüggő",
        features = listOf(
            "Családi felelősségbiztosításhoz kapcsolható",
            "Személyes ügyintézés országszerte",
            "Káresemények gyors bejelentése online"
        ),
        portalUrl = "https://www.groupama.hu",
        portalButtonText = "Groupama Portál Megnyitása"
    )
)

private fun getFaqList(): List<FaqItem> = listOf(
    FaqItem(
        question = "Kell vizsga a 249 grammos drónomhoz (pl. DJI Mini sorozat)?",
        answer = "Igen! Bár a 250 g alatti drónok kedvezőbb kategóriába esnek, amennyiben a drónon kamera vagy egyéb személyes adatok rögzítésére alkalmas érzékelő van (és nem játék), a pilótának kötelező az A1/A3 alapvizsga és az üzembentartói regisztráció.",
        tag = "Vizsga"
    ),
    FaqItem(
        question = "Kötelező-e a felelősségbiztosítás minden drónra?",
        answer = "Igen, a 39/2001. Korm. rendelet szerint Magyarországon minden nem játéknak minősülő drónra kötelező a felelősségbiztosítás (KGFB), legalább 3 millió Ft kártérítési limittel.",
        tag = "Biztosítás"
    ),
    FaqItem(
        question = "Helyettesíti a DJI Care Refresh a kötelező biztosítást?",
        answer = "Nem! A DJI Care Refresh egy gyártói géptörés garancia/csere szolgáltatás saját gépre. Nem fizet harmadik félnek okozott károkért, ezért a törvény szerint NEM minősül felelősségbiztosításnak.",
        tag = "Biztosítás"
    ),
    FaqItem(
        question = "Érvényes-e a magyar KTI vizsga külföldön (pl. Horvátország, Ausztria, Olaszország)?",
        answer = "Igen! A KTI által kiállított EASA A1/A3 és A2 tanúsítványok egységes európai jogosítványok, és automatikusan érvényesek az összes Európai Uniós és EASA tagállamban.",
        tag = "EU Érvényesség"
    ),
    FaqItem(
        question = "Hogyan kapom meg a HUN azonosítómat a regisztráció után?",
        answer = "A Légiközlekedési Hatóság (ÉKM) a sikeres üzembentartói regisztrációt követően egy hivatalos határozatban küldi meg a HUN... kezdetű egyedi azonosítót, amelyet fel kell ragasztanod a drónod testére.",
        tag = "Regisztráció"
    )
)
