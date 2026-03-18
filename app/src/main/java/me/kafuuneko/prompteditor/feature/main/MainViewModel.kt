package me.kafuuneko.prompteditor.feature.main

import android.content.Context
import kotlinx.coroutines.Dispatchers
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.main.presentation.MainDialogState
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiEffect
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState
import me.kafuuneko.prompteditor.libs.core.CoreViewModelWithUiEffect
import me.kafuuneko.prompteditor.libs.core.UiIntentObserver
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel :
    CoreViewModelWithUiEffect<MainUiIntent, MainUiState>(MainUiState.None),
    KoinComponent {

    private val _presetRepository by inject<PresetRepository>()
    private val _context by inject<Context>()

    @UiIntentObserver(MainUiIntent.CreatePage::class)
    suspend fun onCreatePage() {
        MainUiState.Normal().setup()
        loadPresetSets()
        if (!me.kafuuneko.prompteditor.libs.AppModel.isFirstUse) {
            getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.FirstUseConfirm)
                ?.setup()
        }
    }

    @UiIntentObserver(MainUiIntent.LoadPresetSets::class)
    suspend fun onLoadPresetSets() {
        loadPresetSets()
    }

    private suspend fun loadPresetSets() {
        enqueueAsyncTask(Dispatchers.IO) {
            val presetSets = _presetRepository.getAllPresetSets()
            getOrNull<MainUiState.Normal>()?.copy(
                presetSets = presetSets,
                isLoading = false
            )?.setup()
        }
    }

    @UiIntentObserver(MainUiIntent.StartCreatePreset::class)
    fun onStartCreatePreset(intent: MainUiIntent.StartCreatePreset) {
        getOrNull<MainUiState.Normal>()
            ?.copy(dialogState = MainDialogState.CreatePresetSet)
            ?.setup()
    }

    @UiIntentObserver(MainUiIntent.CreatePresetSet::class)
    suspend fun onCreatePresetSet(intent: MainUiIntent.CreatePresetSet) {
        enqueueAsyncTask(Dispatchers.IO) {
            val newPresetSet = PresetSet(name = intent.name, parser = intent.parser)
            _presetRepository.insertPresetSet(newPresetSet)
            MainUiEffect.ShowToast(R.string.preset_set_created, listOf(intent.name)).tryEmit()
            loadPresetSets()
        }
        getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.None)?.setup()
    }

    @UiIntentObserver(MainUiIntent.StartDeletePreset::class)
    fun onStartDeletePreset(intent: MainUiIntent.StartDeletePreset) {
        getOrNull<MainUiState.Normal>()
            ?.copy(dialogState = MainDialogState.DeletePresetSet(intent.id, intent.name))
            ?.setup()
    }

    @UiIntentObserver(MainUiIntent.ConfirmDeletePreset::class)
    suspend fun onConfirmDeletePreset() {
        val dialogState = getOrNull<MainUiState.Normal>()?.dialogState
        if (dialogState is MainDialogState.DeletePresetSet) {
            enqueueAsyncTask(Dispatchers.IO) {
                _presetRepository.deletePresetSet(dialogState.id)
                MainUiEffect.ShowToast(R.string.preset_set_deleted).tryEmit()
                loadPresetSets()
            }
        }
        onDismissDialog()
    }

    @UiIntentObserver(MainUiIntent.ShowRenamePresetSetDialog::class)
    fun onShowRenamePresetSetDialog(intent: MainUiIntent.ShowRenamePresetSetDialog) {
        getOrNull<MainUiState.Normal>()
            ?.copy(dialogState = MainDialogState.RenamePresetSet(intent.id, intent.name))
            ?.setup()
    }

    @UiIntentObserver(MainUiIntent.ConfirmRenamePresetSet::class)
    suspend fun onConfirmRenamePresetSet(intent: MainUiIntent.ConfirmRenamePresetSet) {
        val currentState = getOrNull<MainUiState.Normal>() ?: return

        enqueueAsyncTask(Dispatchers.IO) {
            val presetSet = currentState.presetSets.find { it.id == intent.id }
            if (presetSet != null) {
                _presetRepository.updatePresetSet(presetSet.copy(name = intent.newName))
                MainUiEffect.ShowToast(R.string.preset_set_renamed, listOf(intent.newName))
                    .tryEmit()
                loadPresetSets()
            }
        }
        onDismissDialog()
    }

    @UiIntentObserver(MainUiIntent.OpenPresetSet::class)
    fun onOpenPresetSet(intent: MainUiIntent.OpenPresetSet) {
        val presetSet = getOrNull<MainUiState.Normal>()?.presetSets?.find { it.id == intent.id }
        if (presetSet != null) {
            MainUiEffect.NavigateToPresetSetEdit(presetSet).tryEmit()
        }
    }

    @UiIntentObserver(MainUiIntent.OpenTagsImport::class)
    fun onOpenTagsImport() {
        MainUiEffect.NavigateToTagsImport.tryEmit()
    }

    @UiIntentObserver(MainUiIntent.OpenAbout::class)
    fun onOpenAbout() {
        MainUiEffect.NavigateToAbout.tryEmit()
    }

    @UiIntentObserver(MainUiIntent.Back::class)
    fun onBack() {
        getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.ExitConfirm)?.setup()
    }

    @UiIntentObserver(MainUiIntent.DismissDialog::class)
    fun onDismissDialog() {
        getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.None)?.setup()
    }

    @UiIntentObserver(MainUiIntent.ConfirmExit::class)
    fun onConfirmExit() {
        MainUiState.Finished.setup()
    }

    @UiIntentObserver(MainUiIntent.ConfirmFirstUseImport::class)
    suspend fun onConfirmFirstUseImport() {
        getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.ImportingTags)?.setup()
        enqueueAsyncTask(Dispatchers.IO) {
            try {
                val jaCsv = _context.assets.open("danbooru_tags_ja.csv").bufferedReader()
                    .use { it.readText() }
                val existingTags = _presetRepository.getAllTags()
                me.kafuuneko.prompteditor.libs.utils.CsvParser.processTagImport(
                    jaCsv,
                    existingTags
                ) { tags ->
                    _presetRepository.insertTags(tags)
                }

                val configOptions = _context.resources.configuration.locales
                val isChinese = !configOptions.isEmpty && configOptions.get(0).language == "zh"
                if (isChinese) {
                    val zhCsv = _context.assets.open("danbooru_tags_zh.csv").bufferedReader()
                        .use { it.readText() }
                    val updatedTags = _presetRepository.getAllTags()
                    me.kafuuneko.prompteditor.libs.utils.CsvParser.processTagImport(
                        zhCsv,
                        updatedTags
                    ) { tags ->
                        _presetRepository.insertTags(tags)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                me.kafuuneko.prompteditor.libs.AppModel.isFirstUse = true
                getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.None)?.setup()
            }
        }
    }

    @UiIntentObserver(MainUiIntent.CancelFirstUseImport::class)
    fun onCancelFirstUseImport() {
        me.kafuuneko.prompteditor.libs.AppModel.isFirstUse = true
        getOrNull<MainUiState.Normal>()?.copy(dialogState = MainDialogState.None)?.setup()
    }
}