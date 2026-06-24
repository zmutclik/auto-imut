package com.imut.autoclicker.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.imut.autoclicker.AutoClickerApp
import com.imut.autoclicker.MainActivity
import com.imut.autoclicker.R
import com.imut.autoclicker.accessibility.AutoClickerService
import com.imut.autoclicker.gesture.GestureEngine
import com.imut.autoclicker.gesture.GestureType

/**
 * Foreground service that manages the floating overlay control panel.
 * The overlay appears above all apps and provides Play/Pause/Stop controls.
 */
class FloatingControlService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001

        private var instance: FloatingControlService? = null
        private var gestureEngine: GestureEngine? = null

        fun getInstance(): FloatingControlService? = instance

        fun start(context: Context) {
            val intent = Intent(context, FloatingControlService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingControlService::class.java))
        }

        fun setGestureEngine(engine: GestureEngine) {
            gestureEngine = engine
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var expandedView: View? = null
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, createNotification())
        createCompactOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        removeOverlay()
    }

    // ============== Notification ==============

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FloatingControlService::class.java)
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, AutoClickerApp.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("AutoClicker Active")
                .setContentText("Tap to open settings")
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("AutoClicker Active")
                .setContentText("Tap to open settings")
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    // ============== Overlay Creation ==============

    private fun createCompactOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // Create compact floating bar
        val compactLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
            setPadding(24, 16, 24, 16)
            elevation = 8f
        }

        // Play button
        val playBtn = createOverlayButton("▶").apply {
            setOnClickListener {
                gestureEngine?.startExecution()
            }
        }

        // Pause button
        val pauseBtn = createOverlayButton("⏸").apply {
            setOnClickListener {
                val engine = gestureEngine ?: return@setOnClickListener
                if (engine.isExecuting.value) {
                    engine.pauseExecution()
                } else {
                    engine.resumeExecution()
                }
            }
        }

        // Stop button
        val stopBtn = createOverlayButton("⏹").apply {
            setOnClickListener {
                gestureEngine?.stopExecution()
            }
        }

        // Menu button (expand)
        val menuBtn = createOverlayButton("☰").apply {
            setOnClickListener {
                toggleExpanded()
            }
        }

        compactLayout.addView(playBtn)
        compactLayout.addView(pauseBtn)
        compactLayout.addView(stopBtn)
        compactLayout.addView(menuBtn)

        // Make draggable
        makeDraggable(compactLayout, params)

        overlayView = compactLayout
        windowManager.addView(compactLayout, params)
    }

    private fun createOverlayButton(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(20, 10, 20, 10)
            gravity = Gravity.CENTER
        }
    }

    private fun toggleExpanded() {
        if (isExpanded) {
            removeExpandedView()
        } else {
            showExpandedView()
        }
        isExpanded = !isExpanded
    }

    private fun showExpandedView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 100
        }

        val expandedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
            setPadding(32, 24, 32, 24)
            elevation = 10f
        }

        // Header
        val header = TextView(this).apply {
            text = "🖱️ AutoClicker"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 16)
        }

        // Status info
        val statusText = TextView(this).apply {
            text = "● Status: Idle"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 8, 0, 8)
        }

        val pointText = TextView(this).apply {
            text = "📍 Point: (-, -)"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 4, 0, 4)
        }

        val intervalText = TextView(this).apply {
            text = "⏱ Interval: 1000ms"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 4, 0, 4)
        }

        val countText = TextView(this).apply {
            text = "🔢 Count: 0"
            textSize = 13f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 4, 0, 16)
        }

        // Controls
        val controlsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }

        controlsLayout.addView(createOverlayButton("▶").apply {
            setOnClickListener { gestureEngine?.startExecution() }
        })
        controlsLayout.addView(createOverlayButton("⏸").apply {
            setOnClickListener {
                val engine = gestureEngine ?: return@setOnClickListener
                if (engine.isExecuting.value) engine.pauseExecution()
                else engine.resumeExecution()
            }
        })
        controlsLayout.addView(createOverlayButton("⏹").apply {
            setOnClickListener { gestureEngine?.stopExecution() }
        })

        // Open app button
        val openAppBtn = TextView(this).apply {
            text = "⚙️ Open Settings"
            textSize = 13f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(24, 16, 24, 16)
            gravity = Gravity.CENTER
            setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
            setOnClickListener {
                val intent = Intent(this@FloatingControlService, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        // Close button
        val closeBtn = TextView(this).apply {
            text = "✕"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(16, 8, 16, 8)
            gravity = Gravity.END
            setOnClickListener { toggleExpanded() }
        }

        // Layout
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(header, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(closeBtn)
        }

        expandedLayout.addView(headerLayout)
        expandedLayout.addView(statusText)
        expandedLayout.addView(pointText)
        expandedLayout.addView(intervalText)
        expandedLayout.addView(countText)
        expandedLayout.addView(controlsLayout)
        expandedLayout.addView(openAppBtn)

        expandedView = expandedLayout
        windowManager.addView(expandedLayout, params)
    }

    private fun removeExpandedView() {
        expandedView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        expandedView = null
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        overlayView = null
        removeExpandedView()
    }

    // ============== Drag Support ==============

    private fun makeDraggable(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (dx * dx + dy * dy > 25) { // 5px threshold
                        isDragging = true
                    }
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    isDragging
                }
                else -> false
            }
        }
    }
}
