import de.undercouch.gradle.tasks.download.Download
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

plugins {
    kotlin("multiplatform") version "1.3.72"
    `cpp-library`
    `maven-publish`
    id("org.gradle.crypto.checksum") version "1.1.0"
    id("de.undercouch.download") version "4.1.1"
}

val coroutinesVersion = "1.4.1"

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:1.116")
    }
    repositories {
        mavenCentral()
    }
}

val skiko = SkikoProperties(rootProject)
val debug = false
val buildType = if (debug) SkiaBuildType.DEBUG else SkiaBuildType.RELEASE

allprojects {
    group = "org.jetbrains.skiko"
    version = skiko.deployVersion
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

val skiaZip = run {
    val zipName = skiko.skiaReleaseForTargetOS + ".zip"
    val zipFile = skiko.dependenciesDir.resolve("skia/${zipName.substringAfterLast('/')}")

    tasks.register("downloadSkia", Download::class) {
        onlyIf { skiko.skiaDir == null && !zipFile.exists() }
        inputs.property("skia.release.for.target.os", skiko.skiaReleaseForTargetOS)
        src("https://github.com/JetBrains/skia-build/releases/download/$zipName")
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
}

val skiaDir = run {
    if (skiko.skiaDir != null) {
        tasks.register("skiaDir", DefaultTask::class) {
            // dummy task to simplify usage of the resulting provider (see `else` branch)
            // if a file provider is not created from a task provider,
            // then it cannot be used instead of a task in `dependsOn` clauses of other tasks.
            // e.g. the resulting `skiaDir` could not be used in `dependsOn` of CppCompile configuration
            enabled = false
        }.map { skiko.skiaDir!! }
    } else {
        val targetDir = skiko.dependenciesDir.resolve("skia/skia")
        tasks.register("unzipSkia", Copy::class) {
            from(skiaZip.map { zipTree(it) })
            configureSkiaCopy(targetDir)
        }.map { targetDir }
    }
}

val skijaZip = run {
    val zipFile = skiko.dependenciesDir.resolve("skija/${skiko.skijaCommitHash}.zip")

    tasks.register("downloadSkija", Download::class) {
        onlyIf { skiko.skijaDir == null && !zipFile.exists() }
        inputs.property("skija.commit.hash", skiko.skijaCommitHash)
        src("https://github.com/JetBrains/skija/archive/${skiko.skijaCommitHash}.zip")
        dest(zipFile)
        onlyIfModified(true)
    }.map { zipFile }
}

val skijaDir = run {
    if (skiko.skijaDir != null) {
        tasks.register("skijaDir", DefaultTask::class) {
            enabled = false
        }.map { skiko.skijaDir!! }
    } else {
        val skijaDest = skiko.dependenciesDir.resolve("skija/skija").apply { mkdirs() }
        tasks.register("unzipSkija", Copy::class) {
            from(skijaZip.map { zipTree(it) }) {
                include("skija-${skiko.skijaCommitHash}/**")
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
    lombok("org.projectlombok:lombok:1.18.16")
    jetbrainsAnnotations("org.jetbrains:annotations:19.0.0")
}
val skijaSrcDir = run {
    val delombokSkijaSrcDir = project.file("src/jvmMain/java")
    tasks.register("delombokSkija", JavaExec::class) {
        classpath = lombok + jetbrainsAnnotations
        main = "lombok.launch.Main"
        args("delombok", skijaDir.get().resolve("shared/src/main/java"), "-d", delombokSkijaSrcDir)
        inputs.dir(skijaDir)
        outputs.dir(delombokSkijaSrcDir)

        doFirst {
            delombokSkijaSrcDir.deleteRecursively()
            delombokSkijaSrcDir.mkdirs()
        }
        doLast {
            // Remove Library.java from Skija.
            file(delombokSkijaSrcDir.path + "/org/jetbrains/skija/impl/Library.java").delete()
        }
    }.map { delombokSkijaSrcDir }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
                compileOnly(lombok)
                compileOnly(jetbrainsAnnotations)
            }

        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation(kotlin("test-junit"))
            }
        }
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
        "-I$skiaDir/include/utils",
        "-I$skiaDir/modules/skottie/include",
        "-I$skiaDir/modules/skparagraph/include",
        "-I$skiaDir/modules/skshaper/include",
        "-I$skiaDir/modules/sksg/include",
        "-I$skiaDir/modules/svg/include",
        "-I$skiaDir/third_party/externals/harfbuzz/src",
        "-DSK_ALLOW_STATIC_GLOBAL_INITIALIZERS=1",
        "-DSK_FORCE_DISTANCE_FIELD_TEXT=0",
        "-DSK_GAMMA_APPLY_TO_A8",
        "-DSK_GAMMA_SRGB",
        "-DSK_SCALAR_TO_FLOAT_EXCLUDED",
        "-DSK_SUPPORT_GPU=1",
        "-DSK_GL",
        "-DSK_SHAPER_HARFBUZZ_AVAILABLE",
        "-DSK_SUPPORT_OPENCL=0",
        "-Dskija_EXPORTS",
        "-DSK_UNICODE_AVAILABLE",
        *buildType.flags
    ))
    val includeDir = "$projectDir/src/jvmMain/cpp/include"
    when (targetOs) {
        OS.MacOS -> {
            compilerArgs.addAll(
                listOf(
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-I$jdkHome/include/darwin",
                    "-I$includeDir",
                    "-DSK_SHAPER_CORETEXT_AVAILABLE",
                    "-DSK_BUILD_FOR_MAC",
                    "-DSK_METAL",
                    *buildType.clangFlags
                )
            )
        }
        OS.Linux -> {
            compilerArgs.addAll(
                listOf(
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-I$jdkHome/include/linux",
                    "-I$includeDir",
                    "-DSK_BUILD_FOR_LINUX",
                    "-DSK_R32_SHIFT=16",
                    *buildType.clangFlags
                )
            )
        }
        OS.Windows -> {
            compilerArgs.addAll(
                listOf(
                    "-I$jdkHome/include/win32",
                    "-I$includeDir",
                    "-DSK_BUILD_FOR_WIN",
                    "-D_CRT_SECURE_NO_WARNINGS",
                    "-D_HAS_EXCEPTIONS=0",
                    "-DWIN32_LEAN_AND_MEAN",
                    "-DNOMINMAX",
                    "-DSK_GAMMA_APPLY_TO_A8",
                    "-DSK_DIRECT3D",
                    "/utf-8",
                    "/GR-", // no-RTTI.
                    *buildType.msvcFlags
                    )
            )
        }
    }
}

