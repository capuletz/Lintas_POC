package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AudioOutputMode
import com.example.model.ChatMessage
import com.example.model.MumbleChannel
import com.example.model.MumbleServer
import com.example.model.MumbleUser
import com.example.model.PttMode
import com.example.model.RadioSettings
import com.example.model.ScreenDensityMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MumbleRadioRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("osaka_q8_mumble_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<RadioSettings> = _settings.asStateFlow()

    private val _servers = MutableStateFlow(loadDefaultServers())
    val servers: StateFlow<List<MumbleServer>> = _servers.asStateFlow()

    private val _channels = MutableStateFlow(generateDefaultChannels())
    val channels: StateFlow<List<MumbleChannel>> = _channels.asStateFlow()

    private val _users = MutableStateFlow(generateDefaultUsers())
    val users: StateFlow<List<MumbleUser>> = _users.asStateFlow()

    private val _chatMessages = MutableStateFlow(generateInitialChat())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private fun loadSettings(): RadioSettings {
        val pttKey = prefs.getInt("ptt_key", 261)
        val pttModeStr = prefs.getString("ptt_mode", PttMode.MOMENTARY.name) ?: PttMode.MOMENTARY.name
        val rogerBeep = prefs.getBoolean("roger_beep", true)
        val rogerHz = prefs.getInt("roger_hz", 1050)
        val vibration = prefs.getBoolean("vibration", true)
        val audioOutStr = prefs.getString("audio_out", AudioOutputMode.SPEAKER.name) ?: AudioOutputMode.SPEAKER.name
        val micGain = prefs.getFloat("mic_gain", 1.2f)
        val densityStr = prefs.getString("screen_density", ScreenDensityMode.QVGA_COMPACT.name) ?: ScreenDensityMode.QVGA_COMPACT.name
        val keypadSound = prefs.getBoolean("keypad_sound", true)
        val volume = prefs.getInt("volume_level", 75)

        return RadioSettings(
            pttKeyCode = pttKey,
            pttMode = try { PttMode.valueOf(pttModeStr) } catch (_: Exception) { PttMode.MOMENTARY },
            rogerBeepEnabled = rogerBeep,
            rogerBeepToneHz = rogerHz,
            vibrationFeedback = vibration,
            audioOutput = try { AudioOutputMode.valueOf(audioOutStr) } catch (_: Exception) { AudioOutputMode.SPEAKER },
            micGainBoost = micGain,
            screenDensityMode = try { ScreenDensityMode.valueOf(densityStr) } catch (_: Exception) { ScreenDensityMode.QVGA_COMPACT },
            keypadSoundEnabled = keypadSound,
            volumeLevel = volume
        )
    }

    fun updateSettings(newSettings: RadioSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putInt("ptt_key", newSettings.pttKeyCode)
            .putString("ptt_mode", newSettings.pttMode.name)
            .putBoolean("roger_beep", newSettings.rogerBeepEnabled)
            .putInt("roger_hz", newSettings.rogerBeepToneHz)
            .putBoolean("vibration", newSettings.vibrationFeedback)
            .putString("audio_out", newSettings.audioOutput.name)
            .putFloat("mic_gain", newSettings.micGainBoost)
            .putString("screen_density", newSettings.screenDensityMode.name)
            .putBoolean("keypad_sound", newSettings.keypadSoundEnabled)
            .putInt("volume_level", newSettings.volumeLevel)
            .apply()
    }

    private fun loadDefaultServers(): List<MumbleServer> {
        return listOf(
            MumbleServer(
                id = "srv_default_1",
                name = "Indonesia HT Osaka Net",
                host = "mumble.indonesia-radio.net",
                port = 64738,
                username = "Osaka-Q8-User",
                isFavorite = true,
                pingMs = 24,
                isConnected = true
            ),
            MumbleServer(
                id = "srv_default_2",
                name = "RAPI & ORARI Gateway PoC",
                host = "poc.rapi-nusantara.org",
                port = 64738,
                username = "YD2OSK-Q8",
                isFavorite = true,
                pingMs = 38
            ),
            MumbleServer(
                id = "srv_default_3",
                name = "Public Mumble Voice HQ",
                host = "voice.cleanvoice.ru",
                port = 64738,
                username = "HT_Osaka_Q8",
                isFavorite = false,
                pingMs = 76
            ),
            MumbleServer(
                id = "srv_default_4",
                name = "Emergency Intercom Rescue 115",
                host = "rescue.mumble.cloud",
                port = 64738,
                username = "Basarnas-Q8",
                isFavorite = false,
                pingMs = 19
            )
        )
    }

    fun addServer(server: MumbleServer) {
        val updated = _servers.value + server
        _servers.value = updated
    }

    fun deleteServer(serverId: String) {
        _servers.value = _servers.value.filter { it.id != serverId }
    }

    fun toggleFavoriteServer(serverId: String) {
        _servers.value = _servers.value.map {
            if (it.id == serverId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    private fun generateDefaultChannels(): List<MumbleChannel> {
        return listOf(
            MumbleChannel(1, 1, "CH-01 REPEATERS", "All-Indonesia Mumble Intercom Gateway", 5, true),
            MumbleChannel(2, 2, "CH-02 EMERGENCY", "SAR & Tanggap Darurat Bencana", 2, false),
            MumbleChannel(3, 3, "CH-03 LOGISTIK & CONVOY", "Komunikasi Touring & Ekspedisi", 3, false),
            MumbleChannel(4, 4, "CH-04 SECURITY OPS", "Patroli Area & Operasional Lapangan", 4, false),
            MumbleChannel(5, 5, "CH-05 ORARI HF INTERCOM", "Gateway Amatir Radio 40M/2M", 6, false),
            MumbleChannel(6, 6, "CH-06 RAPI NUSANTARA", "Radio Antar Penduduk 10-28", 3, false),
            MumbleChannel(7, 7, "CH-07 CHAT SANTAI", "Kopi Darat Online & Ngobrol Bebas", 2, false),
            MumbleChannel(8, 8, "CH-08 TEST AUDIO / ECHO", "Loopback echo channel untuk test suara", 1, false)
        )
    }

    private fun generateDefaultUsers(): List<MumbleUser> {
        return listOf(
            MumbleUser(1, "Osaka-Q8 (You)", 1, isTalking = false, isSelf = true, pingMs = 24),
            MumbleUser(2, "YD1BZX-OmBudi", 1, isTalking = false, pingMs = 31),
            MumbleUser(3, "JZ10XYZ-Dani", 1, isTalking = false, pingMs = 28),
            MumbleUser(4, "Leader-Alpha", 1, isTalking = false, pingMs = 45),
            MumbleUser(5, "Mobile-Bravo", 1, isTalking = false, pingMs = 52),
            MumbleUser(6, "SAR-Rescue-01", 2, isTalking = false, pingMs = 20),
            MumbleUser(7, "Command-Post", 2, isTalking = false, pingMs = 18),
            MumbleUser(8, "Convoy-Lead", 3, isTalking = false, pingMs = 35)
        )
    }

    private fun generateInitialChat(): List<ChatMessage> {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return listOf(
            ChatMessage("1", "System", "Terhubung ke Mumble Server [Indonesia HT Osaka Net:64738]", now, isSystem = true),
            ChatMessage("2", "System", "Bergabung ke [CH-01 REPEATERS]", now, isSystem = true),
            ChatMessage("3", "YD1BZX-OmBudi", "Selamat siang rekan-rekan Osaka Q8, audio 10-2 terdengar jernih", now),
            ChatMessage("4", "JZ10XYZ-Dani", "Roger monitor di frekuensi CH-01. PTT berfungsi normal", now)
        )
    }

    fun addChatMessage(sender: String, text: String, isSystem: Boolean = false) {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = sender,
            text = text,
            timestamp = now,
            isSystem = isSystem
        )
        _chatMessages.value = _chatMessages.value + msg
    }
}
