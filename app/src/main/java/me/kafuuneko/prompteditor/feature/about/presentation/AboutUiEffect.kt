package me.kafuuneko.prompteditor.feature.about.presentation

import me.kafuuneko.prompteditor.libs.core.IUiEffect

sealed class AboutUiEffect : IUiEffect {
    data class OpenUrl(val url: String) : AboutUiEffect()
    data object NavigateBack : AboutUiEffect()
}
