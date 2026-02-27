package me.kafuuneko.prompteditor.feature.template

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.template.presentation.TemplateUiIntent
import me.kafuuneko.prompteditor.feature.template.presentation.TemplateUiState
import me.kafuuneko.prompteditor.feature.template.ui.TemplateLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect

class TemplateActivity : CoreActivityWithUiEffect() {
    private val mViewModel by viewModels<TemplateViewModel>()
    override fun getUiEffectFlow() = mViewModel.uiEffectFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is TemplateUiState.Finished) finish()
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            TemplateLayout(uiState) { mViewModel.emit(this) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(TemplateUiIntent.CreatePage)
    }
}
