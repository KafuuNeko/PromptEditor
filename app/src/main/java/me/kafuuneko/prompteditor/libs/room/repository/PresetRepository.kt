package me.kafuuneko.prompteditor.libs.room.repository

import me.kafuuneko.prompteditor.libs.room.AppDatabase
import me.kafuuneko.prompteditor.libs.room.entity.Preset
import me.kafuuneko.prompteditor.libs.room.entity.PresetSet
import me.kafuuneko.prompteditor.libs.room.entity.Tag

class PresetRepository(private val _appDatabase: AppDatabase) {
    private val _presetSetDao = _appDatabase.getPresetSetDao()
    private val _presetDao = _appDatabase.getPresetDao()
    private val _tagDao = _appDatabase.getTagDao()

    // ==================== PresetSet (预设集) Operations ====================

    suspend fun getAllPresetSets(): List<PresetSet> = _presetSetDao.getAllPresetSets()

    suspend fun getPresetSetById(id: Long): PresetSet? = _presetSetDao.getPresetSetById(id)

    suspend fun insertPresetSet(presetSet: PresetSet): Long = _presetSetDao.insertOrReplace(presetSet)

    suspend fun updatePresetSet(presetSet: PresetSet) = _presetSetDao.update(presetSet)

    suspend fun deletePresetSet(id: Long) = _presetSetDao.deleteById(id)

    // ==================== Preset (预设) Operations ====================

    suspend fun getPresetsByPresetSetId(presetSetId: Long): List<Preset> =
        _presetDao.getPresetsByPresetSetId(presetSetId)

    suspend fun getPresetById(id: Long): Preset? = _presetDao.getPresetById(id)

    suspend fun insertPreset(preset: Preset): Long = _presetDao.insertOrReplace(preset)

    suspend fun updatePreset(preset: Preset) = _presetDao.update(preset)

    suspend fun deletePreset(id: Long) = _presetDao.deleteById(id)

    suspend fun updatePresetOrder(id: Long, order: Int) = _presetDao.updateOrder(id, order)

    // ==================== Tag Operations ====================

    suspend fun getAllTags(): List<Tag> = _tagDao.getAllTags()

    suspend fun getTagByName(name: String): Tag? = _tagDao.getTagByName(name)

    suspend fun getTagsByNames(names: List<String>): List<Tag> = _tagDao.getTagsByNames(names)

    suspend fun searchTags(query: String): List<Tag> = _tagDao.searchTags(query)

    suspend fun insertTag(tag: Tag): Long = _tagDao.insertOrReplace(tag)

    suspend fun insertTags(tags: List<Tag>): List<Long> = _tagDao.insertOrReplaceAll(tags)

    suspend fun deleteAllTags() = _tagDao.deleteAllTags()

    suspend fun deleteTagById(id: Long) = _tagDao.deleteById(id)
}