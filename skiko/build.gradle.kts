plugins {
    kotlin("multiplatform") version "1.4-M3"
    `cpp-library`
}
group = "org.jetbrains.skiko"
version = "1.0-SNAPSHOT"

val skiaDir = System.getenv("SKIA_DIR")

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
    val hostOs = System.getProperty("os.name")
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

tasks.withType(CppCompile::class.java).configureEach {
    val jdkHome = System.getenv("JAVA_HOME")
    compilerArgs.addAll(listOf(
        "-std=c++14",
        "-I$jdkHome/include",
        "-I$jdkHome/include/darwin",
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
        "-DSK_BUILD_FOR_MAC",
        "-DSK_FORCE_DISTANCE_FIELD_TEXT=0",
        "-DSK_GAMMA_APPLY_TO_A8",
        "-DSK_GAMMA_SRGB",
        "-DSK_INTERNAL",
        "-DSK_SCALAR_TO_FLOAT_EXCLUDED",
        "-DSK_SUPPORT_GPU=1",
        "-DSK_SUPPORT_OPENCL=0",
        "-Dskija_EXPORTS",
        "-O3",
        "-DNDEBUG"
    ))
}

tasks.withType(LinkSharedLibrary::class.java).configureEach {
    linkerArgs.addAll(listOf(
        "-framework", "CoreFoundation",
        "-framework", "CoreGraphics",
        "-framework", "CoreServices",
        "-framework", "CoreText"
    ))
}

library {
    linkage.addAll(listOf(Linkage.SHARED))
    targetMachines.addAll(listOf(machines.macOS.x86_64, machines.linux.x86_64))
    baseName.set("skiko")

    dependencies {
        implementation(fileTree("$skiaDir/out/Release-x64").matching {
            include("**.a")
        })
    }
}

tasks.register<Jar>("skiko") {
    archiveBaseName.set("skiko")
    from(kotlin.jvm().compilations["main"].output.allOutputs)
}