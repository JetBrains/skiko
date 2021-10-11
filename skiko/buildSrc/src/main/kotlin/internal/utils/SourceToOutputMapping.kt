package internal.utils

import java.io.File
import java.util.*

/**
 * Maps [sourceFiles] to output files, using relative paths.
 * `<SOURCE_ROOT>/relative/path.[sourceFileExt]` is mapped to `[outDir]/relative/path.[outputFileExt]`
 *
 * The algorithm works in O(N * M), where:
 *  * N is a number of source roots;
 *  * M is a number of unique subdirectories, containing source files;
 *  usually both numbers are relatively small, so we should be OK
 *
 * @throws IllegalStateException when a source file does not have a matching source root
 */
internal fun mapSourceFilesToOutputFiles(
    sourceRoots: Collection<File>,
    sourceFiles: Collection<File>,
    outDir: File,
    sourceFileExt: String,
    outputFileExt: String
): SourceToOutputMapping {
    val sourceRoots = sourceRoots.map { it.absoluteFile }
    val sourceRootCache = HashMap<File, File>()
    fun findSourceRootFor(sourceFile: File): File? {
        val parentDir = sourceFile.parentFile ?: return null
        return sourceRootCache.getOrPut(parentDir) {
            sourceRoots.firstOrNull { root -> parentDir.path.startsWith(root.path) }
                ?: throw UnknownSourceRootException(sourceFile, sourceRoots)
        }
    }

    val sourceToOutput = SourceToOutputMapping()
    for (sourceFile in sourceFiles.map { it.absoluteFile }) {
        val sourceRoot = findSourceRootFor(sourceFile)
        val relativeSourcePath =
            if (sourceRoot == null) sourceFile.name
            else sourceFile.relativeTo(sourceRoot).path

        val relativeOutputPath = relativeSourcePath.removeSuffix(sourceFileExt) + outputFileExt
        sourceToOutput[sourceFile] = outDir.resolve(relativeOutputPath)
    }
    return sourceToOutput
}

internal class SourceToOutputMapping {
    private val sourceToOutput = TreeMap<File, File>(object : Comparator<File> {
        override fun compare(f1: File, f2: File): Int =
            f1.absolutePath.compareTo(f2.absolutePath)

    })

    operator fun get(file: File): File? =
        sourceToOutput[file]

    fun remove(file: File): File? =
        sourceToOutput.remove(file)

    operator fun set(sourceFile: File, outputFile: File) {
        sourceToOutput[sourceFile] = outputFile
    }

    fun putAll(other: SourceToOutputMapping) {
        sourceToOutput.putAll(other.sourceToOutput)
    }

    fun clear() {
        sourceToOutput.clear()
    }

    fun save(file: File) {
        file.bufferedWriter().use { writer ->
            for ((source, output) in sourceToOutput.entries) {
                writer.write(source.absolutePath)
                writer.write(File.pathSeparator)
                writer.write(output.absolutePath)
                writer.write("\n")
            }
        }
    }

    fun load(file: File) {
        file.bufferedReader().useLines { lines ->
            for (line in lines) {
                val parts = line.split(File.pathSeparator)
                check(parts.size == 2) {
                    """
                            Line does not match the expected format!
                            Expected format: '<PATH_1>${File.pathSeparator}<PATH_2>'
                            Actual value: '$line'
                        """.trimIndent()
                }
                val (sourcePath, outputPath) = parts

                this[File(sourcePath)] = File(outputPath)
            }
        }
    }
}