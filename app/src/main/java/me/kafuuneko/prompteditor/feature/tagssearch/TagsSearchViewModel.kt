package me.kafuuneko.prompteditor.feature.tagssearch

import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiEffect
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiIntent
import me.kafuuneko.prompteditor.feature.tagssearch.presentation.TagsSearchUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import me.kafuuneko.prompteditor.libs.utils.TextSearcher
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
        val tags = _presetRepository.getAllTags()
        val currentState = getOrNull<TagsSearchUiState.Normal>() ?: TagsSearchUiState.Normal()
        currentState.copy(
            allTags = tags,
            filteredTags = TextSearcher.filterTags(tags, currentState.searchQuery)
        ).setup()
    }

    @UiIntentObserver(TagsSearchUiIntent.SearchTags::class)
    fun onSearchTags(intent: TagsSearchUiIntent.SearchTags) {
        val currentState = getOrNull<TagsSearchUiState.Normal>() ?: return
        val filteredTags = TextSearcher.filterTags(currentState.allTags, intent.query)

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
