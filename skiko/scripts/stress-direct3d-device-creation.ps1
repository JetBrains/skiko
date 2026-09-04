[CmdletBinding()]
param(
    [ValidateRange(1, 100000)]
    [int]$Repetitions = 100,
    [ValidateRange(100, 600000)]
    [int]$MaxDurationMs = 10000,
    [ValidateRange(1, 8192)]
    [int]$TextureSize = 256,
    [ValidateRange(0, 100)]
    [int]$MaxResidentWindows = 12,
    [ValidateRange(1, 16)]
    [int]$Parallelism = 1
)

$ErrorActionPreference = "Stop"

$skikoDirectory = Split-Path -Parent $PSScriptRoot
$gradleWrapper = Join-Path $skikoDirectory "gradlew.bat"
$testName = "org.jetbrains.skiko.swing.AcceleratedSwingPainterTest.stress Direct3D Swing device creation in lifecycle states"

if (-not (Test-Path $gradleWrapper)) {
    throw "Cannot find Gradle wrapper: $gradleWrapper"
}

if ($Parallelism -gt 1) {
    $repositoryRoot = (& git -C $skikoDirectory rev-parse --show-toplevel).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot locate the Git repository root. Run the script from a Git checkout."
    }
    $projectPath = (& git -C $skikoDirectory rev-parse --show-prefix).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot locate the Skiko project path in the Git checkout."
    }

    $workerCount = [Math]::Min($Parallelism, $Repetitions)
    $worktreeRoot = Join-Path ([System.IO.Path]::GetTempPath()) "skiko-direct3d-stress-$([guid]::NewGuid())"
    $worktrees = @()
    $processes = @()
    New-Item -ItemType Directory -Path $worktreeRoot | Out-Null

    try {
        for ($worker = 0; $worker -lt $workerCount; $worker++) {
            $workerRepetitions = [Math]::Floor($Repetitions / $workerCount)
            if ($worker -lt ($Repetitions % $workerCount)) {
                $workerRepetitions++
            }

            $worktree = Join-Path $worktreeRoot "worker-$worker"
            & git -C $repositoryRoot worktree add --detach $worktree HEAD
            if ($LASTEXITCODE -ne 0) {
                throw "Cannot create stress-test worktree $worktree"
            }
            $worktrees += $worktree

            $workerProject = if ($projectPath) { Join-Path $worktree $projectPath } else { $worktree }
            $workerScript = Join-Path $workerProject "scripts\stress-direct3d-device-creation.ps1"
            $workerArguments = @(
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-File", $workerScript,
                "-Repetitions", $workerRepetitions,
                "-MaxDurationMs", $MaxDurationMs,
                "-TextureSize", $TextureSize,
                "-MaxResidentWindows", $MaxResidentWindows
            )
            $processes += Start-Process -FilePath "powershell.exe" -ArgumentList $workerArguments -WorkingDirectory $workerProject -PassThru
        }

        $failedWorkers = @($processes | Wait-Process -PassThru | Where-Object { $_.ExitCode -ne 0 })
        if ($failedWorkers.Count -gt 0) {
            throw "$($failedWorkers.Count) Direct3D stress-test worker(s) failed."
        }
    } finally {
        foreach ($worktree in $worktrees) {
            & git -C $repositoryRoot worktree remove --force $worktree
        }
        Remove-Item -Recurse -Force $worktreeRoot
    }
    return
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
