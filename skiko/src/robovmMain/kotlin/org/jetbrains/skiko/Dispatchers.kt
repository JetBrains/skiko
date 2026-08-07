package org.jetbrains.skiko

import kotlinx.coroutines.*
import org.robovm.apple.dispatch.DispatchBlock
import org.robovm.apple.dispatch.DispatchBlockFlags
import org.robovm.apple.dispatch.DispatchQueue
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

// This is the only dispatcher that shall be used in Skiko on iOS.
object SkikoDispatchers {
    val Main: CoroutineDispatcher = NsQueueDispatcher(DispatchQueue.getMainQueue())
    val IO: CoroutineDispatcher = NsQueueDispatcher(
        DispatchQueue.getGlobalQueue(DispatchQueue.PRIORITY_BACKGROUND.toLong(), 0)
    )
}

@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
internal class NsQueueDispatcher(
    private val dispatchQueue: DispatchQueue
) : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatchQueue.async(block)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val block = Runnable {
            continuation.resume(Unit) { t -> t.printStackTrace() }
        }
        val dispatchBlock = DispatchBlock.create(DispatchBlockFlags.None, block)
        dispatchQueue.after(timeMillis, TimeUnit.MILLISECONDS, dispatchBlock)
        continuation.invokeOnCancellation { DispatchBlock.cancel(dispatchBlock) }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val dispatchBlock = DispatchBlock.create(DispatchBlockFlags.None, block)
        dispatchQueue.after(timeMillis, TimeUnit.MILLISECONDS, dispatchBlock)
        return object : DisposableHandle {
            override fun dispose() {
                DispatchBlock.cancel(dispatchBlock)
            }
        }
    }
}
