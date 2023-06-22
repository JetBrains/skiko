package org.jetbrains.skiko

internal fun isRunningOnJetBrainsRuntime() =
    System.getProperty("java.vendor")
        .equals("JetBrains s.r.o.", ignoreCase = true)
