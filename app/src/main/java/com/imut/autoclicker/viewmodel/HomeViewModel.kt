package com.imut.autoclicker.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imut.autoclicker.accessibility.AutoClickerService
import com.imut.autoclicker.data.SettingsRepository
import com.imut.autoclicker.gesture.GestureEngine
import com.imut.autoclicker.gesture.GesturePoint
import com.imut.autoclicker.gesture.GestureSequence
import com.imut.autoclicker.gesture.GestureType
import com.imut.autoclicker.gesture.LoopMode
import com.imut.autoclicker.overlay.FloatingControlService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val gestureEngine = GestureEngine()

    private val _clickInterval = MutableStateFlow(1000L)
    val clickInterval: StateFlow<Long> = _clickInterval.asStateFlow()

    private val _repeatCount = MutableStateFlow(0) // 0 = infinite
    val repeatCount: StateFlow<Int> = _repeatCount.asStateFlow()

    // Points configured by user
    private val _points = MutableStateFlow<List<GesturePoint>>(emptyList())
    val points: StateFlow<List<GesturePoint>> = _points.asStateFlow()

    // Service states
    val isServiceRunning = AutoClickerService.isRunning
    val isServicePaused = AutoClickerService.isPaused

    val isExecuting = gestureEngine.isExecuting
    val executionCount = gestureEngine.executionCount

    init {
        viewModelScope.launch {
            settingsRepository.defaultClickInterval.collect {
                _clickInterval.value = it
            }
        }
        viewModelScope.launch {
            settingsRepository.defaultRepeatCount.collect {
                _repeatCount.value = it
            }
        }
    }

    fun isAccessibilityEnabled(): Boolean = AutoClickerService.isServiceEnabled()

    fun isOverlayEnabled(): Boolean {
        return Settings.canDrawOverlays(getApplication())
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(intent)
    }

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${getApplication<Application>().packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(intent)
    }

    fun setClickInterval(interval: Long) {
        _clickInterval.value = interval.coerceIn(50L, 60000L)
        viewModelScope.launch {
            settingsRepository.setDefaultClickInterval(_clickInterval.value)
        }
    }

    fun incrementInterval() {
        val step = when {
            _clickInterval.value < 100 -> 10L
            _clickInterval.value < 1000 -> 50L
            else -> 100L
        }
        setClickInterval(_clickInterval.value + step)
    }

    fun decrementInterval() {
        val step = when {
            _clickInterval.value <= 100 -> 10L
            _clickInterval.value <= 1000 -> 50L
            else -> 100L
        }
        setClickInterval(_clickInterval.value - step)
    }

    fun setRepeatCount(count: Int) {
        _repeatCount.value = count.coerceIn(0, 99999)
    }

    fun incrementRepeat() {
        if (_repeatCount.value == 0) {
            setRepeatCount(1)
        } else {
            setRepeatCount(_repeatCount.value + 1)
        }
    }

    fun decrementRepeat() {
        if (_repeatCount.value <= 1) {
            setRepeatCount(0) // Set to infinite
        } else {
            setRepeatCount(_repeatCount.value - 1)
        }
    }

    fun addPoint(x: Float, y: Float, type: GestureType = GestureType.TAP) {
        val newPoint = GesturePoint(
            id = _points.value.size,
            x = x,
            y = y,
            type = type,
            duration = when (type) {
                GestureType.TAP -> 100L
                GestureType.SWIPE -> 300L
                GestureType.LONG_PRESS -> 500L
            }
        )
        _points.value = _points.value + newPoint
    }

    fun removePoint(index: Int) {
        _points.value = _points.value.toMutableList().apply {
            removeAt(index)
        }.mapIndexed { i, point -> point.copy(id = i) }
    }

    fun updatePoint(index: Int, point: GesturePoint) {
        _points.value = _points.value.toMutableList().apply {
            if (index in indices) set(index, point)
        }
    }

    fun clearPoints() {
        _points.value = emptyList()
    }

    fun startAutoClicker() {
        if (!isAccessibilityEnabled()) return

        val sequence = buildSequence()
        gestureEngine.setSequence(sequence)
        gestureEngine.startExecution()

        // Start floating overlay
        FloatingControlService.setGestureEngine(gestureEngine)
        FloatingControlService.start(getApplication())
    }

    fun stopAutoClicker() {
        gestureEngine.stopExecution()
        FloatingControlService.stop(getApplication())
    }

    private fun buildSequence(): GestureSequence {
        val currentPoints = _points.value.ifEmpty {
            // If no points configured, use default tap at screen center
            listOf(GesturePoint(x = 540f, y = 960f, type = GestureType.TAP, duration = 100L))
        }

        return GestureSequence(
            points = currentPoints,
            loopMode = if (_repeatCount.value == 0) LoopMode.INFINITE else LoopMode.CUSTOM,
            loopCount = _repeatCount.value.coerceAtLeast(1),
            delayBetweenPoints = _clickInterval.value
        )
    }

    override fun onCleared() {
        super.onCleared()
        gestureEngine.destroy()
    }
}
