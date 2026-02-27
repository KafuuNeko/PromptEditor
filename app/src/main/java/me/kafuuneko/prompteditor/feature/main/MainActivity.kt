package me.kafuuneko.prompteditor.feature.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState
import me.kafuuneko.prompteditor.feature.main.ui.MainLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect

class MainActivity : CoreActivityWithUiEffect() {
    private val mViewModel by viewModels<MainViewModel>()
    override fun getUiEffectFlow() = mViewModel.uiEffectFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is MainUiState.Finished) finish()
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            MainLayout(uiState) { mViewModel.emit(this) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(MainUiIntent.CreatePage)
    }
}
