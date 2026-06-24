package com.imut.autoclicker.gesture

import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import com.imut.autoclicker.accessibility.AutoClickerService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Gesture types supported by the auto-clicker.
 */
enum class GestureType {
    TAP,
    SWIPE,
    LONG_PRESS
}

/**
 * A single gesture point with its configuration.
 */
data class GesturePoint(
    val id: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val type: GestureType = GestureType.TAP,
    // For swipe: end coordinates
    val endX: Float = 0f,
    val endY: Float = 0f,
    // Timing
    val duration: Long = 100L,     // Duration of the gesture itself
    val delayBefore: Long = 0L,    // Delay before executing this point
    val repeatCount: Int = 1       // How many times to repeat this point
)

/**
 * Configuration for a gesture sequence.
 */
data class GestureSequence(
    val points: List<GesturePoint> = emptyList(),
    val loopMode: LoopMode = LoopMode.INFINITE,
    val loopCount: Int = 1,           // Only used when loopMode = CUSTOM
    val delayBetweenPoints: Long = 500L,
    val delayBetweenLoops: Long = 0L
)

enum class LoopMode {
    ONCE,
    INFINITE,
    CUSTOM
}

/**
 * Engine that orchestrates gesture sequences.
 * Manages timing, scheduling, and execution of click/swipe patterns.
 */
class GestureEngine {

    private var scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var executionJob: Job? = null

    private val _currentSequence = MutableStateFlow(GestureSequence())
    val currentSequence: StateFlow<GestureSequence> = _currentSequence.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _currentPointIndex = MutableStateFlow(0)
    val currentPointIndex: StateFlow<Int> = _currentPointIndex.asStateFlow()

    private val _currentLoop = MutableStateFlow(0)
    val currentLoop: StateFlow<Int> = _currentLoop.asStateFlow()

    private val _executionCount = MutableStateFlow(0)
    val executionCount: StateFlow<Int> = _executionCount.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.0f)
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    /**
     * Update the current gesture sequence.
     */
    fun setSequence(sequence: GestureSequence) {
        _currentSequence.value = sequence
    }

    /**
     * Set the speed multiplier (0.5x, 1x, 2x, etc.)
     */
    fun setSpeedMultiplier(multiplier: Float) {
        _speedMultiplier.value = multiplier.coerceIn(0.1f, 5.0f)
    }

    /**
     * Start executing the current gesture sequence.
     */
    fun startExecution() {
        val service = AutoClickerService.getInstance() ?: return
        val sequence = _currentSequence.value
        if (sequence.points.isEmpty()) return

        stopExecution()

        executionJob = scope.launch {
            _isExecuting.value = true
            _executionCount.value = 0
            _currentLoop.value = 0
            service.setRunning(true)
            service.setPaused(false)

            try {
                val maxLoops = when (sequence.loopMode) {
                    LoopMode.ONCE -> 1
                    LoopMode.INFINITE -> Int.MAX_VALUE
                    LoopMode.CUSTOM -> sequence.loopCount
                }

                for (loop in 0 until maxLoops) {
                    if (!isActive) break

                    _currentLoop.value = loop + 1

                    for ((index, point) in sequence.points.withIndex()) {
                        if (!isActive) break

                        // Wait if paused
                        while (service.isPaused.value && isActive) {
                            delay(100)
                        }

                        if (!isActive) break

                        _currentPointIndex.value = index

                        // Delay before this point
                        if (point.delayBefore > 0) {
                            delay((point.delayBefore / _speedMultiplier.value).toLong())
                        }

                        // Execute the gesture
                        repeat(point.repeatCount) { repeatIndex ->
                            if (!isActive) return@repeat

                            executeGesture(service, point)
                            _executionCount.value++

                            // Small delay between repeats of the same point
                            if (repeatIndex < point.repeatCount - 1) {
                                delay(50)
                            }
                        }

                        // Delay between points
                        if (index < sequence.points.size - 1 && sequence.delayBetweenPoints > 0) {
                            delay((sequence.delayBetweenPoints / _speedMultiplier.value).toLong())
                        }
                    }

                    // Delay between loops
                    if (loop < maxLoops - 1 && sequence.delayBetweenLoops > 0) {
                        delay((sequence.delayBetweenLoops / _speedMultiplier.value).toLong())
                    }
                }
            } catch (e: CancellationException) {
                // Normal cancellation, ignore
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isExecuting.value = false
                service.setRunning(false)
                service.setPaused(false)
            }
        }
    }

    /**
     * Pause the current execution.
     */
    fun pauseExecution() {
        AutoClickerService.getInstance()?.setPaused(true)
    }

    /**
     * Resume the current execution.
     */
    fun resumeExecution() {
        AutoClickerService.getInstance()?.setPaused(false)
    }

    /**
     * Stop the current execution.
     */
    fun stopExecution() {
        executionJob?.cancel()
        executionJob = null
        _isExecuting.value = false
        _currentPointIndex.value = 0
        _currentLoop.value = 0
        AutoClickerService.getInstance()?.setRunning(false)
        AutoClickerService.getInstance()?.setPaused(false)
    }

    /**
     * Execute a single gesture based on the point configuration.
     */
    private suspend fun executeGesture(service: AutoClickerService, point: GesturePoint) {
        suspendCancellableCoroutine { continuation ->
            val callback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }

            val dispatched = when (point.type) {
                GestureType.TAP -> {
                    service.performTap(
                        x = point.x,
                        y = point.y,
                        duration = (point.duration / _speedMultiplier.value).toLong(),
                        callback = callback
                    )
                }
                GestureType.SWIPE -> {
                    service.performSwipe(
                        startX = point.x,
                        startY = point.y,
                        endX = point.endX,
                        endY = point.endY,
                        duration = (point.duration / _speedMultiplier.value).toLong(),
                        callback = callback
                    )
                }
                GestureType.LONG_PRESS -> {
                    service.performLongPress(
                        x = point.x,
                        y = point.y,
                        duration = (point.duration / _speedMultiplier.value).toLong(),
                        callback = callback
                    )
                }
            }

            if (!dispatched) {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    /**
     * Clean up resources.
     */
    fun destroy() {
        stopExecution()
        scope.cancel()
    }

    /**
     * Reset the scope (e.g., after process restart).
     */
    fun resetScope() {
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }
}
