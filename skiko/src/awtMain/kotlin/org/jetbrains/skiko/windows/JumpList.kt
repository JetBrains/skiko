package org.jetbrains.skiko.windows

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
     *
     * The transaction starts with a call to [JumpListBuilder.beginList] and is finalised
     * by a call to [JumpListBuilder.commit].
     */
    fun <R> build(block: JumpListBuilder.() -> R): R = when {
        hostOs.isWindows -> JumpListBuilder().use { builder ->
            builder.initialize()
            block(builder)
        }
        else -> error("Jump List is only supported on Windows")
    }

    /**
     * Wraps a single Jump List building transaction.
     *
     * The transaction starts with a call to [JumpListBuilder.beginList] and is finalised
     * by a call to [JumpListBuilder.commit].
     */
    suspend fun <R> buildAsync(block: suspend JumpListBuilder.() -> R): R = when {
        hostOs.isWindows -> JumpListBuilder().use { builder ->
            builder.initialize()
            block(builder)
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

    private var removedItems: List<JumpListItem> = listOf()

    private val userTasks: MutableList<JumpListInteropItem> = mutableListOf()
    private val categories: MutableMap<String, List<JumpListInteropItem>> = mutableMapOf()
  
    /**
     * Starts the Jump List building transaction.
     */
    internal fun initialize() {
        jumpListPointer = jumpList_init().also { ptr ->
            check(ptr != 0L) { "Failed to initialize Windows jump list" }
        }
    }

    /**
     * Specifies an optional AppUserModelID for the Jump List.
     *
     * This function must be called if the application has an explicit AppUserModelID.
     *
     * @throws JumpListException [setAppID] must be called before [beginList]
     */
    fun setAppID(appID: String) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_setAppID(jumpListPointer, appID)
    }

    /**
     * Creates a new list.
     */
    fun beginList() {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_beginList(jumpListPointer).let { interopItems ->
            removedItems = interopItems.map { it.fromInterop() }
        }
    }

    /**
     * Returns a list of items the user has chosen to remove from their Jump List.
     */
    fun getRemovedItems(): List<JumpListItem> = removedItems

    /**
     * Adds a task to the Jump List. Tasks always appear in the canonical "Tasks" category
     * that is displayed at the bottom of the Jump List, after all other categories.
     */
    fun addUserTask(task: JumpListItem) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        userTasks.add(task.toInterop())
    }

    /**
     * Adds a category to the Jump List. If there are more categories, they will appear
     * from top to bottom in the order they are appended.
     *
     * Make sure to exclude removed items returned by the [beginList] call as they may not be
     * re-added to the list during the same list-building transaction.
     */
    fun addCategory(category: String, items: List<JumpListItem>) {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        items.map { it.toInterop() }.let { interopItems ->
            categories.merge(category, interopItems) { existing, new -> existing + new }
        }
    }

    /**
     * Finalises the Jump List building transaction by committing the list.
     *
     * @return [Result.success] if the list has been committed successfully. [Result.failure] if there was
     * a recoverable error when committing the list. In the latter case, the list was still committed, but
     * might lack some or all categories.
     *
     * @throws JumpListException An attempt to add an item in the removed list was made (see [addCategory])
     */
    fun commit(): Result<Unit> {
        check(jumpListPointer != 0L) { "The jump list pointer is invalid" }
        jumpList_addUserTasks(jumpListPointer, userTasks.toTypedArray())
        val result = try {
            categories.forEach { (name, items) ->
                jumpList_addCategory(jumpListPointer, name, items.toTypedArray())
            }
            Result.success(Unit)
        }
        catch (e: JumpListException) {
            // According to the documentation, in case if AppendCategory fails with E_ACCESSDENIED,
            // the application should continue to update tasks and call CommitList.
            if (e.code != -2147024891) {
                throw e
            }
            Result.failure(e)
        }
        jumpList_commit(jumpListPointer)
        return result
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

    private external fun jumpList_setAppID(ptr: Long, appID: String)
    private external fun jumpList_beginList(ptr: Long): Array<JumpListInteropItem>
    private external fun jumpList_addUserTasks(ptr: Long, tasks: Array<JumpListInteropItem>)
    private external fun jumpList_addCategory(ptr: Long, category: String, itemsArray: Array<JumpListInteropItem>)

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

class JumpListException(message: String, val code: Int) : RuntimeException(message)
