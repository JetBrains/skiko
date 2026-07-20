package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library

// Arms a one-shot inline hook on user32!CreateWindowExW so the next top-level window AWT creates gets
// WS_EX_NOREDIRECTIONBITMAP. That flag is required for the Direct3D synchronous live-resize overlay: with no
// redirection bitmap, the frame's DirectComposition visual IS its content, so there is no redirection surface
// to race the resize. Call between the JFrame constructor and the first pack()/setVisible(true) — the peer
// HWND is realized lazily in addNotify(), and the flag is creation-only.
// TODO: replace this process-wide inline hook with a supported mechanism for creating the frame NOREDIR.
internal object NoRedirectionBitmap {
    init {
        Library.load()
    }

    external fun arm()
}
