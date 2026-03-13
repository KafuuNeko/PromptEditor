package me.kafuuneko.prompteditor.feature.tagssearch.presentation

import me.kafuuneko.prompteditor.libs.room.entity.Tag

sealed class TagsSearchUiState {
    data object None : TagsSearchUiState()

    data class Normal(
        val allTags: List<Tag> = emptyList(),
        val filteredTags: List<Tag> = emptyList(),
        val selectedTags: Set<String> = emptySet(),
        val searchQuery: String = "",
        val isLoading: Boolean = false
    ) : TagsSearchUiState()

    data object Finished : TagsSearchUiState()
}
