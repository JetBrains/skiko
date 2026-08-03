package tasks.configuration

import Arch
import CompileSkikoCppTask
import LinkSkikoTask
import OS
import SkikoProjectContext
import compilerForTarget
import isCompatibleWithHost
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import projectDirs
import registerOrGetSkiaDirProvider
import registerSkikoTask
import supportRoboVMIosArm64
import supportRoboVMIosSimulatorArm64
import java.io.File

/**
 * RoboVM targets build infrastructure.
 *
 * The Skia binaries are shared with the Kotlin/Native iOS targets
 * (see [SkikoProjectContext.declareSkiaTasks]: "ios"/"iosSim" configs).
 */

/** Name of the Kotlin/JVM target running via RoboVM on iOS devices and simulators. */
const val ROBOVM_IOS_TARGET_NAME = "iosRobovm"

/** All RoboVM Kotlin target names; used to carve them out of the "jvm" source set group. */
val robovmIosTargetNames = setOf(ROBOVM_IOS_TARGET_NAME)

const val ROBOVM_MAVEN_GROUP = "com.robovmx"
const val ROBOVM_DEFAULT_VERSION = "10.2.2.5-SNAPSHOT"

/** RoboVM version, overridable with the `dependencies.robovm` gradle property. */
val Project.robovmVersion: String
    get() = findProperty("dependencies.robovm")?.toString() ?: ROBOVM_DEFAULT_VERSION

fun Project.robovmDependency(artifactId: String): String = "$ROBOVM_MAVEN_GROUP:$artifactId:$robovmVersion"

/**
 * Build directory layout for the RoboVM artifacts of a single (arch, device/simulator) pair,
 * e.g. `build/robovm/iosSim-arm64`. Consumed by the bridge compile/link tasks (next phase).
 */
class RoboVmTargetDirs(
    project: Project,
    os: OS,
    arch: Arch,
    isSimulator: Boolean,
) {
    val targetString: String = "${os.idWithSuffix(isUikitSim = isSimulator)}-${arch.id}"

    /** Root for all RoboVM build artifacts of this target. */
    val rootDir: Provider<Directory> = project.layout.buildDirectory.dir("robovm/$targetString")

    /** Object files of the compiled JNI bridges. */
    val bridgesObjDir: Provider<Directory> = rootDir.map { it.dir("bridges/obj") }

    /** Static library with the skiko JNI bridges, linked into the app binary by RoboVM. */
    val bridgesStaticDir: Provider<Directory> = rootDir.map { it.dir("bridges/static") }

    /** Native libraries (Skia + bridges) packaged for RoboVM consumers. */
    val nativeLibsDir: Provider<Directory> = rootDir.map { it.dir("libs") }
}

fun SkikoProjectContext.robovmDirsFor(os: OS, arch: Arch, isSimulator: Boolean): RoboVmTargetDirs =
    RoboVmTargetDirs(project, os, arch, isSimulator)

/**
 * C++ source roots that are compiled into the RoboVM JNI bridges (next phase).
 * Reuses the common and JVM (JNI) bindings, but not the AWT-specific ones.
 * RoboVM-specific bridge sources live in `src/robovmMain/cpp`.
 */
fun robovmBridgesSourceRoots(): List<String> = listOf(
    "src/commonMain/cpp/common",
    "src/jvmMain/cpp/common",
    "src/robovmMain/cpp",
)

/**
 * Configures a RoboVM Kotlin/JVM target: bytecode level, version generation,
 * RoboVM dependencies and the Skia binaries providers shared with the native iOS targets.
 */
