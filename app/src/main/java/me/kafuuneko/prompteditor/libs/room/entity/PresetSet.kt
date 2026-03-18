package me.kafuuneko.prompteditor.libs.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val parser: Int = 0
)
