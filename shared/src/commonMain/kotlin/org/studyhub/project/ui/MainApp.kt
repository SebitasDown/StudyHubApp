package org.studyhub.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.studyhub.project.StudyHubColors
import org.studyhub.project.net.Api
import org.studyhub.project.net.OfflineState
import org.studyhub.project.net.TokenStore
import org.studyhub.project.ui.icons.AppIcon

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Outlined.Home),
    SUBJECTS("Materias", Icons.Outlined.MenuBook),
    TUTOR("Tutor IA", Icons.Outlined.SmartToy),
    ROADMAPS("Rutas", Icons.Outlined.Route),
    PROFILE("Perfil", Icons.Outlined.Person),
}

/** Vistas completas que se abren encima de las pestañas (con botón ‹ para volver). */
sealed interface FullScreen {
    data class Space(val target: SpaceTarget) : FullScreen
    data class SubjectDetail(
        val subjectId: Int,
        val subjectName: String,
        val colorHex: String,
        /** Pestaña desde la que se abrió: el back vuelve a ella. */
        val origin: AppTab,
    ) : FullScreen
}

@Composable
fun MainApp(onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(AppTab.HOME) }
    var fullScreen by remember { mutableStateOf<FullScreen?>(null) }
    var notifOpen by remember { mutableStateOf(false) }
    var unread by remember { mutableStateOf(0) }
    var refreshTick by remember { mutableStateOf(0) }

    val demo = remember { TokenStore.load() == null }
    val scope = rememberCoroutineScope()

    var offline by remember { mutableStateOf(!OfflineState.isOnline) }
    DisposableEffect(Unit) {
        OfflineState.onChanged = { offline = !OfflineState.isOnline }
        onDispose { OfflineState.onChanged = null }
    }

    LaunchedEffect(refreshTick) {
        unread = runCatching { Api.client.unreadCount() }.getOrDefault(unread)
    }

    Box(Modifier.fillMaxSize().background(StudyHubColors.Bg)) {
        Column(Modifier.fillMaxSize()) {
            if (offline) {
                OfflineBanner(
                    pending = OfflineState.pendingCount,
                    onRetry = {
                        scope.launch {
                            Api.client.flushPending()
                            refreshTick++
                        }
                    },
                )
            }
            if (demo) {
                DemoBanner()
            }
            Box(Modifier.weight(1f)) {
                when (tab) {
                    AppTab.HOME -> HomeScreen(
                        unread = unread,
                        onBell = { notifOpen = true },
                        onOpenSpace = { fullScreen = FullScreen.Space(it) },
                        onOpenSubject = { id, name, color -> fullScreen = FullScreen.SubjectDetail(id, name, color, AppTab.HOME) },
                        onOpenSubjects = { tab = AppTab.SUBJECTS },
                    )
                    AppTab.SUBJECTS -> SubjectsScreen(
                        onOpenDetail = { id, name, color -> fullScreen = FullScreen.SubjectDetail(id, name, color, AppTab.SUBJECTS) },
                    )
                    AppTab.TUTOR -> TutorScreen()
                    AppTab.ROADMAPS -> RoadmapsScreen()
                    AppTab.PROFILE -> ProfileScreen(
                        onLogout = onLogout,
                        onOpenSpace = { fullScreen = FullScreen.Space(it) },
                    )
                }
            }
            BottomNav(tab = tab, onSelect = { tab = it })
        }

        val current = fullScreen
        fun closeCurrent() {
            // El detalle de materia vuelve a la pestaña desde la que se abrió
            if (current is FullScreen.SubjectDetail) tab = current.origin
            fullScreen = null
            refreshTick++
        }
        // El botón back del sistema cierra la vista abierta en vez de la app
        PlatformBackHandler(enabled = current != null, onBack = ::closeCurrent)
        PlatformBackHandler(enabled = notifOpen) {
            notifOpen = false
            refreshTick++
        }
        if (current != null) {
            FullScreenView(screen = current, onBack = ::closeCurrent)
        }
        if (notifOpen) {
            NotificationsOverlay(
                onClose = {
                    notifOpen = false
                    refreshTick++
                },
            )
        }
    }
}

// ─── Campana de notificaciones (solo en el dashboard, junto al saludo) ──

@Composable
fun NotificationBell(unread: Int, onBell: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(StudyHubColors.Surface)
                .border(1.dp, StudyHubColors.Border, CircleShape)
                .clickable(onClick = onBell),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(Icons.Outlined.Notifications, tint = StudyHubColors.TextSecondary, size = 20.dp)
        }
        if (unread > 0) {
            Box(
                modifier = Modifier
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(StudyHubColors.Danger)
                    .border(2.dp, StudyHubColors.Bg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unread > 9) "9+" else "$unread",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DemoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StudyHubColors.Accent.copy(alpha = 0.12f))
            .border(1.dp, StudyHubColors.Accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(Icons.Outlined.SmartToy, tint = StudyHubColors.AccentLight, size = 16.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Modo demo: inicia sesión para ver tus datos reales",
            color = StudyHubColors.AccentLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── Banner sin conexión ────────────────────────────────────────────────

@Composable
private fun OfflineBanner(pending: Int, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StudyHubColors.Danger.copy(alpha = 0.14f))
            .border(1.dp, StudyHubColors.Danger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(Icons.Outlined.Notifications, tint = StudyHubColors.DangerLight, size = 16.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (pending > 0) {
                "Sin conexión · $pending cambio${if (pending == 1) "" else "s"} pendiente${if (pending == 1) "" else "s"} de sincronizar"
            } else {
                "Sin conexión · mostrando datos guardados"
            },
            color = StudyHubColors.DangerLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Reintentar",
            color = StudyHubColors.DangerLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

// ─── Barra de navegación inferior (píldora de cristal) ─────────────────

@Composable
private fun BottomNav(tab: AppTab, onSelect: (AppTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(StudyHubColors.Glass)
            .border(1.dp, StudyHubColors.GlassBorder, RoundedCornerShape(24.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AppTab.entries.forEach { item ->
            val active = item == tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .then(
                        if (active) {
                            Modifier.background(Brush.linearGradient(listOf(StudyHubColors.Primary, StudyHubColors.PrimaryDark)))
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(item) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppIcon(
                    icon = item.icon,
                    tint = if (active) Color.White else StudyHubColors.TextSecondary,
                    size = 22.dp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.label,
                    color = if (active) Color.White else StudyHubColors.TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

// ─── Overlay de vista completa ──────────────────────────────────────────

@Composable
private fun FullScreenView(screen: FullScreen, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(StudyHubColors.Bg)) {
        when (screen) {
            is FullScreen.Space -> SecondaryScreen(target = screen.target, onBack = onBack)
            is FullScreen.SubjectDetail -> SubjectDetailScreen(
                subjectId = screen.subjectId,
                subjectName = screen.subjectName,
                colorHex = screen.colorHex,
                onBack = onBack,
            )
        }
    }
}

@Composable
fun FullScreenBackBar(title: String, subtitle: String? = null, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(StudyHubColors.Surface)
                .border(1.dp, StudyHubColors.Border, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(Icons.AutoMirrored.Outlined.ArrowBack, tint = StudyHubColors.TextPrimary, size = 20.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = StudyHubColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = StudyHubColors.TextTertiary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
