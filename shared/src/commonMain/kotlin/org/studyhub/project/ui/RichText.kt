package org.studyhub.project.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Renderiza el contenido que genera el backend (quizzes y chat del tutor IA):
 * - LaTeX `$...$`, `\(...\)` y `\[...\]` → símbolos Unicode (∞, ∪, x², a/b, √x, →…)
 * - Markdown `**negritas**`, `*cursivas*`, listas `-`, encabezados `#`, tablas `|…|` y reglas `---`
 * Basado en el formato real que emite el modelo (Groq): delimitadores `\( \)`/`\[ \]`,
 * `\boxed{}`, `\begin{aligned}`, `\to`, `\displaystyle`, tablas markdown, etc.
 */

private val DOUBLE_STRUCK = mapOf(
    'R' to 'ℝ', 'N' to 'ℕ', 'Z' to 'ℤ', 'Q' to 'ℚ', 'C' to 'ℂ', 'P' to 'ℙ', 'H' to 'ℍ',
)

private val LATEX_COMMANDS = mapOf(
    "infty" to "∞", "cup" to "∪", "cap" to "∩", "setminus" to "∖",
    "subseteq" to "⊆", "subset" to "⊂", "supseteq" to "⊇", "supset" to "⊃",
    "in" to "∈", "notin" to "∉", "ni" to "∋", "notni" to "∌",
    "geq" to "≥", "geqslant" to "≥", "leq" to "≤", "leqslant" to "≤",
    "ge" to "≥", "le" to "≤", "neq" to "≠", "ne" to "≠",
    "approx" to "≈", "equiv" to "≡", "cong" to "≅", "sim" to "∼", "simeq" to "≃",
    "propto" to "∝", "pm" to "±", "mp" to "∓",
    "times" to "×", "cdot" to "·", "div" to "÷", "ast" to "∗", "bullet" to "•",
    "to" to "→", "rightarrow" to "→", "longrightarrow" to "→", "Rightarrow" to "⇒",
    "implies" to "⇒", "leftarrow" to "←", "longleftarrow" to "←", "Leftarrow" to "⇐",
    "leftrightarrow" to "↔", "longleftrightarrow" to "↔", "Leftrightarrow" to "⇔",
    "iff" to "⇔", "mapsto" to "↦", "uparrow" to "↑", "downarrow" to "↓",
    "sum" to "∑", "prod" to "∏", "coprod" to "∐", "int" to "∫", "iint" to "∬",
    "iiint" to "∭", "oint" to "∮", "bigcup" to "⋃", "bigcap" to "⋂",
    "lim" to "lim", "limsup" to "lim sup", "liminf" to "lim inf", "sup" to "sup", "inf" to "inf",
    "max" to "max", "min" to "min", "gcd" to "gcd", "det" to "det", "arg" to "arg",
    "dim" to "dim", "ker" to "ker", "deg" to "deg", "exp" to "exp", "log" to "log",
    "ln" to "ln", "lg" to "lg", "sin" to "sin", "cos" to "cos", "tan" to "tan",
    "sec" to "sec", "csc" to "csc", "cot" to "cot", "arcsin" to "arcsin",
    "arccos" to "arccos", "arctan" to "arctan", "sinh" to "sinh", "cosh" to "cosh",
    "tanh" to "tanh", "coth" to "coth",
    "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ", "epsilon" to "ε",
    "varepsilon" to "ε", "zeta" to "ζ", "eta" to "η", "theta" to "θ", "vartheta" to "ϑ",
    "iota" to "ι", "kappa" to "κ", "varkappa" to "ϰ", "lambda" to "λ", "mu" to "μ",
    "nu" to "ν", "xi" to "ξ", "omicron" to "ο", "pi" to "π", "varpi" to "ϖ",
    "rho" to "ρ", "varrho" to "ϱ", "sigma" to "σ", "varsigma" to "ς", "tau" to "τ",
    "upsilon" to "υ", "phi" to "φ", "varphi" to "φ", "chi" to "χ", "psi" to "ψ",
    "omega" to "ω", "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ",
    "Xi" to "Ξ", "Pi" to "Π", "Sigma" to "Σ", "Upsilon" to "Υ", "Phi" to "Φ",
    "Psi" to "Ψ", "Omega" to "Ω", "digamma" to "ϝ",
    "degree" to "°", "circ" to "°", "angle" to "∠", "measuredangle" to "∡",
    "emptyset" to "∅", "varnothing" to "∅", "partial" to "∂", "nabla" to "∇",
    "forall" to "∀", "exists" to "∃", "nexists" to "∄", "neg" to "¬", "lnot" to "¬",
    "land" to "∧", "wedge" to "∧", "lor" to "∨", "vee" to "∨", "oplus" to "⊕",
    "ominus" to "⊖", "otimes" to "⊗", "odot" to "⊙", "triangle" to "△",
    "triangleq" to "≜", "therefore" to "∴", "because" to "∵",
    "dots" to "…", "ldots" to "…", "cdots" to "⋯", "vdots" to "⋮", "ddots" to "⋱",
    "prime" to "′", "doubleprime" to "″", "star" to "⋆", "bigstar" to "★",
    "dagger" to "†", "ddagger" to "‡", "ell" to "ℓ", "hbar" to "ℏ", "hslash" to "ℏ",
    "Re" to "ℜ", "Im" to "ℑ", "aleph" to "ℵ", "beth" to "ℶ", "top" to "⊤", "bot" to "⊥",
    "perp" to "⊥", "parallel" to "∥", "nparallel" to "∦", "lfloor" to "⌊",
    "rfloor" to "⌋", "lceil" to "⌈", "rceil" to "⌉", "langle" to "⟨",
    "rangle" to "⟩", "vert" to "|", "Vert" to "‖", "aligned" to "",
)

