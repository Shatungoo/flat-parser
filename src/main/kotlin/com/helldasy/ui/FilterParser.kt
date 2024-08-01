package com.helldasy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.http.*


@Composable
fun FilterParser(
    baseUrl: String,
    urlParamMap: SnapshotStateMap<String,String>, apply: () -> Unit) {

    Column {
        Text(baseUrl)
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
    }
}
