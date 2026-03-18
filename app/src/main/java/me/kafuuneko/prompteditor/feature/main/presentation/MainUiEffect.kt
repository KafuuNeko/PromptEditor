package me.kafuuneko.prompteditor.feature.main.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet

sealed class MainUiEffect : IUiEffect {
    data class ShowToast(
        val messageResId: Int, val formatArgs: List<Any> = emptyList()
    ) : MainUiEffect()

    data class NavigateToPresetSetEdit(val presetSet: PresetSet) : MainUiEffect()

    data object NavigateToTagsImport : MainUiEffect()
    
    data object NavigateToAbout : MainUiEffect()
}