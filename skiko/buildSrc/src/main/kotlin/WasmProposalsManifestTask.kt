import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class WasmProposalsManifestTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val wasmFile: RegularFileProperty

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val wasm = wasmFile.get().asFile
        val bytes = wasm.readBytes()
        val detectedProposals = detectProposals(bytes)

        val detected = detectedProposals.filter { it.detected }.sortedWith(
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

    private fun proposalEntry(name: String, detected: Boolean) =
        ProposalEntry(name, detected, chromeMin(name), firefoxMin(name), safariMin(name))

    private fun detectProposals(bytes: ByteArray): List<ProposalEntry> {
        val entries = mutableListOf<ProposalEntry>()

        val sectionIds = parseSectionIds(bytes)
        val customSectionNames = parseCustomSectionNames(bytes)

        // ===== Finished proposals (spec 1.0 / 2.0 / 3.0) =====

        // Bulk memory operations (data count section id=12, or bulk-memory opcodes in code)
        val codeSection = findSection(bytes, 10)
        entries += proposalEntry(
            "bulk-memory-operations",
            12.toByte() in sectionIds || (codeSection != null && detectBulkMemoryOpcodes(codeSection))
        )

        // Reference types: externref (0x6F), ref.null (0xD0), ref.is_null (0xD1), ref.func (0xD2),
        // ref.eq (0xD3), table.get (0x25), table.set (0x26), table.grow/size/fill (0xFC prefixed),
        // select with type (0x1C)
        val typeSection = findSection(bytes, 1)
        if (typeSection != null || codeSection != null) {
            val hasExternref = typeSection != null && 0x6F.toByte() in typeSection
            val refOpcodes = codeSection != null && listOf(0xD0, 0xD1, 0xD2, 0xD3, 0x25, 0x26, 0x1C)
                .map { it.toByte() }.any { it in codeSection }
            entries += proposalEntry(
                "reference-types",
                hasExternref || refOpcodes
            )
        }

        // Multi-value: function types with >1 result
        if (typeSection != null) {
            entries += proposalEntry(
                "multi-value",
                detectMultiValue(typeSection)
            )
        }

        // Import/Export of mutable globals
        val importSection = findSection(bytes, 2)
        val exportSection = findSection(bytes, 7)
        val globalSection = findSection(bytes, 6)
        entries += proposalEntry(
            "mutable-globals",
            (importSection != null && detectMutableGlobalImport(importSection)) ||
                (exportSection != null && globalSection != null && detectMutableGlobalExport(bytes))
        )

        // Sign extension ops: 0xC0-0xC4
        if (codeSection != null) {
            val signExtOpcodes = listOf(0xC0, 0xC1, 0xC2, 0xC3, 0xC4).map { it.toByte() }
            entries += proposalEntry(
                "sign-extension-ops",
                signExtOpcodes.any { it in codeSection }
            )
        }

        // Non-trapping float-to-int: 0xFC 0x00-0x07
        if (codeSection != null) {
            entries += proposalEntry(
                "nontrapping-float-to-int",
                detectSaturatingConversions(codeSection)
            )
        }

        // Fixed-width SIMD: prefix byte 0xFD with sub-opcode < 0x100
        if (codeSection != null) {
            entries += proposalEntry(
                "simd",
                detectSIMD(codeSection)
            )
        }


        // Tail call: return_call (0x12), return_call_indirect (0x13), return_call_ref (0x15)
        if (codeSection != null) {
            entries += proposalEntry(
                "tail-call",
                0x12.toByte() in codeSection || 0x13.toByte() in codeSection || 0x15.toByte() in codeSection
            )
        }

        // Typed function references: call_ref (0x14), return_call_ref (0x15),
        // ref.as_non_null (0xD4), br_on_null (0xD5), br_on_non_null (0xD6)
        if (codeSection != null) {
            val typedFuncRefOpcodes = listOf(0x14, 0xD4, 0xD5, 0xD6).map { it.toByte() }
            entries += proposalEntry(
                "typed-function-references",
                typedFuncRefOpcodes.any { it in codeSection }
            )
        }

        // Exception handling (modern): tag section (id=13), try_table (0x1F), throw (0x08), throw_ref (0x0A)
        entries += proposalEntry(
            "exception-handling",
            13.toByte() in sectionIds ||
                (codeSection != null && listOf(0x1F, 0x08, 0x0A).map { it.toByte() }.any { it in codeSection })
        )

        // Legacy exception handling: try (0x06), catch (0x07), rethrow (0x09), delegate (0x18), catch_all (0x19)
        if (codeSection != null) {
            val legacyEhOpcodes = listOf(0x06, 0x07, 0x09, 0x18, 0x19).map { it.toByte() }
            entries += proposalEntry(
                "legacy-exception-handling",
                legacyEhOpcodes.any { it in codeSection }
            )
        }

        // GC proposal: struct/array type constructors (0x5E=array, 0x5F=struct, 0x50=sub, 0x51=rec)
        // plus GC-prefixed opcodes (0xFB prefix)
        entries += proposalEntry(
            "gc",
            (typeSection != null && detectGC(typeSection)) ||
                (codeSection != null && detectGCOpcodes(codeSection))
        )

        // Multiple memories: more than one memory definition + import
        val memorySection = findSection(bytes, 5)
        entries += proposalEntry(
            "multiple-memories",
            detectMultipleMemories(bytes, memorySection, importSection)
        )

        // Memory64: i64-indexed memory limits
        entries += proposalEntry(
            "memory64",
            memorySection != null && detectMemory64(memorySection)
        )

        // Threads and atomics: shared memory flag or 0xFE-prefixed atomic instructions
        entries += proposalEntry(
            "threads",
            (memorySection != null && detectSharedMemory(memorySection)) ||
                (codeSection != null && 0xFE.toByte() in codeSection)
        )

        // Extended constant expressions: i32/i64 add/sub/mul in global init expressions
        // Detected by finding arithmetic opcodes in global section init expressions
        entries += proposalEntry(
            "extended-const",
            globalSection != null && detectExtendedConst(globalSection)
        )

        // Branch hinting: custom section "metadata.code.branch_hint"
        entries += proposalEntry(
            "branch-hinting",
            "metadata.code.branch_hint" in customSectionNames
        )

        // ===== Active proposals (phase 3-5) that may appear in foreseeable future =====

        // JS String Builtins (phase 5/finished): detected via specific import module name "wasm:js-string"
        entries += proposalEntry(
            "js-string-builtins",
            importSection != null && detectImportModule(importSection, "wasm:js-string")
        )

        // JS Promise Integration (phase 5): detected via "wasm:js-promise" imports or suspending custom section
        entries += proposalEntry(
            "js-promise-integration",
            (importSection != null && detectImportModule(importSection, "wasm:js-promise")) ||
                "promise-integration" in customSectionNames
        )

        // Threads (phase 4) - already covered above

        // Compact Import Section (phase 4): custom section or compressed import encoding
        // Not directly detectable from binary format yet — will use a custom section marker if standardized

        // Note: wide-arithmetic, stack-switching, and custom-page-sizes are omitted because
        // they are phase 3-4 proposals not yet shipped in any browser and cannot be reliably
        // detected with simple byte scanning (false positives from operand bytes).

        return entries
    }

    // --- Binary parsing helpers ---

    private fun parseSectionIds(bytes: ByteArray): Set<Byte> {
        val ids = mutableSetOf<Byte>()
        var i = 8 // skip magic + version
        while (i < bytes.size) {
            val id = bytes[i]
            ids.add(id)
            i++
            val (size, consumed) = readLEB128(bytes, i)
            i += consumed + size.toInt()
        }
        return ids
    }

    private fun parseCustomSectionNames(bytes: ByteArray): Set<String> {
        val names = mutableSetOf<String>()
        var i = 8
        while (i < bytes.size) {
            val id = bytes[i].toInt() and 0xFF
            i++
            val (size, consumed) = readLEB128(bytes, i)
            i += consumed
            if (id == 0 && size > 0) {
                // Custom section: first field is a name (length-prefixed UTF-8 string)
                val (nameLen, nameConsumed) = readLEB128(bytes, i)
                val nameStart = i + nameConsumed
                if (nameStart + nameLen.toInt() <= i + size.toInt()) {
                    names.add(String(bytes, nameStart, nameLen.toInt(), Charsets.UTF_8))
                }
            }
            i += size.toInt()
        }
        return names
    }

    private fun findSection(bytes: ByteArray, sectionId: Int): ByteArray? {
        var i = 8
        while (i < bytes.size) {
            val id = bytes[i].toInt() and 0xFF
            i++
            val (size, consumed) = readLEB128(bytes, i)
            i += consumed
            if (id == sectionId) {
                return bytes.copyOfRange(i, i + size.toInt())
            }
            i += size.toInt()
        }
        return null
    }

    private fun readLEB128(bytes: ByteArray, offset: Int): Pair<Long, Int> {
        var result = 0L
        var shift = 0
        var i = offset
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            i++
            if (b and 0x80 == 0) break
            shift += 7
        }
        return result to (i - offset)
    }

    private fun detectMultiValue(typeSection: ByteArray): Boolean {
        var i = 0
        val (count, consumed) = readLEB128(typeSection, i)
        i += consumed
        repeat(count.toInt()) {
            if (i >= typeSection.size) return false
            if (typeSection[i] == 0x60.toByte()) {
                i++
                val (paramCount, pc) = readLEB128(typeSection, i); i += pc
                i += paramCount.toInt()
                val (resultCount, rc) = readLEB128(typeSection, i); i += rc
                if (resultCount > 1) return true
                i += resultCount.toInt()
            }
        }
        return false
    }

    private fun detectMutableGlobalImport(importSection: ByteArray): Boolean {
        return importSection.indices.any { idx ->
            idx + 2 < importSection.size &&
            importSection[idx] == 0x03.toByte() &&
            importSection[idx + 2] == 0x01.toByte()
        }
    }

    private fun detectMutableGlobalExport(bytes: ByteArray): Boolean {
        // Simplified: check if global section has mutable globals and export section references globals
        val globalSection = findSection(bytes, 6) ?: return false
        val exportSection = findSection(bytes, 7) ?: return false
        // Global export kind = 0x03 in export section
        val hasGlobalExport = exportSection.indices.any { idx ->
            exportSection[idx] == 0x03.toByte()
        }
        // Mutable flag = 0x01 in global section
        val hasMutableGlobal = globalSection.indices.any { idx ->
            idx > 0 && globalSection[idx] == 0x01.toByte() &&
            (globalSection[idx - 1].toInt() and 0xFF) in listOf(0x7F, 0x7E, 0x7D, 0x7C, 0x7B, 0x70, 0x6F)
        }
        return hasGlobalExport && hasMutableGlobal
    }

    private fun detectSaturatingConversions(codeSection: ByteArray): Boolean {
        return codeSection.indices.any { idx ->
            codeSection[idx] == 0xFC.toByte() &&
            idx + 1 < codeSection.size &&
            (codeSection[idx + 1].toInt() and 0xFF) in 0..7
        }
    }

    private fun detectBulkMemoryOpcodes(codeSection: ByteArray): Boolean {
        // memory.init (0xFC 0x08), data.drop (0xFC 0x09), memory.copy (0xFC 0x0A),
        // memory.fill (0xFC 0x0B), table.init (0xFC 0x0C), elem.drop (0xFC 0x0D),
        // table.copy (0xFC 0x0E)
        return codeSection.indices.any { idx ->
            codeSection[idx] == 0xFC.toByte() &&
            idx + 1 < codeSection.size &&
            (codeSection[idx + 1].toInt() and 0xFF) in 8..14
        }
    }

    private fun detectSIMD(codeSection: ByteArray): Boolean {
        return 0xFD.toByte() in codeSection
    }


    private fun detectGCOpcodes(codeSection: ByteArray): Boolean {
        // GC instructions use 0xFB prefix
        return 0xFB.toByte() in codeSection
    }

    private fun detectMultipleMemories(bytes: ByteArray, memorySection: ByteArray?, importSection: ByteArray?): Boolean {
        var memoryCount = 0
        if (memorySection != null) {
            val (count, _) = readLEB128(memorySection, 0)
            memoryCount += count.toInt()
        }
        if (importSection != null) {
            // Count memory imports (import kind = 0x02)
            var i = 0
            val (importCount, consumed) = readLEB128(importSection, i)
            i += consumed
            repeat(importCount.toInt()) {
                if (i >= importSection.size) return@repeat
                // Skip module name
                val (modLen, mc) = readLEB128(importSection, i); i += mc + modLen.toInt()
                // Skip field name
                val (fieldLen, fc) = readLEB128(importSection, i); i += fc + fieldLen.toInt()
                if (i < importSection.size) {
                    val kind = importSection[i].toInt() and 0xFF
                    i++
                    if (kind == 0x02) memoryCount++
                    // Skip the import descriptor (simplified: skip based on kind)
                    i = skipImportDescriptor(importSection, i, kind)
                }
            }
        }
        return memoryCount > 1
    }

    private fun skipImportDescriptor(section: ByteArray, offset: Int, kind: Int): Int {
        var i = offset
        when (kind) {
            0x00 -> { // function: typeidx
                val (_, c) = readLEB128(section, i); i += c
            }
            0x01 -> { // table: reftype + limits
                i++ // reftype
                i = skipLimits(section, i)
            }
            0x02 -> { // memory: limits
                i = skipLimits(section, i)
            }
            0x03 -> { // global: valtype + mut
                i += 2
            }
            0x04 -> { // tag: attribute + typeidx
                i++ // attribute
                val (_, c) = readLEB128(section, i); i += c
            }
        }
        return i
    }

    private fun skipLimits(section: ByteArray, offset: Int): Int {
        var i = offset
        if (i >= section.size) return i
        val flags = section[i].toInt() and 0xFF
        i++
        val (_, c1) = readLEB128(section, i); i += c1 // min
        if (flags and 0x01 != 0) {
            val (_, c2) = readLEB128(section, i); i += c2 // max
        }
        return i
    }

    private fun detectSharedMemory(memorySection: ByteArray): Boolean {
        return memorySection.any { (it.toInt() and 0x02) != 0 }
    }

    private fun detectMemory64(memorySection: ByteArray): Boolean {
        return memorySection.any { (it.toInt() and 0x04) != 0 }
    }

    private fun detectGC(typeSection: ByteArray): Boolean {
        val gcMarkers = listOf(0x5E, 0x5F, 0x50, 0x51).map { it.toByte() }
        return gcMarkers.any { it in typeSection }
    }

    private fun detectExtendedConst(globalSection: ByteArray): Boolean {
        // Extended const expressions allow i32.add (0x6A), i32.sub (0x6B), i32.mul (0x6C),
        // i64.add (0x7C), i64.sub (0x7D), i64.mul (0x7E) in global init expressions.
        // These opcodes are normally not valid in const expressions (MVP only allows
        // i32.const, i64.const, f32.const, f64.const, ref.null, ref.func, global.get).
        // If we find arithmetic opcodes before an 0x0B (end) in the global section, it's extended const.
        val arithmeticOpcodes = setOf(0x6A.toByte(), 0x6B.toByte(), 0x6C.toByte(),
            0x7C.toByte(), 0x7D.toByte(), 0x7E.toByte())
        return arithmeticOpcodes.any { it in globalSection }
    }

    private fun detectImportModule(importSection: ByteArray, moduleName: String): Boolean {
        val target = moduleName.toByteArray(Charsets.UTF_8)
        var i = 0
        val (importCount, consumed) = readLEB128(importSection, i)
        i += consumed
        repeat(importCount.toInt()) {
            if (i >= importSection.size) return false
            val (modLen, mc) = readLEB128(importSection, i); i += mc
            if (modLen.toInt() == target.size && i + modLen.toInt() <= importSection.size) {
                val modBytes = importSection.copyOfRange(i, i + modLen.toInt())
                if (modBytes.contentEquals(target)) return true
            }
            i += modLen.toInt()
            // Skip field name
            val (fieldLen, fc) = readLEB128(importSection, i); i += fc + fieldLen.toInt()
            // Skip import kind + descriptor
            if (i < importSection.size) {
                val kind = importSection[i].toInt() and 0xFF
                i++
                i = skipImportDescriptor(importSection, i, kind)
            }
        }
        return false
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
        val detected: Boolean,
        val chromeMin: String,
        val firefoxMin: String,
        val safariMin: String
    )
}
