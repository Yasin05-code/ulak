package com.ulak.app

import android.app.Application
import com.ulak.app.mesh.DeviceId
import com.ulak.app.mesh.MessageCenter

/**
 * Holds objects that must live as long as the app process,
 * so the screen and (later) the background service share the same mesh node.
 */
class UlakApplication : Application() {

    lateinit var messageCenter: MessageCenter
        private set

    override fun onCreate() {
        super.onCreate()
        messageCenter = MessageCenter(deviceId = DeviceId.get(this))
    }
}
