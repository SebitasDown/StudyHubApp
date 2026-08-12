package org.studyhub.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.SmartToy
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.studyhub.project.StudyHubColors
import org.studyhub.project.net.Api
import org.studyhub.project.net.Conversation
import org.studyhub.project.net.TeacherProfile
import org.studyhub.project.net.TokenStore
import org.studyhub.project.ui.icons.AppIcon

private const val KEY_TEACHER = "studyhub_teacher_id"
private const val KEY_CONVERSATION = "studyhub_conversation_id"

private data class ChatUiMessage(val role: String, val content: String)

private val SUGGESTIONS = listOf(
    "Explícame un tema que no entiendo",
    "Prepara un plan de estudio para hoy",
    "Ayúdame con una tarea",
    "¿Qué brechas de conocimiento tengo?",
)

private enum class TutorTab { CHAT, QUIZZES, HISTORY }

@Composable
fun TutorScreen() {
    var messages by remember { mutableStateOf(listOf<ChatUiMessage>()) }
    var input by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(false) }
    var conversationId by remember { mutableStateOf<String?>(null) }
    var teachers by remember { mutableStateOf<List<TeacherProfile>>(emptyList()) }
    var teacherId by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(TutorTab.CHAT) }
    var history by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        // Restaurar profesor elegido
        TokenStore.loadString(KEY_TEACHER)?.takeIf { it.isNotBlank() }?.let { teacherId = it }
        // Restaurar conversación guardada
        val savedConv = TokenStore.loadString(KEY_CONVERSATION)?.takeIf { it.isNotBlank() }
        if (savedConv != null) {
            conversationId = savedConv
            val loaded = runCatching { Api.client.conversationMessages(savedConv) }.getOrNull()
            if (loaded != null && loaded.isNotEmpty()) {
                messages = loaded.map { ChatUiMessage(it.role, it.content) }
            } else {
                conversationId = null
                TokenStore.saveString(KEY_CONVERSATION, "")
            }
        }
        teachers = runCatching { Api.client.teacherProfiles() }
            .getOrDefault(emptyList())
            .filter { it.active }
    }

    LaunchedEffect(messages.size, typing) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Cargar historial al abrir la pestaña
    LaunchedEffect(tab) {
        if (tab == TutorTab.HISTORY) {
            historyLoading = true
            historyError = null
            try {
                history = Api.client.conversations()
            } catch (e: Exception) {
                historyError = e.message ?: "No se pudo cargar el historial"
            } finally {
                historyLoading = false
            }
        }
    }

    fun openConversation(conv: Conversation) {
        scope.launch {
            val loaded = runCatching { Api.client.conversationMessages(conv.id) }.getOrNull()
            if (loaded != null && loaded.isNotEmpty()) {
                messages = loaded.map { ChatUiMessage(it.role, it.content) }
                conversationId = conv.id
                TokenStore.saveString(KEY_CONVERSATION, conv.id)
                tab = TutorTab.CHAT
            } else {
                historyError = "No se pudo abrir esa conversación"
            }
        }
    }

    fun removeConversation(conv: Conversation) {
        scope.launch {
            runCatching { Api.client.deleteConversation(conv.id) }
            if (conversationId == conv.id) {
                conversationId = null
                TokenStore.saveString(KEY_CONVERSATION, "")
                messages = emptyList()
            }
            history = runCatching { Api.client.conversations() }.getOrDefault(history - conv)
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || typing) return
        messages = messages + ChatUiMessage("user", trimmed)
        input = ""
        typing = true
        scope.launch {
            try {
                val response = Api.client.chat(trimmed, conversationId, teacherId)
                conversationId = response.conversationId.ifBlank { conversationId }
                conversationId?.let { TokenStore.saveString(KEY_CONVERSATION, it) }
                val reply = response.reply.ifBlank { "No pude generar una respuesta." }
                messages = messages + ChatUiMessage("assistant", reply)
            } catch (e: Exception) {
                messages = messages + ChatUiMessage(
                    "assistant",
                    "No pude conectar con el tutor IA: ${e.message ?: "error de red"}",
                )
            } finally {
                typing = false
            }
        }
    }

    val teacherName = teacherId?.let { id -> teachers.firstOrNull { it.id == id }?.name }

    Column(Modifier.fillMaxSize()) {
        // Encabezado del tutor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(StudyHubColors.Primary, StudyHubColors.PrimaryDark),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(Icons.Outlined.SmartToy, tint = Color.White, size = 22.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Tutor IA", color = StudyHubColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(androidx.compose.foundation.shape.CircleShape).background(StudyHubColors.Secondary))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        teacherName ?: "En línea",
                        color = StudyHubColors.SecondaryLight,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
            // Nueva conversación
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudyHubColors.Surface)
                    .clickable {
                        messages = emptyList()
                        conversationId = null
                        TokenStore.saveString(KEY_CONVERSATION, "")
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(Icons.Outlined.AddComment, tint = StudyHubColors.PrimaryLight, size = 16.dp)
                Spacer(Modifier.width(5.dp))
                Text("Nueva", color = StudyHubColors.PrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Selector de profesor IA (solo en el chat)
        if (tab == TutorTab.CHAT && teachers.isNotEmpty()) {
            ProfessorSelector(
                teachers = teachers,
                selectedId = teacherId,
                onSelect = {
                    teacherId = it
                    TokenStore.saveString(KEY_TEACHER, it ?: "")
                },
            )
        }

        // Pestañas Chat / Quizzes
        TutorTabs(current = tab, onSelect = { tab = it })

        Box(Modifier.weight(1f)) {
            if (tab == TutorTab.QUIZZES) {
                QuizzesScreen(onBack = null)
            } else if (tab == TutorTab.HISTORY) {
                HistoryList(
                    conversations = history,
                    loading = historyLoading,
                    error = historyError,
                    onOpen = ::openConversation,
                    onDelete = ::removeConversation,
                    onRetry = {
                        historyLoading = true
                        historyError = null
                        scope.launch {
                            history = runCatching { Api.client.conversations() }.getOrDefault(history)
                            historyLoading = false
                        }
                    },
                )
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    if (messages.isEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Hola, soy tu tutor de estudio con IA. Elige un profesor arriba o pregunta lo que necesites: dudas, planes, tareas o brechas.",
                            color = StudyHubColors.TextSecondary,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(StudyHubColors.Surface)
                                .padding(14.dp),
                        )
                        Spacer(Modifier.height(18.dp))
                        Text("Sugerencias", color = StudyHubColors.TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        SUGGESTIONS.forEach { s ->
                            GlassChip(
                                text = s,
                                selected = false,
                                onClick = { send(s) },
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    } else {
                        messages.forEach { msg ->
                            MessageBubble(msg)
                        }
                        if (typing) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(StudyHubColors.Surface)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Text("Escribiendo…", color = StudyHubColors.TextTertiary, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // Entrada (solo en el chat)
        if (tab == TutorTab.CHAT) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(StudyHubColors.Surface)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = StudyHubColors.TextPrimary,
                        fontSize = 14.sp,
                    ),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (input.isEmpty()) {
                            Text("Escribe tu pregunta…", color = StudyHubColors.TextTertiary, fontSize = 14.sp)
                        }
                        inner()
                    },
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(StudyHubColors.Primary, StudyHubColors.PrimaryDark),
                            ),
                        )
                        .clickable { send(input) },
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(Icons.AutoMirrored.Outlined.Send, tint = Color.White, size = 19.dp)
                }
            }
        }
    }
}

