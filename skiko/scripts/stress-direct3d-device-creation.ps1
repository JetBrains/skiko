[CmdletBinding()]
param(
    [ValidateRange(1, 100000)]
    [int]$Repetitions = 100,
    [ValidateRange(100, 600000)]
    [int]$MaxDurationMs = 10000,
    [ValidateRange(1, 8192)]
    [int]$TextureSize = 256,
    [ValidateRange(0, 100)]
    [int]$MaxResidentWindows = 12
)

$ErrorActionPreference = "Stop"

$skikoDirectory = Split-Path -Parent $PSScriptRoot
$gradleWrapper = Join-Path $skikoDirectory "gradlew.bat"
$testName = "org.jetbrains.skiko.swing.AcceleratedSwingPainterTest.stress Direct3D Swing device creation in lifecycle states"

if (-not (Test-Path $gradleWrapper)) {
    throw "Cannot find Gradle wrapper: $gradleWrapper"
}

Write-Host "SKIKO-1116 Direct3D first-device stress test"
Write-Host "Save work before continuing. While the test runs, you can trigger a display-driver reset with Win+Ctrl+Shift+B."
Write-Host "Also try sleep/resume, monitor reconnect, and RDP connect/disconnect. A display flicker is expected."

for ($attempt = 1; $attempt -le $Repetitions; $attempt++) {
    Write-Host "`n=== Fresh test JVM attempt $attempt of $Repetitions ==="
    $gradleArguments = @(
        ":awtTest",
        "--no-daemon",
        "--tests", $testName,
        "-Dskiko.test.direct3d.stress.enabled=true",
        "-Dskiko.test.direct3d.stress.iterations=1",
        "-Dskiko.test.direct3d.stress.maxDurationMs=$MaxDurationMs",
        "-Dskiko.test.direct3d.stress.textureSize=$TextureSize",
        "-Dskiko.test.direct3d.stress.maxResidentWindows=$MaxResidentWindows",
        "-Dskiko.test.direct3d.stress.runId=$attempt"
    )

    & $gradleWrapper @gradleArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Attempt $attempt failed with exit code $LASTEXITCODE"
    }
}
