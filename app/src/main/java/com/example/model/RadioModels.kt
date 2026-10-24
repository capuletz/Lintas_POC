package com.example.model

data class MumbleServer(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 64738,
    val username: String,
    val password: String = "",
    val isFavorite: Boolean = false,
    val pingMs: Int = 32,
    val isConnected: Boolean = false
)

data class MumbleChannel(
    val id: Int,
    val number: Int, // 1 to 99 for Osaka Q8 keypad direct jump
    val name: String,
    val description: String = "",
    val userCount: Int = 0,
    val isDefault: Boolean = false
)

data class MumbleUser(
    val id: Int,
    val name: String,
    val channelId: Int,
    val isTalking: Boolean = false,
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isSelf: Boolean = false,
    val pingMs: Int = 28
)

data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: String,
    val isSystem: Boolean = false
)

enum class PttMode {
    MOMENTARY, // Hold physical PTT button to talk, release to listen
    TOGGLE     // Press once to transmit, press again to stop
}

enum class AudioOutputMode {
    SPEAKER,
    EARPIECE
}

enum class ScreenDensityMode {
    QVGA_COMPACT, // 240 x 320 px (Osaka Q8 optimized)
    HVGA_STANDARD // 320 x 480 px
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

enum class RadioTab(val title: String, val shortcut: String) {
    QSO("QSO/PTT", "1"),
    CHANNELS("CH", "2"),
    SERVERS("SRV", "3"),
    KEYS("KEYS", "4"),
    CHAT("LOG", "5")
}

data class RadioSettings(
    val pttKeyCode: Int = 261, // KeyCode 261 is standard POC PTT (Osaka Q8 / Inrico)
    val pttMode: PttMode = PttMode.MOMENTARY,
    val rogerBeepEnabled: Boolean = true,
    val rogerBeepToneHz: Int = 1050,
    val vibrationFeedback: Boolean = true,
    val audioOutput: AudioOutputMode = AudioOutputMode.SPEAKER,
    val micGainBoost: Float = 1.2f,
    val screenDensityMode: ScreenDensityMode = ScreenDensityMode.QVGA_COMPACT,
    val keypadSoundEnabled: Boolean = true,
    val volumeLevel: Int = 75 // 0 to 100
)

data class RadioState(
    val connectionState: ConnectionState = ConnectionState.CONNECTED,
    val activeServer: MumbleServer? = null,
    val currentChannel: MumbleChannel = MumbleChannel(1, 1, "CH-01 REPEATERS", "All-Indonesia Mumble Intercom", 4, true),
    val channels: List<MumbleChannel> = emptyList(),
    val usersInCurrentChannel: List<MumbleUser> = emptyList(),
    val allOnlineUsers: List<MumbleUser> = emptyList(),
    val isTransmitting: Boolean = false,
    val isMicMuted: Boolean = false,
    val isAudioDeafened: Boolean = false,
    val txAudioLevel: Float = 0f, // 0..100
    val rxAudioLevel: Float = 0f, // 0..100
    val activeSpeakerName: String? = null,
    val activeSpeakerCallsign: String? = null,
    val volumeLevel: Int = 75,
    val showVolumeOverlay: Boolean = false,
    val lastHardwareKey: HardwareKeyRecord? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val currentTab: RadioTab = RadioTab.QSO,
    val focusedItemIndex: Int = 0
)

data class HardwareKeyRecord(
    val keyCode: Int,
    val keyName: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
