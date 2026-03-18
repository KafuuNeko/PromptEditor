package me.kafuuneko.prompteditor.feature.preset.presentation

import me.kafuuneko.prompteditor.libs.utils.PromptItem

sealed class PresetEditUiState {
    data object None : PresetEditUiState()

    data class Normal(
        val presetId: Long = 0L,
        val presetName: String = "",
        val mode: PresetEditMode = PresetEditMode.ListMode(),
        val isSaved: Boolean = true,
        val dialogState: PresetEditDialogState = PresetEditDialogState.None
    ) : PresetEditUiState()

    data object Finished : PresetEditUiState()

}

sealed class PresetEditMode {
    data class TextMode(
        val promptsText: String = "",
    ) : PresetEditMode()

    data class ListMode(
        val promptItems: List<PromptItem> = emptyList()
    ) : PresetEditMode()
}

sealed class PresetEditDialogState {
    data object None : PresetEditDialogState()
    data object UnsavedChangesConfirm : PresetEditDialogState()
    data class DeleteConfirm(val index: Int, val tagName: String) : PresetEditDialogState()
    data class EditDialog(
        val index: Int,
        val tagName: String,
        val weight: Double,
        val group: Int
    ) : PresetEditDialogState()
}

