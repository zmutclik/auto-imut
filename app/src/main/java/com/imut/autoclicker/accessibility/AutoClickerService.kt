package com.imut.autoclicker.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core AccessibilityService that dispatches gestures (tap, swipe, long press)
 * using Android's AccessibilityService.dispatchGesture() API.
 */
class AutoClickerService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoClickerService"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private var instance: AutoClickerService? = null

        fun getInstance(): AutoClickerService? = instance

        fun isServiceEnabled(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to process accessibility events for auto-clicking
        // This service is used purely for gesture dispatch
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isRunning.value = false
        _isPaused.value = false
        Log.d(TAG, "Accessibility service destroyed")
    }

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }

    // ============== Gesture Dispatch Methods ==============

    /**
     * Perform a single tap at the given coordinates.
     * @param x X coordinate in absolute screen pixels
     * @param y Y coordinate in absolute screen pixels
     * @param duration Duration of the tap in milliseconds (default 100ms)
     * @param callback Optional callback for gesture completion
     */
    fun performTap(
        x: Float,
        y: Float,
        duration: Long = 100L,
        callback: GestureResultCallback? = null
    ): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return dispatchGesture(gesture, callback, null)
    }

    /**
     * Perform a swipe gesture from one point to another.
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param duration Duration of the swipe in milliseconds
     * @param callback Optional callback for gesture completion
     */
    fun performSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        duration: Long = 300L,
        callback: GestureResultCallback? = null
    ): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return dispatchGesture(gesture, callback, null)
    }

    /**
     * Perform a long press at the given coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param duration Duration of the long press in milliseconds (default 500ms)
     * @param callback Optional callback for gesture completion
     */
    fun performLongPress(
        x: Float,
        y: Float,
        duration: Long = 500L,
        callback: GestureResultCallback? = null
    ): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return dispatchGesture(gesture, callback, null)
    }

    /**
     * Perform a multi-touch gesture with multiple simultaneous strokes.
     * @param points List of (x, y) pairs for each touch point
     * @param duration Duration of the gesture in milliseconds
     * @param callback Optional callback for gesture completion
     */
    fun performMultiTouch(
        points: List<Pair<Float, Float>>,
        duration: Long = 100L,
        callback: GestureResultCallback? = null
    ): Boolean {
        val builder = GestureDescription.Builder()

        points.forEach { (x, y) ->
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, duration)
            builder.addStroke(stroke)
        }

        val gesture = builder.build()
        return dispatchGesture(gesture, callback, null)
    }

    /**
     * Perform a custom gesture with a complex path.
     * @param pathPoints List of (x, y) pairs defining the path
     * @param duration Duration of the gesture in milliseconds
     * @param callback Optional callback for gesture completion
     */
    fun performCustomGesture(
        pathPoints: List<Pair<Float, Float>>,
        duration: Long = 500L,
        callback: GestureResultCallback? = null
    ): Boolean {
        if (pathPoints.isEmpty()) return false

        val path = Path().apply {
            moveTo(pathPoints[0].first, pathPoints[0].second)
            pathPoints.drop(1).forEach { (x, y) ->
                lineTo(x, y)
            }
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return dispatchGesture(gesture, callback, null)
    }

    /**
     * Get maximum gesture duration supported by this device.
     */
    fun getMaxGestureDuration(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                // This is a system-defined limit, typically 60000ms
                60000L
            } catch (e: Exception) {
                60000L
            }
        } else {
            60000L
        }
    }

    /**
     * Get maximum stroke count supported by this device.
     */
    fun getMaxStrokeCount(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                // Typically 10 strokes max on most devices
                10
            } catch (e: Exception) {
                10
            }
        } else {
            10
        }
    }
}
