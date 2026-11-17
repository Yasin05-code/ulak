package com.ulak.app.nearby

import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import org.json.JSONObject
import java.time.Instant

/**
 * Turns a mesh-core Message into bytes and back.
 * TEMPORARY wire format: field names follow mesh-core's Message class.
 * Agree on the final format with the mesh-core owner before release.
 */
object MessageCodec {

    fun encode(message: Message): ByteArray {
        val json = JSONObject()
            .put("id", message.id)
            .put("senderId", message.senderId)
            .put("recipientId", message.recipientId ?: JSONObject.NULL)
            .put("type", message.type.name)
            .put("payload", message.payload)
            .put("createdAt", message.createdAt.toString())
            .put("expiresAt", message.expiresAt.toString())
        return json.toString().toByteArray(Charsets.UTF_8)
    }

    /** Returns null for bytes that are not a valid message, so bad input never crashes the app. */
    fun decode(bytes: ByteArray): Message? = try {
        val json = JSONObject(String(bytes, Charsets.UTF_8))
        Message(
            id = json.getString("id"),
            senderId = json.getString("senderId"),
            recipientId = if (json.isNull("recipientId")) null else json.getString("recipientId"),
            type = MessageType.valueOf(json.getString("type")),
            payload = json.getString("payload"),
            createdAt = Instant.parse(json.getString("createdAt")),
            expiresAt = Instant.parse(json.getString("expiresAt"))
        )
    } catch (e: Exception) {
        null
    }
}
