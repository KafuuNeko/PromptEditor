package me.kafuuneko.prompteditor.feature.main.presentation

sealed class MainUiIntent {
    data object CreatePage : MainUiIntent()
}