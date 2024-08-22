package com.helldaisy

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val status = mapOf(
    1 to "Old",
    2 to "New",
    3 to "Under construction",
)

val realEstateType = mapOf(
    1 to "Flat",
    2 to "House",
    3 to "Country house",
    4 to "Land plots",
    5 to "Commercial",
    6 to "Hotels",
)

val dealTypes = mapOf(
    1 to "Sale",
    2 to "Rent",
    3 to "Mortgage",
    4 to "Daily Rent",
)

fun String.toLatin(): String {
    val georgian = mapOf(
        "ი" to "i",
        "ე" to "e",
        "ა" to "a",
        "ბ" to "b",
        "გ" to "g",
        "დ" to "d",
        "ვ" to "v",
        "ზ" to "z",
        "თ" to "t",
        "კ" to "k",
        "ლ" to "l",
        "მ" to "m",
        "ნ" to "n",
        "ო" to "o",
        "პ" to "p",
        "ჟ" to "zh",
        "რ" to "r",
        "ს" to "s",
        "ტ" to "t",
        "უ" to "u",
        "ფ" to "ph",
        "ქ" to "q",
        "ღ" to "gh",
        "ყ" to "k",
        "შ" to "sh",
        "ჩ" to "ch",
        "ც" to "ts",
        "ძ" to "dz",
        "წ" to "ts",
        "ჭ" to "ch",
        "ხ" to "kh",
        "ჯ" to "j",
        "ჰ" to "h"
    )
    val result = this.map { georgian[it.toString()] ?: it }.joinToString("")
    return result
}

val resource = object {}.javaClass.getResource("/cities.json")?.readText()
val locationsCl = Json { ignoreUnknownKeys = true }.decodeFromString<CityJson>(resource!!)


@Serializable
data class CityJson(val data: List<City>) {
    @Serializable
    data class City(val id: Int, val display_name: String, val districts: List<District>)
    @Serializable
    data class District(val id: Int, val display_name: String, val urbans: List<Urban>)
    @Serializable
    data class Urban(val id: Int, val display_name: String)

    val cities = data.associate { it.id to it.display_name }
    val districts = data.associate { it.id to it.districts.associate { it.id to it.display_name.toLatin() } }
    val urbans: Map<Int, Map<Int, Map<Int, String>>> =
        data.associate { it.id to it.districts.associate { it.id to it.urbans.associate { it.id to it.display_name.toLatin() } } }

    fun districts(cityIds: List<Int>): Map<Int, String> =
        cityIds.mapNotNull { districts[it] }.map { it.entries }.flatten().associate { it.key to it.value }

    fun urbans(cityIds: List<Int>, districtIds: List<Int>): Map<Int, String> {
        val map: MutableMap<Int, String> = mutableMapOf()
        cityIds.forEach { cityId ->
            districtIds.forEach() { districtId ->
                map.putAll(urbans[cityId]?.get(districtId) ?: emptyMap())
            }
        }
        return map
    }

    val flatDistricts by lazy {
        val map: MutableMap<Int, String> = mutableMapOf()
        cities.forEach { cityId: Map.Entry<Int, String> ->
            districts[cityId.key]?.forEach() { districtId ->
                map[districtId.key] = districtId.value
            }
        }
        map
    }

    val flatUrbans by lazy {
        val map: MutableMap<Int, String> = mutableMapOf()
        cities.keys.forEach { cityId ->
            districts[cityId]?.keys?.forEach() { districtId ->
                urbans[cityId]?.get(districtId)?.forEach() { urbanId ->
                    map[urbanId.key] = urbanId.value
                }
            }
        }
        map
    }
}
