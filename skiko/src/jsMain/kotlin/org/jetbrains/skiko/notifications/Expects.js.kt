package org.jetbrains.skiko.notifications

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.notifications.DENIED
import org.w3c.notifications.GRANTED
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission
import org.w3c.notifications.Notification as WebNotification

internal actual suspend fun sendNotification(notification: Notification) {
    if (window.asDynamic()["Notification"] == undefined) {
        throw NotificationsNotSupportedError()
    }

    val permission = when (WebNotification.permission) {
        NotificationPermission.GRANTED -> NotificationPermission.GRANTED
        NotificationPermission.DENIED -> NotificationPermission.DENIED
        else -> WebNotification.requestPermission().await()
    }

    if (permission == NotificationPermission.GRANTED) {
        WebNotification(notification.title, NotificationOptions(
            body = notification.body
        ))
    } else {
        throw PermissionNotGrantedError()
    }
}
