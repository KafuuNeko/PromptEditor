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

    data class ShowRenameDialog(val presetId: Long, val presetName: String) : PresetSetEditUiIntent()
    data class ConfirmRename(val presetId: Long, val newName: String) : PresetSetEditUiIntent()

    data class ReorderPresets(val fromIndex: Int, val toIndex: Int) : PresetSetEditUiIntent()


    data class EnterMultiSelectMode(val presetId: Long) : PresetSetEditUiIntent()
    data object ExitMultiSelectMode : PresetSetEditUiIntent()
    data class TogglePresetSelection(val presetId: Long) : PresetSetEditUiIntent()
    data object ShowDeleteMultipleConfirmDialog : PresetSetEditUiIntent()
    data object ConfirmDeleteSelectedPresets : PresetSetEditUiIntent()
    data object CopySelectedPresetsPrompts : PresetSetEditUiIntent()
    data object SelectAllPresets : PresetSetEditUiIntent()
}
