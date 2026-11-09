package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skiko.SkikoRenderDelegate

class SkottieExample : SkikoRenderDelegate {
    private val animation by lazy {
        Animation.makeFromString(
            """
                {
                  "v": "5.7.4",
                  "fr": 60,
                  "ip": 0,
                  "op": 60,
                  "w": 100,
                  "h": 100,
                  "nm": "Rotating rectangle",
                  "ddd": 0,
                  "assets": [],
                  "layers": [
                    {
                      "ddd": 0,
                      "ind": 1,
                      "ty": 4,
                      "nm": "Rectangle",
                      "sr": 1,
                      "ks": {
                        "o": { "a": 0, "k": 100 },
                        "r": {
                          "a": 1,
                          "k": [
                            { "i": { "x": [0.833], "y": [0.833] }, "o": { "x": [0.167], "y": [0.167] }, "t": 0, "s": [0] },
                            { "t": 60, "s": [360] }
                          ]
                        },
                        "p": { "a": 0, "k": [50, 50, 0] },
                        "a": { "a": 0, "k": [0, 0, 0] },
                        "s": { "a": 0, "k": [100, 100, 100] }
                      },
                      "ao": 0,
                      "shapes": [
                        { "ty": "rc", "d": 1, "s": { "a": 0, "k": [45, 45] }, "p": { "a": 0, "k": [0, 0] }, "r": { "a": 0, "k": 4 } },
                        { "ty": "fl", "c": { "a": 0, "k": [0.2, 0.5, 0.9, 1] }, "o": { "a": 0, "k": 100 }, "r": 1 },
                        { "ty": "tr", "p": { "a": 0, "k": [0, 0] }, "a": { "a": 0, "k": [0, 0] }, "s": { "a": 0, "k": [100, 100] }, "r": { "a": 0, "k": 0 }, "o": { "a": 0, "k": 100 } }
                      ],
                      "ip": 0,
                      "op": 60,
                      "st": 0,
                      "bm": 0
                    }
                  ]
                }
            """.trimIndent()
        )
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val duration = animation.duration.coerceAtLeast(1f)
        val elapsedSeconds = nanoTime / 1_000_000_000.0
        val progress = (elapsedSeconds % duration).toFloat() / duration
        animation.seek(progress)
        animation.render(canvas, Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()))
    }
}