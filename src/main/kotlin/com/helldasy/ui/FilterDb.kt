@file:Suppress("UNUSED_EXPRESSION")

package com.helldasy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class FilterDb(
    val priceFrom: MutableState<Int?> = mutableStateOf(null),
    val priceTo: MutableState<Int?> = mutableStateOf(null),
    val roomsFrom: MutableState<Int?> = mutableStateOf(null),
    val roomsTo: MutableState<Int?> = mutableStateOf(null),
    val areaFrom: MutableState<Int?> = mutableStateOf(null),
    val areaTo: MutableState<Int?> = mutableStateOf(null),
    val floorFrom: MutableState<Int?> = mutableStateOf(null),
    val floorTo: MutableState<Int?> = mutableStateOf(null),
    val totalFloorsFrom: MutableState<Int?> = mutableStateOf(null),
    val totalFloorsTo: MutableState<Int?> = mutableStateOf(null),
    val city: MutableState<String?> = mutableStateOf(null),
    val district: MutableState<String?> = mutableStateOf(null),
    val urban: MutableState<String?> = mutableStateOf(null),
    val street: MutableState<String?> = mutableStateOf(null),
    val limit: MutableState<Int> = mutableStateOf(100),
)

fun FilterDb.buildQuery() = run {
    var where = "where 1 = 1 ";
    if (priceFrom.value != null) where += " AND price > ${priceFrom.value} "
    if (priceTo.value != null) where += " AND price < ${priceTo.value} "
    if (roomsFrom.value != null) where += " AND rooms > ${roomsFrom.value} "
    if (roomsTo.value != null) where += " AND rooms < ${roomsTo.value} "
    if (areaFrom.value != null) where += " AND area > ${areaFrom.value} "
    if (areaTo.value != null) where += " AND area < ${areaTo.value} "
    if (floorFrom.value != null) where += " AND floor > ${floorFrom.value} "
    if (floorTo.value != null) where += " AND floor < ${floorTo.value} "
    if (totalFloorsFrom.value != null) where += " AND total_floors > ${totalFloorsFrom.value} "
    if (totalFloorsTo.value != null) where += " AND total_floors < ${totalFloorsTo.value} "
    if (city.value != null) where += " AND city_name in (${city.value}) "
    if (district.value != null) where += " AND district_name in (${district.value}) "
    if (urban.value != null) where += " AND urban_name in (${urban.value}) "
    if (city.value != null) where += " AND street_id in (${street.value}) "
    "SELECT * from FLATS $where order by LAST_UPDATED DESC limit ${limit.value}"
}

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

@Composable
fun FilterDb(filter1: MutableState<FilterDb>, apply: () -> Unit) {
    val filter = filter1.value

    Column {
        TextField(
            value = filter.buildQuery(),
            readOnly = true,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )
        FilterBetween("Rooms", filter.roomsFrom, filter.roomsTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Floor", filter.floorFrom, filter.floorTo)
        FilterBetween("Total floors", filter.totalFloorsFrom, filter.totalFloorsTo)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Limit", style = MaterialTheme.typography.h3, modifier = Modifier.width(100.dp))

            Spacer(modifier = Modifier.width(10.dp))
            FilterValueInt(filter.limit as MutableState<Int?>)
        }
        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = {
            println(filter.buildQuery())
            apply()
            filterView.value = false
        }) {
            Text("Search")
        }
    }
}

@Composable
fun FilterBetween(name: String, from: MutableState<Int?>, to: MutableState<Int?>) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(name, style = MaterialTheme.typography.h3, modifier = Modifier.width(100.dp))

        Spacer(modifier = Modifier.width(10.dp))
        FilterValueInt(from)
        Spacer(modifier = Modifier.width(10.dp))
        FilterValueInt(to)
    }
}

@Composable
fun FilterValueInt(value: MutableState<Int?>) {
    TextField(
        maxLines = 1,
        modifier = Modifier.width(100.dp)
            .height(50.dp),

        value = if (value.value != null) value.value.toString() else "",
        onValueChange = { it ->
            value.value =
                it.filter { it.isDigit() }.let { if (it.isEmpty()) null else it.toInt() }
        })
}
