// port-lint: source src/categorise.rs

package ai.solace.tui.cansi

private const val SEPARATOR = ';'

/**
 * Produces an [Sgr] from a styling sequence match.
 */
internal fun handleSeq(m: Match): Sgr {
    // The slice we want to process skips first two bytes (ESC[) and last byte (terminating byte)
    val slice = m.text.substring(2, m.text.length - 1)
    return slice.split(SEPARATOR).fold(Sgr()) { sgr, seq -> adjustSgr(sgr, seq) }
}

/**
 * Applies the style sequence to the SGR. Maps decimal numbers according to
 * the ECMA-48 SGR parameter specification.
 */
internal fun adjustSgr(sgr: Sgr, seq: String): Sgr {
    when (seq) {
        // Reset
        "0" -> return Sgr()

        "1" -> sgr.intensity = Intensity.Bold

        "2" -> sgr.intensity = Intensity.Faint

        "3" -> sgr.italic = true

        "4" -> sgr.underline = true

        "5" -> sgr.blink = true

        "7" -> sgr.reversed = true

        "8" -> sgr.hidden = true

        "9" -> sgr.strikethrough = true

        "22" -> sgr.intensity = Intensity.Normal

        "23" -> sgr.italic = false

        "24" -> sgr.underline = false

        "25" -> sgr.blink = false

        "27" -> sgr.reversed = false

        "28" -> sgr.hidden = false

        "29" -> sgr.strikethrough = false

        // Foreground colors 30-37
        "30" -> sgr.fg = Color.Black

        "31" -> sgr.fg = Color.Red

        "32" -> sgr.fg = Color.Green

        "33" -> sgr.fg = Color.Yellow

        "34" -> sgr.fg = Color.Blue

        "35" -> sgr.fg = Color.Magenta

        "36" -> sgr.fg = Color.Cyan

        "37" -> sgr.fg = Color.White

        // Background colors 40-47
        "40" -> sgr.bg = Color.Black

        "41" -> sgr.bg = Color.Red

        "42" -> sgr.bg = Color.Green

        "43" -> sgr.bg = Color.Yellow

        "44" -> sgr.bg = Color.Blue

        "45" -> sgr.bg = Color.Magenta

        "46" -> sgr.bg = Color.Cyan

        "47" -> sgr.bg = Color.White

        // Bright foreground colors 90-97
        "90" -> sgr.fg = Color.BrightBlack

        "91" -> sgr.fg = Color.BrightRed

        "92" -> sgr.fg = Color.BrightGreen

        "93" -> sgr.fg = Color.BrightYellow

        "94" -> sgr.fg = Color.BrightBlue

        "95" -> sgr.fg = Color.BrightMagenta

        "96" -> sgr.fg = Color.BrightCyan

        "97" -> sgr.fg = Color.BrightWhite

        // Bright background colors 100-107
        "100" -> sgr.bg = Color.BrightBlack

        "101" -> sgr.bg = Color.BrightRed

        "102" -> sgr.bg = Color.BrightGreen

        "103" -> sgr.bg = Color.BrightYellow

        "104" -> sgr.bg = Color.BrightBlue

        "105" -> sgr.bg = Color.BrightMagenta

        "106" -> sgr.bg = Color.BrightCyan

        "107" -> sgr.bg = Color.BrightWhite
    }
    return sgr
}

/**
 * Converts an [Sgr] to a [CategorisedSlice].
 */
internal fun Sgr.toSlice(text: String, start: Int, end: Int): CategorisedSlice =
    CategorisedSlice(
        text = text,
        start = start,
        end = end,
        fg = fg,
        bg = bg,
        intensity = intensity,
        italic = italic,
        underline = underline,
        blink = blink,
        reversed = reversed,
        hidden = hidden,
        strikethrough = strikethrough,
    )

/**
 * Parses the text and returns each formatted slice in order.
 * The ANSI escape codes are not included in the text slices.
 *
 * Each different text slice is returned in order such that the text without
 * the escape characters can be reconstructed. There is a helper function
 * [constructTextNoCodes] for this.
 *
 * Example:
 * ```kotlin
 * val text = "\u001b[31mHello\u001b[0m, World!"
 * val slices = categoriseText(text)
 * // slices[0] = CategorisedSlice(text="Hello", fg=Color.Red, ...)
 * // slices[1] = CategorisedSlice(text=", World!", fg=null, ...)
 * ```
 *
 * @param text The text containing ANSI escape sequences to parse.
 * @return A list of categorized slices with styling information.
 */
fun categoriseText(text: String): List<CategorisedSlice> {
    val matches = parse(text)
    val bytes = text.encodeToByteArray()

    var sgr = Sgr()
    var lo = 0

    // Will always be less than or equal to matches.size + 1 in length
    val slices = mutableListOf<CategorisedSlice>()

    for (m in matches) {
        // Add in the text before CSI with the previous SGR format
        if (m.start != lo) {
            val sliceText = bytes.sliceArray(lo until m.start).decodeToString()
            slices.add(sgr.toSlice(sliceText, lo, m.start))
        }

        sgr = handleSeq(m)
        lo = m.end
    }

    if (lo != bytes.size) {
        val sliceText = bytes.sliceArray(lo until bytes.size).decodeToString()
        slices.add(sgr.toSlice(sliceText, lo, bytes.size))
    }

    return slices
}

/**
 * Parses the text and returns each formatted slice in order using the v3 API.
 * In Kotlin this is identical to [categoriseText].
 */
fun categoriseTextV3(text: String): List<CategorisedSlice> =
    categoriseText(text)
