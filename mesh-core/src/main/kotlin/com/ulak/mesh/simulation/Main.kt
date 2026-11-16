package com.ulak.mesh.simulation

import com.ulak.mesh.core.Node
import com.ulak.mesh.core.RelayManager
import com.ulak.mesh.model.MessageType

fun main() {

    val nodeA = Node("PHONE_A")
    val nodeB = Node("PHONE_B")
    val nodeC = Node("PHONE_C")

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
}