package com.ulak.app.mesh

import com.ulak.mesh.core.Node
import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thin bridge between the app and the mesh-core Node.
 * All mesh rules (IDs, TTL, duplicates) stay in mesh-core; this class only
 * calls the Node and tells the UI that something changed.
 */
class MessageCenter(deviceId: String) {

    private val node = Node(id = deviceId)

    private val _lastMessage = MutableStateFlow<Message?>(null)
    val lastMessage: StateFlow<Message?> = _lastMessage.asStateFlow()

    fun sendSafe() = create(MessageType.I_AM_SAFE, payload = "I'm Safe")

    fun sendSos() = create(MessageType.SOS, payload = "SOS")

    private fun create(type: MessageType, payload: String) {
        // recipientId = null means "for everyone" in mesh-core.
        _lastMessage.value = node.createMessage(type = type, payload = payload)
    }
}
