import de.undercouch.gradle.tasks.download.Download
import kotlin.text.capitalize
import org.gradle.crypto.checksum.Checksum

plugins {
    kotlin("multiplatform") version "1.3.72"
    `cpp-library`
    `maven-publish`
    id("org.gradle.crypto.checksum") version "1.1.0"
    id("de.undercouch.download") version "4.1.1"
}

val properties = SkikoProperties(rootProject)

group = "org.jetbrains.skiko"
version = run {
    val suffix = if (properties.isRelease) "" else "-SNAPSHOT"
    properties.deployVersion + suffix
}

val hostOs = System.getProperty("os.name")
val target = when {
    hostOs == "Mac OS X" -> "macos"
    hostOs == "Linux" -> "linux"
    hostOs.startsWith("Win") -> "windows"
    else -> throw Error("Unknown os $hostOs")
}

val jdkHome = System.getProperty("java.home") ?: error("'java.home' is null")

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

val skiaZip = run {
    val zipName = properties.skiaReleaseForCurrentOS + ".zip"
    val zipFile = properties.dependenciesDir.resolve("skia/$zipName")

    tasks.register("downloadSkia", Download::class) {
        onlyIf { properties.skiaDir == null && !zipFile.exists() }
        inputs.property("skia.release.for.current.os", properties.skiaReleaseForCurrentOS)
        src("https://bintray.com/api/ui/download/jetbrains/skija/$zipName")
        dest(zipFile)
        onlyIfModified(true)
    }.map { zipFile }
}

fun String.insertAfterFirst(substring: String, stringToInsert: String): String =
    let { orig ->
        buildString {
            var i = orig.indexOf(substring)
            if (i < 0) return orig

            i += substring.length

            append(orig.substring(0, i))
            append(stringToInsert)
            append(orig.substring(i))
        }
    }

fun AbstractCopyTask.configureSkiaCopy(targetDir: File) {
    into(targetDir)
    if (target == "windows") {
        doLast {
            // temporary hack
            // todo: remove after https://github.com/google/skia/commit/0d6f81593b1fa222e8e4afb56cc961ce8c9be375 is included
            // in used version of skia
            val skPathRef = targetDir.resolve("include/private/SkPathRef.h")
            val skPathRefContent = skPathRef.readText()
            if ("#include <tuple>" !in skPathRefContent) {
                val includeToInsertAfter = "#include <limits>"
                check(includeToInsertAfter in skPathRefContent) { "Substring not found: '${includeToInsertAfter}' in $skPathRef" }
                val newContent = skPathRefContent.insertAfterFirst(includeToInsertAfter, "\n#include <tuple>")
                skPathRef.writeText(newContent)
            }
        }
    }
}

val skiaDir = run {
    val targetDir = properties.dependenciesDir.resolve("skia/skia")
    val taskProvider = if (properties.skiaDir != null) {
        tasks.register("syncSkia", Sync::class) {
            from(properties.skiaDir!!.absoluteFile)
            configureSkiaCopy(targetDir)
        }
    } else {
        tasks.register("unzipSkia", Copy::class) {
            from(skiaZip.map { zipTree(it) })
            configureSkiaCopy(targetDir)
        }
    }
    taskProvider.map { targetDir }
}

val skijaZip = run {
    val zipFile = properties.dependenciesDir.resolve("skija/${properties.skijaCommitHash}.zip")

    tasks.register("downloadSkija", Download::class) {
        onlyIf { properties.skijaDir == null && !zipFile.exists() }
        inputs.property("skija.commit.hash", properties.skijaCommitHash)
        src("https://github.com/JetBrains/skija/archive/${properties.skijaCommitHash}.zip")
        dest(zipFile)
        onlyIfModified(true)
    }.map { zipFile }
}

