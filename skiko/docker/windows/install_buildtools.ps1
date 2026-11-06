$ErrorActionPreference = 'Stop'

$arguments = @(
    '--quiet', '--wait', '--norestart', '--nocache',
    '--installPath', 'C:\BuildTools',
    '--channelUri', 'C:\Temp\VisualStudio.chman',
    '--installChannelUri', 'C:\Temp\VisualStudio.chman',
    '--add', 'Microsoft.VisualStudio.Workload.VCTools;includeRecommended',
    '--add', 'Microsoft.VisualStudio.Component.VC.Tools.x86.x64',
    '--add', 'Microsoft.VisualStudio.Component.VC.Tools.ARM64',
    '--add', 'Microsoft.Component.MSBuild',
    '--remove', 'Microsoft.VisualStudio.Component.Windows10SDK.10240',
    '--remove', 'Microsoft.VisualStudio.Component.Windows10SDK.10586',
    '--remove', 'Microsoft.VisualStudio.Component.Windows10SDK.14393',
    '--remove', 'Microsoft.VisualStudio.Component.Windows81SDK'
)

$process = Start-Process 'C:\Temp\vs_buildtools.exe' -Wait -PassThru -ArgumentList $arguments
if ($process.ExitCode -notin @(0, 3010)) {
    throw "Visual Studio Build Tools installation failed with exit code $($process.ExitCode)"
}

foreach ($path in @(
    'C:\BuildTools\Common7\Tools\VsDevCmd.bat',
    'C:\BuildTools\VC\Auxiliary\Build\vcvarsall.bat'
)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Visual Studio Build Tools installation is missing $path"
    }
}
