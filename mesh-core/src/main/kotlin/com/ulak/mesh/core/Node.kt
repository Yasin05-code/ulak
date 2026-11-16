package com.ulak.mesh.core

import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageEventType
import com.ulak.mesh.model.MessageType
import java.time.Clock
import java.time.Duration
import java.time.Instant
import com.ulak.mesh.repository.InMemoryMessageRepository
import com.ulak.mesh.repository.MessageRepository

enum class MessageReceiveResult {
    STORED,
    DUPLICATE,
    EXPIRED
}

class Node(
    val id: String,
    private val clock: Clock = Clock.systemUTC(),
    private val messageFactory: MessageFactory = MessageFactory(clock),
    private val eventLog: MessageEventLog = MessageEventLog(clock),
    private val messageRepository: MessageRepository = InMemoryMessageRepository()
) {

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

        messageRepository.save(message)

        eventLog.record(
            messageId = message.id,
            type = MessageEventType.CREATED,
            deviceId = id
        )

        return message
    }

    fun receive(message: Message): MessageReceiveResult {

        val now = Instant.now(clock)

        if (!now.isBefore(message.expiresAt)) {
            return MessageReceiveResult.EXPIRED
        }

        if (messageRepository.exists(message.id)) {
            return MessageReceiveResult.DUPLICATE
        }

        messageRepository.save(message)

        eventLog.record(
            messageId = message.id,
            type = MessageEventType.RELAY_RECEIVED,
            deviceId = id
        )

        return MessageReceiveResult.STORED
    }

    fun messagesToRelay(): List<Message> {
        val now = Instant.now(clock)

        return messageRepository.getAll().filter {
            now.isBefore(it.expiresAt)
        }
    }

    fun hasMessage(messageId: String): Boolean {
        return messageRepository.exists(messageId)
    }

    fun storedMessageCount(): Int {
        return messageRepository.count()
    }
}