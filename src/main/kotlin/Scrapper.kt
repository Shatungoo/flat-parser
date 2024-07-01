import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
//    val a =getFlatsWithKtor(0)
//    print (a)
//   print(getFlats(0))
//    val flats = File("response.json").readText().let {
//        json.decodeFromString<Response>(it).data.data
//    }
    val db = Db()
    for (i in 0..10){
        println("page $i")
        val response = runBlocking {  getFlatsWithKtor(i) }
         json.decodeFromString<Response>(response).data.data.forEach {
            db.insertFlat(it)
         }
        }
    }

fun Response.Flat.toFlatString(): String = json.encodeToString(Response.Flat.serializer(), this)

val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

suspend fun getFlatsWithKtor(page:Int= 0): String {
    val client = HttpClient(CIO){
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
    }
    val response = client.get("https://api-statements.tnet.ge/v1/statements?" +
            "deal_types=1&real_estate_types=1&cities=1&currency_id=1&urbans=NaN,23,27,43,47,62,64&districts=3.4,3,4,6" +
            "&statuses=2&price_from=50000&price_to=300000&area_from=40&area_to=90&area_types=1&page=$page"){
        headers {
                append("User-Agent", " Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0")
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
    }
    return response.body()
}

@Serializable
data class Response(val data: Data,
                    val result: Boolean,){
    @Serializable data class Data(
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
        val yard_area: String?,
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
        val user_statements_count: Int?
    )

    @Serializable
    data class PriceDetails(
        val price_total: Int?,
        val price_square: Int?
    )

    @Serializable
    data class Image(
        val large: String?,
        val thumb: String?,
        val large_webp: String?,
        val thumb_webp: String?
    )
}