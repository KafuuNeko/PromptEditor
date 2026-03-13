package me.kafuuneko.prompteditor.feature.tagsimport.presentation

sealed class TagsImportUiIntent {
    data object CreatePage : TagsImportUiIntent()
    data object LoadTags : TagsImportUiIntent()
    data class ImportFromCsv(val csvContent: String) : TagsImportUiIntent()
    data object ClearAllTags : TagsImportUiIntent()
    data object ShowClearConfirmDialog : TagsImportUiIntent()
    data object ConfirmClearAll : TagsImportUiIntent()
    data object DismissDialog : TagsImportUiIntent()
    data object Back : TagsImportUiIntent()
}
