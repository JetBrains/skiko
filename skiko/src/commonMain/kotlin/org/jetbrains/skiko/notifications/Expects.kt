package org.jetbrains.skiko.notifications

internal expect suspend fun sendNotification(notification: Notification)