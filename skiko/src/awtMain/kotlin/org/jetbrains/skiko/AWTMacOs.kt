package org.jetbrains.skiko

import kotlinx.coroutines.Runnable
import java.awt.Canvas
import java.awt.Component
import kotlin.use

/**
 * Like [javax.swing.SwingUtilities.invokeAndWait], but keeps the AppKit run loop spinning while waiting, so
 * synchronous Java->AppKit calls made from [runnable] are serviced rather than deadlocking.
 * [component] provides the AWT context for the call.
 *
 * This is done via `LWCToolkit.invokeAndWait`. `LWCToolkit` lives in a non-exported JDK package, but JNI is not
 * subject to module access checks, so this needs no `--add-opens`.
 */
internal external fun macOsInvokeOnEventThreadAndWait(component: Component, runnable: Runnable)
