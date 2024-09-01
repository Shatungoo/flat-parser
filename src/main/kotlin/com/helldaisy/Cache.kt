@file:OptIn(ExperimentalCoroutinesApi::class)

package com.helldaisy

import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.PriorityBlockingQueue

fun main() {
    val id = "123"
    val url = "https://api-statements.tnet.ge/uploads/statements/1mcs02f666c7d4f87f6e_thumb.jpg"
}

val byteCash = LocalCache<String, ByteArray>()
val downloader = ImageDownloader()

interface ICache<N, T> {
    operator fun get(key: N, block: () -> T): T
}

class LocalCache<N, T> : ICache<N, T> {
    private val csh = mutableMapOf<N, T>()
    override fun get(key: N, block: () -> T): T {
        return csh[key] ?: block().also { csh[key] = it }
    }
}

class FileCache : ICache<String, ByteArray> {

    override fun get(key: String, block: () -> ByteArray): ByteArray {
        Paths.get("").parent
        return block()
    }
}

fun getFile(imageId: String, url: String, priority: Int=10): ByteArray? {
    val dir = getTemporalDirectory(imageId)
    val fileName = getFileNameFromUrl(url)
    try {
        val id = "$imageId-$fileName"

        return byteCash.get(id) {
            val file = Paths.get(dir.absolutePath, fileName).toFile()
            (if (file.exists()) file
//            else runBlocking { downloadImage(url, file) }).readBytes()
            else runBlocking { downloader.download(url, Paths.get(dir.absolutePath, fileName), priority) }
                    ).readBytes()

        }
    } catch (e: Exception) {
        println("Error get file image: $url ${e.message}")
        Paths.get(dir.absolutePath, fileName).toFile().delete()
        return null
    }
}

fun cacheImage(imageId: String, url: String, priority: Int) {
    cacheImages(imageId, listOf(url), priority)
}

fun cacheImages(imageId: String, urls: List<String>, priority: Int = 5) {
    CoroutineScope(Dispatchers.IO.limitedParallelism(3)).launch {
        val dir = getTemporalDirectory(imageId)
        urls.forEach {
            try {
                val fileName = getFileNameFromUrl(it)
                downloadImage(it, Paths.get(dir.absolutePath, fileName).toFile())

//                downloader.downloadCache(it, Paths.get(dir.absolutePath, fileName), priority)
            } catch (e: Exception) {
                println("Error caching images: ${e.message}")
            }
        }
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

class ImageDownloader {
    private val downloadQueue = PriorityBlockingQueue<(DownloadTask)>()
    private val inProgress: MutableMap<String, CompletableDeferred<File>> = mutableMapOf()

    data class DownloadTask(
        var priority: Int,
        val url: String,
        val file: File,
        val result: CompletableDeferred<File>,
    ) : Comparable<DownloadTask> {
        override fun compareTo(other: DownloadTask): Int = this.priority.compareTo(other.priority)
    }

    fun downloadCache(url: String, filePath: Path, priority: Int = 5) {
        val file = filePath.toFile()
        if (file.exists() || inProgress.contains(url)) return
        addTask(url, file, priority)
    }

    private fun addTask(
        url: String,
        file: File,
        priority: Int = 5,
        result: CompletableDeferred<File> = CompletableDeferred()
    ): CompletableDeferred<File> {
        if (inProgress[url] != null) {
            return inProgress[url]!!
        }
        downloadQueue.filter { it.url == url }.let {
            if (it.isNotEmpty()) {
                if (it.first().priority < priority)
                    it.first().priority = priority
                return it.first().result
            }
        }

        downloadQueue.add(DownloadTask(priority, url, file, result))
        if (!lock) processQueue()
        return result
    }

    suspend fun download(url: String, filePath: Path, priority: Int = 10): File {
        val file = filePath.toFile()
        inProgress[url]?.let { return it.await() }
        downloadQueue.find { it.url == url }?.let {
            if (it.priority < priority) it.priority = priority
            return it.result.await()
        }
        return addTask(url, file, priority).await()
    }

    private var lock = false
    val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(5))
    private fun processQueue() {
        if (lock) return
        lock = true
        while (downloadQueue.isNotEmpty()) {
                val task = downloadQueue.poll()
                if (task != null) {
                    runBlocking { scope.launch {
                        try {
                            inProgress[task.url] = task.result
                            val file = downloadImage(task.url, task.file)
                            task.result.complete(file)
                        } catch (e: Exception) {
                            task.result.completeExceptionally(e)
                        } finally {
                            inProgress.remove(task.url)
                        }
                    }
                }
            }
        }
        lock = false
    }
}

