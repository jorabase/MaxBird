package com.example.common.network

import com.example.common.model.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Singleton Event Bus emitting syllabus update events and triggering cache invalidations
 */
object AppGlobalEventBus {
    private val _events = MutableSharedFlow<AppEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }

    fun tryEmit(event: AppEvent): Boolean {
        return _events.tryEmit(event)
    }
}
