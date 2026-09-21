package org.jetbrains.skiko

import javax.accessibility.Accessible

/**
 * Calls `sun.lwawt.macosx.CAccessible.getCAccessible(Accessible)` on the given [accessible] object; does nothing
 * if running on a non-Mac platform or in a JVM where there is no `CAccessible` class.
 *
 * Ideally, this and nativeInitializeAccessible should be in Compose, not Skiko.
 * Unfortunately, Compose doesn't currently allow native code, and implementing it via reflection is not
 * possible due to java.desktop module access restrictions:
 * > class androidx.compose.ui.platform.a11y.AccessibilityKt cannot access class sun.lwawt.macosx.CAccessible
 * > (in module java.desktop) because module java.desktop does not export sun.lwawt.macosx to unnamed module
 *
 * As such, this function is not to be considered public Skiko API.
 */
external fun initializeCAccessible(accessible: Accessible)
