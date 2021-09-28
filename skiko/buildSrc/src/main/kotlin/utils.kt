import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

internal fun Provider<out FileSystemLocation>.resolveToAbsolutePath(path: Provider<String>): String =
    get().asFile.absoluteFile.resolve(path.get()).absolutePath
