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
                isLoading = false,
                dialogState = TagsEditDialogState.None
            ).setup()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.ImportFromCsv::class)
    suspend fun onImportFromCsv(intent: TagsEditUiIntent.ImportFromCsv) {
        enqueueAsyncTask(Dispatchers.IO) {
            val csvContent = intent.csvContent
            val lines = csvContent.lines().filter { it.isNotBlank() }

            var successCount = 0
            var failCount = 0
            var updateCount = 0
            val tagsToInsert = mutableListOf<Tag>()
            val tagsToUpdate = mutableListOf<Tag>()

            // 获取所有已存在的 Tag
            val existingTags = _presetRepository.getAllTags()
            val existingTagsMap = existingTags.associateBy { it.name.lowercase() }

            for (line in lines) {
                try {
                    val parts = line.split(",", limit = 2)
                    if (parts.isNotEmpty()) {
                        val tagName = parts[0].trim().removeSurrounding("\"")
                        val description =
                            if (parts.size > 1) parts[1].trim().removeSurrounding("\"") else ""
                        if (tagName.isNotEmpty()) {
                            val existingTag = existingTagsMap[tagName.lowercase()]
                            if (existingTag != null) {
                                // 存在同名 Tag，执行覆盖更新
                                tagsToUpdate.add(existingTag.copy(description = description))
                                updateCount++
                            } else {
                                // 不存在，插入新 Tag
                                tagsToInsert.add(Tag(name = tagName, description = description))
                            }
                            successCount++
                        } else {
                            failCount++
                        }
                    } else {
                        failCount++
                    }
                } catch (e: Exception) {
                    failCount++
                }
            }

            // 批量插入新 Tags
            if (tagsToInsert.isNotEmpty()) {
                _presetRepository.insertTags(tagsToInsert)
            }

            // 批量更新已存在的 Tags（使用 insertOrReplace）
            if (tagsToUpdate.isNotEmpty()) {
                _presetRepository.insertTags(tagsToUpdate)
            }

            val result = ImportResult(
                successCount = successCount,
                updateCount = updateCount,
                failCount = failCount,
                totalCount = lines.size
            )

            val currentState = getOrNull<TagsEditUiState.Normal>()
            if (currentState != null) {
                currentState.copy(
                    tags = _presetRepository.getAllTags(),
                    isLoading = false,
                    importResult = result,
                    dialogState = TagsEditDialogState.None
                ).setup()
            } else {
                TagsEditUiState.Normal(
                    tags = _presetRepository.getAllTags(),
                    isLoading = false,
                    importResult = result,
                    dialogState = TagsEditDialogState.None
                ).setup()
            }
            TagsEditUiEffect.ShowToast(
                R.string.import_complete,
                listOf(tagsToInsert.size + updateCount, failCount)
            ).tryEmit()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.AddTag::class)
    suspend fun onAddTag(intent: TagsEditUiIntent.AddTag) {
        enqueueAsyncTask(Dispatchers.IO) {
            val newTag = Tag(name = intent.name, description = intent.description)
            _presetRepository.insertTag(newTag)
            TagsEditUiEffect.ShowToast(R.string.tag_added, listOf(intent.name)).tryEmit()
            getOrNull<TagsEditUiState.Normal>()
                ?.copy(
                    dialogState = TagsEditDialogState.None,
                    tags = _presetRepository.getAllTags()
                )
                ?.setup()
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

            getOrNull<TagsEditUiState.Normal>()
                ?.copy(
                    dialogState = TagsEditDialogState.None,
                    tags = _presetRepository.getAllTags()
                )
                ?.setup()
        }
    }

    @UiIntentObserver(TagsEditUiIntent.DeleteTag::class)
    suspend fun onDeleteTag(intent: TagsEditUiIntent.DeleteTag) {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deleteTagById(intent.id)
            TagsEditUiEffect.ShowToast(R.string.tag_deleted).tryEmit()

            val currentState = getOrNull<TagsEditUiState.Normal>()
            if (currentState != null) {
                currentState.copy(
                    dialogState = TagsEditDialogState.None,
                    tags = _presetRepository.getAllTags()
                ).setup()
            }
        }
    }

    @UiIntentObserver(TagsEditUiIntent.ClearAllTags::class)
    suspend fun onClearAllTags() {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deleteAllTags()
            val currentState = getOrNull<TagsEditUiState.Normal>()
            if (currentState != null) {
                currentState.copy(
                    tags = emptyList(),
                    isLoading = false
                ).setup()
            }
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

    @UiIntentObserver(TagsEditUiIntent.Back::class)
    fun onBack() {
        TagsEditUiEffect.NavigateBack.tryEmit()
    }
}
