# cansi-kotlin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/cansi-kotlin?color=blue)](https://central.sonatype.com/artifact/io.github.kotlinmania/cansi-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/cansi-kotlin/ci.yml?branch=main&label=CI)](https://github.com/KotlinMania/cansi-kotlin/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Platform: KMP](https://img.shields.io/badge/Platform-Multiplatform-orange.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Swift Export](https://img.shields.io/badge/Swift_Export-Ready-F05138.svg?logo=swift&logoColor=white)](https://github.com/KotlinMania/cansi-kotlin)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

**C**ategorise **ANSI** — ANSI escape code parser and text categoriser for Kotlin Multiplatform.

A line-by-line transliteration port of [`kurtlawrence/cansi`](https://github.com/kurtlawrence/cansi) to Kotlin Multiplatform, providing native ANSI CSI and SGR sequence parsing, formatting metadata extraction, code stripping, and styled line iteration.

---

## Features

- 🎨 **Deconstruct ANSI styling**: Parses CSI (Control Sequence Introducer) and SGR (Select Graphic Rendition) sequences into structured slices with metadata.
- 🧹 **Strip escape codes**: Cleanly reconstitutes plain unstyled text without regex overhead.
- 📜 **Line-by-line iteration**: Iterates through styled lines while maintaining escape sequence state across line breaks.
- 🌐 **True Multiplatform**: Runs on JVM, Android, iOS, macOS, Linux, Windows, JavaScript, and WebAssembly (Wasm-JS & Wasm-WASI).
- 🍏 **Swift Export ready**: Seamlessly integrates into Swift / Xcode projects via Swift PM.

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
// In your multiplatform commonMain dependencies or single-platform build:
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.kotlinmania:cansi-kotlin:0.1.5")
        }
    }
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.kotlinmania</groupId>
    <artifactId>cansi-kotlin</artifactId>
    <version>0.1.5</version>
</dependency>
```

---

## Quick Start

```kotlin
import ai.solace.tui.cansi.Color
import ai.solace.tui.cansi.Intensity
import ai.solace.tui.cansi.categoriseText
import ai.solace.tui.cansi.constructTextNoCodes
import ai.solace.tui.cansi.lineIter

fun main() {
    val styledInput = "\u001b[31;1mHello\u001b[0m, \u001b[32mWorld\u001b[0m!"

    // 1. Categorise text into slices with formatting metadata
    val slices = categoriseText(styledInput)
    for (slice in slices) {
        println("'${slice.text}' (byte range ${slice.start}..${slice.end}) -> FG: ${slice.fg}, Intensity: ${slice.intensity}")
    }

    // 2. Strip ANSI escape sequences back to clean text
    val plain = constructTextNoCodes(slices)
    println(plain) // Output: Hello, World!

    // 3. Iterate over multi-line text preserving ANSI state
    val multiLine = "\u001b[34mFirst Line\nSecond Line\u001b[0m"
    for ((lineIndex, lineSlices) in lineIter(multiLine).withIndex()) {
        println("Line $lineIndex has ${lineSlices.size} slices:")
        for (slice in lineSlices) {
            println("  - '${slice.text}' with color ${slice.fg}")
        }
    }
}
```

---

## Data Structures

### `CategorisedSlice`

Each formatted chunk is represented by a `CategorisedSlice`:

| Property | Type | Description |
|---|---|---|
| `text` | `String` | The slice text without ANSI escape sequences |
| `start` | `Int` | Start index into the UTF-8 byte representation |
| `end` | `Int` | End index into the UTF-8 byte representation |
| `fg` | `Color?` | Foreground ANSI color (if set) |
| `bg` | `Color?` | Background ANSI color (if set) |
| `intensity` | `Intensity?` | `Normal`, `Bold`, or `Faint` |
| `italic` | `Boolean?` | True if italicised |
| `underline` | `Boolean?` | True if underlined |
| `blink` | `Boolean?` | True if blinking |
| `reversed` | `Boolean?` | True if inverted / reversed |
| `hidden` | `Boolean?` | True if concealed |
| `strikethrough` | `Boolean?` | True if crossed out |

---

## Supported Targets

| Platform | Targets |
|---|---|
| **JVM** | Java 21+ |
| **Android** | Android Library (AAR), API 24+; Android Native arm64 & x64 |
| **Apple** | macOS (arm64), iOS (arm64, simulator-arm64, x64), tvOS (arm64, simulator-arm64), watchOS (arm64, device-arm64, simulator-arm64) |
| **Linux** | Linux x64, Linux arm64 |
| **Windows** | Windows mingwX64 |
| **Web** | JS (Node.js & Browser), Wasm-JS, Wasm-WASI |

---

## Building & Testing

```bash
# Build all targets and verify framework links
./gradlew build

# Run all multiplatform test suites + Swift tests
./gradlew test
```

---

## License & Credits

This Kotlin port is distributed under the [MIT License](LICENSE).

- **Original Project:** Based on [`kurtlawrence/cansi`](https://github.com/kurtlawrence/cansi) by Kurt Lawrence and contributors.
- **Kotlin Port:** Copyright (c) 2026 Sydney Renee and The Solace Project.
