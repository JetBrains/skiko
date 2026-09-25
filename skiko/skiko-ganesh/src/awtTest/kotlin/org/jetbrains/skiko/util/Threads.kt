package org.jetbrains.skiko.util

/**
 * Runs body in another thread, join it and throws a last exception in this thread if any
 */
fun joinThreadCatching(body: () -> Unit) {
    var exception: Throwable? = null
    val thread = object : Thread() {
        override fun run() = body()
    }
    thread.setUncaughtExceptionHandler { _, e ->
        exception = e
    }
    thread.start()
    thread.join()
    if (exception != null) {
        throw exception!!
    }
}