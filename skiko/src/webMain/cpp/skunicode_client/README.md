# Vendored `skunicode_client`

The prebuilt Skia distribution used by Skiko ships only `skunicode_core` and `skunicode_icu`;
there is no `skunicode_client` library and the distribution contains headers only.

`SkUnicode_client.cpp` and `SkBidiFactory_icu_subset.cpp` are therefore copied verbatim from
`skia/modules/skunicode/src/` and compiled as part of the Skiko wasm binary, so that
`SkUnicodes::Client::Make()` can be used from `SkUnicodeClient.cc`.

ICU remains linked: the client implementation delegates BiDi resolution (UAX #9) to
`SkBidiSubsetFactory`, which calls into the embedded ICU `ubidi_*` implementation, and inherits
character property lookups from `SkUnicodeHardCodedCharProperties` (`skunicode_core`).

Do not edit these files; re-copy them when the Skia version is updated.
