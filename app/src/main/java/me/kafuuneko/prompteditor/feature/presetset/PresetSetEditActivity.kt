package me.kafuuneko.prompteditor.feature.presetset

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.preset.PresetEditActivity
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiEffect
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiIntent
import me.kafuuneko.prompteditor.feature.presetset.ui.PresetSetEditLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect
import me.kafuuneko.prompteditor.libs.core.IUiEffect

class PresetSetEditActivity : CoreActivityWithUiEffect() {
    private val mViewModel by viewModels<PresetSetEditViewModel>()
    override fun getUiEffectFlow() = mViewModel.uiEffectFlow

    companion object {
        const val EXTRA_PRESET_SET_ID = "extra_preset_set_id"
        const val EXTRA_PRESET_SET_NAME = "extra_preset_set_name"
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        Surface(modifier = Modifier.fillMaxSize()) {
            PresetSetEditLayout(uiState) { mViewModel.emit(this) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val presetSetId = intent.getLongExtra(EXTRA_PRESET_SET_ID, 0L)
        mViewModel.emit(PresetSetEditUiIntent.CreatePage(presetSetId))
    }

    override suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        when (uiEffect) {
            is PresetSetEditUiEffect.ShowToast -> {
                val message = if (uiEffect.formatArgs.isNotEmpty()) {
                    getString(uiEffect.messageResId, *uiEffect.formatArgs.toTypedArray())
                } else {
                    getString(uiEffect.messageResId)
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }

            is PresetSetEditUiEffect.NavigateToPresetEdit -> {
                val intent = android.content.Intent(this, PresetEditActivity::class.java).apply {
                    putExtra(PresetEditActivity.EXTRA_PRESET_ID, uiEffect.presetId)
                }
                startActivity(intent)
            }

            is PresetSetEditUiEffect.NavigateBack -> {
                finish()
            }

            is PresetSetEditUiEffect.CopyToClipboard -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("prompts", uiEffect.text)
                clipboard.setPrimaryClip(clip)
            }

            else -> super.onReceivedUiEffect(uiEffect)
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.emit(PresetSetEditUiIntent.LoadPresets)
    }
}
