package me.kafuuneko.prompteditor.feature.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.kafuuneko.prompteditor.feature.about.ui.AboutLayout
import me.kafuuneko.prompteditor.libs.core.CoreActivity

class AboutActivity : CoreActivity() {
    @Composable
    override fun ViewContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            AboutLayout { finish() }
        }
    }
}