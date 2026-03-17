package me.kafuuneko.prompteditor.feature.tagssearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiIntent
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiState
import me.kafuuneko.prompteditor.libs.room.entity.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsSearchLayout(
    uiState: TagsSearchUiState,
    onIntent: (TagsSearchUiIntent) -> Unit
) {
    when (uiState) {
        is TagsSearchUiState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is TagsSearchUiState.Normal -> {
            NormalTagsSearchLayout(uiState, onIntent)
        }

        is TagsSearchUiState.Finished -> {
            // Handle finished state
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTagsSearchLayout(
    uiState: TagsSearchUiState.Normal,
    onIntent: (TagsSearchUiIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_tags)) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(TagsSearchUiIntent.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(TagsSearchUiIntent.OpenTagsEdit) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_tags)
                        )
                    }
                    IconButton(onClick = { onIntent(TagsSearchUiIntent.ConfirmSelection) }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.confirm)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onIntent(TagsSearchUiIntent.SearchTags(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_tags_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Selected count
            if (uiState.selectedTags.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.tags_selected, uiState.selectedTags.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Tags list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.filteredTags,
                    key = { it.id }
                ) { tag ->
                    TagItem(
                        tag = tag,
                        isSelected = tag.name in uiState.selectedTags,
                        onToggle = { onIntent(TagsSearchUiIntent.ToggleTagSelection(tag.name)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TagItem(
    tag: Tag,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable { onToggle() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (tag.description.isNotEmpty()) {
                    Text(
                        text = tag.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
