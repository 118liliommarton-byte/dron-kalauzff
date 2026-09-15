package com.example.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AirspaceDataRepository
import com.example.data.AirspaceGuideItem
import com.example.data.AirspaceZone
import com.example.data.CountryRule
import com.example.data.ZoneType

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAirspaceDialog(
    country: CountryRule,
    onDismiss: () -> Unit,
    onOpenRadar: () -> Unit
) {
    val context = LocalContext.current
    val zones = remember(country) { AirspaceDataRepository.getZonesForCountry(country) }
    val guideItems = remember { AirspaceDataRepository.guideItems }

    var selectedTab by remember { mutableStateOf(0) } // 0: Térkép & Zónák, 1: Zóna Lista, 2: Légtér Határozó
    var selectedZoneFilter by remember { mutableStateOf<ZoneType?>(null) }
    var zoneSearchQuery by remember { mutableStateOf("") }
    var focusedZoneId by remember { mutableStateOf<String?>(null) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val filteredZones = remember(zones, selectedZoneFilter, zoneSearchQuery) {
        zones.filter { zone ->
            (selectedZoneFilter == null || zone.type == selectedZoneFilter) &&
            (zoneSearchQuery.isEmpty() ||
             zone.name.contains(zoneSearchQuery, ignoreCase = true) ||
             zone.id.contains(zoneSearchQuery, ignoreCase = true) ||
             zone.nearbyCities.contains(zoneSearchQuery, ignoreCase = true) ||
             zone.description.contains(zoneSearchQuery, ignoreCase = true))
        }
    }

    // Generate Dynamic Leaflet Map HTML
    val mapHtml = remember(country, zones) {
        val zonesJsArray = StringBuilder()
        zones.forEach { zone ->
            val safeName = zone.name.replace("'", "\\'").replace("\"", "\\\"")
            val safeDesc = zone.description.replace("'", "\\'").replace("\"", "\\\"")
            val safePermit = zone.permitRequired.replace("'", "\\'").replace("\"", "\\\"")
            val safePenalty = zone.penalties.replace("'", "\\'").replace("\"", "\\\"")
            val safeCities = zone.nearbyCities.replace("'", "\\'").replace("\"", "\\\"")

            zonesJsArray.append("""
                {
                    id: '${zone.id}',
                    name: '$safeName',
                    type: '${zone.type.name}',
                    icon: '${zone.type.icon}',
                    badge: '${zone.type.badgeText}',
                    color: '${zone.type.colorHex}',
                    lat: ${zone.lat},
                    lng: ${zone.lng},
                    radius: ${zone.radiusMeters},
                    altitude: '${zone.maxDroneAltitude}',
                    limits: '${zone.lowerLimit} - ${zone.upperLimit}',
                    hours: '${zone.activityHours}',
                    desc: '$safeDesc',
                    permit: '$safePermit',
                    penalty: '$safePenalty',
                    cities: '$safeCities'
                },
            """)
        }

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body { margin: 0; padding: 0; background-color: #0A0F1D; color: #fff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                #map { width: 100vw; height: 100vh; background: #070B14; }
                .leaflet-container { background: #070B14; }
                .custom-popup .leaflet-popup-content-wrapper {
                    background: #111827;
                    color: #fff;
                    border: 1px solid #374151;
                    border-radius: 12px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.7);
                    padding: 4px;
                }
                .custom-popup .leaflet-popup-tip { background: #111827; }
                .zone-popup-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
                .zone-badge { font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px; display: inline-block; }
                .zone-row { font-size: 11px; margin: 4px 0; color: #D1D5DB; }
                .zone-penalty { background: rgba(239, 68, 68, 0.15); border: 1px solid #EF4444; color: #FCA5A5; padding: 5px; border-radius: 6px; font-size: 10px; margin-top: 6px; }
                .pulse-icon {
                    border-radius: 50%;
                    border: 2px solid white;
                    box-shadow: 0 0 12px rgba(6, 182, 212, 0.9);
                }
                /* Position zoom control below the top 'Összes' filter chip bar */
                .leaflet-top.leaflet-left {
                    top: 54px !important;
                    left: 12px !important;
                    transform: none !important;
                    margin-top: 0 !important;
                }
                .leaflet-touch .leaflet-control-zoom, .leaflet-control-zoom {
                    border: 1px solid #334155 !important;
                    border-radius: 12px !important;
                    box-shadow: 0 8px 24px rgba(0,0,0,0.7) !important;
                    overflow: hidden !important;
                }
                .leaflet-touch .leaflet-control-zoom a, .leaflet-control-zoom a {
                    background-color: #161F30 !important;
                    color: #06B6D4 !important;
                    border-bottom: 1px solid #334155 !important;
                    width: 40px !important;
                    height: 40px !important;
                    line-height: 38px !important;
                    font-size: 22px !important;
                    font-weight: bold !important;
                }
                .leaflet-touch .leaflet-control-zoom a:last-child, .leaflet-control-zoom a:last-child {
                    border-bottom: none !important;
                }
                .leaflet-touch .leaflet-control-zoom a:hover, .leaflet-control-zoom a:hover {
                    background-color: #1E293B !important;
                    color: #F97316 !important;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    center: [${country.lat}, ${country.lng}],
                    zoom: ${country.mapZoom},
                    zoomControl: true,
                    attributionControl: false
                });

                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19,
                    subdomains: 'abcd'
                }).addTo(map);

                var zonesData = [$zonesJsArray];
                var zoneLayers = {};

                zonesData.forEach(function(z) {
                    var circle = L.circle([z.lat, z.lng], {
                        color: z.color,
                        fillColor: z.color,
                        fillOpacity: z.type === 'OPEN_CATEGORY' ? 0.12 : 0.28,
                        weight: z.type === 'PROHIBITED_NO_FLY' ? 3 : 2,
                        dashArray: z.type === 'TEMPORARY_RESERVED' ? '6, 6' : null,
                        radius: z.radius
                    }).addTo(map);

                    var popupContent = `
                        <div class='custom-popup' style='min-width: 210px; max-width: 280px;'>
                            <div class='zone-popup-header'>
                                <span style='font-size: 16px;'>` + z.icon + `</span>
                                <b style='font-size: 12px; color: ` + z.color + `;'>` + z.name + `</b>
                            </div>
                            <div style='margin-bottom: 6px;'>
                                <span class='zone-badge' style='background: ` + z.color + `22; border: 1px solid ` + z.color + `; color: ` + z.color + `;'>` + z.badge + `</span>
                            </div>
                            <div class='zone-row'><b>Drón Plafon:</b> <span style='color: ` + (z.altitude.includes('0 m') ? '#EF4444' : '#10B981') + `; font-weight: bold;'>` + z.altitude + `</span></div>
                            <div class='zone-row'><b>Légtér Sáv:</b> ` + z.limits + `</div>
                            <div class='zone-row'><b>Aktív Időszak:</b> ` + z.hours + `</div>
                            <div class='zone-row' style='color:#9CA3AF;'>` + z.desc + `</div>
                            <div class='zone-penalty'>⚠️ <b>Engedély:</b> ` + z.permit + `</div>
                        </div>
                    `;

                    circle.bindPopup(popupContent);
                    zoneLayers[z.id] = { circle: circle, lat: z.lat, lng: z.lng };
                });

                // Map Click Inspector
                var inspectorMarker = null;
                map.on('click', function(e) {
                    var clickLat = e.latlng.lat;
                    var clickLng = e.latlng.lng;

                    // Find nearest zone
                    var nearestZone = null;
                    var minDistance = 999999;

                    zonesData.forEach(function(z) {
                        var d = map.distance([clickLat, clickLng], [z.lat, z.lng]);
                        if (d < minDistance) {
                            minDistance = d;
                            nearestZone = z;
                        }
                    });

                    var distKm = (minDistance / 1000).toFixed(1);
                    var isInside = nearestZone && (minDistance <= nearestZone.radius);

                    var statusBadge = isInside
                        ? `<span style='color:#EF4444; font-weight:bold;'>⚠️ ZÓNÁN BELÜL: ` + nearestZone.name + `</span>`
                        : `<span style='color:#10B981; font-weight:bold;'>🟢 Nyílt Légtér Térség</span>`;

                    var popupHtml = `
                        <div class='custom-popup' style='min-width: 200px;'>
                            <div style='font-size: 13px; font-weight: bold; margin-bottom: 4px;'>📍 Kiválasztott Pont</div>
                            <div class='zone-row'>` + statusBadge + `</div>
                            <div class='zone-row'><b>Koordináta:</b> ` + clickLat.toFixed(4) + `, ` + clickLng.toFixed(4) + `</div>
                            <div class='zone-row'><b>Legközelebbi zóna:</b> ` + (nearestZone ? nearestZone.name : 'N/A') + ` (` + distKm + ` km)</div>
                            <div class='zone-row'><b>Max magasság:</b> ` + (isInside ? nearestZone.altitude : '${country.maxAltitude}') + `</div>
                        </div>
                    `;

                    if (inspectorMarker) {
                        map.removeLayer(inspectorMarker);
                    }

                    inspectorMarker = L.marker([clickLat, clickLng]).addTo(map).bindPopup(popupHtml).openPopup();
                });

                window.focusZone = function(zoneId) {
                    if (zoneLayers[zoneId]) {
                        var item = zoneLayers[zoneId];
                        map.flyTo([item.lat, item.lng], 12, { animate: true, duration: 1.2 });
                        setTimeout(function() {
                            item.circle.openPopup();
                        }, 1300);
                    }
                };

                window.filterZones = function(filterType) {
                    zonesData.forEach(function(z) {
                        var item = zoneLayers[z.id];
                        if (item && item.circle) {
                            if (!filterType || filterType === 'ALL' || z.type === filterType) {
                                if (!map.hasLayer(item.circle)) {
                                    item.circle.addTo(map);
                                }
                            } else {
                                if (map.hasLayer(item.circle)) {
                                    map.removeLayer(item.circle);
                                }
                            }
                        }
                    });
                };
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    val appTheme = com.example.ui.theme.LocalAppTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(appTheme.dialogCornerRadiusDp.dp),
            color = appTheme.surfaceColor,
            border = BorderStroke(appTheme.dialogBorderWidthDp.dp, appTheme.cardBorderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Compact Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0F1D))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = country.flag, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${country.name} Légtér Zónák",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Összesen ${zones.size} zóna • Plafon: ${country.maxAltitude} • ${country.officialMapName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_app_drone_logo),
                            contentDescription = "Bezárás",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Compact Tab Switcher Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF161F30),
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp) },
                    modifier = Modifier.height(40.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.height(40.dp),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Térkép",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.height(40.dp),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Zónák (${filteredZones.size})",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.height(40.dp),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Útmutató",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.height(40.dp),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RssFeed,
                                    contentDescription = null,
                                    tint = if (selectedTab == 3) Color(0xFFF97316) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Élő NOTAM",
                                    fontSize = 11.5.sp,
                                    color = if (selectedTab == 3) Color(0xFFF97316) else Color.White,
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                // Main Content Body based on selected Tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // TAB 0: Interactive Map with Zone Filter Pills Overlay
                            Box(modifier = Modifier.fillMaxSize()) {
                                AndroidView(
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            settings.apply {
                                                javaScriptEnabled = true
                                                domStorageEnabled = true
                                                loadWithOverviewMode = true
                                                useWideViewPort = true
                                                cacheMode = WebSettings.LOAD_DEFAULT
                                            }
                                            webViewClient = WebViewClient()
                                            loadDataWithBaseURL(null, mapHtml, "text/html", "UTF-8", null)
                                            webViewInstance = this
                                        }
                                    },
                                    update = { webView ->
                                        val filterTypeStr = selectedZoneFilter?.name ?: "ALL"
                                        webView.evaluateJavascript("if (window.filterZones) { window.filterZones('$filterTypeStr'); }", null)

                                        if (focusedZoneId != null) {
                                            webView.evaluateJavascript("window.focusZone('$focusedZoneId');", null)
                                            focusedZoneId = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Top Zone Filter Chips Bar on Map
                                ZoneTypeFilterBar(
                                    zones = zones,
                                    selectedZoneFilter = selectedZoneFilter,
                                    onSelectZoneFilter = { selectedZoneFilter = it },
                                    modifier = Modifier.padding(8.dp)
                                )

                                // Bottom Floating Hint Banner
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(10.dp)
                                        .fillMaxWidth(0.92f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = Color(0xFF06B6D4),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Koppints bárhova a térképen a légtér ellenőrzéséhez, vagy válassz zónát a listából!",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 1: Detailed Scrollable Zone List
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                // Search Input for Zones
                                OutlinedTextField(
                                    value = zoneSearchQuery,
                                    onValueChange = { zoneSearchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("airspace_zone_search_input"),
                                    placeholder = { Text("Keresés név, repülőtér vagy város szerint...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8))
                                    },
                                    trailingIcon = {
                                        if (zoneSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { zoneSearchQuery = "" }) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = Color(0xFF94A3B8))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF161F30),
                                        unfocusedContainerColor = Color(0xFF161F30)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Category Filter Chips for List
                                ZoneTypeFilterBar(
                                    zones = zones,
                                    selectedZoneFilter = selectedZoneFilter,
                                    onSelectZoneFilter = { selectedZoneFilter = it }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (filteredZones.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Nem található zóna a megadott keresési feltételekkel.",
                                            color = Color(0xFF94A3B8),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        items(filteredZones, key = { it.id }) { zone ->
                                            AirspaceZoneCard(
                                                zone = zone,
                                                onFocusOnMap = {
                                                    focusedZoneId = zone.id
                                                    selectedTab = 0
                                                    webViewInstance?.evaluateJavascript("window.focusZone('${zone.id}');", null)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 2: Airspace Guide & Knowledge Base
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.VerifiedUser,
                                                    contentDescription = null,
                                                    tint = Color(0xFF06B6D4),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Légtér Kategória & Jogszabályi Útmutató",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "A drónozás során a légterek szigorú nemzetközi (ICAO/EASA) és nemzeti (KLH/HungaroControl) besorolás alá esnek. Itt megtalálod az összes zónatípus hivatalos szabályzatát és engedélyezési rendjét.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFCBD5E1)
                                            )
                                        }
                                    }
                                }

                                items(guideItems) { guide ->
                                    AirspaceGuideCard(guide = guide)
                                }
                            }
                        }

                        3 -> {
                            // TAB 3: NOTAM Live Radar & Decoder
                            NotamRadarView(
                                country = country,
                                zones = zones,
                                onFocusZoneOnMap = { zoneId ->
                                    focusedZoneId = zoneId
                                    selectedTab = 0
                                    webViewInstance?.evaluateJavascript("window.focusZone('$zoneId');", null)
                                }
                            )
                        }
                    }
                }

                // Footer Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0F1D))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(country.officialMapUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF0A0F1D), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = country.officialMapName,
                            color = Color(0xFF0A0F1D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenRadar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF06B6D4)),
                        border = BorderStroke(1.dp, Color(0xFF06B6D4))
                    ) {
                        Icon(imageVector = Icons.Default.Air, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Élő Széltérkép", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AirspaceZoneCard(
    zone: AirspaceZone,
    onFocusOnMap: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val zoneColor = remember(zone.type) {
        Color(android.graphics.Color.parseColor(zone.type.colorHex))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        border = BorderStroke(1.dp, zoneColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = zone.type.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(zoneColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, zoneColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = zone.type.badgeText,
                                fontSize = 10.sp,
                                color = zoneColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = onFocusOnMap) {
                    Icon(
                        imageVector = Icons.Default.LocationSearching,
                        contentDescription = "Térképre ugrás",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Parameters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Drón Plafon:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = zone.maxDroneAltitude,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (zone.maxDroneAltitude.contains("0 m")) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
                Column {
                    Text(text = "Légtér Sáv:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(text = zone.upperLimit, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text(text = "Sugár:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(text = "${zone.radiusMeters / 1000} km", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = zone.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                maxLines = if (isExpanded) 10 else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 6.dp))

                    // Permit Details
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Engedélyeztetési követelmények:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                            Text(text = zone.permitRequired, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Penalties
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Szankciók & Bírságok:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Text(text = zone.penalties, fontSize = 11.sp, color = Color(0xFFFCA5A5))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cities
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Érintett települések & Régió:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text(text = zone.nearbyCities, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Kevesebb részlet ▲" else "Teljes engedély & büntetések ▼",
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4)
                    )
                }

                Button(
                    onClick = onFocusOnMap,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Térképen", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AirspaceGuideCard(guide: AirspaceGuideItem) {
    var isExpanded by remember { mutableStateOf(false) }
    val itemColor = remember(guide.colorHex) {
        Color(android.graphics.Color.parseColor(guide.colorHex))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        border = BorderStroke(1.dp, itemColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = guide.icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = guide.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = guide.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1)
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(bottom = 8.dp))

                    Text(
                        text = "Engedélyezési & Belépési Feltételek:",
                        style = MaterialTheme.typography.labelMedium,
                        color = itemColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    guide.requirements.forEach { req ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                            Text(text = "•", color = itemColor, modifier = Modifier.padding(end = 6.dp))
                            Text(text = req, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kötelező Drónos Előírások:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF06B6D4),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    guide.rulesForDrones.forEach { rule ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                            Text(text = "•", color = Color(0xFF06B6D4), modifier = Modifier.padding(end = 6.dp))
                            Text(text = rule, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Penalty Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = guide.penaltyInfo, fontSize = 11.sp, color = Color(0xFFFCA5A5))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = { isExpanded = !isExpanded },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (isExpanded) "Kevesebb részlet ▲" else "Részletes szabályzat & követelmények ▼",
                    fontSize = 12.sp,
                    color = Color(0xFF06B6D4)
                )
            }
        }
    }
}

@Composable
fun NotamRadarView(
    country: CountryRule,
    zones: List<AirspaceZone>,
    onFocusZoneOnMap: (String) -> Unit
) {
    var notamSearchQuery by remember { mutableStateOf("") }
    var selectedFilterTag by remember { mutableStateOf<String?>(null) } // e.g. "LHBP", "KATONAI", "HEMS"
    var isInfoExpanded by remember { mutableStateOf(false) }

    val syncStatus = com.example.data.LiveAviationNetworkManager.syncStatus
    val coroutineScope = rememberCoroutineScope()

    val notamZones = remember(zones) {
        zones.filter { it.type != ZoneType.OPEN_CATEGORY }
    }

    val filteredNotams = remember(notamZones, notamSearchQuery, selectedFilterTag) {
        notamZones.filter { z ->
            (selectedFilterTag == null ||
             z.id.contains(selectedFilterTag!!, ignoreCase = true) ||
             z.name.contains(selectedFilterTag!!, ignoreCase = true) ||
             z.type.name.contains(selectedFilterTag!!, ignoreCase = true)) &&
            (notamSearchQuery.isEmpty() ||
             z.name.contains(notamSearchQuery, ignoreCase = true) ||
             z.id.contains(notamSearchQuery, ignoreCase = true) ||
             z.description.contains(notamSearchQuery, ignoreCase = true) ||
             z.nearbyCities.contains(notamSearchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // NOTAM Live Sync Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, if (syncStatus.isConnected) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFF97316).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (syncStatus.isSyncing) Color(0xFFF59E0B)
                                else if (syncStatus.isConnected) Color(0xFF10B981)
                                else Color(0xFFEF4444),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (syncStatus.isConnected) "ÉLŐ NOAA & AWC NOTAM CSATLAKOZVA" else "AUTOMATIKUS ÉLŐ NOTAM SZENZOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = syncStatus.statusMessage,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            com.example.data.LiveAviationNetworkManager.syncOnAppStartup()
                        }
                    },
                    enabled = !syncStatus.isSyncing,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (syncStatus.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFFF97316),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Frissítés",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data Scope & Limitations Disclaimer
        DataScopeDisclaimerCard()

        Spacer(modifier = Modifier.height(8.dp))

        // NOTAM Search Input
        OutlinedTextField(
            value = notamSearchQuery,
            onValueChange = { notamSearchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notam_search_input"),
            placeholder = { Text("NOTAM kód (pl. A0142/26), ICAO vagy kulcsszó keresése...", color = Color(0xFF94A3B8), fontSize = 12.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFFF97316))
            },
            trailingIcon = {
                if (notamSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { notamSearchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF97316),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF161F30),
                unfocusedContainerColor = Color(0xFF161F30)
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick ICAO / Type Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(
                "Összes" to null,
                "LHBP (Budapest)" to "LHBP",
                "Katonai D-Zóna" to "MILITARY",
                "HEMS Mentő" to "HEMS",
                "LHDC (Debrecen)" to "LHDC",
                "LOWW (Bécs)" to "LOWW"
            )

            filters.forEach { (label, tag) ->
                val isSelected = selectedFilterTag == tag
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterTag = if (isSelected) null else tag },
                    label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF97316),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFF97316) else Color(0xFF334155)),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Collapsible NOTAM Integration Explanatory Note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { isInfoExpanded = !isInfoExpanded },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = if (isInfoExpanded) 0.6f else 0.3f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NOTAM Integrációs Tájékoztató",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4)
                        )
                    }
                    IconButton(
                        onClick = { isInfoExpanded = !isInfoExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isInfoExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isInfoExpanded) "Becsukás" else "Kinyitás",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isInfoExpanded) {
                    Column(modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 6.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFF06B6D4).copy(alpha = 0.3f)
                        )
                        Text(
                            text = "A NOTAM-ok időlegesen hatályon kívül helyezhetik az alapvető nyílt légteret (pl. katonai lőgyakorlatok, VIP helikopter-kíséretek vagy légifényképészeti mérések idejére). A szimulált és élő adatcsatornából beérkező NOTAM-ok automatikusan kirajzolódnak az app interaktív térképén is!",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // NOTAM Item List
        if (filteredNotams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nincs aktív NOTAM korlátozás a kiválasztott szűrők alapján.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredNotams, key = { it.id }) { notam ->
                    NotamCardItem(
                        notam = notam,
                        onFocusOnMap = { onFocusZoneOnMap(notam.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotamCardItem(
    notam: AirspaceZone,
    onFocusOnMap: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = "📡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = notam.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF97316).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0xFFF97316), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = notam.type.badgeText,
                                fontSize = 9.sp,
                                color = Color(0xFFF97316),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Validity Time & Limits
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = notam.activityHours, fontSize = 11.sp, color = Color(0xFFFFEDD5), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Height, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Vertikális határ: ${notam.lowerLimit} - ${notam.upperLimit}", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notam.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1)
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(bottom = 8.dp))

                    Text(text = "Kötelező eljárás & Engedélyező:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                    Text(text = notam.permitRequired, fontSize = 11.sp, color = Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "Illetékes légiforgalmi hatóság:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                    Text(text = notam.contactAuthority, fontSize = 11.sp, color = Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "Érintett terület / Régió:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Text(text = notam.nearbyCities, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Kevesebb ▲" else "Nyers NOTAM & Részletek ▼",
                        fontSize = 11.sp,
                        color = Color(0xFFF97316)
                    )
                }

                Button(
                    onClick = onFocusOnMap,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.8f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Térképen Megjelenítés", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ZoneTypeFilterBar(
    zones: List<AirspaceZone>,
    selectedZoneFilter: ZoneType?,
    onSelectZoneFilter: (ZoneType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selectedZoneFilter == null,
            onClick = { onSelectZoneFilter(null) },
            label = {
                Text(
                    text = "✨ Összes (${zones.size})",
                    fontSize = 11.sp,
                    fontWeight = if (selectedZoneFilter == null) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color(0xFF0F172A).copy(alpha = 0.95f),
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                labelColor = Color.White
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Color(0xFF334155),
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                enabled = true,
                selected = selectedZoneFilter == null
            )
        )

        ZoneType.values().forEach { zType ->
            val count = zones.count { it.type == zType }
            if (count > 0) {
                val isSelected = selectedZoneFilter == zType
                val typeColor = Color(android.graphics.Color.parseColor(zType.colorHex))
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectZoneFilter(if (isSelected) null else zType) },
                    label = {
                        Text(
                            text = "${zType.icon} ${zType.shortName} ($count)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF0F172A).copy(alpha = 0.95f),
                        selectedContainerColor = typeColor.copy(alpha = 0.35f),
                        labelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF334155),
                        selectedBorderColor = typeColor,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }
    }
}

@Composable
fun DataScopeDisclaimerCard(
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ℹ️ ADATFORRÁS ÉS LEHATÁROLÁSI TÁJÉKOZTATÓ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Kattintson az adatok pontos tartalmához és a jogi korlátokhoz",
                            fontSize = 9.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✅ MIT TARTALMAZ AZ ALKALMAZÁS ADATBÁZISA?",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "• Állandó ellenőrzött légterek: Repülőtéri CTR-ek, repülési magassági korlátok.\n" +
                           "• Katonai és tiltott övezetek: ED-R, TSA, TRA légibázisok, lőterek, börtönök, atomerőművek.\n" +
                           "• Védett természeti területek: Nemzeti parkok, madárvédelmi körzetek.\n" +
                           "• Élő NOAA/AWC Hálózati Feed: Nemzetközi SIGMET légi veszélyességi táviratok és repülőtéri METAR időjárási riasztások valós időben.",
                    fontSize = 10.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "❌ MIT NEM TARTALMAZ (ÉS MIÉRT)?",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "• NEM TARTALMAZZA a magánszemélyek vagy cégek által igényelt egyedi, néhány órás helyi eseti drónzónákat (pl. helyi fotózás, események).\n" +
                           "• Az egyedi eseti drónos engedélyek zárt adatbázisához kizárólag a hivatalos nemzeti állami hatóságok rendelkeznek élő API-val.",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KÖTELEZŐ ELŐÍRÁS: Tényleges drónos felszállás előtt mindig kötelező a hivatalos nemzeti állami alkalmazás (pl. MyDroneSpace, DIPUL, Austro Control) konzultációja!",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE68A),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}