val skijaDir = run {
    if (properties.skijaDir != null) {
        tasks.register("unzipSkija", Copy::class) {
            enabled = false
        }.map { properties.skijaDir!! }
    } else {
        val skijaDest = properties.dependenciesDir.resolve("skija/skija").apply { mkdirs() }
        tasks.register("unzipSkija", Copy::class) {
            from(skijaZip.map { zipTree(it) }) {
                include("skija-${properties.skijaCommitHash}/**")
                eachFile {
                    // drop skija-<COMMIT> subdir
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                }
                includeEmptyDirs = false
            }
            into(skijaDest)
        }.map { skijaDest }
    }
}

val lombok by configurations.creating
val jetbrainsAnnotations by configurations.creating
dependencies {
    lombok("org.projectlombok:lombok:1.18.12")
    jetbrainsAnnotations("org.jetbrains:annotations:19.0.0")
}
val skijaSrcDir = run {
    val delombokSkijaSrcDir = project.file("src/jvmMain/java")
    tasks.register("delombokSkija", JavaExec::class) {
        classpath = lombok + jetbrainsAnnotations
        main = "lombok.launch.Main"
        args("delombok", skijaDir.get().resolve("src/main/java"), "-d", delombokSkijaSrcDir)
        inputs.dir(skijaDir)
        outputs.dirs(delombokSkijaSrcDir)
    }.map { delombokSkijaSrcDir }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }

    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            kotlin.srcDirs(skijaSrcDir)
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                compileOnly(lombok)
                compileOnly(jetbrainsAnnotations)
            }

        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    this.getOptions().compilerArgs.addAll(listOf("-source", "11", "-target", "11"))
}

// See https://docs.gradle.org/current/userguide/cpp_library_plugin.html.
tasks.withType(CppCompile::class.java).configureEach {
    // Prefer 'java.home' system property to simplify overriding from Intellij.
    // When used from command-line, it is effectively equal to JAVA_HOME.
    if (JavaVersion.current() < JavaVersion.VERSION_11) {
        error("JDK 11+ is required, but Gradle JVM is ${JavaVersion.current()}. " +
                "Check JAVA_HOME (CLI) or Gradle settings (Intellij).")
    }
    val jdkHome = System.getProperty("java.home") ?: error("'java.home' is null")
    dependsOn(skiaDir)
    val skiaDir = skiaDir.get().absolutePath
    compilerArgs.addAll(listOf(
        "-I$jdkHome/include",
        "-I$skiaDir",
        "-I$skiaDir/include",
        "-I$skiaDir/include/core",
        "-I$skiaDir/include/gpu",
        "-I$skiaDir/include/effects",
        "-I$skiaDir/include/pathops",
        "-I$skiaDir/modules/skparagraph/include",
        "-I$skiaDir/modules/skshaper/include",
        "-I$skiaDir/third_party/externals/harfbuzz/src",
        "-DSK_ALLOW_STATIC_GLOBAL_INITIALIZERS=1",
        "-DSK_FORCE_DISTANCE_FIELD_TEXT=0",
        "-DSK_GAMMA_APPLY_TO_A8",
        "-DSK_GAMMA_SRGB",
        "-DSK_SCALAR_TO_FLOAT_EXCLUDED",
        "-DSK_SUPPORT_GPU=1",
        "-DSK_SUPPORT_OPENCL=0",
        "-Dskija_EXPORTS",
        "-DNDEBUG"
    ))
    when (target) {
        "macos" -> {
            compilerArgs.addAll(
                listOf(
                    "-std=c++14",
                    "-O3",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-I$jdkHome/include/darwin",
                    "-DSK_BUILD_FOR_MAC"
                )
            )
        }
        "linux" -> {
            compilerArgs.addAll(
                listOf(
                    "-std=c++14",
                    "-O3",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-I$jdkHome/include/linux",
                    "-DSK_BUILD_FOR_LINUX",
                    "-DSK_R32_SHIFT=16"
                )
            )
        }
        "windows" -> {
            compilerArgs.addAll(
                listOf(
                    "-I$jdkHome/include/win32",
                    "-DSK_BUILD_FOR_WIN",
                    "-D_CRT_SECURE_NO_WARNINGS",
                    "-D_HAS_EXCEPTIONS=0",
                    "-DWIN32_LEAN_AND_MEAN",
                    "-DNOMINMAX",
                    "-DSK_GAMMA_APPLY_TO_A8",
                    "/utf-8",
                    "/O2",
                    "/GR-" // no-RTTI.
                )
            )
        }
    }
}

