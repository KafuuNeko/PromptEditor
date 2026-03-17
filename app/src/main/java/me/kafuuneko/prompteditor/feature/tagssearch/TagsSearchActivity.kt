package me.kafuuneko.prompteditor.feature.tagssearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.kafuuneko.prompteditor.feature.tagsedit.TagsEditActivity
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiEffect
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiIntent
import me.kafuuneko.prompteditor.feature.tagssearch.ui.TagsSearchLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect
import me.kafuuneko.prompteditor.libs.core.IUiEffect

class TagsSearchActivity : CoreActivityWithUiEffect() {
    private val viewModel by viewModels<TagsSearchViewModel>()

    companion object {
        const val EXTRA_SELECTED_TAGS = "extra_selected_tags"

        fun createResultIntent(selectedTags: List<String>): Intent {
            return Intent().apply {
                putStringArrayListExtra(EXTRA_SELECTED_TAGS, ArrayList(selectedTags))
            }
        }
    }

    override fun getUiEffectFlow() = viewModel.uiEffectFlow

    @Composable
    override fun ViewContent() {
        val uiState by viewModel.uiStateFlow.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.emit(TagsSearchUiIntent.CreatePage)
        }

        TagsSearchLayout(
            uiState = uiState,
            onIntent = { viewModel.emit(it) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.emit(TagsSearchUiIntent.Back)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.emit(TagsSearchUiIntent.CreatePage)
    }

    override suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        when (uiEffect) {
            is TagsSearchUiEffect.ReturnSelectedTags -> {
                val resultIntent = createResultIntent(uiEffect.tags)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            is TagsSearchUiEffect.NavigateBack -> {
                finish()
            }

            is TagsSearchUiEffect.NavigateToTagsEdit -> {
                startActivity(Intent(this, TagsEditActivity::class.java))
            }

            else -> super.onReceivedUiEffect(uiEffect)
        }
    }
}
