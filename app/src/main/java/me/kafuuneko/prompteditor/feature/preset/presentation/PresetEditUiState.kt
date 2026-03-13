package me.kafuuneko.prompteditor.feature.preset.presentation

sealed class PresetEditUiState {
    data object None : PresetEditUiState()

    data class Normal(
        val presetId: Long = 0L,
        val presetName: String = "",
        val promptsText: String = "",
        val promptItems: List<PromptItem> = emptyList(),
        val isTextMode: Boolean = false,
        val isLoading: Boolean = false,
        val isSaved: Boolean = true,
        val dialogState: PresetEditDialogState = PresetEditDialogState.None
    ) : PresetEditUiState()

    data object Finished : PresetEditUiState()
}

sealed class PresetEditDialogState {
    data object None : PresetEditDialogState()
    data object UnsavedChangesConfirm : PresetEditDialogState()
    data class DeleteConfirm(val index: Int, val tagName: String) : PresetEditDialogState()
}

data class PromptItem(
    val originalText: String,
    val tagName: String,
    val weight: String = "",
    val description: String = ""
)
