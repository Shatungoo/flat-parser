package com.helldaisy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldaisy.*


@Composable
fun FilterParser(
    filter: Filter,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(5.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterWithClassifier("Deal types", filter.dealTypes, dealTypes)
        FilterWithClassifier("Real estate types", filter.realEstateTypes, realEstateType)
        FilterExactInt("Currency id", filter.currencyId)
        FilterWithClassifier("Cities", filter.cities, locationsCl.cities)
        if (filter.cities.value.isNotEmpty()) {
            FilterWithClassifier("Districts", filter.districts,
                filter.cities.value.mapNotNull {
                    locationsCl.districts[it]
                }.map {it.entries }.flatten().associate { it.key to it.value }
            )
            if (filter.districts.value.isNotEmpty()) {
                FilterWithClassifier(
                    "Urbans", filter.urbans,
                    locationsCl.urbans(filter.cities.value, filter.districts.value)
                )
            }
        }
        FilterWithClassifier("Statuses", filter.statuses, status)
        FilterExactInt("Area types", filter.areaTypes)
        FilterExactInt("Pages", filter.limit as MutableState<Int?>)
        Button(onClick = { onClick() }) {
            Text("Apply")
        }
    }
}


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

