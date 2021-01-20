package org.jetbrains.skiko

import java.util.*
import kotlin.math.roundToInt

internal class FPSCounter(
    private val count: Int,
    private val probability: Double
) {
    private var i = 0
    private val times = LinkedList<Double>()
    private var t1 = System.nanoTime()

    /**
     * [value] 0.0 - min, 1.0 - max, 0.5 - median
     */
    private fun MutableList<Double>.quantile(value: Double) : Double {
        val index = (value * (size - 1)).toInt()
        return sorted()[index]
    }

    fun tick() {
        val t2 = System.nanoTime()
        val frameTime = (t2 - t1) / 1E6
        t1 = t2

        i++
        times.add(frameTime)

        if (times.size > count) {
            times.removeFirst()
        }

        if (i % count == 0) {
            val quantile = (1 - probability) / 2.0
            val average = (1000.0 / times.average()).roundToInt()
            val min = (1000.0 / times.quantile(1 - quantile)).roundToInt()
            val max = (1000.0 / times.quantile(quantile)).roundToInt()
            val probability = (100 * probability).roundToInt()
            println("FPS $average ($min-$max $probability%)")
        }
    }
}