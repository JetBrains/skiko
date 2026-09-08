package org.jetbrains.skiko.swing

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test

class SkikoFramePacingServiceTest {

    private fun headfulService(): FramePacingService {
        assumeFalse(GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance)
        val service = SkikoFramePacingService.instance
        assertNotNull(service, "service must be available in a headful environment")
        return service
    }

    private fun defaultDisplayId(service: FramePacingService): Long {
        val gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .defaultConfiguration
        val displayId = service.displayId(gc)
        assertNotEquals(-1L, displayId, "default screen must resolve to a display id")
        return displayId
    }

    @Test
    fun `subscription receives ticks at roughly the display rate`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        val ticks = AtomicLong()
        val first = CountDownLatch(1)
        val subscription = service.subscribe(displayId) { _, _ ->
            ticks.incrementAndGet()
            first.countDown()
        }
        assertNotNull(subscription, "subscribe must succeed for the default display")

        try {
            assertTrue(first.await(2, TimeUnit.SECONDS), "no tick arrived within 2s")

            val before = ticks.get()
            Thread.sleep(500)
            val delivered = ticks.get() - before

            // Between ~10 Hz and 4x the fastest mainstream refresh: the assertion is about a
            // sane, continuous cadence, not about jitter quality.
            assertTrue(delivered in 5..500, "implausible tick count over 500ms: $delivered")
        } finally {
            subscription.close()
        }
    }

    @Test
    fun `closing the subscription stops the ticks`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        val ticks = AtomicLong()
        val subscription = service.subscribe(displayId) { _, _ -> ticks.incrementAndGet() }
        assertNotNull(subscription)

        awaitTicks(ticks)
        subscription.close()
        subscription.close() // idempotent

        // One in-flight delivery may complete after close; after a grace period the count
        // must be stable.
        Thread.sleep(100)
        val after = ticks.get()
        Thread.sleep(300)
        assertEquals(after, ticks.get(), "ticks kept arriving after close()")
    }

    @Test
    fun `a throwing listener does not starve its sibling`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        val siblingTicks = CountDownLatch(10)
        val throwing = service.subscribe(displayId) { _, _ -> error("deliberate test exception") }
        val sibling = service.subscribe(displayId) { _, _ -> siblingTicks.countDown() }
        assertNotNull(throwing)
        assertNotNull(sibling)

        try {
            assertTrue(
                siblingTicks.await(5, TimeUnit.SECONDS),
                "sibling listener starved by a throwing listener"
            )
        } finally {
            throwing.close()
            sibling.close()
        }
    }

    @Test
    fun `two subscriptions share one clock and survive each other`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        val firstTicks = AtomicLong()
        val secondTicks = AtomicLong()
        val first = service.subscribe(displayId) { _, _ -> firstTicks.incrementAndGet() }
        val second = service.subscribe(displayId) { _, _ -> secondTicks.incrementAndGet() }
        assertNotNull(first)
        assertNotNull(second)

        awaitTicks(firstTicks)
        awaitTicks(secondTicks)

        first.close()

        val before = secondTicks.get()
        awaitTicks(secondTicks, moreThan = before)

        second.close()
    }

    @Test
    fun `unknown display is refused`() {
        val service = headfulService()
        assertNull(service.subscribe(-1L) { _, _ -> })
        assertNull(service.subscribe(0x7FFF_FFFF_FFFFL) { _, _ -> })
    }

    @Test
    fun `refresh period is plausible for the default display`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)

        val period = service.refreshPeriodNanos(displayId)
        // 0 is allowed (unknown refresh rate); a known one must be between 20 and 500 Hz.
        if (period != 0L) {
            assertTrue(
                period in 2_000_000L..50_000_000L,
                "implausible refresh period: $period ns"
            )
        }
    }

    @Test
    fun `native display clock is used on macOS`() {
        assumeTrue(hostOs == OS.MacOS)
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        // The exploration's point on macOS is DISPLAY_LINK-grade ticks: the probe must accept
        // the default display, so the clock created for it is the CADisplayLink one.
        assertTrue(
            MacDisplayLinkClock.available(displayId),
            "display-link probe refused the default display"
        )
    }

    @Test
    fun `native vblank clock is used on Windows`() {
        assumeTrue(hostOs == OS.Windows)
        headfulService()
        Library.load()

        // The exploration's point on Windows is per-display vblank ticks: with a desktop
        // session and an attached output, DXGI must be usable, so the clock created is the
        // WaitForVBlank one (not the DWM or timer fallback).
        assertTrue(
            WinNativeClock.vblankAvailable(),
            "DXGI vblank probe found no desktop-attached output"
        )
    }

    @Test
    fun `native drm vblank clock is available on Linux`() {
        assumeTrue(hostOs == OS.Linux)
        headfulService()
        Library.load()

        // The exploration's point on Linux is kernel-vblank ticks: a real seat with DRM device access must
        // get them, not the timer. But a headful X11 session is not proof of one — CI's xvfb desktop runs
        // against a software renderer with no DRM nodes the runner may use, and there the service is
        // *supposed* to fall back to the timer — so the probe runs everywhere (it still fails the test if
        // the native path is broken) but only asserts where it can pass. This mirrors the guard in
        // `an available native backend is used instead of the timer`.
        assumeTrue(
            "DRM vblank probe found no accessible active CRTC: not a DRM-backed seat",
            LinuxDrmVBlankClock.available()
        )
    }

    @Test
    fun `the clock is recreated after the last subscription closes`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        val firstTicks = AtomicLong()
        val first = service.subscribe(displayId) { _, _ -> firstTicks.incrementAndGet() }
        assertNotNull(first)
        awaitTicks(firstTicks)
        first.close()

        // The registry entry went with it, so this builds a whole new clock instead of adding a
        // listener to a stopped one, which would never tick again.
        val secondTicks = AtomicLong()
        val second = service.subscribe(displayId) { _, _ -> secondTicks.incrementAndGet() }
        assertNotNull(second, "resubscribe after the last close must succeed")
        try {
            awaitTicks(secondTicks)
        } finally {
            second.close()
        }
    }

    @Test
    fun `an available native backend is used instead of the timer`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()
        assumeTrue(nativeBackendAvailable(displayId))

        // The probe passing is not the same thing as the platform branch being wired to it: an
        // unconditional TimerClock would still tick at roughly the right rate and pass every other
        // test in this file.
        val clock = SkikoFramePacingService.createClock(
            displayId, PLAUSIBLE_PERIOD_NANOS, PLAUSIBLE_PERIOD_NANOS, forceTimer = false
        )
        val expected = when (hostOs) {
            OS.MacOS -> MacDisplayLinkClock::class
            OS.Windows -> WinNativeClock::class
            OS.Linux -> LinuxDrmVBlankClock::class
            else -> error("unreachable: nativeBackendAvailable is false on $hostOs")
        }
        assertEquals(expected, clock::class, "wrong clock backend on $hostOs")
    }

    @Test
    fun `forceTimer overrides an available native backend`() {
        val service = headfulService()
        val displayId = defaultDisplayId(service)
        Library.load()

        val clock = SkikoFramePacingService.createClock(
            displayId, PLAUSIBLE_PERIOD_NANOS, PLAUSIBLE_PERIOD_NANOS, forceTimer = true
        )
        assertEquals(TimerClock::class, clock::class, "the debug escape hatch did not force the timer")
    }

    @Test
    fun `display ids come from the id string`() {
        assertEquals(-1L, SkikoFramePacingService.deviceDisplayId(null))
        assertEquals(
            -1L,
            SkikoFramePacingService.deviceDisplayId(
                FakeGraphicsDevice(GraphicsDevice.TYPE_PRINTER, "Display 3")
            ),
            "only raster screens have a display id"
        )

        when (hostOs) {
            // "Display <CGDirectDisplayID>" on macOS and "\\Display<screen>" on Windows both end in
            // the number; an id string that does not end in one is unusable.
            OS.MacOS, OS.Windows -> {
                assertEquals(1L, SkikoFramePacingService.deviceDisplayId(rasterDevice("Display 1")))
                assertEquals(0L, SkikoFramePacingService.deviceDisplayId(rasterDevice("\\Display0")))
                assertEquals(-1L, SkikoFramePacingService.deviceDisplayId(rasterDevice("Display")))
            }

            // Anywhere else the id only keys the clock registry, so any stable non-negative value
            // does. What matters is that one device is stable and two devices do not collide.
            else -> {
                val one = SkikoFramePacingService.deviceDisplayId(rasterDevice(":0.0"))
                assertEquals(one, SkikoFramePacingService.deviceDisplayId(rasterDevice(":0.0")))
                assertNotEquals(one, SkikoFramePacingService.deviceDisplayId(rasterDevice(":0.1")))
                assertTrue(one >= 0, "display id must be non-negative, was $one")
            }
        }
    }

    private fun rasterDevice(idString: String) =
        FakeGraphicsDevice(GraphicsDevice.TYPE_RASTER_SCREEN, idString)

    private fun nativeBackendAvailable(displayId: Long): Boolean = when (hostOs) {
        OS.MacOS -> MacDisplayLinkClock.available(displayId)
        // Both Windows flavours are WinNativeClock, so that branch is taken either way.
        OS.Windows -> true
        OS.Linux -> LinuxDrmVBlankClock.available()
        else -> false
    }

    private class FakeGraphicsDevice(
        private val deviceType: Int,
        private val idString: String
    ) : GraphicsDevice() {
        override fun getType(): Int = deviceType
        override fun getIDstring(): String = idString
        override fun getConfigurations(): Array<GraphicsConfiguration> = emptyArray()
        override fun getDefaultConfiguration(): GraphicsConfiguration? = null
    }

    private fun awaitTicks(ticks: AtomicLong, moreThan: Long = 0) {
        val deadline = System.currentTimeMillis() + 5000
        while (ticks.get() <= moreThan) {
            assertTrue(System.currentTimeMillis() < deadline, "no ticks within 5s")
            Thread.sleep(10)
        }
    }

    private companion object {
        /** 60 Hz: a period the clocks accept, for tests that only care which backend is built. */
        const val PLAUSIBLE_PERIOD_NANOS = 1_000_000_000L / 60
    }
}
