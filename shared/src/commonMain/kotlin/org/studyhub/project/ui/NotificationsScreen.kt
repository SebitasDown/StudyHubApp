package org.studyhub.project.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.studyhub.project.StudyHubColors
import org.studyhub.project.net.Api
import org.studyhub.project.net.NotificationItem
import org.studyhub.project.ui.icons.AppIcon

@Composable
fun NotificationsOverlay(onClose: () -> Unit) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reload) {
        loading = true
        try {
            notifications = Api.client.notifications()
            error = null
        } catch (e: Exception) {
            error = e.message ?: "No se pudieron cargar las notificaciones"
        } finally {
            loading = false
        }
    }

    fun markAll() {
        scope.launch {
            runCatching { Api.client.markAllRead() }
            reload++
        }
    }

    fun markOne(id: Int) {
        scope.launch {
            runCatching { Api.client.markRead(id) }
            reload++
        }
    }

    Column(Modifier.fillMaxSize().background(StudyHubColors.Bg)) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(StudyHubColors.Surface)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(Icons.AutoMirrored.Outlined.ArrowBack, tint = StudyHubColors.TextPrimary, size = 20.dp)
            }
            Spacer(Modifier.width(12.dp))
            Text("Notificaciones", color = StudyHubColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(StudyHubColors.Surface)
                    .clickable(onClick = ::markAll)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(Icons.Outlined.DoneAll, tint = StudyHubColors.PrimaryLight, size = 15.dp)
                Spacer(Modifier.width(5.dp))
                Text("Leer todas", color = StudyHubColors.PrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(Modifier.weight(1f)) {
            when {
                loading -> LoadingState()
                error != null -> ErrorState(error.orEmpty()) {
                    loading = true
                    error = null
                    reload++
                }
                notifications.isEmpty() -> EmptyState("No tienes notificaciones por ahora.")
                else -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                ) {
                    notifications.forEach { n ->
                        val unread = !n.read
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (unread) StudyHubColors.Glass else StudyHubColors.Surface)
                                .clickable { if (unread) markOne(n.id) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LeadingIcon(notificationIcon(n.type), notificationColor(n.type))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    n.title,
                                    color = StudyHubColors.TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (unread) FontWeight.ExtraBold else FontWeight.SemiBold,
                                )
                                Text(n.message, color = StudyHubColors.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                                Spacer(Modifier.height(3.dp))
                                Text(formatDate(n.createdAt), color = StudyHubColors.TextTertiary, fontSize = 10.5.sp)
                            }
                            if (unread) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(StudyHubColors.PrimaryLight),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun notificationIcon(type: String): ImageVector = when (type.uppercase()) {
    "TASK_DUE", "EXAM_ALERT" -> Icons.Outlined.EventNote
    "KNOWLEDGE_GAP" -> Icons.Outlined.Psychology
    "STREAK_RISK" -> Icons.Outlined.LocalFireDepartment
    "CLASS_REMINDER" -> Icons.Outlined.Notifications
    else -> Icons.Outlined.WarningAmber
}

private fun notificationColor(type: String): Color = when (type.uppercase()) {
    "TASK_DUE", "EXAM_ALERT" -> StudyHubColors.DangerLight
    "KNOWLEDGE_GAP" -> StudyHubColors.PrimaryLight
    "STREAK_RISK" -> StudyHubColors.AccentLight
    "CLASS_REMINDER" -> StudyHubColors.InfoLight
    else -> StudyHubColors.TextSecondary
}
