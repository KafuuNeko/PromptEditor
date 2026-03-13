package me.kafuuneko.prompteditor.feature.tagsimport.presentation

import me.kafuuneko.prompteditor.libs.room.entity.Tag

sealed class TagsImportUiState {
    data class Normal(
        val tags: List<Tag> = emptyList(),
        val isLoading: Boolean = false,
        val importResult: ImportResult? = null,
        val dialogState: TagsImportDialogState = TagsImportDialogState.None
    ) : TagsImportUiState()

    data object Finished : TagsImportUiState()
}

sealed class TagsImportDialogState {
    data object None : TagsImportDialogState()
    data object ClearConfirm : TagsImportDialogState()
}

data class ImportResult(
    val successCount: Int,
    val failCount: Int,
    val totalCount: Int
)