// Very hacky way to compile Objective-C sources and add the
// resulting object files into the final library.
project.tasks.register<Exec>("objcCompile") {
    val inputDir = "$projectDir/src/jvmMain/objectiveC/$target"
    val outDir = "$buildDir/objc/$target"
    val objcSrc = "drawlayer"
    commandLine = listOf(
        "clang",
        "-I$jdkHome/include",
        "-I$jdkHome/include/darwin",
        "-c",
        "$inputDir/$objcSrc.m",
        "-o",
        "$outDir/$objcSrc.o"
    )
    file(outDir).mkdirs()
    inputs.files("$inputDir/$objcSrc.m")
    outputs.files("$outDir/$objcSrc.o")
}

tasks.withType(LinkSharedLibrary::class.java).configureEach {
    when (target) {
        "macos" -> {
            dependsOn(project.tasks.named("objcCompile"))
            linkerArgs.addAll(
                listOf(
                    "-dead_strip",
                    "-framework", "AppKit",
                    "-framework", "CoreFoundation",
                    "-framework", "CoreGraphics",
                    "-framework", "CoreServices",
                    "-framework", "CoreText",
                    "-framework", "Foundation",
                    "-framework", "OpenGL",
                    "-framework", "QuartzCore" // for CoreAnimation
                )
            )
        }
        "linux" -> {
            linkerArgs.addAll(
                listOf(
                    "-lGL",
                    "-lfontconfig"
                )
            )
        }
        "windows" -> {
            linkerArgs.addAll(
                listOf(
                    "gdi32.lib",
                    "opengl32.lib",
                    "shcore.lib",
                    "user32.lib"
                )
            )
        }
    }
}

extensions.configure<CppLibrary> {
    source.from(
        skijaDir.map { fileTree(it.resolve("src/main/cc")) },
        fileTree("$projectDir/src/jvmMain/cpp/common"),
        fileTree("$projectDir/src/jvmMain/cpp/$target")
    )
}

library {
    linkage.addAll(listOf(Linkage.SHARED))
    targetMachines.addAll(listOf(machines.macOS.x86_64, machines.linux.x86_64, machines.windows.x86_64))
    baseName.set("skiko")

    dependencies {
        implementation(
            skiaDir.map {
                fileTree(it.resolve("out/Release-x64"))
                    .matching { include(if (target == "windows") "**.lib" else "**.a") }
            }
        )
        implementation(fileTree("$buildDir/objc/$target").matching {
             include("**.o")
        })
    }

    toolChains {
        withType(VisualCpp::class.java) {
            // In some cases Gradle is unable to find VC++ toolchain
            // https://github.com/gradle/gradle-native/issues/617
            properties.visualStudioBuildToolsDir?.let {
                setInstallDir(it)
            }
        }
    }
}

val skikoJvmJar: Provider<Jar> by tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-jvm")
    from(kotlin.jvm().compilations["main"].output.allOutputs)
}

val createChecksums by project.tasks.registering(org.gradle.crypto.checksum.Checksum::class) {
    val linkTask = project.tasks.named("linkRelease${target.capitalize()}")
    dependsOn(linkTask)
    files = linkTask.get().outputs.files.filter { it.isFile } +
            if (target == "windows") files(skiaDir.map { it.resolve("out/Release-x64/icudtl.dat") }) else files()
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums")
}

val skikoJvmRuntimeJar by project.tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-$target")
    dependsOn(createChecksums)
    from(skikoJvmJar.map { zipTree(it.archiveFile) })
    from(project.tasks.named("linkRelease${target.capitalize()}").map {
        it.outputs.files.filter { it.isFile }
    })
    if (target == "windows") {
        from(files(skiaDir.map { it.resolve("out/Release-x64/icudtl.dat") }))
    }
    from(createChecksums.get().outputs.files)
}

