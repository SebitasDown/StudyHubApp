package org.studyhub.project.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.studyhub.project.StudyHubColors
import org.studyhub.project.net.SubjectTask

class TaskUrgencyTest {

    private fun task(id: Int, dueDate: String, status: String = "PENDING") =
        SubjectTask(id = id, title = "Tarea $id", dueDate = dueDate, status = status)

    @Test
    fun pendingTasksComeBeforeCompleted() {
        // Tarea lejana pendiente vs. tarea completada reciente: la pendiente va primero
        val tasks = listOf(
            task(1, "2099-01-01T00:00:00.000Z", "COMPLETED"),
            task(2, "2099-06-01T00:00:00.000Z"),
        )
        val sorted = sortTasksByUrgency(tasks)
        assertEquals(listOf(2, 1), sorted.map { it.id })
    }

    @Test
    fun mostUrgentFirst() {
        val tasks = listOf(
            task(1, "2099-06-01T00:00:00.000Z"), // menos urgente
            task(2, "2020-01-01T00:00:00.000Z"), // atrasada → la más urgente
            task(3, "2099-01-01T00:00:00.000Z"), // la próxima más cercana
        )
        val sorted = sortTasksByUrgency(tasks)
        assertEquals(listOf(2, 3, 1), sorted.map { it.id })
    }

    @Test
    fun tasksWithoutDateGoLast() {
        val tasks = listOf(
            task(1, ""),
            task(2, "2099-01-01T00:00:00.000Z"),
        )
        val sorted = sortTasksByUrgency(tasks)
        assertEquals(listOf(2, 1), sorted.map { it.id })
    }

    @Test
    fun overdueStatusComesFirstEvenWithLaterDate() {
        // OVERDUE explícito gana aunque otra pendiente venza antes
        val tasks = listOf(
            task(1, "2099-06-01T00:00:00.000Z"),
            task(2, "2099-01-01T00:00:00.000Z", "OVERDUE"),
        )
        val sorted = sortTasksByUrgency(tasks)
        assertEquals(listOf(2, 1), sorted.map { it.id })
    }

    @Test
    fun overdueDetection() {
        assertEquals(true, isOverdue(task(1, "2020-01-01T00:00:00.000Z")))
        assertEquals(true, isOverdue(task(2, "2099-01-01T00:00:00.000Z", "OVERDUE")))
        assertEquals(false, isOverdue(task(3, "2099-01-01T00:00:00.000Z")))
        assertEquals(false, isOverdue(task(4, "")))
    }

    @Test
    fun urgencyBarColors() {
        // Atrasada (por fecha o por estado OVERDUE) → rojo
        assertEquals(StudyHubColors.Danger, urgencyBarColor(task(1, "2020-01-01T00:00:00.000Z")))
        assertEquals(StudyHubColors.Danger, urgencyBarColor(task(2, "2099-01-01T00:00:00.000Z", "OVERDUE")))
        // Con mucha holgura → verde
        assertEquals(StudyHubColors.Secondary, urgencyBarColor(task(3, "2099-01-01T00:00:00.000Z")))
        // Sin fecha o completada → sin barra
        assertNull(urgencyBarColor(task(4, "")))
        assertNull(urgencyBarColor(task(5, "2020-01-01T00:00:00.000Z", "COMPLETED")))
    }
}
