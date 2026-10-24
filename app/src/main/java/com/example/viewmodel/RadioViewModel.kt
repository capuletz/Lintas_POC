package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MicAudioRecorder
import com.example.audio.RogerBeepGenerator
import com.example.data.MumbleRadioRepository
import com.example.hardware.KeycodeHelper
import com.example.model.ConnectionState
import com.example.model.HardwareKeyRecord
import com.example.model.MumbleChannel
import com.example.model.MumbleServer
import com.example.model.PttMode
import com.example.model.RadioSettings
import com.example.model.RadioState
import com.example.model.RadioTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MumbleRadioRepository(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _state = MutableStateFlow(
        RadioState(
            channels = repository.channels.value,
            usersInCurrentChannel = repository.users.value.filter { it.channelId == 1 },
            allOnlineUsers = repository.users.value,
            activeServer = repository.servers.value.firstOrNull(),
            volumeLevel = repository.settings.value.volumeLevel,
            chatMessages = repository.chatMessages.value
        )
    )
    val state: StateFlow<RadioState> = _state.asStateFlow()

    val settings: StateFlow<RadioSettings> = repository.settings
    val servers: StateFlow<List<MumbleServer>> = repository.servers

    private var volumeOverlayHideJob: Job? = null
    private var rxSimulationJob: Job? = null
    var hasAudioPermission: Boolean = false

    private val micRecorder = MicAudioRecorder { level ->
        _state.update { it.copy(txAudioLevel = level) }
    }

    init {
        // Collect repository updates
        viewModelScope.launch {
            repository.channels.collect { chList ->
                _state.update { current ->
                    current.copy(
                        channels = chList,
                        currentChannel = chList.find { it.id == current.currentChannel.id } ?: chList.firstOrNull() ?: current.currentChannel
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.chatMessages.collect { msgs ->
                _state.update { it.copy(chatMessages = msgs) }
            }
        }

        // Periodic incoming radio QSO simulation (so Osaka Q8 display shows real RX activity and speakers)
        startRxTrafficSimulation()
    }

    // --- HARDWARE KEY HANDLING (OSAKA Q8) ---

    fun onHardwareKeyDown(keyCode: Int): Boolean {
        val keyName = KeycodeHelper.getKeyLabel(keyCode)
        val currentSettings = settings.value

        _state.update {
            it.copy(
                lastHardwareKey = HardwareKeyRecord(keyCode, keyName, "DOWN")
            )
        }

        // 1. PTT Button Pressed
        if (keyCode == currentSettings.pttKeyCode) {
            if (currentSettings.pttMode == PttMode.MOMENTARY) {
                if (!_state.value.isTransmitting) {
                    startTransmitting()
                }
            } else {
                // TOGGLE Mode
                if (_state.value.isTransmitting) {
                    stopTransmitting()
                } else {
                    startTransmitting()
                }
            }
            return true
        }

        // 2. Hardware Volume Rotary Knob
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            adjustVolume(5)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Check if user specifically configured Volume Down as PTT
            if (currentSettings.pttKeyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (!_state.value.isTransmitting) startTransmitting()
                return true
            } else {
                adjustVolume(-5)
                return true
            }
        }

        // 3. Keypad Number 1..8: Quick Channel Switch (Osaka Q8 Front Keypad)
        if (keyCode in KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_8) {
            val chNum = keyCode - KeyEvent.KEYCODE_0
            switchChannelByNumber(chNum)
            if (currentSettings.keypadSoundEnabled) {
                RogerBeepGenerator.playKeyClickTone()
            }
            return true
        }

        // 4. Keypad Star (*): Toggle Mic Mute
        if (keyCode == KeyEvent.KEYCODE_STAR) {
            toggleMicMute()
            if (currentSettings.keypadSoundEnabled) RogerBeepGenerator.playKeyClickTone()
            return true
        }

        // 5. Keypad Pound (#): Toggle Audio Deafen / Mute Speaker
        if (keyCode == KeyEvent.KEYCODE_POUND) {
            toggleAudioDeafen()
            if (currentSettings.keypadSoundEnabled) RogerBeepGenerator.playKeyClickTone()
            return true
        }

        // 6. D-Pad Navigation between channels
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            navigateChannelDelta(-1)
            if (currentSettings.keypadSoundEnabled) RogerBeepGenerator.playKeyClickTone()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            navigateChannelDelta(1)
            if (currentSettings.keypadSoundEnabled) RogerBeepGenerator.playKeyClickTone()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            switchTabDelta(-1)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switchTabDelta(1)
            return true
        }

        return false
    }

    fun onHardwareKeyUp(keyCode: Int): Boolean {
        val keyName = KeycodeHelper.getKeyLabel(keyCode)
        val currentSettings = settings.value

        _state.update {
            it.copy(
                lastHardwareKey = HardwareKeyRecord(keyCode, keyName, "UP")
            )
        }

        if (keyCode == currentSettings.pttKeyCode) {
            if (currentSettings.pttMode == PttMode.MOMENTARY && _state.value.isTransmitting) {
                stopTransmitting()
            }
            return true
        }

        return false
    }

    // --- PTT & AUDIO TRANSMISSION ---

    fun startTransmitting() {
        if (_state.value.isMicMuted) {
            // Unmute mic automatically or warn
            _state.update { it.copy(isMicMuted = false) }
        }

        _state.update {
            it.copy(
                isTransmitting = true,
                activeSpeakerName = null, // Stop showing RX speaker while transmitting
                activeSpeakerCallsign = null,
                rxAudioLevel = 0f
            )
        }

        triggerHaptic(durationMs = 60)
        RogerBeepGenerator.playTxStartChirp()

        micRecorder.startRecording(
            hasPermission = hasAudioPermission,
            gainBoost = settings.value.micGainBoost
        )

        repository.addChatMessage(
            sender = "Osaka-Q8 (TX)",
            text = "--> PTT Active on [${_state.value.currentChannel.name}]",
            isSystem = true
        )
    }

    fun stopTransmitting() {
        if (!_state.value.isTransmitting) return

        _state.update {
            it.copy(
                isTransmitting = false,
                txAudioLevel = 0f
            )
        }

        micRecorder.stopRecording()
        triggerHaptic(durationMs = 40)

        if (settings.value.rogerBeepEnabled) {
            RogerBeepGenerator.playRogerBeep(settings.value.rogerBeepToneHz)
        }

        repository.addChatMessage(
            sender = "Osaka-Q8 (TX)",
            text = "<-- PTT Released. Roger beep transmitted.",
            isSystem = true
        )
    }

    // --- CHANNEL NAVIGATION ---

    fun selectChannel(channel: MumbleChannel) {
        _state.update { current ->
            current.copy(
                currentChannel = channel,
                usersInCurrentChannel = current.allOnlineUsers.filter { it.channelId == channel.id || it.isSelf }
            )
        }
        repository.addChatMessage("System", "Pindah ke [${channel.name}]", isSystem = true)
        if (settings.value.keypadSoundEnabled) {
            RogerBeepGenerator.playKeyClickTone()
        }
    }

    private fun switchChannelByNumber(number: Int) {
        val target = _state.value.channels.find { it.number == number }
        if (target != null) {
            selectChannel(target)
        }
    }

    private fun navigateChannelDelta(delta: Int) {
        val channels = _state.value.channels
        if (channels.isEmpty()) return
        val currentIndex = channels.indexOfFirst { it.id == _state.value.currentChannel.id }
        val newIndex = (currentIndex + delta).coerceIn(0, channels.size - 1)
        selectChannel(channels[newIndex])
    }

    // --- TAB SWITCHING ---

    fun setTab(tab: RadioTab) {
        _state.update { it.copy(currentTab = tab) }
        if (settings.value.keypadSoundEnabled) RogerBeepGenerator.playKeyClickTone()
    }

    private fun switchTabDelta(delta: Int) {
        val tabs = RadioTab.values()
        val currentIndex = tabs.indexOf(_state.value.currentTab)
        val newIndex = (currentIndex + delta + tabs.size) % tabs.size
        setTab(tabs[newIndex])
    }

    // --- VOLUME KNOB OVERLAY ---

    fun adjustVolume(deltaPercent: Int) {
        val current = _state.value.volumeLevel
        val newVolume = (current + deltaPercent).coerceIn(0, 100)
        _state.update {
            it.copy(
                volumeLevel = newVolume,
                showVolumeOverlay = true
            )
        }
        repository.updateSettings(settings.value.copy(volumeLevel = newVolume))

        // Set audio stream volume proportionally
        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val streamVal = (newVolume * maxVol / 100).coerceIn(0, maxVol)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVal, 0)
        } catch (_: Exception) {
        }

        // Hide overlay after 1.8 seconds of inactivity
        volumeOverlayHideJob?.cancel()
        volumeOverlayHideJob = viewModelScope.launch {
            delay(1800)
            _state.update { it.copy(showVolumeOverlay = false) }
        }
    }

    // --- MUTE & CONTROLS ---

    fun toggleMicMute() {
        val newMute = !_state.value.isMicMuted
        _state.update { it.copy(isMicMuted = newMute) }
        if (newMute && _state.value.isTransmitting) {
            stopTransmitting()
        }
        triggerHaptic(30)
    }

    fun toggleAudioDeafen() {
        val newDeafen = !_state.value.isAudioDeafened
        _state.update { it.copy(isAudioDeafened = newDeafen) }
        triggerHaptic(30)
    }

    fun updateSettings(newSettings: RadioSettings) {
        repository.updateSettings(newSettings)
        _state.update { it.copy(volumeLevel = newSettings.volumeLevel) }
    }

    fun connectServer(server: MumbleServer) {
        viewModelScope.launch {
            _state.update { it.copy(connectionState = ConnectionState.CONNECTING, activeServer = server) }
            delay(1000)
            _state.update { it.copy(connectionState = ConnectionState.CONNECTED) }
            repository.addChatMessage("System", "Terhubung ke [${server.name} - ${server.host}:${server.port}]", isSystem = true)
        }
    }

    fun disconnectServer() {
        _state.update { it.copy(connectionState = ConnectionState.DISCONNECTED) }
        repository.addChatMessage("System", "Terputus dari server", isSystem = true)
    }

    fun addServer(name: String, host: String, port: Int, username: String) {
        val srv = MumbleServer(
            id = "srv_${System.currentTimeMillis()}",
            name = name,
            host = host,
            port = port,
            username = username,
            isFavorite = false
        )
        repository.addServer(srv)
    }

    fun deleteServer(serverId: String) {
        repository.deleteServer(serverId)
    }

    fun toggleFavoriteServer(serverId: String) {
        repository.toggleFavoriteServer(serverId)
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        repository.addChatMessage(
            sender = "Osaka-Q8",
            text = text,
            isSystem = false
        )
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!settings.value.vibrationFeedback) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun startRxTrafficSimulation() {
        rxSimulationJob?.cancel()
        rxSimulationJob = viewModelScope.launch {
            val callsigns = listOf("YD1BZX-OmBudi", "JZ10XYZ-Dani", "Leader-Alpha", "Mobile-Bravo")
            while (true) {
                // Wait for silence
                delay(12000)
                if (!_state.value.isTransmitting && !_state.value.isAudioDeafened && _state.value.connectionState == ConnectionState.CONNECTED) {
                    val speaker = callsigns.random()
                    _state.update {
                        it.copy(
                            activeSpeakerName = speaker,
                            activeSpeakerCallsign = speaker,
                            rxAudioLevel = 45f
                        )
                    }
                    // Simulate receiving audio bursts for 3-5 seconds
                    for (i in 0..30) {
                        if (_state.value.isTransmitting) break
                        val simRx = (30f + 50f * kotlin.math.abs(kotlin.math.sin(i * 0.5))).toFloat()
                        _state.update { it.copy(rxAudioLevel = simRx) }
                        delay(100)
                    }
                    // Squelch close
                    _state.update {
                        it.copy(
                            activeSpeakerName = null,
                            activeSpeakerCallsign = null,
                            rxAudioLevel = 0f
                        )
                    }
                }
            }
        }
    }
}
