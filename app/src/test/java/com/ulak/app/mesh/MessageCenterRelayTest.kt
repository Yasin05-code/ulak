package com.ulak.app.mesh

import com.ulak.app.nearby.FakeTransport
import com.ulak.mesh.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Three phones in a line: A - B - C. A and C never see each other directly. */
@RunWith(RobolectricTestRunner::class)
class MessageCenterRelayTest {

    private val transportA = FakeTransport("A")
    private val transportB = FakeTransport("B")
    private val transportC = FakeTransport("C")

    private val phoneA = MessageCenter("phone-a", transportA)
    private val phoneB = MessageCenter("phone-b", transportB)
    private val phoneC = MessageCenter("phone-c", transportC)

    @Test
    fun messageTravelsThroughMiddlePhone() {
        FakeTransport.connect(transportA, transportB)
        FakeTransport.connect(transportB, transportC)

        phoneA.sendSos()

        val created = phoneA.messages.value.single()
        assertEquals(MessageType.SOS, created.type)
        assertEquals(listOf(created), phoneB.messages.value)
        assertEquals(listOf(created), phoneC.messages.value)
    }

    @Test
    fun storedMessagesAreSyncedToALaterPeer() {
        phoneA.sendSafe()
        assertEquals(0, phoneB.messages.value.size)

        FakeTransport.connect(transportA, transportB)

        assertEquals(phoneA.messages.value, phoneB.messages.value)
    }

    @Test
    fun duplicatesAreNotStoredOrRelayedAgain() {
        FakeTransport.connect(transportA, transportB)
        FakeTransport.connect(transportB, transportC)
        FakeTransport.connect(transportA, transportC)

        phoneA.sendSafe()

        // Every phone keeps exactly one copy even though the triangle sends it twice.
        assertEquals(1, phoneA.messages.value.size)
        assertEquals(1, phoneB.messages.value.size)
        assertEquals(1, phoneC.messages.value.size)
    }
}
