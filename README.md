# cansi-kotlin

<div align="center">

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/cansi-kotlin?color=blue&logo=apachemaven)](https://central.sonatype.com/artifact/io.github.kotlinmania/cansi-kotlin)
[![CI](https://img.shields.io/github/actions/workflow/status/KotlinMania/cansi-kotlin/ci.yml?branch=main&label=CI&logo=githubactions&logoColor=white)](https://github.com/KotlinMania/cansi-kotlin/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Platform: KMP](https://img.shields.io/badge/Platform-Multiplatform-blue.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Swift Export](https://img.shields.io/badge/Swift_Export-Ready-F05138.svg?logo=swift&logoColor=white)](https://github.com/KotlinMania/cansi-kotlin)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![GitHub Release](https://img.shields.io/github/v/release/KotlinMania/cansi-kotlin?color=purple&logo=github)](https://github.com/KotlinMania/cansi-kotlin/releases)
[![Upstream Crate](https://img.shields.io/crates/v/cansi.svg?label=upstream%20cansi&color=orange&logo=rust)](https://crates.io/crates/cansi)

**C**ategorise **ANSI** — Fast, zero-dependency ANSI escape sequence parser and categoriser for Kotlin Multiplatform.

*A faithful line-by-line transliteration of [Kurt Lawrence's](https://github.com/kurtlawrence) [`cansi`](https://github.com/kurtlawrence/cansi) Rust crate.*

</div>

---

## Overview

Terminal applications, build tools, test runners, and command-line interfaces emit ANSI escape sequences to format text with colors, font weights, and text styling. However, when consuming this output—such as rendering into terminal user interfaces (TUIs), logging to disk, or displaying in web interfaces—you often need to:

1. **Deconstruct** text into styled spans while preserving formatting metadata.
2. **Strip** escape sequences cleanly without regex overhead.
3. **Stream** lines while carrying terminal styling across newline boundaries.

`cansi-kotlin` provides high-performance, non-allocating parsing of ANSI **CSI** (*Control Sequence Introducer*) sequences, focusing specifically on **SGR** (*Select Graphic Rendition*) parameters. It avoids unnecessary abstractions, regex engines, and external dependencies.

---

## Features

- ⚡ **Zero External Dependencies**: Self-contained parsing logic with no runtime dependencies.
- 🎨 **Complete SGR Support**: Parses foreground/background colors (standard and bright), text intensity (bold/faint), italics, underline, blink, inversion, concealment, and strikethrough.
- 📏 **Byte-Accurate Offsets**: Returns exact UTF-8 byte boundary indices (`start` and `end`) for terminal buffer alignment.
- 🧹 **Zero-Regex Plaintext Extraction**: Fast string reconstruction without ANSI escape codes via `constructTextNoCodes`.
- 📜 **Stateful Multi-Line Streaming**: `lineIter` transparently maintains and propagates active styling across `\n` and `\r\n` lines.
- 🌐 **Comprehensive Multiplatform Targets**: Compiles for JVM, Android, iOS, macOS, Linux, Windows, JavaScript, and WebAssembly (Wasm-JS & Wasm-WASI).
- 🍏 **Native Swift Export**: First-class Swift PM package generation ready for direct Xcode consumption.

---

## Installation

### Gradle (Kotlin DSL)

Add `cansi-kotlin` to your `commonMain` dependencies:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.kotlinmania:cansi-kotlin:0.1.6")
        }
    }
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.kotlinmania</groupId>
    <artifactId>cansi-kotlin</artifactId>
    <version>0.1.6</version>
</dependency>
```

---

## How the Kotlin Implementation Works

The codebase is organized around four core components designed for speed, type safety, and memory efficiency:

```
[Raw Terminal Text]
         │
         ▼
 ┌───────────────┐
 │ parse(text)   │  ──▶ Scans byte sequence for CSI introducers (0x1b, 0x5b)
 └───────────────┘      and extracts Match(start, end) byte positions.
         │
         ▼
 ┌───────────────┐
 │ handleSeq(m)  │  ──▶ Parses semicolon-delimited decimal parameters
 └───────────────┘      and builds an Sgr state model.
         │
         ▼
 ┌──────────────────────┐
 │ categoriseText(text) │  ──▶ Slices text between escape sequences, applying
 └──────────────────────┘      accumulated Sgr style to each CategorisedSlice.
         │
         ▼
 [List<CategorisedSlice>] ──▶ Clean text spans with nullable Color & Intensity.
```

### 1. Byte-Oriented CSI Scanning (`parse`)
ANSI CSI sequences begin with the byte sequence `ESC [` (`0x1B, 0x5B`) and terminate on any byte in the ASCII range `0x40..0x7E` (such as `'m'`). 
The Kotlin scanner inspects the UTF-8 byte representation of the input string:
- Computes character-to-byte boundary mappings without allocating duplicate buffers.
- Identifies sequence parameters (e.g. `31;1` in `\u001b[31;1m`).
- Ensures terminal boundaries align strictly with standard UTF-8 offsets.

### 2. Stateful SGR State Accumulator (`adjustSgr`)
ANSI escape sequences alter terminal state cumulatively. An escape sequence like `\u001b[1m` enables bold, and subsequent sequences modify foreground or background colors without resetting prior styling unless explicitly reset with code `0`.
The Kotlin engine incrementally applies ECMA-48 code numbers:
- `0`: Clears all formatting to default/null.
- `1` / `2` / `22`: Sets intensity to `Bold`, `Faint`, or clears back to `Normal`.
- `30..37` / `39`: Standard foreground colors and reset.
- `40..47` / `49`: Standard background colors and reset.
- `90..97`: High-intensity / bright foreground colors.
- `100..107`: High-intensity / bright background colors.
- `3`–`9` / `23`–`29`: Boolean flags for italic, underline, blink, reversed, hidden, and strikethrough.

### 3. Slices with Rich Styling Metadata (`CategorisedSlice`)
Instead of parsing into an abstract syntax tree (AST), `cansi-kotlin` yields a flat `List<CategorisedSlice>`. Each slice contains:
- The stripped slice text.
- Byte index boundaries into the original text string.
- Nullable style properties (`null` indicates standard/unaltered terminal default).

### 4. Stateful Line Iterator (`lineIter`)
When processing multi-line text (such as compiler errors or command logs), ANSI styles often wrap across line breaks. `lineIter(text)` returns an `Iterator<List<CategorisedSlice>>`:
- Splits on `\n` or `\r\n`.
- Carries forward active terminal formatting from the previous line to the beginning of the next.
- Emits each line as a standalone `List<CategorisedSlice>`.

---

## Code Examples

### 1. Basic Text Categorisation

```kotlin
import ai.solace.tui.cansi.Color
import ai.solace.tui.cansi.Intensity
import ai.solace.tui.cansi.categoriseText

fun main() {
    // Red, bold "Hello" followed by unstyled ", " and green "World!"
    val input = "\u001b[31;1mHello\u001b[0m, \u001b[32mWorld!\u001b[0m"

    val slices = categoriseText(input)

    for (slice in slices) {
        println(
            "Text: \"${slice.text}\" | " +
            "Range: [${slice.start}, ${slice.end}) | " +
            "FG: ${slice.fg} | " +
            "Intensity: ${slice.intensity}"
        )
    }
}
```

**Output:**
```
Text: "Hello"  | Range: [0, 16)  | FG: Red   | Intensity: Bold
Text: ", "     | Range: [16, 22) | FG: null  | Intensity: null
Text: "World!" | Range: [22, 39) | FG: Green | Intensity: null
```

### 2. Stripping Escape Codes to Clean Plaintext

```kotlin
import ai.solace.tui.cansi.categoriseText
import ai.solace.tui.cansi.constructTextNoCodes

fun main() {
    val styled = "\u001b[33mWarning:\u001b[0m Something \u001b[1mnotable\u001b[0m happened."
    
    // Categorise, then reconstruct without escape codes
    val slices = categoriseText(styled)
    val plain = constructTextNoCodes(slices)

    println(plain)
    // Output: "Warning: Something notable happened."
}
```

### 3. Iterating Across Multi-Line Terminal Output

```kotlin
import ai.solace.tui.cansi.lineIter

fun main() {
    // A blue style spans across lines
    val logOutput = "\u001b[34m[INFO] Task started.\nProcessing step 1...\nProcessing step 2...\u001b[0m"

    for ((index, line) in lineIter(logOutput).withIndex()) {
        println("--- Line $index ---")
        for (slice in line) {
            println("  \"${slice.text}\" (Color: ${slice.fg})")
        }
    }
}
```

---

## API Reference & Data Structures

### `CategorisedSlice`

```kotlin
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
)
```

### `Color` Enum

Standard 16-color ANSI terminal palette:

| Standard (30-37 / 40-47) | Bright / High Intensity (90-97 / 100-107) |
|---|---|
| `Color.Black` | `Color.BrightBlack` |
| `Color.Red` | `Color.BrightRed` |
| `Color.Green` | `Color.BrightGreen` |
| `Color.Yellow` | `Color.BrightYellow` |
| `Color.Blue` | `Color.BrightBlue` |
| `Color.Magenta` | `Color.BrightMagenta` |
| `Color.Cyan` | `Color.BrightCyan` |
| `Color.White` | `Color.BrightWhite` |

### `Intensity` Enum

- `Intensity.Normal`: Standard weight (`22`).
- `Intensity.Bold`: Bold or increased intensity (`1`).
- `Intensity.Faint`: Faint, decreased intensity, or dim (`2`).

### Functions

| Function | Signature | Description |
|---|---|---|
| `categoriseText` | `(String) -> List<CategorisedSlice>` | Deconstructs text into categorised slices. |
| `categoriseTextV3` | `(String) -> List<CategorisedSlice>` | Direct alias to `categoriseText` for v3 parity. |
| `constructTextNoCodes` | `(List<CategorisedSlice>) -> String` | Builds an unstyled string by joining slice text. |
| `lineIter` | `(String) -> Iterator<List<CategorisedSlice>>` | Iterates lines while preserving style state. |
| `terminatedByte` | `(Byte) -> Boolean` | Returns true if the byte terminates a CSI sequence. |

---

## Supported Targets

`cansi-kotlin` builds for the complete Kotlin Multiplatform tier matrix:

| Platform | Target Identifiers |
|---|---|
| **JVM** | `jvm` (Java 21+ toolchain) |
| **Android** | `android` (AAR, API 24+), `androidNativeArm64`, `androidNativeX64` |
| **Apple** | `macosArm64`, `iosArm64`, `iosSimulatorArm64`, `iosX64`, `tvosArm64`, `tvosSimulatorArm64`, `watchosArm64`, `watchosDeviceArm64`, `watchosSimulatorArm64` |
| **Linux** | `linuxX64`, `linuxArm64` |
| **Windows** | `mingwX64` |
| **Web** | `js` (Browser & Node.js), `wasmJs` (Browser & Node.js), `wasmWasi` (WASI) |

---

## Acknowledgements & Attribution

`cansi-kotlin` is a direct transliteration of the [`cansi`](https://github.com/kurtlawrence/cansi) Rust crate.

- **Original Author**: [Kurt Lawrence](https://github.com/kurtlawrence) ([@kurtlawrence](https://github.com/kurtlawrence)) and contributors.
- **Upstream Crate**: Published on [crates.io/crates/cansi](https://crates.io/crates/cansi) with documentation at [docs.rs/cansi](https://docs.rs/cansi/).
- **Original Project Intent**: Providing an ergonomic, lightweight mechanism for parsing ANSI CSI and SGR sequences without constructing or modifying styled text.
- **Kotlin Multiplatform Port**: Maintained by Sydney Renee and The Solace Project as part of the [KotlinMania](https://github.com/KotlinMania) initiative to deliver high-quality, native Kotlin Multiplatform ports of essential systems libraries.

All architectural credit, escape sequence mappings, and design decisions originate from Kurt Lawrence's work.

---

## License

This project is licensed under the [MIT License](LICENSE) matching the upstream project license.
