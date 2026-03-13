package me.kafuuneko.prompteditor.feature.main.presentation

import me.kafuuneko.prompteditor.libs.room.entity.PresetSet

sealed class MainUiState {
    data object None : MainUiState()

    data class Normal(
        val presetSets: List<PresetSet> = emptyList(),
        val isLoading: Boolean = false,
        val dialogState: MainDialogState = MainDialogState.None
    ) : MainUiState()

    data object Finished : MainUiState()
}

sealed class MainDialogState {
    data object None : MainDialogState()
    data object CreatePresetSet : MainDialogState()
    data object ExitConfirm : MainDialogState()
    data class DeletePresetSet(val id: Long, val name: String) : MainDialogState()
}