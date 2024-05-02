package org.jetbrains.skiko

import kotlinx.coroutines.*
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.event.FocusEvent
import java.beans.PropertyChangeEvent
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext

/**
 * See [nativeInitializeAccessible] doc for details
 */
internal external fun initializeCAccessible(accessible: Accessible)

/**
 * A helper class for implementing requesting accessibility focus on a given accessible.
 */
internal class NativeAccessibleFocusHelper(
    private val component: Component,
    private val externalAccessible: Accessible?,
) {

    private var focusedAccessible: Accessible? = null

    val accessibleContext: AccessibleContext?
        get() = (focusedAccessible ?: externalAccessible)?.accessibleContext

    private var resetFocusAccessibleJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        focusedAccessible = accessible

        when (hostOs) {
            OS.Windows -> requestAccessBridgeFocusOnAccessible()
            OS.MacOS -> requestMacOSFocusOnAccessible(accessible)
            else -> {
                focusedAccessible = null
                return
            }
        }

        // Listener spawns asynchronous notification post procedure, reading current focus owner
        // and its accessibility context. This timeout is used to deal with concurrency
        // TODO Find more reliable procedure
        resetFocusAccessibleJob?.cancel()
        resetFocusAccessibleJob = GlobalScope.launch(MainUIDispatcher) {
            delay(100)
            focusedAccessible = null
        }
    }

    private fun requestAccessBridgeFocusOnAccessible() {
        val focusEvent = FocusEvent(component, FocusEvent.FOCUS_GAINED)
        component.focusListeners.forEach { it.focusGained(focusEvent) }
    }

    private fun requestMacOSFocusOnAccessible(accessible: Accessible?) {
        val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
        val listeners = focusManager.getPropertyChangeListeners("focusOwner")
        val event = PropertyChangeEvent(focusManager, "focusOwner", null, accessible)
        listeners.forEach { it.propertyChange(event) }
    }

    fun dispose() {
        resetFocusAccessibleJob?.cancel()
    }
}