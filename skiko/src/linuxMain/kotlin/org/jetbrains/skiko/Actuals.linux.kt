package org.jetbrains.skiko

import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UByteVarOf
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pin
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.gtk.GAsyncResult
import org.gtk.GCallback
import org.gtk.GCallbackVar
import org.gtk.GObject
import org.gtk.gdk_clipboard_read_text_async
import org.gtk.gdk_clipboard_read_text_finish
import org.gtk.gdk_clipboard_set_text
import org.gtk.gdk_display_get_default
import org.gtk.gdk_display_get_primary_clipboard
import org.gtk.gtk_uri_launcher_launch
import org.gtk.gtk_uri_launcher_launch_finish
import org.gtk.gtk_uri_launcher_new
import org.gtk.gtk_widget_get_cursor
import org.gtk.gtk_widget_set_cursor
import org.jetbrains.skiko.binding.NativeCancellable
import org.jetbrains.skiko.binding.NativeCursor
import org.jetbrains.skiko.binding.NativeWidget
import kotlin.coroutines.resume

actual fun URIHandler_openUri(uri: String) {
    val uriLauncher = gtk_uri_launcher_new(uri)
    val callback = staticCFunction { launcher: CPointer<GObject>?, res: CPointer<GAsyncResult>?, _: CPointer<*>? ->
        gtk_uri_launcher_launch_finish(launcher?.reinterpret(), res, null)
        Unit
    }
    gtk_uri_launcher_launch(uriLauncher, null, null, callback, null)
}

internal actual fun ClipboardManager_setText(text: String) {
    val display = gdk_display_get_default()
    val clipboard = gdk_display_get_primary_clipboard(display)
    gdk_clipboard_set_text(clipboard, text)
}

internal actual fun ClipboardManager_getText(): String? {
    val display = gdk_display_get_default()
    val clipboard = gdk_display_get_primary_clipboard(display)
    return runBlocking {
        suspendCancellableCoroutine { cont ->
            val contRef = StableRef.create(cont)
            val callback = staticCFunction { clipboard: CPointer<GObject>?, res: CPointer<GAsyncResult>?, data: CPointer<*>? ->
                val stable = data!!.asStableRef<CancellableContinuation<String?>>()
                val kCont = stable.get()
                try {
                    val cStr = gdk_clipboard_read_text_finish(clipboard?.reinterpret(), res, null)
                    val result = cStr?.toKString()
                    kCont.resume(result)
                } finally {
                    stable.dispose()
                }
            }
            cont.invokeOnCancellation {
                contRef.dispose()
            }
            gdk_clipboard_read_text_async(
                clipboard = clipboard,
                cancellable = NativeCancellable().apply {
                    onCancel { cont.cancel() }
                }.handle,
                callback = callback.reinterpret(),
                contRef.asCPointer()
            )
        }
    }
}

internal actual fun ClipboardManager_hasText(): Boolean = !ClipboardManager_getText().isNullOrEmpty()

actual typealias Cursor = NativeCursor

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    (component as NativeWidget).handle?.let {
        gtk_widget_set_cursor(it, cursor.handle)
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return (component as NativeWidget).handle?.let {
        gtk_widget_get_cursor(it)?.let {
            NativeCursor(it)
        }
    }
}

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> NativeCursor("default")
        PredefinedCursorsId.CROSSHAIR -> NativeCursor("crosshair")
        PredefinedCursorsId.HAND -> NativeCursor("pointer")
        PredefinedCursorsId.TEXT -> NativeCursor("text")
    }