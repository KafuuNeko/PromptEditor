package me.kafuuneko.prompteditor.feature.preset

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiEffect
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiIntent
import me.kafuuneko.prompteditor.feature.preset.ui.PresetEditLayout
import me.kafuuneko.prompteditor.feature.tagssearch.TagsSearchActivity
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect
import me.kafuuneko.prompteditor.libs.core.IUiEffect

class PresetEditActivity : CoreActivityWithUiEffect() {
    private val mViewModel by viewModels<PresetEditViewModel>()
    override fun getUiEffectFlow() = mViewModel.uiEffectFlow

    companion object {
        const val EXTRA_PRESET_ID = "extra_preset_id"
    }

    private val tagsSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedTags = result.data?.getStringArrayListExtra(
                TagsSearchActivity.EXTRA_SELECTED_TAGS
            )
            if (!selectedTags.isNullOrEmpty()) {
                mViewModel.emit(PresetEditUiIntent.AddTagsFromSearch(selectedTags))
            }
        }
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        Surface(modifier = Modifier.fillMaxSize()) {
            PresetEditLayout(uiState) { mViewModel.emit(this) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val presetId = intent.getLongExtra(EXTRA_PRESET_ID, 0L)
        mViewModel.emit(PresetEditUiIntent.CreatePage(presetId))
    }

    override suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        when (uiEffect) {
            is PresetEditUiEffect.NavigateBack -> {
                finish()
            }

            is PresetEditUiEffect.NavigateToTagsSearch -> {
                val intent = Intent(this, TagsSearchActivity::class.java)
                tagsSearchLauncher.launch(intent)
            }

            else -> super.onReceivedUiEffect(uiEffect)
        }
    }
}
