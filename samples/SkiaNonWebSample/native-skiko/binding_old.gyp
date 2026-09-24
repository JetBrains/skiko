{
    "variables": {
        "skiko_root": "<(module_root_dir)/../../skiko",
        "skia_version": "m2-2cf7502f5b",
        "skiko_arch": "<!(node -p \"process.arch === 'arm64' ? 'arm64' : 'x64'\")",
        "skia_root": "<(skiko_root)/dependencies/skia/<(skia_version)/Skia-<(skia_version)-macos-Release-<(skiko_arch)"
    },

    "targets": [
        {
            "target_name": "native_skiko",
            "sources": ["binding.cc"],
            "cflags_cc": ["-std=c++20"],
            "libraries": [
                "/Users/dustin.feucht/projects/skiko/skiko/build/nativeBridges/static/macos-arm64/skiko-native-bridges-macos-arm64.a",
                "/Users/dustin.feucht/projects/skiko/skiko/dependencies/skia/m2-2cf7502f5b/Skia-m2-2cf7502f5b-macos-Release-arm64/out/Release-macos-arm64/libskia.a",
                "/Users/dustin.feucht/projects/skiko/skiko/dependencies/skia/m2-2cf7502f5b/Skia-m2-2cf7502f5b-macos-Release-arm64/out/Release-macos-arm64/libsvg.a"
            ],
            "xcode_settings": {
                "CLANG_CXX_LANGUAGE_STANDARD": "c++20",
                "CLANG_CXX_LIBRARY": "libc++",
                "GCC_ENABLE_CPP_EXCEPTIONS": "YES",
                "MACOSX_DEPLOYMENT_TARGET": "11.0"
            }
        }
    ]
}