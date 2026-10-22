// port-lint: tests src/parsing.rs

package ai.solace.tui.cansi

import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingTest {
    @Test
    fun parseTest() {
        val ansiText = "Hello, \u001b[31;4mworld\u001b[0m!"
        val parsed = parse(ansiText)
        assertEquals(
            listOf(
                Match(start = 7, end = 14, text = "\u001b[31;4m"),
                Match(start = 19, end = 23, text = "\u001b[0m"),
            ),
            parsed,
        )
    }

    @Test
    fun parseStringWithDifferentChars() {
        val t = "👋, \u001b[31;4m🌍\u001b[0m!"
        val parsed = parse(t)
        assertEquals(
            listOf(
                Match(start = 6, end = 13, text = "\u001b[31;4m"),
                Match(start = 17, end = 21, text = "\u001b[0m"),
            ),
            parsed,
        )
    }

    @Test
    fun malformedEscape() {
        val x = parse("oops\u001b[\n")
        assertEquals(emptyList(), x)
    }
}