fun SkikoProjectContext.configureRoboVmTarget(
    os: OS,
    target: KotlinJvmTarget,
): Unit = with(this.project) {
    if (!os.isCompatibleWithHost) return
    if (os != OS.IOS) throw GradleException("RoboVM is only supported for iOS, got: $os")

    // RoboVM sticks to Java 8 bytecode
    target.compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
    
    target.attributes {
        attribute(Attribute.of("skiko.robovmVariant", String::class.java), "ios")
    }

    target.generateVersion(os, Arch.Arm64, skiko)

    // RoboVM runtime and CocoaTouch bindings are provided by the RoboVM toolchain
    // of the consuming app, so they must not leak into the published dependencies.
    target.compilations.getByName("main").defaultSourceSet.dependencies {
        compileOnly(robovmDependency("robovm-rt"))
        compileOnly(robovmDependency("robovm-cocoatouch"))
    }
    target.compilations.getByName("test").defaultSourceSet.dependencies {
        compileOnly(robovmDependency("robovm-rt"))
        compileOnly(robovmDependency("robovm-cocoatouch"))
    }

    val linkTasks = mutableListOf<TaskProvider<LinkSkikoTask>>()
    if (project.supportRoboVMIosArm64) {
        linkTasks += registerRoboVmBridgesTasks(os, Arch.Arm64, isSimulator = false)
    }
    if (project.supportRoboVMIosSimulatorArm64) {
        linkTasks += registerRoboVmBridgesTasks(os, Arch.Arm64, isSimulator = true)
    }
    
    val libsTask = registerRoboVmLibsTask(linkTasks)
    
    val createRoboVmXmlTask = project.tasks.register("createRoboVmXml") {
        val xmlFile = project.layout.buildDirectory.file("robovm/robovm.xml")
        outputs.file(xmlFile)
        
        doLast {
            xmlFile.get().asFile.apply {
                parentFile.mkdirs()
                writeText("""
                    <config>
                        <libs>
                            <lib variant="simulator" force="true">libs_sim/libskiko.a</lib>
                            <lib variant="device" force="true">libs_iphone/libskiko.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskresources.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskresources.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskparagraph.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskparagraph.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskia.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskia.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libicu.a</lib>
                            <lib variant="device" force="false">libs_iphone/libicu.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libjsonreader.a</lib>
                            <lib variant="device" force="false">libs_iphone/libjsonreader.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskottie.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskottie.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libsvg.a</lib>
                            <lib variant="device" force="false">libs_iphone/libsvg.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libpng.a</lib>
                            <lib variant="device" force="false">libs_iphone/libpng.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libwebp_sse41.a</lib>
                            <lib variant="device" force="false">libs_iphone/libwebp_sse41.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libsksg.a</lib>
                            <lib variant="device" force="false">libs_iphone/libsksg.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskunicode_core.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskunicode_core.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskunicode_icu.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskunicode_icu.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libwebp.a</lib>
                            <lib variant="device" force="false">libs_iphone/libwebp.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libdng_sdk.a</lib>
                            <lib variant="device" force="false">libs_iphone/libdng_sdk.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libpiex.a</lib>
                            <lib variant="device" force="false">libs_iphone/libpiex.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libharfbuzz.a</lib>
                            <lib variant="device" force="false">libs_iphone/libharfbuzz.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libexpat.a</lib>
                            <lib variant="device" force="false">libs_iphone/libexpat.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libzlib.a</lib>
                            <lib variant="device" force="false">libs_iphone/libzlib.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libjpeg.a</lib>
                            <lib variant="device" force="false">libs_iphone/libjpeg.a</lib>
                            <lib variant="simulator" force="false">libs_sim/libskshaper.a</lib>
                            <lib variant="device" force="false">libs_iphone/libskshaper.a</lib>
                        </libs>
                        <forceLinkClasses>
                            <pattern>org.jetbrains.skia.AnimationFrameInfo</pattern>
                            <pattern>org.jetbrains.skia.Color4f</pattern>
                            <pattern>org.jetbrains.skia.Drawable</pattern>
                            <pattern>org.jetbrains.skia.FontFamilyName</pattern>
                            <pattern>org.jetbrains.skia.FontFeature</pattern>
                            <pattern>org.jetbrains.skia.FontMgr</pattern>
                            <pattern>org.jetbrains.skia.FontVariation</pattern>
                            <pattern>org.jetbrains.skia.FontVariationAxis</pattern>
                            <pattern>org.jetbrains.skia.IPoint</pattern>
                            <pattern>org.jetbrains.skia.IRect</pattern>
                            <pattern>org.jetbrains.skia.ImageInfo</pattern>
                            <pattern>org.jetbrains.skia.PaintFilterCanvas</pattern>
                            <pattern>org.jetbrains.skia.Path</pattern>
                            <pattern>org.jetbrains.skia.PathSegment</pattern>
                            <pattern>org.jetbrains.skia.Point</pattern>
                            <pattern>org.jetbrains.skia.RRect</pattern>
                            <pattern>org.jetbrains.skia.RSXform</pattern>
                            <pattern>org.jetbrains.skia.Rect</pattern>
                            <pattern>org.jetbrains.skia.impl.Native</pattern>
                            <pattern>org.jetbrains.skia.paragraph.DecorationStyle</pattern>
                            <pattern>org.jetbrains.skia.paragraph.LineMetrics</pattern>
                            <pattern>org.jetbrains.skia.paragraph.Shadow</pattern>
                            <pattern>org.jetbrains.skia.paragraph.TextBox</pattern>
                            <pattern>org.jetbrains.skia.shaper.BidiRun</pattern>
                            <pattern>org.jetbrains.skia.shaper.FontMgrRunIterator</pattern>
                            <pattern>org.jetbrains.skia.shaper.FontRun</pattern>
                            <pattern>org.jetbrains.skia.shaper.HbIcuScriptRunIterator</pattern>
                            <pattern>org.jetbrains.skia.shaper.IcuBidiRunIterator</pattern>
                            <pattern>org.jetbrains.skia.shaper.LanguageRun</pattern>
                            <pattern>org.jetbrains.skia.shaper.RunHandler</pattern>
                            <pattern>org.jetbrains.skia.shaper.RunInfo</pattern>
                            <pattern>org.jetbrains.skia.shaper.ScriptRun</pattern>
                            <pattern>org.jetbrains.skia.shaper.ShapingOptions</pattern>
                            <pattern>org.jetbrains.skia.shaper.TextBlobBuilderRunHandler</pattern>
                            <pattern>org.jetbrains.skia.svg.SVGLength</pattern>
                            <pattern>org.jetbrains.skia.svg.SVGPreserveAspectRatio</pattern>
                            <pattern>org.jetbrains.skia.skottie.Logger</pattern>
                        </forceLinkClasses>
                        <frameworks>
                            <framework>Metal</framework>"
                            <framework>CoreGraphics</framework>"
                            <framework>CoreText</framework>"
                        </frameworks>
                    </config>
                """.trimIndent())
            }
        }
    }
    
    // Package the libs into the JAR
    val jarTaskName = target.artifactsTaskName
    project.tasks.named(jarTaskName, org.gradle.api.tasks.bundling.Jar::class.java) {
        dependsOn(libsTask, createRoboVmXmlTask)
        from(libsTask.map { it.outputs.files }) {
            into("META-INF/robovm/ios/")
        }
        from(createRoboVmXmlTask.map { it.outputs.files }) {
            into("META-INF/robovm/ios/")
        }
    }
}

