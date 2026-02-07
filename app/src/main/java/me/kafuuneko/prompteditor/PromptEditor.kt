package me.kafuuneko.prompteditor

import android.app.Application
import com.chibatching.kotpref.Kotpref
import me.kafuuneko.prompteditor.libs.AppLibs
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class PromptEditor : Application() {
    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        startKoin {
            androidContext(this@PromptEditor)
            modules(appModules)
        }
    }
}

private val appModules = module {
    singleOf(::AppLibs)
}