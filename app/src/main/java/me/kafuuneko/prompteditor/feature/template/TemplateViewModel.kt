package me.kafuuneko.prompteditor.feature.template

import me.kafuuneko.prompteditor.feature.template.presentation.TemplateUiIntent
import me.kafuuneko.prompteditor.feature.template.presentation.TemplateUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver

class TemplateViewModel :
    CoreViewModelWithUiEffect<TemplateUiIntent, TemplateUiState>(TemplateUiState.None) {

    @UiIntentObserver(TemplateUiIntent.CreatePage::class)
    fun onCreatePage() {
        TemplateUiState.Normal().setup()
    }

}