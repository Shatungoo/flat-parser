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

val resource = object {}.javaClass.getResource("/cities.json")?.readText()
val cities = Json{ ignoreUnknownKeys = true }.decodeFromString<CityJson>(resource!!)


@Serializable data class CityJson(val data: List<City>){
    @Serializable data class City(val id: Int,val display_name: String, val districts: List<District>)
    @Serializable data class District(val id: Int, val display_name: String, val urbans: List<Urban>)
    @Serializable data class Urban(val id: Int, val display_name: String)
    public val cities = data.map { it.id to it.display_name }.toMap()
    fun districts(cityId: Int):Map<String, String> =
        data.find { it.id == cityId }?.districts?.associate { it.id.toString() to it.display_name }
        ?: emptyMap()
    fun urbans(cityId: Int, districtId: Int):Map<String, String> =
        data.find { it.id == cityId }?.districts?.find { it.id == districtId }?.urbans?.associate { it.id.toString() to it.display_name }
        ?: emptyMap()
}

val districts = mapOf(
    "1" to "Vake",
    "2" to "Saburtalo",
    "3" to "Didube",
    "4" to "Gldani",
    "5" to "Samgori",
    "6" to "Isani",
    "7" to "Chugureti",
    "8" to "Nadzaladevi",
    "9" to "Krtsanisi",
    "10" to "Mtatsminda",
    "11" to "Vazisubani",
    "12" to "Dighomi",
)
val urbans = mapOf(
    "1" to "Vake",
)
