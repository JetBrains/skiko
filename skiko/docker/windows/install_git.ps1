# Install 7zip
Write-Host 'Downloading 7z...'
$7zinstaller='C:\TEMP\7zsetup.exe'
(New-Object System.Net.WebClient).DownloadFile('https://github.com/ip7z/7zip/releases/download/26.05/7z2605-x64.exe', $7zinstaller)
Write-Host 'Installing 7z...'
Start-Process $7zinstaller -ArgumentList '/S' -Wait
Remove-Item $7zinstaller -Force
$env:Path += ';C:\Program Files\7-Zip\'

# Install Git
Write-Host 'Downloading Git...'
$gitarchive='C:\TEMP\portableGit.7z.exe'
(New-Object System.Net.WebClient).DownloadFile('https://github.com/git-for-windows/git/releases/download/v2.56.0.windows.4/PortableGit-2.56.0.4-64-bit.7z.exe', $gitarchive)
Write-Host 'Installing Git...'
7z.exe x $gitarchive -o'C:\Git'
Remove-Item $gitarchive -Force
