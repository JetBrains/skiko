import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register
import java.util.*

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
    parts.joinToString(separator = "") { toTitleCase(it) }

fun toTitleCase(s: String): String =
    s.capitalize(Locale.ROOT)

fun <T> ListProperty<T>.set(vararg elements: T) {
    set(elements.toList())
}