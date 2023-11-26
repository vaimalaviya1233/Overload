package cloud.pablos.overload.ui.tabs.configurations

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.EmojiNature
import androidx.compose.material.icons.rounded.PestControl
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.room.withTransaction
import cloud.pablos.overload.R
import cloud.pablos.overload.data.item.Item
import cloud.pablos.overload.data.item.ItemDatabase
import cloud.pablos.overload.data.item.ItemState
import cloud.pablos.overload.data.item.backupItemsToCsv
import cloud.pablos.overload.ui.views.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ConfigurationsTab(state: ItemState) {
    val context = LocalContext.current
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val acraEnabledKey = "acra.enable"
    val acraSysLogsEnabledKey = "acra.syslog.enable"

    val acraEnabled = sharedPreferences.getBoolean(acraEnabledKey, true)
    val acraSysLogsEnabled = sharedPreferences.getBoolean(acraSysLogsEnabledKey, true)

    val acraEnabledState = remember(acraEnabled) {
        mutableStateOf(sharedPreferences.getBoolean(acraEnabledKey, true))
    }

    val acraSysLogsEnabledState = remember(acraSysLogsEnabled) {
        mutableStateOf(sharedPreferences.getBoolean(acraSysLogsEnabledKey, true))
    }

    val importDialogState = remember { mutableStateOf(false) }
    val workGoalDialogState = remember { mutableStateOf(false) }
    val pauseGoalDialogState = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ConfigurationsTabTopAppBar()
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Goals Title
            item {
                ConfigurationsTabItem(title = stringResource(id = R.string.goals))
            }

            // Work Goal
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        workGoalDialogState.value = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Work,
                        contentDescription = stringResource(id = R.string.work),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            ConfigurationTitle(stringResource(id = R.string.work))
                            ConfigurationDescription(stringResource(id = R.string.work_goal_descr))
                        }
                    }
                }
            }

            // Pause Goal
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        pauseGoalDialogState.value = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = stringResource(id = R.string.pause),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            ConfigurationTitle(stringResource(id = R.string.pause))
                            ConfigurationDescription(stringResource(id = R.string.pause_goal_descr))
                        }
                    }
                }
            }

            // Goals Divider
            item {
                Row {
                    HorizontalDivider()
                }
            }

            // Analytics Title
            item {
                ConfigurationsTabItem(title = stringResource(id = R.string.analytics))
            }

            // Analytics Crash Reports
            item {
                ConfigurationsTabItem(
                    title = stringResource(id = R.string.crash_reports),
                    description = stringResource(id = R.string.crash_reports_descr),
                    preferenceKey = acraEnabledKey,
                    switchState = acraEnabledState,
                    icon = Icons.Rounded.BugReport,
                )
            }

            // Analytics System Logs
            item {
                AnimatedVisibility(
                    visible = acraEnabledState.value,
                    enter = expandIn(),
                    exit = shrinkOut(),
                ) {
                    ConfigurationsTabItem(
                        title = stringResource(id = R.string.system_logs),
                        description = stringResource(id = R.string.system_logs_descr),
                        preferenceKey = acraSysLogsEnabledKey,
                        switchState = acraSysLogsEnabledState,
                        icon = Icons.Rounded.PestControl,
                    )
                }
            }

            // Analytics Divider
            item {
                Row {
                    HorizontalDivider()
                }
            }

            // Storage Title
            item {
                ConfigurationsTabItem(title = stringResource(id = R.string.storage))
            }

            // Storage Backup
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        backup(state, context)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = stringResource(id = R.string.backup),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            ConfigurationTitle(stringResource(id = R.string.backup))
                            ConfigurationDescription(stringResource(id = R.string.backup_descr))
                        }
                    }
                }
            }

            // Storage Import
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        importDialogState.value = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Unarchive,
                        contentDescription = stringResource(id = R.string.import_ol),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            ConfigurationTitle(stringResource(id = R.string.import_ol))
                            ConfigurationDescription(stringResource(id = R.string.import_descr))
                        }
                    }
                }
            }

            // Storage Divider
            item {
                Row {
                    HorizontalDivider()
                }
            }

            // About Title
            item {
                ConfigurationsTabItem(title = stringResource(id = R.string.about))
            }

            // About Source Code
            item {
                ConfigurationsTabItem(
                    title = stringResource(id = R.string.sourcecode),
                    description = stringResource(id = R.string.sourcecode_descr),
                    link = "https://codeberg.org/pabloscloud/Overload".toUri(),
                    icon = Icons.Rounded.Code,
                )
            }

            // About ITS
            item {
                ConfigurationsTabItem(
                    title = stringResource(id = R.string.issue_reports),
                    description = stringResource(id = R.string.issue_reports_descr),
                    link = "https://codeberg.org/pabloscloud/Overload/issues".toUri(),
                    icon = Icons.Rounded.EmojiNature,
                )
            }

            // About Translations
            item {
                ConfigurationsTabItem(
                    title = stringResource(id = R.string.translate),
                    description = stringResource(id = R.string.translate_descr),
                    link = "https://translate.codeberg.org/engage/overload".toUri(),
                    icon = Icons.Rounded.Translate,
                )
            }

            // About License
            item {
                ConfigurationsTabItem(
                    title = stringResource(id = R.string.license),
                    description = stringResource(id = R.string.license_descr),
                    link = "https://codeberg.org/pabloscloud/Overload/raw/branch/main/LICENSE".toUri(),
                    icon = Icons.Rounded.Copyright,
                )
            }

            // About Divider
            item {
                Row {
                    HorizontalDivider()
                }
            }

            // Footer
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                ) {
                    ConfigurationDescription(stringResource(id = R.string.footer))
                }
            }
        }
        if (importDialogState.value) {
            ConfigurationsTabImportDialog(onClose = { importDialogState.value = false })
        }

        if (workGoalDialogState.value) {
            ConfigurationsTabGoalDialog(onClose = { workGoalDialogState.value = false }, isPause = false)
        }

        if (pauseGoalDialogState.value) {
            ConfigurationsTabGoalDialog(onClose = { pauseGoalDialogState.value = false }, isPause = true)
        }
    }
}

