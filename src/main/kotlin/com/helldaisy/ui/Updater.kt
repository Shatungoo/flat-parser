package com.helldaisy.ui

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.content.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

val url = "https://github.com/Shatungoo/flat-parser/releases/latest"

fun main() {
//    if (needUpdate) {
//        println("Need update")
//    } else {
//        println("No need update")
//    }
}

enum class UpdateStatus {
    NEED_UPDATE,
    NO_NEED_UPDATE,
    UPDATE_IN_PROGRESS,
    REQUIRE_RESTART,
}


fun updateApp() {
    val commandList = arrayOf(
        "powershell.exe",
        "-Command",
        "Expand-Archive -Path flat-parser.zip -DestinationPath . -force",
        "&&Remove-Item flat-parser.zip",
        "&&Start-Process flat-parser.exe"
    )
    ProcessBuilder(*commandList).start()
    exitProcess(0);
}


val currentVersion by lazy { Version.fromString(System.getProperty("jpackage.app-version")) }

//https://github.com/Shatungoo/flat-parser/releases/tag/v0.1.30/flat-parser.zip
suspend fun downloadLatest(
    onEnd: () -> Unit = {},
) {
    val latestVersion = latestVersion()
    val url = "https://github.com/Shatungoo/flat-parser/releases/download/v$latestVersion/flat-parser.zip"
    val client = HttpClient(CIO) {
        install(HttpTimeout)
    }
    val file = File("flat-parser.zip")
    try {
        val response = client.get(url) {
            timeout {
                requestTimeoutMillis = 300000
            }
        }
        if (!file.exists()) withContext(Dispatchers.IO) {
            file.createNewFile()
        }
        println("Downloading latest version")
        response.bodyAsChannel().copyAndClose(file.writeChannel())
        onEnd()
    } catch (e: Exception) {
        println("Error update $url File deleted" + e.message)
        file.delete()
    }
}

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val rest: String?,
) {
    companion object {

        fun fromString(version: String?): Version {
            if (version == null) {
                return Version(0, 0, 0, null)
            }
            val (major, minor, patch, rest) = version.split(".", limit = 4) + listOf(null, null, null)
            return Version(
                major?.toInt() ?: 0,
                minor?.toInt() ?: 0,
                patch?.toInt() ?: 0,
                rest
            )
        }
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    operator fun compareTo(other: Version): Int {
        return when {
            major != other.major -> major - other.major
            minor != other.minor -> minor - other.minor
            patch != other.patch -> patch - other.patch
            else -> 0
        }
    }
}

private lateinit var _latestVersion: Version

suspend fun latestVersion(): Version {
    if (!::_latestVersion.isInitialized) {
        val url = "https://github.com/Shatungoo/flat-parser/releases/latest"
        val client = HttpClient(CIO) {
            followRedirects = true
        }

        try {
            val response: HttpResponse = client.get(url)
            val finalUrl = response.request.url.toString()
            val version = finalUrl.substringAfterLast("/tag/").removePrefix("v")
            println("Check latest version: $version")
            _latestVersion = Version.fromString(version)
        } catch (e: Exception) {
            e.printStackTrace()
            return Version(0, 0, 0, null)
        } finally {
            client.close()
        }
    }
    return _latestVersion
}

suspend fun checkUpdate(): Boolean {
    val latest = latestVersion()
    return latest > currentVersion
}
