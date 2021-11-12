plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(gradleApi())
    implementation(kotlin("gradle-plugin-api", "1.5.31"))
}
