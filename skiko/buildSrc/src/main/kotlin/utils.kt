import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
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

inline fun <reified T : Task> Project.registerSkikoTask(
    taskName: String,
    crossinline fn: T.() -> Unit
): TaskProvider<T> = tasks.register(taskName, T::class) { fn() }

inline fun <reified T : Task> Project.registerSkikoTask(
    actionName: String,
    targetOs: OS,
    targetArch: Arch,
    crossinline fn: T.() -> Unit
): TaskProvider<T> {
    val taskName = joinToTitleCamelCase(actionName, targetOs.id, targetArch.id)
    return registerSkikoTask(taskName, fn)
}

fun joinToTitleCamelCase(vararg parts: String): String =
    parts.joinToString(separator = "") { toTitleCase(it) }

fun toTitleCase(s: String): String =
    s.capitalize(Locale.ROOT)

fun Task.projectDirs(vararg relativePaths: String): List<Directory> {
    val projectDir = project.layout.projectDirectory
    return relativePaths.map { path -> projectDir.dir(path) }
}