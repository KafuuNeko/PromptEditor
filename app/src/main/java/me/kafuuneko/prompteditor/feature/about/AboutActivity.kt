package me.kafuuneko.prompteditor.feature.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.about.presentation.AboutUiEffect
import me.kafuuneko.prompteditor.feature.about.presentation.AboutUiIntent
import me.kafuuneko.prompteditor.feature.about.ui.AboutLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect
import me.kafuuneko.prompteditor.libs.core.IUiEffect
import androidx.core.net.toUri

class AboutActivity : CoreActivityWithUiEffect() {
    private val mViewModel by viewModels<AboutViewModel>()
    override fun getUiEffectFlow() = mViewModel.uiEffectFlow

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        Surface(modifier = Modifier.fillMaxSize()) {
            AboutLayout(uiState) { mViewModel.emit(this) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(AboutUiIntent.CreatePage)
    }

    override suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        when (uiEffect) {
            is AboutUiEffect.NavigateBack -> {
                finish()
            }
            is AboutUiEffect.OpenUrl -> {
                val intent = Intent(Intent.ACTION_VIEW, uiEffect.url.toUri())
                startActivity(intent)
            }
            else -> super.onReceivedUiEffect(uiEffect)
        }
    }
}