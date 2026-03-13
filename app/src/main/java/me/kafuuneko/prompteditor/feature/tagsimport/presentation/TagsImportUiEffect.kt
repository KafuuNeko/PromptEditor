package me.kafuuneko.prompteditor.feature.tagsimport.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect

sealed class TagsImportUiEffect : IUiEffect {
    data class ShowToast(val messageResId: Int, val formatArgs: Array<Any> = emptyArray()) :
        TagsImportUiEffect()

    data object NavigateBack : TagsImportUiEffect()
    data object OpenFilePicker : TagsImportUiEffect()
}
