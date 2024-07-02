package org.jetbrains.skiko.windows

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.jetbrains.skiko.hostOs

/**
 * Provides access to Windows Jump List features
 */
object JumpList {
    fun isSupported(): Boolean = when {
        hostOs.isWindows -> true
        else -> false
    }

    fun build(block: JumpListBuilder.() -> Unit) = when {
        hostOs.isWindows -> JumpListBuilder().use { jumpListBuilder ->
            block(jumpListBuilder)
            jumpListBuilder.commit()
        }
        else -> error("Jump List is only supported on Windows")
    }
}

data class JumpListItem(val title: String, val arguments: String)

class JumpListBuilder : AutoCloseable {
    private val jumpListPointer: Long = jumpList_init().also { ptr ->
        check(ptr != 0L) { "Failed to initialize Windows jump list" }
    }

    fun addUserTask(item: JumpListItem) =
        jumpList_addUserTask(jumpListPointer, item.title, item.arguments)

    fun addCategory(category: String, items: List<JumpListItem>) =
        jumpList_addCategory(jumpListPointer, category, items.toTypedArray())

    fun getRemovedItems(): List<JumpListItem> =
        jumpList_getRemovedItems(jumpListPointer).asList()

    internal fun commit() = jumpList_commit(jumpListPointer)

    override fun close() = jumpList_dispose(jumpListPointer)

    private external fun jumpList_init(): Long
    private external fun jumpList_dispose(ptr: Long)

    private external fun jumpList_addUserTask(ptr: Long, taskName: String, taskExecArg: String)
    private external fun jumpList_addCategory(ptr: Long, category: String, itemsArray: Array<JumpListItem>)
    private external fun jumpList_getRemovedItems(ptr: Long): Array<JumpListItem>

    private external fun jumpList_commit(ptr: Long)
}
