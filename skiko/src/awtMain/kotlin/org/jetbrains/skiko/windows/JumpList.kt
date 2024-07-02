package org.jetbrains.skiko.windows

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.jetbrains.skiko.hostOs

/**
 * Provides access to Windows Jump List features
 */
object JumpList {
    /**
     * Returns a value that indicates whether Jump Lists are supported.
     */
    fun isSupported(): Boolean = when {
        hostOs.isWindows -> true
        else -> false
    }

    /**
     * Wraps a single Jump List building transaction.
     */
    fun build(block: JumpListBuilder.() -> Unit) = when {
        hostOs.isWindows -> JumpListBuilder().use { builder ->
            builder.initialize()
            block(builder)
            builder.commit()
        }
        else -> error("Jump List is only supported on Windows")
    }
}

/**
 * Represents an item in the Jump List.
 */
class JumpListItem(val title: String, val arguments: String) {
    var attributes: JumpListItemAttributes? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JumpListItem

        if (title != other.title) return false
        if (arguments != other.arguments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + arguments.hashCode()
        return result
    }
}

/**
 * Represents optional attributes that can be added to a Jump List item.
 */
class JumpListItemAttributes {
    var description: String? = null
    var icon: JumpListItemIcon? = null
}

/**
 * Represents an icon that can be assigned to a Jump List item.
 *
 * @param path A path to the file containing the icon.
 * @param index An index of the icon in the specified file.
 */
class JumpListItemIcon(val path: String, val index: Int) {
    companion object {
        internal fun fromParts(path: String?, num: Int): JumpListItemIcon? {
            if (path.isNullOrEmpty()) return null
            return JumpListItemIcon(path, num)
        }
    }
}

/**
 * Provides functions to build the Jump List.
 *
 * This is a wrapper for ICustomDestinationList Windows COM interface.
 */
class JumpListBuilder internal constructor() : AutoCloseable {
    private var jumpListPointer: Long = 0L

    /**
     * Starts the Jump List building transaction.
     */
    internal fun initialize() {
        jumpListPointer = jumpList_init().also { ptr ->
            check(ptr != 0L) { "Failed to initialize Windows jump list" }
        }
    }

    /**
     * Adds a task to the Jump List. Tasks always appear in the canonical "Tasks" category
     * that is displayed at the bottom of the Jump List, after all other categories.
     */
    fun addUserTask(task: JumpListItem) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_addUserTask(jumpListPointer, task.toInterop())
    }

    /**
     * Adds a category to the Jump List. If there are more categories, they will appear
     * from top to bottom in the order they are appended.
     *
     * Make sure to exclude items returned by the [getRemovedItems] call as they may not be
     * re-added to the list during the same list-building transaction.
     * [addCategory] will fail if an attempt to add an item in the removed list is made.
     */
    fun addCategory(category: String, items: List<JumpListItem>) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        val interopItems = items.map { it.toInterop() }.toTypedArray()
        jumpList_addCategory(jumpListPointer, category, interopItems)
    }

    /**
     * Returns a list of items the user has chosen to remove from their Jump List.
     */
    fun getRemovedItems(): List<JumpListItem> {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        val interopItems = jumpList_getRemovedItems(jumpListPointer)
        return interopItems.map { it.fromInterop() }
    }

    /**
     * Finalises the Jump List building transaction by committing the list.
     */
    internal fun commit() {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_commit(jumpListPointer)
    }

    /**
     * Frees the Jump List's native resources.
     */
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

    private class JumpListInteropItem(val title: String,
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
