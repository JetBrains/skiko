#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
SKIKO_DIR="$ROOT_DIR/skiko"
OUT_DIR="$SKIKO_DIR/build/wine-smoke"

if ! command -v wine >/dev/null 2>&1; then
    echo "wine is required" >&2
    exit 1
fi

if ! command -v java >/dev/null 2>&1; then
    if [[ -z "${JAVA_HOME:-}" ]]; then
        JAVA_HOME=$(find "$HOME/.jdks" -maxdepth 3 -type f -path '*/bin/java' -printf '%h\n' 2>/dev/null \
            | sed 's#/bin$##' | sort -V | head -n 1 || true)
        export JAVA_HOME
    fi
    if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/java" ]]; then
        echo "A JDK is required; set JAVA_HOME" >&2
        exit 1
    fi
    export PATH="$JAVA_HOME/bin:$PATH"
fi

KOTLIN_VERSION=$(awk -F'"' '/^kotlin = / { print $2; exit }' "$ROOT_DIR/dependencies.toml")
KONAN_HOME=${KONAN_HOME:-"$HOME/.konan/kotlin-native-prebuilt-linux-x86_64-$KOTLIN_VERSION"}
KONANC="$KONAN_HOME/bin/konanc"

"$ROOT_DIR/gradlew" -p "$SKIKO_DIR" mingwX64MainKlibrary \
    -Pskiko.awt.enabled=false \
    -Pskiko.native.windows.enabled=true \
    --console=plain

if [[ ! -x "$KONANC" ]]; then
    echo "Kotlin/Native compiler not found at $KONANC" >&2
    exit 1
fi

SKIKO_KLIB="$SKIKO_DIR/build/classes/kotlin/mingwX64/main/klib/skiko"
ATOMICFU=$(find "$HOME/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/atomicfu-mingwx64/0.23.1" -name 'atomicfu.klib' -print -quit)
ATOMICFU_CINTEROP=$(find "$HOME/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/atomicfu-mingwx64/0.23.1" -name 'atomicfu-cinterop-interop.klib' -print -quit)
COROUTINES=$(find "$HOME/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core-mingwx64/1.8.0" -name 'kotlinx-coroutines-core.klib' -print -quit)

for dependency in "$SKIKO_KLIB" "$ATOMICFU" "$ATOMICFU_CINTEROP" "$COROUTINES"; do
    if [[ -z "$dependency" || ! -e "$dependency" ]]; then
        echo "Required KLIB is missing: $dependency" >&2
        exit 1
    fi
done

mkdir -p "$OUT_DIR"
cat > "$OUT_DIR/main.kt" <<'KOTLIN'
import org.jetbrains.skia.Color
import org.jetbrains.skiko.Version
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.WindowsNativeWindow
import org.jetbrains.skiko.currentSystemTheme
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.kotlinBackend

fun main() {
    val color = Color.makeRGB(0x12, 0x34, 0x56)
    check(Color.getR(color) == 0x12)
    check(Color.getG(color) == 0x34)
    check(Color.getB(color) == 0x56)

    val layer = SkiaLayer()
    check(layer.contentScale == 1f)
    check(currentSystemTheme in SystemTheme.entries)

    val window = WindowsNativeWindow.create("Skiko Wine smoke", 320, 200)
    check(window.isValid)
    window.close()

    println("skiko=${Version.skiko}")
    println("skia=${Version.skia}")
    println("host=${hostOs.id}-${hostArch.id}")
    println("backend=${kotlinBackend.id}")
    println("theme=${currentSystemTheme.name.lowercase()}")
    println("renderApi=${layer.renderApi.name.lowercase()}")
    println("windowHost=ok")
    println("color=${color.toUInt().toString(16)}")
}
KOTLIN

"$KONANC" \
    -target mingw_x64 \
    -produce program \
    -library "$SKIKO_KLIB" \
    -library "$ATOMICFU" \
    -library "$ATOMICFU_CINTEROP" \
    -library "$COROUTINES" \
    "$OUT_DIR/main.kt" \
    -o "$OUT_DIR/skiko-wine-smoke"

cat > "$OUT_DIR/native-library-smoke.kt" <<'KOTLIN'
import org.jetbrains.skiko.loadAngleLibrary
import org.jetbrains.skiko.loadOpenGLLibrary

fun main() {
    loadOpenGLLibrary()
    println("opengl=loaded")
    try {
        loadAngleLibrary()
        println("angle=loaded")
    } catch (error: Throwable) {
        println("angle=unavailable:${error.message}")
    }
}
KOTLIN

"$KONANC" \
    -target mingw_x64 \
    -produce program \
    -friend-modules "$SKIKO_KLIB" \
    -library "$SKIKO_KLIB" \
    -library "$ATOMICFU" \
    -library "$ATOMICFU_CINTEROP" \
    -library "$COROUTINES" \
    "$OUT_DIR/native-library-smoke.kt" \
    -o "$OUT_DIR/native-library-smoke"

WINEDEBUG_VALUE=${WINEDEBUG:--all}
WINEDEBUG="$WINEDEBUG_VALUE" wine "$OUT_DIR/skiko-wine-smoke.exe"
WINEDEBUG="$WINEDEBUG_VALUE" wine "$OUT_DIR/native-library-smoke.exe"
