# Skiko Benchmarks

This project contains lightweight multiplatform benchmarks for comparing Skiko artifacts.
The same benchmark definitions are used by JVM, JS, and wasmJs entry points; wasmJs is the
primary target.

Current benchmark coverage:

```text
surface_allocation      raster surface creation and teardown
rect_grid_draw          many small filled rect draws
path_parse_and_draw     SVG path parsing and stroked path rendering
image_snapshot_encode   image snapshot and PNG encoding
image_scale_draw        scaled image draws with sampling
clip_transform_draw     save/restore, clip, transform, and rounded-rect drawing
path_boolean_ops        path union/intersection operations
surface_read_pixels     raster readback into Bitmap
text_blob_draw          positioned glyph blob drawing
```

## Run Against a Maven Artifact

```bash
./benchmarks/SkikoBenchmarks/run_benchmarks.main.kts web version=<skiko-version>
```

This follows the Compose benchmark flow: Gradle starts a local result server, starts the wasm
browser benchmark app, waits for the browser to POST the completed report, and archives the result to:

```text
benchmarks/SkikoBenchmarks/build/benchmarks/archive/web/<skiko-version>_run1/skiko-wasm.json
```

Use any resolvable Skiko Maven version. The version is required; the benchmark
project intentionally has no default Skiko artifact version.

You can also run the Gradle convenience task:

```bash
./gradlew -p benchmarks/SkikoBenchmarks runBrowserAndSaveStats -Pskiko.version=<skiko-version>
```

JVM can be run with:

```bash
./benchmarks/SkikoBenchmarks/run_benchmarks.main.kts jvm version=<skiko-version>
```

The JVM task prints the same JSON report directly to the terminal.

Additional arguments:

```bash
./benchmarks/SkikoBenchmarks/run_benchmarks.main.kts web version=<skiko-version> runs=3 benchmarks=rect_grid_draw,path_parse_and_draw
```

Each run prints a compact table to stdout and archives one JSON file per benchmark.

## Benchmark Modes

Skiko benchmarks support the same `modes=` argument shape as Compose, with modes
that make sense for synchronous Skia operations:

```bash
./benchmarks/SkikoBenchmarks/run_benchmarks.main.kts web \
    version=<skiko-version> \
    modes=SIMPLE,STARTUP
```

Supported modes:

```text
SIMPLE            warmed repeated operation timing; default mode
STARTUP           first operation timing with no warmup
```

## Compare Artifacts

```bash
./benchmarks/SkikoBenchmarks/compare_benchmarks.main.kts \
    v1=<old-skiko-version> \
    v2=<new-skiko-version> \
    platform=web \
    runs=3 \
    modes=SIMPLE \
    metric=average
```

If `v2` is omitted, the comparison target is the current checkout using the
composite build path:

```bash
./benchmarks/SkikoBenchmarks/compare_benchmarks.main.kts \
    v1=<published-skiko-version> \
    platform=web \
    runs=3 \
    modes=SIMPLE,STARTUP
```

The comparison script runs missing benchmark data, reads archived JSON reports, and prints a
stdout table with the selected timing metric, percentage difference, and status.
`metric=average` is the default. Use `metric=median` to compare by `medianMillis`.

To reuse existing archives:

```bash
./benchmarks/SkikoBenchmarks/compare_benchmarks.main.kts \
    v1=<old-skiko-version> \
    v2=<new-skiko-version> \
    platform=web \
    runs=3 \
    metric=median \
    skipExisting=true
```

To search for a degradation across versions:

```bash
./benchmarks/SkikoBenchmarks/find_degradation.main.kts \
    benchmarks=rect_grid_draw \
    versions=versions.txt \
    platform=web
```

## Run Against the Current Checkout

```bash
./benchmarks/SkikoBenchmarks/run_benchmarks.main.kts web \
    version=<version-label> \
    composite=true
```

The included-build path follows the web samples: `org.jetbrains.skiko:skiko` is substituted
with the local `skiko` project, and the wasm runtime is copied from the included build's
`skikoWasmJar` output.

JVM can also run against the current checkout:

```bash
./benchmarks/SkikoBenchmarks/run_benchmarks.main.kts jvm \
    version=<version-label> \
    composite=true
```
