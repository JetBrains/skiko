#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../../.." && pwd)"
SAMPLE_DIR="$ROOT_DIR/samples/SkikoExtensionsSample"
PLAYWRIGHT_BIN="${PLAYWRIGHT_BIN:-playwright}"

run_web_sample() {
  local task="$1"
  WEB_SERVER_COMMAND="$ROOT_DIR/gradlew -p $SAMPLE_DIR --no-daemon --stacktrace $task" \
    "$PLAYWRIGHT_BIN" test --config "$SAMPLE_DIR/scripts/playwright.config.js"
}

run_web_sample :jsBrowserDevelopmentRun
run_web_sample :wasmJsBrowserDevelopmentRun
