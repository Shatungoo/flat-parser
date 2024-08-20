package com.helldaisy

import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
    val id = "123"
    val url = "https://api-statements.tnet.ge/uploads/statements/1mcs02f666c7d4f87f6e_thumb.jpg"
}

val byteCash  = LocalCache<String, ByteArray>()

interface ICache<N, T> {
    operator fun get(key: N, block: () -> T): T
}
class LocalCache<N, T>:ICache<N, T> {
    private val csh = mutableMapOf<N, T>()
    override fun get(key: N, block: () -> T): T {
        return csh[key] ?: block().also { csh[key] = it }
    }
}

class FileCache:ICache<String, ByteArray> {

    override fun get(key: String, block: () -> ByteArray): ByteArray {
        Paths.get("").parent
        return block()
    }
}

fun getFile(imageId: String, url: String): ByteArray? {
    try {
        val dir = getTemporalDirectory(imageId)
        val fileName = getFileNameFromUrl(url)
        val id = "$imageId-$fileName"

        return byteCash.get(id){
            val file = Paths.get(dir.absolutePath, fileName).toFile()
            if (file.exists()) file.readBytes()
            else runBlocking { downloadImage(url, file).readBytes() }
        }
    } catch (e: Exception) {
        println("Error uploading image: $url ${e.message}")
        return null
    }
}

fun getTemporalDirectory(flatId: String): File {
    val path = Paths.get(System.getProperty("java.io.tmpdir"), appName, flatId)
    val file = path.toFile()
    file.mkdirs()
    return file
}

fun getFileNameFromUrl(url: String): String {
    val parts = url.split("/")
    return parts[parts.size - 1]
}