private val SUP = mapOf(
    '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴', '5' to '⁵',
    '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
    'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'e' to 'ᵉ', 'a' to 'ᵃ', 'm' to 'ᵐ',
    'p' to 'ᵖ', 't' to 'ᵗ', 'u' to 'ᵘ', 'v' to 'ᵛ', 'w' to 'ʷ', 'y' to 'ʸ', 'z' to 'ᶻ',
    '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
)

private val SUB = mapOf(
    '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄', '5' to '₅',
    '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
    'n' to 'ₙ', 'i' to 'ᵢ', 'x' to 'ₓ', 'e' to 'ₑ', 'a' to 'ₐ', 'o' to 'ₒ',
    'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ', 'v' to 'ᵥ', 'h' to 'ₕ', 'k' to 'ₖ',
    '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
)

private fun supChar(c: Char): String = SUP[c]?.toString() ?: c.toString()

private fun subChar(c: Char): String = SUB[c]?.toString() ?: c.toString()

/**
 * Convierte comandos LaTeX a símbolos Unicode. El orden importa: primero se quitan
 * los delimitadores/entornos, luego se convierten las construcciones con argumentos
 * (fracciones, raíces, acentos) y al final los comandos simples y super/subíndices.
 */
/**
 * Reemplaza \cmd{...} con llaves balanceadas (soporta contenido anidado como x^{2})
 * aplicando [transform] al contenido. [index] es el opcional [n] (ej. \\sqrt[3]{...}).
 */
private fun unwrapCommands(s: String, transforms: Map<String, (inner: String, index: String) -> String>): String {
    val out = StringBuilder()
    var i = 0
    val nameRegex = Regex("[a-zA-Z]+")
    while (i < s.length) {
        if (s[i] == '\\') {
            val m = nameRegex.find(s, i + 1)
            if (m != null) {
                val transform = transforms[m.value]
                if (transform != null) {
                    var j = m.range.last + 1
                    var index = ""
                    if (j < s.length && s[j] == '[') {
                        val close = s.indexOf(']', j)
                        if (close > j) {
                            index = s.substring(j + 1, close)
                            j = close + 1
                        }
                    }
                    if (j < s.length && s[j] == '{') {
                        var depth = 1
                        var k = j + 1
                        while (k < s.length && depth > 0) {
                            when (s[k]) {
                                '{' -> depth++
                                '}' -> depth--
                            }
                            k++
                        }
                        if (depth == 0) {
                            val inner = s.substring(j + 1, k - 1)
                            out.append(transform(inner, index))
                            i = k
                            continue
                        }
                    }
                }
            }
        }
        out.append(s[i])
        i++
    }
    return out.toString()
}

