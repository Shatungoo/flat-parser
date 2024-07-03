import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

fun main() {
    val id = "123"
    val url = "https://api-statements.tnet.ge/uploads/statements/1mcs02f666c7d4f87f6e_thumb.jpg"
    getFile(id, url)
}

fun getFile(id: String, url: String): File? {
    try {
        val dir = getTemporalDirectory(id)
        val fileName = getFileNameFromUrl(url)
        val file = Paths.get(dir.absolutePath, fileName).toFile()
        if (!file.exists()) {
            return runBlocking {
                downloadImage(url, file)
            }
        }
        return file
    } catch (e: Exception) {
        println("Error uploading image: $url ${e.message}")
        return null
    }
}

fun getTemporalDirectory(id: String): File {
    val path = Paths.get(System.getProperty("java.io.tmpdir"), appName, id)
    val file = path.toFile()
    file.mkdirs()
    return file
}

//https://api-statements.tnet.ge/uploads/statements/1mcs02f666c7d4f87f6e_thumb.jpg
fun getFileNameFromUrl(url: String): String {
    val parts = url.split("/")
    return parts[parts.size - 1]
}

fun checkFileExists(path: String): Boolean {
    val file = File(path)
    return file.exists()
}