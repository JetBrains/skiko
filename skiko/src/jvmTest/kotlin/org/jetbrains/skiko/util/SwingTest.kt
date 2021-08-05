package org.jetbrains.skiko.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.junit.Assume.assumeFalse
import java.awt.GraphicsEnvironment

fun swingTest(block: suspend CoroutineScope.() -> Unit) {
    assumeFalse(GraphicsEnvironment.isHeadless())

    runBlocking(Dispatchers.Swing) {
        block()
    }
}