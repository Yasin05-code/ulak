package com.ulak.mesh.model

import java.time.Instant

data class Message(
    val id: String,
    val senderId: String,
    val recipientId: String? = null,
    val type: MessageType,
    val payload: String,
    val createdAt: Instant,
    val expiresAt: Instant
)