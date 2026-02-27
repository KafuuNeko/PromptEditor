package me.kafuuneko.prompteditor.feature.main.presentation

sealed class MainUiState {
    data object None : MainUiState()

    data class Normal(
        val presets: Any? = null
    ) : MainUiState()

    data object Finished : MainUiState()
}