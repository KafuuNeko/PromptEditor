package me.kafuuneko.prompteditor.feature.tagsedit

import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.ImportResult
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditDialogState
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditUiEffect
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditUiIntent
import me.kafuuneko.prompteditor.feature.tagsedit.presentation.TagsEditUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import me.kafuuneko.prompteditor.libs.utils.CsvParser
import me.kafuuneko.prompteditor.libs.utils.TextSearcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagsEditViewModel :
    CoreViewModelWithUiEffect<TagsEditUiIntent, TagsEditUiState>(TagsEditUiState.None),
    KoinComponent {

    private val _presetRepository by inject<PresetRepository>()

    @UiIntentObserver(TagsEditUiIntent.CreatePage::class)
    suspend fun onCreatePage() {
        loadTags()
    }

    @UiIntentObserver(TagsEditUiIntent.LoadTags::class)
    suspend fun onLoadTags() {
        loadTags()
    }

    private suspend fun loadTags() {
        enqueueAsyncTask(Dispatchers.IO) {
            val tags = _presetRepository.getAllTags()
            TagsEditUiState.Normal(
                tags = tags,
                filteredTags = tags,
                searchQuery = "",
                isLoading = false,
                dialogState = TagsEditDialogState.None
            ).setup()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.ImportFromCsv::class)
    suspend fun onImportFromCsv(intent: TagsEditUiIntent.ImportFromCsv) {
        enqueueAsyncTask(Dispatchers.IO) {
            val existingTags = _presetRepository.getAllTags()

            val result = CsvParser.processTagImport(
                csvContent = intent.csvContent,
                existingTags = existingTags
            ) { tags ->
                _presetRepository.insertTags(tags)
            }

            val currentState = getOrNull<TagsEditUiState.Normal>()
            val allTags = _presetRepository.getAllTags()
            if (currentState != null) {
                currentState.copy(
                    tags = allTags,
                    filteredTags = TextSearcher.filterTags(allTags, currentState.searchQuery),
                    isLoading = false,
                    importResult = result,
                    dialogState = TagsEditDialogState.None
                ).setup()
            } else {
                TagsEditUiState.Normal(
                    tags = allTags,
                    filteredTags = allTags,
                    searchQuery = "",
                    isLoading = false,
                    importResult = result,
                    dialogState = TagsEditDialogState.None
                ).setup()
            }
            TagsEditUiEffect.ShowToast(
                R.string.import_complete,
                listOf(result.successCount, result.failCount)
            ).tryEmit()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.AddTag::class)
    suspend fun onAddTag(intent: TagsEditUiIntent.AddTag) {
        enqueueAsyncTask(Dispatchers.IO) {
            val newTag = Tag(name = intent.name, description = intent.description)
            _presetRepository.insertTag(newTag)
            TagsEditUiEffect.ShowToast(R.string.tag_added, listOf(intent.name)).tryEmit()
            val currentState = getOrNull<TagsEditUiState.Normal>() ?: return@enqueueAsyncTask
            val allTags = _presetRepository.getAllTags()
            currentState.copy(
                dialogState = TagsEditDialogState.None,
                tags = allTags,
                filteredTags = TextSearcher.filterTags(allTags, currentState.searchQuery)
            ).setup()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.UpdateTag::class)
    suspend fun onUpdateTag(intent: TagsEditUiIntent.UpdateTag) {
        enqueueAsyncTask(Dispatchers.IO) {
            val existingTag = _presetRepository.getTagByName(intent.name)
            if (existingTag != null && existingTag.id != intent.id) {
                TagsEditUiEffect.ShowToast(R.string.tag_name_exists).tryEmit()
            } else {
                _presetRepository.insertTag(
                    Tag(id = intent.id, name = intent.name, description = intent.description)
                )
                TagsEditUiEffect.ShowToast(R.string.tag_updated).tryEmit()
            }

            val currentState = getOrNull<TagsEditUiState.Normal>() ?: return@enqueueAsyncTask
            val allTags = _presetRepository.getAllTags()
            currentState.copy(
                dialogState = TagsEditDialogState.None,
                tags = allTags,
                filteredTags = TextSearcher.filterTags(allTags, currentState.searchQuery)
            ).setup()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.DeleteTag::class)
    suspend fun onDeleteTag(intent: TagsEditUiIntent.DeleteTag) {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deleteTagById(intent.id)
            TagsEditUiEffect.ShowToast(R.string.tag_deleted).tryEmit()

            val currentState = getOrNull<TagsEditUiState.Normal>() ?: return@enqueueAsyncTask
            val allTags = _presetRepository.getAllTags()
            currentState.copy(
                dialogState = TagsEditDialogState.None,
                tags = allTags,
                filteredTags = TextSearcher.filterTags(allTags, currentState.searchQuery)
            ).setup()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.ClearAllTags::class)
    suspend fun onClearAllTags() {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deleteAllTags()
            val currentState = getOrNull<TagsEditUiState.Normal>() ?: return@enqueueAsyncTask
            currentState.copy(
                tags = emptyList(),
                filteredTags = emptyList(),
                searchQuery = "",
                isLoading = false
            ).setup()
            TagsEditUiEffect.ShowToast(R.string.all_tags_cleared).tryEmit()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.ShowAddDialog::class)
    fun onShowAddDialog() {
        val currentState = getOrNull<TagsEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = TagsEditDialogState.AddTag()
        ).setup()
    }

    @UiIntentObserver(TagsEditUiIntent.ShowEditDialog::class)
    fun onShowEditDialog(intent: TagsEditUiIntent.ShowEditDialog) {
        val currentState = getOrNull<TagsEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = TagsEditDialogState.EditTag(
                tagId = intent.tagId,
                name = intent.tagName,
                description = intent.tagDescription
            )
        ).setup()
    }

    @UiIntentObserver(TagsEditUiIntent.ShowDeleteConfirmDialog::class)
    fun onShowDeleteConfirmDialog(intent: TagsEditUiIntent.ShowDeleteConfirmDialog) {
        val currentState = getOrNull<TagsEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = TagsEditDialogState.DeleteConfirm(
                tagId = intent.tagId,
                tagName = intent.tagName
            )
        ).setup()
    }

    @UiIntentObserver(TagsEditUiIntent.ShowClearConfirmDialog::class)
    fun onShowClearConfirmDialog() {
        val currentState = getOrNull<TagsEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = TagsEditDialogState.ClearConfirm
        ).setup()
    }

    @UiIntentObserver(TagsEditUiIntent.ConfirmClearAll::class)
    suspend fun onConfirmClearAll() {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deleteAllTags()
            TagsEditUiEffect.ShowToast(R.string.all_tags_cleared).tryEmit()
        }
        onDismissDialog()
    }

    @UiIntentObserver(TagsEditUiIntent.DismissDialog::class)
    fun onDismissDialog() {
        val currentState = getOrNull<TagsEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = TagsEditDialogState.None
        ).setup()
    }

    @UiIntentObserver(TagsEditUiIntent.SearchTags::class)
    fun onSearchTags(intent: TagsEditUiIntent.SearchTags) {
        val currentState = getOrNull<TagsEditUiState.Normal>() ?: return
        val query = intent.query.lowercase()

        val filteredTags = if (query.isEmpty()) {
            currentState.tags
        } else {
            currentState.tags.filter {
                it.name.lowercase().contains(query) ||
                        it.description.lowercase().contains(query)
            }
        }

        currentState.copy(
            filteredTags = filteredTags,
            searchQuery = intent.query
        ).setup()
    }

    @UiIntentObserver(TagsEditUiIntent.Back::class)
    fun onBack() {
        TagsEditUiEffect.NavigateBack.tryEmit()
    }
}
