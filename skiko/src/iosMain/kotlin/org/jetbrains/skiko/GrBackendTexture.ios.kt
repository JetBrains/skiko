/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.skiko

import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.GrBackendTexture
import org.jetbrains.skia.impl.*
import platform.Metal.MTLTextureProtocol

fun GrBackendTexture.Companion.createFromMetalTexture(
    mtlTexture: MTLTextureProtocol,
    width: Int,
    height: Int
): GrBackendTexture {
    return try {
        Stats.onNativeCall()
        val ptr = mtlTexture.objcPtr()
        if (ptr == Native.NullPointer) {
            throw RuntimeException("Failed to GrBackendTexture::createFromMetalTexture($mtlTexture, $width, $height")
        }
        GrBackendTexture(_nCreateFromMetalTexture(ptr, width, height))
    } finally {
        reachabilityBarrier(mtlTexture)
    }
}

@ExternalSymbolName("org_jetbrains_skia_GrBackendTexture__1nCreateFromMetalTexture")
private external fun _nCreateFromMetalTexture(mtlTexturePtr: NativePointer, width: Int, height: Int): NativePointer
