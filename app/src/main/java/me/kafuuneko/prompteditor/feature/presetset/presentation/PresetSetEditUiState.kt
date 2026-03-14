package me.kafuuneko.prompteditor.feature.presetset.presentation

import me.kafuuneko.prompteditor.libs.room.entity.Preset

sealed class PresetSetEditUiState {
    data object None : PresetSetEditUiState()

    data class Normal(
        val presetSetId: Long = 0L,
        val presetSetName: String = "",
        val presets: List<Preset> = emptyList(),
        val isLoading: Boolean = false,
        val dialogState: PresetSetEditDialogState = PresetSetEditDialogState.None,
        val isMultiSelectMode: Boolean = false,
        val selectedPresetIds: Set<Long> = emptySet()
    ) : PresetSetEditUiState()

    data object Finished : PresetSetEditUiState()
}

sealed class PresetSetEditDialogState {
    data object None : PresetSetEditDialogState()

    data object CreatePreset : PresetSetEditDialogState()

    data class DeleteConfirm(
        val presetName: String,
        val presetId: Long
    ) : PresetSetEditDialogState()

    data class DeleteMultipleConfirm(
        val presetCount: Int
    ) : PresetSetEditDialogState()

    data class RenamePreset(
        val presetName: String,
        val presetId: Long
    ) : PresetSetEditDialogState()
}
