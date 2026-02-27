package me.kafuuneko.prompteditor.libs.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DownloadTask")
data class PresetData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
)