fun SkikoProjectContext.registerRoboVmBridgesTasks(
    os: OS, arch: Arch, isSimulator: Boolean
): TaskProvider<LinkSkikoTask> {
    val skiaDirProvider = registerOrGetSkiaDirProvider(os, arch, isUikitSim = isSimulator)
    val targetDirs = robovmDirsFor(os, arch, isSimulator)
    
    return with(project) {
        val suffix = arch.id.capitalize() + if (isSimulator) "Sim" else ""
        val compileName = "compileRoboVmBridges$suffix"
        val linkName = "linkRoboVmBridges$suffix"
        
        val compileTask = registerSkikoTask<CompileSkikoCppTask>(compileName, os, arch) {
            dependsOn(skiaDirProvider)
            val unpackedSkia = skiaDirProvider.get()
            
            compiler.set(compilerForTarget(os, arch))
            buildTargetOS.set(os)
            if (isSimulator) buildSuffix.set("sim")
            buildTargetArch.set(arch)
            buildVariant.set(buildType)
            
            sourceRoots.set(projectDirs(*robovmBridgesSourceRoots().toTypedArray()))
            
            val jdkHome = File(System.getProperty("java.home") ?: error("'java.home' is null"))
            includeHeadersNonRecursive(jdkHome.resolve("include"))
            includeHeadersNonRecursive(jdkHome.resolve("include/darwin"))
            
            skiaHeadersDirs(unpackedSkia).forEach {
                includeHeadersNonRecursive(it)
            }
            
            val projectDir = project.projectDir
            includeHeadersNonRecursive(projectDir.resolve("src/awtMain/cpp/include"))
            includeHeadersNonRecursive(projectDir.resolve("src/jvmMain/cpp/common"))
            includeHeadersNonRecursive(projectDir.resolve("src/jvmMain/cpp/include"))
            includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
            
            val sdkRoot = findXcodeSdkRoot()
            val iphoneOsSdk = "$sdkRoot/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk"
            val iphoneSimSdk = "$sdkRoot/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk"
            val iosArchFlags = when (arch) {
                Arch.Arm64 -> arrayOf(
                    "-target", if (isSimulator) "arm64-apple-ios-simulator" else "arm64-apple-ios",
                    "-isysroot", if (isSimulator) iphoneSimSdk else iphoneOsSdk,
                    if (isSimulator) "-mios-simulator-version-min=12.0" else "-mios-version-min=12.0"
                )
                else -> throw GradleException("Unsupported arch: $arch")
            }
            
            flags.set(listOf(
                *iosArchFlags,
                *buildType.clangFlags,
                "-stdlib=libc++",
                *skiaPreprocessorFlags(OS.IOS, buildType),
            ))
            
            outDir.set(targetDirs.bridgesObjDir)
        }
        
        registerSkikoTask<LinkSkikoTask>(linkName, os, arch) {
            dependsOn(compileTask, skiaDirProvider)
            buildTargetOS.set(os)
            buildTargetArch.set(arch)
            if (isSimulator) buildSuffix.set("sim")
            buildVariant.set(buildType)
            
            linker.set("libtool")
            flags.set(listOf("-static"))
            
            objectFiles = project.fileTree(compileTask.flatMap { it.outDir }) { include("**/*.o") }
            libFiles = project.files() // Empty, so we don't pack Skia objects into libskiko.a
            
            outDir.set(targetDirs.bridgesStaticDir)
            libOutputFileName.set("libskiko.a")
        }
    }
}

