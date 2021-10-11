package internal.utils

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.io.File

interface ArgBuilder {
    /**
     * Writes all args to [file]
     *
     * @return an argument to a compiler/linker, pointing to the arg file (aka response file)
     */
    fun createArgFile(file: File): String
    fun copy(fn: ArgBuilder.() -> Unit): ArgBuilder
    fun arg(argName: String? = null, value: Any? = null)
    fun repeatedArg(argName: String? = null, values: Collection<Any>) {
        values.forEach { value -> arg(argName, value) }
    }
    fun rawArg(arg: String)
    fun rawArgs(args: Collection<String>) {
        args.forEach { rawArg(it) }
    }
    fun toArray(): Array<String>
}

internal abstract class AbstractArgBuilder : ArgBuilder {
    protected val args = arrayListOf<String>()

    protected abstract fun newSelfInstance(): ArgBuilder

    override fun copy(fn: ArgBuilder.() -> Unit): ArgBuilder {
        val newArgs = newSelfInstance()
        newArgs.rawArgs(args)
        newArgs.fn()
        return newArgs
    }

    override fun createArgFile(file: File): String {
        file.writeLines(args)
        return "@${escapePathIfNeeded(file)}"
    }

    override fun arg(argName: String?, value: Any?) {
        addTransformedArgs(
            argName = transformName(argName),
            value = transformValue(value)
        )
    }

    override fun rawArg(arg: String) {
        args.add(arg)
    }

    protected open fun addTransformedArgs(argName: String?, value: String?) {
        argName?.let { args.add(it) }
        value?.let { args.add(it) }
    }

    protected open fun transformName(argName: String?): String? =
        argName

    private fun transformValue(value: Any?): String? =
        when (value) {
            is Provider<*> -> transformValue(value.get())
            is FileSystemLocation -> transformValue(value.asFile)
            is File -> escapePathIfNeeded(value)
            is Any -> value.toString()
            else -> null
        }

    protected open fun escapePathIfNeeded(file: File): String =
        file.absolutePath

    override fun toArray(): Array<String> =
        args.toTypedArray()
}

internal abstract class BaseVisualStudioBuildToolsArgBuilder : AbstractArgBuilder() {
    override fun escapePathIfNeeded(file: File): String {
        val path = file.absolutePath
            .replace("/", "\\")
            .replace("\\", "\\\\")
        return if (" " in path) "\"$path\"" else path
    }
}

internal class DefaultArgBuilder() : AbstractArgBuilder() {
    override fun newSelfInstance(): ArgBuilder = DefaultArgBuilder()
}

internal class VisualCppCompilerArgBuilder : BaseVisualStudioBuildToolsArgBuilder() {
    private val objectOutputArg = "/Fo"
    private val includeDirArg = "/I"
    private val argsToJoinWithValues = listOf(objectOutputArg, includeDirArg)

    override fun newSelfInstance(): ArgBuilder = VisualCppCompilerArgBuilder()

    override fun transformName(argName: String?): String? =
        when (argName) {
            "-o", "--output" -> objectOutputArg
            "-c", "--compile" -> "/c"
            "-I", "--include-directory" -> includeDirArg
            else -> super.transformName(argName)
        }

    override fun addTransformedArgs(argName: String?, value: String?) {
        if (argName in argsToJoinWithValues) {
            args.add("$argName$value")
        } else {
            super.addTransformedArgs(argName, value)
        }
    }
}

internal class VisualCppLinkerArgBuilder : BaseVisualStudioBuildToolsArgBuilder() {
    private val outArg = "/OUT"
    private val libPathArg = "/LIBPATH"
    private val argsToJoinWithValues = listOf(outArg, libPathArg)

    override fun newSelfInstance(): ArgBuilder = VisualCppLinkerArgBuilder()

    override fun transformName(argName: String?): String? =
        when (argName) {
            "-o", "--output" -> outArg
            "-L", "--library-directory" -> libPathArg
            else -> super.transformName(argName)
        }

    override fun addTransformedArgs(argName: String?, value: String?) {
        if (argName in argsToJoinWithValues) {
            args.add("$argName:$value")
        } else {
            super.addTransformedArgs(argName, value)
        }
    }
}