project.tasks.register<JavaExec>("run") {
    main = "org.jetbrains.skiko.MainKt"
    classpath = files(skikoJvmRuntimeJar.map { it.archiveFile })
}

// disable unexpected native publications (default C++ publications are failing)
tasks.withType<AbstractPublishToMaven>().configureEach {
    doFirst {
        if (!publication.name.startsWith("skiko")) {
            throw StopExecutionException("Publication '${publication.name}' is disabled")
        }
    }
}

fun Publication.isSkikoPublication() =
    (this as? MavenPublication)?.name?.startsWith("skiko") == true

fun Task.disable() {
    enabled = false
    group = "Disabled tasks"
}

afterEvaluate {
    tasks.configureEach {
        if (group == "publishing") {
            // There are many intermediate tasks in 'publishing' group.
            // There are a lot of them and they have verbose names.
            // To decrease noise in './gradlew tasks' output and Intellij Gradle tool window,
            // group verbose tasks in a separate group 'other publishing'.
            val allRepositories = publishing.repositories.map { it.name } + "MavenLocal"
            val publishToTasks = allRepositories.map { "publishTo$it" }
            if (name != "publish" && name !in publishToTasks) {
                group = "other publishing"
            }
        }
    }

    // Disable publishing Gradle Metadata, because it seems unnecessary at the moment.
    // Also generating metadata for default C++ publications fails
    tasks.withType(GenerateModuleMetadata::class).configureEach {
        disable()
    }

    // Disable publishing for default publications, because
    // publishing for default C++ publications fails
    tasks.withType<AbstractPublishToMaven>().configureEach {
        if (!publication.isSkikoPublication()) {
            disable()
        }
    }
}

publishing {
    repositories {
        configureEach {
            val repoName = name
            tasks.register("publishTo${repoName}") {
                group = "publishing"
                dependsOn(tasks.named("publishAllPublicationsTo${repoName}Repository"))
            }
        }
        maven {
            name = "BuildRepo"
            url = uri("${rootProject.buildDir}/repo")
        }
        maven {
            name = "Space"
            url = uri("https://packages.jetbrains.team/maven/p/ui/dev")
            credentials {
                username = System.getenv("SKIKO_SPACE_USERNAME")
                password = System.getenv("SKIKO_SPACE_KEY")
            }
        }
    }
    publications {
        configureEach {
            this as MavenPublication
            groupId = "org.jetbrains.skiko"
            pom {
                name.set("Skiko")
                description.set("Kotlin Skia bindings")
                url.set("http://www.github.com/JetBrains/skiko")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
        create<MavenPublication>("skikoJvm") {
            artifactId = "skiko-jvm"
            afterEvaluate {
                artifact(skikoJvmJar.map { it.archiveFile.get() })
            }
        }
        create<MavenPublication>("skikoJvmRuntime") {
            artifactId = "skiko-jvm-runtime-$target"
            afterEvaluate {
                artifact(skikoJvmRuntimeJar.map { it.archiveFile.get() })
            }
        }
    }
}

class SkikoProperties(private val myProject: Project) {
    val isCIBuild: Boolean
        get() = myProject.hasProperty("teamcity")

    val deployVersion: String
        get() = myProject.property("deploy.version") as String

    val isRelease: Boolean
        get() = myProject.findProperty("deploy.release") == "true"

    val skijaCommitHash: String
        get() = myProject.property("dependencies.skija.git.commit") as String

    val skiaReleaseForCurrentOS: String
        get() = (myProject.property("dependencies.skia.$target") as String)

    val visualStudioBuildToolsDir: File?
        get() = System.getenv()["SKIKO_VSBT_PATH"]?.let { File(it) }?.takeIf { it.isDirectory }

    val skijaDir: File?
        get() = System.getenv()["SKIJA_DIR"]?.let { File(it) }?.takeIf { it.isDirectory }

    val skiaDir: File?
        get() = (System.getenv()["SKIA_DIR"] ?: System.getProperty("skia.dir"))?.let { File(it) }?.takeIf { it.isDirectory }

    val dependenciesDir: File
        get() = myProject.rootProject.projectDir.resolve("dependencies")
}
