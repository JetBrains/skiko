#pragma once

// True while the CURRENT thread is blocked inside WinApiEdtInvoker.invokeAndWaitWhilePumping. A WndProc that renders
// on the EDT must go inert while it holds: the nested pump re-dispatches this thread's own messages, so acting on
// them would re-enter the render, and a second round-trip cannot reach the already-blocked EDT anyway.
bool isPumpingEdt();