/** Comandos que envuelven contenido y se reducen a él (\boxed{x} → x, \text{a} → a…). */
private val UNWRAP_IDENTITY: Map<String, (String, String) -> String> = mapOf(
    "boxed" to { inner: String, _: String -> inner },
    "text" to { inner: String, _: String -> inner },
    "mathbf" to { inner: String, _: String -> inner },
    "boldsymbol" to { inner: String, _: String -> inner },
    "mathit" to { inner: String, _: String -> inner },
    "mathsf" to { inner: String, _: String -> inner },
    "mathcal" to { inner: String, _: String -> inner },
    "mathrm" to { inner: String, _: String -> inner },
    "operatorname" to { inner: String, _: String -> inner },
    "mbox" to { inner: String, _: String -> inner },
    "textbf" to { inner: String, _: String -> inner },
    "textit" to { inner: String, _: String -> inner },
    "emph" to { inner: String, _: String -> inner },
    "textrm" to { inner: String, _: String -> inner },
    "mathtt" to { inner: String, _: String -> inner },
    "mathscr" to { inner: String, _: String -> inner },
    "mathfrak" to { inner: String, _: String -> inner },
    "bm" to { inner: String, _: String -> inner },
    "pmb" to { inner: String, _: String -> inner },
)

/** Convierte comandos LaTeX a símbolos Unicode. El orden importa: primero se quitan
 *  los delimitadores/entornos, luego las construcciones con argumentos y al final
 *  los comandos simples y super/subíndices. */
