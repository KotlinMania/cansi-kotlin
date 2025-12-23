import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.android.kotlin.multiplatform.library") version "8.6.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.0"

// Setup Android SDK location and licenses automatically
val sdkDir = file(".android-sdk")
val licensesDir = sdkDir.resolve("licenses")
if (!licensesDir.exists()) licensesDir.mkdirs()
val licenseFile = licensesDir.resolve("android-sdk-license")
if (!licenseFile.exists()) {
    licenseFile.writeText(
        """
        8933bad161af4178b1185d1a37fbf41ea5269c55
        d56f5187479451eabf01fb74abc367c344559d7b
        24333f8a63b6825ea9c5514f83c2829b004d1fee
        """.trimIndent()
    )
}
val localProperties: File? = rootProject.file("local.properties")
if (!localProperties?.exists()!!) {
    localProperties.writeText("sdk.dir=${sdkDir.absolutePath}")
}

kotlin {
    applyDefaultHierarchyTemplate()

    val xcf = XCFramework("Cansi")

    macosArm64 {
        binaries.framework {
            baseName = "Cansi"
            xcf.add(this)
        }
    }
    macosX64 {
        binaries.framework {
            baseName = "Cansi"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()
    iosArm64 {
        binaries.framework {
            baseName = "Cansi"
            xcf.add(this)
        }
    }
    iosX64 {
        binaries.framework {
            baseName = "Cansi"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Cansi"
            xcf.add(this)
        }
    }
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    jvmToolchain(21)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kotlinmania.cansi"
        compileSdk = 34
        minSdk = 24
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "cansi-kotlin", version.toString())

    pom {
        name.set("cansi-kotlin")
        description.set("Kotlin Multiplatform library for ANSI terminal colors and styles")
        inceptionYear.set("2024")
        url.set("https://github.com/KotlinMania/cansi-kotlin")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("sydneyrenee")
                name.set("Sydney Renee")
                email.set("sydney@solace.ofharmony.ai")
                url.set("https://github.com/sydneyrenee")
            }
        }

        scm {
            url.set("https://github.com/KotlinMania/cansi-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/cansi-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/cansi-kotlin.git")
        }
    }
}
