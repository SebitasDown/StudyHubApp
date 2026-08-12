package org.studyhub.project.ui

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichTextTest {

    // ─── convertLatex ───────────────────────────────────────────────

    @Test
    fun stripsDollarDelimiters() {
        assertEquals("x²", convertLatex("\$x^2\$"))
        assertEquals("a/b", convertLatex("$$\\frac{a}{b}$$"))
    }

    @Test
    fun convertsSuperscriptsAndSubscripts() {
        assertEquals("x²", convertLatex("x^2"))
        assertEquals("x²ⁿ", convertLatex("x^{2n}"))
        assertEquals("x₁", convertLatex("x_1"))
        assertEquals("xᵢ", convertLatex("x_i"))
    }

    @Test
    fun convertsFractions() {
        assertEquals("1/2", convertLatex("\\frac{1}{2}"))
        assertEquals("x+1/x-1", convertLatex("\\dfrac{x+1}{x-1}"))
    }

    @Test
    fun convertsSqrtAndMathbb() {
        assertEquals("√x", convertLatex("\\sqrt{x}"))
        assertEquals("∛8", convertLatex("\\sqrt[3]{8}"))
        assertEquals("ℝ", convertLatex("\\mathbb{R}"))
    }

    @Test
    fun convertsSymbolsFromRealBackendOutput() {
        // Patrones vistos en quizzes reales del backend
        assertEquals("A ∪ B", convertLatex("A \\cup B"))
        assertEquals("x ∈ A", convertLatex("x \\in A"))
        assertEquals("f(x)=x²", convertLatex("f(x)=x^{2}"))
        assertEquals("a ≤ b", convertLatex("a \\leq b"))
        assertEquals("λ = 2", convertLatex("\\lambda = 2"))
        assertEquals("∑ᵢ₌₁ⁿ xᵢ", convertLatex("\\sum_{i=1}^{n} x_i"))
    }

    @Test
    fun leavesPlainTextUntouched() {
        assertEquals("¿Cuál es la derivada de f(x)?", convertLatex("¿Cuál es la derivada de f(x)?"))
    }

    // ─── renderRichText ─────────────────────────────────────────────

    @Test
    fun rendersBoldAndItalic() {
        val result = renderRichText("La **derivada** y *x*")
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        val italic = SpanStyle(fontStyle = FontStyle.Italic)
        assertTrue(result.spanStyles.any { it.item == bold && result.text.substring(it.start, it.end) == "derivada" })
        assertTrue(result.spanStyles.any { it.item == italic && result.text.substring(it.start, it.end) == "x" })
        assertEquals("La derivada y x", result.text)
    }

    @Test
    fun rendersLatexInsideBold() {
        val result = renderRichText("**f(x) = \$x^2\$**")
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        assertTrue(result.spanStyles.any { it.item == bold })
        assertEquals("f(x) = x²", result.text)
    }

    @Test
    fun rendersListsAndHeaders() {
        val result = renderRichText("Pasos:\n- Primero\n- Segundo")
        assertEquals("Pasos:\n• Primero\n• Segundo", result.text)
        assertEquals("Encabezado", renderRichText("# Encabezado").text)
    }

    @Test
    fun rendersRealChatMarkdown() {
        val chat = "**¡Hola!** 👋 ¿Qué tema te gustaría abordar hoy?\n\nPuedo ayudarte con:\n- Cálculo\n- Álgebra"
        val result = renderRichText(chat)
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        assertTrue(result.spanStyles.any { it.item == bold && result.text.substring(it.start, it.end) == "¡Hola!" })
        assertTrue(result.text.contains("• Cálculo"))
    }

    @Test
    fun rendersRealQuizExplanation() {
        val explanation = "La derivada de **\$x^2\$** es **\$2x\$** porque aplicamos la regla de la potencia: \$n \\cdot x^{n-1}\$."
        val result = renderRichText(explanation)
        assertEquals(
            "La derivada de x² es 2x porque aplicamos la regla de la potencia: n · xⁿ⁻¹.",
            result.text,
        )
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        val bolds = result.spanStyles.filter { it.item == bold }
        assertEquals(2, bolds.size)
    }

    // ─── Patrones reales de producción (quiz + chat del tutor) ───────

    @Test
    fun rendersRealQuizQuestionWithBoldAndLatex() {
        // Respuesta real del backend (POST /ai/resources/quiz)
        val question = "**Conceptual:** ¿Cuál es la derivada de \$\\sin(x)\$ respecto a \$x\$?"
        val result = renderRichText(question)
        assertEquals("Conceptual: ¿Cuál es la derivada de sin(x) respecto a x?", result.text)
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        assertTrue(result.spanStyles.any { it.item == bold && result.text.substring(it.start, it.end) == "Conceptual:" })
    }

    @Test
    fun rendersRealQuizChoices() {
        assertEquals("cos(x)", convertLatex("\$\\cos(x)\$"))
        assertEquals("x=π/4", convertLatex("\$x=\\frac{\\pi}{4}\$"))
        assertEquals("sec²(π/4)=2", convertLatex("\$\\sec^2(\\pi/4)=2\$"))
        assertEquals("sec(π/4)=√2", convertLatex("\$\\sec(\\pi/4)=\\sqrt{2}\$"))
    }

    @Test
    fun rendersRealChatMathDelimiters() {
        // Respuesta real del chat: \[ ... \] y \( ... \)
        assertEquals("f(x)=x² sin x", convertLatex("\\[f(x)=x^{2}\\,\\sin x\\]"))
        assertEquals("(u· v)' = u' v + u v'", convertLatex("\\((u\\cdot v)' = u'\\,v + u\\,v'\\)"))
        assertEquals("g'(x)=u'v+uv' = 1·sin x + x·cos x = sin x + x cos x.",
            convertLatex("\\[ g'(x)=u'v+uv' = 1\\cdot\\sin x + x\\cdot\\cos x = \\sin x + x\\cos x. \\]").trim())
    }

    @Test
    fun rendersBoxedAndNested() {
        // \boxed{...} con llaves anidadas (x^{2}) y \sqrt{x^{2}}
        assertEquals("f'(x)=2x sin x + x² cos x", convertLatex("\\boxed{\\,f'(x)=2x\\sin x + x^{2}\\cos x\\,}").trim())
        assertEquals("√x²", convertLatex("\\sqrt{x^{2}}"))
    }

    @Test
    fun rendersToAndLimits() {
        assertEquals("limₓ→₀", convertLatex("\\lim_{x \\to 0}"))
        assertEquals("x→∞", convertLatex("x \\to \\infty"))
        assertEquals("a ≥ b", convertLatex("a \\ge b"))
        assertEquals("C(n, k)", convertLatex("\\binom{n}{k}"))
        assertEquals("x ≡ y (mod n)", convertLatex("x \\equiv y \\pmod{n}"))
    }

    @Test
    fun rendersRealChatTablesAndRules() {
        val chat = "---\n\n| **1.1** | Identificar las dos funciones. |\n|--------|-------------------------------|\n| **1.2** | Recordar la regla del producto. |"
        val result = renderRichText(chat)
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        assertTrue(result.text.contains("1.1"))
        assertTrue(result.text.contains("Identificar las dos funciones."))
        assertTrue(result.text.contains("1.2"))
        assertTrue(!result.text.contains("---"))
        assertTrue(!result.text.contains("|"))
        assertTrue(result.spanStyles.any { it.item == bold && result.text.substring(it.start, it.end) == "1.1" })
    }

    @Test
    fun rendersHeadersBold() {
        val result = renderRichText("## Paso 1 – Divide el problema")
        val bold = SpanStyle(fontWeight = FontWeight.Bold)
        assertEquals("Paso 1 – Divide el problema", result.text)
        assertTrue(result.spanStyles.any { it.item == bold })
    }

    @Test
    fun normalizesNarrowSpaces() {
        assertEquals("sin x", convertLatex("sin\u202Fx"))
        assertEquals("Paso 1", convertLatex("Paso\u202F1"))
    }

    @Test
    fun convertsQuadsAndArrows() {
        // \quad/\qquad mal escapados en el código anterior → salían como "quadyquad"
        assertEquals("u(x) v(x)", convertLatex("u(x) \\quad v(x)"))
        assertEquals("u'(x)=2x, v'(x)=cos x", convertLatex("u'(x)=2x,\\qquad v'(x)=\\cos x"))
        assertEquals("v(x+h)→h→0 u'(x)", convertLatex("v(x+h) \\xrightarrow{h \\to 0} u'(x)"))
        assertEquals("a \n b", convertLatex("a \\\\[4pt] b"))
        assertEquals("limₕ→₀", convertLatex("\\lim_{h \\to 0}"))
    }
}
