package me.kafuuneko.prompteditor.libs.room.dao

import androidx.room.Dao
import me.kafuuneko.prompteditor.libs.room.MutableDao
import me.kafuuneko.prompteditor.libs.room.entity.PresetData

@Dao
interface PresetDao : MutableDao<PresetData> {

}