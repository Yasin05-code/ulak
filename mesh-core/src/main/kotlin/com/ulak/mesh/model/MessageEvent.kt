package com.ulak.mesh.model

import java.time.Instant

data class MessageEvent(
    val messageId: String,
    val type: MessageEventType,
    val deviceId: String,
    val occurredAt: Instant
)