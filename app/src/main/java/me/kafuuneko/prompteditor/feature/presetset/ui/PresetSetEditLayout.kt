package me.kafuuneko.prompteditor.feature.presetset.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditDialogState
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiIntent
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiState
import me.kafuuneko.prompteditor.libs.room.entity.Preset
import me.kafuuneko.prompteditor.ui.dialog.ConfirmDialog
import me.kafuuneko.prompteditor.ui.dialog.InputConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSetEditLayout(
    uiState: PresetSetEditUiState,
    emit: PresetSetEditUiIntent.() -> Unit
) {
    when (uiState) {
        is PresetSetEditUiState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PresetSetEditUiState.Normal -> {
            NormalPresetSetEditLayout(uiState, emit)
            DialogSwitch(uiState.dialogState, emit)
        }

        is PresetSetEditUiState.Finished -> Unit
    }
}

@Composable
private fun DialogSwitch(
    dialogState: PresetSetEditDialogState,
    emit: PresetSetEditUiIntent.() -> Unit
) {
    // Dialogs
    when (val dialogState = dialogState) {
        PresetSetEditDialogState.None -> Unit

        is PresetSetEditDialogState.CreatePreset -> {
            InputConfirmDialog(
                title = stringResource(R.string.create_preset),
                hintText = stringResource(R.string.preset_name),
                onConfirmRequest = {
                    emit(PresetSetEditUiIntent.CreatePreset(it))
                    emit(PresetSetEditUiIntent.DismissDialog)
                },
                onDismissRequest = { emit(PresetSetEditUiIntent.DismissDialog) }
            )
        }

        is PresetSetEditDialogState.DeleteConfirm -> {
            ConfirmDialog(
                onConfirmRequest = {
                    emit(PresetSetEditUiIntent.ConfirmDelete(dialogState.presetId))
                },
                onDismissRequest = { emit(PresetSetEditUiIntent.DismissDialog) },
                confirmContent = {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            ) {
                Text(stringResource(R.string.delete_preset_confirmation, dialogState.presetName))
            }
        }

        is PresetSetEditDialogState.RenamePreset -> {
            InputConfirmDialog(
                title = stringResource(R.string.rename_preset),
                hintText = stringResource(R.string.preset_name),
                defaultText = dialogState.presetName,
                onConfirmRequest = {
                    emit(PresetSetEditUiIntent.ConfirmRename(dialogState.presetId, it))
                    emit(PresetSetEditUiIntent.DismissDialog)
                },
                onDismissRequest = { emit(PresetSetEditUiIntent.DismissDialog) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalPresetSetEditLayout(
    uiState: PresetSetEditUiState.Normal,
    emit: PresetSetEditUiIntent.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.presetSetName) },
                navigationIcon = {
                    IconButton(onClick = { emit(PresetSetEditUiIntent.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { emit(PresetSetEditUiIntent.ShowCreateDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_preset))
            }
        }
    ) { paddingValues ->
        if (uiState.presets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_presets),
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
                items(uiState.presets, key = { it.id }) { preset ->
                    PresetItem(
                        preset = preset,
                        onClick = { emit(PresetSetEditUiIntent.OpenPreset(preset.id)) },
                        onCopy = { emit(PresetSetEditUiIntent.CopyPrompts(preset.prompts)) },
                        onDelete = {
                            emit(
                                PresetSetEditUiIntent.ShowDeleteConfirmDialog(
                                    preset.id,
                                    preset.name
                                )
                            )
                        },
                        onRename = {
                            emit(
                                PresetSetEditUiIntent.ShowRenameDialog(
                                    preset.id,
                                    preset.name
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetItem(
    preset: Preset,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (preset.prompts.isNotEmpty()) {
                    Text(
                        text = preset.prompts.take(50) + if (preset.prompts.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
                        text = { Text(stringResource(R.string.copy)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onCopy()
                        }
                    )
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
                            onRename()
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
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
