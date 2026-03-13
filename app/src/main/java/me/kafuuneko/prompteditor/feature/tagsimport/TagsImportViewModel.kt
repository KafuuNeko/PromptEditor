package me.kafuuneko.prompteditor.feature.tagsimport

import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.ImportResult
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportDialogState
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiEffect
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiIntent
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TagsImportViewModel :
    CoreViewModelWithUiEffect<TagsImportUiIntent, TagsImportUiState>(TagsImportUiState.Normal()),
    KoinComponent {

    private val _presetRepository by inject<PresetRepository>()

    @UiIntentObserver(TagsImportUiIntent.CreatePage::class)
    suspend fun onCreatePage() {
        loadTags()
    }

    @UiIntentObserver(TagsImportUiIntent.LoadTags::class)
    suspend fun onLoadTags() {
        loadTags()
    }

    private suspend fun loadTags() {
        enqueueAsyncTask(Dispatchers.IO) {
            val tags = _presetRepository.getAllTags()
            TagsImportUiState.Normal(
                tags = tags,
                isLoading = false
            ).setup()
        }
    }

    @UiIntentObserver(TagsImportUiIntent.ImportFromCsv::class)
    suspend fun onImportFromCsv(intent: TagsImportUiIntent.ImportFromCsv) {
        enqueueAsyncTask(Dispatchers.IO) {
            val csvContent = intent.csvContent
            val lines = csvContent.lines().filter { it.isNotBlank() }

            var successCount = 0
            var failCount = 0

            val tagsToInsert = mutableListOf<Tag>()

            for (line in lines) {
                try {
                    // 解析CSV行，格式: tag名称, tag描述
                    val parts = line.split(",", limit = 2)
                    if (parts.isNotEmpty()) {
                        val tagName = parts[0].trim().removeSurrounding("\"")
                        val description = if (parts.size > 1) {
                            parts[1].trim().removeSurrounding("\"")
                        } else ""

                        if (tagName.isNotEmpty()) {
                            tagsToInsert.add(Tag(name = tagName, description = description))
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

            if (tagsToInsert.isNotEmpty()) {
                _presetRepository.insertTags(tagsToInsert)
            }

            val result = ImportResult(
                successCount = successCount,
                failCount = failCount,
                totalCount = lines.size
            )

            TagsImportUiState.Normal(
                tags = _presetRepository.getAllTags(),
                isLoading = false,
                importResult = result
            ).setup()

            TagsImportUiEffect.ShowToast(
                R.string.import_complete, arrayOf(successCount, failCount)
            ).tryEmit()
        }
    }

    @UiIntentObserver(TagsImportUiIntent.ClearAllTags::class)
    suspend fun onClearAllTags() {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deleteAllTags()
            TagsImportUiState.Normal(
                tags = emptyList(),
                isLoading = false
            ).setup()
            TagsImportUiEffect.ShowToast(R.string.all_tags_cleared).tryEmit()
        }
    }

    @UiIntentObserver(TagsImportUiIntent.ShowClearConfirmDialog::class)
    fun onShowClearConfirmDialog() {
        getOrNull<TagsImportUiState.Normal>()
            ?.copy(dialogState = TagsImportDialogState.ClearConfirm)
            ?.setup()
    }

    @UiIntentObserver(TagsImportUiIntent.ConfirmClearAll::class)
    suspend fun onConfirmClearAll() {
        onClearAllTags()
        onDismissDialog()
    }

    @UiIntentObserver(TagsImportUiIntent.DismissDialog::class)
    fun onDismissDialog() {
        getOrNull<TagsImportUiState.Normal>()
            ?.copy(dialogState = TagsImportDialogState.None)
            ?.setup()
    }

    @UiIntentObserver(TagsImportUiIntent.Back::class)
    fun onBack() {
        TagsImportUiEffect.NavigateBack.tryEmit()
    }
}
