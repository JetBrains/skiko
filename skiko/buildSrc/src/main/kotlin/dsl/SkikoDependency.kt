package dsl

import Arch
import OS

enum class TargetEnv { JVM, NATIVE, WASM }
enum class Linkage { STATIC, DIRECT_STATIC, DYNAMIC }

data class ResolvedBinaryConfiguration(
    val staticLibBaseNames: List<String> = emptyList(),
    val staticArchivePaths: List<String> = emptyList(),
    val directStaticLibBaseNames: List<String> = emptyList(),
    val directStaticArchivePaths: List<String> = emptyList(),
    val dynamicLibNames: List<String> = emptyList(),
    val frameworks: List<String> = emptyList(),
    val linkFlags: List<String> = emptyList(),
)

@DslMarker
annotation class SkikoBinaryDsl

val OS.validEnvs: List<TargetEnv>
    get() = when (this) {
        OS.MacOS, OS.Linux -> listOf(TargetEnv.JVM, TargetEnv.NATIVE)
        OS.Windows, OS.Android -> listOf(TargetEnv.JVM)
        OS.IOS, OS.TVOS -> listOf(TargetEnv.NATIVE)
        OS.Wasm -> listOf(TargetEnv.WASM)
    }

class BinaryRegistry {

    private enum class RuleKind {
        STATIC_LIBS,
        DIRECT_STATIC_LIBS,
        DYNAMIC_LIBS,
        LINK_FLAGS,
        FRAMEWORKS,
    }

    private data class Rule(
        val os: OS,
        val arch: Arch?,
        val env: TargetEnv,
        val kind: RuleKind,
        val values: List<String>,
    ) {
        fun matches(os: OS, arch: Arch, env: TargetEnv, kind: RuleKind): Boolean =
            this.os == os && (this.arch == null || this.arch == arch) && this.env == env && this.kind == kind
    }

    private val rules = mutableListOf<Rule>()

    fun on(
        os: OS,
        env: TargetEnv,
        linkage: Linkage,
        vararg libs: String,
        arch: Arch? = null
    ) {
        rules.add(Rule(os, arch, env, linkage.ruleKind, libs.toList()))
    }

    fun linkFlags(os: OS, env: TargetEnv, vararg flags: String, arch: Arch? = null) {
        rules.add(Rule(os, arch, env, RuleKind.LINK_FLAGS, flags.toList()))
    }

    fun frameworks(os: OS, env: TargetEnv, vararg frameworks: String, arch: Arch? = null) {
        rules.add(Rule(os, arch, env, RuleKind.FRAMEWORKS, frameworks.toList()))
    }

    fun getLibs(os: OS, arch: Arch, env: TargetEnv, linkage: Linkage): List<String> =
        valuesFor(os, arch, env, linkage.ruleKind)

    fun getLinkFlags(os: OS, arch: Arch, env: TargetEnv): List<String> =
        valuesFor(os, arch, env, RuleKind.LINK_FLAGS)

    fun getFrameworks(os: OS, arch: Arch, env: TargetEnv): List<String> =
        valuesFor(os, arch, env, RuleKind.FRAMEWORKS).flatMap { listOf("-framework", it) }

    private fun valuesFor(os: OS, arch: Arch, env: TargetEnv, kind: RuleKind): List<String> =
        rules
            .filter { it.matches(os, arch, env, kind) }
            .flatMap { it.values }

    private val Linkage.ruleKind: RuleKind
        get() = when (this) {
            Linkage.STATIC -> RuleKind.STATIC_LIBS
            Linkage.DIRECT_STATIC -> RuleKind.DIRECT_STATIC_LIBS
            Linkage.DYNAMIC -> RuleKind.DYNAMIC_LIBS
        }
}

@SkikoBinaryDsl
interface ActionScope {
    fun staticSkiaLibs(vararg libs: String)
    fun directStaticSkiaLibs(vararg libs: String)
    fun dynamicSystemLibs(vararg libs: String)
    fun linkFlags(vararg flags: String)
    fun frameworks(vararg frameworks: String)
}

@SkikoBinaryDsl
class ArchScope internal constructor(
    private val registry: BinaryRegistry,
    private val env: TargetEnv,
    private val os: OS,
    private val arch: Arch
) : ActionScope {

    override fun staticSkiaLibs(vararg libs: String) =
        registry.on(os, env, Linkage.STATIC, *libs, arch = arch)

    override fun directStaticSkiaLibs(vararg libs: String) =
        registry.on(os, env, Linkage.DIRECT_STATIC, *libs, arch = arch)

    override fun dynamicSystemLibs(vararg libs: String) =
        registry.on(os, env, Linkage.DYNAMIC, *libs, arch = arch)

    override fun linkFlags(vararg flags: String) =
        registry.linkFlags(os, env, *flags, arch = arch)

    override fun frameworks(vararg frameworks: String) =
        registry.frameworks(os, env, *frameworks, arch = arch)
}

