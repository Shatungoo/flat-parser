@file:OptIn(ExperimentalCoroutinesApi::class)

package com.helldaisy

import com.helldaisy.ui.cache
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

fun main() {
    val id = "123"
    val url = "https://api-statements.tnet.ge/uploads/statements/1mcs02f666c7d4f87f6e_thumb.jpg"
}

val byteCash = LocalCache<String, ByteArray>()
val downloader = ImageDownloader()

interface ICache<N, T> {
    fun get(key: N, block: () -> T): T
}

class LocalCache<N, T> : ICache<N, T> {
    private val csh = mutableMapOf<N, T>()
    override fun get(key: N, block: () -> T): T {
        return csh[key] ?: block().also { csh[key] = it }
    }

    fun exists(key: N): Boolean {
        return csh.containsKey(key)
    }
}

class FileCache : ICache<String, ByteArray> {

    override fun get(key: String, block: () -> ByteArray): ByteArray {
        Paths.get("").parent
        return block()
    }
}

fun getFile(imageId: String, url: String, priority: Int = 10): ByteArray? {
    val dir = getTemporalDirectory(imageId)
    val fileName = getFileNameFromUrl(url)
    try {
        val id = "$imageId-$fileName"

        return byteCash.get(id) {
            val file = Paths.get(dir.absolutePath, fileName).toFile()
            (if (file.exists()) file.readBytes()
            else runBlocking {
                downloader.download(
                    url,
                    Paths.get(dir.absolutePath, fileName),
                    priority
                )
            })

        }
    } catch (e: Exception) {
        println("Error get file image: $url ${e.message}")
        Paths.get(dir.absolutePath, fileName).toFile().delete()
        return null
    }
}

fun cacheImages(imageId: String, urls: List<String>, priority: Int = 5) {
    val dir = getTemporalDirectory(imageId)
    CoroutineScope(Dispatchers.IO.limitedParallelism(1)).launch {
        urls.forEach {
            try {
                val fileName = getFileNameFromUrl(it)
                if (byteCash.exists("$imageId-$fileName")) return@forEach
                val file = Paths.get(dir.absolutePath, fileName).toFile()
                if (file.exists()) return@forEach
                downloader.cache(it, Paths.get(dir.absolutePath, fileName), priority)
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
    private val inProgress: MutableMap<String, CompletableDeferred<ByteArray>> = mutableMapOf()

    data class DownloadTask(
        var priority: Int,
        val url: String,
        val filepath: Path,
        val result: CompletableDeferred<ByteArray>,
    ) : Comparable<DownloadTask> {
        override fun compareTo(other: DownloadTask): Int = this.priority.compareTo(other.priority)
    }

    val addTaskLock = ReentrantLock()
    private fun addTask(
        url: String,
        filePath: Path,
        priority: Int = 5,
        result: CompletableDeferred<ByteArray> = CompletableDeferred(),
    ): CompletableDeferred<ByteArray> {
        addTaskLock.withLock {
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

            downloadQueue.add(DownloadTask(priority, url, filePath, result))
            CoroutineScope(Dispatchers.IO).launch { processQueue() }
            return result
        }
    }

    fun cache(url: String, filePath: Path, priority: Int = 5) {
        addTask(url, filePath, priority)
    }

    suspend fun download(url: String, filePath: Path, priority: Int = 10): ByteArray {
        inProgress[url]?.let { return it.await() }
        downloadQueue.find { it.url == url }?.let {
            if (it.priority < priority) it.priority = priority
            return it.result.await()
        }
        return addTask(url, filePath, priority).await()
    }

    private var lock = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(5))

    private suspend fun processQueue() {
        lock.withLock {
            while (downloadQueue.isNotEmpty()) {
                scope.launch {
                    val task = downloadQueue.poll()
                    if (task != null) {
                        try {
                            inProgress[task.url] = task.result
                            val file = task.filepath.toFile()
                            if (file.exists()) return@launch
                            val image = downloadImage(task.url) ?: return@launch
                            file.writeBytes(image)
                            task.result.complete(image)
                        } catch (e: Exception) {
                            task.result.completeExceptionally(e)
                        } finally {
                            inProgress.remove(task.url)
                        }
                    }
                }
            }
        }
    }
}

