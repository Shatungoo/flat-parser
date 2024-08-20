package com.helldaisy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldaisy.*


@Composable
fun FilterParser(
    filter: Filter,
    onClick: () -> Unit
    ) {
    Column(modifier = Modifier.padding(5.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        FilterValueStr("Base url", filter.baseUrl)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterWithClassifier("Deal types", filter.dealTypes, dealTypes)
        FilterExactInt("Real estate types", filter.realEstateTypes)
        FilterExactInt("Currency id", filter.currencyId)
        FilterWithClassifier("Cities", filter.cities, cities.cities)
        FilterWithClassifier("Districts", filter.districts, cities.districts(filter.cities.value.first()))
        FilterWithClassifier("Urbans", filter.urbans, urbans)
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
        "real_estate_types" to realEstateTypes.value.toString(),
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
        realEstateTypes = mutableStateOf(this["real_estate_types"]!!.toInt()),
        cities = mutableStateOf(this["cities"]!!.split(",").map { it.toInt() }),
        currencyId = mutableStateOf(this["currency_id"]!!.toInt()),
        urbans = mutableStateOf(this["urbans"]!!.split(",")),
        districts = mutableStateOf(this["districts"]!!.split(",")),
        statuses = mutableStateOf(this["statuses"]!!.split(",").map { it.toInt() }),
        priceFrom = mutableStateOf(this["price_from"]!!.toInt()),
        priceTo = mutableStateOf(this["price_to"]!!.toInt()),
        areaFrom = mutableStateOf(this["area_from"]!!.toInt()),
        areaTo = mutableStateOf(this["area_to"]!!.toInt()),
        areaTypes = mutableStateOf(this["area_types"]!!.toInt()),
    )
}

