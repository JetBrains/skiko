{
    "targets": [
        {
            "target_name": "native_skiko",
            "sources": ["binding.cc"],
            "cflags_cc": ["-std=c++20"],
            "libraries": ["-framework OpenGL"],
            "xcode_settings": {
                "CLANG_CXX_LANGUAGE_STANDARD": "c++20",
                "CLANG_CXX_LIBRARY": "libc++",
                "GCC_ENABLE_CPP_EXCEPTIONS": "YES",
                "MACOSX_DEPLOYMENT_TARGET": "11.0"
            }
        }
    ]
}