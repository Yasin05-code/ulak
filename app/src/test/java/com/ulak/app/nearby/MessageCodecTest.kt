package com.ulak.app.nearby

import com.ulak.mesh.model.Message
import com.ulak.mesh.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class MessageCodecTest {

    @Test
    fun encodeThenDecodeGivesSameMessage() {
        val message = Message(
            id = "id-1",
            senderId = "phone-a",
            recipientId = null,
            type = MessageType.SOS,
            payload = "SOS",
            createdAt = Instant.parse("2026-11-17T10:00:00Z"),
            expiresAt = Instant.parse("2026-11-17T11:00:00Z")
        )

        assertEquals(message, MessageCodec.decode(MessageCodec.encode(message)))
    }

    @Test
    fun invalidBytesAreIgnored() {
        assertNull(MessageCodec.decode("not json".toByteArray()))
    }
}
