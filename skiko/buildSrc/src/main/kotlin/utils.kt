import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

// Utils, that are not needed in scripts can be placed to internal/utils

inline fun <reified T : Task> TaskContainer.registerOrGetTask(
    name: String, crossinline fn: T.() -> Unit
): Provider<T> {
    val taskProvider =
        if (name in names) named(name)
        else register(name, T::class) { fn(this) }
    return taskProvider.map { it as T }
}

fun joinToTitleCamelCase(vararg parts: String): String =
    parts.joinToString(separator = "") { part ->
        if (part.isEmpty()) part
        else buildString {
            append(part.first().toTitleCase())
            append(part.substring(1))
        }
    }