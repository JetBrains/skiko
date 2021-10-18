package org.jetbrains.skiko

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import org.jetbrains.skia.*
import org.jetbrains.skiko.util.swingTest
import org.junit.Test
import java.awt.Point
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

@Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
class SkiaWindowPerfomanceTest {
    // TODO uncomment fontManager and fix native crash (Windows)
    /*
J 1680  org.jetbrains.skija.impl.Managed._nInvokeFinalizer(JJ)V (0 bytes) @ 0x0000027907c261f4 [0x0000027907c261a0+0x0000000000000054]
J 1674 c1 org.jetbrains.skija.impl.Managed$CleanerThunk.run()V (31 bytes) @ 0x0000027900aac2cc [0x0000027900aabbe0+0x00000000000006ec]
J 724 c1 jdk.internal.ref.CleanerImpl$PhantomCleanableRef.performCleanup()V java.base@14.0.2 (10 bytes) @ 0x000002790088b3ec [0x000002790088b2c0+0x000000000000012c]
J 723 c1 jdk.internal.ref.PhantomCleanable.clean()V java.base@14.0.2 (16 bytes) @ 0x000002790088b91c [0x000002790088b740+0x00000000000001dc]
j  jdk.internal.ref.CleanerImpl.run()V+77 java.base@14.0.2
j  java.lang.Thread.run()V+11 java.base@14.0.2
j  jdk.internal.misc.InnocuousThread.run()V+20 java.base@14.0.2
v  ~StubRoutines::call_stub
native crash in SkiaWindowTest "render single window"
     */
//    private val fontManager = FontMgr.getDefault()
//    private val fontCollection = FontCollection()
//        .setDefaultFontManager(fontManager)
//
//    private fun paragraph(size: Float, text: String) =
//        ParagraphBuilder(ParagraphStyle(), fontCollection)
//            .pushStyle(
//                TextStyle()
//                    .setColor(Color.RED.rgb)
//                    .setFontSize(size)
//            )
//            .addText(text)
//            .popStyle()
//            .build()

    private class TestWindow(
        width: Int,
        height: Int,
        private val frameCount: Int,
        private val deviatedTerminalCount: Int
    ) : SkiaWindow() {
        private val expectedDeviatePercent1 = 0.05
        private val expectedDeviatePercent2 = 0.15
        private val expectedDeviatePercent3 = 0.30
        private val expectedDeviatePercentTerminal = 0.50

        private val expectedFrameNanos get() = 1E9 / layer.backedLayer.getDisplayRefreshRate()
        private val frameTimes = mutableListOf<Long>()
        private var canCollect = false

        val frameTimeDeltas get() = frameTimes.zipWithNext { a, b -> b - a }

        val isCollected get() = frameTimes.size >= frameCount

        private fun deviated(deviatePercent: Double) = frameTimeDeltas.filter {
            abs(log2(it / expectedFrameNanos)) > log2(1 + deviatePercent)
        }

        init {
            setSize(width, height)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.skikoView = object : NoInputSkikoView() {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    if (canCollect && frameTimes.size < frameCount) {
                        frameTimes.add(System.nanoTime()) // we check the real time, not the time provided by the argument
                    }
                    layer.needRedraw()
                }
            }
            isUndecorated = true
            isVisible = true
        }

        fun startCollect() {
            canCollect = true
        }

        fun printInfo() {
            println("[Window frame times ($frameCount frames)]")
            val millis = frameTimeDeltas.map { it / 1E6 }
            println("Deltas " + millis.map { String.format("%.1f", it) })
            println("Average %.2f".format(millis.average()))
            println("Standard deviation %.2f".format(millis.stddev()))

            fun deviateMessage(percent: Double, deviated: List<Long>): String {
                val deviatedStr = deviated.map { String.format("%.1f", it / 1E6) }
                val percentTStr = (percent * 100).roundToInt()
                return "$deviatedStr deviate by $percentTStr%"
            }

            val deviated1 = deviated(expectedDeviatePercent1)
            val deviated2 = deviated(expectedDeviatePercent2)
            val deviated3 = deviated(expectedDeviatePercent3)
            val deviatedTerminal = deviated(expectedDeviatePercentTerminal)

            println(deviateMessage(expectedDeviatePercent1, deviated1 - deviated2 - deviated3 - deviatedTerminal))
            println(deviateMessage(expectedDeviatePercent2, deviated2 - deviated3 - deviatedTerminal))
            println(deviateMessage(expectedDeviatePercent3, deviated3 - deviatedTerminal))

            if (deviatedTerminal.size > deviatedTerminalCount) {
                throw AssertionError(deviateMessage(expectedDeviatePercentTerminal, deviatedTerminal))
            } else {
                println(deviateMessage(expectedDeviatePercentTerminal, deviatedTerminal))
            }

            println()
        }

