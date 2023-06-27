package org.jetbrains.skiko

data class OSVersion(
    val major: Int,
    val minor: Int = 0,
    val patch: Int = 0
) : Comparable<OSVersion> {

    init {
        require(major >= 0) { "Major version must be a positive number" }
        require(minor >= 0) { "Minor version must be a positive number" }
        require(patch >= 0) { "Patch version must be a positive number" }
    }

    override fun toString(): String = "$major.$minor.$patch"

    override fun compareTo(other: OSVersion): Int {
        if (major > other.major) return 1
        if (major < other.major) return -1
        if (minor > other.minor) return 1
        if (minor < other.minor) return -1
        if (patch > other.patch) return 1
        if (patch < other.patch) return -1

        return 0
    }
}
