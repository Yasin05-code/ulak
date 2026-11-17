package com.ulak.app

import android.app.Application
import com.ulak.app.mesh.DeviceId
import com.ulak.app.mesh.MessageCenter
import com.ulak.app.nearby.NearbyTransport

/**
 * Holds objects that must live as long as the app process,
 * so the screen and the background service share the same mesh node.
 */
class UlakApplication : Application() {

    lateinit var messageCenter: MessageCenter
        private set

    override fun onCreate() {
        super.onCreate()
        val deviceId = DeviceId.get(this)
        val transport = NearbyTransport(this, localName = deviceId.take(8))
        messageCenter = MessageCenter(deviceId, transport)
    }
}
