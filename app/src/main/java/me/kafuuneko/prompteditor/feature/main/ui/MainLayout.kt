package me.kafuuneko.prompteditor.feature.main.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.main.presentation.MainDialogState
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet
import me.kafuuneko.prompteditor.ui.dialog.ConfirmDialog
import me.kafuuneko.prompteditor.ui.dialog.InputConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    uiState: MainUiState,
    emit: MainUiIntent.() -> Unit
) {

    BackHandler { MainUiIntent.Back.emit() }
    when (uiState) {
        MainUiState.Finished, MainUiState.None -> Unit
        is MainUiState.Normal -> {
            Normal(uiState, emit)
            DialogSwitch(uiState.dialogState, emit)
        }
    }
}

@Composable
private fun DialogSwitch(
    dialogState: MainDialogState,
    emit: MainUiIntent.() -> Unit
) {
    when (dialogState) {
        MainDialogState.None -> Unit

        MainDialogState.ExitConfirm -> ConfirmDialog(
            onConfirmRequest = { MainUiIntent.ConfirmExit.emit() },
            onDismissRequest = { MainUiIntent.DismissDialog.emit() }
        ) {
            Text(stringResource(R.string.exit_app_confirmation))
        }

        MainDialogState.CreatePresetSet -> CreatePresetSetDialog(
            onDismissRequest = { MainUiIntent.DismissDialog.emit() },
            onConfirmRequest = { name, parser -> MainUiIntent.CreatePresetSet(name, parser).emit() }
        )

        is MainDialogState.DeletePresetSet -> ConfirmDialog(
            onConfirmRequest = { MainUiIntent.ConfirmDeletePreset.emit() },
            onDismissRequest = { MainUiIntent.DismissDialog.emit() },
            confirmContent = {
                Text(
                    stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        ) {
            Text(
                stringResource(R.string.delete_preset_set_confirmation, dialogState.name)
            )
        }

        is MainDialogState.RenamePresetSet -> InputConfirmDialog(
            title = stringResource(R.string.rename_preset_set),
            hintText = stringResource(R.string.preset_set_name_hint),
            defaultText = dialogState.name,
            onConfirmRequest = {
                MainUiIntent.ConfirmRenamePresetSet(dialogState.id, it).emit()
            },
            onDismissRequest = { MainUiIntent.DismissDialog.emit() }
        )
    }
}

@Composable
private fun CreatePresetSetDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: (String, Int) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var selectedParser by remember { mutableIntStateOf(0) }

    ConfirmDialog(
        onDismissRequest = onDismissRequest,
        onConfirmRequest = { onConfirmRequest(inputText, selectedParser) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.create_preset_set),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.preset_set_name_hint)) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.parser_type),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedParser == 0,
                        onClick = { selectedParser = 0 }
                    )
                    Text(text = stringResource(R.string.parser_novelai))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedParser == 1,
                        onClick = { selectedParser = 1 }
                    )
                    Text(text = stringResource(R.string.parser_sd))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Normal(
    uiState: MainUiState.Normal,
    emit: MainUiIntent.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { MainUiIntent.OpenTagsImport.emit() }) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = stringResource(R.string.import_tags)
                        )
                    }
                    IconButton(onClick = { MainUiIntent.StartCreatePreset.emit() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.create_preset_set)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.presetSets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_preset_sets),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.presetSets, key = { it.id }) { presetSet ->
                    PresetSetItem(
                        presetSet = presetSet,
                        onClick = { MainUiIntent.OpenPresetSet(presetSet.id).emit() },
                        onStartDelete = {
                            MainUiIntent.StartDeletePreset(
                                presetSet.id,
                                presetSet.name
                            ).emit()
                        },
                        onStartRename = {
                            MainUiIntent.ShowRenamePresetSetDialog(
                                presetSet.id,
                                presetSet.name
                            ).emit()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetSetItem(
    presetSet: PresetSet,
    onClick: () -> Unit,
    onStartDelete: () -> Unit,
    onStartRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = presetSet.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onStartRename()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onStartDelete()
                        }
                    )
                }
            }
        }
    }
}