#pragma once

// True while the CURRENT thread is blocked inside EdtInvoker.invokeAndWaitWhilePumping — i.e. it has posted a
// runnable to the EDT and is spinning a nested message loop for it, dispatching this thread's messages meanwhile.
// A subclassed WndProc that renders on the EDT must consult this before doing ANY of its own synchronous work:
//   * starting another EDT round-trip re-entrantly would post a runnable the blocked EDT can't reach yet; and
//   * the nested loop re-dispatches this thread's own window messages (WM_NCCALCSIZE, WM_PAINT, ...), so a WndProc
//     that acts on them would re-enter its render mid-pump.
// So while this is true the live-resize WndProc goes inert and just chains to the original proc. Thread-scoped, so
// it stays meaningful no matter which thread drives the invocation.
bool isPumpingEdt();
