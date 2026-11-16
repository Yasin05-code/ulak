package com.ulak.mesh.repository

import com.ulak.mesh.model.Message

class InMemoryMessageRepository : MessageRepository {

    private val messages = mutableMapOf<String, Message>()

    override fun save(message: Message) {
        messages[message.id] = message
    }

    override fun exists(messageId: String): Boolean {
        return messages.containsKey(messageId)
    }

    override fun getAll(): List<Message> {
        return messages.values.toList()
    }

    override fun count(): Int {
        return messages.size
    }
}