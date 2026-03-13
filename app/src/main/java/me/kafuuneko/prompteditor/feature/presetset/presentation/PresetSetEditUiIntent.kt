package me.kafuuneko.prompteditor.feature.presetset.presentation

sealed class PresetSetEditUiIntent {
    data class CreatePage(val presetSetId: Long) : PresetSetEditUiIntent()
    data object LoadPresets : PresetSetEditUiIntent()
    data class CreatePreset(val name: String) : PresetSetEditUiIntent()
    data class DeletePreset(val id: Long) : PresetSetEditUiIntent()
    data object ShowCreateDialog : PresetSetEditUiIntent()
    data class ShowDeleteConfirmDialog(val presetId: Long, val presetName: String) :
        PresetSetEditUiIntent()

    data object DismissDialog : PresetSetEditUiIntent()
    data class ConfirmDelete(val presetId: Long) : PresetSetEditUiIntent()
    data class OpenPreset(val id: Long) : PresetSetEditUiIntent()
    data class CopyPrompts(val prompts: String) : PresetSetEditUiIntent()
    data object Back : PresetSetEditUiIntent()
}
