import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.compose") version "1.7.0-alpha02"
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20-RC"
}

group = "com.helldaisy"
version = "1.0-SNAPSHOT"

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
//    implementation ("androidx.sqlite:sqlite-ktx:2.4.0")
//    implementation ("com.h2database:h2:2.2.224")
//    implementation ("com.google.cloud:google-cloud-translate:2.46.0")
    implementation("org.jxmapviewer:jxmapviewer2:2.8")

    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("org.ktorm:ktorm-support-sqlite:${ktorm_version}")
    implementation("org.ktorm:ktorm-core:${ktorm_version}")
    implementation("org.ktorm:ktorm-jackson:${ktorm_version}")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "flat-parser"
            packageVersion = "1.0.0"
        }
    }
}