// Very hacky way to compile Objective-C sources and add the
// resulting object files into the final library.
project.tasks.register<Exec>("objcCompile") {
    val inputDir = "$projectDir/src/jvmMain/objectiveC/${targetOs.id}"
    val outDir = "$buildDir/objc/$target"
    val names = File(inputDir).listFiles()!!.map { it.name.removeSuffix(".m") }
    val srcs = names.map { "$inputDir/$it.m" }.toTypedArray()
    val outs = names.map { "$outDir/$it.o" }.toTypedArray()
    workingDir = File(outDir)
    commandLine = listOf(
        "clang",
        "-mmacosx-version-min=10.13",
        "-I$jdkHome/include",
        "-I$jdkHome/include/darwin",
        "-c",
        *srcs
    )
    file(outDir).mkdirs()
    inputs.files(srcs)
    outputs.files(outs)
}

fun localSign(signer: String, lib: File) {
    println("Local signing $lib as $signer")
    val proc = ProcessBuilder("codesign", "-f", "-s", signer, lib.absolutePath)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor(5, TimeUnit.MINUTES)
    if (proc.exitValue() != 0) {
        val out = proc.inputStream.bufferedReader().readText()
        val err = proc.errorStream.bufferedReader().readText()
        println(out)
        println(err)
        throw GradleException("Cannot sign $lib: $err")
    }
}

fun remoteSign(sign_host: String, lib: File) {
    println("Remote signing $lib on $sign_host")
    val user = project.properties["sign_host_user"] as String
    val token = project.properties["sign_host_token"] as String
    val out = file("${lib.absolutePath}.signed")
    val cmd = """
        TOKEN=`curl -fsSL --user $user:$token --url "$sign_host/auth" | grep token | cut -d '"' -f4` \
        && curl --no-keepalive --data-binary @${lib.absolutePath} \
        -H "Authorization: Bearer ${'$'}TOKEN" \
        -H "Content-Type:application/x-mac-app-bin" \
        "$sign_host/sign?name=${lib.name}" -o "${out.absolutePath}"
    """.trimIndent()
    val proc = ProcessBuilder("bash", "-c", cmd)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor(5, TimeUnit.MINUTES)
    if (proc.exitValue() != 0) {
        val out = proc.inputStream.bufferedReader().readText()
        val err = proc.errorStream.bufferedReader().readText()
        println(out)
        println(err)
        throw GradleException("Cannot sign $lib: $err")
    } else {
        lib.delete()
        out.renameTo(lib)
    }
}

