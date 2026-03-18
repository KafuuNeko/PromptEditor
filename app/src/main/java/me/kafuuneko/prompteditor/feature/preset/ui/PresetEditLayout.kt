package me.kafuuneko.prompteditor.feature.preset.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditDialogState
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditMode
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiIntent
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiState
import me.kafuuneko.prompteditor.libs.utils.PromptItem
import me.kafuuneko.prompteditor.ui.dialog.ConfirmDialog
import me.kafuuneko.prompteditor.ui.dialog.PresetEditDialog
import me.kafuuneko.prompteditor.ui.dialog.getGroupColor
import me.kafuuneko.prompteditor.ui.widgets.DraggableItem
import me.kafuuneko.prompteditor.ui.widgets.dragContainer
import me.kafuuneko.prompteditor.ui.widgets.rememberGridDragDropState
import me.kafuuneko.prompteditor.ui.dialog.getGroupColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditLayout(
    uiState: PresetEditUiState,
    emit: PresetEditUiIntent.() -> Unit
) {
    BackHandler { PresetEditUiIntent.Back.emit() }
    when (uiState) {
        is PresetEditUiState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PresetEditUiState.Normal -> {
            NormalPresetEditLayout(uiState, emit)
            DialogSwitch(uiState.dialogState, emit)
        }

        is PresetEditUiState.Finished -> Unit
    }
}

@Composable
private fun DialogSwitch(
    dialogState: PresetEditDialogState,
    emit: PresetEditUiIntent.() -> Unit
) {
    when (dialogState) {
        PresetEditDialogState.None -> Unit

        is PresetEditDialogState.UnsavedChangesConfirm -> {
            ConfirmDialog(
                onConfirmRequest = { emit(PresetEditUiIntent.ConfirmDiscardChanges) },
                onDismissRequest = { emit(PresetEditUiIntent.DismissDialog) },
                confirmContent = { Text(stringResource(R.string.exit)) },
                cancelContent = { Text(stringResource(R.string.cancel)) }
            ) {
                Text(stringResource(R.string.unsaved_changes_confirmation))
            }
        }

        is PresetEditDialogState.DeleteConfirm -> {
            ConfirmDialog(
                onConfirmRequest = { emit(PresetEditUiIntent.ConfirmDeletePromptItem) },
                onDismissRequest = { emit(PresetEditUiIntent.DismissDialog) },
                confirmContent = {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            ) {
                Text(stringResource(R.string.delete_tag_confirmation, dialogState.tagName))
            }
        }

        is PresetEditDialogState.EditDialog -> {
            PresetEditDialog(
                tagName = dialogState.tagName,
                initialWeight = dialogState.weight,
                initialGroup = dialogState.group,
                onConfirm = { tagName, weight, group ->
                    emit(
                        PresetEditUiIntent.ConfirmUpdateItem(
                            dialogState.index,
                            tagName,
                            weight,
                            group
                        )
                    )
                },
                onDismissRequest = { emit(PresetEditUiIntent.DismissDialog) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalPresetEditLayout(
    uiState: PresetEditUiState.Normal,
    emit: PresetEditUiIntent.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.presetName) },
                navigationIcon = {
                    IconButton(onClick = { emit(PresetEditUiIntent.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { PresetEditUiIntent.ToggleEditMode.emit() }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = when (uiState.mode) {
                                is PresetEditMode.ListMode -> stringResource(R.string.switch_to_text_mode)
                                is PresetEditMode.TextMode -> stringResource(R.string.switch_to_list_mode)
                            }
                        )
                    }
                    IconButton(
                        onClick = { emit(PresetEditUiIntent.SavePreset) },
                        enabled = !uiState.isSaved
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = stringResource(R.string.save),
                            tint = if (!uiState.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.mode) {
                is PresetEditMode.ListMode -> {
                    DragListMode(
                        items = uiState.mode.promptItems,
                        onDelete = { index, tagName ->
                            emit(PresetEditUiIntent.ShowDeleteConfirmDialog(index, tagName))
                        },
                        onEdit = { index ->
                            emit(PresetEditUiIntent.ShowEditDialog(index))
                        },
                        onReorder = { from, to ->
                            emit(PresetEditUiIntent.ReorderPrompts(from, to))
                        },
                        onReorderEnd = {
                            emit(PresetEditUiIntent.ReassignGroups)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                is PresetEditMode.TextMode -> {
                    TextEditMode(
                        text = uiState.mode.promptsText,
                        onTextChange = { emit(PresetEditUiIntent.UpdatePromptsText(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = { emit(PresetEditUiIntent.OpenTagsSearch) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.add_from_tags_repository))
            }
        }
    }
}

@Composable
private fun TextEditMode(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var localText by remember(text) { mutableStateOf(text) }

    LaunchedEffect(text) {
        localText = text
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.prompt_separated_by_commas),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = localText,
            onValueChange = {
                localText = it
                onTextChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = TextStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        )
    }
}

@Composable
private fun DragListMode(
    items: List<PromptItem>,
    onDelete: (Int, String) -> Unit,
    onEdit: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onReorderEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val dragDropState = rememberGridDragDropState(
        gridState = gridState,
        onMove = { fromIndex, toIndex ->
            onReorder(fromIndex, toIndex)
        },
        onDragEnd = {
            onReorderEnd()
        }
    )

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_prompts_switch_to_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.tip_drag_to_reorder),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                    .dragContainer(dragDropState),
                state = gridState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = items,
                    key = { index, _ -> index }
                ) { index, item ->
                    DraggableItem(dragDropState, index) { isDragging ->
                        PromptItemCard(
                            item = item,
                            isDragging = isDragging,
                            onDelete = { onDelete(index, item.tagName) },
                            onEdit = { onEdit(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptItemCard(
    item: PromptItem,
    isDragging: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDragging) Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                else Modifier.background(MaterialTheme.colorScheme.surface)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.primaryContainer
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
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(
                        getGroupColor(item.group),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "(${item.weight})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.tagName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }

                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
