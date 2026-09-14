package org.jetbrains.skia.skottie

import org.jetbrains.skia.Point
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimationMakeFromStringIsolationTest {
    @Test
    fun makeFromStringEmptyAnimation() = runTest {
        assertMakeFromStringSucceeds(
            "empty-animation",
            """{"nm":"empty-animation","v":"1.42.0","ip":0,"op":180,"fr":60,"w":32,"h":32,"layers":[]}"""
        )
    }

    @Test
    fun makeFromStringNullLayer() = runTest {
        assertMakeFromStringSucceeds(
            "null-layer",
            """
            {
              "nm": "null-layer",
              "v": "1.42.0",
              "ip": 0,
              "op": 180,
              "fr": 60,
              "w": 32,
              "h": 32,
              "layers": [
                {
                  "ddd": 0,
                  "ind": 1,
                  "ty": 3,
                  "nm": "null",
                  "sr": 1,
                  "ks": {
                    "o": { "a": 0, "k": 100 },
                    "r": { "a": 0, "k": 0 },
                    "p": { "a": 0, "k": [16, 16, 0] },
                    "a": { "a": 0, "k": [0, 0, 0] },
                    "s": { "a": 0, "k": [100, 100, 100] }
                  },
                  "ao": 0,
                  "ip": 0,
                  "op": 180,
                  "st": 0,
                  "bm": 0
                }
              ]
            }
            """
        )
    }

    @Test
    fun makeFromStringSolidLayer() = runTest {
        assertMakeFromStringSucceeds(
            "solid-layer",
            """
            {
              "nm": "solid-layer",
              "v": "5.7.4",
              "ip": 0,
              "op": 90,
              "fr": 30,
              "w": 32,
              "h": 32,
              "layers": [
                {
                  "ddd": 0,
                  "ind": 1,
                  "ty": 1,
                  "nm": "solid",
                  "sr": 1,
                  "ks": {
                    "o": { "a": 0, "k": 100 },
                    "r": { "a": 0, "k": 0 },
                    "p": { "a": 0, "k": [16, 16, 0] },
                    "a": { "a": 0, "k": [0, 0, 0] },
                    "s": { "a": 0, "k": [100, 100, 100] }
                  },
                  "ao": 0,
                  "sw": 16,
                  "sh": 16,
                  "sc": "#2b6ff0",
                  "ip": 0,
                  "op": 90,
                  "st": 0,
                  "bm": 0
                }
              ]
            }
            """,
            expectedVersion = "5.7.4",
            expectedFps = 30.0f,
            expectedDuration = 3.0f,
            expectedOutPoint = 90.0f
        )
    }

    @Test
    fun makeFromStringStaticShapeLayer() = runTest {
        assertMakeFromStringSucceeds(
            "static-shape-layer",
            staticShapeLayerJson(animatedTransform = false),
            expectedVersion = "5.7.4",
            expectedFps = 30.0f,
            expectedDuration = 3.0f,
            expectedOutPoint = 90.0f
        )
    }

    @Test
    fun makeFromStringAnimatedTransformShapeLayer() = runTest {
        assertMakeFromStringSucceeds(
            "animated-transform-shape-layer",
            staticShapeLayerJson(animatedTransform = true),
            expectedVersion = "5.7.4",
            expectedFps = 30.0f,
            expectedDuration = 3.0f,
            expectedOutPoint = 90.0f
        )
    }

    private fun assertMakeFromStringSucceeds(
        name: String,
        json: String,
        expectedVersion: String = "1.42.0",
        expectedFps: Float = 60.0f,
        expectedDuration: Float = 3.0f,
        expectedOutPoint: Float = 180.0f
    ) {
        val compactJson = json.compactJson()
        println("skottie-makeFromString-isolation: before $name bytes=${compactJson.length}")
        val animation = Animation.makeFromString(compactJson)
        println("skottie-makeFromString-isolation: after $name")

        assertEquals(expectedVersion, animation.version, name)
        assertEquals(Point(32.0f, 32.0f), animation.size, name)
        assertEquals(expectedFps, animation.fPS, name)
        assertEquals(expectedDuration, animation.duration, name)
        assertEquals(0.0f, animation.inPoint, name)
        assertEquals(expectedOutPoint, animation.outPoint, name)
    }

    private fun staticShapeLayerJson(animatedTransform: Boolean): String {
        val position = if (animatedTransform) {
            """
            {
              "a": 1,
              "k": [
                { "t": 0, "s": [8, 16, 0], "e": [24, 16, 0], "i": [0.833, 1], "o": [0.167, 0] },
                { "t": 45, "s": [24, 16, 0], "e": [8, 16, 0], "i": [0.833, 1], "o": [0.167, 0] },
                { "t": 90, "s": [8, 16, 0] }
              ]
            }
            """
        } else {
            """{ "a": 0, "k": [16, 16, 0] }"""
        }

        return """
        {
          "nm": "shape-layer",
          "v": "5.7.4",
          "ip": 0,
          "op": 90,
          "fr": 30,
          "w": 32,
          "h": 32,
          "layers": [
            {
              "ddd": 0,
              "ind": 1,
              "ty": 4,
              "nm": "shape",
              "sr": 1,
              "ks": {
                "o": { "a": 0, "k": 100 },
                "r": { "a": 0, "k": 0 },
                "p": $position,
                "a": { "a": 0, "k": [0, 0, 0] },
                "s": { "a": 0, "k": [100, 100, 100] }
              },
              "ao": 0,
              "shapes": [
                {
                  "ty": "gr",
                  "it": [
                    { "ty": "el", "p": { "a": 0, "k": [0, 0] }, "s": { "a": 0, "k": [12, 12] }, "nm": "ellipse" },
                    { "ty": "fl", "c": { "a": 0, "k": [0.15, 0.43, 0.95, 1] }, "o": { "a": 0, "k": 100 }, "r": 1, "nm": "fill" },
                    { "ty": "tr", "p": { "a": 0, "k": [0, 0] }, "a": { "a": 0, "k": [0, 0] }, "s": { "a": 0, "k": [100, 100] }, "r": { "a": 0, "k": 0 }, "o": { "a": 0, "k": 100 } }
                  ],
                  "nm": "dot"
                }
              ],
              "ip": 0,
              "op": 90,
              "st": 0,
              "bm": 0
            }
          ]
        }
        """
    }

    private fun String.compactJson(): String {
        val compacted = StringBuilder(length)
        var inString = false
        var escaping = false

        for (char in this) {
            if (inString) {
                compacted.append(char)
                when {
                    escaping -> escaping = false
                    char == '\\' -> escaping = true
                    char == '"' -> inString = false
                }
            } else {
                when {
                    char == '"' -> {
                        compacted.append(char)
                        inString = true
                    }

                    !char.isWhitespace() -> compacted.append(char)
                }
            }
        }

        return compacted.toString()
    }
}
