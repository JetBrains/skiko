package symbols

import OS
import Arch
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class GenerateSymbolsListTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val targetOs: Property<OS>

    @get:Input
    abstract val targetArch: Property<Arch>

    @get:Optional
    @get:Input
    abstract val androidLlvmNm: Property<String>

    @get:InputFiles
    abstract val coreObjectFiles: ConfigurableFileCollection

    @get:InputFiles
    abstract val moduleObjectFiles: ConfigurableFileCollection

    @get:InputFiles
    abstract val skiaLibs: ConfigurableFileCollection

    @get:InputFiles
    abstract val moduleLibs: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val os = targetOs.get()
        val arch = targetArch.get()
        val outDir = outputDir.get().asFile
        outDir.mkdirs()
        if (os == OS.IOS || os == OS.TVOS || os == OS.Wasm) {
            throw IllegalStateException("generateSymbolsList does not support ${os.name} target")
        }

        logger.lifecycle(
            "generateSymbolsList: targetOs=${os.name}, targetArch=${arch.name}, coreObjects=${coreObjectFiles.files.size}, moduleObjects=${moduleObjectFiles.files.size}, skiaLibs=${skiaLibs.files.size}, moduleLibs=${moduleLibs.files.size}"
        )

        val coreExports = outDir.resolve("core_exports.txt")
        val extImports = outDir.resolve("ext_imports.txt")
        val symbolsFiltered = outDir.resolve("symbols_filtered.txt")
        val symbolsUnexported = outDir.resolve("symbols_unexported.txt")

        // 1. core exports
        val coreExportedList = extractSymbols(skiaLibs.files.toList() + coreObjectFiles.files.toList(), true)
        coreExports.writeText(coreExportedList.sorted().joinToString("\n"))

        // 2. all ext imports
        val extImportedList =
            extractSymbols(moduleObjectFiles.files.toList() + moduleLibs.files.toList(), false).toMutableList()

        // Keep JVM/JNI infrastructure globals
        extImportedList.addAll(coreExportedList.filter(::isJniInfrastructureSymbol))

        extImports.writeText(extImportedList.distinct().sorted().joinToString("\n"))

        // 3. initial keep list = intersection of ext imports + JNI with core exports
        val coreExportsSet = coreExportedList.toSet()
        val keepSet = extImportedList.filter { it in coreExportsSet }.toSet()
        symbolsFiltered.writeText(keepSet.sorted().joinToString("\n"))

        // 4. unexported = core exports minus what strip decided to keep
        val unexportedSet = coreExportsSet - keepSet
        symbolsUnexported.writeText(unexportedSet.sorted().joinToString("\n"))

        // Create export files for Linux or Windows. MacOS uses the raw txt file
        if (os.isLinux || os == OS.Android) {
            val versionScript = outDir.resolve("symbols.map")
            generateVersionScript(symbolsFiltered.toPath(), versionScript.toPath())
        }

        if (os.isWindows) {
            val defFile = outDir.resolve("symbols.def")
            generateDefFile(symbolsFiltered.toPath(), defFile.toPath())
        }

        logger.lifecycle("Symbols to keep: ${keepSet.size}, to hide: ${unexportedSet.size}")
    }

    private fun extractSymbols(files: List<File>, exported: Boolean): List<String> {
        val os = targetOs.get()
        logger.lifecycle(
            "generateSymbolsList: extracting ${if (exported) "exported" else "undefined"} " +
                    "from ${files.size} files"
        )

        val type = if (exported) SymbolType.DefinedGlobal else SymbolType.Undefined
        return SymbolExtractor(
            execOperations = execOperations,
            os = os,
            androidLlvmNm = if (os == OS.Android) androidLlvmNm.get() else null,
        ).extract(files, type).toList()
    }

}
