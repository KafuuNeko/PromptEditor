package me.kafuuneko.prompteditor.feature.about.presentation

sealed class AboutUiState {
    data object None : AboutUiState()
    data object Normal : AboutUiState()
}
