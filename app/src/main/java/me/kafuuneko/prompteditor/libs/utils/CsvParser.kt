package me.kafuuneko.prompteditor.libs.utils

import me.kafuuneko.prompteditor.feature.tagsedit.presentation.ImportResult
import me.kafuuneko.prompteditor.libs.room.entity.Tag

/**
 * CSV解析工具类
 * 处理CSV格式的数据导入解析
 */
object CsvParser {

    /**
     * 待处理的Tag数据
     */
    data class TagData(
        val name: String,
        val description: String,
        val isValid: Boolean
    )

    /**
     * 解析CSV内容为Tag数据列表
     *
     * @param csvContent CSV文本内容
     * @return 解析后的TagData列表
     */
    fun parseTagsFromCsv(csvContent: String): List<TagData> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        return lines.map { line ->
            parseLineToTagData(line)
        }
    }

    /**
     * 解析单行CSV数据
     *
     * @param line 原始行
     * @return TagData
     */
    private fun parseLineToTagData(line: String): TagData {
        return try {
            val parts = line.split(",", limit = 2)
            if (parts.isNotEmpty()) {
                val tagName = parts[0].trim().removeSurrounding("\"")
                val description = if (parts.size > 1) parts[1].trim().removeSurrounding("\"") else ""
                if (tagName.isNotEmpty()) {
                    TagData(name = tagName, description = description, isValid = true)
                } else {
                    TagData(name = "", description = "", isValid = false)
                }
            } else {
                TagData(name = "", description = "", isValid = false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            TagData(name = "", description = "", isValid = false)
        }
    }

    /**
     * 处理CSV导入，比较现有数据并返回导入结果
     *
     * @param csvContent CSV文本内容
     * @param existingTags 现有的Tag列表
     * @return ImportResult 导入结果统计
     */
    suspend fun processTagImport(
        csvContent: String,
        existingTags: List<Tag>,
        insertAction: suspend (List<Tag>) -> Unit
    ): ImportResult {
        val tagDataList = parseTagsFromCsv(csvContent)
        val existingTagsMap = existingTags.associateBy { it.name.lowercase() }

        val tagsToInsert = mutableListOf<Tag>()
        val tagsToUpdate = mutableListOf<Tag>()

        var successCount = 0
        var failCount = 0
        var updateCount = 0

        for (tagData in tagDataList) {
            if (!tagData.isValid) {
                failCount++
                continue
            }

            val existingTag = existingTagsMap[tagData.name.lowercase()]
            if (existingTag != null) {
                // 存在同名Tag，执行覆盖更新
                tagsToUpdate.add(existingTag.copy(description = tagData.description))
                updateCount++
            } else {
                // 不存在，插入新Tag
                tagsToInsert.add(Tag(name = tagData.name, description = tagData.description))
            }
            successCount++
        }

        // 批量插入新Tags
        if (tagsToInsert.isNotEmpty()) {
            insertAction(tagsToInsert)
        }

        // 批量更新已存在的Tags
        if (tagsToUpdate.isNotEmpty()) {
            insertAction(tagsToUpdate)
        }

        return ImportResult(
            successCount = successCount,
            updateCount = updateCount,
            failCount = failCount,
            totalCount = tagDataList.size
        )
    }
}
