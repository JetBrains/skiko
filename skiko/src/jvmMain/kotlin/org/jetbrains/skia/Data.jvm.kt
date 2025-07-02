package org.jetbrains.skia

import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope

/**
 * Create a new dataref the file with the specified path.
 * If the file cannot be opened, this returns null.
 */
fun Data.Companion.makeFromFileName(path: String?): Data {
    Stats.onNativeCall()
    interopScope {
        return Data(Data_nMakeFromFileName(toInterop(path)))
    }
}