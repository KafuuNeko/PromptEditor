package me.kafuuneko.prompteditor.feature.main.presentation

sealed class MainUiIntent {
    data object CreatePage : MainUiIntent()
    data object LoadPresetSets : MainUiIntent()
    data object StartCreatePreset : MainUiIntent()
    data class CreatePresetSet(val name: String) : MainUiIntent()
    data class StartDeletePreset(val id: Long, val name: String) : MainUiIntent()
    data object ConfirmDeletePreset : MainUiIntent()
    data class OpenPresetSet(val id: Long) : MainUiIntent()
    data object OpenTagsImport : MainUiIntent()
    data object Back : MainUiIntent()
    data object DismissDialog : MainUiIntent()
    data object ConfirmExit : MainUiIntent()

    data class ShowRenamePresetSetDialog(val id: Long, val name: String) : MainUiIntent()
    data class ConfirmRenamePresetSet(val id: Long, val newName: String) : MainUiIntent()
}