package com.helldasy

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.helldasy.ui.FilterDb
import com.helldasy.ui.FilterParser
import com.helldasy.ui.buildQuery
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

@Serializable
data class Settings(

    @Transient
    val view: MutableState<Views> = mutableStateOf(Views.Main),

    @Transient
    val filterDb: MutableState<FilterDb> = mutableStateOf(FilterDb()),

//    @Transient
//    val filterParser: MutableState<FilterParser> = mutableStateOf(FilterParser()),

    @Serializable(with = MutableStateSerializer::class)
    val darkTheme: MutableState<Boolean> = mutableStateOf(false),

    @Serializable(with = MutableStateSerializer::class)
    val theme: MutableState<String> = mutableStateOf("Default"),

    @Serializable(with = MutableStateSerializer::class)
    val link: MutableState<String> = mutableStateOf(""),

    val dbPath: String = settingsPath + " ",

    @Transient val db: Db = Db(),

    @Transient
    val baseUrl:String = "https://api-statements.tnet.ge/v1/statements",

    @Transient
    val urlParamMap: SnapshotStateMap<String, String> = mutableStateMapOf(
        "deal_types" to "1",
        "real_estate_types" to "1",
        "cities" to "1",
        "currency_id" to "1",
        "urbans" to "NaN,23,27,43,47,62,64",
        "districts" to "3.4,3,4,6",
        "statuses" to "2",
        "price_from" to "50000",
        "price_to" to "300000",
        "area_from" to "40",
        "area_to" to "90",
        "area_types" to "1",
    ),

//    @Transient
//    val query: MutableState<String> = mutableStateOf("SELECT * from FLATS order by LAST_UPDATED DESC limit 100"),

    @Transient
    val flats: MutableState<List<Response.Flat>> = mutableStateOf(db.getFlats(query = filterDb.value.buildQuery()))
)

private val settingsPath: String
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
        settingsPath.toString()
    }.toString()

private val settingsFile: () -> File
    get() = {
        Paths.get(settingsPath, settingsFileName).toFile()
    }

fun loadSettings(): Settings {
    println("loadSettings" +settingsFile().absolutePath)
    return if (settingsFile().exists()) {
        Json.decodeFromString(settingsFile().readText())
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
