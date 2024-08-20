@file:OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)

package com.helldaisy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helldaisy.MutableStateSerializer
import com.helldaisy.cities
import com.helldaisy.dealTypes
import com.helldaisy.status
import io.ktor.http.*
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
    val urbans: MutableState<List<Int>> = mutableStateOf(listOf()),
    @Serializable(with = MutableStateSerializer::class)
    val districts: MutableState<List<Int>> = mutableStateOf(emptyList()),
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
    val street: MutableState<String?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val limit: MutableState<Int> = mutableStateOf(1000),

    @Serializable(with = MutableStateSerializer::class)
    val lanFrom: MutableState<Double?> = mutableStateOf(null),
    @Serializable(with = MutableStateSerializer::class)
    val lanTo: MutableState<Double?> = mutableStateOf(null),

    @Serializable(with = MutableStateSerializer::class)
    val lngFrom: MutableState<Double?> = mutableStateOf(null),

    @Serializable(with = MutableStateSerializer::class)
    val lngTo: MutableState<Double?> = mutableStateOf(null),

    @Serializable(with = MutableStateSerializer::class)
    val lastUpdated: MutableState<Int> = mutableStateOf(7),
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

    Column(modifier = Modifier.padding(5.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        FilterBetween("Rooms", filter.roomsFrom, filter.roomsTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Floor", filter.floorFrom, filter.floorTo)
        FilterBetween("Total floors", filter.totalFloorsFrom, filter.totalFloorsTo)
        FilterWithClassifier("Deal types", filter.dealTypes, dealTypes)
        FilterWithClassifier("Statuses", filter.statuses, status)
        FilterWithClassifier("Cities", filter.cities, cities.cities)
        if (filter.cities.value.isNotEmpty()) {
            FilterWithClassifier("Districts", filter.districts, cities.districts(filter.cities.value.first()))
            if (filter.districts.value.isNotEmpty()) {
                FilterWithClassifier(
                    "Urbans", filter.urbans,
                    cities.urbans(filter.cities.value.first(),
                        filter.districts.value
                    ))
            }
        }
        FilterExactInt("Updated, d", filter.lastUpdated as MutableState<Int?>)
        FilterExactInt("Limit", filter.limit as MutableState<Int?>)
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
        Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            FilterValueInt(from)
            FilterValueInt(to)
        }
    }
}


@Composable
fun FilterValueInt(value: MutableState<Int?>, modifier: Modifier = Modifier) {
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
fun FilterExactInt(name: String, value: MutableState<Int?>, modifier: Modifier = Modifier) {
    FilterText(name) {
        FilterValueInt(value, modifier)
    }
}

@Composable
fun <T> ClassifierAdd(classifier: Map<T, String>, values: MutableState<List<T>>, close: () -> Unit = {}) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier
            .background(MaterialTheme.colors.surface, RoundedCornerShape(5.dp))
            .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(5.dp))
    ) {
        for (value in classifier.keys) {
            if (value !in values.value) {
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(5.dp))
                ) {
                    Text(
                        classifier[value] ?: "Unknown($value)",
                        modifier = Modifier
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                            .onClick(onClick = {
                                values.value += value
                                close()
                            }),
                        style = LocalTextStyle.current.copy(baselineShift = BaselineShift(-0.3f))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagBtn(name: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.height(25.dp).border(
            1.dp,
            MaterialTheme.colors.primary,
            shape = RoundedCornerShape(5.dp)
        ).padding(5.dp),
        verticalAlignment = Alignment.Top,

        ) {
        Text(
            name,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxHeight(),
            maxLines = 1,
            style = LocalTextStyle.current.copy(baselineShift = BaselineShift(-0.6f)),
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            modifier = Modifier.padding(start = 5.dp).onClick(onClick = onClick)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FilterWithClassifier(name: String, values: MutableState<List<T>>, classifier: Map<T, String>) {
    var open by remember { mutableStateOf(false) }
    FilterText(name) {
        FlowRow(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (value in values.value) {
                TagBtn(classifier[value] ?: "Unknown($value)", onClick = {
                    values.value = values.value.filter { it != value }
                })
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.padding(start = 5.dp)
                    .onClick(onClick = {
                        open = !open
                    })
            )
        }

    }
    if (open) ClassifierAdd(classifier, values, close = { open = false })
}


@Composable
fun FilterText(
    name: String,
    modifier: Modifier = Modifier.padding(5.dp).fillMaxWidth(),
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(name, style = MaterialTheme.typography.h3, modifier = Modifier.height(50.dp).width(100.dp))
        content(this)
    }
}
