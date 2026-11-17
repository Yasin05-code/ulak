package com.ulak.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulak.app.R
import com.ulak.app.permission.PermissionHelper
import com.ulak.app.ui.theme.SafeGreen
import com.ulak.app.ui.theme.SosRed
import com.ulak.app.ui.theme.UlakTheme
import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val peers by viewModel.connectedPeers.collectAsState()
    val isCommunicationOn by viewModel.isCommunicationOn.collectAsState()
    val permissionNotice by viewModel.permissionNotice.collectAsState()
    val context = LocalContext.current

    // Opens the system permission dialog; Android skips it for permissions already granted.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.onPermissionResult() }

    HomeContent(
        deviceId = viewModel.deviceId,
        isCommunicationOn = isCommunicationOn,
        peerCount = peers.size,
        messages = messages,
        permissionNotice = permissionNotice,
        onCommunicationClick = {
            if (isCommunicationOn) {
                viewModel.stopCommunication()
            } else {
                permissionLauncher.launch(PermissionHelper.permissionsToRequest())
            }
        },
        onSafeClick = viewModel::onSafeClick,
        onSosClick = viewModel::onSosClick,
        onOpenSettingsClick = { openAppSettings(context) }
    )
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null))
    context.startActivity(intent)
}

/** Pure layout: gets data and callbacks as parameters, so it is easy to preview. */
@Composable
private fun HomeContent(
    deviceId: String,
    isCommunicationOn: Boolean,
    peerCount: Int,
    messages: List<Message>,
    permissionNotice: PermissionNotice?,
    onCommunicationClick: () -> Unit,
    onSafeClick: () -> Unit,
    onSosClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(
                    if (isCommunicationOn) R.string.mode_active else R.string.mode_off
                ),
                style = MaterialTheme.typography.titleMedium
            )
            if (isCommunicationOn) {
                Text(stringResource(R.string.nearby_phones, peerCount))
            }

            OutlinedButton(
                onClick = onCommunicationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (isCommunicationOn) {
                            R.string.button_stop_communication
                        } else {
                            R.string.button_start_communication
                        }
                    )
                )
            }

            if (permissionNotice != null) {
                PermissionNoticeText(permissionNotice, onOpenSettingsClick)
            }

            BigButton(stringResource(R.string.button_safe), SafeGreen, onSafeClick)
            BigButton(stringResource(R.string.button_sos), SosRed, onSosClick)

            Text(
                text = stringResource(R.string.messages_title),
                style = MaterialTheme.typography.titleMedium
            )
            MessageList(messages = messages, deviceId = deviceId)
        }
    }
}

@Composable
private fun BigButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)
    ) {
        Text(text = text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermissionNoticeText(notice: PermissionNotice, onOpenSettingsClick: () -> Unit) {
    val text = when (notice) {
        PermissionNotice.REQUIRED_DENIED -> stringResource(R.string.permission_required_denied)
        PermissionNotice.NOTIFICATIONS_DENIED -> stringResource(R.string.permission_notifications_denied)
    }
    Column {
        Text(text = text, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onOpenSettingsClick) {
            Text(stringResource(R.string.permission_open_settings))
        }
    }
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

@Composable
private fun MessageList(messages: List<Message>, deviceId: String) {
    if (messages.isEmpty()) {
        Text(stringResource(R.string.messages_empty))
        return
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(messages, key = { it.id }) { message ->
            val type = when (message.type) {
                MessageType.I_AM_SAFE -> stringResource(R.string.button_safe)
                MessageType.SOS -> stringResource(R.string.button_sos)
            }
            val sender = if (message.senderId == deviceId) {
                stringResource(R.string.sender_you)
            } else {
                message.senderId.take(8)
            }
            Text(
                text = "$type · ${timeFormatter.format(message.createdAt)} · $sender",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeContentPreview() {
    UlakTheme {
        HomeContent(
            deviceId = "preview",
            isCommunicationOn = true,
            peerCount = 2,
            messages = emptyList(),
            permissionNotice = null,
            onCommunicationClick = {},
            onSafeClick = {},
            onSosClick = {},
            onOpenSettingsClick = {}
        )
    }
}