        private fun List<Double>.stddev(): Double {
            val average = average()
            fun f(x: Double) = (x - average) * (x - average)
            return sqrt(map(::f).average())
        }
    }

    private suspend fun awaitFrameCollection(windows: List<TestWindow>) {
        while (!windows.all(TestWindow::isCollected)) {
            delay(100)
        }
    }

    @Test
    fun `FPS is near display refresh rate (multiple windows)`() = swingTest {
        val windows = (1..3).map { index ->
            TestWindow(width = 40, height = 20, frameCount = 300, deviatedTerminalCount = 20).apply {
                toFront()
                location = Point((index + 1) * 200, 200)
            }
        }
        delay(1000)
        try {
            windows.forEach(TestWindow::startCollect)
            awaitFrameCollection(windows)
            windows.forEach(TestWindow::printInfo)
        } finally {
            windows.forEach(TestWindow::close)
        }
    }

    // TODO fix native crash on macOs in previous test if this test is performed before it
    /*
Stack: [0x00007ffee09ff000,0x00007ffee11ff000],  sp=0x00007ffee11f9630,  free space=8169k
Native frames: (J=compiled Java code, A=aot compiled Java code, j=interpreted, Vv=VM code, C=native code)
C  [libskiko-macos-x64.dylib+0xe999b]  decltype(fp((SkRecords::NoOp)())) SkRecord::Record::visit<SkRecords::Draw&>(SkRecords::Draw&) const+0xb
C  [libskiko-macos-x64.dylib+0xe940f]  SkRecordDraw(SkRecord const&, SkCanvas*, SkPicture const* const*, SkDrawable* const*, int, SkBBoxHierarchy const*, SkPicture::AbortCallback*)+0x20f
C  [libskiko-macos-x64.dylib+0x188783]  SkBigPicture::playback(SkCanvas*, SkPicture::AbortCallback*) const+0xa3
C  [libskiko-macos-x64.dylib+0x65b4b]  SkCanvas::onDrawPicture(SkPicture const*, SkMatrix const*, SkPaint const*)+0x13b
C  [libskiko-macos-x64.dylib+0x659d6]  SkCanvas::drawPicture(SkPicture const*, SkMatrix const*, SkPaint const*)+0x146
C  [libskiko-macos-x64.dylib+0x169c4]  Java_org_jetbrains_skija_Canvas__1nDrawPicture+0x34
j  org.jetbrains.skija.Canvas._nDrawPicture(JJ[FJ)V+0
j  org.jetbrains.skija.Canvas.drawPicture(Lorg/jetbrains/skija/Picture;Lorg/jetbrains/skija/Matrix33;Lorg/jetbrains/skija/Paint;)Lorg/jetbrains/skija/Canvas;+27
j  org.jetbrains.skija.Canvas.drawPicture(Lorg/jetbrains/skija/Picture;)Lorg/jetbrains/skija/Canvas;+4
j  org.jetbrains.skiko.SkiaLayer.draw$skiko()V+229
j  org.jetbrains.skiko.redrawer.MacOsRedrawer$drawLayer$1.draw()V+7

     */
    //@Test
    fun `check FPS (elementary picture)`() = swingTest {
        // we don't count FPS straightforward because in window mode there is always vsync (we can get rid of it only in exclusive fullscreen mode)
        // FPS will be capped by display's refresh rate.
        //
        // So we want to reach the point when the FPS is much below refresh rate drawing very big amount of pictures avery frame.
        // When we reach that point, we can approximate FPS: FPS = 1000.0 / calculatedFrameTime * picturesPerFrame

        val frameCheckCount = 10
        val refreshRatePercent = 0.02 // We need to reach FPS = refreshRatePercent * refreshRate

        var picturesPerFrame = 1000
        val smoothFPSCounter = FPSCounter(count = frameCheckCount)
        val onComplete = CompletableDeferred<Unit>()

        fun renderer(window: SkiaWindow) = object : NoInputSkikoView() {
            var t1 = Long.MAX_VALUE
            val refreshRate = window.graphicsConfiguration.device.displayMode.refreshRate

            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                val t2 = System.nanoTime()
                val frameTime = (t2 - t1).coerceAtLeast(0)
                t1 = t2

                val currentFPS = 1E9 / frameTime
                val targetFPS = refreshRate * refreshRatePercent
                val smoothFPS = smoothFPSCounter.tick()

                if (currentFPS > targetFPS) {
                    picturesPerFrame *= 2
                }

                if (smoothFPS < targetFPS && smoothFPSCounter.isCountReached) {
                    onComplete.complete(Unit)
                }

                val random = Random(123)
                repeat(picturesPerFrame) {
                    canvas.save()
                    canvas.translate(width * random.nextFloat(), height * random.nextFloat())
                    canvas.drawTestPicture()
                    canvas.restore()
                }

                window.layer.needRedraw()
            }
        }

        val window = SkiaWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 400)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            window.layer.skikoView = renderer(window)
            window.layer.needRedraw()

            onComplete.await()

            val fps = (smoothFPSCounter.fps * picturesPerFrame).roundToInt()
            println("FPS is $fps")
            println("We draw $picturesPerFrame pictures per frame to avoid VSYNC lag")
        } finally {
            window.close()
        }
    }

    //private val paragraph by lazy { paragraph(10f, "Text") }

    fun Canvas.drawTestPicture() {
        save()
        clipRect(Rect(2f, 2f, 18f, 18f))

        drawRect(Rect(0f, 0f, 20f, 20f), Paint().apply {
            color = 0x88FF0000.toInt()
        })

        drawRRect(RRect.makeLTRB(0f, 0f, 20f, 20f, 4f), Paint().apply {
            mode = PaintMode.STROKE
            strokeWidth = 2f
            color = 0x8800FF00.toInt()
        })

        drawLine(0f, 0f, 10f, 10f, Paint().apply {
            color = 0x880000FF.toInt()
            isAntiAlias = true
        })

        //paragraph.layout(Float.POSITIVE_INFINITY)  // TODO fix native crash on Linux (free(): invalid pointer)
        //paragraph.paint(this, 0f, 0f)

        restore()
    }

    private class FPSCounter(
        private val count: Int
    ) {
        private val times = LinkedList<Long>()
        private var t1 = System.nanoTime()

        val isCountReached get() = times.size == count
        val fps get() = 1E9 / times.average()

        fun tick(): Double {
            val t2 = System.nanoTime()
            val frameTime = t2 - t1
            t1 = t2

            times.add(frameTime)

            if (times.size > count) {
                times.removeFirst()
            }

            return fps
        }
    }
}

private fun JFrame.close() = dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))