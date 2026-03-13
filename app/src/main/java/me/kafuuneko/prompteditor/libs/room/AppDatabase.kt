package me.kafuuneko.prompteditor.libs.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.kafuuneko.prompteditor.libs.room.dao.PresetDao
import me.kafuuneko.prompteditor.libs.room.dao.PresetSetDao
import me.kafuuneko.prompteditor.libs.room.dao.TagDao
import me.kafuuneko.prompteditor.libs.room.entity.Preset
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet
import me.kafuuneko.prompteditor.libs.room.entity.Tag

@Database(
    entities = [PresetSet::class, Preset::class, Tag::class],
    version = 1,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getPresetSetDao(): PresetSetDao
    abstract fun getPresetDao(): PresetDao
    abstract fun getTagDao(): TagDao
}
