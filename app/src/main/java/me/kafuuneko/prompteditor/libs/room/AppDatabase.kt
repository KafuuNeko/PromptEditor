package me.kafuuneko.prompteditor.libs.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.kafuuneko.prompteditor.libs.room.dao.PresetDao
import me.kafuuneko.prompteditor.libs.room.entity.PresetData

@Database(
    entities = [PresetData::class],
    version = 1,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getPresetDao(): PresetDao
}
