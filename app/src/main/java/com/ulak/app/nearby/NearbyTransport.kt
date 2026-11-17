package com.ulak.app.nearby

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.ulak.mesh.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MeshTransport over Google Nearby Connections (Bluetooth + Wi-Fi).
 * Every phone advertises and discovers at the same time and accepts every
 * connection automatically: in a disaster there is nobody to tap "Accept".
 */
class NearbyTransport(
    context: Context,
    private val localName: String
) : MeshTransport {

    private val client = Nearby.getConnectionsClient(context.applicationContext)

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    override val connectedPeers: StateFlow<Set<String>> = _connectedPeers.asStateFlow()

    override var onMessageReceived: ((Message) -> Unit)? = null
    override var onPeerConnected: ((peerId: String) -> Unit)? = null

    private var running = false

    override fun startDiscovery() {
        if (running) return
        running = true

        client.startAdvertising(
            localName,
            SERVICE_ID,
            connectionCallback,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        )
            .addOnSuccessListener { Log.i(TAG, "Advertising started") }
            .addOnFailureListener { Log.w(TAG, "Advertising failed", it) }

        client.startDiscovery(
            SERVICE_ID,
            discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        )
            .addOnSuccessListener { Log.i(TAG, "Discovery started") }
            .addOnFailureListener { Log.w(TAG, "Discovery failed", it) }
    }

    override fun stopDiscovery() {
        if (!running) return
        running = false

        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
        _connectedPeers.value = emptySet()
        Log.i(TAG, "Nearby stopped")
    }

    override fun sendMessage(message: Message, peerId: String?) {
        val targets = if (peerId != null) listOf(peerId) else _connectedPeers.value.toList()
        if (targets.isEmpty()) return

        client.sendPayload(targets, Payload.fromBytes(MessageCodec.encode(message)))
            .addOnFailureListener { Log.w(TAG, "Send failed for ${message.id}", it) }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "Found $endpointId (${info.endpointName}), requesting connection")
            client.requestConnection(localName, endpointId, connectionCallback)
                .addOnFailureListener { Log.w(TAG, "Connection request to $endpointId failed", it) }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "Lost $endpointId")
        }
    }

    private val connectionCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                Log.i(TAG, "Connected to $endpointId")
                _connectedPeers.value = _connectedPeers.value + endpointId
                onPeerConnected?.invoke(endpointId)
            } else {
                Log.w(TAG, "Connection to $endpointId failed: ${resolution.status}")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "Disconnected from $endpointId")
            _connectedPeers.value = _connectedPeers.value - endpointId
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val message = MessageCodec.decode(bytes)
            if (message == null) {
                Log.w(TAG, "Ignored invalid payload from $endpointId")
                return
            }
            onMessageReceived?.invoke(message)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private companion object {
        const val TAG = "NearbyTransport"
        const val SERVICE_ID = "com.ulak.mesh"

        // Many-to-many connections: what a mesh needs.
        val STRATEGY: Strategy = Strategy.P2P_CLUSTER
    }
}
