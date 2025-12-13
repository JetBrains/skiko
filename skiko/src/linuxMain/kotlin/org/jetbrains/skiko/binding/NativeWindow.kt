package org.jetbrains.skiko.binding

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import org.gtk.GtkWindow
import org.gtk.gtk_window_new

class NativeWindow(
    handle: CPointer<GtkWindow>? = gtk_window_new()?.reinterpret()
) : NativeWidget(handle?.reinterpret()) {
}