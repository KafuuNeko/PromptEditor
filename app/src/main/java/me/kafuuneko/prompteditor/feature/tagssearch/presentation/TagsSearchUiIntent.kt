package me.kafuuneko.prompteditor.feature.tagssearch.presentation

sealed class TagsSearchUiIntent {
    data object CreatePage : TagsSearchUiIntent()
    data class SearchTags(val query: String) : TagsSearchUiIntent()
    data class ToggleTagSelection(val tagName: String) : TagsSearchUiIntent()
    data object ConfirmSelection : TagsSearchUiIntent()
    data object Back : TagsSearchUiIntent()
    data object OpenTagsEdit : TagsSearchUiIntent()
}
