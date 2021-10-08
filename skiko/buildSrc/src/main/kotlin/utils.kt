import org.gradle.api.Task
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal val Provider<out FileSystemLocation>.absolutePath: String
        get() = get().asFile.absolutePath

internal fun Provider<out FileSystemLocation>.resolveToAbsolutePath(path: Provider<String>): String =
    get().asFile.absoluteFile.resolve(path.get()).absolutePath

inline fun <reified T : Any> ObjectFactory.nullableProperty(): Property<T?> =
    property(T::class.java)

inline fun <reified T : Any> ObjectFactory.notNullProperty(): Property<T> =
    property(T::class.java)

inline fun <reified T : Any> ObjectFactory.notNullProperty(defaultValue: T): Property<T> =
    property(T::class.java).value(defaultValue)

inline fun <reified T> Task.provider(noinline fn: () -> T): Provider<T> =
    project.provider(fn)
