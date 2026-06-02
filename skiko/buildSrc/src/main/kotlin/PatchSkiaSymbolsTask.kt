import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Renames all public symbols in Skia and skiko-bridge static libraries (iOS/tvOS)
 * into a `skiko`-namespaced form to prevent symbol clashes when multiple copies of
 * Skia are linked into the same app binary.
 *
 * - C++ mangled names get `skiko` encoded as an outer namespace
 * (`__ZN7SkPaint4MakeEv` → `__ZN5skiko7SkPaint4MakeEv`).
 * - Plain C names receive a `_skiko` suffix (`_uloc_getDefault` → `_uloc_getDefault_skiko`).
 * - Symbols already carrying the `_skiko` suffix are left untouched.
 *
 * Produces patched copies of every input library mapping file in [outputDir].
 */
abstract class PatchSkiaSymbolsTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    /** Paths to Skia static libraries (.a). Public symbols are extracted from these. */
    @get:InputFiles
    abstract val skiaLibs: ListProperty<File>

    /** Path to the skiko C++ native-bridges static library (.a). */
    @get:InputFile
    abstract val skikoBridge: Property<File>

    /** Directory where patched libraries and redefine-syms.txt will be written. */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun execute() {
        val llvmObjcopy = findLlvmObjcopy()
        val outDir = outputDir.get().asFile
        outDir.mkdirs()

        val skiaLibFiles = skiaLibs.get()
        val bridgeFile = skikoBridge.get()
        val allLibs = skiaLibFiles + bridgeFile

        // 1. Collect all publicly defined symbols from the Skia libraries,
        //    skipping any that already carry the "_skiko" suffix — those were
        //    pre-renamed at the skia-pack build level (e.g. ICU C functions
        //    compiled with -DU_LIB_SUFFIX_C_NAME=_skiko) and must not be
        //    renamed a second time.
        logger.lifecycle("Extracting public symbols from Skia libraries …")
        val allSymbols = mutableSetOf<String>()
        for (lib in skiaLibFiles) {
            val syms = extractGlobalDefinedSymbols(lib)
            val newSyms = syms.filterTo(mutableSetOf()) { !it.endsWith("_skiko") }
            logger.lifecycle(
                "  ${lib.name.padEnd(40)}  ${"%6d".format(newSyms.size)} symbols " +
                    "(${syms.size - newSyms.size} already-renamed skipped)"
            )
            allSymbols.addAll(newSyms)
        }
        logger.lifecycle("  ${"TOTAL".padEnd(40)}  ${"%6d".format(allSymbols.size)} unique symbols to rename")

        // 1b. Also collect C++ mangled symbols defined in the bridge itself.
        //
        //     The bridge's .cpp files include Skia headers and may produce
        //     symbol definitions locally — template instantiations or `inline`
        //     function copies — that were never instantiated inside the Skia
        //     static libraries.  Those symbols are NOT captured by the scan
        //     above, so without this step they would survive patching under
        //     their original names and could clash with a second copy of Skia
        //     linked into the same binary.  We rename ONLY symbols whose Mach-O
        //     name starts with "__Z".
        logger.lifecycle("Extracting C++ symbols from skiko bridge …")
        val bridgeSyms = extractGlobalDefinedSymbols(bridgeFile)
        val bridgeCxxSyms = bridgeSyms.filterTo(mutableSetOf()) {
            it.startsWith("__Z") && !it.endsWith("_skiko")
        }
        val newInBridge = bridgeCxxSyms - allSymbols
        allSymbols.addAll(bridgeCxxSyms)
        logger.lifecycle(
            "  ${"bridge (C++ only)".padEnd(40)}  ${"%6d".format(bridgeCxxSyms.size)} symbols " +
                "(${newInBridge.size} not already covered by Skia libs)"
        )

        // 2. Write redefine-syms.txt (llvm-objcopy --redefine-syms format)
        val symbolsSorted = allSymbols.sorted()
        val redefineSymsFile = File(outDir, "redefine-syms.txt")
        redefineSymsFile.writeText(
            symbolsSorted.joinToString("\n") { sym -> "$sym ${renamed(sym)}" } + "\n"
        )
        logger.lifecycle("Written: $redefineSymsFile")

        // 3. Patch every library (Skia libs + skiko bridge)
        logger.lifecycle("Patching libraries …")
        for (lib in allLibs) {
            val outLib = File(outDir, lib.name)
            logger.lifecycle("  ${lib.name.padEnd(40)}  ->  $outLib")
            patchLibrary(lib, redefineSymsFile, outLib, llvmObjcopy)
        }

        logger.lifecycle("Done.")
    }

    /**
     * Run a command, capture stdout, and fail with a descriptive message on non-zero exit.
     */
    private fun run(vararg args: String): String {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(*args)
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            val cmd = args.joinToString(" ")
            val stdoutStr = stdout.toString(Charsets.UTF_8)
            val stderrStr = stderr.toString(Charsets.UTF_8)
            error("FAILED (${result.exitValue}): $cmd\n$stdoutStr\n$stderrStr")
        }
        return stdout.toString(Charsets.UTF_8)
    }

    /**
     * Run a command without failing on non-zero exit. Returns trimmed stdout on success, null on failure.
     */
    private fun runOrNull(vararg args: String): String? {
        val stdout = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(*args)
            standardOutput = stdout
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }
        return if (result.exitValue == 0) stdout.toString(Charsets.UTF_8).trim() else null
    }

    /**
     * Return the set of all globally-visible, defined symbol names found in
     * a Mach-O static library.
     *
     * `nm -g --defined-only` outputs lines of the form:
     *
     *     0000000000000020 T _SkPaint_Make
     *
     * followed by bare archive-member headers such as:
     *
     *     /path/to/lib.a(object.o):
     *
     * We keep only lines with exactly three whitespace-separated tokens whose
     * middle token is a single uppercase letter (defined global symbol type).
     */
    private fun extractGlobalDefinedSymbols(libPath: File): Set<String> {
        val output = run("xcrun", "nm", "-g", "--defined-only", libPath.absolutePath)
        val symbols = mutableSetOf<String>()
        for (line in output.lineSequence()) {
            val parts = line.trim().split(WHITESPACE_REGEX)
            if (parts.size == 3 && parts[1].length == 1 && parts[1][0].isUpperCase()) {
                symbols.add(parts[2])
            }
        }
        return symbols
    }

    /**
     * Locate llvm-objcopy. The tool is not bundled with Xcode — on macOS it
     * comes from Homebrew LLVM (installed by the setup-native CI action).
     */
    private fun findLlvmObjcopy(): String {
        // Homebrew LLVM (installed by CI, or manually via `brew install llvm`).
        runOrNull("brew", "--prefix", "llvm")?.let { prefix ->
            val candidate = File(prefix, "bin/llvm-objcopy")
            if (candidate.exists()) return candidate.absolutePath
        }
        // xcrun works on some local setups where the tool is registered.
        runOrNull("xcrun", "-f", "llvm-objcopy")?.let { path ->
            if (path.isNotEmpty()) return path
        }
        // Last resort: PATH.
        runOrNull("which", "llvm-objcopy")?.let { path ->
            if (path.isNotEmpty()) return path
        }
        error("llvm-objcopy not found. Install via: brew install llvm")
    }

    /**
     * Rename symbols in [libPath] according to [redefineSymsFile] and write
     * the result to [outputPath].
     *
     * Uses `llvm-objcopy --redefine-syms` which processes every object file
     * inside the archive individually and rewrites both the definitions and the
     * undefined (imported) references of each renamed symbol.
     */
    private fun patchLibrary(libPath: File, redefineSymsFile: File, outputPath: File, llvmObjcopy: String) {
        run(
            llvmObjcopy,
            "--redefine-syms=${redefineSymsFile.absolutePath}",
            libPath.absolutePath,
            outputPath.absolutePath,
        )
    }

    companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()

        private const val SKIKO_NS = "skiko"
        /** Itanium ABI source-name encoding: "5skiko" */
        private const val NS_ENCODING = "${SKIKO_NS.length}$SKIKO_NS" // "5skiko"
        /** CV-qualifiers (K=const, V=volatile, r=restrict) and ref-qualifiers (R=&, O=&&). */
        private val NESTED_QUALIFIERS = setOf('K', 'V', 'r', 'R', 'O')
        private val VTABLE_TAGS = setOf("TV", "TI", "TS", "TT")

        /**
         * Attempt to rewrite [sym] so that the `skiko` namespace is encoded directly
         * into the Itanium ABI mangled name. Returns `null` when the symbol shape is
         * too complex to rewrite safely without a full grammar parser.
         *
         * ### Handled forms
         *
         * **`__ZN [cv/ref-quals] <source-name|nested-N…>`**
         *
         * Nested name (class methods, functions in a namespace).
         * The namespace component is inserted after `N` (and after any leading
         * CV / ref qualifiers), becoming the outermost qualifier:
         *
         * ```
         * __ZN7SkPaint4MakeEv     →  __ZN5skiko7SkPaint4MakeEv
         *   demangles as: skiko::SkPaint::Make()
         *
         * __ZNK7SkPaint6getCapEv  →  __ZNK5skiko7SkPaint6getCapEv
         *   demangles as: skiko::SkPaint::getCap() const
         * ```
         *
         * **`__ZTV|TI|TS|TT <type>`**
         *
         * Vtable / typeinfo / typeinfo-name / VTT.
         *
         * Simple source-name type (digit-prefixed):
         * ```
         * __ZTV7SkPaint           →  __ZTVN5skiko7SkPaintE
         *   demangles as: vtable for skiko::SkPaint
         * ```
         *
         * Already-nested type (N…E):
         * ```
         * __ZTVN7SkPaint5InnerE   →  __ZTVN5skiko7SkPaint5InnerE
         *   demangles as: vtable for skiko::SkPaint::Inner
         * ```
         *
         * ### Limitations
         *
         * Symbols whose parameter types contain Itanium substitution back-references
         * (S_, S0_, …) will have those references displaced by +1 after namespace
         * insertion (the new 'skiko' namespace itself becomes substitution 0).
         * In practice this affects parameter *type* display in the demangled output
         * while leaving the function *name* readable.
         *
         * Symbols that start with a substitution (S_), template parameter (T), or
         * other complex production as their first qualifier are left to the suffix
         * fallback rather than risk silently producing wrong output.
         */
        internal fun tryMangleInNamespace(sym: String): String? {
            if (sym.startsWith("__ZN")) {
                val body = sym.substring(4)
                // Skip any leading CV-qualifiers and ref-qualifiers.
                var i = 0
                while (i < body.length && body[i] in NESTED_QUALIFIERS) {
                    i++
                }
                val cvQuals = body.substring(0, i)
                val rest = body.substring(i)
                // Only rewrite when the first qualifier is a source-name (digit) or a
                // further nested-name (N). Substitution references (S) and template
                // parameters (T) would need index-shifted rewrites — skip them.
                if (rest.isNotEmpty() && (rest[0].isDigit() || rest[0] == 'N')) {
                    return "__ZN$cvQuals$NS_ENCODING$rest"
                }
                return null
            }

            if (sym.startsWith("__Z") && sym.length >= 5) {
                val tag = sym.substring(3, 5)
                if (tag in VTABLE_TAGS) {
                    val typeEnc = sym.substring(5)
                    if (typeEnc.startsWith("N")) {
                        // Already-nested type: N<body> — insert ns after N.
                        return "__Z${tag}N$NS_ENCODING${typeEnc.substring(1)}"
                    }
                    if (typeEnc.isNotEmpty() && typeEnc[0].isDigit()) {
                        // Simple source-name type — wrap in N…E.
                        return "__Z${tag}N$NS_ENCODING${typeEnc}E"
                    }
                }
            }

            return null
        }

        /**
         * Rename [sym] into a `skiko`-namespaced form while preserving
         * demanglability for the Itanium-ABI mangled shapes we can rewrite.
         * For C++ Itanium-ABI mangled names the `skiko` namespace is encoded
         * directly into the mangled grammar, producing names that LLDB, c++filt,
         * Instruments, Crashlytics, and Sentry can decode:
         *
         * ```
         * __ZN7SkPaint4MakeEv    →  __ZN5skiko7SkPaint4MakeEv
         *                            ↳ skiko::SkPaint::Make()
         *
         * __ZNK7SkPaint6getCapEv →  __ZNK5skiko7SkPaint6getCapEv
         *                            ↳ skiko::SkPaint::getCap() const
         *
         * __ZTV7SkPaint          →  __ZTVN5skiko7SkPaintE
         *                            ↳ vtable for skiko::SkPaint
         * ```
         *
         * For plain C symbols (single leading `_`, no `__Z`) the suffix is
         * appended — C identifiers allow trailing underscores and the result
         * remains a legal C name:
         *
         * ```
         * _uloc_getDefault       →  _uloc_getDefault_skiko
         * ```
         *
         * Symbols with mangled shapes too complex to rewrite without a full
         * Itanium ABI parser also receive the suffix as a safe fallback.
         * That fallback preserves uniqueness and linker-valid naming, but for
         * complex C++ mangled inputs it does not guarantee that the result will
         * remain demangleable by standard tools.
         */
        internal fun renamed(sym: String, suffix: String = "_skiko"): String {
            return tryMangleInNamespace(sym) ?: (sym + suffix)
        }
    }
}
