package ai.solace.tui.cansi

/**
 * Type definition of the collection of [CategorisedSlice]s.
 */
typealias CategorisedSlices = List<CategorisedSlice>

/**
 * The item type representing a line of [CategorisedSlice]s.
 */
typealias CategorisedLine = List<CategorisedSlice>

/**
 * The 16 standard ANSI colors (8 normal + 8 bright).
 */
enum class Color {
    Black,
    Red,
    Green,
    Yellow,
    Blue,
    Magenta,
    Cyan,
    White,
    BrightBlack,
    BrightRed,
    BrightGreen,
    BrightYellow,
    BrightBlue,
    BrightMagenta,
    BrightCyan,
    BrightWhite,
}

/**
 * The emphasis (bold, faint) states.
 */
enum class Intensity {
    /** Normal intensity (no emphasis). */
    Normal,

    /** Bold. */
    Bold,

    /** Faint. */
    Faint,
}

/**
 * Data structure that holds information about coloring and styling of a text slice.
 *
 * Uses nullable properties to indicate whether a style was explicitly set.
 * This is the v3 API which uses Option-style semantics.
 *
 * @property text The text slice.
 * @property start Inclusive starting byte position.
 * @property end Exclusive ending byte position.
 * @property fg The foreground (text) color, or null if not set.
 * @property bg The background color, or null if not set.
 * @property intensity The emphasis state (bold, faint, normal), or null if not set.
 * @property italic Italicized, or null if not set.
 * @property underline Underlined, or null if not set.
 * @property blink Slow blink text, or null if not set.
 * @property reversed Inverted colors, or null if not set.
 * @property hidden Invisible text, or null if not set.
 * @property strikethrough Struck-through, or null if not set.
 */
data class CategorisedSlice(
    val text: String,
    val start: Int,
    val end: Int,
    val fg: Color? = null,
    val bg: Color? = null,
    val intensity: Intensity? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val blink: Boolean? = null,
    val reversed: Boolean? = null,
    val hidden: Boolean? = null,
    val strikethrough: Boolean? = null,
) {
    /**
     * Creates a new slice with the same style but different text and positions.
     */
    fun cloneStyle(newText: String, newStart: Int, newEnd: Int): CategorisedSlice =
        copy(text = newText, start = newStart, end = newEnd)

    companion object {
        /**
         * Creates a slice with default styling (no style attributes set).
         */
        fun defaultStyle(text: String, start: Int, end: Int): CategorisedSlice =
            CategorisedSlice(text = text, start = start, end = end)

        /**
         * Constructs a [CategorisedSlice] with given SGR formatting.
         */
        fun withSgr(
            fg: Color? = null,
            bg: Color? = null,
            intensity: Intensity? = null,
            italic: Boolean? = null,
            underline: Boolean? = null,
            blink: Boolean? = null,
            reversed: Boolean? = null,
            hidden: Boolean? = null,
            strikethrough: Boolean? = null,
            text: String,
            start: Int,
            end: Int,
        ): CategorisedSlice =
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
    }
}

/**
 * Internal representation of SGR (Select Graphic Rendition) parameters.
 */
internal data class Sgr(
    var fg: Color? = null,
    var bg: Color? = null,
    var intensity: Intensity? = null,
    var italic: Boolean? = null,
    var underline: Boolean? = null,
    var blink: Boolean? = null,
    var reversed: Boolean? = null,
    var hidden: Boolean? = null,
    var strikethrough: Boolean? = null,
)

/**
 * Constructs a string of the categorized text without the ANSI escape characters.
 *
 * Example:
 * ```kotlin
 * val categorized = categoriseText("\u001b[30mH\u001b[31me\u001b[32ml\u001b[33ml\u001b[34mo")
 * val plainText = constructTextNoCodes(categorized)
 * // plainText = "Hello"
 * ```
 *
 * @param slices The categorized slices to reconstruct.
 * @return The plain text without ANSI escape codes.
 */
fun constructTextNoCodes(slices: List<CategorisedSlice>): String =
    buildString(slices.sumOf { it.text.length }) {
        for (slice in slices) {
            append(slice.text)
        }
    }

internal data class NewLineSplit(
    val firstText: String,
    val firstByteLength: Int,
    val remainderText: String?,
    val remainderByteStart: Int,
)

/**
 * Splits on the first instance of `\r\n` or `\n`.
 * The returned offsets are UTF-8 byte counts within [txt].
 */
