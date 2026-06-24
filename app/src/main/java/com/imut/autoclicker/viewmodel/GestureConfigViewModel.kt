package com.imut.autoclicker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imut.autoclicker.data.MacroEntity
import com.imut.autoclicker.data.MacroRepository
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
class GestureConfigViewModel @Inject constructor(
    application: Application,
    private val macroRepository: MacroRepository
) : AndroidViewModel(application) {

    private val _points = MutableStateFlow<List<GesturePoint>>(emptyList())
    val points: StateFlow<List<GesturePoint>> = _points.asStateFlow()

    private val _gestureType = MutableStateFlow(GestureType.TAP)
    val gestureType: StateFlow<GestureType> = _gestureType.asStateFlow()

    private val _loopMode = MutableStateFlow(LoopMode.INFINITE)
    val loopMode: StateFlow<LoopMode> = _loopMode.asStateFlow()

    private val _loopCount = MutableStateFlow(1)
    val loopCount: StateFlow<Int> = _loopCount.asStateFlow()

    private val _delayBetweenPoints = MutableStateFlow(500L)
    val delayBetweenPoints: StateFlow<Long> = _delayBetweenPoints.asStateFlow()

    private val _editingMacroId = MutableStateFlow<Long?>(null)

    fun setGestureType(type: GestureType) {
        _gestureType.value = type
    }

    fun setLoopMode(mode: LoopMode) {
        _loopMode.value = mode
    }

    fun setLoopCount(count: Int) {
        _loopCount.value = count.coerceIn(1, 99999)
    }

    fun setDelayBetweenPoints(delay: Long) {
        _delayBetweenPoints.value = delay.coerceIn(0L, 10000L)
    }

    fun incrementDelay() {
        val step = when {
            _delayBetweenPoints.value < 100 -> 10L
            _delayBetweenPoints.value < 1000 -> 50L
            else -> 100L
        }
        setDelayBetweenPoints(_delayBetweenPoints.value + step)
    }

    fun decrementDelay() {
        val step = when {
            _delayBetweenPoints.value <= 100 -> 10L
            _delayBetweenPoints.value <= 1000 -> 50L
            else -> 100L
        }
        setDelayBetweenPoints(_delayBetweenPoints.value - step)
    }

    fun incrementLoopCount() {
        setLoopCount(_loopCount.value + 1)
    }

    fun decrementLoopCount() {
        setLoopCount(_loopCount.value - 1)
    }

    fun addPoint(x: Float, y: Float) {
        val type = _gestureType.value
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

    fun addSwipePoint(startX: Float, startY: Float, endX: Float, endY: Float) {
        val newPoint = GesturePoint(
            id = _points.value.size,
            x = startX,
            y = startY,
            type = GestureType.SWIPE,
            endX = endX,
            endY = endY,
            duration = 300L
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

    fun loadMacro(macroId: Long) {
        viewModelScope.launch {
            val macro = macroRepository.getMacroById(macroId) ?: return@launch
            _editingMacroId.value = macro.id
            _points.value = macro.points
            _loopMode.value = try { LoopMode.valueOf(macro.loopMode) } catch (e: Exception) { LoopMode.INFINITE }
            _loopCount.value = macro.loopCount
            _delayBetweenPoints.value = macro.delayBetweenPoints
        }
    }

    fun saveMacro(name: String) {
        viewModelScope.launch {
            val sequence = buildSequence()
            val macro = MacroEntity.fromSequence(name, sequence)
            val existingId = _editingMacroId.value
            if (existingId != null) {
                macroRepository.updateMacro(macro.copy(id = existingId))
            } else {
                macroRepository.saveMacro(macro)
            }
        }
    }

    fun testSequence() {
        val sequence = buildSequence()
        val engine = GestureEngine()
        engine.setSequence(sequence)
        engine.startExecution()
    }

    fun buildSequence(): GestureSequence {
        return GestureSequence(
            points = _points.value,
            loopMode = _loopMode.value,
            loopCount = _loopCount.value,
            delayBetweenPoints = _delayBetweenPoints.value
        )
    }
}
