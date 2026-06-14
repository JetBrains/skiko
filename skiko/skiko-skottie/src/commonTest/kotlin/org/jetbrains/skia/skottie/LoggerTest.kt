package org.jetbrains.skia.skottie

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoggerTest {
    @Test
    fun canLog() {
        var actualLevel: LogLevel? = null
        var actualMessage: String? = null
        var actualJson: String? = null

        val logger = object: Logger() {
            override fun log(level: LogLevel, message: String, json: String?) {
                actualLevel = level
                actualMessage = message
                actualJson = json
            }
        }

        try {
            AnimationBuilder().setLogger(logger).buildFromString(
                """{"nm": "Test","v": "1.42.0","ip": <ERROR HERE>,"op": 180,"fr": 60,"w": 32,"h": 32,"layers": []}"""
            )
        } catch (e: IllegalArgumentException) {
            // Must fail
        }

        assertEquals("Failed to parse JSON input.\n", actualMessage)
        assertEquals(LogLevel.ERROR, actualLevel)
        assertNull(actualJson)
    }
}