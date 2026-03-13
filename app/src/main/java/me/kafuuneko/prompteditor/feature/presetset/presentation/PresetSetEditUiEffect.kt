package me.kafuuneko.prompteditor.feature.presetset.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect

sealed class PresetSetEditUiEffect : IUiEffect {
    data class ShowToast(val messageResId: Int, val formatArgs: List<Any> = emptyList()) :
        PresetSetEditUiEffect()

    data class NavigateToPresetEdit(val presetId: Long) : PresetSetEditUiEffect()

    data object NavigateBack : PresetSetEditUiEffect()

    data class CopyToClipboard(val text: String) : PresetSetEditUiEffect()
}
