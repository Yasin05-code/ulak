package com.ulak.mesh.repository

import com.ulak.mesh.model.Message

interface MessageRepository {

    fun save(message: Message)

    fun exists(messageId: String): Boolean

    fun getAll(): List<Message>

    fun count(): Int
}