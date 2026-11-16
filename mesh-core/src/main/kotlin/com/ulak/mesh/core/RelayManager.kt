package com.ulak.mesh.core

data class RelayReport(
    val senderId: String,
    val receiverId: String,
    val attempted: Int,
    val stored: Int,
    val duplicates: Int,
    val expired: Int
)

class RelayManager {

    fun relay(
        sender: Node,
        receiver: Node
    ): RelayReport {

        require(sender.id != receiver.id) {
            "Sender and receiver cannot be the same node"
        }

        val messages = sender.messagesToRelay()

        var storedCount = 0
        var duplicateCount = 0
        var expiredCount = 0

        messages.forEach { message ->

            when (receiver.receive(message)) {

                MessageReceiveResult.STORED -> {
                    storedCount++
                }

                MessageReceiveResult.DUPLICATE -> {
                    duplicateCount++
                }

                MessageReceiveResult.EXPIRED -> {
                    expiredCount++
                }
            }
        }

        return RelayReport(
            senderId = sender.id,
            receiverId = receiver.id,
            attempted = messages.size,
            stored = storedCount,
            duplicates = duplicateCount,
            expired = expiredCount
        )
    }
}