package org.jetbrains.skiko

import kgfw.readClipboard
import kgfw.writeClipboard
import kotlinx.cinterop.*
import platform.posix.sprintf
import platform.posix.system
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
actual fun URIHandler_openUri(uri: String) {
    memScoped {
        val command = cValue<ByteVarOf<Byte>>()
        when (Platform.osFamily) {
            OsFamily.MACOSX ->
                sprintf(command, "open %s", uri.cstr)

            OsFamily.LINUX ->
                sprintf(command, "xdg-open %s", uri.cstr)

            OsFamily.WINDOWS ->
                sprintf(command, "start %s", uri.cstr)

            else -> {
                TODO("Not yet implemented for ${Platform.osFamily}")
            }
        }
        system(command.getBytes().toKString())
    }
}

internal actual fun ClipboardManager_setText(text: String) {
    writeClipboard(text)
}

internal actual fun ClipboardManager_getText(): String? = readClipboard()


internal actual fun ClipboardManager_hasText(): Boolean = !ClipboardManager_getText().isNullOrEmpty()

actual typealias Cursor = Any

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    TODO("Implement CursorManager_setCursor on Linux")
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    TODO("Implement CursorManager_getCursor on Linux")
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> Any()
        PredefinedCursorsId.CROSSHAIR -> Any()
        PredefinedCursorsId.HAND -> Any()
        PredefinedCursorsId.TEXT -> Any()
    }