@Composable
private fun ProfessorSelector(
    teachers: List<TeacherProfile>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    val selected = teachers.firstOrNull { it.id == selectedId }
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Text("Elige tu profesor IA", color = StudyHubColors.TextTertiary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfessorChip(text = "Auto", selected = selectedId == null) { onSelect(null) }
            teachers.forEach { t ->
                ProfessorChip(text = t.name, selected = selectedId == t.id) { onSelect(t.id) }
            }
        }
        // Detalle del profesor seleccionado
        Spacer(Modifier.height(8.dp))
        if (selected != null) {
            GlassCard(radius = 14.dp, contentPadding = 12.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = selected.description.ifBlank { selected.name },
                    color = StudyHubColors.TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                )
                if (selected.subjects.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Materias: ${selected.subjects.joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } }}",
                        color = StudyHubColors.TextTertiary,
                        fontSize = 11.5.sp,
                    )
                }
                if (selected.teachingStyle.isNotBlank() || selected.difficultyLevel.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (selected.teachingStyle.isNotBlank()) {
                            Badge(styleName(selected.teachingStyle), StudyHubColors.PrimaryLight, small = true)
                        }
                        if (selected.difficultyLevel.isNotBlank()) {
                            Badge("Nivel ${selected.difficultyLevel.lowercase()}", StudyHubColors.AccentLight, small = true)
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Auto elige el profesor según el tema de tu pregunta.",
                color = StudyHubColors.TextTertiary,
                fontSize = 11.5.sp,
            )
        }
    }
}

