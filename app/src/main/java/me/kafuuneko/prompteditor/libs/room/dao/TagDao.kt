package me.kafuuneko.prompteditor.libs.room.dao

import androidx.room.Dao
import androidx.room.Query
import me.kafuuneko.prompteditor.libs.room.MutableDao
import me.kafuuneko.prompteditor.libs.room.entity.Tag

@Dao
interface TagDao : MutableDao<Tag> {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<Tag>

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): Tag?

    @Query("SELECT * FROM tags WHERE name IN (:names)")
    suspend fun getTagsByNames(names: List<String>): List<Tag>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchTags(query: String): List<Tag>

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)
}
