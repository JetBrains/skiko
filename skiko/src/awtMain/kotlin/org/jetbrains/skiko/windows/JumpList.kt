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
        hostOs.isWindows -> JumpListBuilder().use { builder ->
            builder.initialize()
            block(builder)
            builder.commit()
        }
        else -> error("Jump List is only supported on Windows")
    }
}

data class JumpListItem(val title: String, val arguments: String) {
    var attributes: JumpListItemAttributes? = null
}

class JumpListItemAttributes {
    var description: String? = null
    var icon: JumpListItemIcon? = null
}

data class JumpListItemIcon(val path: String, val index: Int) {
    companion object {
        internal fun fromParts(path: String?, num: Int): JumpListItemIcon? {
            if (path.isNullOrBlank()) return null
            return JumpListItemIcon(path, num)
        }
    }
}

class JumpListBuilder internal constructor() : AutoCloseable {
    private var jumpListPointer: Long = 0L

    internal fun initialize() {
        jumpListPointer = jumpList_init().also { ptr ->
            check(ptr != 0L) { "Failed to initialize Windows jump list" }
        }
    }

    fun addUserTask(task: JumpListItem) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_addUserTask(jumpListPointer, task.toInterop())
    }

    fun addCategory(category: String, items: List<JumpListItem>) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        val interopItems = items.map { it.toInterop() }.toTypedArray()
        jumpList_addCategory(jumpListPointer, category, interopItems)
    }

    fun getRemovedItems(): List<JumpListItem> {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        val interopItems = jumpList_getRemovedItems(jumpListPointer)
        return interopItems.map { it.fromInterop() }
    }

    internal fun commit() {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_commit(jumpListPointer)
    }

    override fun close() {
        if (jumpListPointer != 0L) {
            jumpList_dispose(jumpListPointer)
        }
        jumpListPointer = 0L
    }

    private external fun jumpList_init(): Long
    private external fun jumpList_dispose(ptr: Long)

    private external fun jumpList_addUserTask(ptr: Long, task: JumpListInteropItem)
    private external fun jumpList_addCategory(ptr: Long, category: String, itemsArray: Array<JumpListInteropItem>)
    private external fun jumpList_getRemovedItems(ptr: Long): Array<JumpListInteropItem>

    private external fun jumpList_commit(ptr: Long)

    private data class JumpListInteropItem(val title: String,
                                           val arguments: String,
                                           val description: String?,
                                           val iconPath: String?,
                                           val iconNum: Int)

    private fun JumpListItem.toInterop() = JumpListInteropItem(
        title, arguments, attributes?.description, attributes?.icon?.path, attributes?.icon?.index ?: 0
    )

    private fun JumpListInteropItem.fromInterop() = JumpListItem(title, arguments)
        .also { item ->
            item.attributes = when {
                iconPath.isNullOrEmpty() && description.isNullOrEmpty() -> null
                else -> JumpListItemAttributes().also { attr ->
                    attr.description = description
                    attr.icon = JumpListItemIcon.fromParts(iconPath, iconNum)
                }
            }
        }
}
