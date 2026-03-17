package me.kafuuneko.prompteditor.feature.preset.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
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
import me.kafuuneko.prompteditor.ui.widgets.DraggableItem
import me.kafuuneko.prompteditor.ui.widgets.dragContainer
import me.kafuuneko.prompteditor.ui.widgets.rememberGridDragDropState

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
                        onConfirmUpdate = { index, newTag ->
                            emit(PresetEditUiIntent.ConfirmUpdatePromptItem(index, newTag))
                        },
                        onDelete = { index, tagName ->
                            emit(PresetEditUiIntent.ShowDeleteConfirmDialog(index, tagName))
                        },
                        onReorder = { from, to ->
                            emit(PresetEditUiIntent.ReorderPrompts(from, to))
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
    onConfirmUpdate: (Int, String) -> Unit,
    onDelete: (Int, String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val dragDropState = rememberGridDragDropState(gridState) { fromIndex, toIndex ->
        onReorder(fromIndex, toIndex)
    }

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
                    .dragContainer(dragDropState),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                            onConfirmUpdate = { newTag -> onConfirmUpdate(index, newTag) },
                            onDelete = { onDelete(index, item.tagName) }
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
    onConfirmUpdate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var editText by remember(item.tagName) { mutableStateOf(item.tagName) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(item.tagName) { editText = item.tagName }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                if (isEditing) {
                    BasicTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Text(
                        text = item.tagName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }

                if (isEditing) {
                    TextButton(
                        onClick = {
                            onConfirmUpdate(editText)
                            isEditing = false
                        }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                    TextButton(onClick = {
                        editText = item.tagName
                        isEditing = false
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                } else {
                    TextButton(
                        onClick = { isEditing = true }
                    ) {
                        Text(stringResource(R.string.edit))
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

            if (item.description.isNotEmpty() && !isEditing) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
