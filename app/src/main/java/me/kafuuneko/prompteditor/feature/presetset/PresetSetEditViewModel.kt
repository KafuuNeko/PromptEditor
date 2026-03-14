package me.kafuuneko.prompteditor.feature.presetset

import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditDialogState
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiEffect
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiIntent
import me.kafuuneko.prompteditor.feature.presetset.presentation.PresetSetEditUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.entity.Preset
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PresetSetEditViewModel :
    CoreViewModelWithUiEffect<PresetSetEditUiIntent, PresetSetEditUiState>(PresetSetEditUiState.None),
    KoinComponent {

    private val _presetRepository by inject<PresetRepository>()

    private var _currentPresetSetId: Long = 0L

    @UiIntentObserver(PresetSetEditUiIntent.CreatePage::class)
    suspend fun onCreatePage(intent: PresetSetEditUiIntent.CreatePage) {
        _currentPresetSetId = intent.presetSetId
        loadPresets()
    }

    @UiIntentObserver(PresetSetEditUiIntent.LoadPresets::class)
    suspend fun onLoadPresets() {
        loadPresets()
    }

    private suspend fun loadPresets() {
        enqueueAsyncTask(Dispatchers.IO) {
            val presetSet = _presetRepository.getPresetSetById(_currentPresetSetId)
            val presets = _presetRepository.getPresetsByPresetSetId(_currentPresetSetId)
            PresetSetEditUiState.Normal(
                presetSetId = _currentPresetSetId,
                presetSetName = presetSet?.name ?: "",
                presets = presets,
                isLoading = false,
                dialogState = PresetSetEditDialogState.None
            ).setup()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.CreatePreset::class)
    suspend fun onCreatePreset(intent: PresetSetEditUiIntent.CreatePreset) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>()
        enqueueAsyncTask(Dispatchers.IO) {
            val newPreset = Preset(
                presetSetId = _currentPresetSetId,
                name = intent.name,
                prompts = "",
                order = currentState?.presets?.size ?: 0
            )
            _presetRepository.insertPreset(newPreset)
            PresetSetEditUiEffect.ShowToast(R.string.preset_created, listOf(intent.name)).tryEmit()
            loadPresets()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.DeletePreset::class)
    suspend fun onDeletePreset(intent: PresetSetEditUiIntent.DeletePreset) {
        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deletePreset(intent.id)
            PresetSetEditUiEffect.ShowToast(R.string.preset_deleted).tryEmit()
            loadPresets()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.ShowCreateDialog::class)
    fun onShowCreateDialog() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = PresetSetEditDialogState.CreatePreset
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.ShowDeleteConfirmDialog::class)
    fun onShowDeleteConfirmDialog(intent: PresetSetEditUiIntent.ShowDeleteConfirmDialog) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            isMultiSelectMode = false,
            selectedPresetIds = emptySet(),
            dialogState = PresetSetEditDialogState.DeleteConfirm(
                presetName = intent.presetName,
                presetId = intent.presetId
            )
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.DismissDialog::class)
    fun onDismissDialog() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = PresetSetEditDialogState.None
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.ConfirmDelete::class)
    suspend fun onConfirmDelete(intent: PresetSetEditUiIntent.ConfirmDelete) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = PresetSetEditDialogState.None
        ).setup()

        enqueueAsyncTask(Dispatchers.IO) {
            _presetRepository.deletePreset(intent.presetId)
            PresetSetEditUiEffect.ShowToast(R.string.preset_deleted).tryEmit()
            loadPresets()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.OpenPreset::class)
    fun onOpenPreset(intent: PresetSetEditUiIntent.OpenPreset) {
        PresetSetEditUiEffect.NavigateToPresetEdit(intent.id).tryEmit()
    }

    @UiIntentObserver(PresetSetEditUiIntent.CopyPrompts::class)
    fun onCopyPrompts(intent: PresetSetEditUiIntent.CopyPrompts) {
        PresetSetEditUiEffect.CopyToClipboard(intent.prompts).tryEmit()
        PresetSetEditUiEffect.ShowToast(R.string.copied_to_clipboard).tryEmit()
    }

    @UiIntentObserver(PresetSetEditUiIntent.Back::class)
    fun onBack() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        if (currentState.isMultiSelectMode) {
            currentState.copy(isMultiSelectMode = false, selectedPresetIds = emptySet()).setup()
        } else {
            PresetSetEditUiEffect.NavigateBack.tryEmit()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.ShowRenameDialog::class)
    fun onShowRenameDialog(intent: PresetSetEditUiIntent.ShowRenameDialog) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            isMultiSelectMode = false,
            selectedPresetIds = emptySet(),
            dialogState = PresetSetEditDialogState.RenamePreset(
                presetName = intent.presetName,
                presetId = intent.presetId
            )
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.ConfirmRename::class)
    suspend fun onConfirmRename(intent: PresetSetEditUiIntent.ConfirmRename) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            dialogState = PresetSetEditDialogState.None
        ).setup()

        enqueueAsyncTask(Dispatchers.IO) {
            val preset = currentState.presets.find { it.id == intent.presetId }
            if (preset != null) {
                _presetRepository.updatePreset(preset.copy(name = intent.newName))
                PresetSetEditUiEffect.ShowToast(R.string.preset_renamed, listOf(intent.newName)).tryEmit()
                loadPresets()
            }
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.ReorderPresets::class)
    suspend fun onReorderPresets(intent: PresetSetEditUiIntent.ReorderPresets) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        if (currentState.isMultiSelectMode) return

        val updatedPresets = currentState.presets.toMutableList()
        if (intent.fromIndex in updatedPresets.indices && intent.toIndex in updatedPresets.indices) {
            val item = updatedPresets.removeAt(intent.fromIndex)
            updatedPresets.add(intent.toIndex, item)

            val updatedPresetsWithOrder = updatedPresets.mapIndexed { index, preset ->
                preset.copy(order = index)
            }

            currentState.copy(presets = updatedPresetsWithOrder).setup()

            enqueueAsyncTask(Dispatchers.IO) {
                updatedPresetsWithOrder.forEach { preset ->
                    _presetRepository.updatePresetOrder(preset.id, preset.order)
                }
            }
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.EnterMultiSelectMode::class)
    fun onEnterMultiSelectMode(intent: PresetSetEditUiIntent.EnterMultiSelectMode) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            isMultiSelectMode = true,
            selectedPresetIds = setOf(intent.presetId)
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.ExitMultiSelectMode::class)
    fun onExitMultiSelectMode() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        currentState.copy(
            isMultiSelectMode = false,
            selectedPresetIds = emptySet()
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.TogglePresetSelection::class)
    fun onTogglePresetSelection(intent: PresetSetEditUiIntent.TogglePresetSelection) {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        if (!currentState.isMultiSelectMode) return

        val newSelectedIds = currentState.selectedPresetIds.toMutableSet()
        if (intent.presetId in newSelectedIds) {
            newSelectedIds.remove(intent.presetId)
        } else {
            newSelectedIds.add(intent.presetId)
        }

        // If no items selected, exit multi-select mode
        if (newSelectedIds.isEmpty()) {
            currentState.copy(
                isMultiSelectMode = false,
                selectedPresetIds = emptySet()
            ).setup()
        } else {
            currentState.copy(selectedPresetIds = newSelectedIds).setup()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.ShowDeleteMultipleConfirmDialog::class)
    fun onShowDeleteMultipleConfirmDialog() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        val selectedCount = currentState.selectedPresetIds.size
        if (selectedCount == 0) return
        currentState.copy(
            dialogState = PresetSetEditDialogState.DeleteMultipleConfirm(selectedCount)
        ).setup()
    }

    @UiIntentObserver(PresetSetEditUiIntent.ConfirmDeleteSelectedPresets::class)
    suspend fun onConfirmDeleteSelectedPresets() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        val selectedIds = currentState.selectedPresetIds.toList()

        if (selectedIds.isEmpty()) return

        currentState.copy(
            isMultiSelectMode = false,
            selectedPresetIds = emptySet(),
            dialogState = PresetSetEditDialogState.None
        ).setup()

        enqueueAsyncTask(Dispatchers.IO) {
            selectedIds.forEach { id ->
                _presetRepository.deletePreset(id)
            }
            PresetSetEditUiEffect.ShowToast(R.string.selected_presets_deleted).tryEmit()
            loadPresets()
        }
    }

    @UiIntentObserver(PresetSetEditUiIntent.CopySelectedPresetsPrompts::class)
    fun onCopySelectedPresetsPrompts() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        val selectedIds = currentState.selectedPresetIds

        if (selectedIds.isEmpty()) return

        val promptsList = mutableListOf<String>()
        currentState.presets
            .filter { it.id in selectedIds }
            .forEach { preset ->
                if (preset.prompts.isNotEmpty()) {
                    promptsList.add(preset.prompts)
                }
            }

        if (promptsList.isEmpty()) {
            PresetSetEditUiEffect.ShowToast(R.string.no_prompts_to_copy).tryEmit()
            return
        }

        val combinedPrompts = promptsList.joinToString(",") { it.trim() }
            .replace(",,", ",")
            .trim(',')

        PresetSetEditUiEffect.CopyToClipboard(combinedPrompts).tryEmit()
        PresetSetEditUiEffect.ShowToast(R.string.copied_to_clipboard).tryEmit()
    }

    @UiIntentObserver(PresetSetEditUiIntent.SelectAllPresets::class)
    fun onSelectAllPresets() {
        val currentState = getOrNull<PresetSetEditUiState.Normal>() ?: return
        if (!currentState.isMultiSelectMode) return

        val allIds = currentState.presets.map { it.id }.toSet()
        currentState.copy(selectedPresetIds = allIds).setup()
    }
}
