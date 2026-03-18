package me.kafuuneko.prompteditor.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.kafuuneko.prompteditor.R

// Helper to generate a distinct color from a group index using the Golden Ratio
fun getGroupColor(group: Int): Color {
    val goldenRatioConjugate = 0.618033988749895f
    var hue = group * goldenRatioConjugate
    hue %= 1f
    // Convert HSV to Color: using constant high saturation and value for bright distinct colors
    val hsv = floatArrayOf(hue * 360f, 0.7f, 0.9f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

@Composable
fun PresetEditDialog(
    tagName: String,
    initialWeight: Double,
    initialGroup: Int,
    onConfirm: (tagName: String, weight: Double, group: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var tagNameText by remember { mutableStateOf(tagName) }
    var weightText by remember { mutableStateOf(initialWeight.toString()) }
    var groupState by remember { mutableIntStateOf(Math.max(0, initialGroup)) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.edit)) },
        text = {
            Column {
                OutlinedTextField(
                    value = tagNameText,
                    onValueChange = { tagNameText = it },
                    label = { Text(stringResource(R.string.tag_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.group),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Minus button
                    IconButton(
                        onClick = { if (groupState > 0) groupState-- },
                        enabled = groupState > 0
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Group")
                    }

                    // Group indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(getGroupColor(groupState))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = groupState.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Plus button
                    IconButton(onClick = { groupState++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Group")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightText.toDoubleOrNull() ?: initialWeight
                    onConfirm(tagNameText, weight, groupState)
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
