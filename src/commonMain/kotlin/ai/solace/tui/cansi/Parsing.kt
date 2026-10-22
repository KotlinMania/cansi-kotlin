// port-lint: source src/parsing.rs

package ai.solace.tui.cansi

/**
 * A match representing an ANSI escape sequence found in text.
 *
 * @property start First byte index (inclusive).
 * @property end Last byte index + 1 (exclusive).
 * @property text The matched escape sequence text.
 */
data class Match(
    val start: Int,
    val end: Int,
    val text: String,
)

/**
 * CSI byte sequence: ESC (0x1b) followed by '[' (0x5b).
 */
val CSI: ByteArray = byteArrayOf(0x1b, 0x5b)
private val CSI_BYTES = CSI

/**
 * Checks if a byte is a CSI sequence terminating character.
 * Terminating bytes are in the range 0x40-0x7E.
 */
fun terminatedByte(byte: Byte): Boolean =
    byte in 0x40..0x7E

/**
 * Parses ANSI escape codes from the given text, returning a list of [Match].
 *
 * Only CSI (Control Sequence Introducer) sequences are detected.
 * The escape codes themselves are returned, not the styled text.
 * Positions are byte indices into the UTF-8 encoded string.
 *
 * Example:
 * ```kotlin
 * val ansiText = "Hello, \u001b[31;4mworld\u001b[0m!"
 * val parsed = parse(ansiText).map { it.start to it.end }
 * // parsed = [(7, 14), (19, 23)]
 * ```
 *
 * @param text The text to parse for ANSI escape codes.
 * @return A list of matches representing escape sequences found.
 */
fun parse(text: String): List<Match> {
    val matches = mutableListOf<Match>()
    val bytes = text.encodeToByteArray()
    val csiLen = CSI_BYTES.size

    var start = 0
    var end = start + csiLen

    while (end <= bytes.size) {
        // Check if we have CSI at this position
        if (bytes[start] == CSI_BYTES[0] && bytes[start + 1] == CSI_BYTES[1]) {
            // Start of a CSI sequence - find the terminating byte
            while (end < bytes.size && !terminatedByte(bytes[end])) {
                end++
            }

            val finalEnd = end + 1

            if (finalEnd > bytes.size) {
                break
            }

            matches.add(
                Match(
                    start = start,
                    end = finalEnd,
                    text = bytes.sliceArray(start until finalEnd).decodeToString(),
                ),
            )

            start = finalEnd
        } else {
            // Move past current UTF-8 character
            // UTF-8 encoding: bytes starting with 0xxxxxxx or 11xxxxxx are start bytes
            // Continuation bytes start with 10xxxxxx
            start++
            // Skip continuation bytes (10xxxxxx pattern)
            while (start < bytes.size && (bytes[start].toInt() and 0xC0) == 0x80) {
                start++
            }
        }

        end = start + csiLen
    }

    return matches
}
