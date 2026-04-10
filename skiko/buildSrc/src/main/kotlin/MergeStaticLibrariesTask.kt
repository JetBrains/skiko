import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Merges multiple static libraries (.a) into a single static library with
 * only bridge API symbols kept global. All other symbols (Skia, ICU, harfbuzz, …)
 * are turned local so they cannot clash with the consumer's own copies.
 *
 * Pipeline:
 *  1. `ld -r`          — partial-link bridges (force-loaded) + Skia archives into one .o
 *  2. `llvm-objcopy`   — globalize the required symbols (some arrive as hidden/local
 *                         from Skia), then localize everything else
 *  3. `libtool`        — wrap the .o into a .a for Kotlin/Native `-include-binary`
 *
 * Apple-only. Handles fat (universal) Skia archives via `-arch`.
 */
abstract class MergeStaticLibrariesTask : DefaultTask() {

    /** The bridges static library — its defined global symbols become the export list. */
    @get:InputFile
    abstract val bridgesLibrary: RegularFileProperty

    /** All static libraries to merge (Skia libs + bridges). */
    @get:InputFiles
    abstract val inputLibraries: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val outputFileName: Property<String>

    /** Architecture name for Apple's ld (e.g. "arm64", "x86_64"). */
    @get:Input
    abstract val ldArch: Property<String>

    /** Apple platform name for ld (e.g. "ios", "ios-simulator"). */
    @get:Input
    abstract val platformName: Property<String>

    /** Minimum OS deployment version (e.g. "12.0"). */
    @get:Input
    abstract val minOsVersion: Property<String>

    /**
     * Kotlin source root to scan for @SymbolName / @ExternalSymbolName declarations.
     * Symbols declared this way in Kotlin/Native code must stay global even if they
     * originate from Skia (e.g. ICU wrappers like `uloc_getDefault_skiko`).
     */
    @get:Internal
    abstract val kotlinSourceRoot: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun merge() {
        val workDir = temporaryDir
        workDir.deleteRecursively()
        workDir.mkdirs()

        val outDir = outputDir.get().asFile
        outDir.deleteRecursively()
        outDir.mkdirs()

        // 1. Partial-link everything into a single relocatable Mach-O
        val mergedObj = partialLink(workDir)

        // 2. Build the export list and adjust symbol visibility
        val exportsFile = buildExportsList(workDir)
        adjustSymbolVisibility(mergedObj, exportsFile)

        // 3. Wrap into a .a
        archiveLibrary(outDir, mergedObj)
    }

    /**
     * Partial-links all archives into one relocatable .o.
     *
     * The bridges archive is force-loaded so every bridge object is included.
     * Skia archives are linked lazily — the linker pulls in only the objects
     * needed to resolve the bridges' undefined references, which avoids
     * duplicate-symbol errors from Skia archives that share identical members
     * (e.g. harfbuzz objects present in both libskia.a and libskshaper.a).
     */
    private fun partialLink(workDir: File): File {
        val sdkVersion = querySdkVersion()
        val bridgesFile = bridgesLibrary.get().asFile
        val skiaLibPaths = inputLibraries.files
            .filter { it != bridgesFile }
            .map { it.absolutePath }
        val mergedObj = File(workDir, "merged.o")

        execOperations.exec {
            executable = "ld"
            args = listOf(
                "-r",
                "-arch", ldArch.get(),
                "-platform_version", platformName.get(), minOsVersion.get(), sdkVersion,
                "-force_load", bridgesFile.absolutePath,
            ) + skiaLibPaths + listOf(
                "-o", mergedObj.absolutePath,
            )
        }
        return mergedObj
    }

    /**
     * Builds the list of symbols that must stay global by scanning Kotlin sources
     * for @SymbolName / @ExternalSymbolName annotations.
     *
     * This captures both bridge API symbols (defined in bridges) and Skia-provided
     * symbols referenced directly from K/N (e.g. `uloc_getDefault_skiko` from ICU).
     */
    private fun buildExportsList(workDir: File): File {
        val kotlinSymbols = scanKotlinSymbolNames()
        return File(workDir, "exported_symbols.txt").apply {
            writeText(kotlinSymbols.joinToString("\n") + "\n")
        }
    }

    /**
     * Uses `llvm-objcopy` to adjust symbol visibility in the merged .o:
     *
     * 1. `--globalize-symbols` — promotes listed symbols to global. This is critical
     *    for symbols that arrive from Skia with hidden visibility (private external)
     *    and get demoted to local by `ld -r` (e.g. `uloc_getDefault_skiko`).
     *    Unlike `nmedit -s`, llvm-objcopy can *promote* local symbols back to global.
     *
     * 2. `--keep-global-symbols` (same file) — localizes every global symbol NOT in
     *    the list. Combined with step 1, only the listed symbols remain global.
     */
    private fun adjustSymbolVisibility(obj: File, exportsFile: File) {
        execOperations.exec {
            executable = "xcrun"
            args = listOf(
                "llvm-objcopy",
                "--globalize-symbols=${exportsFile.absolutePath}",
                "--keep-global-symbols=${exportsFile.absolutePath}",
                obj.absolutePath,
            )
        }
    }

    /**
     * Scans Kotlin sources for `@SymbolName("…")` and `@ExternalSymbolName("…")`
     * annotations and returns the referenced symbol names with Mach-O `_` prefix.
     */
    private fun scanKotlinSymbolNames(): List<String> {
        val root = kotlinSourceRoot.orNull?.asFile ?: return emptyList()
        if (!root.isDirectory) return emptyList()
        val pattern = Regex("""@(?:SymbolName|ExternalSymbolName)\("([^"]+)"\)""")
        return root.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                file.useLines { lines ->
                    lines.mapNotNull { pattern.find(it)?.groupValues?.get(1) }.toList()
                }
            }
            .map { "_$it" }
            .distinct()
            .sorted()
            .toList()
    }

    private fun archiveLibrary(outDir: File, mergedObj: File) {
        execOperations.exec {
            executable = "libtool"
            args = listOf(
                "-static",
                "-o", File(outDir, outputFileName.get()).absolutePath,
                mergedObj.absolutePath,
            )
        }
    }

    // ---- helpers ----

    private fun querySdkVersion(): String {
        val sdkName = when (platformName.get()) {
            "ios" -> "iphoneos"
            "ios-simulator" -> "iphonesimulator"
            else -> error("Unknown platform: ${platformName.get()}")
        }
        val out = ByteArrayOutputStream()
        execOperations.exec {
            executable = "xcrun"
            args = listOf("--sdk", sdkName, "--show-sdk-version")
            standardOutput = out
        }
        return out.toString().trim()
    }
}
