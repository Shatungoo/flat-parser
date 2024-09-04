import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.compose") version "1.7.0-alpha03"
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("pl.allegro.tech.build.axion-release") version "1.18.6"
}

group = "com.helldaisy"
version = scmVersion.version.replace(Regex("[^0-9.]"),"")

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val ktor_version = "2.3.12"
val ktorm_version = "4.1.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-encoding:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("org.jxmapviewer:jxmapviewer2:2.8")

    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("org.ktorm:ktorm-support-sqlite:${ktorm_version}")
    implementation("org.ktorm:ktorm-core:${ktorm_version}")
    implementation("org.ktorm:ktorm-jackson:${ktorm_version}")
}

compose.desktop {
    application {
        buildTypes.release {
            proguard {
                version.set("7.4.0")
                configurationFiles.from(project.file("compose-desktop.pro"))
            }
        }
        mainClass = "com.helldaisy.MainKt"

        nativeDistributions {
            windows{
                modules("java.instrument", "java.management", "java.sql.rowset", "jdk.unsupported")
                version = version.toString()
            }
            macOS {
                modules("java.instrument", "java.management", "java.sql.rowset", "jdk.unsupported")
                version = if (version.toString().startsWith("0.")) {
                    version.toString().replaceFirst("0.", "1.")
                } else {
                    version.toString()
                }
            }
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "flat-parser"
            packageVersion = version.toString()
        }
    }
}