fun convertLatex(input: String): String {
    var s = input
    // La IA a veces emite espacios estrechos \u202F / \u00A0 (ej. "Paso 1", "sin x")
    s = s.replace("\u202F", " ").replace("\u00A0", " ")

    // Delimitadores de matemáticas: \[ \] \( \) (los $ se limpian al final).
    // El lookbehind evita comerse el "[" de un salto de línea "\\[4pt]" (doble backslash).
    s = s.replace(Regex("(?<!\\\\)\\\\[\\[\\]()]"), "")

    // Entornos \begin{...}/\end{...} (aligned, cases, matrix…) y el espec. de columnas de array
    s = s.replace(Regex("\\\\begin\\{array\\}\\{[^}]*\\}"), " ")
    s = s.replace(Regex("\\\\begin\\{[^}]*\\}"), " ")
    s = s.replace(Regex("\\\\end\\{[^}]*\\}"), " ")

    // \boxed{...}, \mathbf{...}, \text{...}, \sqrt{...} → contenido (con llaves balanceadas).
    // Iterativo: soporta anidamiento (\boxed{\text{...}}, \sqrt{x^{2}}).
    repeat(3) {
        val before = s
        s = unwrapCommands(s, UNWRAP_IDENTITY + mapOf(
            "sqrt" to { inner: String, index: String ->
                when (index) {
                    "3" -> "∛$inner"
                    "4" -> "∜$inner"
                    else -> "√$inner"
                }
            },
        ))
        if (s == before) return@repeat
    }
    // \mathbb{R} → ℝ
    s = s.replace(Regex("\\\\mathbb\\{([A-Za-z])\\}")) { m ->
        DOUBLE_STRUCK[m.groupValues[1][0]]?.toString() ?: m.groupValues[1]
    }
    // Acentos/vectores: \vec{F} → F⃗, \hat{x} → x̂, \bar{x} → x̄…
    s = s.replace(
        Regex("\\\\(overline|bar|vec|hat|widehat|tilde|widetilde|dot|ddot|check|breve|acute|grave|mathring|overrightarrow|overleftrightarrow)\\{([^{}]*)\\}"),
    ) { m ->
        when (m.groupValues[1]) {
            "vec", "overrightarrow" -> m.groupValues[2] + "\u20D7"
            "overleftrightarrow" -> m.groupValues[2] + "\u2194"
            "hat", "widehat" -> m.groupValues[2] + "\u0302"
            "tilde", "widetilde" -> m.groupValues[2] + "\u0303"
            "dot" -> m.groupValues[2] + "\u0307"
            "ddot" -> m.groupValues[2] + "\u0308"
            "check" -> m.groupValues[2] + "\u030C"
            "breve" -> m.groupValues[2] + "\u0306"
            "acute" -> m.groupValues[2] + "\u0301"
            "grave" -> m.groupValues[2] + "\u0300"
            "mathring" -> m.groupValues[2] + "\u030A"
            else -> m.groupValues[2] + "\u0304" // bar, overline
        }
    }
    // Combinaciones: \binom{n}{k} → C(n, k)
    s = s.replace(Regex("\\\\(binom|dbinom|tbinom)\\{([^{}]*)\\}\\{([^{}]*)\\}")) { m ->
        "C(${m.groupValues[2]}, ${m.groupValues[3]})"
    }
    // \pmod{n} → (mod n); \bmod/\mod → mod
    s = s.replace(Regex("\\\\pmod\\{([^{}]*)\\}")) { m -> "(mod ${m.groupValues[1]})" }
    s = s.replace(Regex("\\\\bmod"), " mod ")
    s = s.replace(Regex("\\\\mod"), " mod ")
    // Flechas con texto debajo: \xrightarrow{h \to 0} → → h→0
    s = s.replace(Regex("\\\\xrightarrow\\{([^{}]*)\\}")) { m -> "→ ${m.groupValues[1]}" }
    s = s.replace(Regex("\\\\xrightarrow|\\\\xleftarrow"), "→")

    // Fracciones (iterativo para soportar anidadas): \frac{a}{b} → a/b
    repeat(5) {
        val before = s
        s = s.replace(Regex("\\\\d?frac\\{([^{}]*)\\}\\{([^{}]*)\\}")) { m ->
            "${m.groupValues[1]}/${m.groupValues[2]}"
        }
        if (s == before) return@repeat
    }

    // Separadores y modificadores sin contenido: \left( \right) \big[ \displaystyle…
    s = s.replace(
        Regex("\\\\left|\\\\right|\\\\middle|\\\\big[lr]?|\\\\Big[lr]?|\\\\bigg[lr]?|\\\\Bigg[lr]?|\\\\displaystyle|\\\\limits|\\\\nolimits|\\\\color\\{[^}]*\\}"),
    ) { "" }
    // Espacios LaTeX
    s = s.replace("\\qquad", "  ").replace("\\quad", " ")
    s = s.replace("\\,", " ").replace("\\;", " ").replace("\\:", " ").replace("\\!", "")
    // Salto de línea dentro de display math: \\ → \n (y el ajuste [4pt] que a veces le sigue)
    s = s.replace("\\\\", "\n")
    s = s.replace(Regex("\\[[0-9.]+(pt|ex|em|mm|cm)\\]"), "")

    // Comandos simples (uno o más): \sin → sin, \pi → π, \to → →
    s = s.replace(Regex("\\\\[a-zA-Z]+")) { m ->
        LATEX_COMMANDS[m.groupValues[0].drop(1)] ?: m.groupValues[0].drop(1)
    }

    // Superíndices: x^2 → x², x^{2n} → x²ⁿ
    s = s.replace(Regex("\\^\\{([^}]*)\\}")) { m ->
        m.groupValues[1].map { supChar(it) }.joinToString("")
    }
    s = s.replace(Regex("\\^([0-9a-zA-Z+\\-()=])")) { m -> supChar(m.groupValues[1][0]) }
    // Subíndices: x_1 → x₁, x_{i} → xᵢ
    s = s.replace(Regex("_\\{([^}]*)\\}")) { m ->
        m.groupValues[1].map { subChar(it) }.joinToString("")
    }
    s = s.replace(Regex("_([0-9a-zA-Z+\\-()=])")) { m -> subChar(m.groupValues[1][0]) }

    // Caracteres escapados
    s = s.replace("\\{", "{").replace("\\}", "}")
    s = s.replace("\\%", "%").replace("\\&", "&").replace("\\#", "#").replace("\\_", "_")
    // Alineación de entornos (aligned, cases…)
    s = s.replace("&", " ")
    // Restos de llaves y delimitadores de matemáticas
    s = s.replace("{", "").replace("}", "").replace("$", "")
    // Espacio antes de funciones pegadas a una variable/resultado: 2x\sin x → "2x sin x"
    s = s.replace(Regex("(\\S)(sin|cos|tan|sec|csc|cot|log|ln|lg|exp|lim|max|min|gcd|det|arg|dim|ker|sinh|cosh|tanh|arcsin|arccos|arctan|sup|inf)\\b")) { m ->
        val prev = m.groupValues[1][0]
        if (prev.isLetter() || prev.isDigit() || prev in SUPER_CHARS || prev == ')') {
            "${m.groupValues[1]} ${m.groupValues[2]}"
        } else {
            m.groupValues[0]
        }
    }
    // Colapsar espacios duplicados (pmod, \qquad, …) sin tocar saltos de línea
    s = s.replace(Regex(" {2,}"), " ")
    // LaTeX ignora los espacios alrededor de → (h \to 0 → h→0, como en los límites)
    s = s.replace(Regex("[ ]*→[ ]*"), "→")
    return s
}

