package org.jetbrains.skiko

/**
 * Asynchronous request to open a URI in system browser.
 * [uri] a universal resource identifier to open, exact set of supported APIs is platform dependent
 */
expect fun openUri(uri: String)