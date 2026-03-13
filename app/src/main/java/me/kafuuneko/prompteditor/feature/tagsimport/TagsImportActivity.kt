package me.kafuuneko.prompteditor.feature.tagsimport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiEffect
import me.kafuuneko.prompteditor.feature.tagsimport.presentation.TagsImportUiIntent
import me.kafuuneko.prompteditor.feature.tagsimport.ui.TagsImportLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivityWithUiEffect
import me.kafuuneko.prompteditor.libs.core.IUiEffect
import java.io.BufferedReader
import java.io.InputStreamReader

class TagsImportActivity : CoreActivityWithUiEffect() {
    private val mViewModel by viewModels<TagsImportViewModel>()
    override fun getUiEffectFlow() = mViewModel.uiEffectFlow

    private val csvFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val csvContent = reader.readText()
                reader.close()
                inputStream?.close()

                mViewModel.emit(TagsImportUiIntent.ImportFromCsv(csvContent))
            } catch (e: Exception) {
                Toast.makeText(this, "读取文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Composable
    override fun ViewContent() {
        val uiState by mViewModel.uiStateFlow.collectAsState()
        Surface(modifier = Modifier.fillMaxSize()) {
            TagsImportLayout(
                uiState = uiState,
                emit = { mViewModel.emit(this) },
                onImportClick = { openFilePicker() }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.emit(TagsImportUiIntent.CreatePage)
    }

    private fun openFilePicker() {
        csvFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
    }

    override suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        when (uiEffect) {
            is TagsImportUiEffect.ShowToast -> {
                val message = if (uiEffect.formatArgs.isNotEmpty()) {
                    getString(uiEffect.messageResId, *uiEffect.formatArgs)
                } else {
                    getString(uiEffect.messageResId)
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }

            is TagsImportUiEffect.NavigateBack -> {
                finish()
            }

            is TagsImportUiEffect.OpenFilePicker -> {
                openFilePicker()
            }

            else -> super.onReceivedUiEffect(uiEffect)
        }
    }
}
