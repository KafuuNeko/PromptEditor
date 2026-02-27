package me.kafuuneko.prompteditor.feature.template.presentation

sealed class TemplateUiIntent {
    data object CreatePage : TemplateUiIntent()
}