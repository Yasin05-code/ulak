
package com.ulak.mesh.core

import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

class MessageFactory(
    private val clock: Clock = Clock.systemUTC()
) {

    fun create(
        senderId: String,
        type: MessageType,
        payload: String,
        recipientId: String? = null,
        ttl: Duration = Duration.ofHours(1)
    ): Message {

        require(senderId.isNotBlank()) {
            "Sender ID cannot be blank"
        }

        require(payload.isNotBlank()) {
            "Message payload cannot be blank"
        }

        require(!ttl.isZero && !ttl.isNegative) {
            "TTL must be greater than zero"
        }

        val now = Instant.now(clock)

        return Message(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            recipientId = recipientId,
            type = type,
            payload = payload,
            createdAt = now,
            expiresAt = now.plus(ttl)
        )
    }
}
