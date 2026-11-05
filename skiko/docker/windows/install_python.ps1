$ErrorActionPreference = 'Stop'

$url = ('https://www.python.org/ftp/python/{0}/python-{1}-amd64.exe' -f $env:PYTHON_RELEASE, $env:PYTHON_VERSION)
$installer='C:\TEMP\python.exe'
Write-Host ('Downloading {0} ...' -f $url)
(New-Object System.Net.WebClient).DownloadFile($url, $installer)
Write-Host 'Installing ...'
$process = Start-Process $installer -Wait -PassThru -ArgumentList @('/quiet', 'TargetDir=C:\Python', 'InstallAllUsers=1', 'PrependPath=1')
if ($process.ExitCode -notin @(0, 3010)) {
    throw "Python installation failed with exit code $($process.ExitCode)"
}
Remove-Item $installer -Force
Write-Host 'Python installation is complete.'
