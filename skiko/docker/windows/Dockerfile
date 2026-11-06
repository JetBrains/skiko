# escape=`

# Use the latest Windows Server Core 2022 image.
FROM mcr.microsoft.com/windows/servercore:ltsc2022

# Restore the default Windows shell for correct batch processing.
SHELL ["cmd", "/S", "/C"]

# Download BuildTools installer
ADD https://aka.ms/vs/17/release/vs_buildtools.exe C:\Temp\vs_buildtools.exe

# Download channel for fixed install.
ADD https://aka.ms/vs/17/release/channel C:\Temp\VisualStudio.chman

# Install MSVC C++ compiler, CMake, and MSBuild.
COPY install_buildtools.ps1 C:\Temp\install_buildtools.ps1
RUN powershell -NoProfile -ExecutionPolicy Bypass -File C:\Temp\install_buildtools.ps1

RUN setx /M SKIKO_VSBT_PATH "C:\BuildTools"

ADD https://github.com/llvm/llvm-project/releases/download/llvmorg-20.1.7/LLVM-20.1.7-win64.exe C:\Temp\llvm.exe
RUN powershell Start-Process -Wait C:\Temp\llvm.exe -ArgumentList /S
RUN setx /M PATH "C:\Program Files\LLVM\bin;%PATH%"

# Install Java
COPY install_jdk.ps1 C:\TEMP\install_jdk.ps1
RUN powershell C:\TEMP\install_jdk.ps1 -url https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip -targetDir C:\jdk-21
RUN setx /M PATH "C:\jdk-21\bin;%PATH%"
RUN setx /M JAVA_HOME C:\jdk-21

ENV PYTHON_VERSION=3.15.2
ENV PYTHON_RELEASE=3.15.2
ADD install_python.ps1 C:\TEMP\install_python.ps1
RUN powershell C:\TEMP\install_python.ps1

COPY install_git.ps1 C:\TEMP\install_git.ps1
RUN powershell C:\TEMP\install_git.ps1
RUN setx /M PATH "C:\Git\cmd;C:\Git\bin;C:\Git\usr\bin;%PATH%"

RUN git.exe clone "https://chromium.googlesource.com/chromium/tools/depot_tools.git" "C:\depot_tools"
RUN setx /M PATH "C:\depot_tools;%PATH%"

# Define the entry point for the docker container.
# This entry point starts the developer command prompt and launches the PowerShell shell.
ENTRYPOINT ["C:\\BuildTools\\Common7\\Tools\\VsDevCmd.bat", "&&", "powershell.exe", "-NoLogo", "-ExecutionPolicy", "Bypass"]
