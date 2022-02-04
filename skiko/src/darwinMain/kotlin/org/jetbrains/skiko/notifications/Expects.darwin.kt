package org.jetbrains.skiko.notifications

import kotlinx.coroutines.withContext
import org.jetbrains.skiko.SkikoDispatchers
import platform.Foundation.NSError
import platform.Foundation.NSUUID
import platform.UserNotifications.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("NAME_SHADOWING")
internal actual suspend fun sendNotification(notification: Notification) = withContext(SkikoDispatchers.IO) {
    val nc = UNUserNotificationCenter.currentNotificationCenter()
    val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

    val sent = suspendCoroutine<Boolean> { cont ->
        nc.requestAuthorizationWithOptions(options) { granted: Boolean, err: NSError? ->
            if (granted) {
                with(notification) {
                    val request = UNNotificationRequest.requestWithIdentifier(id, content, null)
                    nc.addNotificationRequest(request) { err: NSError? ->
                        err?.let { println("Sent with error: $it") }
                        cont.resume(err == null)
                    }
                }
            } else {
                println("No auth: $err")
                cont.resume(false)
            }
        }
    }
    println("Sent: $sent")
}

private val Notification.content by lazy {
    UNMutableNotificationContent().apply {
        setBody(body)
        setTitle(title)
    }
}

private val Notification.id by lazy {
    NSUUID().UUIDString
}