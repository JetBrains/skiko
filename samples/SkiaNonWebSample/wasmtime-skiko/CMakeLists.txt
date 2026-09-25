cmake_minimum_required(VERSION 3.24)

project(skiko_wasmtime LANGUAGES CXX OBJCXX)

if(NOT APPLE)
    message(FATAL_ERROR "The current proof of concept uses macOS CGL/OpenGL")
endif()

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(WASMTIME_ROOT "" CACHE PATH "Root of an unpacked Wasmtime C API release")
set(SKIA_ROOT "/Users/dustin.feucht/projects/skia" CACHE PATH "Skia source tree")
set(
    SKIA_LIBRARY
    "${SKIA_ROOT}/out/Release-macos-arm64-gl/libskia.a"
    CACHE FILEPATH
    "Release-mode Skia archive built with Ganesh and desktop GL"
)

find_path(
    WASMTIME_INCLUDE_DIR
    wasmtime.h
    HINTS "${WASMTIME_ROOT}/include"
    REQUIRED
)
find_library(
    WASMTIME_LIBRARY
    NAMES wasmtime
    HINTS "${WASMTIME_ROOT}/lib"
    REQUIRED
)

if(NOT EXISTS "${SKIA_LIBRARY}")
    message(FATAL_ERROR "SKIA_LIBRARY does not exist: ${SKIA_LIBRARY}")
endif()

add_executable(
    skiko-wasmtime
    src/main.cc
    src/kotlin_app_host.cc
    src/macos_window.mm
    src/skiko_host.cc
)

set_source_files_properties(
    src/macos_window.mm
    PROPERTIES COMPILE_OPTIONS "-fobjc-arc"
)

target_include_directories(
    skiko-wasmtime
    PRIVATE
        "${WASMTIME_INCLUDE_DIR}"
        "${SKIA_ROOT}"
        "${CMAKE_CURRENT_SOURCE_DIR}/src"
        "${CMAKE_CURRENT_SOURCE_DIR}/generated"
)

target_compile_definitions(
    skiko-wasmtime
    PRIVATE
        NDEBUG
        SK_GANESH
        SK_GL
        SK_BUILD_FOR_SKIKO
        SK_ASSUME_GL=1
        GL_SILENCE_DEPRECATION
)

target_compile_options(
    skiko-wasmtime
    PRIVATE
        -Wall
        -Wextra
        -Wpedantic
        -fno-rtti
)

target_link_libraries(
    skiko-wasmtime
    PRIVATE
        "${WASMTIME_LIBRARY}"
        "${SKIA_LIBRARY}"
        "-framework Cocoa"
        "-framework CoreGraphics"
        "-framework OpenGL"
)
