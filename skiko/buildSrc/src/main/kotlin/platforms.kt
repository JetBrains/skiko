import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction
import java.util.*
import kotlin.collections.ArrayDeque
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import kotlin.collections.HashMap

enum class SkikoPlatform(val extends: SkikoPlatform? = null) {
    Common,
    Jvm(Common),
    NativeJs(Common),
    Js(NativeJs),
    Native(NativeJs),
    Linux(Native),
    LinuxX64(Linux),
    Darwin(Native),
    Macos(Darwin),
    MacosX64(Macos),
    MacosArm64(Macos),
    Ios(Darwin),
    IosX64(Ios),
    IosArm64(Ios);

    val id: String = name.first().toLowerCase() + name.substring(1)
}

class SkikoSourceSets(
    kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>,
    requestedPlatforms: Set<SkikoPlatform>,
) {
    private val platformSourceSets = HashMap<SkikoPlatform, PlatformSourceSets>()

    init {
        val allPlatforms = requestedPlatforms.withAllChildrenPlatforms().withAllBasePlatforms()
        for (platform in allPlatforms) {
            val main = kotlinSourceSets.maybeCreate("${platform.id}Main")
            val test = kotlinSourceSets.maybeCreate("${platform.id}Test")
            platformSourceSets[platform] = PlatformSourceSets(platform, main, test)
        }
        for (platform in allPlatforms) {
            val basePlatform = platform.extends ?: continue
            val basePlatformSourceSets = platformSourceSets[basePlatform]!!
            platformSourceSets[platform]!!.extends(basePlatformSourceSets)
        }
        println("Requested platforms: ${requestedPlatforms.joinToString { "'${it.id}'" }}")
        println("Configured platforms: ${allPlatforms.joinToString { "'${it.id}'" }}")
    }

    class PlatformSourceSets(
        val platform: SkikoPlatform,
        val main: KotlinSourceSet,
        val test: KotlinSourceSet
    ) {
        fun extends(other: PlatformSourceSets) {
            main.dependsOn(other.main)
            test.dependsOn(other.test)
        }
    }

    fun configureIfDefined(platform: SkikoPlatform, fn: PlatformSourceSets.() -> Unit) {
        platformSourceSets[platform]?.fn()
    }

    operator fun get(platform: SkikoPlatform): PlatformSourceSets? =
        platformSourceSets[platform]
}

fun Set<SkikoPlatform>.withAllChildrenPlatforms(): Set<SkikoPlatform> {
    val children = SkikoPlatform.values()
        .filter { it.extends != null }
        .groupBy { it.extends }
    val result = EnumSet.noneOf(SkikoPlatform::class.java)
    bfs(seed = this) { platform ->
        result.add(platform)
        children[platform]
    }
    return result
}

fun Set<SkikoPlatform>.withAllBasePlatforms(): Set<SkikoPlatform> {
    val result = EnumSet.noneOf(SkikoPlatform::class.java)
    bfs(seed = this) { platform ->
        result.add(platform)
        listOfNotNull(platform.extends)
    }
    return result
}

open class ListSkikoPlatformsTask : DefaultTask() {
    @TaskAction
    fun run() {
        val (root, nonRoot) = SkikoPlatform.values().partition { it.extends == null }
        val children = nonRoot.groupBy { it.extends }
        dfs(seed = root) { platform, level ->
            val indent = if (level == 0) "" else "   ".repeat(level - 1) + "|--"
            logger.warn(indent + platform.id)
            children[platform]
        }
    }
}

private fun bfs(seed: Collection<SkikoPlatform>, next: (SkikoPlatform) -> Collection<SkikoPlatform>?) {
    val queue = ArrayDeque(seed)
    val visited = EnumSet.noneOf(SkikoPlatform::class.java)
    while (queue.isNotEmpty()) {
        val platform = queue.removeFirst()
        if (visited.add(platform)) {
            queue.addAll(next(platform) ?: continue)
        }
    }
}

private fun dfs(seed: Collection<SkikoPlatform>, next: (SkikoPlatform, level: Int) -> Collection<SkikoPlatform>?) {
    val stack = ArrayDeque(seed.map { it to 0 })
    val visited = EnumSet.noneOf(SkikoPlatform::class.java)
    while (stack.isNotEmpty()) {
        val (platform, level) = stack.removeFirst()
        if (visited.add(platform)) {
            val nextElements = next(platform, level) ?: continue
            stack.addAll(nextElements.map { it to level + 1 })
        }
    }
}
