package com.imut.autoclicker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "autoclicker_settings")

/**
 * Repository for managing app settings using DataStore.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        // General
        val DEFAULT_CLICK_INTERVAL = longPreferencesKey("default_click_interval")
        val DEFAULT_CLICK_DURATION = longPreferencesKey("default_click_duration")
        val DEFAULT_REPEAT_COUNT = intPreferencesKey("default_repeat_count")

        // Overlay
        val OVERLAY_SIZE = stringPreferencesKey("overlay_size")
        val OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")

        // Appearance
        val THEME_MODE = stringPreferencesKey("theme_mode")

        // Advanced
        val IGNORE_BATTERY_OPT = booleanPreferencesKey("ignore_battery_opt")
        val SHOW_NOTIFICATION = booleanPreferencesKey("show_notification")
        val VIBRATE_ON_TAP = booleanPreferencesKey("vibrate_on_tap")

        // Gesture defaults
        val DEFAULT_GESTURE_TYPE = stringPreferencesKey("default_gesture_type")
        val DEFAULT_DELAY_BETWEEN_POINTS = longPreferencesKey("default_delay_between_points")
        val DEFAULT_LOOP_MODE = stringPreferencesKey("default_loop_mode")
    }

    // ============== General ==============

    val defaultClickInterval: Flow<Long> = dataStore.data.map {
        it[DEFAULT_CLICK_INTERVAL] ?: 1000L
    }

    suspend fun setDefaultClickInterval(interval: Long) {
        dataStore.edit { it[DEFAULT_CLICK_INTERVAL] = interval }
    }

    val defaultClickDuration: Flow<Long> = dataStore.data.map {
        it[DEFAULT_CLICK_DURATION] ?: 100L
    }

    suspend fun setDefaultClickDuration(duration: Long) {
        dataStore.edit { it[DEFAULT_CLICK_DURATION] = duration }
    }

    val defaultRepeatCount: Flow<Int> = dataStore.data.map {
        it[DEFAULT_REPEAT_COUNT] ?: 0 // 0 = infinite
    }

    suspend fun setDefaultRepeatCount(count: Int) {
        dataStore.edit { it[DEFAULT_REPEAT_COUNT] = count }
    }

    // ============== Overlay ==============

    val overlaySize: Flow<String> = dataStore.data.map {
        it[OVERLAY_SIZE] ?: "medium"
    }

    suspend fun setOverlaySize(size: String) {
        dataStore.edit { it[OVERLAY_SIZE] = size }
    }

    val overlayOpacity: Flow<Float> = dataStore.data.map {
        it[OVERLAY_OPACITY] ?: 0.8f
    }

    suspend fun setOverlayOpacity(opacity: Float) {
        dataStore.edit { it[OVERLAY_OPACITY] = opacity }
    }

    // ============== Appearance ==============

    val themeMode: Flow<String> = dataStore.data.map {
        it[THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    // ============== Advanced ==============

    val ignoreBatteryOpt: Flow<Boolean> = dataStore.data.map {
        it[IGNORE_BATTERY_OPT] ?: false
    }

    suspend fun setIgnoreBatteryOpt(ignore: Boolean) {
        dataStore.edit { it[IGNORE_BATTERY_OPT] = ignore }
    }

    val showNotification: Flow<Boolean> = dataStore.data.map {
        it[SHOW_NOTIFICATION] ?: true
    }

    suspend fun setShowNotification(show: Boolean) {
        dataStore.edit { it[SHOW_NOTIFICATION] = show }
    }

    val vibrateOnTap: Flow<Boolean> = dataStore.data.map {
        it[VIBRATE_ON_TAP] ?: false
    }

    suspend fun setVibrateOnTap(vibrate: Boolean) {
        dataStore.edit { it[VIBRATE_ON_TAP] = vibrate }
    }

    // ============== Gesture Defaults ==============

    val defaultGestureType: Flow<String> = dataStore.data.map {
        it[DEFAULT_GESTURE_TYPE] ?: "tap"
    }

    suspend fun setDefaultGestureType(type: String) {
        dataStore.edit { it[DEFAULT_GESTURE_TYPE] = type }
    }

    val defaultDelayBetweenPoints: Flow<Long> = dataStore.data.map {
        it[DEFAULT_DELAY_BETWEEN_POINTS] ?: 500L
    }

    suspend fun setDefaultDelayBetweenPoints(delay: Long) {
        dataStore.edit { it[DEFAULT_DELAY_BETWEEN_POINTS] = delay }
    }

    val defaultLoopMode: Flow<String> = dataStore.data.map {
        it[DEFAULT_LOOP_MODE] ?: "infinite"
    }

    suspend fun setDefaultLoopMode(mode: String) {
        dataStore.edit { it[DEFAULT_LOOP_MODE] = mode }
    }

    // ============== Reset ==============

    suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }
}
