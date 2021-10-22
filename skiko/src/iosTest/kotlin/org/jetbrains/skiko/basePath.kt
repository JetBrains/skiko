package org.jetbrains.skiko

import platform.Foundation.NSBundle

private val KEXE_DIR: String = NSBundle.mainBundle.bundlePath

internal actual fun basePath() = "$KEXE_DIR/../../../.."
