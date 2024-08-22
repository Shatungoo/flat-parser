package com.helldaisy

import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.Semaphore

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

fun getFile(imageId: String, url: String): ByteArray? {
    try {
        val dir = getTemporalDirectory(imageId)
        val fileName = getFileNameFromUrl(url)
        val id = "$imageId-$fileName"

        return byteCash.get(id) {
            downloader.download(url, Paths.get(dir.absolutePath, fileName), 10).readBytes()

        }
    } catch (e: Exception) {
        println("Error uploading image: $url ${e.message}")
        return null
    }
}
fun cacheImage(imageId: String, url: String, priority: Int) {
    cacheImages(imageId, listOf(url), priority)
}

fun cacheImages(imageId: String, urls: List<String>, priority: Int=5) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val dir = getTemporalDirectory(imageId)
            urls.forEach {
                val fileName = getFileNameFromUrl(it)
                downloader.downloadCache(it, Paths.get(dir.absolutePath, fileName), priority)
            }
        } catch (e: Exception) {
            println("Error caching images: ${e.message}")
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
    private val semaphore = Semaphore(10)
    private val semaphore2 = Semaphore(1)

    data class DownloadTask(
        var priority: Int,
        val url: String,
        val file: File,
        val result: CompletableDeferred<File>,
    ) : Comparable<DownloadTask> {
        override fun compareTo(other: DownloadTask): Int = this.priority.compareTo(other.priority)
    }

    fun downloadCache(url: String, filePath: Path, priority: Int=5) {
        val file = filePath.toFile()
        if (file.exists()) return
        addTask(url, file, priority)
    }

    private fun addTask(url: String,
                        file: File,
                        priority: Int=5,
                        result: CompletableDeferred<File> = CompletableDeferred()): CompletableDeferred<File> {
        semaphore2.acquire()
        downloadQueue.filter { it.url == url }.let {
            if (it.isNotEmpty()) {
                if (it.first().priority < priority)
                    it.first().priority = priority
                return it.first().result
            }
        }

        downloadQueue.add(DownloadTask(priority, url, file, result))
        semaphore2.release()
        processQueue()
        return result
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun download(url: String, filePath: Path, priority: Int=10): File {
        val file = filePath.toFile()
        if (file.exists() && downloadQueue.none { it.url != url }) return file
        return addTask(url, file, priority).getCompleted()
    }

    private fun processQueue() {
        while (downloadQueue.isNotEmpty()) {
            semaphore.acquire()
            val task = downloadQueue.poll()
            if (task != null) {
                runBlocking {
                    println(downloadQueue.size)
                    try {
                        val file = downloadImage(task.url, task.file)
                        task.result.complete(file)
                    } catch (e: Exception) {
                        task.result.completeExceptionally(e)
                    } finally {
                        semaphore.release()
                    }
                }
            }
        }
    }
}

