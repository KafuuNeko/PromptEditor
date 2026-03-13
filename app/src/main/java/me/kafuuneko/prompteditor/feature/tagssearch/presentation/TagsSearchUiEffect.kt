package me.kafuuneko.prompteditor.feature.tagssearch.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect

sealed class TagsSearchUiEffect : IUiEffect {
    data class ReturnSelectedTags(val tags: List<String>) : TagsSearchUiEffect()
    data object NavigateBack : TagsSearchUiEffect()
}
