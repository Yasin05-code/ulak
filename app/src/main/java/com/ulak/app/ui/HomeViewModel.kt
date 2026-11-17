package com.ulak.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ulak.app.UlakApplication
import com.ulak.mesh.model.Message
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val messageCenter = (application as UlakApplication).messageCenter

    val lastMessage: StateFlow<Message?> = messageCenter.lastMessage

    fun onSafeClick() = messageCenter.sendSafe()

    fun onSosClick() = messageCenter.sendSos()
}
