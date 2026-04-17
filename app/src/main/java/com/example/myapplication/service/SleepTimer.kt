package com.example.myapplication.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepTimer {
    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private var timerJob: Job? = null
    private var onTimerFinished: (() -> Unit)? = null

    fun start(durationMs: Long, scope: CoroutineScope, onFinished: () -> Unit) {
        cancel()
        onTimerFinished = onFinished
        _remainingMs.value = durationMs
        _isActive.value = true

        timerJob = scope.launch {
            while (_remainingMs.value > 0) {
                delay(1000)
                _remainingMs.value = (_remainingMs.value - 1000).coerceAtLeast(0)
            }
            _isActive.value = false
            onTimerFinished?.invoke()
        }
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _remainingMs.value = 0L
        _isActive.value = false
    }

    fun extend(extraMs: Long) {
        _remainingMs.value += extraMs
    }
}