tasks.withType(LinkSharedLibrary::class.java).configureEach {
    when (targetOs) {
        OS.MacOS -> {
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
                    "-framework", "Metal",
                    "-framework", "OpenGL",
                    "-framework", "QuartzCore" // for CoreAnimation
                )
            )
        }
        OS.Linux -> {
            linkerArgs.addAll(
                listOf(
                    "-lGL",
                    "-lfontconfig",
                    // Hack to fix problem with linker not always finding certain declarations.
                    skiaDir.get().absolutePath + "/out/${buildType.id}-${targetArch.id}/libsksg.a"
                )
            )
        }
        OS.Windows -> {
            linkerArgs.addAll(
                listOf(
                    "gdi32.lib",
                    "Dwmapi.lib",
                    "opengl32.lib",
                    "shcore.lib",
                    "user32.lib",
                    "dxgi.lib",
                    "d3dcompiler.lib"
                )
            )
        }
    }

    doLast {
        val dylib = outputs.files.files.singleOrNull { it.name.endsWith(".dylib") }
        (project.properties["signer"] as String?)?.let { signer ->
            dylib?.let { lib ->
                localSign(signer, lib)
            }
        }
        (project.properties["sign_host"] as String?)?.let { sign_host ->
            dylib?.let { lib ->
                remoteSign(sign_host, lib)
            }
        }
    }
}

extensions.configure<CppLibrary> {
    source.from(
        skijaDir.map { fileTree(it.resolve("native/src")) },
        fileTree("$projectDir/src/jvmMain/cpp/common"),
        fileTree("$projectDir/src/jvmMain/cpp/${targetOs.id}")
    )
}

library {
    linkage.addAll(listOf(Linkage.SHARED))
    targetMachines.addAll(listOf(machines.macOS.x86_64, machines.linux.x86_64, machines.windows.x86_64))
    baseName.set("skiko-$target")

    dependencies {
        implementation(
            skiaDir.map {
                fileTree(it.resolve("out/${buildType.id}-${targetArch.id}"))
                    .matching { include(if (targetOs.isWindows) "**.lib" else "**.a") }
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
            skiko.visualStudioBuildToolsDir?.let {
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
    val linkTask = project.tasks.withType(LinkSharedLibrary::class.java).single { it.name.contains(buildType.id) }
    dependsOn(linkTask)
    files = linkTask.outputs.files.filter { it.isFile } +
            if (targetOs.isWindows) files(skiaDir.map { it.resolve("out/${buildType.id}-x64/icudtl.dat") }) else files()
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums")
}

val skikoJvmRuntimeJar by project.tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-$target")
    dependsOn(createChecksums)
    from(skikoJvmJar.map { zipTree(it.archiveFile) })
    val linkTask = project.tasks.withType(LinkSharedLibrary::class.java).single { it.name.contains(buildType.id) }
    from(linkTask.outputs.files.filter { it.isFile })
    if (targetOs.isWindows) {
        from(files(skiaDir.map { it.resolve("out/${buildType.id}-x64/icudtl.dat") }))
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

    tasks.named("clean").configure {
        doLast {
            delete(skiko.dependenciesDir)
            delete(project.file("src/jvmMain/java"))
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
            name = "ComposeRepo"
            url = uri(skiko.composeRepoUrl)
            credentials {
                username = skiko.composeRepoUserName
                password = skiko.composeRepoKey
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
            artifactId = SkikoArtifacts.commonArtifactId
            afterEvaluate {
                artifact(skikoJvmJar.map { it.archiveFile.get() })
            }
        }
        create<MavenPublication>("skikoJvmRuntime") {
            artifactId = SkikoArtifacts.runtimeArtifactIdFor(targetOs, targetArch)
            afterEvaluate {
                artifact(skikoJvmRuntimeJar.map { it.archiveFile.get() })
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    if (name == "compileTestKotlinJvm") {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}