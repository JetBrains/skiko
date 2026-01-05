@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.posix.*

private fun shellEscapeSingleQuotes(value: String): String = value.replace("'", "'\"'\"'")

internal actual fun URIHandler_openUri(uri: String) {
    // Keep this dependency-free and best-effort. Prefer xdg-open; fall back to gio/open.
    val escapedUri = shellEscapeSingleQuotes(uri)
    val candidates = listOf(
        "xdg-open",
        "gio open",
        "gnome-open",
        "kde-open",
    )
    for (cmd in candidates) {
        val status = system("$cmd '$escapedUri' >/dev/null 2>&1")
        if (status == 0) return
    }
}

private fun pipeWrite(command: String, bytes: ByteArray): Boolean {
    val pipe = popen(command, "w") ?: return false
    val ok = bytes.isEmpty() || bytes.usePinned { pinned ->
        fwrite(pinned.addressOf(0), 1.convert(), bytes.size.convert(), pipe) == bytes.size.convert<size_t>()
    }
    val exitCode = pclose(pipe)
    return ok && exitCode == 0
}

private fun pipeRead(command: String): String? {
    val pipe = popen(command, "r") ?: return null
    val builder = ByteArrayBuilder()
    val buffer = ByteArray(8 * 1024)
    while (true) {
        val read = buffer.usePinned { pinned ->
            fread(pinned.addressOf(0), 1.convert(), buffer.size.convert(), pipe).toInt()
        }
        if (read <= 0) break
        builder.append(buffer, read)
    }
    val exitCode = pclose(pipe)
    if (exitCode != 0) return null
    return builder.toByteArray().decodeToString().takeIf { it.isNotEmpty() }
}

private class ByteArrayBuilder(initialCapacity: Int = 1024) {
    private var data = ByteArray(initialCapacity)
    private var size = 0

    fun append(source: ByteArray, count: Int) {
        if (count <= 0) return
        ensureCapacity(size + count)
        source.copyInto(data, destinationOffset = size, startIndex = 0, endIndex = count)
        size += count
    }

    fun toByteArray(): ByteArray = data.copyOf(size)

    private fun ensureCapacity(required: Int) {
        if (required <= data.size) return
        var newSize = data.size.coerceAtLeast(1)
        while (newSize < required) newSize *= 2
        data = data.copyOf(newSize)
    }
}

internal actual fun ClipboardManager_setText(text: String) {
    val bytes = text.encodeToByteArray()

    // Wayland (wl-clipboard), then X11 (xclip/xsel). Best-effort.
    if (pipeWrite("wl-copy", bytes)) return
    if (pipeWrite("xclip -selection clipboard -in", bytes)) return
    pipeWrite("xsel --clipboard --input", bytes)
}

internal actual fun ClipboardManager_getText(): String? {
    // Wayland (wl-clipboard), then X11 (xclip/xsel).
    return pipeRead("wl-paste --no-newline")
        ?: pipeRead("xclip -selection clipboard -out")
        ?: pipeRead("xsel --clipboard --output")
}

internal actual fun ClipboardManager_hasText(): Boolean = !ClipboardManager_getText().isNullOrEmpty()

actual typealias Cursor = Any

/**
 * Optional cursor hook for native Linux hosts. If the underlying component implements this interface,
 * [CursorManager] APIs will update/query its cursor.
 */
interface LinuxCursorHost {
    var cursor: Cursor?
}

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    (component as? LinuxCursorHost)?.cursor = cursor
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return (component as? LinuxCursorHost)?.cursor
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    id
