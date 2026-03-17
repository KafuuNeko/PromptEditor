package me.kafuuneko.prompteditor.feature.tagssearch

import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiEffect
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiIntent
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagsSearchViewModel :
    CoreViewModelWithUiEffect<TagsSearchUiIntent, TagsSearchUiState>(TagsSearchUiState.None),
    KoinComponent {

    private val _presetRepository by inject<PresetRepository>()

    @UiIntentObserver(TagsSearchUiIntent.CreatePage::class)
    suspend fun onCreatePage() {
        loadAllTags()
    }

    private suspend fun loadAllTags() {
        enqueueAsyncTask(Dispatchers.IO) {
            val tags = _presetRepository.getAllTags()
            TagsSearchUiState.Normal(
                allTags = tags,
                filteredTags = tags,
                selectedTags = emptySet(),
                searchQuery = "",
                isLoading = false
            ).setup()
        }
    }

    @UiIntentObserver(TagsSearchUiIntent.SearchTags::class)
    fun onSearchTags(intent: TagsSearchUiIntent.SearchTags) {
        val currentState = getOrNull<TagsSearchUiState.Normal>() ?: return
        val query = intent.query.lowercase()

        val filteredTags = if (query.isEmpty()) {
            currentState.allTags
        } else {
            // 同时匹配名称和描述
            currentState.allTags.filter {
                it.name.lowercase().contains(query) ||
                        it.description.lowercase().contains(query)
            }
        }

        currentState.copy(
            filteredTags = filteredTags,
            searchQuery = intent.query
        ).setup()
    }

    @UiIntentObserver(TagsSearchUiIntent.ToggleTagSelection::class)
    fun onToggleTagSelection(intent: TagsSearchUiIntent.ToggleTagSelection) {
        val currentState = getOrNull<TagsSearchUiState.Normal>() ?: return
        val newSelection = if (intent.tagName in currentState.selectedTags) {
            currentState.selectedTags - intent.tagName
        } else {
            currentState.selectedTags + intent.tagName
        }
        currentState.copy(selectedTags = newSelection).setup()
    }

    @UiIntentObserver(TagsSearchUiIntent.ConfirmSelection::class)
    fun onConfirmSelection() {
        val currentState = getOrNull<TagsSearchUiState.Normal>() ?: return
        val selectedTagsList = currentState.selectedTags.toList()
        TagsSearchUiEffect.ReturnSelectedTags(selectedTagsList).tryEmit()
    }

    @UiIntentObserver(TagsSearchUiIntent.Back::class)
    fun onBack() {
        TagsSearchUiEffect.NavigateBack.tryEmit()
    }

    @UiIntentObserver(TagsSearchUiIntent.OpenTagsEdit::class)
    fun onOpenTagsEdit() {
        TagsSearchUiEffect.NavigateToTagsEdit.tryEmit()
    }
}
