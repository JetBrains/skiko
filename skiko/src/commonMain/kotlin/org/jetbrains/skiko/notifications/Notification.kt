package org.jetbrains.skiko.notifications

class Notification(var title: String, var body: String) {
    var iconPath: String? = null

    suspend fun send() = sendNotification(this)
}
