package me.kafuuneko.prompteditor.feature.about

import me.kafuuneko.prompteditor.feature.about.presentation.AboutUiEffect
import me.kafuuneko.prompteditor.feature.about.presentation.AboutUiIntent
import me.kafuuneko.prompteditor.feature.about.presentation.AboutUiState
import me.kafuuneko.prompteditor.libs.AppModel
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver

class AboutViewModel : CoreViewModelWithUiEffect<AboutUiIntent, AboutUiState>(AboutUiState.None) {

    @UiIntentObserver(AboutUiIntent.CreatePage::class)
    fun onCreatePage() {
        AboutUiState.Normal.setup()
    }

    @UiIntentObserver(AboutUiIntent.OpenGithub::class)
    fun onOpenGithub() {
        AboutUiEffect.OpenUrl(AppModel.CODE_REPOSITORY_URL).tryEmit()
    }

    @UiIntentObserver(AboutUiIntent.Back::class)
    fun onBack() {
        AboutUiEffect.NavigateBack.tryEmit()
    }
}
