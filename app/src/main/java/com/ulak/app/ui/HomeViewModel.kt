package com.ulak.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ulak.app.UlakApplication
import com.ulak.app.permission.PermissionHelper
import com.ulak.app.service.CommunicationService
import com.ulak.mesh.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A message shown on screen after the permission dialog closes. */
enum class PermissionNotice {
    REQUIRED_DENIED,
    NOTIFICATIONS_DENIED
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val messageCenter = (application as UlakApplication).messageCenter

    val deviceId: String = messageCenter.deviceId

    val messages: StateFlow<List<Message>> = messageCenter.messages

    val connectedPeers: StateFlow<Set<String>> = messageCenter.connectedPeers

    val isCommunicationOn: StateFlow<Boolean> = CommunicationService.isRunning

    private val _permissionNotice = MutableStateFlow<PermissionNotice?>(null)
    val permissionNotice: StateFlow<PermissionNotice?> = _permissionNotice.asStateFlow()

    fun onSafeClick() = messageCenter.sendSafe()

    fun onSosClick() = messageCenter.sendSos()

    /** Called after the system permission dialog closes (or right away if all were granted). */
    fun onPermissionResult() {
        val app = getApplication<Application>()

        if (PermissionHelper.missingRequired(app).isNotEmpty()) {
            _permissionNotice.value = PermissionNotice.REQUIRED_DENIED
            return
        }

        _permissionNotice.value =
            if (PermissionHelper.notificationsAllowed(app)) null else PermissionNotice.NOTIFICATIONS_DENIED

        CommunicationService.start(app)
    }

    fun stopCommunication() {
        CommunicationService.stop(getApplication())
    }
}
