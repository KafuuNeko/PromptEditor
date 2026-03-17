package me.kafuuneko.prompteditor.libs.utils

import me.kafuuneko.prompteditor.libs.room.entity.Preset

/**
 * Prompt合并工具类
 * 处理多个Preset的Prompts合并和复制功能
 */
object PromptCombiner {

    /**
     * 合并多个Preset的Prompts文本
     *
     * @param presets 要合并的Preset列表
     * @param presetIds 要包含的Preset ID集合
     * @return 合并后的Prompts文本，如果没有任何有效prompts则返回null
     */
    fun combinePrompts(presets: List<Preset>, presetIds: Set<Long>): String? {
        val promptsList = presets
            .filter { it.id in presetIds && it.prompts.isNotEmpty() }
            .map { it.prompts }

        if (promptsList.isEmpty()) {
            return null
        }

        return promptsList.joinToString(",") { it.trim() }
            .replace(",,", ",")
            .trim(',')
    }

    /**
     * 合并多个Preset的Prompts文本（静态方法版本）
     *
     * @param promptsList 要合并的prompts文本列表
     * @return 合并后的文本
     */
    fun combineMultiplePrompts(promptsList: List<String>): String {
        if (promptsList.isEmpty()) {
            return ""
        }

        return promptsList.joinToString(",") { it.trim() }
            .replace(",,", ",")
            .trim(',')
    }
}