private val INLINE_PATTERN = Regex("\\*\\*[^*]+?\\*\\*|__[^_]+?__|\\*[^*]+?\\*")

private val HORIZONTAL_RULE = Regex("^[-*_ ]{3,}$")

/** Superíndices comunes que pueden quedar pegados a una función (x²cos x). */
private const val SUPER_CHARS = "²³¹⁰⁹⁺⁻ⁿˣᵉᵃᵐᵖᵗᵘᵛʷʸᶻ⁼⁽⁾ⁱ"

private val TABLE_SEPARATOR = Regex("^:?-{2,}:?$")

/** Convierte markdown + LaTeX en un AnnotatedString con negritas, cursivas y encabezados reales. */
fun renderRichText(markdown: String): AnnotatedString {
    val text = convertLatex(markdown)
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { i, rawLine ->
            if (i > 0) append("\n")
            val line = rawLine.trimEnd()
            val trimmed = line.trim()

            // Regla horizontal: --- o *** sola en la línea
            if (trimmed.length >= 3 && HORIZONTAL_RULE.matches(trimmed)) return@forEachIndexed

            // Tabla markdown: | celda | celda | (la fila separadora se descarta)
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                val cells = trimmed.trim('|').split("|").map { it.trim() }
                if (cells.all { it.isEmpty() || TABLE_SEPARATOR.matches(it) }) return@forEachIndexed
                cells.forEachIndexed { cIdx, cell ->
                    if (cIdx > 0) append("  ·  ")
                    appendInline(cell)
                }
                return@forEachIndexed
            }

            // Lista markdown
            val listMatch = Regex("^\\s*[-*]\\s+").find(line)
            if (listMatch != null) {
                append("• ")
                appendInline(line.substring(listMatch.range.last + 1))
                return@forEachIndexed
            }

            // Encabezado markdown (# …) → en negrita
            val headMatch = Regex("^#{1,6}\\s+").find(line)
            if (headMatch != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInline(line.substring(headMatch.range.last + 1))
                }
                return@forEachIndexed
            }

            appendInline(line)
        }
    }
}

private fun AnnotatedString.Builder.appendInline(text: String) {
    var rest = text
    while (rest.isNotEmpty()) {
        val match = INLINE_PATTERN.find(rest)
        if (match == null) {
            append(rest)
            break
        }
        if (match.range.first > 0) append(rest.substring(0, match.range.first))
        val token = match.value
        when {
            token.startsWith("**") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(token.substring(2, token.length - 2))
            }
            token.startsWith("__") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(token.substring(2, token.length - 2))
            }
            else -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(token.substring(1, token.length - 1))
            }
        }
        rest = rest.substring(match.range.last + 1)
    }
}

/** Texto con soporte de markdown y LaTeX, manteniendo los estilos base. */
@Composable
fun RichText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = renderRichText(text),
        color = color,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        maxLines = maxLines,
    )
}
