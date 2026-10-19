{
    "variables": {
        "skia_dir%": "<!(node -p \"process.env.SKIA_DIR || process.env.SKIA_ROOT || ''\")",
        "skia_library%": "<!(node -p \"process.env.SKIA_LIBRARY || (process.env.SKIA_DIR || process.env.SKIA_ROOT || '') + '/out/Release-macos-' + process.arch + '/libskia.a'\")"
    },
    "targets": [
        {
            "target_name": "native_skiko",
            "sources": ["binding.cc"],
            "include_dirs": [
                "<(skia_dir)",
                "<!(sdl2-config --prefix)/include/SDL2"
            ],

            "cflags_cc": [
                "-std=c++20",
                "-Wunused-function",
                "-Wunused-member-function"
            ],
            "libraries": [
                "<(skia_library)",
                "-L<!(sdl2-config --prefix)/lib",
                "-lSDL2",
                "-framework OpenGL"
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
                "SK_ASSUME_GL=1",
                "SDL_MAIN_HANDLED"
            ]
        }
    ]
}
