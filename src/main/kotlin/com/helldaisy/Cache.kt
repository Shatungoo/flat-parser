package com.helldaisy

import java.io.File
import java.nio.file.Paths

fun main() {
    val id = "123"
    val url = "https://api-statements.tnet.ge/uploads/statements/1mcs02f666c7d4f87f6e_thumb.jpg"
//    getFile(id, url)
//    val c = get(id) { url }
//    println(c)
}

val cash: MutableMap<Any, ByteArray> = mutableMapOf()

interface ICache {
    fun get(key: Any, block: () -> Any): Any
}
class LocalCache:ICache {
    private val csh = mutableMapOf<Any, Any>()
    override fun get(key: Any, block: () -> Any): Any {
        return csh[key] ?: block().also { csh[key] = it }
    }
}

suspend fun getFile(imageId: String, url: String): ByteArray? {
    try {
        val dir = getTemporalDirectory(imageId)
        val fileName = getFileNameFromUrl(url)
        val id = "$imageId-$fileName"
        if (!cash.containsKey(id)) {
            val file = Paths.get(dir.absolutePath, fileName).toFile()
            putToCache(file, url, id)
        }
        return cash[id]
    } catch (e: Exception) {
        println("Error uploading image: $url ${e.message}")
        return null
    }
}

private suspend fun putToCache(
    file: File,
    url: String,
    id: String,
): Boolean {
    if (file.exists()) {
        println("Load from file: $url")
        cash[id] = file.readBytes()
        return true
    }
    downloadImage(url, file).let {
        println("Load from url: $url")
        cash[id] = it.readBytes()
    }
    return false
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

