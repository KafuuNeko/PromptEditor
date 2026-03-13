package me.kafuuneko.prompteditor.feature.tagsedit.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect

sealed class TagsEditUiEffect : IUiEffect {
    data class ShowToast(val messageResId: Int, val formatArgs: List<Any> = emptyList()) :
        TagsEditUiEffect()

    data object NavigateBack : TagsEditUiEffect()
}
