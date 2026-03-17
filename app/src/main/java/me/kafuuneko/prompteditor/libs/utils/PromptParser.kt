package me.kafuuneko.prompteditor.libs.utils

import me.kafuuneko.prompteditor.feature.preset.presentation.PromptItem
import me.kafuuneko.prompteditor.libs.room.entity.Tag
import java.util.regex.Pattern

/**
 * Prompt解析工具类
 * 处理Prompt文本与PromptItem之间的转换，以及tag和权重的提取
 */
object PromptParser {

    // 匹配格式:
    // 单层: {tag}, {tag:weight}, [tag], [tag:weight]
    // 多层: {{tag}}, {{tag:weight}}, [[tag]], [[tag:weight]]
    private val WEIGHT_PATTERN = Pattern.compile(
        "^" +
        // 多层大括号: {{tag}} 或 {{tag:weight}}
        "(\\{{2,})([^:}]+)(?::([^}]+))?(\\}{2,})" +
        "|" +
        // 多层中括号: [[tag]] 或 [[tag:weight]]
        "(\\[{2,})([^:\\]]+)(?::([^]]+))?(]{2,})" +
        "|" +
        // 单层大括号: {tag} 或 {tag:weight}
        "(\\{)([^:}]+)(?::([^}]+))?(\\})" +
        "|" +
        // 单层中括号: [tag] 或 [tag:weight]
        "(\\[)([^:\\]]+)(?::([^]]+))?(])" +
        "$"
    )

    private val TAG_SPLITTER = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")


    /**
     * 将prompts文本解析为PromptItem列表
     *
     * @param prompts 原始prompts文本
     * @param allTags 所有可用的Tag列表，用于获取描述
     * @return 解析后的PromptItem列表
     */
    fun parsePromptsToItems(prompts: String, allTags: List<Tag>): List<PromptItem> {
        if (prompts.isBlank()) return emptyList()

        val items = mutableListOf<PromptItem>()
        val tags = TAG_SPLITTER.split(prompts.trim())
        val tagMap = allTags.associateBy { it.name.lowercase() }

        for (tag in tags) {
            val trimmed = tag.trim()
            if (trimmed.isEmpty()) continue

            val (tagName, weight) = extractTagAndWeight(trimmed)
            val tagDescription = tagMap[tagName.lowercase()]?.description ?: ""

            items.add(
                PromptItem(
                    originalText = trimmed,
                    tagName = tagName,
                    weight = weight,
                    description = tagDescription
                )
            )
        }

        return items
    }

    /**
     * 从文本中提取tag名称和权重
     *
     * @param text 原始文本（如 {tag:1.2} 或 [tag]）
     * @return Pair(tagName, weight)
     */
    fun extractTagAndWeight(text: String): Pair<String, String> {
        val matcher = WEIGHT_PATTERN.matcher(text)
        return if (matcher.matches()) {
            // Groups: 1-4 (multi {}), 5-8 (multi []), 9-12 (single {}), 13-16 (single [])
            val tagName = matcher.group(2) ?: matcher.group(6) ?: matcher.group(10) ?: matcher.group(14) ?: text
            val weight = matcher.group(3) ?: matcher.group(7) ?: matcher.group(11) ?: matcher.group(15) ?: ""
            tagName to weight
        } else {
            text to ""
        }
    }

    /**
     * 从文本中提取tag名称
     *
     * @param text 原始文本
     * @return tag名称
     */
    fun extractTagName(text: String): String {
        val (tagName, _) = extractTagAndWeight(text)
        return tagName
    }

    /**
     * 将PromptItem列表转换为prompts文本
     *
     * @param items PromptItem列表
     * @return 转换后的prompts文本
     */
    fun convertItemsToText(items: List<PromptItem>): String {
        return items.joinToString(", ") { item ->
            if (item.weight.isNotEmpty()) {
                "{${item.tagName}:${item.weight}}"
            } else {
                item.tagName
            }
        }
    }
}
