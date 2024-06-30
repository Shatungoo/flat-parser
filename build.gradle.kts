import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val kotlinVersion = "1.9.22"

plugins {
    kotlin("jvm") version "1.9.22"//"2.0.0"
    id("org.jetbrains.compose") version "1.6.0"//"1.7.0-dev1703"
    kotlin("plugin.serialization") version "1.9.22"
//    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

group = "com.helldaisy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "settings"
            packageVersion = "1.0.0"
        }
    }
}