@SkikoBinaryDsl
class OSScope internal constructor(
    private val registry: BinaryRegistry,
    private val env: TargetEnv,
    private val os: OS
) : ActionScope {

    fun x64(configure: ArchScope.() -> Unit) = ArchScope(registry, env, os, Arch.X64).configure()
    fun arm64(configure: ArchScope.() -> Unit) = ArchScope(registry, env, os, Arch.Arm64).configure()
    fun wasm(configure: ArchScope.() -> Unit) = ArchScope(registry, env, os, Arch.Wasm).configure()

    override fun staticSkiaLibs(vararg libs: String) =
        registry.on(os, env, Linkage.STATIC, *libs, arch = null)

    override fun directStaticSkiaLibs(vararg libs: String) =
        registry.on(os, env, Linkage.DIRECT_STATIC, *libs, arch = null)

    override fun dynamicSystemLibs(vararg libs: String) =
        registry.on(os, env, Linkage.DYNAMIC, *libs, arch = null)

    override fun linkFlags(vararg flags: String) =
        registry.linkFlags(os, env, *flags, arch = null)

    override fun frameworks(vararg frameworks: String) =
        registry.frameworks(os, env, *frameworks, arch = null)
}

@SkikoBinaryDsl
class EnvScope internal constructor(
    private val registry: BinaryRegistry,
    private val env: TargetEnv
) : ActionScope {

    //  Silently ignores combinations that don't make sense (e.g., jvm { ios { } })
    fun linux(configure: OSScope.() -> Unit) = applyIfValid(OS.Linux, configure)
    fun macos(configure: OSScope.() -> Unit) = applyIfValid(OS.MacOS, configure)
    fun windows(configure: OSScope.() -> Unit) = applyIfValid(OS.Windows, configure)
    fun android(configure: OSScope.() -> Unit) = applyIfValid(OS.Android, configure)
    fun ios(configure: OSScope.() -> Unit) = applyIfValid(OS.IOS, configure)
    fun tvos(configure: OSScope.() -> Unit) = applyIfValid(OS.TVOS, configure)
    fun wasm(configure: OSScope.() -> Unit) = applyIfValid(OS.Wasm, configure)

    private fun applyIfValid(os: OS, configure: OSScope.() -> Unit) {
        if (env in os.validEnvs) {
            OSScope(registry, env, os).configure()
        }
    }

    private fun forEachValidOs(action: (OS) -> Unit) {
        OS.values().filter { env in it.validEnvs }.forEach(action)
    }

    // ActionScope implementation (applies to all valid OSs for this Env, arch = null)
    override fun staticSkiaLibs(vararg libs: String) = forEachValidOs { os ->
        registry.on(os, env, Linkage.STATIC, *libs, arch = null)
    }

    override fun directStaticSkiaLibs(vararg libs: String) = forEachValidOs { os ->
        registry.on(os, env, Linkage.DIRECT_STATIC, *libs, arch = null)
    }

    override fun dynamicSystemLibs(vararg libs: String) = forEachValidOs { os ->
        registry.on(os, env, Linkage.DYNAMIC, *libs, arch = null)
    }

    override fun linkFlags(vararg flags: String) = forEachValidOs { os ->
        registry.linkFlags(os, env, *flags, arch = null)
    }

    override fun frameworks(vararg frameworks: String) = forEachValidOs { os ->
        registry.frameworks(os, env, *frameworks, arch = null)
    }
}

@SkikoBinaryDsl
class TargetScope internal constructor(
    private val registry: BinaryRegistry
) {
    fun all(configure: EnvScope.() -> Unit) {
        TargetEnv.values().forEach { env -> EnvScope(registry, env).configure() }
    }

    fun jvm(configure: EnvScope.() -> Unit) = EnvScope(registry, TargetEnv.JVM).configure()
    fun native(configure: EnvScope.() -> Unit) = EnvScope(registry, TargetEnv.NATIVE).configure()
    fun wasm(configure: EnvScope.() -> Unit) = EnvScope(registry, TargetEnv.WASM).configure()
}

@SkikoBinaryDsl
class SkikoDependencyScope internal constructor(
    private val registry: BinaryRegistry
) {
    internal var dependsOnCore: Boolean = false
        private set

    fun dependsOnCore() {
        dependsOnCore = true
    }

    fun targets(configure: TargetScope.() -> Unit) {
        TargetScope(registry).configure()
    }

    fun binary(configure: BinaryRegistry.() -> Unit) {
        registry.configure()
    }
}
