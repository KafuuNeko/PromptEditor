package me.kafuuneko.prompteditor.feature.main.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.feature.main.presentation.MainDialogState
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiIntent
import me.kafuuneko.prompteditor.feature.main.presentation.MainUiState
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet
import me.kafuuneko.prompteditor.ui.dialog.ConfirmDialog
import me.kafuuneko.prompteditor.ui.dialog.InputConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    uiState: MainUiState,
    emit: MainUiIntent.() -> Unit
) {

    BackHandler { MainUiIntent.Back.emit() }
    when (uiState) {
        MainUiState.Finished, MainUiState.None -> Unit
        is MainUiState.Normal -> {
            Normal(uiState, emit)
            DialogSwitch(uiState.dialogState, emit)
        }
    }
}

@Composable
private fun DialogSwitch(
    dialogState: MainDialogState,
    emit: MainUiIntent.() -> Unit
) {
    when (dialogState) {
        MainDialogState.None -> Unit

        MainDialogState.ExitConfirm -> ConfirmDialog(
            onConfirmRequest = { MainUiIntent.ConfirmExit.emit() },
            onDismissRequest = { MainUiIntent.DismissDialog.emit() }
        ) {
            Text(stringResource(R.string.exit_app_confirmation))
        }

        MainDialogState.CreatePresetSet -> InputConfirmDialog(
            title = stringResource(R.string.create_preset_set),
            hintText = stringResource(R.string.preset_set_name_hint),
            onConfirmRequest = { MainUiIntent.CreatePresetSet(it).emit() },
            onDismissRequest = { MainUiIntent.DismissDialog.emit() }
        )

        is MainDialogState.DeletePresetSet -> ConfirmDialog(
            onConfirmRequest = { MainUiIntent.ConfirmDeletePreset.emit() },
            onDismissRequest = { MainUiIntent.DismissDialog.emit() },
            confirmContent = {
                Text(
                    stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        ) {
            Text(
                stringResource(R.string.delete_preset_set_confirmation, dialogState.name)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Normal(
    uiState: MainUiState.Normal,
    emit: MainUiIntent.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { MainUiIntent.OpenTagsImport.emit() }) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = stringResource(R.string.import_tags)
                        )
                    }
                    IconButton(onClick = { MainUiIntent.StartCreatePreset.emit() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.create_preset_set)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.presetSets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_preset_sets),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.presetSets, key = { it.id }) { presetSet ->
                    PresetSetItem(
                        presetSet = presetSet,
                        onClick = { MainUiIntent.OpenPresetSet(presetSet.id).emit() },
                        onStartDelete = {
                            MainUiIntent.StartDeletePreset(
                                presetSet.id,
                                presetSet.name
                            ).emit()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetSetItem(
    presetSet: PresetSet,
    onClick: () -> Unit,
    onStartDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = presetSet.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onStartDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}