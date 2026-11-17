package org.jetbrains.skia.svg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats

/**
 * Loads images that an [SVGDOM] does not decode on its own.
 *
 * Skia asks the provider while rendering, once per image, and the decoded bitmap stays with the
 * [SVGDOM] until that DOM is closed. [makeDataURI] decodes bitmap payloads embedded in the
 * document. It does not open files and it does not fetch URLs.
 */
class SVGResourceProvider internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        /**
         * Decodes base64 `data:image/` payloads embedded in the SVG.
         *
         * The IRI is read from `xlink:href`. A plain `href` on `<image>` is not an image source in this
         * Skia. The payload has to be `;base64,` encoded image bytes in a format Skia can decode, such
         * as PNG, JPEG, or WebP. A `data:image/svg+xml` payload, a data URI that is not base64, and a
         * payload that is not a bitmap are not drawn. Opacity on the `<image>` itself is ignored;
         * opacity on an ancestor group is applied.
         *
         * A name that is not a `data:image/` URI is not loaded. This includes `http`, `https`, and
         * `file` URLs, and relative paths such as `images/icon.png`. Nothing is fetched and nothing
         * is read from the filesystem.
         */
        fun makeDataURI(): SVGResourceProvider {
            Stats.onNativeCall()
            return SVGResourceProvider(SVGResourceProvider_nMakeDataURI())
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_svg_SVGResourceProvider__1nMakeDataURI")
// Needs ForceNativeThreadState: builds a proxy that can later decode image bytes, and drops any
// previous provider the caller replaces. Decoding itself happens on SVGDOM.render.
// Native call path: skresources::DataURIResourceProviderProxy::Make
@ForceNativeThreadState
private external fun SVGResourceProvider_nMakeDataURI(): NativePointer