fun SkikoProjectContext.registerRoboVmLibsTask(
    linkTasks: List<TaskProvider<LinkSkikoTask>>
): TaskProvider<out org.gradle.api.Task> = project.tasks.register("createRoboVmLibs") {
    dependsOn(linkTasks)
    
    val outDir = project.layout.buildDirectory.dir("robovm/packaged_libs").get().asFile
    outputs.dir(outDir)
    
    doLast {
        outDir.deleteRecursively()
        outDir.mkdirs()
        
        val libIphoneDir = File(outDir, "libs_iphone")
        val libSimDir = File(outDir, "libs_sim")
        libIphoneDir.mkdirs()
        libSimDir.mkdirs()
        
        // 1. Process skiko
        for (taskProvider in linkTasks) {
            val task = taskProvider.get()
            val taskOutDir = task.outDir.get().asFile
            val libFile = File(taskOutDir, "libskiko.a")
            if (task.buildSuffix.orNull == "sim") {
                libFile.copyTo(File(libSimDir, "libskiko.a"), overwrite = true)
            } else {
                libFile.copyTo(File(libIphoneDir, "libskiko.a"), overwrite = true)
            }
        }
        
        // 2. Process skia libraries
        for (taskProvider in linkTasks) {
            val task = taskProvider.get()
            val isSim = task.buildSuffix.orNull == "sim"
            val taskOs = task.buildTargetOS.get()
            val taskArch = task.buildTargetArch.get()
            val skiaDirProvider = registerOrGetSkiaDirProvider(taskOs, taskArch, isUikitSim = isSim)
            val buildType = task.buildVariant.get()
            val targetDirs = robovmDirsFor(taskOs, taskArch, isSim)
            val skiaOutDir = skiaDirProvider.get().resolve("out/${buildType.id}-${targetDirs.targetString}")
            
            val libs = skiaOutDir.listFiles { _, name -> name.startsWith("lib") && name.endsWith(".a") } ?: emptyArray()
            
            for (libFile in libs) {
                if (isSim) {
                    libFile.copyTo(File(libSimDir, libFile.name), overwrite = true)
                } else {
                    libFile.copyTo(File(libIphoneDir, libFile.name), overwrite = true)
                }
            }
        }
    }
}
