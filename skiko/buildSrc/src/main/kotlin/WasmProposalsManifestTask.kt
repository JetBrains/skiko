import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class WasmProposalsManifestTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val optimizer: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val wasmFile: RegularFileProperty

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val wasm = wasmFile.get().asFile
        val features = detectFeaturesViaWasmOpt(wasm)

        val detected = features.mapNotNull { feature ->
            val proposal = featureToProposal(feature) ?: return@mapNotNull null
            val chrome = chromeMin(proposal)
            val firefox = firefoxMin(proposal)
            val safari = safariMin(proposal)
            ProposalEntry(proposal, chrome, firefox, safari)
        }.distinctBy { it.name }.sortedWith(
            compareBy<ProposalEntry> { it.chromeMin.toVersionNumber() }
                .thenBy { it.firefoxMin.toVersionNumber() }
                .thenBy { it.safariMin.toVersionNumber() }
        )

        // Fail if any detected proposal is missing browser support information
        val missingBrowserInfo = detected.filter { entry ->
            entry.chromeMin.isBlank() || entry.firefoxMin.isBlank() || entry.safariMin.isBlank()
        }
        if (missingBrowserInfo.isNotEmpty()) {
            val details = missingBrowserInfo.joinToString("\n") { entry ->
                val missing = buildList {
                    if (entry.chromeMin.isBlank()) add("chrome_min")
                    if (entry.firefoxMin.isBlank()) add("firefox_min")
                    if (entry.safariMin.isBlank()) add("safari_min")
                }
                "  - ${entry.name}: missing ${missing.joinToString(", ")}"
            }
            throw org.gradle.api.GradleException(
                "Detected proposals with missing browser support information:\n$details\n" +
                "Every detected proposal must have chrome_min, firefox_min, and safari_min versions. " +
                "Please update the browser version lookup tables in WasmProposalsManifestTask."
            )
        }

        val csv = buildString {
            appendLine("proposal,chrome_min,firefox_min,safari_min")
            detected.forEach { proposal ->
                appendLine("${proposal.name},${proposal.chromeMin},${proposal.firefoxMin},${proposal.safariMin}")
            }
        }

        val output = manifestFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(csv)
        logger.lifecycle("Wrote ${detected.size} proposals to ${output.name}")
    }

    private fun detectFeaturesViaWasmOpt(wasmFile: java.io.File): List<String> {
        val stdout = ByteArrayOutputStream()
        execOperations.exec {
            executable = optimizer.get()
            args = listOf(wasmFile.absolutePath, "--print-features", "--all-features")
            standardOutput = stdout
        }
        // Output lines look like: --enable-bulk-memory
        return stdout.toString().lines()
            .map { it.trim() }
            .filter { it.startsWith("--enable-") }
            .map { it.removePrefix("--enable-") }
    }

    /**
     * Maps wasm-opt feature names to proposal names used in the manifest.
     * Returns null for features that should be ignored (MVP features or features
     * without a corresponding proposal entry).
     */
    private fun featureToProposal(feature: String): String? = when (feature) {
        "bulk-memory" -> "bulk-memory-operations"
        "reference-types" -> "reference-types"
        "multivalue" -> "multi-value"
        "mutable-globals" -> "mutable-globals"
        "sign-ext" -> "sign-extension-ops"
        "nontrapping-float-to-int-conversions" -> "nontrapping-float-to-int"
        "simd" -> "simd"
        "tail-call" -> "tail-call"
        "typed-function-references" -> "typed-function-references"
        "exception-handling" -> "exception-handling"
        "gc" -> "gc"
        "multimemory" -> null  // not yet supported in Safari
        "memory64" -> null  // not yet supported in Safari
        "threads" -> "threads"
        "extended-const" -> "extended-const"
        "strings" -> "js-string-builtins"
        // Features that don't map to tracked proposals
        "relaxed-simd" -> null
        "fp16" -> null
        "shared-everything" -> null
        "nontrapping-float-to-int" -> null  // wasm-opt alias; canonical is nontrapping-float-to-int-conversions
        "stack-switching" -> null
        "bulk-memory-opt" -> null
        "call-indirect-overlong" -> null
        "custom-descriptors" -> null
        else -> {
            logger.warn("Unknown wasm-opt feature '${feature}', ignoring.")
            null
        }
    }

    // Browser minimum version data from https://github.com/WebAssembly/website/blob/main/features.json
    // Empty string means not yet shipped (flag-only or absent) — the task will fail if such a proposal is detected.
    private fun chromeMin(proposal: String): String = when (proposal) {
        "sign-extension-ops" -> "74"
        "nontrapping-float-to-int" -> "75"
        "multi-value" -> "85"
        "reference-types" -> "96"
        "bulk-memory-operations" -> "75"
        "simd" -> "91"
        "mutable-globals" -> "74"
        "tail-call" -> "112"
        "typed-function-references" -> "119"
        "gc" -> "119"
        "multiple-memories" -> "120"
        "exception-handling" -> "137"
        "legacy-exception-handling" -> "95"
        "extended-const" -> "114"
        "memory64" -> "133"
        "threads" -> "74"
        "js-string-builtins" -> "130"
        "js-promise-integration" -> "137"
        "branch-hinting" -> "137"
        else -> throw IllegalArgumentException("Unknown proposal '$proposal': no Chrome version data. Add it to chromeMin().")
    }

    private fun firefoxMin(proposal: String): String = when (proposal) {
        "sign-extension-ops" -> "62"
        "nontrapping-float-to-int" -> "64"
        "multi-value" -> "78"
        "reference-types" -> "79"
        "bulk-memory-operations" -> "79"
        "simd" -> "89"
        "mutable-globals" -> "61"
        "tail-call" -> "121"
        "typed-function-references" -> "120"
        "gc" -> "120"
        "multiple-memories" -> "125"
        "exception-handling" -> "131"
        "legacy-exception-handling" -> "100"
        "extended-const" -> "112"
        "memory64" -> "134"
        "threads" -> "79"
        "js-string-builtins" -> "134"
        "js-promise-integration" -> ""  // flag-only in Firefox
        "branch-hinting" -> ""  // flag-only in Firefox
        else -> throw IllegalArgumentException("Unknown proposal '$proposal': no Firefox version data. Add it to firefoxMin().")
    }

    private fun safariMin(proposal: String): String = when (proposal) {
        "sign-extension-ops" -> "14.1"
        "nontrapping-float-to-int" -> "15"
        "multi-value" -> "13.1"
        "reference-types" -> "15"
        "bulk-memory-operations" -> "15"
        "simd" -> "16.4"
        "mutable-globals" -> "13.1"
        "tail-call" -> "18.2"
        "typed-function-references" -> "18"
        "gc" -> "18.2"
        "exception-handling" -> "18.4"
        "legacy-exception-handling" -> "15.2"
        "extended-const" -> "17.4"
        "memory64" -> ""  // not yet in Safari
        "multiple-memories" -> ""  // not yet in Safari
        "threads" -> "14.1"
        "js-string-builtins" -> "26.2"
        "js-promise-integration" -> ""  // flag-only in Safari
        "branch-hinting" -> "16"
        else -> throw IllegalArgumentException("Unknown proposal '$proposal': no Safari version data. Add it to safariMin().")
    }

    /**
     * Converts a version string like "74", "14.1", "18.2" to a comparable Double.
     * Empty string maps to [Double.MAX_VALUE] so unsupported proposals sort last.
     */
    private fun String.toVersionNumber(): Double =
        if (isBlank()) Double.MAX_VALUE else toDoubleOrNull() ?: Double.MAX_VALUE

    data class ProposalEntry(
        val name: String,
        val chromeMin: String,
        val firefoxMin: String,
        val safariMin: String
    )
}
