cmake_minimum_required(VERSION 3.24)

project(skiko_wasmtime LANGUAGES CXX)

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

find_package(SDL2 CONFIG QUIET)
if(TARGET SDL2::SDL2)
    set(SKIKO_SDL_TARGET SDL2::SDL2)
else()
    find_path(
        SDL2_INCLUDE_DIR
        SDL.h
        HINTS /opt/homebrew/include /usr/local/include
        PATH_SUFFIXES SDL2
    )
    find_library(
        SDL2_LIBRARY
        NAMES SDL2
        HINTS /opt/homebrew/lib /usr/local/lib
    )
    if(NOT SDL2_INCLUDE_DIR OR NOT SDL2_LIBRARY)
        message(FATAL_ERROR
            "SDL2 was not found. Install it with: brew install sdl2")
    endif()
    add_library(skiko_sdl2 UNKNOWN IMPORTED)
    set_target_properties(
        skiko_sdl2
        PROPERTIES
            IMPORTED_LOCATION "${SDL2_LIBRARY}"
            INTERFACE_INCLUDE_DIRECTORIES "${SDL2_INCLUDE_DIR}"
    )
    set(SKIKO_SDL_TARGET skiko_sdl2)
endif()

if(NOT EXISTS "${SKIA_LIBRARY}")
    message(FATAL_ERROR "SKIA_LIBRARY does not exist: ${SKIA_LIBRARY}")
endif()

add_executable(
    skiko-wasmtime
    src/main.cc
    src/kotlin_app_host.cc
    src/sdl_window.cc
    src/skiko_host.cc
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
        SDL_MAIN_HANDLED
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
        "${SKIKO_SDL_TARGET}"
        "-framework OpenGL"
)
