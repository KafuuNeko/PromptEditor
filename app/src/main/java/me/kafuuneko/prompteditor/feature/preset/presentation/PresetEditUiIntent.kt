package me.kafuuneko.prompteditor.feature.preset.presentation

sealed class PresetEditUiIntent {
    data class CreatePage(val presetId: Long) : PresetEditUiIntent()
    data class UpdatePromptsText(val text: String) : PresetEditUiIntent()
    data class ConfirmUpdatePromptItem(val index: Int, val newTag: String) : PresetEditUiIntent()
    data class ReorderPrompts(val fromIndex: Int, val toIndex: Int) : PresetEditUiIntent()
    data object ToggleEditMode : PresetEditUiIntent()
    data object SavePreset : PresetEditUiIntent()
    data object OpenTagsSearch : PresetEditUiIntent()
    data class AddTagsFromSearch(val tags: List<String>) : PresetEditUiIntent()
    data class ShowDeleteConfirmDialog(val index: Int, val tagName: String) : PresetEditUiIntent()
    data object ConfirmDeletePromptItem : PresetEditUiIntent()
    data object Back : PresetEditUiIntent()
    data object DismissDialog : PresetEditUiIntent()
    data object ConfirmDiscardChanges : PresetEditUiIntent()
}
