package com.ulak.app.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Decides which runtime permissions Communication Mode needs on this Android version. */
object PermissionHelper {

    /** Without these, phones cannot find or talk to each other. */
    fun requiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += Manifest.permission.BLUETOOTH_SCAN
            permissions += Manifest.permission.BLUETOOTH_ADVERTISE
            permissions += Manifest.permission.BLUETOOTH_CONNECT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.NEARBY_WIFI_DEVICES
        } else {
            // Up to Android 12L, Bluetooth and Wi-Fi discovery only work with location.
            permissions += Manifest.permission.ACCESS_FINE_LOCATION
        }
        return permissions
    }

    /** Nice to have: the app still works if these are denied. */
    fun optionalPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // On Android 13+ location is only used for the SOS position (later phase).
            permissions += Manifest.permission.ACCESS_FINE_LOCATION
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        return permissions
    }

    /** Everything we ask for when the user taps Start Communication. */
    fun permissionsToRequest(): Array<String> {
        val all = requiredPermissions() + optionalPermissions()
        // FINE location is never asked alone; Android expects COARSE next to it.
        return (all + Manifest.permission.ACCESS_COARSE_LOCATION).distinct().toTypedArray()
    }

    fun missingRequired(context: Context): List<String> =
        requiredPermissions().filterNot { isGranted(context, it) }

    fun notificationsAllowed(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            isGranted(context, Manifest.permission.POST_NOTIFICATIONS)

    private fun isGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
