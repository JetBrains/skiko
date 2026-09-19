plugins {
    org.jetbrains.dokka
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
}

dokka {
    dokkaPublications.html {
        moduleName.set("skiko")
    }
}

dependencies {
    dokka(project(":"))
    rootProject.findProject(":skiko-graphite")?.let {
        dokka(it)
    }
    dokka(project(":skiko-skottie"))
}
