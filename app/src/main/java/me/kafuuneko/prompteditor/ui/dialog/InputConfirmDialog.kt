package me.kafuuneko.prompteditor.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R
import me.kafuuneko.prompteditor.ui.theme.AppTheme

@Composable
fun InputConfirmDialog(
    title: String,
    hintText: String = "",
    defaultText: String = "",
    cancelContent: @Composable (RowScope.() -> Unit) = { Text(stringResource(R.string.cancel)) },
    confirmContent: @Composable (RowScope.() -> Unit) = { Text(stringResource(R.string.confirm)) },
    onDismissRequest: () -> Unit = {},
    onConfirmRequest: (String) -> Unit = {},
) {
    var inputText by remember { mutableStateOf(defaultText) }
    ConfirmDialog(
        cancelContent = cancelContent,
        confirmContent = confirmContent,
        onDismissRequest = onDismissRequest,
        onConfirmRequest = { onConfirmRequest(inputText) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(
                    Alignment.Start
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(hintText) }
            )
        }
    }
}

@Preview
@Composable
private fun InputConfirmDialogPreview() {
    AppTheme(dynamicColor = false) {
        InputConfirmDialog(
            title = "Test",
            hintText = "Enter text"
        )
    }
}