package me.kafuuneko.prompteditor.feature.tagsedit.presentation

sealed class TagsEditUiIntent {
    data object CreatePage : TagsEditUiIntent()
    data object LoadTags : TagsEditUiIntent()
    data class ImportFromCsv(val csvContent: String) : TagsEditUiIntent()
    data class AddTag(val name: String, val description: String) : TagsEditUiIntent()
    data class UpdateTag(val id: Long, val name: String, val description: String) :
        TagsEditUiIntent()

    data class DeleteTag(val id: Long) : TagsEditUiIntent()
    data object ClearAllTags : TagsEditUiIntent()

    data object ShowAddDialog : TagsEditUiIntent()
    data class ShowEditDialog(val tagId: Long, val tagName: String, val tagDescription: String) :
        TagsEditUiIntent()

    data class ShowDeleteConfirmDialog(val tagId: Long, val tagName: String) : TagsEditUiIntent()
    data object ShowClearConfirmDialog : TagsEditUiIntent()
    data object ConfirmClearAll : TagsEditUiIntent()
    data object DismissDialog : TagsEditUiIntent()

    data object Back : TagsEditUiIntent()
}
