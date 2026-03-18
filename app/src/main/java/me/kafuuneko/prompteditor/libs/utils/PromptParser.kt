package me.kafuuneko.prompteditor.libs.utils

import me.kafuuneko.prompteditor.libs.room.entity.Tag
import kotlin.math.pow
import kotlin.math.round

data class PromptGroupItem(
    val tags: List<Pair<String, String>>, // Pair<TagName, Description>
    val weight: Double = 1.0
)

data class PromptItem(
    val tagName: String,
    val description: String,
    val weight: Double = 1.0,
    val group: Int = 0
)

fun List<PromptGroupItem>.expand(): List<PromptItem> = this.flatMapIndexed { index, groupItem ->
    groupItem.tags.map { (name, desc) ->
        PromptItem(
            tagName = name,
            description = desc,
            weight = groupItem.weight,
            group = index
        )
    }
}

fun List<PromptItem>.fold(): List<PromptGroupItem> = this.groupBy { it.group }
    .values
    .map { items ->
        PromptGroupItem(
            tags = items.map { it.tagName to it.description },
            weight = items.firstOrNull()?.weight ?: 1.0
        )
    }

interface IPromptsParser {
    fun parse(input: String, tagMap: Map<String, Tag> = emptyMap()): List<PromptGroupItem>
    fun stringify(items: List<PromptGroupItem>): String
}

private fun Double.formatWeight(): String {
    val rounded = round(this * 100) / 100
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

private fun parseTagsWithMap(input: String, tagMap: Map<String, Tag>): List<Pair<String, String>> {
    return input.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { tagName ->
            Pair(tagName, tagMap[tagName.lowercase()]?.description ?: "")
        }
}

/**
 * NovelAI 语法解析器
 */
class NovelAIPromptsParser : IPromptsParser {
    companion object {
        private const val NAI_STEP = 1.05
        private val NAI_SCOPE_REGEX = Regex("""^([0-9.]+)\s*::(.*)::$""")
    }

    override fun parse(input: String, tagMap: Map<String, Tag>): List<PromptGroupItem> {
        return splitRespectingScopes(input).mapNotNull { chunk ->
            var text = chunk.trim()
            if (text.isEmpty()) return@mapNotNull null

            // 1. 优先匹配显式数值权重 1.5::...::
            val match = NAI_SCOPE_REGEX.find(text)
            if (match != null) {
                val weight = match.groupValues[1].toDoubleOrNull() ?: 1.0
                val tagsString = match.groupValues[2]
                return@mapNotNull PromptGroupItem(
                    tags = parseTagsWithMap(tagsString, tagMap),
                    weight = weight
                )
            }

            // 2. 剥洋葱逻辑：循环处理最外层的 {} 和 []
            var braceDepth = 0
            var bracketDepth = 0

            while (true) {
                if (text.startsWith("{") && text.endsWith("}")) {
                    braceDepth++
                    text = text.substring(1, text.length - 1).trim()
                } else if (text.startsWith("[") && text.endsWith("]")) {
                    bracketDepth++
                    text = text.substring(1, text.length - 1).trim()
                } else {
                    break
                }
            }

            val weight = NAI_STEP.pow(braceDepth.toDouble()) / NAI_STEP.pow(bracketDepth.toDouble())

            PromptGroupItem(
                tags = parseTagsWithMap(text, tagMap),
                weight = weight
            )
        }
    }

    override fun stringify(items: List<PromptGroupItem>): String {
        return items.joinToString(", ") { item ->
            val tagsStr = item.tags.joinToString(", ") { it.first }
            if (item.weight == 1.0 && item.tags.size <= 1) {
                tagsStr
            } else {
                "${item.weight.formatWeight()}::${tagsStr}::"
            }
        }
    }

    private fun splitRespectingScopes(input: String): List<String> {
        val result = mutableListOf<String>()
        val buffer = StringBuilder()
        var depth = 0
        var inScope = false
        var i = 0

        while (i < input.length) {
            val char = input[i]
            if (char == ':' && i + 1 < input.length && input[i + 1] == ':') {
                inScope = !inScope
                buffer.append("::")
                i += 2
                continue
            }
            when (char) {
                '{', '[' -> depth++
                '}', ']' -> depth--
            }
            if (char == ',' && depth <= 0 && !inScope) {
                result.add(buffer.toString())
                buffer.clear()
            } else {
                buffer.append(char)
            }
            i++
        }
        if (buffer.isNotEmpty()) result.add(buffer.toString())
        return result
    }
}

/**
 * Stable Diffusion 语法解析器
 */
class SDPromptsParser : IPromptsParser {
    companion object {
        private const val SD_STEP = 1.1
        private val SD_WEIGHT_REGEX = Regex("""^\((.*):\s*([0-9.]+)\s*\)$""")
    }

    override fun parse(input: String, tagMap: Map<String, Tag>): List<PromptGroupItem> {
        return splitRespectingBrackets(input).mapNotNull { chunk ->
            var text = chunk.trim()
            if (text.isEmpty()) return@mapNotNull null

            // 1. 优先匹配显式数值权重 (tags...: 1.5)
            val match = SD_WEIGHT_REGEX.find(text)
            if (match != null) {
                val tagsString = match.groupValues[1]
                val weight = match.groupValues[2].toDoubleOrNull() ?: 1.0
                return@mapNotNull PromptGroupItem(
                    tags = parseTagsWithMap(tagsString, tagMap),
                    weight = weight
                )
            }

            // 2. 剥洋葱逻辑：循环处理最外层的 () 和 []
            var parenDepth = 0
            var bracketDepth = 0

            while (true) {
                if (text.startsWith("(") && text.endsWith(")")) {
                    parenDepth++
                    text = text.substring(1, text.length - 1).trim()
                } else if (text.startsWith("[") && text.endsWith("]")) {
                    bracketDepth++
                    text = text.substring(1, text.length - 1).trim()
                } else {
                    break
                }
            }

            val weight = SD_STEP.pow(parenDepth.toDouble()) * (0.9).pow(bracketDepth.toDouble())

            PromptGroupItem(
                tags = parseTagsWithMap(text, tagMap),
                weight = weight
            )
        }
    }

    override fun stringify(items: List<PromptGroupItem>): String {
        return items.joinToString(", ") { item ->
            val tagsStr = item.tags.joinToString(", ") { it.first }
            if (item.weight == 1.0 && item.tags.size <= 1) {
                tagsStr
            } else {
                "(${tagsStr}:${item.weight.formatWeight()})"
            }
        }
    }

    private fun splitRespectingBrackets(input: String): List<String> {
        val result = mutableListOf<String>()
        val buffer = StringBuilder()
        var depth = 0

        for (char in input) {
            when (char) {
                '(', '[' -> depth++
                ')', ']' -> depth--
            }
            if (char == ',' && depth <= 0) {
                result.add(buffer.toString())
                buffer.clear()
            } else {
                buffer.append(char)
            }
        }
        if (buffer.isNotEmpty()) result.add(buffer.toString())
        return result
    }
}