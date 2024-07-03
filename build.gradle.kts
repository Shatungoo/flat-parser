import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.ir.backend.js.compile

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

val ktor_version = "2.3.12"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-encoding:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
//    implementation ("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation ("com.h2database:h2:2.2.224")
    implementation ("com.google.cloud:google-cloud-translate:2.46.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "flat-parser`"
            packageVersion = "1.0.0"
        }
    }
}
