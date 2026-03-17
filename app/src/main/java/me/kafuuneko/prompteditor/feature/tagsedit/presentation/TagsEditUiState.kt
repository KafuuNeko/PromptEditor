package me.kafuuneko.prompteditor.feature.tagsedit.presentation

import me.kafuuneko.prompteditor.libs.room.entity.Tag

sealed class TagsEditUiState {
    data object None : TagsEditUiState()

    data class Normal(
        val tags: List<Tag> = emptyList(),
        val filteredTags: List<Tag> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val importResult: ImportResult? = null,
        val dialogState: TagsEditDialogState = TagsEditDialogState.None
    ) : TagsEditUiState()

    data object Finished : TagsEditUiState()
}

sealed class TagsEditDialogState {
    data object None : TagsEditDialogState()

    data class AddTag(
        val name: String = "",
        val description: String = ""
    ) : TagsEditDialogState()

    data class EditTag(
        val tagId: Long,
        val name: String,
        val description: String
    ) : TagsEditDialogState()

    data class DeleteConfirm(
        val tagId: Long,
        val tagName: String
    ) : TagsEditDialogState()

    data object ClearConfirm : TagsEditDialogState()

    data object ImportCsv : TagsEditDialogState()
}

data class ImportResult(
    val successCount: Int,
    val updateCount: Int = 0,
    val failCount: Int,
    val totalCount: Int
)
