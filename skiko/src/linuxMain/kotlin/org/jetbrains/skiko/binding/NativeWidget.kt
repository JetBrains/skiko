package org.jetbrains.skiko.binding

import kotlinx.cinterop.CPointer
import org.gtk.GtkWidget

open class NativeWidget(
    handle: CPointer<GtkWidget>?
) : CHandled<GtkWidget>(handle) {
}