internal fun splitOnNewLine(txt: String): NewLineSplit {
    val nl = txt.indexOf('\n')

    if (nl == -1) {
        return NewLineSplit(
            firstText = txt,
            firstByteLength = txt.encodeToByteArray().size,
            remainderText = null,
            remainderByteStart = -1,
        )
    }

    val firstCharEnd = if (nl > 0 && txt[nl - 1] == '\r') nl - 1 else nl
    val remainderCharStart = nl + 1
    val firstText = txt.substring(0, firstCharEnd)
    val remainderText = txt.substring(remainderCharStart)
    val firstByteLength = txt.substring(0, firstCharEnd).encodeToByteArray().size
    val remainderByteStart = txt.substring(0, remainderCharStart).encodeToByteArray().size

    return NewLineSplit(
        firstText = firstText,
        firstByteLength = firstByteLength,
        remainderText = remainderText,
        remainderByteStart = remainderByteStart,
    )
}

/**
 * Constructs an iterator over each new line (`\n` or `\r\n`) and returns
 * the categorized slices within those. [CategorisedSlice]s that include
 * a new line are split with the same style.
 *
 * Example:
 * ```kotlin
 * val text = "Hello\nWorld"
 * val slices = categoriseText(text)
 * val lines = lineIter(slices).toList()
 * // lines[0] = [CategorisedSlice(text="Hello", ...)]
 * // lines[1] = [CategorisedSlice(text="World", ...)]
 * ```
 *
 * @param slices The categorized slices to iterate over by line.
 * @return An iterator that yields lines of categorized slices.
 */
fun lineIter(slices: List<CategorisedSlice>): CategorisedLineIterator =
    CategorisedLineIterator(slices)

/**
 * An iterator structure for `List<CategorisedSlice>`, iterating over each new line
 * (`\n` or `\r\n`) and returning the categorized slices within those.
 * [CategorisedSlice]s that include a new line are split with the same style.
 */
class CategorisedLineIterator(
    private val slices: List<CategorisedSlice>,
) : Iterator<List<CategorisedSlice>> {
    private var idx = 0
    private var prev: CategorisedSlice? = null
    private var hasMoreLines = true

    override fun hasNext(): Boolean {
        if (!hasMoreLines) return false
        // Check if we have more content
        return prev != null || idx < slices.size
    }

    override fun next(): List<CategorisedSlice> {
        if (!hasNext()) throw NoSuchElementException()

        val v = mutableListOf<CategorisedSlice>()

        prev?.let { prevSlice ->
            // Need to test splitting this, might be more new lines in remainder
            val split = splitOnNewLine(prevSlice.text)

            // Push first slice on -- only if not empty
            // If first == 0 it is because there is a sequence of new lines
            v.add(
                prevSlice.cloneStyle(
                    split.firstText,
                    prevSlice.start,
                    prevSlice.start + split.firstByteLength,
                ),
            )

            if (split.remainderText != null) {
                // There is a remainder, which means a new line was hit
                prev =
                    prevSlice.cloneStyle(
                        split.remainderText,
                        prevSlice.start + split.remainderByteStart,
                        prevSlice.end,
                    )
                return v // Exit early
            }

            prev = null // Consumed prev
        }

        while (idx < slices.size) {
            val slice = slices[idx]
            idx++ // Increment to next slice

            val split = splitOnNewLine(slice.text)

            // Push first slice on -- only if not empty
            if (split.firstByteLength > 0 || v.isEmpty()) {
                v.add(
                    slice.cloneStyle(
                        split.firstText,
                        slice.start,
                        slice.start + split.firstByteLength,
                    ),
                )
            }

            if (split.remainderText != null) {
                // There is a remainder, which means a new line was hit
                if (split.remainderText.isNotEmpty()) {
                    // Not just a trailing new line
                    prev =
                        slice.cloneStyle(
                            split.remainderText,
                            slice.start + split.remainderByteStart,
                            slice.end,
                        )
                }
                break // Exit looping
            }
        }

        if (v.isEmpty() && idx >= slices.size) {
            hasMoreLines = false
            throw NoSuchElementException()
        }

        return v
    }
}

/**
 * Extension function to convert the iterator to a sequence for easier use.
 */
fun CategorisedLineIterator.asSequence(): Sequence<List<CategorisedSlice>> =
    generateSequence { if (hasNext()) next() else null }
