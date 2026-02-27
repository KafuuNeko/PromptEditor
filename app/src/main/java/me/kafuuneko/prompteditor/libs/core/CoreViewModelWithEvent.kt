package me.kafuuneko.prompteditor.libs.core

import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 带 UiEffect 的 CoreViewModel
 */
abstract class CoreViewModelWithUiEffect<I, S>(initStatus: S) : CoreViewModel<I, S>(initStatus) {
    private val _uiEffectFlow = MutableSharedFlow<UiEffectWrapper>(extraBufferCapacity = 64)
    val uiEffectFlow = _uiEffectFlow.asSharedFlow()

    /**
     * 尝试分发UI Effect（一次性事件）
     */
    protected fun IUiEffect.tryEmit(): Boolean {
        return _uiEffectFlow.tryEmit(UiEffectWrapper(this))
    }

    /**
     * 分发UI Event，缓冲区满则等待
     */
    protected suspend fun IUiEffect.emit() {
        _uiEffectFlow.emit(UiEffectWrapper(this))
    }

    /**
     * 发一个 UI Event, 并等待其事件消费完成
     */
    protected suspend fun IUiEffect.emitAndAwait() {
        UiEffectWrapper(this)
            .apply { _uiEffectFlow.emit(this) }
            .waitForConsumption()
    }
}

class UiEffectWrapper(private val content: IUiEffect) {
    private val mMutex = Mutex()
    private val mHasHandled = MutableStateFlow(false)

    suspend fun consumeIfNotHandled(handle: suspend (IUiEffect) -> Unit) = mMutex.withLock {
        if (mHasHandled.value) return@withLock false
        handle(content)
        mHasHandled.value = true
        return@withLock true
    }

    fun isHandled() = mHasHandled.value

    suspend fun waitForConsumption() {
        if (mHasHandled.value) return
        mHasHandled.first { it }
    }
}

interface IUiEffect

sealed class AppUiEffect : IUiEffect {
    data class PopupToastMessage(val message: String) : AppUiEffect()
    data class PopupToastMessageByResId(val messageResId: Int) : AppUiEffect()
    data class StartActivity(val activity: Class<*>, val extras: Bundle? = null) : AppUiEffect()
    data class StartActivityByIntent(val intent: Intent) : AppUiEffect()
    data class SetResult(val resultCode: Int, val intent: Intent? = null) : AppUiEffect()
}