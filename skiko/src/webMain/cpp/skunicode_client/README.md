# Vendored `skunicode_client`

The prebuilt Skia distribution used by Skiko ships only `skunicode_core` and `skunicode_icu`;
there is no `skunicode_client` library and the distribution contains headers only.

`SkUnicode_client.cpp` and `SkBidiFactory_icu_subset.cpp` are therefore copied from
`skia/modules/skunicode/src/` and compiled as part of the Skiko wasm binary, so that
`SkUnicodes::Client::Make()` can be used from `SkUnicodeClient.cc`.

The copies are guarded by `SKIKO_USE_ICU`:

* with ICU (default) `SkUnicode_client` delegates BiDi resolution to the ICU bidi subset
  (`SkBidiFactory_icu_subset.cpp`), exactly like upstream Skia;
* without ICU (`-Pskiko.wasm.useIcu=false`) `SkBidiFactory_icu_subset.cpp` compiles to nothing
  and BiDi resolution is served by `SkikoBidi.cpp`, a self-contained UAX #9 implementation, so
  that the resulting binary contains no ICU code or data at all.

ICU remains linked: the client implementation delegates BiDi resolution (UAX #9) to
`SkBidiSubsetFactory`, which calls into the embedded ICU `ubidi_*` implementation, and inherits
character property lookups from `SkUnicodeHardCodedCharProperties` (`skunicode_core`).

Do not edit these files; re-copy them when the Skia version is updated.
