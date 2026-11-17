package com.ulak.app.nearby

import com.ulak.mesh.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory MeshTransport for tests. Messages still go through MessageCodec,
 * so the JSON format is tested too.
 */
class FakeTransport(val name: String) : MeshTransport {

    private val links = mutableMapOf<String, FakeTransport>()

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    override val connectedPeers: StateFlow<Set<String>> = _connectedPeers

    override var onMessageReceived: ((Message) -> Unit)? = null
    override var onPeerConnected: ((peerId: String) -> Unit)? = null

    var sentCount = 0
        private set

    override fun startDiscovery() = Unit

    override fun stopDiscovery() = Unit

    override fun sendMessage(message: Message, peerId: String?) {
        val targets = if (peerId != null) listOfNotNull(links[peerId]) else links.values.toList()
        targets.forEach { target ->
            sentCount++
            val decoded = requireNotNull(MessageCodec.decode(MessageCodec.encode(message)))
            target.onMessageReceived?.invoke(decoded)
        }
    }

    companion object {
        fun connect(a: FakeTransport, b: FakeTransport) {
            a.links[b.name] = b
            b.links[a.name] = a
            a._connectedPeers.value = a.links.keys.toSet()
            b._connectedPeers.value = b.links.keys.toSet()
            a.onPeerConnected?.invoke(b.name)
            b.onPeerConnected?.invoke(a.name)
        }
    }
}
