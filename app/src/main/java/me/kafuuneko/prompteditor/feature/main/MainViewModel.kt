package me.kafuuneko.prompteditor.feature.main

import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver

class MainViewModel :
    CoreViewModelWithUiEffect<MainUiIntent, MainUiState>(MainUiState.None) {

    @UiIntentObserver(MainUiIntent.CreatePage::class)
    fun onCreatePage() {
        MainUiState.Normal().setup()
    }

}