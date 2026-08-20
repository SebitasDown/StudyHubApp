package org.studyhub.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import org.studyhub.project.net.QuizQuestion
import org.studyhub.project.ui.icons.AppIcon

/** Item jugable genérico: quiz de recurso IA o práctica de un paso de roadmap. */
data class QuizItem(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val topic: String = "",
)

/**
 * Jugador de quiz reutilizable: pregunta por pregunta con barra de progreso,
 * feedback con explicación y pantalla de resultado con anillo de puntaje.
 * El contenido extra de la pantalla de resultado (guardar/completar) va en [resultAction].
 */
@Composable
fun QuizPlayer(
    title: String,
    items: List<QuizItem>,
    onBack: () -> Unit,
    resultMessage: (passed: Boolean) -> String,
    resultAction: @Composable (correct: Int, total: Int) -> Unit,
    onDone: () -> Unit,
) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var showExplainDialog by remember { mutableStateOf(false) }
    var explainText by remember { mutableStateOf("") }
    var explaining by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        FullScreenBackBar(
            title = title,
            subtitle = when {
                items.isEmpty() -> null
                finished -> "Resultado"
                else -> "Pregunta ${index + 1} de ${items.size}"
            },
            onBack = onBack,
        )
        Box(Modifier.weight(1f)) {
            if (items.isEmpty()) {
                ErrorState("Este contenido no tiene preguntas jugables.")
            } else if (finished) {
                ResultView(
                    correct = correctCount,
                    total = items.size,
                    resultMessage = resultMessage,
                    action = resultAction,
                )
            } else {
                items.getOrNull(index)?.let { q ->
                    QuestionView(
                        question = q,
                        number = index + 1,
                        total = items.size,
                        selected = selected,
                        answered = answered,
                        explaining = explaining,
                        onSelect = { i ->
                            if (!answered) {
                                selected = i
                                answered = true
                                if (i == q.correctIndex) correctCount++
                            }
                        },
                        onNext = {
                            if (index + 1 < items.size) {
                                index++
                                selected = -1
                                answered = false
                            } else {
                                finished = true
                            }
                        },
                        onExplain = {
                            explaining = true
                            explainText = ""
                            scope.launch {
                                try {
                                    val correct = if (q.correctIndex in q.options.indices) q.options[q.correctIndex] else ""
                                    explainText = Api.client.explainAnswer(
                                        question = q.question,
                                        choices = q.options,
                                        correctAnswer = correct,
                                        topic = q.topic,
                                    )
                                } catch (_: Exception) {
                                    // Si el endpoint IA no está deployado, usar la explicación estática del quiz
                                    explainText = q.explanation.ifBlank {
                                        "La respuesta correcta es: **${if (q.correctIndex in q.options.indices) q.options[q.correctIndex] else "N/A"}**. " +
                                        "La explicación detallada estará disponible cuando el backend se actualice."
                                    }
                                }
                                explaining = false
                                showExplainDialog = true
                            }
                        },
                    )
                }
            }
        }

        if (showExplainDialog) {
            AlertDialog(
                onDismissRequest = { showExplainDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(Icons.Outlined.Info, tint = StudyHubColors.Primary, size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Explicación IA", color = StudyHubColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    }
                },
                text = {
                    Box(
                        Modifier.fillMaxWidth().heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                    ) {
                        RichText(
                            text = explainText,
                            color = StudyHubColors.TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExplainDialog = false }) {
                        Text("Entendido", color = StudyHubColors.Primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = StudyHubColors.Surface,
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun QuestionView(
    question: QuizItem,
    number: Int,
    total: Int,
    selected: Int,
    answered: Boolean,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onExplain: () -> Unit = {},
    explaining: Boolean = false,
) {
    val isCorrect = selected == question.correctIndex

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        // Barra de progreso
        GlassProgress(
            progress = (number - 1 + if (answered) 1 else 0) / total.toFloat(),
            color = StudyHubColors.Primary,
            height = 6.dp,
        )
        Spacer(Modifier.height(16.dp))

        // Pregunta
        GlassCard(radius = 20.dp, contentPadding = 18.dp) {
            RichText(
                text = question.question,
                color = StudyHubColors.TextPrimary,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))

        // Opciones
        question.options.forEachIndexed { i, choice ->
            val isSelected = i == selected
            val isAnswer = i == question.correctIndex
            val background: Color
            val borderColor: Color
            val textColor: Color
            when {
                !answered -> {
                    background = StudyHubColors.Surface
                    borderColor = StudyHubColors.Border
                    textColor = StudyHubColors.TextPrimary
                }
                isAnswer -> {
                    background = StudyHubColors.Secondary.copy(alpha = 0.15f)
                    borderColor = StudyHubColors.SecondaryLight
                    textColor = StudyHubColors.TextPrimary
                }
                isSelected -> {
                    background = StudyHubColors.Danger.copy(alpha = 0.12f)
                    borderColor = StudyHubColors.DangerLight
                    textColor = StudyHubColors.TextPrimary
                }
                else -> {
                    background = StudyHubColors.Surface
                    borderColor = StudyHubColors.Border
                    textColor = StudyHubColors.TextTertiary
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(background)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable(enabled = !answered) { onSelect(i) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (answered && isAnswer) StudyHubColors.Secondary else StudyHubColors.SurfaceLight),
                    contentAlignment = Alignment.Center,
                ) {
                    if (answered && isAnswer) {
                        AppIcon(Icons.Outlined.CheckCircle, tint = Color.White, size = 15.dp)
                    } else {
                        Text(
                            text = ('A' + i).toString(),
                            color = if (answered && isSelected) StudyHubColors.DangerLight else StudyHubColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                RichText(
                    text = choice,
                    color = textColor,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = if (answered && isAnswer) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Feedback
        if (answered) {
            GlassCard(
                radius = 16.dp,
                contentPadding = 14.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        icon = if (isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Close,
                        tint = if (isCorrect) StudyHubColors.SecondaryLight else StudyHubColors.DangerLight,
                        size = 20.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isCorrect) "¡Correcto!" else "Incorrecto",
                        color = if (isCorrect) StudyHubColors.SecondaryLight else StudyHubColors.DangerLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                if (question.explanation.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    RichText(
                        text = question.explanation,
                        color = StudyHubColors.TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudyHubColors.Primary.copy(alpha = 0.08f))
                        .clickable(enabled = !explaining) { onExplain() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (explaining) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = StudyHubColors.Primary,
                        )
                    } else {
                        AppIcon(Icons.Outlined.Info, tint = StudyHubColors.Primary, size = 18.dp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (explaining) "IA explicando..." else "¿Por qué?",
                        color = StudyHubColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            PrimaryButton(
                text = if (number < total) "Siguiente" else "Ver resultado",
                icon = Icons.Outlined.Quiz,
            ) { onNext() }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ResultView(
    correct: Int,
    total: Int,
    resultMessage: (passed: Boolean) -> String,
    action: @Composable (correct: Int, total: Int) -> Unit,
) {
    val pct = if (total > 0) (correct * 100) / total else 0
    val passed = pct >= 60
    val color = if (passed) StudyHubColors.SecondaryLight else StudyHubColors.AccentLight

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(30.dp))
        ScoreRing(score = pct, label = "aciertos", color = color, size = 132.dp)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "$correct de $total respuestas correctas",
            color = StudyHubColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = resultMessage(passed),
            color = StudyHubColors.TextSecondary,
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(24.dp))
        action(correct, total)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun QuizPlayScreen(
    resourceId: String,
    title: String,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
) {
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val detail = Api.client.aiResourceDetail(resourceId)
            val quiz = detail.content?.quiz ?: emptyList()
            if (quiz.isEmpty()) {
                error = "Este recurso no tiene preguntas jugables."
            } else {
                questions = quiz
            }
        } catch (e: Exception) {
            error = e.message ?: "No se pudo cargar el quiz"
        } finally {
            loading = false
        }
    }

    val barTitle = "Quiz: ${title.ifBlank { "Práctica" }}"

    if (loading || error != null) {
        Column(Modifier.fillMaxSize()) {
            FullScreenBackBar(title = barTitle, onBack = onBack)
            Box(Modifier.weight(1f)) {
                if (loading) LoadingState() else ErrorState(error.orEmpty())
            }
        }
        return
    }

    QuizPlayer(
        title = barTitle,
        items = questions.map { q ->
            QuizItem(
                question = q.question,
                options = q.choices,
                correctIndex = q.choices.indexOf(q.answer).coerceAtLeast(0),
                explanation = q.explanation,
                topic = title,
            )
        },
        onBack = onBack,
        resultMessage = { passed ->
            if (passed) "¡Aprobaste! Tu brecha de conocimiento se reforzó."
            else "Sigue practicando: revisa el tema y vuelve a intentarlo."
        },
        resultAction = { correct, total ->
            if (!saved) {
                PrimaryButton(
                    text = "Marcar como completado",
                    icon = Icons.Outlined.Save,
                    loading = saving,
                    enabled = !saving,
                ) {
                    saving = true
                    scope.launch {
                        runCatching { Api.client.completeResource(resourceId, correct, total) }
                        saving = false
                        saved = true
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Esto registra tu resultado en el backend y cierra la brecha",
                    color = StudyHubColors.TextTertiary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            } else {
                GlassCard(radius = 16.dp, contentPadding = 14.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(Icons.Outlined.CheckCircle, tint = StudyHubColors.SecondaryLight, size = 20.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Resultado guardado correctamente",
                            color = StudyHubColors.SecondaryLight,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(text = "Volver a quizzes", icon = Icons.Outlined.Quiz) { onCompleted() }
            }
        },
        onDone = onCompleted,
    )
}
