package me.kafuuneko.prompteditor.feature.preset

import android.content.Context
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditDialogState
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditMode
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiEffect
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiIntent
import me.kafuuneko.prompteditor.feature.preset.presentation.PresetEditUiState
import me.kafuuneko.prompteditor.libs.core.AppUiEffect
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import me.kafuuneko.prompteditor.libs.utils.IPromptsParser
import me.kafuuneko.prompteditor.libs.utils.NovelAIPromptsParser
import me.kafuuneko.prompteditor.libs.utils.SDPromptsParser
import me.kafuuneko.prompteditor.libs.utils.PromptItem
import me.kafuuneko.prompteditor.libs.utils.expand
import me.kafuuneko.prompteditor.libs.utils.fold
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PresetEditViewModel :
    CoreViewModelWithUiEffect<PresetEditUiIntent, PresetEditUiState>(PresetEditUiState.None),
    KoinComponent {
    private var _parser: IPromptsParser = NovelAIPromptsParser()

    private var _tagMap = emptyMap<String, Tag>()
    private val _context by inject<Context>()
    private val _presetRepository by inject<PresetRepository>()

    private var _currentPresetId: Long = 0L

    @UiIntentObserver(PresetEditUiIntent.CreatePage::class)
    suspend fun onCreatePage(intent: PresetEditUiIntent.CreatePage) {
        _tagMap = _presetRepository.getAllTags().associateBy { it.name }
        _currentPresetId = intent.presetId
        loadPreset()
    }

    private suspend fun loadPreset() {
        val preset = _presetRepository.getPresetById(_currentPresetId)
        if (preset != null) {
            val presetSet = _presetRepository.getPresetSetById(preset.presetSetId)
            _parser = if (presetSet?.parser == 1) SDPromptsParser() else NovelAIPromptsParser()
            val promptItems = _parser.parse(input = preset.prompts, tagMap = _tagMap)
            PresetEditUiState.Normal(
                presetId = preset.id,
                presetName = preset.name,
                mode = PresetEditMode.ListMode(promptItems.expand()),
                isSaved = true,
                dialogState = PresetEditDialogState.None
            ).setup()
        } else {
            AppUiEffect.PopupToastMessageByResId(R.string.failed_to_load_preset).tryEmit()
            PresetEditUiEffect.NavigateBack.tryEmit()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.UpdatePromptsText::class)
    fun onUpdatePromptsText(intent: PresetEditUiIntent.UpdatePromptsText) {
        getOrNull<PresetEditUiState.Normal>()?.copy(
            mode = PresetEditMode.TextMode(intent.text),
            isSaved = false
        )?.setup()
    }

    @UiIntentObserver(PresetEditUiIntent.ShowDeleteConfirmDialog::class)
    fun onShowDeleteConfirmDialog(intent: PresetEditUiIntent.ShowDeleteConfirmDialog) {
        val dialog = PresetEditDialogState.DeleteConfirm(
            index = intent.index,
            tagName = intent.tagName
        )
        getOrNull<PresetEditUiState.Normal>()
            ?.copy(dialogState = dialog)
            ?.setup()
    }

    @UiIntentObserver(PresetEditUiIntent.ShowEditDialog::class)
    fun onShowEditDialog(intent: PresetEditUiIntent.ShowEditDialog) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val currentMode = currentState.mode as? PresetEditMode.ListMode ?: return
        val items = currentMode.promptItems

        if (intent.index in items.indices) {
            val item = items[intent.index]
            val dialog = PresetEditDialogState.EditDialog(
                index = intent.index,
                tagName = item.tagName,
                weight = item.weight,
                group = item.group
            )
            currentState.copy(dialogState = dialog).setup()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ConfirmUpdateItem::class)
    fun onConfirmUpdateItem(intent: PresetEditUiIntent.ConfirmUpdateItem) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val currentMode = currentState.mode as? PresetEditMode.ListMode ?: return
        val items = currentMode.promptItems.toMutableList()

        if (intent.index in items.indices) {
            val oldGroup = items[intent.index].group
            val isChangingGroup = oldGroup != intent.group
            val newDescription =
                _tagMap[intent.tagName.lowercase()]?.description ?: items[intent.index].description

            val newGroupItems =
                items.filter { it.group == intent.group && it !== items[intent.index] }
            val finalWeight = if (isChangingGroup && newGroupItems.isNotEmpty()) {
                newGroupItems.first().weight
            } else {
                intent.weight
            }

            // 更新同一组内所有 item 的 weight 和 group。如果是修改单个 tagName，则只更新目标 index 的 tagName
            val updatedItems = items.mapIndexed { index, item ->
                if (index == intent.index) {
                    item.copy(
                        tagName = intent.tagName,
                        description = newDescription,
                        weight = finalWeight,
                        group = intent.group
                    )
                } else if (item.group == intent.group) {
                    item.copy(weight = finalWeight)
                } else {
                    item
                }
            }

            // 按 group 排序，使相同组别的 tags 排列在一起
            val sortedItems = updatedItems.sortedBy { it.group }

            currentState.copy(
                mode = currentMode.copy(promptItems = sortedItems),
                dialogState = PresetEditDialogState.None,
                isSaved = false
            ).setup()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ConfirmDeletePromptItem::class)
    suspend fun onConfirmDeletePromptItem() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val currentMode = currentState.mode as? PresetEditMode.ListMode ?: return
        val dialogState = currentState.dialogState
        if (dialogState is PresetEditDialogState.DeleteConfirm) {
            val updatedItems = currentMode.promptItems.toMutableList()
            if (dialogState.index in updatedItems.indices) {
                updatedItems.removeAt(dialogState.index)
                currentState.copy(
                    mode = currentMode.copy(promptItems = updatedItems),
                    dialogState = PresetEditDialogState.None,
                    isSaved = false
                ).setup()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ReorderPrompts::class)
    fun onReorderPrompts(intent: PresetEditUiIntent.ReorderPrompts) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val currentMode = currentState.mode as? PresetEditMode.ListMode ?: return
        val updatedItems = currentMode.promptItems.toMutableList()
        if (intent.fromIndex in updatedItems.indices && intent.toIndex in updatedItems.indices) {
            val item = updatedItems.removeAt(intent.fromIndex)
            updatedItems.add(intent.toIndex, item)
            currentState.copy(
                mode = currentMode.copy(promptItems = updatedItems),
                isSaved = false
            ).setup()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.ReassignGroups::class)
    fun onReassignGroups() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val currentMode = currentState.mode as? PresetEditMode.ListMode ?: return
        val updatedItems = currentMode.promptItems
        if (updatedItems.isEmpty()) return

        var currentGroupIndex = 0
        var currentOldGroup = updatedItems.first().group
        val newItems = updatedItems.map { item ->
            if (item.group != currentOldGroup) {
                currentGroupIndex++
                currentOldGroup = item.group
            }
            item.copy(group = currentGroupIndex)
        }

        currentState.copy(
            mode = currentMode.copy(promptItems = newItems),
            isSaved = false
        ).setup()
    }

    @UiIntentObserver(PresetEditUiIntent.ToggleEditMode::class)
    fun onToggleEditMode() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val mode = when (val currentMode = currentState.mode) {
            is PresetEditMode.ListMode -> {
                PresetEditMode.TextMode(_parser.stringify(currentState.mode.promptItems.fold()))
            }

            is PresetEditMode.TextMode -> {
                PresetEditMode.ListMode(_parser.parse(currentMode.promptsText, _tagMap).expand())
            }
        }
        currentState.copy(mode = mode).setup()
    }

    @UiIntentObserver(PresetEditUiIntent.SavePreset::class)
    suspend fun onSavePreset() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        val promptsText = when (val currentMode = currentState.mode) {
            is PresetEditMode.ListMode -> _parser.stringify(currentMode.promptItems.fold())
            is PresetEditMode.TextMode -> currentMode.promptsText
        }
        val preset = _presetRepository.getPresetById(_currentPresetId)
        if (preset != null) {
            _presetRepository.updatePreset(preset.copy(prompts = promptsText))
            currentState.copy(isSaved = true).setup()
            AppUiEffect.PopupToastMessageByResId(R.string.saved).tryEmit()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.OpenTagsSearch::class)
    fun onOpenTagsSearch() {
        PresetEditUiEffect.NavigateToTagsSearch.tryEmit()
    }

    @UiIntentObserver(PresetEditUiIntent.AddTagsFromSearch::class)
    suspend fun onAddTagsFromSearch(intent: PresetEditUiIntent.AddTagsFromSearch) {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        when (val mode = currentState.mode) {
            is PresetEditMode.ListMode -> {
                val allTags = _presetRepository.getAllTags()
                val tagMap = allTags.associateBy { it.name.lowercase() }

                val newItems = intent.tags.map { tagName ->
                    val description = tagMap[tagName.lowercase()]?.description ?: ""
                    PromptItem(tagName = tagName, description = description)
                }

                val updatedItems = mode.promptItems + newItems
                currentState.copy(
                    mode = mode.copy(promptItems = updatedItems),
                    isSaved = false
                ).setup()
                AppUiEffect.PopupToastMessage(
                    _context.getString(R.string.tags_added, newItems.size)
                ).emit()
            }

            is PresetEditMode.TextMode -> {
                val newText = if (mode.promptsText.isBlank()) {
                    intent.tags.joinToString(", ")
                } else {
                    "${mode.promptsText}, ${intent.tags.joinToString(", ")}"
                }
                currentState.copy(
                    mode = mode.copy(promptsText = newText),
                    isSaved = false
                ).setup()
            }
        }
    }

    @UiIntentObserver(PresetEditUiIntent.Back::class)
    fun onBack() {
        val currentState = getOrNull<PresetEditUiState.Normal>()
        if (currentState != null && !currentState.isSaved) {
            currentState.copy(dialogState = PresetEditDialogState.UnsavedChangesConfirm).setup()
        } else {
            PresetEditUiEffect.NavigateBack.tryEmit()
        }
    }

    @UiIntentObserver(PresetEditUiIntent.DismissDialog::class)
    fun onDismissDialog() {
        val currentState = getOrNull<PresetEditUiState.Normal>() ?: return
        currentState.copy(dialogState = PresetEditDialogState.None).setup()
    }

    @UiIntentObserver(PresetEditUiIntent.ConfirmDiscardChanges::class)
    fun onConfirmDiscardChanges() {
        PresetEditUiEffect.NavigateBack.tryEmit()
    }
}
