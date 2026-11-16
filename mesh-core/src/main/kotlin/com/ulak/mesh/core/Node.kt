
package com.ulak.mesh.core

import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import java.time.Clock
import java.time.Duration
import java.time.Instant

enum class MessageReceiveResult {
    STORED,
    DUPLICATE,
    EXPIRED
}

class Node(
    val id: String,
    private val clock: Clock = Clock.systemUTC(),
    private val messageFactory: MessageFactory = MessageFactory(clock)
) {

    private val storedMessages = mutableMapOf<String, Message>()

    init {
        require(id.isNotBlank()) {
            "Node ID cannot be blank"
        }
    }

    fun createMessage(
        type: MessageType,
        payload: String,
        recipientId: String? = null,
        ttl: Duration = Duration.ofHours(1)
    ): Message {

        val message = messageFactory.create(
            senderId = id,
            type = type,
            payload = payload,
            recipientId = recipientId,
            ttl = ttl
        )

        storedMessages[message.id] = message

        return message
    }

    fun receive(message: Message): MessageReceiveResult {

        val now = Instant.now(clock)

        if (!now.isBefore(message.expiresAt)) {
            return MessageReceiveResult.EXPIRED
        }

        if (storedMessages.containsKey(message.id)) {
            return MessageReceiveResult.DUPLICATE
        }

        storedMessages[message.id] = message

        return MessageReceiveResult.STORED
    }

    fun messagesToRelay(): List<Message> {
        val now = Instant.now(clock)

        return storedMessages.values.filter {
            now.isBefore(it.expiresAt)
        }
    }

    fun hasMessage(messageId: String): Boolean {
        return storedMessages.containsKey(messageId)
    }

    fun storedMessageCount(): Int {
        return storedMessages.size
    }
}
