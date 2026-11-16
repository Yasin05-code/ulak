package com.ulak.mesh.simulation

import com.ulak.mesh.core.Node
import com.ulak.mesh.core.RelayManager
import com.ulak.mesh.model.MessageType
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import com.ulak.mesh.core.MessageEventLog

fun main() {

    val eventLog = MessageEventLog()

    val nodeA = Node(
        id = "PHONE_A",
        eventLog = eventLog
    )

    val nodeB = Node(
        id = "PHONE_B",
        eventLog = eventLog
    )

    val nodeC = Node(
        id = "PHONE_C",
        eventLog = eventLog
    )

    val relayManager = RelayManager()

    val message = nodeA.createMessage(
        type = MessageType.I_AM_SAFE,
        payload = "I'm Safe"
    )

    println("Message created by ${nodeA.id}")
    println("Message ID: ${message.id}")
    println()

    val firstRelay = relayManager.relay(
        sender = nodeA,
        receiver = nodeB
    )

    println("Relay: ${firstRelay.senderId} -> ${firstRelay.receiverId}")
    println("Attempted: ${firstRelay.attempted}")
    println("Stored: ${firstRelay.stored}")
    println("Duplicates: ${firstRelay.duplicates}")
    println()

    val secondRelay = relayManager.relay(
        sender = nodeB,
        receiver = nodeC
    )

    println("Relay: ${secondRelay.senderId} -> ${secondRelay.receiverId}")
    println("Attempted: ${secondRelay.attempted}")
    println("Stored: ${secondRelay.stored}")
    println("Duplicates: ${secondRelay.duplicates}")
    println()

    val duplicateRelay = relayManager.relay(
        sender = nodeB,
        receiver = nodeC
    )

    println("Relay again: ${duplicateRelay.senderId} -> ${duplicateRelay.receiverId}")
    println("Attempted: ${duplicateRelay.attempted}")
    println("Stored: ${duplicateRelay.stored}")
    println("Duplicates: ${duplicateRelay.duplicates}")

    println()
    println("Does PHONE_C have the original message?")
    println(nodeC.hasMessage(message.id))

    println()
    println("----- MESSAGE EVENT HISTORY -----")

    eventLog.eventsFor(message.id).forEach { event ->
        println(
            "${event.type} | ${event.deviceId} | ${event.occurredAt}"
        )
    }

    println()
    println("----- EXPIRY TEST -----")

    val baseTime = Instant.parse("2026-11-16T12:00:00Z")

    val senderClock = Clock.fixed(
        baseTime,
        ZoneOffset.UTC
    )

    val validReceiverClock = Clock.fixed(
        baseTime.plus(Duration.ofMinutes(30)),
        ZoneOffset.UTC
    )

    val expiredReceiverClock = Clock.fixed(
        baseTime.plus(Duration.ofHours(2)),
        ZoneOffset.UTC
    )

    val expirySender = Node(
        id = "EXPIRY_SENDER",
        clock = senderClock
    )

    val validReceiver = Node(
        id = "VALID_RECEIVER",
        clock = validReceiverClock
    )

    val expiredReceiver = Node(
        id = "EXPIRED_RECEIVER",
        clock = expiredReceiverClock
    )

    val expiringMessage = expirySender.createMessage(
        type = MessageType.SOS,
        payload = "Need help",
        ttl = Duration.ofHours(1)
    )

    println("Created at: ${expiringMessage.createdAt}")
    println("Expires at: ${expiringMessage.expiresAt}")

    val validResult = validReceiver.receive(expiringMessage)

    println()
    println("Received after 30 minutes:")
    println("Result: $validResult")

    val expiredResult = expiredReceiver.receive(expiringMessage)

    println()
    println("Received after 2 hours:")
    println("Result: $expiredResult")
}