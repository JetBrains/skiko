package org.jetbrains.skiko.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkikoProperties
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment

fun uiTest(block: suspend CoroutineScope.() -> Unit) {
    assumeFalse(GraphicsEnvironment.isHeadless())
    assumeTrue(System.getProperty("skiko.test.ui.enabled", "false") == "true")

    runBlocking(Dispatchers.Swing) {
        if (System.getProperty("skiko.test.ui.allRenderApi", "true") == "true") {
            SkikoProperties.fallbackRenderApiQueue.forEach {
                withRenderApi(it) {
                    println("Testing $it renderApi")
                    println()
                    block()
                }
            }
        } else {
            block()
        }
    }
}

private inline fun withRenderApi(renderApi: GraphicsApi, block: () -> Unit) {
    val oldValue = System.getProperty("skiko.renderApi")
    try {
        System.setProperty("skiko.renderApi", renderApi.toString())
        block()
    } finally {
        System.setProperty("skiko.renderApi", oldValue.orEmpty())
    }
}