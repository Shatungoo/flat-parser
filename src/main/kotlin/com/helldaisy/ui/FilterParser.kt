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
                FilterWithClassifier("Districts", filter.districts,
                    filter.cities.value.mapNotNull {
                        locationsCl.districts[it]
                    }.map {it.entries }.flatten().associate { it.key to it.value })
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
    return mapOf(
        "deal_types" to dealTypes.value.joinToString(","),
        "real_estate_types" to realEstateTypes.value.joinToString(","),
        "cities" to cities.value.joinToString(","),
        "currency_id" to currencyId.value.toString(),
        "urbans" to urbans.value.joinToString(","),
        "districts" to districts.value.joinToString(","),
        "statuses" to statuses.value.joinToString(","),
        "price_from" to priceFrom.value.toString(),
        "price_to" to priceTo.value.toString(),
        "area_from" to areaFrom.value.toString(),
        "area_to" to areaTo.value.toString(),
        "area_types" to areaTypes.value.toString(),
    )
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

