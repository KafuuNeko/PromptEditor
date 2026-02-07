package me.kafuuneko.prompteditor.libs

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.net.toUri
import me.kafuuneko.prompteditor.R
import org.koin.core.component.KoinComponent

class AppLibs(
    private val _context: Context
) : KoinComponent {
    fun getString(@StringRes id: Int, vararg args: Any): String {
        return _context.resources?.getString(id, *args).toString()
    }

    fun getVersionName(): String {
        return _context.packageManager.getPackageInfo(_context.packageName, 0)
            .versionName ?: getString(R.string.unknown_version)
    }

    fun jumpToUrl(url: String) {
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            _context.startActivity(it)
        }
    }
}