package org.jetbrains.skiko

import kotlinx.cinterop.interpretCPointer
import kotlinx.coroutines.*
import platform.CoreFoundation.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t
import platform.posix.QOS_CLASS_BACKGROUND
import kotlin.coroutines.CoroutineContext
import kotlin.concurrent.AtomicNativePtr
import kotlin.native.concurrent.freeze
import kotlin.native.internal.NativePtr

// This is the only dispatcher that shall be used in Skiko on iOS.
// Current (as of 1.5.2) dispatchers in kotlinx.coroutines are not usable
// for needs of Skiko.
object SkikoDispatchers {
    val Main: CoroutineDispatcher = NsQueueDispatcher(dispatch_get_main_queue())
    val IO: CoroutineDispatcher = NsQueueDispatcher(
        dispatch_get_global_queue(
            QOS_CLASS_BACKGROUND.toLong(), 0)
    )
}

@OptIn(InternalCoroutinesApi::class)
internal class NsQueueDispatcher(
    private val dispatchQueue: dispatch_queue_t
) : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatchQueue) {
            block.run()
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val timer = Timer()
        val timerBlock: TimerBlock = {
            timer.dispose()
            continuation.resume(Unit) { t -> t.printStackTrace() }
        }
        timer.start(timeMillis, timerBlock)
        continuation.disposeOnCancellation(timer)
    }


    @OptIn(InternalCoroutinesApi::class)
    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val timer = Timer()
        val timerBlock: TimerBlock = {
            timer.dispose()
            block.run()
        }
        timer.start(timeMillis, timerBlock)
        return timer

    }
}

internal typealias TimerBlock = (CFRunLoopTimerRef?) -> Unit

private val TIMER_NEW = NativePtr.NULL

private val TIMER_DISPOSED = NativePtr.NULL.plus(1)

private class Timer : DisposableHandle {
    private val ref = AtomicNativePtr(TIMER_NEW)

    fun start(timeMillis: Long, timerBlock: TimerBlock) {
        val fireDate = CFAbsoluteTimeGetCurrent() + timeMillis / 1000.0
        val timer = CFRunLoopTimerCreateWithHandler(null, fireDate, 0.0, 0u, 0, timerBlock)!!
        CFRunLoopAddTimer(CFRunLoopGetMain(), timer, kCFRunLoopCommonModes)
        if (!ref.compareAndSet(TIMER_NEW, timer.rawValue)) {
            // dispose was already called concurrently
            release(timer)
        }
    }

    override fun dispose() {
        while (true) {
            val ptr = ref.value
            if (ptr == TIMER_DISPOSED) return
            if (ref.compareAndSet(ptr, TIMER_DISPOSED)) {
                if (ptr != TIMER_NEW) release(interpretCPointer(ptr))
                return
            }
        }
    }

    private fun release(timer: CFRunLoopTimerRef?) {
        CFRunLoopRemoveTimer(CFRunLoopGetMain(), timer, kCFRunLoopCommonModes)
        CFRelease(timer)
    }
}

