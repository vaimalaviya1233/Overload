import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reply.R
import com.example.reply.data.ItemEvent
import com.example.reply.data.ItemState
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReplyNavigationFab(
    state: ItemState,
    onEvent: (ItemEvent) -> Unit
) {
    var isOngoing = state.items.isNotEmpty() && state.items.last().ongoing

    NavigationDrawerItem(
        selected = false,
        label = {
            Text(
                if (isOngoing)
                    "Pause"
                else
                    "Start"
            )
        },
        icon = {
            Icon(
                imageVector =
                if (isOngoing)
                    Icons.Default.Pause
                else
                    Icons.Default.PlayArrow,
                contentDescription =
                if (isOngoing)
                    "Pause"
                else
                    "Start"
            )
        },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        onClick = {
            var isFirst = state.items.isEmpty()
            var isNotOngoing = state.items.isEmpty() || !state.items.last().ongoing

            if (isFirst) {
                onEvent(ItemEvent.SetStart(start = LocalDateTime.now().toString()))
                onEvent(ItemEvent.SetOngoing(ongoing = true))
                onEvent(ItemEvent.SetPause(pause = false))
                onEvent(ItemEvent.SaveItem)

                onEvent(ItemEvent.SetIsOngoing(isOngoing = true))
            }
            else if (isNotOngoing) {
                onEvent(ItemEvent.SetStart(start = state.items.last().endTime))
                onEvent(ItemEvent.SetEnd(end = LocalDateTime.now().toString()))
                onEvent(ItemEvent.SetOngoing(ongoing = false))
                onEvent(ItemEvent.SetPause(pause = true))
                onEvent(ItemEvent.SaveItem)

                onEvent(ItemEvent.SetStart(start = LocalDateTime.now().toString()))
                onEvent(ItemEvent.SetOngoing(ongoing = true))
                onEvent(ItemEvent.SetPause(pause = false))
                onEvent(ItemEvent.SaveItem)

                onEvent(ItemEvent.SetIsOngoing(isOngoing = true))
            } else {
                onEvent(ItemEvent.SetId(id = state.items.last().id))
                onEvent(ItemEvent.SetStart(start = state.items.last().startTime))
                onEvent(ItemEvent.SetEnd(end = LocalDateTime.now().toString()))
                onEvent(ItemEvent.SetOngoing(ongoing = false))
                onEvent(ItemEvent.SaveItem)

                onEvent(ItemEvent.SetIsOngoing(isOngoing = false))
            }
        },
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp),
    )
}

/*@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun HomeTabPreview() {
    HomeTab()
}
*/