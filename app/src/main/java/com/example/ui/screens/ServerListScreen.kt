package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.model.MumbleServer
import com.example.model.RadioState
import com.example.ui.theme.RadioBorder
import com.example.ui.theme.RadioDarkBg
import com.example.ui.theme.RadioFocusRing
import com.example.ui.theme.RadioLcdAmber
import com.example.ui.theme.RadioLcdText
import com.example.ui.theme.RadioPttOrange
import com.example.ui.theme.RadioRxGreen
import com.example.ui.theme.RadioSurface
import com.example.ui.theme.RadioSurfaceVariant
import com.example.ui.theme.RadioTxRed

@Composable
fun ServerListScreen(
    state: RadioState,
    servers: List<MumbleServer>,
    onConnectServer: (MumbleServer) -> Unit,
    onDisconnectServer: () -> Unit,
    onAddServer: (name: String, host: String, port: Int, username: String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDarkBg)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Header with Add Server Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MUMBLE SERVERS (${servers.size})",
                color = RadioLcdAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .height(28.dp)
                    .testTag("add_server_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RadioPttOrange),
                shape = RoundedCornerShape(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.size(2.dp))
                Text("TAMBAH", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(servers) { server ->
                val isConnected = state.activeServer?.id == server.id && state.connectionState == ConnectionState.CONNECTED

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isConnected) Color(0xFF1B2E21) else RadioSurface)
                        .border(
                            width = if (isConnected) 1.5.dp else 1.dp,
                            color = if (isConnected) RadioRxGreen else RadioBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = "Server",
                                    tint = if (isConnected) RadioRxGreen else RadioLcdAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = server.name,
                                    color = if (isConnected) RadioLcdText else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Favorite toggle
                            IconButton(
                                onClick = { onToggleFavorite(server.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = if (server.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (server.isFavorite) RadioFocusRing else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Host, Port, and User details
                        Text(
                            text = "${server.host}:${server.port} [User: ${server.username}]",
                            color = Color(0xFF90A4AE),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Connect / Disconnect button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PING: ${server.pingMs}ms",
                                color = RadioLcdText,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Button(
                                onClick = {
                                    if (isConnected) onDisconnectServer() else onConnectServer(server)
                                },
                                modifier = Modifier
                                    .height(26.dp)
                                    .testTag("connect_btn_${server.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConnected) RadioTxRed else RadioSurfaceVariant
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isConnected) "PUTUSKAN" else "HUBUNGKAN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isConnected) Color.White else RadioFocusRing
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var hostInput by remember { mutableStateOf("") }
        var portInput by remember { mutableStateOf("64738") }
        var userInput by remember { mutableStateOf("Osaka-Q8") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Tambah Server Mumble",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Server", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("server_name_field"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Host / IP Address", fontSize = 10.sp) },
                        placeholder = { Text("mumble.example.org", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("server_host_field"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Port", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).testTag("server_port_field"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            label = { Text("Callsign / User", fontSize = 10.sp) },
                            modifier = Modifier.weight(1.5f).testTag("server_user_field"),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && hostInput.isNotBlank()) {
                            val port = portInput.toIntOrNull() ?: 64738
                            onAddServer(nameInput, hostInput, port, userInput.ifBlank { "Osaka-Q8" })
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RadioPttOrange),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("SIMPAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("BATAL", fontSize = 10.sp, color = Color.Gray)
                }
            },
            containerColor = RadioSurface,
            shape = RoundedCornerShape(8.dp)
        )
    }
}
