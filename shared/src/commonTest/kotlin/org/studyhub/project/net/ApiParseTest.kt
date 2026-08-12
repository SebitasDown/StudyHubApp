package org.studyhub.project.net

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiParseTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun parsesResourceListWithNumericDifficulty() {
        // El backend manda difficulty como número (1) en la mayoría de los recursos
        val raw = """{"resources":[{"id":"abc","subject":"matemáticas","type":"QUIZ","title":"Quiz","difficulty":1,"completed":false,"content":{"type":"QUIZ","topic":"t","subject":"s","quiz":[]}}]}"""
        val parsed = json.decodeFromString<ResourceListResponse>(raw)
        assertEquals(1, parsed.resources.size)
        assertEquals("1", parsed.resources[0].difficulty)
        assertEquals("QUIZ", parsed.resources[0].type)
    }

    @Test
    fun parsesResourceListWithStringDifficulty() {
        val raw = """{"resources":[{"id":"abc","type":"QUIZ","title":"Quiz","difficulty":"INTERMEDIATE"}]}"""
        val parsed = json.decodeFromString<ResourceListResponse>(raw)
        assertEquals("INTERMEDIATE", parsed.resources[0].difficulty)
    }

    @Test
    fun parsesNotificationsPagination() {
        // El backend responde paginado, no un array plano
        val raw = """{"notifications":[{"id":1,"title":"Tarea","message":"m","type":"TASK","read":false,"createdAt":"2026-08-10T00:00:00.000Z"}],"total":1,"unreadCount":1,"page":1,"limit":50}"""
        val parsed = json.decodeFromString<NotificationsResponse>(raw)
        assertEquals(1, parsed.notifications.size)
        assertEquals("Tarea", parsed.notifications[0].title)
        assertEquals(1, parsed.unreadCount)
    }

    @Test
    fun parsesRiskWithNestedFactors() {
        // El backend manda reasons.factors como objetos {score, max} + summary
        val raw = """{"id":10,"score":69,"level":"MEDIUM","reasons":{"factors":{"roadmaps":{"max":15,"score":13,"progress":0.13},"engagement":{"max":10,"score":6},"confidenceIA":{"max":20,"score":20},"overdueTasks":{"max":25,"score":0},"knowledgeGaps":{"max":30,"score":30,"gapsCount":28}},"summary":["28 gap(s) activo(s)","confianza baja"]},"createdAt":"2026-08-09T15:42:17.275Z"}"""
        val parsed = json.decodeFromString<RiskAnalysis>(raw)
        assertEquals(69, parsed.score)
        assertEquals("MEDIUM", parsed.level)
        assertEquals(13, parsed.reasons?.factors?.roadmaps?.score)
        assertEquals(15, parsed.reasons?.factors?.roadmaps?.max)
        assertEquals(2, parsed.reasons?.summary?.size)
    }

    @Test
    fun parsesRiskWithLegacyArrayReasons() {
        // Filas antiguas del historial: reasons era un array de strings
        val raw = """[{"id":9,"score":65,"level":"MEDIUM","reasons":["20 gap(s) activo(s)","confianza baja en las materias"],"createdAt":"2026-08-01T00:00:00.000Z"}]"""
        val parsed = json.decodeFromString<List<RiskAnalysis>>(raw)
        assertEquals(1, parsed.size)
        assertEquals(2, parsed[0].reasons?.summary?.size)
        assertEquals("20 gap(s) activo(s)", parsed[0].reasons?.summary?.firstOrNull())
        assertEquals(null, parsed[0].reasons?.factors)
    }

    @Test
    fun parsesGoalsWrappedInObject() {
        // El backend responde { goals: [...] } con computedProgress.subject anidado
        val raw = """{"goals":[{"_id":"abc","title":"Problema de área bajo curva","status":"active","progress":13,"targetDate":null,"computedProgress":{"subject":"Calculo 1","mastery":0}}]}"""
        val parsed = json.decodeFromString<GoalsResponse>(raw)
        assertEquals(1, parsed.goals.size)
        assertEquals("active", parsed.goals[0].status)
        assertEquals("Calculo 1", parsed.goals[0].subjectLabel)
    }

    @Test
    fun parsesStudySessionPage() {
        // GET /study-timer/sessions ahora devuelve { sessions, total, page, limit }
        val raw = """{"sessions":[{"id":1,"completedAt":"2026-08-12T10:00:00.000Z","durationMinutes":25,"technique":"POMODORO_25_5","xpEarned":15}],"total":1,"page":1,"limit":20}"""
        val parsed = json.decodeFromString<StudySessionPage>(raw)
        assertEquals(1, parsed.sessions.size)
        assertEquals(1, parsed.total)
        assertEquals(25, parsed.sessions[0].durationMinutes)
    }

    @Test
    fun parsesStudySessionFlatArrayFallback() {
        // El backend desplegado aún manda array plano; debe tolerarse
        val raw = """[{"id":1,"completedAt":"2026-08-12T10:00:00.000Z","durationMinutes":25,"technique":"POMODORO_25_5","xpEarned":15}]
"""
        // parseSessionPage es privado; se verifica el fallback vía el formato tolerado
        // (aquí solo comprobamos que el array plano es decodificable como lista)
        val parsed = json.decodeFromString<List<StudySession>>(raw.trim())
        assertEquals(1, parsed.size)
        assertEquals(25, parsed[0].durationMinutes)
    }

    @Test
    fun parsesResourceDetailWithQuizContent() {
        // El detalle de un quiz: content.quiz con preguntas reales (LaTeX incluido)
        val raw = """{"resource":{"id":"abc","type":"QUIZ","difficulty":1,"completed":false,"content":{"type":"QUIZ","topic":"t","subject":"s","quiz":[{"question":"¿Cuál es la derivada de ${'$'}f(x)=x^2\\sin(x)${'$'}?","choices":["${'$'}2x\\cos(x)${'$'}","${'$'}2x\\sin(x)+x^2\\cos(x)${'$'}"],"answer":"B","explanation":"Regla del producto","difficulty":"easy"}]}}}"""
        val parsed = json.decodeFromString<ResourceResponse>(raw)
        val quiz = parsed.resource?.content?.quiz.orEmpty()
        assertEquals(1, quiz.size)
        assertEquals("B", quiz[0].answer)
        assertEquals("easy", quiz[0].difficulty)
    }
}
