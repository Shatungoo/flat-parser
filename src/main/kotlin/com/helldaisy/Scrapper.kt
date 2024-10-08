package com.helldaisy

import com.fasterxml.jackson.annotation.JsonIgnore
import com.helldaisy.ui.Filter
import com.helldaisy.ui.toMap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


fun main() {
//        val a = runBlocking { getUser(1887702) }
//        println(a)
    val a = runBlocking { getReports(18909004) }
    println(a)
//    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
//    val db = Db(path)
//
//    val flats = runBlocking {
//        getFlats(
//
//            mapOf(
//                "deal_types" to "1",
//                "real_estate_types" to "1",
//                "cities" to "1",
//                "currency_id" to "1",
//                "urbans" to "10,23,27,43,47,62,64",
//                "districts" to "1,3,4,6",
//                "statuses" to "2",
//                "price_from" to "50000",
//                "price_to" to "300000",
//                "area_from" to "40",
//                "area_to" to "90",
//                "area_types" to "1",
//            ).toFilterDb().apply {
//                limit.value = 2
//            },
//        )
//    }
}

fun updateDb(
    db: Db,
    filter: Filter,
    cb: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.Default).launch {
        val response = runBlocking { getFlats(filter) }
        db.insertFlats(response)
        cb()
    }
}

fun Response.Flat.toFlatString(): String = json.encodeToString(Response.Flat.serializer(), this)

val json by lazy {
    Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}

suspend fun getFlats(
    filter: Filter,
): List<Response.Flat> {
    val urlParamMap = filter.toMap()
    val count = filter.limit.value
    val result = coroutineScope {
        (0..count).map { n ->
            async {
                getFlatsPage(urlParamMap, n)
            }
        }
    }.awaitAll()
    return result.flatMap { json.decodeFromString<Response>(it).data.data }
}


suspend fun get(
    baseUrl: String,
    urlParamMap: Map<String, String> = emptyMap(),
): String {
    val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
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

suspend fun getFlatsPage(
    urlParamMap: Map<String, String>,
    page: Int = 0,
): String {
    val baseUrl = "https://api-statements.tnet.ge/v1/statements"
    println("Getting page $page")
    val map = urlParamMap.toMutableMap()
    map["page"] = page.toString()
    return get(
        baseUrl,
        map.toMap(),
    )
}

suspend fun downloadImage(url: String): ByteArray? {
    val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
    }
    println("Downloading image from $url")
    try {
        val response = client.get(url)
//        response.bodyAsChannel().copyAndClose(file.writeChannel())
        val responseBody: ByteArray = response.body()
        return responseBody
    }catch (e: CancellationException){
        println("Cancel downloading image $url")
    } catch (e: Exception) {
        println("Error downloading image $url")
    }
    return null
}

@Serializable
data class Response(
    val data: Data,
    val result: Boolean,
) {
    @Serializable
    data class Data(val data: List<Flat>)

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
    ) {
        override fun toString(): String = json.encodeToString(serializer(), this)
        @get:JsonIgnore
        val imagesUrl: List<String> get() = images.mapNotNull { it.large_webp }.ifEmpty {
            images.mapNotNull { it.large } }
        @get:JsonIgnore
        val thumbsUrl: List<String> get() = images.mapNotNull { it.thumb_webp }.ifEmpty {
            images.mapNotNull { it.thumb }
        }
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


suspend fun search(text: String): String {
    val baseUrl = "https://api2.myhome.ge/api/ru/loc/suggestions?q=$text"
    return get(baseUrl)
}

//https://api-statements.tnet.ge/v1/statements/phone/show?statement_uuid=e77acb65-f185-4d2f-810d-692f41b39e19
//need token in body
suspend fun getPhones(statementId: String): String {
    val baseUrl = "https://api-statements.tnet.ge/v1/statements/phone/show?statement_uuid=$statementId"
    val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
    }

    val req = client.post(
        baseUrl
    ) {
        headers {
            append("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:130.0) Gecko/20100101 Firefox/130.0")
            append("Accept", "application/json, text/plain, */*")
            append("Accept-Language", "en,en-US;q=0.7,ru;q=0.3")
            append("Accept-Encoding", "gzip, deflate, zstd")
            append("Content-Type", "application/json")
            append("X-Website-Key", "myhome")
            append("Origin", "https://www.myhome.ge")
            append("Connection", "keep-alive")
            append("Referer", " https://www.myhome.ge/")
            append("Sec-Fetch-Dest", "empty")
            append("Sec-Fetch-Mode", "cors")
            append("Sec-Fetch-Site", "cross-site")
        }
    }.request
    val response = req.call
    return response.body()
}

// user contains flats of the user
suspend fun getUser(userId: Int): String {
    val baseUrl = "https://api-statements.tnet.ge/v1/statements?users=$userId"
    return get(baseUrl)
}

//currently used built-in classifier
suspend fun getCities(urlParamMap: Map<String, String>): String {
    val baseUrl = "https://api2.myhome.ge/api/ru/loc/cities"
    return get(baseUrl, urlParamMap)
}

//https://api2.myhome.ge/api/ru/collection/reports/?product_id=18909004&token=undefined
//https://api2.myhome.ge/api/ru/collection/reports/?product_id=1890902204&token=undefined
suspend fun getReports(productId: Int): String {
    val baseUrl = "https://api2.myhome.ge/api/ru/collection/reports/?product_id=$productId&token=undefined"
    return get(baseUrl)
}
