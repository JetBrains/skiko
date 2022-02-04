package org.jetbrains.skiko.notifications

sealed class NotificationError(override val message: String) : Throwable(message)

class PermissionNotGrantedError : NotificationError("Permission not granted for notification")
class NotificationsNotSupportedError : NotificationError("Notifications are not supported for this platform")