plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "3.2.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(gradleApi())
}