/** Traduce el código de estilo de enseñanza a una etiqueta legible. */
private fun styleName(style: String): String = when (style.lowercase()) {
    "step_by_step" -> "Paso a paso"
    "socratic" -> "Socrático"
    "project_based" -> "Por proyectos"
    "analogies" -> "Con analogías"
    "concise" -> "Directo"
    "contextual" -> "Contextual"
    "explain_then_code" -> "Explica y codifica"
    "correction" -> "Corrección"
    "corrective" -> "Correctivo"
    "narrative" -> "Narrativo"
    "example_based" -> "Con ejemplos"
    "exam_focused" -> "Enfocado a exámenes"
    else -> style.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

@Composable
private fun ProfessorChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) StudyHubColors.Primary.copy(alpha = 0.18f) else StudyHubColors.Surface)
            .border(1.dp, if (selected) StudyHubColors.PrimaryLight else StudyHubColors.Border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = if (selected) StudyHubColors.PrimaryLight else StudyHubColors.TextSecondary,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun TutorTabs(current: TutorTab, onSelect: (TutorTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StudyHubColors.Surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TutorTab.entries.forEach { t ->
            val selected = t == current
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) StudyHubColors.Primary.copy(alpha = 0.18f) else Color.Transparent)
                    .clickable { onSelect(t) }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    icon = when (t) {
                        TutorTab.CHAT -> Icons.Outlined.ChatBubbleOutline
                        TutorTab.QUIZZES -> Icons.Outlined.Quiz
                        TutorTab.HISTORY -> Icons.Outlined.History
                    },
                    tint = if (selected) StudyHubColors.PrimaryLight else StudyHubColors.TextTertiary,
                    size = 16.dp,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when (t) {
                        TutorTab.CHAT -> "Chat"
                        TutorTab.QUIZZES -> "Quizzes"
                        TutorTab.HISTORY -> "Historial"
                    },
                    color = if (selected) StudyHubColors.PrimaryLight else StudyHubColors.TextTertiary,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    conversations: List<Conversation>,
    loading: Boolean,
    error: String?,
    onOpen: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit,
    onRetry: () -> Unit,
) {
    when {
        loading -> LoadingState()
        error != null -> ErrorState(error) { onRetry() }
        conversations.isEmpty() -> EmptyState("Aún no tienes historial. Chatea con el tutor y aquí podrás retomar tus conversaciones.")
        else -> Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            conversations.forEach { conv ->
                val date = formatDate(conv.lastMessageAt)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    radius = 16.dp,
                    contentPadding = 14.dp,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(StudyHubColors.Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(Icons.Outlined.History, tint = StudyHubColors.PrimaryLight, size = 18.dp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable { onOpen(conv) },
                        ) {
                            Text(
                                text = conv.title.ifBlank { "Conversación sin título" },
                                color = StudyHubColors.TextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = buildString {
                                    if (date.isNotBlank()) {
                                        append(date)
                                        append(" · ")
                                    }
                                    append("${conv.messageCount} mensajes")
                                },
                                color = StudyHubColors.TextTertiary,
                                fontSize = 11.5.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(StudyHubColors.Surface)
                                .clickable { onOpen(conv) }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                        ) {
                            Text("Abrir", color = StudyHubColors.PrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(StudyHubColors.Surface)
                                .clickable { onDelete(conv) },
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(Icons.Outlined.DeleteOutline, tint = StudyHubColors.DangerLight, size = 17.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatUiMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.78f else 0.92f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp,
                    ),
                )
                .background(
                    if (isUser) StudyHubColors.PrimaryDark
                    else StudyHubColors.Surface,
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
        ) {
            RichText(
                text = msg.content,
                color = if (isUser) Color.White else StudyHubColors.TextPrimary,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
            )
        }
    }
}
