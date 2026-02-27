package me.kafuuneko.prompteditor.feature.main.ui

import androidx.compose.runtime.Composable
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState

@Composable
fun MainLayout(
    uiState: MainUiState,
    emit: MainUiIntent.() -> Unit
) {
    when (uiState) {
        MainUiState.None, MainUiState.Finished -> Unit
        is MainUiState.Normal -> Normal(uiState, emit)
    }
}

@Composable
private fun Normal(
    uiState: MainUiState.Normal,
    emit: MainUiIntent.() -> Unit
) {

}