package com.helldaisy.ui

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
import com.helldaisy.MutableStateSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    @Serializable
    val baseUrl: MutableState<String> = mutableStateOf(""),
    @Serializable(with = MutableStateSerializer::class)
    val dealTypes: MutableState<List<Int>> = mutableStateOf(emptyList()),
    @Serializable(with = MutableStateSerializer::class)
    val realEstateTypes: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val cities: MutableState<List<Int>> = mutableStateOf(emptyList()),
    @Serializable(with = MutableStateSerializer::class)
    val currencyId: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val urbans: MutableState<List<String>> = mutableStateOf(listOf()),
    @Serializable(with = MutableStateSerializer::class)
    val districts: MutableState<List<String>> = mutableStateOf(emptyList()),
    @Serializable(with = MutableStateSerializer::class)
    val statuses: MutableState<List<Int>> = mutableStateOf(emptyList()),
    @Serializable(with = MutableStateSerializer::class)
    val areaTypes: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val priceFrom: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val priceTo: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val roomsFrom: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val roomsTo: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val areaFrom: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val areaTo: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val floorFrom: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val floorTo: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val totalFloorsFrom: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val totalFloorsTo: MutableState<Int?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val city: MutableState<String?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val district: MutableState<String?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val urban: MutableState<String?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val street: MutableState<String?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val limit: MutableState<Int> = mutableStateOf(1000),
    @Serializable(with = MutableStateSerializer::class)
    val lan: MutableState<Double?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val lng: MutableState<Double?> = mutableStateOf(null),
){}


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
fun FilterDb(filter1: MutableState<Filter>, apply: () -> Unit) {
    val filter = filter1.value

    Column {
        FilterBetween("Rooms", filter.roomsFrom, filter.roomsTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Floor", filter.floorFrom, filter.floorTo)
        FilterBetween("Total floors", filter.totalFloorsFrom, filter.totalFloorsTo)
//        FilterBetween("Coordinates", filter.lan as MutableState<Int?>, filter.lng as MutableState<Int?>)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Limit", style = MaterialTheme.typography.h3, modifier = Modifier.width(100.dp))

            Spacer(modifier = Modifier.width(10.dp))
            FilterValueInt(filter.limit as MutableState<Int?>)
        }
        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = {
            apply()
            filterDbView.value = false
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
