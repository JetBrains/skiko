import kotlin.text.capitalize

plugins {
    kotlin("multiplatform") version "1.4-M3"
    `cpp-library`
    `objective-c`
    `maven-publish`
}

val isCIBuild = project.hasProperty("teamcity")
group = "org.jetbrains.skiko"
version = when {
    isCIBuild -> project.property("deploy.ci.version") as String
    else -> project.property("deploy.version") as String
}

val skiaDir = System.getenv("SKIA_DIR") ?: "/Users/igotti/compose/skija/third_party/skia"
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
            //kotlin.srcDirs.add(file("$projectDir/../skija/java_delombok"))
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                compileOnly("org.projectlombok:lombok:1.18.12")
                compileOnly("org.jetbrains:annotations:19.0.0")
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

// See https://docs.gradle.org/current/userguide/cpp_library_plugin.html.
tasks.withType(CppCompile::class.java).configureEach {
    source.from(
            fileTree("$projectDir/../skija/src/main/cc"),
            fileTree("$projectDir/src/jvmMain/cpp/common"),
            fileTree("$projectDir/src/jvmMain/cpp/$target")
    )

    // Prefer 'java.home' system property to simplify overriding from Intellij.
    // When used from command-line, it is effectively equal to JAVA_HOME.
    if (JavaVersion.current() < JavaVersion.VERSION_11) {
        error("JDK 11+ is required, but Gradle JVM is ${JavaVersion.current()}. " +
                "Check JAVA_HOME (CLI) or Gradle settings (Intellij).")
    }
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

/*
project.tasks.register<ObjectiveCCompile>("objcCompile") {
    source.from(fileTree("$projectDir/src/jvmMain/objectiveC"))
    compilerArgs.addAll(listOf(
            "-I$jdkHome/include",
            "-I$jdkHome/darwin"
    ))
    toolChain.set(object : Clang {
        override fun getPath(): MutableList<File> {
            return mutableListOf(file("/usr/bin"))
        }
    })
    doFirst {
        println("Exec!")
    }
}

project.tasks.register<Exec>("objcCompile") {
    commandLine = listOf("clang",
            "-c",
            "$projectDir/src/jvmMain/objectiveC/macos/drawcanvas.m",
            "$projectDir/src/jvmMain/objectiveC/macos/openglapi.m",
            "-o",
            "$buildDir/objc/"
    )
}*/

// See https://docs.gradle.org/current/userguide/cpp_library_plugin.html.
tasks.withType(LinkSharedLibrary::class.java).configureEach {
    when (target) {
        "macos" -> {
            // dependsOn(tasks.withType(ObjectiveCCompile::class.java))
            linkerArgs.addAll(
                listOf(
                    "-dead_strip",
                    "-framework", "CoreFoundation",
                    "-framework", "CoreGraphics",
                    "-framework", "CoreServices",
                    "-framework", "CoreText"
                )
            )
        }
        "linux" -> {
            linkerArgs.addAll(
                listOf(
                    "-L$jdkHome/lib",
                    "-ljawt",
                    "-lGL",
                    "-lfontconfig"
                )
            )
        }
        "windows" -> {
            linkerArgs.addAll(
                listOf(
                    "user32.lib",
                    "opengl32.lib"
                )
            )
        }
    }
}

library {
    linkage.addAll(listOf(Linkage.SHARED))
    targetMachines.addAll(listOf(machines.macOS.x86_64, machines.linux.x86_64, machines.windows.x86_64))
    baseName.set("skiko")

    dependencies {
        implementation(fileTree("$skiaDir/out/Release-x64").matching {
            if (target == "windows")
                include("**.lib")
            else
                include("**.a")
        })
    }
}

val skikoJvmJar: Provider<Jar> by tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-jvm")
    from(kotlin.jvm().compilations["main"].output.allOutputs)
}

val skikoJvmRuntimeJar by project.tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-$target")
    from(skikoJvmJar.map { zipTree(it.archiveFile) })
    from(project.tasks.named("linkRelease${target.capitalize()}").map { it.outputs.files.filter { it.isFile }})
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
