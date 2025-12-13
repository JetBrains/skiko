package org.jetbrains.skiko.binding

import kotlinx.cinterop.CPointer
import org.gtk.GdkCursor
import org.gtk.gdk_cursor_new_from_name

class NativeCursor(
    handle: CPointer<GdkCursor>?
) : CHandled<GdkCursor>(handle) {

    constructor(name: String) : this(gdk_cursor_new_from_name(name, null))
}