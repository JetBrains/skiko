import org.gradle.kotlin.dsl.gradleKotlinDsl

plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("stdlib"))

    val jacksonVersion = "2.12.5"
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("io.ktor:ktor-client-okhttp:1.6.2")
    implementation("org.apache.tika:tika-parsers:1.24.1")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("de.undercouch:gradle-download-task:4.1.2")
}
