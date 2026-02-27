package me.kafuuneko.prompteditor

import android.app.Application
import androidx.room.Room
import com.chibatching.kotpref.Kotpref
import me.kafuuneko.prompteditor.libs.AppLibs
import me.kafuuneko.prompteditor.libs.room.AppDatabase
import me.kafuuneko.prompteditor.libs.room.repository.PresetRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class PromptEditorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        startKoin {
            androidContext(this@PromptEditorApp)
            modules(appModules)
        }
    }
}

private val appModules = module {
    singleOf(::AppLibs)

    // Room
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "primary.sqlite")
            .allowMainThreadQueries()
            .build()
    }
    singleOf(::PresetRepository)
}