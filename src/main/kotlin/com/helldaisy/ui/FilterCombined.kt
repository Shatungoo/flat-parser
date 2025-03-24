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
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import com.helldaisy.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlin.time.Duration


@Composable
fun FilterCombined(filter: Filter, apply: (filter: Filter) -> Unit) {

    Column(
        modifier = Modifier.padding(5.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        FilterBetween("Rooms", filter.roomsFrom, filter.roomsTo)
        FilterBetween("Price", filter.priceFrom, filter.priceTo)
        FilterBetween("Area", filter.areaFrom, filter.areaTo)
        FilterBetween("Floor", filter.floorFrom, filter.floorTo)
        FilterBetween("Total floors", filter.totalFloorsFrom, filter.totalFloorsTo)
        FilterWithClassifier("Deal types", filter.dealTypes, dealTypes)
        FilterWithClassifier("Statuses", filter.statuses, status)
        FilterWithClassifier("Cities", filter.cities, locationsCl.cities)
        if (filter.cities.value.isNotEmpty()) {
            FilterWithClassifier("Districts", filter.districts, locationsCl.districts(filter.cities.value))
            if (filter.districts.value.isNotEmpty()) {
                FilterWithClassifier(
                    "Urbans", filter.urbans,
                    locationsCl.urbans(filter.cities.value, filter.districts.value)
                )
            }
        }
        FilterExactInt("Updated, d", filter.lastUpdated as MutableState<Int?>)
        FilterExactInt("Limit, p", filter.limitParser as MutableState<Int?>)
        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = {
            apply(filter)
        }) {
            Text("Search")
        }
    }
}
