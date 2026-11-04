# escape=`

FROM mcr.microsoft.com/windows/servercore:ltsc2022

SHELL ["cmd", "/S", "/C"]

RUN if not exist C:\Temp mkdir C:\Temp

# Refresh Windows distrust information before downloading installers.
RUN powershell -NoProfile -Command `
    "$ErrorActionPreference = 'Stop'; `
    New-Item -ItemType Directory -Force C:\Temp\ctl | Out-Null; `
    & certutil.exe -syncWithWU C:\Temp\ctl; `
    if ($LASTEXITCODE -ne 0) { throw 'Certificate-list download failed' }; `
    & certutil.exe -addstore -f Disallowed C:\Temp\ctl\disallowedcert.sst; `
    if ($LASTEXITCODE -ne 0) { throw 'Disallowed-certificate import failed' }"

# Extract only CA certificate data from a fixed certifi wheel.
# The Python 3 package itself is NOT installed.
ADD https://files.pythonhosted.org/packages/c4/51/29046e48f693c54e5b7412216d447a1d8362c9653148d98c02cdf65fa733/certifi-2026.10.19-py3-none-any.whl C:\Temp\certifi.zip

RUN powershell -NoProfile -Command `
    "$ErrorActionPreference = 'Stop'; `
    $expected = '42d35340cc5b9ba78e818ddbff0c5b1c2b779de448a340fd1b989f5fa8670e1b'; `
    $actual = (Get-FileHash C:\Temp\certifi.zip -Algorithm SHA256).Hash; `
    if ($actual -ne $expected) { throw 'certifi checksum mismatch' }; `
    Expand-Archive C:\Temp\certifi.zip C:\Temp\certifi; `
    New-Item -ItemType Directory -Force C:\certs | Out-Null; `
    Copy-Item C:\Temp\certifi\certifi\cacert.pem C:\certs\cacert.pem"

# Shared trust bundle for clients that support these settings.
# Schannel clients use the Windows trust store instead.
ENV CURL_CA_BUNDLE=C:\certs\cacert.pem
ENV SSL_CERT_FILE=C:\certs\cacert.pem
ENV REQUESTS_CA_BUNDLE=C:\certs\cacert.pem
ENV PIP_CERT=C:\certs\cacert.pem
ENV CMAKE_TLS_CAINFO=C:\certs\cacert.pem
ENV CMAKE_TLS_VERIFY=1

# Fixed Visual Studio Build Tools 2022 release: 17.14.44.
# Do not override this with the moving release-channel manifest.
ADD https://download.visualstudio.microsoft.com/download/pr/6bd63d7f-1818-4e03-a552-9feb11496c19/5dc747350fb0589f908915c7b5fa52ec22d22b37ae2e5f4964d83519667498b2/vs_BuildTools.exe C:\Temp\vs_buildtools.exe

RUN start /w C:\Temp\vs_buildtools.exe `
    --quiet --wait --norestart --nocache `
    --installPath C:\BuildTools `
    --add Microsoft.VisualStudio.Workload.VCTools `
    --includeRecommended `
    --add Microsoft.VisualStudio.Component.VC.Tools.x86.x64 `
    --add Microsoft.VisualStudio.Component.VC.Tools.ARM64 `
    --add Microsoft.VisualStudio.Component.VC.CMake.Project `
    --add Microsoft.Component.MSBuild `
    --remove Microsoft.VisualStudio.Component.Windows10SDK.10240 `
    --remove Microsoft.VisualStudio.Component.Windows10SDK.10586 `
    --remove Microsoft.VisualStudio.Component.Windows10SDK.14393 `
    --remove Microsoft.VisualStudio.Component.Windows81SDK `
    || IF "%ERRORLEVEL%"=="3010" EXIT /B 0

ENV SKIKO_VSBT_PATH=C:\BuildTools

# LLVM.
ADD https://github.com/llvm/llvm-project/releases/download/llvmorg-23.1.5/LLVM-23.1.5-win64.exe C:\Temp\llvm.exe

RUN powershell -NoProfile -Command `
    "$p = Start-Process C:\Temp\llvm.exe -ArgumentList '/S' -Wait -PassThru; `
    exit $p.ExitCode"

# Microsoft OpenJDK 21 security update.
COPY install_jdk.ps1 C:\Temp\install_jdk.ps1

RUN powershell -NoProfile -ExecutionPolicy Bypass `
    -File C:\Temp\install_jdk.ps1 `
    -url https://aka.ms/download-jdk/microsoft-jdk-21.0.13.0.2-windows-x64.zip `
    -targetDir C:\jdk-21

ENV JAVA_HOME=C:\jdk-21
ENV PATH="C:\jdk-21\bin;C:\Program Files\LLVM\bin;${PATH}"

