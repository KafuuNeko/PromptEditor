package me.kafuuneko.prompteditor.libs.utils

import me.kafuuneko.prompteditor.libs.room.entity.Tag

/**
 * 文本搜索过滤工具类
 * 提供通用的文本搜索和过滤功能
 */
object TextSearcher {

    /**
     * 过滤Tag列表，支持按名称和描述搜索
     *
     * @param tags 原始Tag列表
     * @param query 搜索关键词
     * @return 过滤后的Tag列表
     */
    fun filterTags(tags: List<Tag>, query: String): List<Tag> {
        return if (query.isEmpty()) {
            tags
        } else {
            val lowerQuery = query.lowercase()
            tags.filter {
                it.name.lowercase().contains(lowerQuery) ||
                        it.description.lowercase().contains(lowerQuery)
            }
        }
    }

    /**
     * 过滤字符串列表，支持自定义匹配逻辑
     *
     * @param items 原始字符串列表
     * @param query 搜索关键词
     * @param matcher 自定义匹配函数
     * @return 过滤后的列表
     */
    fun <T> filterList(items: List<T>, query: String, matcher: (T, String) -> Boolean): List<T> {
        return if (query.isEmpty()) {
            items
        } else {
            val lowerQuery = query.lowercase()
            items.filter { matcher(it, lowerQuery) }
        }
    }
}
