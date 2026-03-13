package me.kafuuneko.prompteditor.libs.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "preset",
    foreignKeys = [
        ForeignKey(
            entity = PresetSet::class,
            parentColumns = ["id"],
            childColumns = ["presetSetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("presetSetId")]
)
data class Preset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val presetSetId: Long,
    val name: String,
    val prompts: String = ""
)
