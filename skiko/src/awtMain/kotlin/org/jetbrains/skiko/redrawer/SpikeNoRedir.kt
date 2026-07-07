package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library

// SPIKE (milestone 1): arm a one-shot IAT hook so the next top-level window AWT creates gets
// WS_EX_NOREDIRECTIONBITMAP. Call between the JFrame constructor and the first pack()/setVisible(true):
// the peer HWND is realized lazily in addNotify(), and the flag is creation-only. Throwaway.
internal object SpikeNoRedir {
    init {
        Library.load()
    }

    external fun arm()
}
