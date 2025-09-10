param ($url, $targetDir)
$archiveFile="C:\TEMP\jdk.zip" 
Write-Host ('Downloading {0} ...' -f $url)		
(New-Object System.Net.WebClient).DownloadFile($url, $archiveFile)
Write-Host 'Installing ...'
tar -xf $archiveFile
# rename an unpacked directory like 'jdk-21.0.8+9' to '$targetDir'
$jdkDir=Get-ChildItem -Filter "jdk*"|Select-Object -First 1
Rename-Item -Path $jdkDir.FullName -NewName $targetDir
Remove-Item $archiveFile -Force 		
Write-Host 'Installation is complete.'
