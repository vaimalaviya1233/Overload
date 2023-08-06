package cloud.pablos.overload.ui.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cloud.pablos.overload.R
import cloud.pablos.overload.data.item.ItemEvent
import cloud.pablos.overload.data.item.ItemState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DayView(
    state: ItemState,
    onEvent: (ItemEvent) -> Unit,
) {
    val date = when (state.selectedDay) {
        "" -> LocalDate.now()
        else -> getLocalDate(state.selectedDay)
    }

    val itemsForSelectedDay = state.items.filter { item ->
        val startTime = parseToLocalDateTime(item.startTime)
        extractDate(startTime) == date
    }

    val itemsForSelectedDayAsc = itemsForSelectedDay.sortedByDescending { it.startTime }

    if (itemsForSelectedDayAsc.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            val itemSize = itemsForSelectedDayAsc.size
            items(itemSize) { index ->
                val isFirstItem = index == 0
                val isLastItem = index == itemSize - 1

                val item = itemsForSelectedDayAsc[index]
                val isSelected = state.selectedItems.contains(item)
                Box(
                    modifier = Modifier
                        .padding(10.dp, if (isFirstItem) 10.dp else 0.dp, 10.dp, if (isLastItem) 80.dp else 10.dp)
                        .combinedClickable(
                            onLongClick = {
                                onEvent(ItemEvent.SetIsDeleting(true))
                                onEvent(ItemEvent.SetSelectedItems(listOf(item)))
                            },
                            onClick = {
                                when (state.isDeleting) {
                                    true -> when (isSelected) {
                                        true -> onEvent(ItemEvent.SetSelectedItems(state.selectedItems.filterNot { it == item }))
                                        else -> onEvent(ItemEvent.SetSelectedItems(state.selectedItems + listOf(item)))
                                    }
                                    else -> {}
                                }
                            },
                        ),
                ) {
                    when (!item.ongoing && item.endTime.isNotBlank()) {
                        true -> DayViewItemNotOngoing(item, isSelected = isSelected)
                        else -> DayViewItemOngoing(item, isSelected = isSelected)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(id = R.string.no_items_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(id = R.string.no_items_subtitle),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseToLocalDateTime(dateTimeString: String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    return LocalDateTime.parse(dateTimeString, formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getLocalDate(selectedDay: String): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(selectedDay, formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun extractDate(localDateTime: LocalDateTime): LocalDate {
    return localDateTime.toLocalDate()
}
/*
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun DayViewPreview(){
    DayView()
}*/
