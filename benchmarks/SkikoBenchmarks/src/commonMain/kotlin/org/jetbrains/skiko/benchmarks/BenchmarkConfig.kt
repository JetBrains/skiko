package org.jetbrains.skiko.benchmarks

enum class BenchmarkMode(val argumentName: String, val resultSuffix: String?) {
    SIMPLE("SIMPLE", null),
    VSYNC_EMULATION("VSYNC_EMULATION", "vsync_emulation"),
    STARTUP("STARTUP", "startup");

    companion object {
        fun parseModes(value: String?): Set<BenchmarkMode> {
            if (value.isNullOrBlank()) return setOf(SIMPLE)

            return value
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { mode ->
                    entries.find { it.argumentName.equals(mode, ignoreCase = true) }
                        ?: throw IllegalArgumentException(
                            "Unsupported benchmark mode: $mode. Supported: ${entries.joinToString { it.argumentName }}"
                        )
                }
                .toSet()
                .ifEmpty { setOf(SIMPLE) }
        }
    }
}

data class BenchmarkConfig(
    val modes: Set<BenchmarkMode> = setOf(BenchmarkMode.SIMPLE),
    val benchmarks: Set<String>? = null,
    val versionInfo: String? = null,
    val warmupsOverride: Int? = null,
    val iterationsOverride: Int? = null,
) {
    companion object {
        fun fromArgs(args: Iterable<String>): BenchmarkConfig =
            BenchmarkConfig(
                modes = BenchmarkMode.parseModes(args.valueFor("modes")),
                benchmarks = args.valueFor("benchmarks")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.toSet(),
                versionInfo = args.valueFor("versionInfo"),
                warmupsOverride = args.valueFor("warmupCount")?.toInt(),
                iterationsOverride = args.valueFor("iterationCount")?.toInt()
                    ?: args.valueFor("frameCount")?.toInt(),
            )
    }
}

fun Iterable<String>.valueFor(key: String): String? =
    firstOrNull { it.startsWith("$key=", ignoreCase = true) }?.substringAfter("=")
