package me.kafuuneko.prompteditor.feature.preset

import android.content.Context
import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditDialogState
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiEffect
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiIntent
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiState
import me.kafuuneko.prompteditor.feature.preset.presentation.PromptItem
import me.kafuuneko.prompteditor.libs.core.AppUiEffect
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.regex.Pattern

class PresetEditViewModel :
    CoreViewModelWithUiEffect<PresetEditUiIntent, PresetEditUiState>(PresetEditUiState.None),
    KoinComponent {

    private val _context by inject<Context>()
    private val _presetRepository by inject<PresetRepository>()

    private var _currentPresetId: Long = 0L

    companion object {
        // 匹配格式: {tag} 或 {tag:weight} 或 [tag] 或 [tag:weight]
        private val WEIGHT_PATTERN =
            Pattern.compile("^\\{([^:}]+)(?::([^}]+))?\\}$|^\\[([^:\\]]+)(?::([^\\]]+))?\\]$")
        private val TAG_SPLITTER = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")
    }

    @UiIntentObserver(PresetEditUiIntent.CreatePage::class)
    suspend fun onCreatePage(intent: PresetEditUiIntent.CreatePage) {
        _currentPresetId = intent.presetId
        loadPreset()
    }

    @UiIntentObserver(PresetEditUiIntent.LoadPreset::class)
    suspend fun onLoadPreset() {
        loadPreset()
    }

    private suspend fun loadPreset() {
        enqueueAsyncTask(Dispatchers.IO) {
            val preset = _presetRepository.getPresetById(_currentPresetId)
            if (preset != null) {
                val allTags = _presetRepository.getAllTags()
                val promptItems = parsePromptsToItems(preset.prompts, allTags)
                PresetEditUiState.Normal(
                    presetId = preset.id,
                    presetName = preset.name,
                    promptsText = preset.prompts,
                    promptItems = promptItems,
                    isTextMode = false,
                    isLoading = false,
                    isSaved = true,
                    dialogState = PresetEditDialogState.None
                ).setup()
            } else {
                AppUiEffect.PopupToastMessageByResId(R.string.failed_to_load_preset).tryEmit()
                PresetEditUiEffect.NavigateBack.tryEmit()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.UpdatePromptsText::class)
    fun onUpdatePromptsText(intent: PresetEditUiIntent.UpdatePromptsText) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        currentState.copy(
            promptsText = intent.text,
            isSaved = false
        ).setup()
    }

    @UiIntentObserver(PresetEditUiIntent.UpdatePromptItem::class)
    fun onUpdatePromptItem(intent: PresetEditUiIntent.UpdatePromptItem) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val updatedItems = currentState.promptItems.toMutableList()
        if (intent.index in updatedItems.indices) {
            updatedItems[intent.index] = updatedItems[intent.index].copy(
                originalText = intent.newTag,
                tagName = extractTagName(intent.newTag)
            )
            currentState.copy(
                promptItems = updatedItems,
                isSaved = false
            ).setup()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ConfirmUpdatePromptItem::class)
    suspend fun onConfirmUpdatePromptItem(intent: PresetEditUiIntent.ConfirmUpdatePromptItem) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val updatedItems = currentState.promptItems.toMutableList()
        if (intent.index in updatedItems.indices) {
            // 从数据库重新查找修改后的 tag 描述
            enqueueAsyncTask(Dispatchers.IO) {
                val allTags = _presetRepository.getAllTags()
                val tagMap = allTags.associateBy { it.name.lowercase() }
                val newTagName = extractTagName(intent.newTag)
                val newDescription = tagMap[newTagName.lowercase()]?.description ?: ""

                updatedItems[intent.index] = updatedItems[intent.index].copy(
                    originalText = intent.newTag,
                    tagName = newTagName,
                    description = newDescription
                )
                currentState.copy(
                    promptItems = updatedItems,
                    isSaved = false
                ).setup()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.CancelEditPromptItem::class)
    suspend fun onCancelEditPromptItem(intent: PresetEditUiIntent.CancelEditPromptItem) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        // 取消编辑，恢复原始数据（重新加载）
        enqueueAsyncTask(Dispatchers.IO) {
            val allTags = _presetRepository.getAllTags()
            val tagMap = allTags.associateBy { it.name.lowercase() }

            val updatedItems = currentState.promptItems.toMutableList()
            if (intent.index in updatedItems.indices) {
                val item = updatedItems[intent.index]
                val originalDescription =
                    tagMap[item.tagName.lowercase()]?.description ?: item.description
                updatedItems[intent.index] = item.copy(description = originalDescription)
            }
            currentState.copy(promptItems = updatedItems).setup()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ShowDeleteConfirmDialog::class)
    fun onShowDeleteConfirmDialog(intent: PresetEditUiIntent.ShowDeleteConfirmDialog) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = PresetEditDialogState.DeleteConfirm(
                index = intent.index,
                tagName = intent.tagName
            )
        ).setup()
    }

    @UiIntentObserver(PresetEditUiIntent.ConfirmDeletePromptItem::class)
    suspend fun onConfirmDeletePromptItem() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val dialogState = currentState.dialogState
        if (dialogState is PresetEditDialogState.DeleteConfirm) {
            val updatedItems = currentState.promptItems.toMutableList()
            if (dialogState.index in updatedItems.indices) {
                updatedItems.removeAt(dialogState.index)
                currentState.copy(
                    promptItems = updatedItems,
                    dialogState = PresetEditDialogState.None,
                    isSaved = false
                ).setup()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ReorderPrompts::class)
    fun onReorderPrompts(intent: PresetEditUiIntent.ReorderPrompts) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val updatedItems = currentState.promptItems.toMutableList()
        if (intent.fromIndex in updatedItems.indices && intent.toIndex in updatedItems.indices) {
            val item = updatedItems.removeAt(intent.fromIndex)
            updatedItems.add(intent.toIndex, item)
            currentState.copy(
                promptItems = updatedItems,
                isSaved = false
            ).setup()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ToggleEditMode::class)
    suspend fun onToggleEditMode(intent: PresetEditUiIntent.ToggleEditMode) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return

        if (intent.isTextMode) {
            // 切换到文本模式，将items转换为文本
            val text = convertItemsToText(currentState.promptItems)
            currentState.copy(
                isTextMode = true,
                promptsText = text,
                isSaved = false
            ).setup()
        } else {
            // 切换到列表模式，解析文本为items
            enqueueAsyncTask(Dispatchers.IO) {
                val allTags = _presetRepository.getAllTags()
                val promptItems = parsePromptsToItems(currentState.promptsText, allTags)
                currentState.copy(
                    isTextMode = false,
                    promptItems = promptItems,
                    isSaved = false
                ).setup()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.SavePreset::class)
    suspend fun onSavePreset() {
        enqueueAsyncTask(Dispatchers.IO) {
            val currentState = getOrNull<PresetEditUiState.Normal>() ?: return@enqueueAsyncTask

            val promptsText = if (currentState.isTextMode) {
                currentState.promptsText
            } else {
                convertItemsToText(currentState.promptItems)
            }

            val preset = _presetRepository.getPresetById(_currentPresetId)
            if (preset != null) {
                val updatedPreset = preset.copy(prompts = promptsText)
                _presetRepository.updatePreset(updatedPreset)
                currentState.copy(
                    promptsText = promptsText,
                    isSaved = true
                ).setup()
                AppUiEffect.PopupToastMessageByResId(R.string.saved).tryEmit()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.OpenTagsSearch::class)
    fun onOpenTagsSearch() {
        PresetEditUiEffect.NavigateToTagsSearch.tryEmit()
    }

    @UiIntentObserver(PresetEditUiIntent.AddTagsFromSearch::class)
    suspend fun onAddTagsFromSearch(intent: PresetEditUiIntent.AddTagsFromSearch) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return

        if (currentState.isTextMode) {
            // 文本模式：直接追加到文本末尾
            val newText = if (currentState.promptsText.isBlank()) {
                intent.tags.joinToString(", ")
            } else {
                "${currentState.promptsText}, ${intent.tags.joinToString(", ")}"
            }
            currentState.copy(
                promptsText = newText,
                isSaved = false
            ).setup()
        } else {
            // 列表模式：追加到列表末尾
            enqueueAsyncTask(Dispatchers.IO) {
                val allTags = _presetRepository.getAllTags()
                val tagMap = allTags.associateBy { it.name.lowercase() }

                val newItems = intent.tags.map { tagName ->
                    val description = tagMap[tagName.lowercase()]?.description ?: ""
                    PromptItem(
                        originalText = tagName,
                        tagName = tagName,
                        weight = "",
                        description = description
                    )
                }

                val updatedItems = currentState.promptItems + newItems
                currentState.copy(
                    promptItems = updatedItems,
                    isSaved = false
                ).setup()
                AppUiEffect.PopupToastMessage(_context.getString(R.string.tags_added, newItems.size)).emit()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.Back::class)
    fun onBack() {
        val currentState = getOrNull<PresetEditUiState.Normal>()
        if (currentState != null && !currentState.isSaved) {
            currentState.copy(
                dialogState = PresetEditDialogState.UnsavedChangesConfirm
            ).setup()
        } else {
            PresetEditUiEffect.NavigateBack.tryEmit()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.DismissDialog::class)
    fun onDismissDialog() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = PresetEditDialogState.None
        ).setup()
    }

    @UiIntentObserver(PresetEditUiIntent.ConfirmDiscardChanges::class)
    fun onConfirmDiscardChanges() {
        PresetEditUiEffect.NavigateBack.tryEmit()
    }

    private fun parsePromptsToItems(prompts: String, allTags: List<Tag>): List<PromptItem> {
        if (prompts.isBlank()) return emptyList()

        val items = mutableListOf<PromptItem>()
        val tags = TAG_SPLITTER.split(prompts.trim())
        val tagMap = allTags.associateBy { it.name.lowercase() }

        for (tag in tags) {
            val trimmed = tag.trim()
            if (trimmed.isEmpty()) continue

            val (tagName, weight) = extractTagAndWeight(trimmed)
            val tagDescription = tagMap[tagName.lowercase()]?.description ?: ""

            items.add(
                PromptItem(
                    originalText = trimmed,
                    tagName = tagName,
                    weight = weight,
                    description = tagDescription
                )
            )
        }

        return items
    }

    private fun extractTagAndWeight(text: String): Pair<String, String> {
        val matcher = WEIGHT_PATTERN.matcher(text)
        return if (matcher.matches()) {
            val tagName = matcher.group(1) ?: matcher.group(3) ?: text
            val weight = matcher.group(2) ?: matcher.group(4) ?: ""
            tagName to weight
        } else {
            text to ""
        }
    }

    private fun extractTagName(text: String): String {
        val (tagName, _) = extractTagAndWeight(text)
        return tagName
    }

    private fun convertItemsToText(items: List<PromptItem>): String {
        return items.joinToString(", ") { item ->
            if (item.weight.isNotEmpty()) {
                "{${item.tagName}:${item.weight}}"
            } else {
                item.tagName
            }
        }
    }
}
