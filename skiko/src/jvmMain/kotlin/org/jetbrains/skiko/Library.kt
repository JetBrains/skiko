package org.jetbrains.skiko

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.UIManager

object Library {
    private var loaded = false

    private val cacheRoot = "${System.getProperty("user.home")}/.skiko/"

    private fun loadOrGet(cacheDir: File, path: String, resourceName: String, isLibrary: Boolean) {
        val file = File(cacheDir, resourceName)
        if (!file.exists()) {
            val tempFile = File.createTempFile("skiko", "", cacheDir)
            Library::class.java.getResourceAsStream("$path$resourceName").use { input ->
                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }
        if (isLibrary) {
            System.load(file.absolutePath)
        }
    }

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. Also, it could load additional
    // localization resource on platforms wher it is needed.
    @Synchronized
    fun load() {
        if (loaded) return

        val resourcePath = "/"
        val name = "skiko-$hostId"
        val platformName = System.mapLibraryName(name)

        val hash = Library::class.java.getResourceAsStream("$resourcePath$platformName.sha256").use {
            BufferedReader(InputStreamReader(it)).lines().toArray()[0] as String
        }
        val cacheDir = File("$cacheRoot/$hash")
        cacheDir.mkdirs()
        loadOrGet(cacheDir, resourcePath, platformName, true)
        val loadIcu = hostOs.isWindows
        if (loadIcu) {
            loadOrGet(cacheDir, resourcePath, "icudtl.dat", false)
        }

        miscSystemInit()

        try {
            // Init code executed after library was loaded.
            org.jetbrains.skija.impl.Library._nAfterLoad()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        loaded = true
    }

    // This function doesn't actually belong to this file.
    private fun miscSystemInit() {
        // we have to set this property to avoid render flickering.
        System.setProperty("sun.awt.noerasebackground", "false")
        System.setProperty("skija.staticLoad", "false")
        System.setProperty("skiko.renderApi", "RASTER")

        // setup menu look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            System.setProperty("apple.laf.useScreenMenuBar", "true")
        } catch (e: UnsupportedOperationException) {
            // Not all platforms allow this.
        }
    }
}
