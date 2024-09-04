package com.helldaisy

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.helldaisy.ui.Filter
import com.helldaisy.ui.toFilterDb
import com.helldaisy.ui.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths


val SearchParams: Map<String, String> = mapOf(
    "deal_types" to "1",
    "real_estate_types" to "1",
    "cities" to "1",
    "currency_id" to "1",
    "urbans" to "23,27,43,47,62,64",
    "districts" to "3,4,6",
    "statuses" to "2",
    "price_from" to "50000",
    "price_to" to "300000",
    "area_from" to "40",
    "area_to" to "90",
    "area_types" to "1",
)

@Serializable
data class Settings(

    val filterDb: Filter = Filter(),

    val filterParser: Filter = SearchParams.toFilterDb().apply {
        limit.value = 2
    },

    @Serializable(with = MutableStateSerializer::class)
    val darkTheme: MutableState<Boolean> = mutableStateOf(false),

    @Serializable(with = MutableStateSerializer::class)
    val theme: MutableState<String> = mutableStateOf("Default"),


    @Transient
    val favorites: SnapshotStateList<Int> = mutableStateListOf(),
) {
    @Transient
    private val dbPath: String = Paths.get(settingsPath, "flats").toAbsolutePath().toString()

    val db by lazy { Db(dbPath) }

}


val settingsPath by lazy {
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()
    val settingsPath1 = when {
        "windows" in osName -> Paths.get(userHome, "AppData", "Local", appName)
        "mac" in osName -> Paths.get(userHome, "Library", "Application Support", appName)
        else -> Paths.get(userHome, ".config", appName, settingsFileName)
    }
    if (!settingsPath1.toFile().exists())
        settingsPath1.toFile().mkdirs()
    settingsPath1.toString()
}

private val settingsFile: () -> File
    get() = {
        Paths.get(settingsPath, settingsFileName).toFile()
    }

fun loadSettings(): Settings {
    println("loadSettings: " + settingsFile().absolutePath)
    return if (settingsFile().exists()) {
        Json { ignoreUnknownKeys = true }.decodeFromString(settingsFile().readText())
    } else {
        Settings() // Return default settings if file doesn't exist
    }
}

fun Settings.saveSettings() {
    println("SaveSettings")
    settingsFile().writeText(Json { prettyPrint = true }.encodeToString(this))
}

class MutableStateSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<MutableState<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: MutableState<T>) = dataSerializer.serialize(encoder, value.value)
    override fun deserialize(decoder: Decoder) = mutableStateOf(dataSerializer.deserialize(decoder))
}

class SnapshotListSerializer<T>(private val dataSerializer: KSerializer<T>) :
    KSerializer<SnapshotStateList<T>> {

    override val descriptor: SerialDescriptor = ListSerializer(dataSerializer).descriptor
    override fun serialize(encoder: Encoder, value: SnapshotStateList<T>) {
        encoder.encodeSerializableValue(ListSerializer(dataSerializer), value as List<T>)
    }

    override fun deserialize(decoder: Decoder): SnapshotStateList<T> {
        val list = mutableStateListOf<T>()
        val items = decoder.decodeSerializableValue(ListSerializer(dataSerializer))
        list.addAll(items)
        return list
    }
}
