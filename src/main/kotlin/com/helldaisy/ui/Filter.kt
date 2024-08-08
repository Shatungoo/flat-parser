package com.helldaisy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import com.helldaisy.MutableStateSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    @Serializable(with = MutableStateSerializer::class)
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
) {}


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
fun FilterDb(filter: Filter, apply: () -> Unit) {

    Column {
        FilterBetween("Rooms", filter.roomsFrom, filter.roomsTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Floor", filter.floorFrom, filter.floorTo)
        FilterBetween("Total floors", filter.totalFloorsFrom, filter.totalFloorsTo)
        FilterText("Limit") {
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
    FilterText(name) {
            FilterValueInt(from, Modifier.weight(1f))
            FilterValueInt(to, Modifier.weight(1f))
    }
}


@Composable
fun FilterValueStr(name: String, value: MutableState<String>, modifier: Modifier = Modifier.fillMaxSize()) {
    FilterText(name) {
        TextField(
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(-0.5f)),
            modifier = modifier,
            value = value.value,
            onValueChange = {
                value.value = it
            })
    }
}


@Composable
fun FilterValueInt(value: MutableState<Int?>, modifier: Modifier = Modifier.fillMaxSize()) {
    TextField(
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(-0.5f)),
        modifier = modifier,
        value = if (value.value != null) value.value.toString() else "",
        onValueChange = { it ->
            value.value =
                it.filter { it.isDigit() }.let { if (it.isEmpty()) null else it.toInt() }
        })
}

@Composable
fun FilterExactLstStr(name: String, value: MutableState<List<String>>) {
    FilterText(name) {
        TextField(
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(-0.5f)),
            value = value.value.joinToString(","),
            onValueChange = { value.value = it.split(",") },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FilterExactInt(name: String, value: MutableState<Int?>) {
    FilterText(name) {
        FilterValueInt(value)
    }
}

@Composable
fun FilterExactLstInt(name: String, value: MutableState<List<Int>>) {
    FilterText(name) {
        TextField(
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(-0.5f)),
            value = value.value.joinToString(","),
            onValueChange = { value.value = it.split(",")
                .map { it.trim().toInt() }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FilterText(name: String, content: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier.padding(5.dp).fillMaxWidth().height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(name, style = MaterialTheme.typography.h3, modifier = Modifier.width(100.dp))
        content(this)
    }
}
