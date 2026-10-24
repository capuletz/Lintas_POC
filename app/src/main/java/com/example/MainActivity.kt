package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.AudioOutputMode
import com.example.model.RadioTab
import com.example.ui.components.RuggedTabsBar
import com.example.ui.components.VolumeKnobOverlay
import com.example.ui.screens.ChannelListScreen
import com.example.ui.screens.ChatMessagesScreen
import com.example.ui.screens.KeypadHardwareSettingsScreen
import com.example.ui.screens.RadioDashboardScreen
import com.example.ui.screens.ServerListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RadioDarkBg
import com.example.viewmodel.RadioViewModel

class MainActivity : ComponentActivity() {

    private val radioViewModel: RadioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialAudioPerm = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        radioViewModel.hasAudioPermission = initialAudioPerm

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = radioViewModel,
                    onRequestAudioPermission = { granted ->
                        radioViewModel.hasAudioPermission = granted
                    }
                )
            }
        }
    }

    /**
     * Intercepts hardware keys on Osaka Q8:
     * - Hardware PTT key down (Keycode 261 / F1 / HeadsetHook / Volume Down)
     * - Volume Knob rotation (Volume Up/Down)
     * - Front numeric keypad (0-9, *, #)
     * - D-Pad navigation (Up, Down, Left, Right, OK)
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val handled = radioViewModel.onHardwareKeyDown(keyCode)
        if (handled) return true
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Intercepts hardware PTT release on Osaka Q8
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val handled = radioViewModel.onHardwareKeyUp(keyCode)
        if (handled) return true
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun MainAppContent(
    viewModel: RadioViewModel,
    onRequestAudioPermission: (Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val servers by viewModel.servers.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onRequestAudioPermission(granted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Handle physical BACK key to return to QSO dashboard tab if on another screen
    BackHandler(enabled = state.currentTab != RadioTab.QSO) {
        viewModel.setTab(RadioTab.QSO)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RadioDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = RadioDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Active Screen Content (Compact QVGA layout)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    when (state.currentTab) {
                        RadioTab.QSO -> {
                            RadioDashboardScreen(
                                state = state,
                                settings = settings,
                                onStartTransmitting = { viewModel.startTransmitting() },
                                onStopTransmitting = { viewModel.stopTransmitting() },
                                onToggleMicMute = { viewModel.toggleMicMute() },
                                onToggleAudioDeafen = { viewModel.toggleAudioDeafen() },
                                onToggleAudioOutput = {
                                    val next = if (settings.audioOutput == AudioOutputMode.SPEAKER) AudioOutputMode.EARPIECE else AudioOutputMode.SPEAKER
                                    viewModel.updateSettings(settings.copy(audioOutput = next))
                                }
                            )
                        }

                        RadioTab.CHANNELS -> {
                            ChannelListScreen(
                                state = state,
                                onChannelSelected = { ch ->
                                    viewModel.selectChannel(ch)
                                    viewModel.setTab(RadioTab.QSO)
                                }
                            )
                        }

                        RadioTab.SERVERS -> {
                            ServerListScreen(
                                state = state,
                                servers = servers,
                                onConnectServer = { srv -> viewModel.connectServer(srv) },
                                onDisconnectServer = { viewModel.disconnectServer() },
                                onAddServer = { name, host, port, user ->
                                    viewModel.addServer(name, host, port, user)
                                },
                                onToggleFavorite = { srvId -> viewModel.toggleFavoriteServer(srvId) }
                            )
                        }

                        RadioTab.KEYS -> {
                            KeypadHardwareSettingsScreen(
                                state = state,
                                settings = settings,
                                onUpdateSettings = { newSettings ->
                                    viewModel.updateSettings(newSettings)
                                }
                            )
                        }

                        RadioTab.CHAT -> {
                            ChatMessagesScreen(
                                state = state,
                                onSendMessage = { text -> viewModel.sendChatMessage(text) }
                            )
                        }
                    }
                }

                // Bottom Rugged Navigation Bar
                RuggedTabsBar(
                    currentTab = state.currentTab,
                    onTabSelected = { tab -> viewModel.setTab(tab) },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Top Floating Volume Knob Overlay HUD
            VolumeKnobOverlay(
                visible = state.showVolumeOverlay,
                volumePercent = state.volumeLevel,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}
