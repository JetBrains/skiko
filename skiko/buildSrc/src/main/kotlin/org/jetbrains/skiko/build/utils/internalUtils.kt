package org.jetbrains.skiko.build.utils

import java.io.File
import java.io.Writer
import org.gradle.api.Task
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

internal fun Provider<out FileSystemLocation>.resolveToIoFile(relative: Provider<String>): File =
    get().asFile.resolve(relative.get())

internal inline fun <reified T : Any> Task.provider(noinline fn: () -> T): Provider<T> =
    project.provider(fn)

internal fun File.writeLines(lines: Collection<String>) {
    if (exists()) {
        delete()
    } else {
        parentFile?.mkdirs()
    }

    bufferedWriter().use { writer ->
        lines.forEach { writer.writeLine(it) }
    }
}

internal fun Writer.writeLine(line: String) {
    write(line)
    write("\n")
}
