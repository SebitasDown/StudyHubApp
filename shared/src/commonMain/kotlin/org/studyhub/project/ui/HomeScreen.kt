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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.studyhub.project.StudyHubColors
import org.studyhub.project.net.Api
import org.studyhub.project.net.CalendarEvent
import org.studyhub.project.net.DashboardSummary
import org.studyhub.project.net.GamificationProgress
import org.studyhub.project.net.RiskLevel
import org.studyhub.project.net.UpcomingClass
import org.studyhub.project.net.platformCurrentTimeHM
import org.studyhub.project.ui.icons.AppIcon
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    unread: Int,
    onBell: () -> Unit,
    onOpenSpace: (SpaceTarget) -> Unit,
    onOpenSubject: (Int, String, String) -> Unit,
    onOpenSubjects: () -> Unit,
) {
    var summary by remember { mutableStateOf<DashboardSummary?>(null) }
    var exams by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var todayClasses by remember { mutableStateOf<List<UpcomingClass>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            summary = Api.client.dashboard()
            error = null
        } catch (e: Exception) {
            error = e.message ?: "No se pudo cargar el resumen"
        } finally {
            loading = false
        }
        // Exámenes próximos (no bloquean el dashboard si fallan)
        exams = runCatching { Api.client.upcomingExams() }.getOrDefault(emptyList())

        // Clases de HOY (todas las del día, incluidas las en curso y las que ya terminaron):
        // el backend solo manda las que aún no empiezan, así que las calculamos de los horarios.
        try {
            val subjects = Api.client.subjects()
            val today = todayDayOfWeek()
            val details = coroutineScope {
                subjects.map { s ->
                    async { runCatching { Api.client.subjectDetail(s.id) }.getOrNull() }
                }.mapNotNull { it.await() }
            }
            todayClasses = details
                .flatMap { d ->
                    d.schedules.filter { it.dayOfWeek == today }.map { sch ->
                        UpcomingClass(
                            subject = d.nombre,
                            classroom = sch.classroom,
                            profesor = d.profesor,
                            startTime = sch.startTime,
                            endTime = sch.endTime,
                            color = d.color,
                        )
                    }
                }
                .sortedBy { it.startTime }
        } catch (_: Exception) {
            // Sin conexión: se muestran las del resumen (si las hay)
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> LoadingState()
            error != null -> ErrorState(error.orEmpty()) {
                // retry: force reload
                loading = true
                error = null
                summary = null
            }
            else -> summary?.let { DashboardContent(it, exams, todayClasses, unread, onBell, onOpenSpace, onOpenSubject, onOpenSubjects) }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardSummary,
    exams: List<CalendarEvent>,
    todayClasses: List<UpcomingClass>,
    unread: Int,
    onBell: () -> Unit,
    onOpenSpace: (SpaceTarget) -> Unit,
    onOpenSubject: (Int, String, String) -> Unit,
    onOpenSubjects: () -> Unit,
) {
    val stats = data.stats
    val gamification = data.gamification

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Este es tu resumen académico de hoy.",
                    color = StudyHubColors.TextSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "¡Hola, ${data.user?.nombre?.ifBlank { "Estudiante" }}!",
                    color = StudyHubColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            // La campana de notificaciones vive aquí, junto al saludo
            NotificationBell(unread = unread, onBell = onBell)
        }
        Spacer(Modifier.height(12.dp))

        // Racha
        if ((gamification?.streak ?: 0) > 0) {
            StreakPill(gamification!!.streak)
            Spacer(Modifier.height(16.dp))
        }

        // Stats 2x2
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                icon = Icons.Outlined.Book,
                tint = StudyHubColors.InfoLight,
                value = "${stats?.subjects ?: 0}",
                label = "Materias Registradas",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Outlined.PendingActions,
                tint = StudyHubColors.DangerLight,
                value = "${stats?.pendingTasks ?: 0}",
                label = "Tareas Pendientes",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                icon = Icons.Outlined.CheckCircle,
                tint = StudyHubColors.SecondaryLight,
                value = "${stats?.completedTasks ?: 0}",
                label = "Tareas Completadas",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Outlined.NoteAlt,
                tint = StudyHubColors.InfoLight,
                value = "${stats?.notes ?: 0}",
                label = "Apuntes Creados",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(20.dp))

        // Progreso de tareas
        val completion = data.completionRate
        val done = stats?.completedTasks ?: 0
        val total = (stats?.completedTasks ?: 0) + (stats?.pendingTasks ?: 0)
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Progreso de Tareas",
                    color = StudyHubColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${completion.roundToInt()}%",
                    color = StudyHubColors.PrimaryLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            GlassProgress(progress = (completion / 100f).toFloat())
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$done de $total tareas completadas",
                color = StudyHubColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(20.dp))

        // Clases de hoy (todas las del día: próximas, en curso y terminadas)
        val classesToShow = todayClasses.ifEmpty { data.upcomingClasses }
        if (classesToShow.isNotEmpty()) {
            SectionTitle(
                text = "Clases de Hoy",
                icon = Icons.Outlined.Schedule,
                iconTint = StudyHubColors.InfoLight,
                action = "Ver horario",
                onAction = { onOpenSpace(SpaceTarget.SCHEDULE) },
            )
            Spacer(Modifier.height(10.dp))
            classesToShow.forEach { cls ->
                GlassCard(modifier = Modifier.padding(bottom = 8.dp), radius = 16.dp, contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(70.dp)) {
                            Text(
                                text = cls.startTime,
                                color = StudyHubColors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = cls.endTime,
                                color = StudyHubColors.TextTertiary,
                                fontSize = 11.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(38.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                                .background(parseColor(cls.color)),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = cls.subject,
                                color = StudyHubColors.TextPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${cls.profesor} · Salón: ${cls.classroom}",
                                color = StudyHubColors.TextTertiary,
                                fontSize = 12.sp,
                            )
                        }
                        when (classStatus(cls.startTime, cls.endTime)) {
                            "En curso" -> Badge("En curso", StudyHubColors.AccentLight, small = true)
                            "Terminó" -> Text("Terminó", color = StudyHubColors.TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            else -> {}
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Exámenes y eventos
        if (exams.isNotEmpty()) {
            SectionTitle(
                text = "Exámenes y Eventos",
                icon = Icons.Outlined.Event,
                iconTint = StudyHubColors.DangerLight,
                action = "Ver agenda",
                onAction = { onOpenSpace(SpaceTarget.CALENDAR) },
            )
            Spacer(Modifier.height(10.dp))
            exams.forEach { exam ->
                GlassCard(modifier = Modifier.padding(bottom = 8.dp), radius = 16.dp, contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge("Examen", StudyHubColors.DangerLight, small = true)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = exam.title.ifBlank { "Examen" },
                                color = StudyHubColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = buildString {
                                    append("${formatDate(exam.startAt)} · ${timeOf(exam.startAt)}")
                                    if (!exam.subject?.nombre.isNullOrBlank()) {
                                        append(" · ${exam.subject?.nombre}")
                                    }
                                },
                                color = StudyHubColors.TextTertiary,
                                fontSize = 11.5.sp,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        DaysLeftBadge(exam.startAt)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Tareas próximas
        if (data.upcomingTasks.isNotEmpty()) {
            SectionTitle(
                text = "Tareas Próximas",
                icon = Icons.Outlined.PendingActions,
                iconTint = StudyHubColors.AccentLight,
                action = "Ver materias",
                onAction = onOpenSubjects,
            )
            Spacer(Modifier.height(10.dp))
            data.upcomingTasks.forEach { task ->
                GlassCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    radius = 16.dp,
                    contentPadding = 14.dp,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeadingIcon(Icons.Outlined.PendingActions, StudyHubColors.AccentLight)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = StudyHubColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(task.subject, parseColor(task.subjectColor), small = true)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = formatDate(task.dueDate),
                                    color = StudyHubColors.TextTertiary,
                                    fontSize = 11.5.sp,
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        DaysLeftBadge(task.dueDate)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Apuntes fijados
        val pinnedNotes = data.recentNotes.filter { it.pinned }
        if (pinnedNotes.isNotEmpty()) {
            SectionTitle(
                text = "Apuntes Fijados",
                icon = Icons.Outlined.PushPin,
                iconTint = StudyHubColors.AccentLight,
            )
            Spacer(Modifier.height(10.dp))
            pinnedNotes.forEach { note ->
                GlassCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    radius = 16.dp,
                    contentPadding = 14.dp,
                    borderColor = StudyHubColors.Accent.copy(alpha = 0.4f),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(Icons.Filled.PushPin, tint = StudyHubColors.Accent, size = 16.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = note.title,
                                color = StudyHubColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (note.subject.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Badge(note.subject, parseColor(note.subjectColor), small = true)
                            }
                            if (note.content.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = note.content,
                                    color = StudyHubColors.TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Gamificación
        if (gamification != null) {
            GamificationCard(gamification)
            Spacer(Modifier.height(20.dp))
        }

        // Metas de aprendizaje
        if (data.activeGoals.isNotEmpty()) {
            SectionTitle(
                text = "Metas de Aprendizaje",
                icon = Icons.Outlined.TrackChanges,
                iconTint = StudyHubColors.PrimaryLight,
                action = "Gestionar metas",
                onAction = { onOpenSpace(SpaceTarget.GOALS) },
            )
            Spacer(Modifier.height(10.dp))
            data.activeGoals.forEach { goal ->
                GlassCard(modifier = Modifier.padding(bottom = 8.dp), radius = 16.dp, contentPadding = 14.dp) {
                    Text(
                        text = goal.title,
                        color = StudyHubColors.TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    GlassProgress(progress = goal.progress / 100f, color = StudyHubColors.SecondaryLight)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun StreakPill(streak: Int) {
    Row(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF97316))))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(Icons.Outlined.LocalFireDepartment, tint = Color.White, size = 17.dp)
        Spacer(Modifier.width(7.dp))
        Text(
            text = "$streak días de racha",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
fun GamificationCard(g: GamificationProgress) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeadingIcon(Icons.Outlined.WorkspacePremium, StudyHubColors.AccentLight)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Nivel ${g.level}",
                    color = StudyHubColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "${g.xp} XP · ${g.xpForNextLevel} XP para el siguiente nivel",
                    color = StudyHubColors.TextTertiary,
                    fontSize = 12.sp,
                )
            }
            Badge("${g.achievements} logros", StudyHubColors.AccentLight)
        }
        Spacer(Modifier.height(12.dp))
        GlassProgress(
            progress = if (g.xpForNextLevel > 0) g.xp.toFloat() / g.xpForNextLevel else 0f,
            color = StudyHubColors.Accent,
        )
    }
}

@Composable
fun RiskCard(risk: RiskLevel, onOpenSpace: () -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeadingIcon(Icons.Outlined.Security, StudyHubColors.DangerLight)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Riesgo Académico",
                    color = StudyHubColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Nivel de riesgo: ${risk.level}",
                    color = riskColor(risk.level),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            ScoreRing(score = risk.score, label = "riesgo", color = riskColor(risk.level), size = 74.dp)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenSpace)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Ver análisis",
                color = StudyHubColors.PrimaryLight,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
            AppIcon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                tint = StudyHubColors.PrimaryLight,
                size = 15.dp,
            )
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────

fun parseColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val value = clean.toLongOrNull(16) ?: return StudyHubColors.Primary
    return if (clean.length == 6) Color(0xFF000000L or value) else Color(value)
}

fun riskColor(level: String): Color = when (level.uppercase()) {
    "ALTO", "HIGH" -> StudyHubColors.DangerLight
    "MEDIO", "MEDIUM" -> StudyHubColors.AccentLight
    else -> StudyHubColors.SecondaryLight
}

val MONTHS_SHORT = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

/** Días naturales que faltan para la fecha ISO (UTC); negativo si ya pasó. */
@OptIn(kotlin.time.ExperimentalTime::class)
fun daysUntilDue(iso: String): Int? {
    if (iso.isBlank()) return null
    val parts = iso.substringBefore("T").split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    val dueDay = dateToEpochDay(y, m, d) ?: return null
    val today = kotlin.time.Clock.System.now().toEpochMilliseconds() / 86_400_000L
    return (dueDay - today).toInt()
}

/** Día epoch (días desde 1970-01-01) de una fecha civil. */
internal fun dateToEpochDay(y: Int, m: Int, d: Int): Long? {
    if (m < 1 || m > 12 || d < 1 || d > 31) return null
    var year = y.toLong()
    var month = m.toLong()
    if (month <= 2) {
        year -= 1
        month += 12
    }
    val era = year / 400
    val yoe = year - era * 400
    val doy = (153 * (month - 3) + 2) / 5 + d - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

/** Fecha de hoy en formato AAAA-MM-DD (UTC). */
@OptIn(kotlin.time.ExperimentalTime::class)
fun todayIso(): String {
    val epochDay = kotlin.time.Clock.System.now().toEpochMilliseconds() / 86_400_000L
    var z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    val year = if (m <= 2) y + 1 else y
    fun p2(n: Long) = n.toString().padStart(2, '0')
    return "$year-${p2(m)}-${p2(d)}"
}

/** Badge con los días que faltan para la fecha límite (reemplaza la prioridad). */
@Composable
fun DaysLeftBadge(dueDate: String) {
    val days = daysUntilDue(dueDate) ?: return
    val (text, color) = when {
        days < 0 -> "Atrasada" to StudyHubColors.DangerLight
        days == 0 -> "Hoy" to StudyHubColors.AccentLight
        days == 1 -> "Mañana" to StudyHubColors.AccentLight
        else -> "en $days días" to StudyHubColors.InfoLight
    }
    Badge(text, color, small = true)
}

fun formatDate(iso: String): String {
    if (iso.isBlank()) return ""
    val datePart = iso.substringBefore("T")
    val parts = datePart.split("-")
    if (parts.size != 3) return datePart
    val day = parts[2].toIntOrNull() ?: return datePart
    val month = parts[1].toIntOrNull() ?: return datePart
    return "$day ${MONTHS_SHORT.getOrNull(month - 1) ?: parts[1]}"
}

private fun timeOf(iso: String): String {
    if (iso.isBlank()) return ""
    val t = iso.substringAfter("T", "").substringBefore("Z").substringBefore("+")
    return t.split(":").take(2).joinToString(":")
}

/** dayOfWeek del backend (0=Domingo … 6=Sábado) para hoy. */
internal fun todayDayOfWeek(): Int {
    val today = kotlin.time.Clock.System.now().toEpochMilliseconds() / 86_400_000L
    // 1970-01-01 (día epoch 0) fue jueves → dayOfWeek 4 en la convención 0=Domingo
    return (((today + 4) % 7) + 7).toInt() % 7
}

/** "En curso", "Terminó" o null según la hora actual local. */
internal fun classStatus(startTime: String, endTime: String): String? {
    if (startTime.isBlank() || endTime.isBlank()) return null
    val now = platformCurrentTimeHM()
    return when {
        startTime <= now && now < endTime -> "En curso"
        endTime <= now -> "Terminó"
        else -> null
    }
}

