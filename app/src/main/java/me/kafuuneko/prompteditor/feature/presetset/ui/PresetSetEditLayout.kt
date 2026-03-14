package me.kafuuneko.prompteditor.feature.presetset.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import me.kafuuneko.prompteditor.ui.widgets.DraggableItem
import me.kafuuneko.prompteditor.ui.widgets.dragContainer
import me.kafuuneko.prompteditor.ui.widgets.rememberGridDragDropState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSetEditLayout(
    uiState: PresetSetEditUiState,
    emit: PresetSetEditUiIntent.() -> Unit
) {
    BackHandler { PresetSetEditUiIntent.Back.emit() }
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

        is PresetSetEditDialogState.DeleteMultipleConfirm -> {
            ConfirmDialog(
                onConfirmRequest = {
                    emit(PresetSetEditUiIntent.ConfirmDeleteSelectedPresets)
                },
                onDismissRequest = { emit(PresetSetEditUiIntent.DismissDialog) },
                confirmContent = {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            ) {
                Text(stringResource(R.string.delete_selected_presets_confirmation, dialogState.presetCount))
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
    val selectedCount = uiState.selectedPresetIds.size
    val isSingleSelection = selectedCount == 1
    val selectedPreset = if (isSingleSelection) {
        uiState.presets.find { it.id in uiState.selectedPresetIds }
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isMultiSelectMode) {
                        Text(stringResource(R.string.selected_count, selectedCount))
                    } else {
                        Text(uiState.presetSetName)
                    }
                },
                navigationIcon = {
                    if (uiState.isMultiSelectMode) {
                        IconButton(onClick = { emit(PresetSetEditUiIntent.ExitMultiSelectMode) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }
                    } else {
                        IconButton(onClick = { emit(PresetSetEditUiIntent.Back) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isMultiSelectMode) {
                        IconButton(onClick = { emit(PresetSetEditUiIntent.SelectAllPresets) }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.select_all)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isMultiSelectMode) {
                FloatingActionButton(
                    onClick = { emit(PresetSetEditUiIntent.ShowCreateDialog) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_preset))
                }
            }
        },
        bottomBar = {
            if (uiState.isMultiSelectMode && selectedCount > 0) {
                MultiSelectBottomBar(
                    isSingleSelection = isSingleSelection,
                    onCopy = {
                        if (isSingleSelection && selectedPreset != null) {
                            emit(PresetSetEditUiIntent.CopyPrompts(selectedPreset.prompts))
                        } else {
                            emit(PresetSetEditUiIntent.CopySelectedPresetsPrompts)
                        }
                    },
                    onRename = {
                        if (selectedPreset != null) {
                            emit(PresetSetEditUiIntent.ShowRenameDialog(selectedPreset.id, selectedPreset.name))
                        }
                    },
                    onDelete = {
                        emit(PresetSetEditUiIntent.ShowDeleteMultipleConfirmDialog)
                    }
                )
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
            DragListMode(
                presets = uiState.presets,
                isMultiSelectMode = uiState.isMultiSelectMode,
                selectedPresetIds = uiState.selectedPresetIds,
                onPresetClick = { preset ->
                    if (uiState.isMultiSelectMode) {
                        emit(PresetSetEditUiIntent.TogglePresetSelection(preset.id))
                    } else {
                        emit(PresetSetEditUiIntent.OpenPreset(preset.id))
                    }
                },
                onEnterMultiSelect = { presetId ->
                    emit(PresetSetEditUiIntent.EnterMultiSelectMode(presetId))
                },
                onCopy = { emit(PresetSetEditUiIntent.CopyPrompts(it)) },
                onDelete = { presetId, presetName ->
                    emit(PresetSetEditUiIntent.ShowDeleteConfirmDialog(presetId, presetName))
                },
                onRename = { presetId, presetName ->
                    emit(PresetSetEditUiIntent.ShowRenameDialog(presetId, presetName))
                },
                onReorder = { from, to ->
                    emit(PresetSetEditUiIntent.ReorderPresets(from, to))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun MultiSelectBottomBar(
    isSingleSelection: Boolean,
    onCopy: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Copy action - always available
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onCopy)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.copy),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Rename action - only when single selection
            if (isSingleSelection) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(onClick = onRename)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.rename),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.rename),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Delete action - always available
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.delete),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DragListMode(
    presets: List<Preset>,
    isMultiSelectMode: Boolean,
    selectedPresetIds: Set<Long>,
    onPresetClick: (Preset) -> Unit,
    onEnterMultiSelect: (Long) -> Unit,
    onCopy: (String) -> Unit,
    onDelete: (Long, String) -> Unit,
    onRename: (Long, String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val dragDropState = rememberGridDragDropState(gridState) { fromIndex, toIndex ->
        onReorder(fromIndex, toIndex)
    }

    Column(modifier = modifier) {
        // Only show tip when not in multi-select mode
        if (!isMultiSelectMode) {
            Text(
                text = stringResource(R.string.tip_drag_to_reorder),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isMultiSelectMode) Modifier.dragContainer(dragDropState)
                    else Modifier
                ),
            state = gridState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = presets,
                key = { _, preset -> preset.id }
            ) { index, preset ->
                val isSelected = preset.id in selectedPresetIds
                DraggableItem(dragDropState, index) { isDragging ->
                    PresetItem(
                        preset = preset,
                        isMultiSelectMode = isMultiSelectMode,
                        isSelected = isSelected,
                        isDragging = isDragging,
                        onClick = { onPresetClick(preset) },
                        onEnterMultiSelect = { onEnterMultiSelect(preset.id) },
                        onCopy = { onCopy(preset.prompts) },
                        onDelete = { onDelete(preset.id, preset.name) },
                        onRename = { onRename(preset.id, preset.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetItem(
    preset: Preset,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onEnterMultiSelect: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDragging) Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                else Modifier.background(MaterialTheme.colorScheme.surface)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.primaryContainer
            else if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMultiSelectMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
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
                        text = { Text(stringResource(R.string.select)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onEnterMultiSelect()
                        }
                    )
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
