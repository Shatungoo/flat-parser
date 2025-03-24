package com.helldaisy.ui

import androidx.compose.runtime.mutableStateOf
import kotlin.collections.Map
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.set


fun Filter.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
        if (dealTypes.value.isNotEmpty()) map["deal_types"] = dealTypes.value.joinToString(",")
        if (realEstateTypes.value.isNotEmpty()) map["real_estate_types"]= realEstateTypes.value.joinToString(",")
        if (cities.value.isNotEmpty()) map["cities"]= cities.value.joinToString(",")
        if (currencyId.value!= null) map["currency_id"]= currencyId.value.toString()
        if (urbans.value.isNotEmpty()) map["urbans"]= urbans.value.joinToString(",")
        if (districts.value.isNotEmpty()) map["districts"]= districts.value.joinToString(",")
        if (statuses.value.isNotEmpty()) map["statuses"]= statuses.value.joinToString(",")
        if (priceFrom.value != null) map["price_from"]= priceFrom.value.toString()
        if (priceTo.value   != null) map["price_to"]= priceTo.value.toString()
        if (areaFrom.value  != null) map["area_from"]= areaFrom.value.toString()
        if (areaTo.value    != null) map["area_to"]= areaTo.value.toString()
        if (areaTypes.value != null) map["area_types"]= areaTypes.value.toString()
    return map
}

fun Map<String, String>.toFilterDb(): Filter {
    return Filter(
        dealTypes = mutableStateOf(this["deal_types"]!!.split(",").map { it.toInt() }),
        realEstateTypes = mutableStateOf(this["real_estate_types"]!!.split(",").map { it.toInt() }),

        cities = mutableStateOf(this["cities"]!!.split(",").map { it.toInt() }),
        currencyId = mutableStateOf(this["currency_id"]!!.toInt()),
        urbans = mutableStateOf(this["urbans"]!!.split(",").map { it.toInt() }),
        districts = mutableStateOf(this["districts"]!!.split(",").map { it.toInt() }),
        statuses = mutableStateOf(this["statuses"]!!.split(",").map { it.toInt() }),
        priceFrom = mutableStateOf(this["price_from"]!!.toInt()),
        priceTo = mutableStateOf(this["price_to"]!!.toInt()),
        areaFrom = mutableStateOf(this["area_from"]!!.toInt()),
        areaTo = mutableStateOf(this["area_to"]!!.toInt()),
        areaTypes = mutableStateOf(this["area_types"]!!.toInt()),
    )
}

