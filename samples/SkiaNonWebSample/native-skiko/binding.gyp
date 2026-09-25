{
    "targets": [
        {
            "target_name": "native_skiko",
            "sources": ["binding.cc"],
            "include_dirs": [
                "/Users/dustin.feucht/projects/skia"
            ],

            "cflags_cc": ["-std=c++20", "-Wunused-function", "-Wunused-member-function"],
            "libraries": [
                "/Users/dustin.feucht/projects/skia/out/Release-macos-arm64/libskia.a",

                "-framework OpenGL",
            ],
            "xcode_settings": {
                "CLANG_CXX_LANGUAGE_STANDARD": "c++20",
                "CLANG_CXX_LIBRARY": "libc++",
                "GCC_ENABLE_CPP_EXCEPTIONS": "YES",
                "MACOSX_DEPLOYMENT_TARGET": "11.0"
            },
            "defines": [
                "NDEBUG",
                "SK_GANESH",
                "SK_GL",
                "SK_BUILD_FOR_SKIKO",
                "SK_ASSUME_GL=1"
            ]
        }
    ]
}