package org.jetbrains.skia

/**
 * PathAddMode chooses how addPath() appends paths.
 * Adding one Path to another can extend the last contour or start a new contour.
 */
enum class PathAddMode {
    /**
     * Contours are appended to the destination path as new contours.
     */
    APPEND,
    
    /**
     * Extends the last contour of the destination path with the first contour
     * of the source path, connecting them with a line.
     */
    EXTEND
}