fun backup(state: ItemState, context: Context) {
    try {
        val exportedData = backupItemsToCsv(state)
        val cachePath = File(context.cacheDir, "backup.csv")

        cachePath.writeText(exportedData)

        val contentUri = FileProvider.getUriForFile(
            context,
            "cloud.pablos.overload.fileprovider",
            cachePath,
        )

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "text/comma-separated-values"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Backup and share failed", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

fun handleIntent(intent: Intent?, lifecycleScope: LifecycleCoroutineScope, db: ItemDatabase, context: Context, contentResolver: ContentResolver) {
    if (intent != null && intent.action == Intent.ACTION_SEND && intent.type == "text/comma-separated-values") {
        if (intent.getStringExtra(Intent.EXTRA_TEXT)?.isBlank() == false) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                importCsvData(
                    sharedText,
                    lifecycleScope,
                    db,
                    context,
                )
            }
        } else if (intent.getStringExtra(Intent.EXTRA_STREAM)?.isBlank() == false) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            uri?.let {
                importFile(uri, contentResolver, context, db, lifecycleScope)
            }
        } else if (intent.clipData != null) {
            val uri = intent.clipData?.getItemAt(0)?.uri
            if (uri != null) {
                importFile(uri, contentResolver, context, db, lifecycleScope)
            } else {
                showImportFailedToast(context)
            }
        } else {
            showImportFailedToast(context)
        }
    }
}

private fun importCsvData(csvData: String, lifecycleScope: LifecycleCoroutineScope, db: ItemDatabase, context: Context) {
    val parsedData = parseCsvData(csvData)

    lifecycleScope.launch(Dispatchers.IO) {
        val itemDao = db.itemDao()

        var allImportsSucceeded = true

        db.withTransaction {
            parsedData.drop(1).forEach { row ->
                if (row.size >= 4) {
                    val startTime = row[1].trim()
                    val endTime = row[2].trim()
                    val ongoing = row[3].trim()
                    val pause = row[4].trim()

                    val item = Item(
                        startTime = startTime,
                        endTime = endTime,
                        ongoing = ongoing.toBoolean(),
                        pause = pause.toBoolean(),
                    )

                    val importResult = itemDao.upsertItem(item)
                    if (importResult != Unit) {
                        allImportsSucceeded = false
                    }
                }
            }
        }

        withContext(Dispatchers.Main) {
            if (allImportsSucceeded) {
                showImportSuccessToast(context)
            } else {
                showImportFailedToast(context)
            }
        }
    }
}

fun parseCsvData(csvData: String): List<List<String>> {
    val rows = csvData.split("\n")
    return rows.map { row ->
        val separator = when {
            row.contains(',') -> ","
            row.contains(';') -> ";"
            else -> ","
        }

        row.split(separator)
    }
}

fun showImportSuccessToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_SHORT).show()
}

fun showImportFailedToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.import_failure), Toast.LENGTH_SHORT).show()
}

private fun importFile(uri: Uri, contentResolver: ContentResolver, context: Context, db: ItemDatabase, lifecycleScope: LifecycleCoroutineScope) {
    uri.let {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val sharedData = inputStream.bufferedReader().readText()

            importCsvData(sharedData, lifecycleScope, db, context)
        }
    }
}

@Composable
fun ConfigurationLabel(
    text: String,
) {
    TextView(
        text = text,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun ConfigurationTitle(
    text: String,
) {
    TextView(
        text = text,
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun ConfigurationDescription(
    text: String,
) {
    Text(
        text = text,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

class OlSharedPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("ol_prefs", Context.MODE_PRIVATE)

    fun saveWorkGoal(goal: Int) {
        sharedPreferences.edit { putInt("workGoal", goal) }
    }

    fun savePauseGoal(goal: Int) {
        sharedPreferences.edit { putInt("pauseGoal", goal) }
    }

    fun getWorkGoal(): Int = sharedPreferences.getInt("workGoal", 0)

    fun getPauseGoal(): Int = sharedPreferences.getInt("pauseGoal", 0)
}
