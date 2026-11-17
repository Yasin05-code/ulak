package com.ulak.app.mesh

import android.util.Log
import com.ulak.app.nearby.MeshTransport
import com.ulak.mesh.core.MessageReceiveResult
import com.ulak.mesh.core.Node
import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bridge between Android and mesh-core.
 * - Mesh rules (IDs, expiry, duplicates) stay in mesh-core's Node.
 * - Moving bytes between phones stays in MeshTransport.
 * This class only connects the two and tells the UI what changed.
 */
class MessageCenter(
    val deviceId: String,
    private val transport: MeshTransport
) {

    private val node = Node(id = deviceId)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    /** Every message this phone knows about, newest first. */
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    val connectedPeers: StateFlow<Set<String>> = transport.connectedPeers

    init {
        transport.onMessageReceived = ::onMessageFromPeer
        transport.onPeerConnected = ::syncTo
    }

    fun startMesh() = transport.startDiscovery()

    fun stopMesh() = transport.stopDiscovery()

    fun sendSafe() = createAndSend(MessageType.I_AM_SAFE, payload = "I'm Safe")

    fun sendSos() = createAndSend(MessageType.SOS, payload = "SOS")

    private fun createAndSend(type: MessageType, payload: String) {
        // recipientId = null means "for everyone" in mesh-core.
        val message = node.createMessage(type = type, payload = payload)
        addToList(message)
        transport.sendMessage(message)
    }

    /** Hands an incoming message to mesh-core; only new, valid messages are kept and passed on. */
    private fun onMessageFromPeer(message: Message) {
        when (node.receive(message)) {
            MessageReceiveResult.STORED -> {
                addToList(message)
                // Epidemic routing: pass it to everyone else. Duplicate checks stop loops.
                transport.sendMessage(message)
            }
            MessageReceiveResult.DUPLICATE -> Log.d(TAG, "Duplicate ${message.id}")
            MessageReceiveResult.EXPIRED -> Log.d(TAG, "Expired ${message.id}")
        }
    }

    /** A new phone joined: give it everything we still carry. */
    private fun syncTo(peerId: String) {
        node.messagesToRelay().forEach { transport.sendMessage(it, peerId) }
    }

    private fun addToList(message: Message) {
        _messages.value = (listOf(message) + _messages.value).sortedByDescending { it.createdAt }
    }

    private companion object {
        const val TAG = "MessageCenter"
    }
}
