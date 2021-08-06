package org.jetbrains.skiko.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment

fun swingTest(block: suspend CoroutineScope.() -> Unit) {
    assumeFalse(GraphicsEnvironment.isHeadless())
    assumeTrue(System.getProperty("skiko.test.window.test.enabled") != "false")

    runBlocking(Dispatchers.Swing) {
        block()
    }
}