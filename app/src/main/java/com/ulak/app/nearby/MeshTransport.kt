package com.ulak.app.nearby

import com.ulak.mesh.model.Message
import kotlinx.coroutines.flow.StateFlow

/**
 * Moves messages between phones. It knows nothing about mesh rules
 * (duplicates, expiry); it only finds peers and carries bytes.
 * NearbyTransport is the real implementation; a fake one can be used in tests.
 */
interface MeshTransport {

    /** IDs of phones we are connected to right now. */
    val connectedPeers: StateFlow<Set<String>>

    /** Called for every message that arrives from another phone. */
    var onMessageReceived: ((Message) -> Unit)?

    /** Called when a new phone connects, so we can sync stored messages to it. */
    var onPeerConnected: ((peerId: String) -> Unit)?

    /** Start advertising this phone and looking for others. */
    fun startDiscovery()

    /** Stop looking for phones and drop all connections. */
    fun stopDiscovery()

    /** Send to one peer, or to every connected peer when [peerId] is null. */
    fun sendMessage(message: Message, peerId: String? = null)
}
