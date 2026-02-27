package me.kafuuneko.prompteditor.feature.template.presentation

sealed class TemplateUiState {
    data object None : TemplateUiState()

    data class Normal(
        val presets: Any? = null
    ) : TemplateUiState()

    data object Finished : TemplateUiState()
}