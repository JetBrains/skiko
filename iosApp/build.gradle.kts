plugins {
    id("org.jetbrains.gradle.apple.applePlugin") version "222.849-0.15.1"
}

apple {
    iosApp {
        productName = "kmmappcode"

        sceneDelegateClass = "SceneDelegate"
        launchStoryboard = "LaunchScreen"

        //productInfo["NSAppTransportSecurity"] = mapOf("NSAllowsArbitraryLoads" to true)
        //buildSettings.OTHER_LDFLAGS("")

        dependencies {
            implementation(project(":shared"))
        }
    }
}
