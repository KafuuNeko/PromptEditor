package me.kafuuneko.prompteditor.feature.template.ui

import androidx.compose.runtime.Composable
import me.kafuuneko.prompteditor.feature.template.presentation.TemplateUiIntent
import me.kafuuneko.prompteditor.feature.template.presentation.TemplateUiState

@Composable
fun TemplateLayout(
    uiState: TemplateUiState,
    emit: TemplateUiIntent.() -> Unit
) {
    when (uiState) {
        TemplateUiState.None, TemplateUiState.Finished -> Unit
        is TemplateUiState.Normal -> Normal(uiState, emit)
    }
}

@Composable
private fun Normal(
    uiState: TemplateUiState.Normal,
    emit: TemplateUiIntent.() -> Unit
) {

}