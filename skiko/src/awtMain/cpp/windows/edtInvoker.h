#pragma once

// True while the CURRENT thread is blocked inside EdtInvoker.invokeAndWaitWhilePumping — i.e. it has posted a
// runnable to the EDT and is pump-waiting for it, servicing cross-thread SENT messages meanwhile. Any caller that
// dispatches those SENT messages (e.g. a subclassed WndProc that renders on the EDT) must consult this before
// starting ANOTHER EDT round-trip: doing so re-entrantly would post a runnable the blocked EDT can't reach yet,
// deadlocking. Thread-scoped, so it stays meaningful no matter which thread drives the invocation.
bool isPumpingEdt();
