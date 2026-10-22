// port-lint: tests src/categorise.rs

package ai.solace.tui.cansi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CategoriseTest {
    @Test
    fun catAndMatchesLen() {
        val txt1 = "hello"
        val matches1 = parse(txt1)
        val cat1 = categoriseText(txt1)
        assertTrue(matches1.size + 1 >= cat1.size)

        val txt2 = "\u001b[92mhello\u001b[0m"
        val matches2 = parse(txt2)
        val cat2 = categoriseText(txt2)
        assertTrue(matches2.size + 1 >= cat2.size)

        val txt3 = "\u001b[92mhello\u001b[0m\u001b[31mworld\u001b[0mwhatever"
        val matches3 = parse(txt3)
        val cat3 = categoriseText(txt3)
        assertTrue(matches3.size + 1 >= cat3.size)
    }

    @Test
    fun malformedEscapes() {
        val x = categoriseTextV3("oops\u001b[\n")
        assertEquals(
            listOf(
                CategorisedSlice(
                    text = "oops\u001b[\n",
                    start = 0,
                    end = 7,
                    fg = null,
                    bg = null,
                    blink = null,
                    underline = null,
                    hidden = null,
                    intensity = null,
                    italic = null,
                    reversed = null,
                    strikethrough = null,
                ),
            ),
            x,
        )
    }
}
