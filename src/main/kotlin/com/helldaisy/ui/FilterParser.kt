package com.helldaisy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun FilterParser(
    baseUrl: String,
    urlParamMap: SnapshotStateMap<String,String>, apply: () -> Unit) {
    val filter = FilterDb()
    Column {
        Text(baseUrl)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        urlParamMap.toSortedMap().map { entry ->
            Row(modifier = Modifier.padding(5.dp).fillMaxWidth().height(55.dp)) {
                Text(entry.key, modifier = Modifier.fillMaxHeight().width(100.dp))
                TextField(
                    value = entry.value,
                    onValueChange = { urlParamMap[entry.key] = entry.value },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
        Button(onClick = apply) { Text("Apply") }
    }
}


val urlParamMap: SnapshotStateMap<String, String> = mutableStateMapOf(
    "deal_types" to "1",
    "real_estate_types" to "1",
    "cities" to "1",
    "currency_id" to "1",
    "urbans" to "NaN,23,27,43,47,62,64",
    "districts" to "3.4,3,4,6",
    "statuses" to "2",
//    "price_from" to "50000",
//    "price_to" to "300000",
//    "area_from" to "40",
//    "area_to" to "90",
    "area_types" to "1",
)
fun FilterDb.toMap(): Map<String, String> {

    return mapOf(
//        "rooms_from" to roomsFrom.value.toString(),
//        "rooms_to" to roomsTo.value.toString(),
        "price_from" to priceFrom.value.toString(),
        "price_to" to priceTo.value.toString(),
        "area_from" to areaFrom.value.toString(),
        "area_to" to areaTo.value.toString(),
//        "floor_from" to floorFrom.value.toString(),
//        "floor_to" to floorTo.value.toString(),
//        "total_floors_from" to totalFloorsFrom.value.toString(),
//        "total_floors_to" to totalFloorsTo.value.toString(),
//        "limit" to limit.value.toString(),
    )
}
