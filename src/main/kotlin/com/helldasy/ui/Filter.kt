package com.helldasy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldasy.Settings


data class Filter(
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
)

fun Filter.buildQuery() = run {
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
    "SELECT * from FLATS $where order by LAST_UPDATED DESC limit 100"
}


@Composable
fun Filters(filter1: MutableState<Filter>, apply: () -> Unit) {
    val filter= filter1.value

    Column {
        TextField(
            value = filter.buildQuery(),
            readOnly = true,
            onValueChange = {},
        )
        filterTextInt("Rooms", filter.roomsFrom, filter.roomsTo)
        filterTextInt("Price", filter.priceFrom, filter.priceTo)
        filterTextInt("Area", filter.areaFrom, filter.areaTo)
        filterTextInt("Floor", filter.floorFrom, filter.floorTo)
        filterTextInt("Total floors", filter.totalFloorsFrom, filter.totalFloorsTo)

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
fun filterTextInt(name: String, from: MutableState<Int?>, to: MutableState<Int?>) {
    Row {
        Column {
            Text(name)

            Row {
                Text("From")
                TextField(value = if (from.value != null) from.value.toString() else "",
                    onValueChange = { it ->
                        from.value =
                            it.filter { it.isDigit() }.let { if (it.isEmpty()) null else it.toInt() }
                    })
                Spacer(modifier = Modifier.width(10.dp))
                Text("To")
                TextField(
                    value = if (to.value != null) to.value.toString() else "",
                    onValueChange = { it ->
                        to.value =
                            it.filter { it.isDigit() }.let { if (it.isEmpty()) null else it.toInt() }
                    })
            }
        }
    }
}
