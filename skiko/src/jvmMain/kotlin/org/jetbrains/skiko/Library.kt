package org.jetbrains.skiko

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Library {
    private var loaded = false

    private val cacheDir = "${System.getProperty("user.home")}/.skiko/"

    private fun loadOrGet(path: String, resource: String, isLibrary: Boolean) {
        File(cacheDir).mkdirs()
        val resourceName = if (isLibrary) System.mapLibraryName(resource) else resource
        val hash = Library::class.java.getResourceAsStream("$path$resourceName.sha256").use {
            BufferedReader(InputStreamReader(it)).lines().toArray()[0] as String
        }

        val fileName = if (isLibrary) System.mapLibraryName(hash) else hash
        val file = File(cacheDir, fileName)
        // TODO: small race change when multiple Compose apps are started first time, can handle with atomic rename.
        if (!file.exists()) {
            Library::class.java.getResourceAsStream("$path$resourceName").use { input ->
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
        if (isLibrary) {
            System.load(file.absolutePath)
        }
    }

    @Synchronized
    fun load(resourcePath: String, name: String) {
        if (loaded) return

        loadOrGet(resourcePath, name, true)
        val loadIcu = System.getProperty("os.name").toLowerCase().startsWith("win")
        if (loadIcu) {
            loadOrGet(resourcePath, "icudtl.dat", true)
        }
        loaded = true

        // we have to set this property to avoid render flickering.
        System.setProperty("sun.awt.noerasebackground", "true")
    }
}