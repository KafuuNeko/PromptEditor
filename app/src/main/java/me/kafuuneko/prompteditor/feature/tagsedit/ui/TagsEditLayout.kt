package me.kafuuneko.prompteditor.feature.tagsedit.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditDialogState
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditUiIntent
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditUiState
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import me.kafuuneko.prompteditor.ui.dialog.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsEditLayout(
    uiState: TagsEditUiState,
    emit: TagsEditUiIntent.() -> Unit,
    onImportClick: () -> Unit
) {
    when (uiState) {
        is TagsEditUiState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is TagsEditUiState.Normal -> {
            NormalTagsEditLayout(uiState, emit, onImportClick)
            DialogSwitch(uiState.dialogState, emit)
        }

        is TagsEditUiState.Finished -> Unit
    }
}

@Composable
fun DialogSwitch(
    dialogState: TagsEditDialogState,
    emit: TagsEditUiIntent.() -> Unit
) {
    when (val dialogState = dialogState) {
        is TagsEditDialogState.AddTag -> {
            AddEditTagDialog(
                title = stringResource(R.string.add_tag),
                initialName = dialogState.name,
                initialDescription = dialogState.description,
                onConfirm = { name, description ->
                    emit(TagsEditUiIntent.AddTag(name, description))
                },
                onDismiss = { emit(TagsEditUiIntent.DismissDialog) }
            )
        }

        is TagsEditDialogState.EditTag -> {
            AddEditTagDialog(
                title = stringResource(R.string.edit_tag),
                initialName = dialogState.name,
                initialDescription = dialogState.description,
                onConfirm = { name, description ->
                    emit(TagsEditUiIntent.UpdateTag(dialogState.tagId, name, description))
                },
                onDismiss = { emit(TagsEditUiIntent.DismissDialog) }
            )
        }

        is TagsEditDialogState.DeleteConfirm -> {
            ConfirmDialog(
                onConfirmRequest = {
                    emit(TagsEditUiIntent.DeleteTag(dialogState.tagId))
                    emit(TagsEditUiIntent.DismissDialog)
                },
                onDismissRequest = { emit(TagsEditUiIntent.DismissDialog) },
                confirmContent = {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            ) {
                Text(stringResource(R.string.delete_tag_confirmation, dialogState.tagName))
            }
        }

        is TagsEditDialogState.ClearConfirm -> {
            ConfirmDialog(
                onConfirmRequest = { emit(TagsEditUiIntent.ConfirmClearAll) },
                onDismissRequest = { emit(TagsEditUiIntent.DismissDialog) },
                confirmContent = {
                    Text(
                        stringResource(R.string.clear),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            ) {
                Text(stringResource(R.string.clear_all_tags_confirmation))
            }
        }

        is TagsEditDialogState.ImportCsv -> Unit

        TagsEditDialogState.None -> Unit

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTagsEditLayout(
    uiState: TagsEditUiState.Normal,
    emit: TagsEditUiIntent.() -> Unit,
    onImportClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tags_editor)) },
                navigationIcon = {
                    IconButton(onClick = { emit(TagsEditUiIntent.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { emit(TagsEditUiIntent.ShowClearConfirmDialog) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear),
                            tint = MaterialTheme.colorScheme.error
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
            // 按钮区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onImportClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.import_csv))
                }
                Button(
                    onClick = { emit(TagsEditUiIntent.ShowAddDialog) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_tag))
                }
            }

            // Tags列表
            if (uiState.tags.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_tags_import_or_add),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tags, key = { it.id }) { tag ->
                        TagItem(
                            tag = tag,
                            onEdit = {
                                emit(
                                    TagsEditUiIntent.ShowEditDialog(
                                        tag.id,
                                        tag.name,
                                        tag.description
                                    )
                                )
                            },
                            onDelete = {
                                emit(TagsEditUiIntent.ShowDeleteConfirmDialog(tag.id, tag.name))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagItem(
    tag: Tag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (tag.description.isNotEmpty()) {
                    Text(
                        text = tag.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
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
}

@Composable
private fun AddEditTagDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.tag_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.tag_description)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
