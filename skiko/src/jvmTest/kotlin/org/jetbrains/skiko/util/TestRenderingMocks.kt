package org.jetbrains.skiko.util

import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.testNonSoftwareContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.testNonSoftwareRedrawer
import java.lang.IllegalStateException

fun withFailingRenderingMocksOnInitContext(action: () -> Unit) {
    try {
        testNonSoftwareContextHandler = { layer ->
            object : ContextHandler(layer) {
                override fun initContext() = false
                override fun initCanvas() = Unit
            }
        }
        testNonSoftwareRedrawer = {
            object : Redrawer {
                override fun dispose() = Unit
                override fun needRedraw() = Unit
                override fun redrawImmediately() = Unit
            }
        }
        action()
    } finally {
        testNonSoftwareContextHandler = null
        testNonSoftwareRedrawer = null
    }
}

fun withFailingRenderingMocksOnRedrawerCreate(action: () -> Unit) {
    try {
        testNonSoftwareContextHandler = { layer ->
            object : ContextHandler(layer) {
                override fun initContext() = true
                override fun initCanvas() = Unit
            }
        }
        testNonSoftwareRedrawer = {
            throw IllegalStateException()
        }
        action()
    } finally {
        testNonSoftwareContextHandler = null
        testNonSoftwareRedrawer = null
    }
}

fun withFailingRenderingMocksOnDraw(action: () -> Unit) {
    try {
        testNonSoftwareContextHandler = { layer ->
            object : ContextHandler(layer) {
                override fun initContext() = true
                override fun initCanvas() = Unit
            }
        }
        testNonSoftwareRedrawer = {
            object : Redrawer {
                override fun dispose() = Unit
                override fun needRedraw() = Unit
                override fun redrawImmediately() = throw IllegalStateException()
            }
        }
        action()
    } finally {
        testNonSoftwareContextHandler = null
        testNonSoftwareRedrawer = null
    }
}