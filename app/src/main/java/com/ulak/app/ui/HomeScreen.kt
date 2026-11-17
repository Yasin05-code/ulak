package com.ulak.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    val lastMessage by viewModel.lastMessage.collectAsState()
    val permissionNotice by viewModel.permissionNotice.collectAsState()
    val isCommunicationOn by viewModel.isCommunicationOn.collectAsState()
    val context = LocalContext.current

    // Opens the system permission dialog; Android skips it for permissions already granted.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.onPermissionResult() }

    HomeContent(
        lastMessage = lastMessage,
        permissionNotice = permissionNotice,
        isCommunicationOn = isCommunicationOn,
        onSafeClick = viewModel::onSafeClick,
        onSosClick = viewModel::onSosClick,
        onCommunicationClick = {
            if (isCommunicationOn) {
                viewModel.stopCommunication()
            } else {
                permissionLauncher.launch(PermissionHelper.permissionsToRequest())
            }
        },
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
    lastMessage: Message?,
    permissionNotice: PermissionNotice?,
    isCommunicationOn: Boolean,
    onSafeClick: () -> Unit,
    onSosClick: () -> Unit,
    onCommunicationClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(isCommunicationOn)

            if (permissionNotice != null) {
                PermissionNoticeCard(permissionNotice, onOpenSettingsClick)
            }

            // The two emergency buttons share all free space, so they stay large.
            ActionButton(
                text = stringResource(R.string.button_safe),
                color = SafeGreen,
                onClick = onSafeClick
            )
            ActionButton(
                text = stringResource(R.string.button_sos),
                color = SosRed,
                onClick = onSosClick
            )

            OutlinedButton(
                onClick = onCommunicationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val label = if (isCommunicationOn) {
                    R.string.button_stop_communication
                } else {
                    R.string.button_start_communication
                }
                Text(stringResource(label), fontSize = 18.sp)
            }

            LastMessageCard(lastMessage)
        }
    }
}

@Composable
private fun StatusCard(isOn: Boolean) {
    val colors = if (isOn) {
        CardDefaults.cardColors(containerColor = SafeGreen, contentColor = Color.White)
    } else {
        CardDefaults.cardColors()
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = colors) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.status_title),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(if (isOn) R.string.status_on else R.string.status_off),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(if (isOn) R.string.status_on_detail else R.string.status_off_detail),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PermissionNoticeCard(notice: PermissionNotice, onOpenSettingsClick: () -> Unit) {
    val text = when (notice) {
        PermissionNotice.REQUIRED_DENIED -> stringResource(R.string.permission_required_denied)
        PermissionNotice.NOTIFICATIONS_DENIED -> stringResource(R.string.permission_notifications_denied)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onOpenSettingsClick) {
                Text(stringResource(R.string.permission_open_settings))
            }
        }
    }
}

@Composable
private fun ColumnScope.ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)
    ) {
        Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

@Composable
private fun LastMessageCard(message: Message?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.last_message_title),
                style = MaterialTheme.typography.labelLarge
            )
            if (message == null) {
                Text(
                    text = stringResource(R.string.last_message_empty),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                val typeLabel = when (message.type) {
                    MessageType.I_AM_SAFE -> stringResource(R.string.button_safe)
                    MessageType.SOS -> stringResource(R.string.button_sos)
                }
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.last_message_detail,
                        timeFormatter.format(message.createdAt),
                        message.id.take(8)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    UlakTheme {
        HomeContent(
            lastMessage = null,
            permissionNotice = null,
            isCommunicationOn = false,
            onSafeClick = {},
            onSosClick = {},
            onCommunicationClick = {},
            onOpenSettingsClick = {}
        )
    }
}
