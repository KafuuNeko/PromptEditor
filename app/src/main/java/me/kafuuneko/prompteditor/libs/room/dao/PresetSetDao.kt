package me.kafuuneko.prompteditor.libs.room.dao

import androidx.room.Dao
import androidx.room.Query
import me.kafuuneko.prompteditor.libs.room.MutableDao
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet

@Dao
interface PresetSetDao : MutableDao<PresetSet> {
    @Query("SELECT * FROM presets ORDER BY id DESC")
    suspend fun getAllPresetSets(): List<PresetSet>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getPresetSetById(id: Long): PresetSet?

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
