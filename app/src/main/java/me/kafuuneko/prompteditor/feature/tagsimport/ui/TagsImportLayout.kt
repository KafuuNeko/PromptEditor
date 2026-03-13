package me.kafuuneko.prompteditor.feature.tagsimport.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportDialogState
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiIntent
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiState
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import me.kafuuneko.prompteditor.ui.dialog.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsImportLayout(
    uiState: TagsImportUiState,
    emit: TagsImportUiIntent.() -> Unit,
    onImportClick: () -> Unit
) {
    when (uiState) {
        is TagsImportUiState.Normal -> {
            NormalTagsImportLayout(uiState, emit, onImportClick)
            DialogSwitch(uiState.dialogState, emit)
        }

        is TagsImportUiState.Finished -> Unit
    }
}

@Composable
private fun DialogSwitch(
    dialogState: TagsImportDialogState,
    emit: TagsImportUiIntent.() -> Unit
) {
    when (dialogState) {
        is TagsImportDialogState.ClearConfirm -> {
            ConfirmDialog(
                onConfirmRequest = { emit(TagsImportUiIntent.ConfirmClearAll) },
                onDismissRequest = { emit(TagsImportUiIntent.DismissDialog) },
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

        TagsImportDialogState.None -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTagsImportLayout(
    uiState: TagsImportUiState.Normal,
    emit: TagsImportUiIntent.() -> Unit,
    onImportClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tags_import)) },
                navigationIcon = {
                    IconButton(onClick = { emit(TagsImportUiIntent.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { emit(TagsImportUiIntent.ShowClearConfirmDialog) }) {
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
            // 导入按钮
            Button(
                onClick = onImportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.select_csv_file))
            }

            // 导入说明
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.csv_format说明),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.csv_format_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 显示Tags数量
            Text(
                text = stringResource(R.string.tags_imported_count, uiState.tags.size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags列表
            if (uiState.tags.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_tags_import_csv),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tags, key = { it.id }) { tag ->
                        TagItem(tag = tag)
                    }
                }
            }
        }
    }
}

@Composable
private fun TagItem(tag: Tag) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (tag.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tag.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
