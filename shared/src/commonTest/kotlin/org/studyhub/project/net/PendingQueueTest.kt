package org.studyhub.project.net

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PendingQueueTest {

    @Test
    fun pendingActionRoundTrip() {
        val json = Json { ignoreUnknownKeys = true }
        val list = listOf(
            PendingAction(method = "POST", url = "/subjects/1/tasks", body = """{"title":"Tarea offline"}"""),
            PendingAction(method = "DELETE", url = "/subjects/1/tasks/7"),
            PendingAction(method = "POST", url = "/subjects/1/tasks/7/toggle"),
        )
        val encoded = json.encodeToString(ListSerializer(PendingAction.serializer()), list)
        val decoded = json.decodeFromString<List<PendingAction>>(encoded)
        assertEquals(list, decoded)
    }

    @Test
    fun pendingActionDefaults() {
        val json = Json { ignoreUnknownKeys = true }
        val encoded = json.encodeToString(PendingAction.serializer(), PendingAction("POST", "/x"))
        val decoded = json.decodeFromString<PendingAction>(encoded)
        assertEquals("POST", decoded.method)
        assertEquals("/x", decoded.url)
        assertEquals(null, decoded.body)
    }
}
