// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "SwiftTestHarness",
    platforms: [
        .macOS(.v14),
    ],
    dependencies: [
        .package(name: "Cansi", path: "../build/SPMPackage/macosArm64/Debug")
    ],
    targets: [
        .testTarget(
            name: "SwiftTestHarnessTests",
            dependencies: [
                .product(name: "CansiLibrary", package: "Cansi")
            ],
            swiftSettings: [
                .unsafeFlags([
                    "-F", "/Library/Developer/CommandLineTools/Library/Developer/Frameworks",
                ]),
            ],
            linkerSettings: [
                .unsafeFlags([
                    "-L", "../build/swift-test",
                    "-lCansi",
                    "-F", "/Library/Developer/CommandLineTools/Library/Developer/Frameworks",
                    "-Xlinker", "-rpath", "-Xlinker", "/Library/Developer/CommandLineTools/Library/Developer/Frameworks",
                    "-Xlinker", "-rpath", "-Xlinker", "/Library/Developer/CommandLineTools/Library/Developer/usr/lib",
                ]),
            ]
        ),
    ]
)
