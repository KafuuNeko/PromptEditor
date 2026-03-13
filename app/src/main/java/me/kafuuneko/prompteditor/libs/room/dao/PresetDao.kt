package me.kafuuneko.prompteditor.libs.room.dao

import androidx.room.Dao
import androidx.room.Query
import me.kafuuneko.prompteditor.libs.room.MutableDao
import me.kafuuneko.prompteditor.libs.room.entity.Preset

@Dao
interface PresetDao : MutableDao<Preset> {
    @Query("SELECT * FROM preset WHERE presetSetId = :presetSetId ORDER BY id DESC")
    suspend fun getPresetsByPresetSetId(presetSetId: Long): List<Preset>

    @Query("SELECT * FROM preset WHERE id = :id")
    suspend fun getPresetById(id: Long): Preset?

    @Query("DELETE FROM preset WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM preset WHERE presetSetId = :presetSetId")
    suspend fun deleteByPresetSetId(presetSetId: Long)
}
