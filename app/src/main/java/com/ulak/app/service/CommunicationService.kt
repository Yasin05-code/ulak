package com.ulak.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.ulak.app.MainActivity
import com.ulak.app.R
import com.ulak.app.UlakApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Communication Mode: a foreground service of type connectedDevice.
 * It only starts when the user taps Start Communication.
 * While it runs, Nearby discovery is on and messages are relayed.
 */
class CommunicationService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Service types exist since Android 10 (API 29); older versions ignore them.
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)

        if (!_isRunning.value) {
            _isRunning.value = true
            messageCenter().startMesh()
            scope.launch { heartbeat() }
        }

        // If Android kills the service, do not restart it on its own:
        // Communication Mode should only run after a user action.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        messageCenter().stopMesh()
        scope.cancel()
        _isRunning.value = false
        Log.i(TAG, "Communication Mode stopped")
        super.onDestroy()
    }

    private fun messageCenter() = (application as UlakApplication).messageCenter

    private suspend fun heartbeat() {
        var tick = 0
        while (scope.isActive) {
            val peers = messageCenter().connectedPeers.value.size
            Log.i(TAG, "Communication Mode alive, tick ${tick++}, peers $peers")
            delay(HEARTBEAT_INTERVAL_MS)
        }
    }

    private fun buildNotification(): Notification {
        createChannel()

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this,
            1,
            Intent(this, CommunicationService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_relaying))
            .setContentIntent(openApp)
            .addAction(0, getString(R.string.button_stop_communication), stop)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "CommunicationService"
        private const val CHANNEL_ID = "communication_mode"
        private const val NOTIFICATION_ID = 1
        private const val HEARTBEAT_INTERVAL_MS = 10_000L
        private const val ACTION_STOP = "com.ulak.app.action.STOP_COMMUNICATION"

        private val _isRunning = MutableStateFlow(false)

        /** True while the service is alive. The home screen observes this. */
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, CommunicationService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CommunicationService::class.java))
        }
    }
}
