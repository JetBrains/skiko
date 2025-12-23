package org.jetbrains.skiko

internal interface CloseScope {
    fun <T : AutoCloseable> T.autoClose(): T
}

/**
 * Scope that takes care of closing resources inside this scope.
 * It is convenient method to avoid nested blocks of code such as:
 * ```
 * openResource("resource1").use { resource1 ->
 *     openResource("resource2").use { resource2 ->
 *
 *     }
 * }
 * ```
 * Instead, we can write:
 * ```
 * autoCloseScope {
 *     val resource1 = openResource("resource1").autoClose()
 *     val resource2 = openResource("resource2").autoClose()
 * }
 * ```
 */
internal fun autoCloseScope(body: CloseScope.() -> Unit) {
    val resources = mutableListOf<AutoCloseable>()
    val scope = object : CloseScope {
        override fun <T : AutoCloseable> T.autoClose(): T {
            resources.add(this)
            return this
        }
    }
    try {
        scope.body()
    } finally {
        for (index in resources.indices.reversed()) {
            val item = resources[index]
            item.close()
        }
    }
}