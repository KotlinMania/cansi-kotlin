#if canImport(Testing)
import Testing
import Cansi

@Suite("Cansi Swift Export Tests")
struct CansiExportTests {
    @Test("Cansi swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Cansi swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import Cansi

final class CansiExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Cansi swift module imported cleanly")
    }
}
#endif
