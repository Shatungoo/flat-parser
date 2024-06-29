import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

@Serializable
class Settings(
    var darkTheme: Boolean = false
) {

}


private val settingsFile: () -> File
    get() = {
        val userHome = System.getProperty("user.home")
        val osName = System.getProperty("os.name").lowercase()
        val settingsPath = when {
            "windows" in osName -> Paths.get(userHome, "AppData", "Local", appName)
            "mac" in osName -> Paths.get(userHome, "Library", "Application Support", appName)
            else -> Paths.get(userHome, ".config", appName, settingsFileName)
        }
        if (!settingsPath.toFile().exists())
            settingsPath.toFile().mkdirs()

        Paths.get(settingsPath.toString(), settingsFileName).toFile()
    }

fun loadSettings(): Settings {
    println(settingsFile().absolutePath)
    return if (settingsFile().exists()) {
        Json.decodeFromString(settingsFile().readText())
    } else {
        Settings() // Return default settings if file doesn't exist
    }
}

fun Settings.saveSettings() {
    settingsFile().writeText(Json.encodeToString(this))
}
