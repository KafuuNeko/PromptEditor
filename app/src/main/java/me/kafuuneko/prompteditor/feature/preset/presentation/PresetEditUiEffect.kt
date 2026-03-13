package me.kafuuneko.prompteditor.feature.preset.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect

sealed class PresetEditUiEffect : IUiEffect {
    data class ShowToast(val messageResId: Int) : PresetEditUiEffect()
    data object NavigateBack : PresetEditUiEffect()
    data object NavigateToTagsSearch : PresetEditUiEffect()
}
