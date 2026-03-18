package me.kafuuneko.prompteditor.feature.about.presentation

sealed class AboutUiIntent {
    data object CreatePage : AboutUiIntent()
    data object OpenGithub : AboutUiIntent()
    data object Back : AboutUiIntent()
}
