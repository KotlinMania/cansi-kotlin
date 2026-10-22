import XCTest
import Cansi

private typealias CansiPkg = ExportedKotlinPackages.ai.solace.tui.cansi

final class CansiExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Cansi swift module imported cleanly")
    }

    func testParseAnsiSequences() throws {
        let ansiText = "Hello, \u{001b}[31;4mworld\u{001b}[0m!"
        let matches = CansiPkg.parse(text: ansiText)

        XCTAssertEqual(matches.count, 2)
        XCTAssertEqual(matches[0].start, 7)
        XCTAssertEqual(matches[0].end, 14)
        XCTAssertEqual(matches[0].text, "\u{001b}[31;4m")

        XCTAssertEqual(matches[1].start, 19)
        XCTAssertEqual(matches[1].end, 23)
        XCTAssertEqual(matches[1].text, "\u{001b}[0m")
    }

    func testCategoriseTextAndConstructTextNoCodes() throws {
        let text = "\u{001b}[31mHello\u{001b}[0m, World!"
        let slices = CansiPkg.categoriseText(text: text)

        XCTAssertEqual(slices.count, 2)
        XCTAssertEqual(slices[0].text, "Hello")
        XCTAssertEqual(slices[0].start, 5)
        XCTAssertEqual(slices[0].end, 10)
        XCTAssertEqual(slices[0].fg, .Red)
        XCTAssertNil(slices[0].bg)

        XCTAssertEqual(slices[1].text, ", World!")
        XCTAssertEqual(slices[1].start, 14)
        XCTAssertEqual(slices[1].end, 22)
        XCTAssertNil(slices[1].fg)

        let plain = CansiPkg.constructTextNoCodes(slices: slices)
        XCTAssertEqual(plain, "Hello, World!")
    }

    func testLineIterator() throws {
        let text = "Hello\nWorld"
        let slices = CansiPkg.categoriseText(text: text)
        let iter = CansiPkg.lineIter(slices: slices)

        XCTAssertTrue(iter.hasNext())
        let line1 = iter.next()
        XCTAssertEqual(line1.count, 1)
        XCTAssertEqual(line1[0].text, "Hello")

        XCTAssertTrue(iter.hasNext())
        let line2 = iter.next()
        XCTAssertEqual(line2.count, 1)
        XCTAssertEqual(line2[0].text, "World")

        XCTAssertFalse(iter.hasNext())
    }

    func testTerminatedByte() throws {
        XCTAssertTrue(CansiPkg.terminatedByte(byte: 0x40))
        XCTAssertTrue(CansiPkg.terminatedByte(byte: 0x6D))
        XCTAssertTrue(CansiPkg.terminatedByte(byte: 0x7E))
        XCTAssertFalse(CansiPkg.terminatedByte(byte: 0x3F))
        XCTAssertFalse(CansiPkg.terminatedByte(byte: 0x7F))
    }
}