# Retain Python 2 for existing build scripts.
ENV PYTHON_VERSION=2.7.18
ENV PYTHON_RELEASE=2.7.18

COPY install_python.ps1 C:\Temp\install_python.ps1

RUN powershell -NoProfile -ExecutionPolicy Bypass `
    -File C:\Temp\install_python.ps1

# Locate the Python installed by install_python.ps1.
# Replace existing loose CA bundles, including vendored pip/Requests copies.
# Do not upgrade pip or certifi to Python-3-only versions.
RUN powershell -NoProfile -Command `
    "$ErrorActionPreference = 'Stop'; `
    $python = (Get-Command python.exe -CommandType Application).Source; `
    $version = & $python -c 'import platform; print(platform.python_version())'; `
    if ($LASTEXITCODE -ne 0 -or $version.Trim() -ne '2.7.18') { `
      throw 'Expected Python 2.7.18 on PATH after install_python.ps1' `
    }; `
    $prefix = & $python -c 'import sys; print(sys.prefix)'; `
    if ($LASTEXITCODE -ne 0) { throw 'Cannot locate Python installation' }; `
    Get-ChildItem -LiteralPath $prefix.Trim() -Recurse -File -Filter cacert.pem | `
      ForEach-Object { `
        Write-Host ('Updating CA bundle: ' + $_.FullName); `
        Copy-Item C:\certs\cacert.pem $_.FullName -Force `
      }"

# Git for Windows, including bundled curl.
ADD https://github.com/git-for-windows/git/releases/download/v2.56.0.windows.4/Git-2.56.0.4-64-bit.exe C:\Temp\git.exe

RUN powershell -NoProfile -Command `
    "$p = Start-Process C:\Temp\git.exe `
      -ArgumentList '/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /SP- /DIR=C:\Git' `
      -Wait -PassThru; `
    exit $p.ExitCode"

ENV PATH="C:\Git\cmd;C:\Git\bin;C:\Git\usr\bin;${PATH}"

# Git uses Windows certificate trust and distrust information.
RUN C:\Git\cmd\git.exe config --system http.sslBackend schannel
RUN C:\Git\cmd\git.exe config --system http.sslVerify true

# Fetch and initialize depot_tools.
RUN git.exe clone `
    https://chromium.googlesource.com/chromium/tools/depot_tools.git `
    C:\depot_tools

ENV PATH="C:\depot_tools;${PATH}"

RUN C:\depot_tools\update_depot_tools.bat

# Refresh loose CA bundles downloaded by depot_tools bootstrap.
RUN powershell -NoProfile -Command `
    "$ErrorActionPreference = 'Stop'; `
    Get-ChildItem C:\depot_tools -Recurse -File -Filter cacert.pem | `
      ForEach-Object { `
        Write-Host ('Updating CA bundle: ' + $_.FullName); `
        Copy-Item C:\certs\cacert.pem $_.FullName -Force `
      }"

# Automatically check Windows curl and common Git curl locations.
# Normal HTTPS must work; compromised chains must fail verification.
RUN powershell -NoProfile -Command `
    "$ErrorActionPreference = 'Stop'; `
    $candidates = @( `
      'C:\Windows\System32\curl.exe', `
      'C:\Git\mingw64\bin\curl.exe', `
      'C:\Git\usr\bin\curl.exe' `
    ) + @(& where.exe curl.exe); `
    $curls = @($candidates | `
      Where-Object { Test-Path -LiteralPath $_ } | `
      Select-Object -Unique); `
    if (!$curls.Count) { throw 'No curl executable found' }; `
    $badHosts = @( `
      'dv-g3b-emudhra.we-have-a-problem.com', `
      'dv-g3b-aaa-emudhra.we-have-a-problem.com' `
    ); `
    foreach ($exe in $curls) { `
      Write-Host ('Checking ' + $exe); `
      & $exe -q --fail --silent --show-error `
        --connect-timeout 10 --max-time 30 `
        -o NUL https://www.python.org/; `
      if ($LASTEXITCODE -ne 0) { `
        throw ('Normal HTTPS failed: ' + $exe) `
      }; `
      foreach ($hostName in $badHosts) { `
        & $exe -q --silent --show-error `
          --connect-timeout 10 --max-time 30 `
          -o NUL ('https://' + $hostName + '/'); `
        $code = $LASTEXITCODE; `
        if ($code -ne 60) { `
          throw ('Expected certificate rejection: ' + `
            $exe + ' / ' + $hostName + '; exit=' + $code) `
        } `
      } `
    }"

RUN powershell -NoProfile -Command `
    "Remove-Item C:\Temp\* -Recurse -Force"

ENTRYPOINT ["C:\\BuildTools\\Common7\\Tools\\VsDevCmd.bat", "&&", "powershell.exe", "-NoLogo", "-ExecutionPolicy", "Bypass"]
