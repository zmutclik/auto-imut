package com.imut.autoclicker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imut.autoclicker.data.MacroEntity
import com.imut.autoclicker.data.MacroRepository
import com.imut.autoclicker.data.SettingsRepository
import com.imut.autoclicker.gesture.GestureEngine
import com.imut.autoclicker.overlay.FloatingControlService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val defaultClickInterval = settingsRepository.defaultClickInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000L)

    val defaultClickDuration = settingsRepository.defaultClickDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100L)

    val overlaySize = settingsRepository.overlaySize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "medium")

    val overlayOpacity = settingsRepository.overlayOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.8f)

    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val ignoreBatteryOpt = settingsRepository.ignoreBatteryOpt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showNotification = settingsRepository.showNotification
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrateOnTap = settingsRepository.vibrateOnTap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDefaultClickInterval(interval: Long) {
        viewModelScope.launch { settingsRepository.setDefaultClickInterval(interval) }
    }

    fun setDefaultClickDuration(duration: Long) {
        viewModelScope.launch { settingsRepository.setDefaultClickDuration(duration) }
    }

    fun setOverlaySize(size: String) {
        viewModelScope.launch { settingsRepository.setOverlaySize(size) }
    }

    fun setOverlayOpacity(opacity: Float) {
        viewModelScope.launch { settingsRepository.setOverlayOpacity(opacity) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setIgnoreBatteryOpt(ignore: Boolean) {
        viewModelScope.launch { settingsRepository.setIgnoreBatteryOpt(ignore) }
    }

    fun setShowNotification(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowNotification(show) }
    }

    fun setVibrateOnTap(vibrate: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrateOnTap(vibrate) }
    }

    fun resetAll() {
        viewModelScope.launch { settingsRepository.resetAll() }
    }
}

@HiltViewModel
class MacroListViewModel @Inject constructor(
    application: Application,
    private val macroRepository: MacroRepository
) : AndroidViewModel(application) {

    val macros = macroRepository.getAllMacros()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteMacro(macro: MacroEntity) {
        viewModelScope.launch { macroRepository.deleteMacro(macro) }
    }

    fun runMacro(macro: MacroEntity) {
        viewModelScope.launch {
            macroRepository.markUsed(macro.id)
        }

        val sequence = macro.toSequence()
        val engine = GestureEngine()
        engine.setSequence(sequence)
        engine.startExecution()

        FloatingControlService.setGestureEngine(engine)
        FloatingControlService.start(getApplication())
    }
}
