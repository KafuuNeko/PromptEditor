package me.kafuuneko.prompteditor.libs.core

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class CoreActivityWithUiEffect: CoreActivity() {
    private var _uiEffectCollectJob: Job? = null

    protected abstract fun getUiEffectFlow(): Flow<UiEffectWrapper>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerUiEffectFlow()
    }

    /**
     * 注册并收集一个[IUiEffect]流
     *
     * 此方法会在 Activity 的生命周期范围内自动管理协程收集任务：
     * - 如果之前已经存在一个收集任务，会先取消旧的任务，再启动新的收集；
     * - 只会在Activity的生命周期处于[Lifecycle.State.CREATED]后才会开始收集
     * - 注册后会处理默认的[IUiEffect]，但如果你需要处理其它Effect，则请重写[onReceivedUiEffect]
     *
     * @see onReceivedUiEffect
     */
    private fun registerUiEffectFlow() {
        if (_uiEffectCollectJob?.isActive == true) _uiEffectCollectJob?.cancel()
        _uiEffectCollectJob = lifecycleScope.launch {
            getUiEffectFlow()
                .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
                .collect { wrapper ->
                    currentCoroutineContext().ensureActive()
                    wrapper.consumeIfNotHandled {
                        onReceivedUiEffect(it)
                    }
                }
        }
    }

    /**
     * 处理由 [registerUiEffectFlow] 收集到的 [IUiEffect] 事件
     *
     * 子类可以通过重写本方法，自定义处理逻辑，或在调用 `super.onReceivedUiEffect()` 前后增加额外行为
     *
     * @param uiEffect 收到的 UI 事件，具体类型由 [IUiEffect] 定义
     */
    protected open suspend fun onReceivedUiEffect(uiEffect: IUiEffect) {
        if (uiEffect is AppUiEffect) handleAppUiEffect(uiEffect)
    }

    /**
     * 处理AppUiEffect
     */
    private fun handleAppUiEffect(uiEffect: AppUiEffect) {
        when (uiEffect) {
            is AppUiEffect.PopupToastMessage -> {
                Toast.makeText(this, uiEffect.message, Toast.LENGTH_SHORT).show()
            }

            is AppUiEffect.PopupToastMessageByResId -> {
                Toast.makeText(this, getString(uiEffect.messageResId), Toast.LENGTH_SHORT).show()
            }

            is AppUiEffect.StartActivity -> {
                val intent = Intent(this, uiEffect.activity).apply {
                    uiEffect.extras?.run { putExtras(this) }
                }
                startActivity(intent)
            }

            is AppUiEffect.StartActivityByIntent -> {
                startActivity(uiEffect.intent)
            }

            is AppUiEffect.SetResult -> {
                if (uiEffect.intent == null) {
                    setResult(uiEffect.resultCode)
                } else {
                    setResult(uiEffect.resultCode, uiEffect.intent)
                }
            }
        }
    }
}
