package com.ulak.mesh.core

import com.ulak.mesh.model.MessageEvent
import com.ulak.mesh.model.MessageEventType
import java.time.Clock
import java.time.Instant

class MessageEventLog(
    private val clock: Clock = Clock.systemUTC()
) {

    private val events = mutableListOf<MessageEvent>()

    fun record(
        messageId: String,
        type: MessageEventType,
        deviceId: String
    ): MessageEvent {

        val event = MessageEvent(
            messageId = messageId,
            type = type,
            deviceId = deviceId,
            occurredAt = Instant.now(clock)
        )

        events.add(event)

        return event
    }

    fun eventsFor(messageId: String): List<MessageEvent> {
        return events
            .filter { it.messageId == messageId }
            .toList()
    }

    fun allEvents(): List<MessageEvent> {
        return events.toList()
    }
}