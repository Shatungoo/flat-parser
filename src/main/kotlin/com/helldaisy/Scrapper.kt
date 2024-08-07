package com.helldaisy

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun main() {
//
    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
    val db = Db(path)

    val flats = runBlocking {
        getFlats(
            "https://api-statements.tnet.ge/v1/statements",
            mapOf(
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
            2
        )
    }
}

fun updateDb(db: Db, cb: () -> Unit = {}) {
    CoroutineScope(Dispatchers.Default).launch {
        val response = runBlocking { getFlats(settings.baseUrl, settings.urlParamMap, 5) }
        db.insertFlats(response)
        cb()
    }
}

fun Response.Flat.toFlatString(): String = json.encodeToString(Response.Flat.serializer(), this)

val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

suspend fun getFlats(
    baseUrl: String,
    urlParamMap: Map<String, String>,
    count: Int,
): List<Response.Flat> {
    val result = coroutineScope {
        (0..count).map { n ->
            async {
                getFlatsPage(baseUrl, urlParamMap, n)
            }
        }
    }.awaitAll()
    return result.flatMap { json.decodeFromString<Response>(it).data.data }
}

suspend fun getFlatsPage(
    baseUrl: String,
    urlParamMap: Map<String, String>,
    page: Int = 0,
): String {
    println("Getting page $page")
    val client = HttpClient(CIO) {
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
    }

    val req = client.get(
        baseUrl
    ) {
        url {
            urlParamMap.forEach { (key, value) ->
                parameters.append(key, value)
            }
            parameters.append("page", page.toString())
        }
        headers {
            append(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
            )
            append("Accept", " application/json, text/plain, */*")
            append("Accept-Language", " en,en-US;q=0.7,ru;q=0.3")
            append("Accept-Encoding", " gzip, deflate")
            append("Global-Authorization", "qq ")
            append("locale", "ru")
            append("X-Website-Key", "myhome")
            append("Origin", "https://www.myhome.ge")
            append("Referer", "https://www.myhome.ge/")
            append("Sec-Fetch-Dest", "empty")
            append("Sec-Fetch-Mode", "cors")
            append("Sec-Fetch-Site", "cross-site")
        }
    }.request
    val response = req.call
    return response.body()
}

suspend fun downloadImage(url: String, file: File): File {
    val client = HttpClient(CIO) {}
    println("Downloading image from $url")
    val response = client.get(url)
    response.bodyAsChannel().copyAndClose(file.writeChannel())
    return file
}

@Serializable
data class Response(
    val data: Data,
    val result: Boolean,
) {
    @Serializable
    data class Data(
        val data: List<Flat>,
    )

    @Serializable
    data class Flat(
        val id: Int,
        val deal_type_id: Int?,
        val real_estate_type_id: Int?,
        val status_id: Int?,
        val uuid: String?,
        val price: Map<String, PriceDetails>,
        val price_negotiable: Boolean?,
        val price_from: Boolean?,
        val lat: Double?,
        val lng: Double?,
        val images: List<Image>,
        val address: String?,
        val area: Double?,
        val yard_area: Int?,
        val area_type_id: Int?,
        val bedroom: String?,
        val room: String?,
//        val gifts: List<Any>,
        val favorite: Boolean?,
        val is_old: Boolean?,
        val has_3d: Boolean?,
        val `3d_url`: String?,
        val dynamic_title: String?,
        val dynamic_slug: String?,
        val last_updated: String?,
        val floor: Int?,
        val total_floors: Int?,
        val street_id: Int?,
        val urban_id: Int?,
        val urban_name: String?,
        val district_id: Int?,
        val district_name: String?,
        val city_name: String?,
        val quantity_of_day: Int?,
        val hidden: Boolean?,
        val viewed: Boolean?,
        val user_id: Int?,
        val price_type_id: Int,
        val statement_currency_id: Int?,
        val currency_id: Int?,
        val user_title: String?,
        val grouped_street_id: Int?,
//        val parameters: List<Parameter>,
        val comment: String?,
        val has_color: Boolean?,
        val is_vip: Boolean?,
        val is_vip_plus: Boolean?,
        val is_super_vip: Boolean?,
        val user_statements_count: Int?,
    ){
        override fun toString(): String = json.encodeToString(Flat.serializer(), this)
//        fun merge(flat: Flat): Flat {
//
//        }
    }

    @Serializable
    data class PriceDetails(
        val price_total: Int?,
        val price_square: Int?,
    )

    @Serializable
    data class Image(
        val large: String?,
        val thumb: String?,
        val large_webp: String?,
        val thumb_webp: String?,
    )
}

fun String.toDate(): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(this, formatter)
}
