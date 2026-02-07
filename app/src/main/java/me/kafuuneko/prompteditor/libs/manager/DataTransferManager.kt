package me.kafuuneko.prompteditor.libs.manager

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DataTransferManager {
    private data class Entry(val value: Any)

    // 存储
    private val _store = ConcurrentHashMap<String, Entry>()

    /**
     * 推入对象
     * @return token（UUID 字符串）
     */
    fun push(obj: Any): String {
        val token = UUID.randomUUID().toString()
        _store[token] = Entry(obj)
        return token
    }

    /**
     * 取出并删除。
     */
    fun take(token: String?): Any? {
        if (token.isNullOrEmpty()) return null
        val entry = _store.remove(token) ?: return null
        return entry.value
    }

    inline fun <reified T> takeAs(token: String?): T? {
        return take(token) as? T
    }
}
