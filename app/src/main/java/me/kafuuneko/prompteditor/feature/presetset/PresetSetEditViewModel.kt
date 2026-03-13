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
        enqueueAsyncTask(Dispatchers.IO) {
            val newPreset = Preset(
                presetSetId = _currentPresetSetId,
                name = intent.name,
                prompts = ""
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
        PresetSetEditUiEffect.NavigateBack.tryEmit()
    }
}
