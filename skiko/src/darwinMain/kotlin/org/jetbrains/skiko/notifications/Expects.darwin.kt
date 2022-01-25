package org.jetbrains.skiko.notifications

import platform.Foundation.NSError
import platform.Foundation.NSUUID
import platform.UserNotifications.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_block_t
import platform.darwin.dispatch_get_main_queue
import kotlin.native.concurrent.freeze

internal actual suspend fun sendNotification(notification: Notification) {
    val nc = UNUserNotificationCenter.currentNotificationCenter().freeze()
    val sendNotification: dispatch_block_t = {
        val callback = { granted: Boolean, err: NSError? ->
            if (granted) {
                val content = UNMutableNotificationContent().apply {
                    setBody(notification.body)
                    setTitle(notification.title)
                }
                val id = NSUUID().UUIDString
                val request = UNNotificationRequest.requestWithIdentifier(id, content, null)
                val callback = { _: NSError? -> }
                nc.addNotificationRequest(request, callback.freeze())
            }
        }
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        nc.requestAuthorizationWithOptions(options, callback.freeze())
    }
    dispatch_async(dispatch_get_main_queue(), sendNotification.freeze())
}

