package me.kafuuneko.prompteditor.feature.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiEffect
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState
import me.kafuuneko.prompteditor.feature.main.ui.MainLayout
import me.kafuuneko.prompteditor.feature.presetset.PresetSetEditActivity
import me.kafuuneko.prompteditor.feature.tagsedit.TagsEditActivity
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect
import me.kafuuneko.prompteditor.libs.core.IUiEffect

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

    override suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        when (uiEffect) {
            is MainUiEffect.ShowToast -> {
                val message = if (uiEffect.formatArgs.isNotEmpty()) {
                    getString(uiEffect.messageResId, *uiEffect.formatArgs.toTypedArray())
                } else {
                    getString(uiEffect.messageResId)
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }

            is MainUiEffect.NavigateToPresetSetEdit -> {
                val intent = Intent(this, PresetSetEditActivity::class.java).apply {
                    putExtra(PresetSetEditActivity.EXTRA_PRESET_SET_ID, uiEffect.presetSet.id)
                    putExtra(PresetSetEditActivity.EXTRA_PRESET_SET_NAME, uiEffect.presetSet.name)
                }
                startActivity(intent)
            }

            is MainUiEffect.NavigateToTagsImport -> {
                startActivity(Intent(this, TagsEditActivity::class.java))
            }

            else -> super.onReceivedUiEffect(uiEffect)
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.emit(MainUiIntent.LoadPresetSets)
    }
}
