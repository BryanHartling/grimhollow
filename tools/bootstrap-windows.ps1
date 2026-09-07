# SPDX-License-Identifier: GPL-3.0-or-later
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$toolRoot = Join-Path $repoRoot '.toolchain'
New-Item -ItemType Directory -Force $toolRoot | Out-Null
$jdkRoot = Join-Path $toolRoot 'jdk-17'
if (!(Test-Path "$jdkRoot/bin/java.exe")) {
    $packages = Invoke-RestMethod 'https://api.adoptium.net/v3/assets/latest/17/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'
    $package = $packages[0].binary.package
    $archive = Join-Path $toolRoot 'temurin17.zip'
    Invoke-WebRequest $package.link -OutFile $archive
    if ((Get-FileHash $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $package.checksum) { throw 'JDK SHA256 mismatch' }
    Expand-Archive -LiteralPath $archive -DestinationPath "$toolRoot/jdk-extract" -Force
    $extractedJdk = Get-ChildItem "$toolRoot/jdk-extract" -Directory | Select-Object -First 1
    Copy-Item -LiteralPath $extractedJdk.FullName -Destination $jdkRoot -Recurse
}
$env:JAVA_HOME = $jdkRoot
$env:ANDROID_HOME = Join-Path $toolRoot 'android-sdk'
$env:GRADLE_USER_HOME = Join-Path $repoRoot '.gradle-user-home'
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
& "$env:JAVA_HOME/bin/java.exe" -version
if (!(Test-Path "$env:ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager.bat")) {
    $index = [xml](Invoke-WebRequest 'https://dl.google.com/android/repository/repository2-1.xml').Content
    $sdkPackage = $index.sdkrepository.remotePackage | Where-Object { $_.path -eq 'cmdline-tools;latest' }
    $sdkArchive = $sdkPackage.archives.archive | Where-Object { $_.'host-os' -eq 'windows' }
    $sdkZip = Join-Path $toolRoot 'android-commandline.zip'
    Invoke-WebRequest ("https://dl.google.com/android/repository/" + $sdkArchive.complete.url) -OutFile $sdkZip
    if ((Get-FileHash $sdkZip -Algorithm SHA1).Hash.ToLowerInvariant() -ne $sdkArchive.complete.checksum.'#text') { throw 'Android tools SHA1 mismatch' }
    Expand-Archive -LiteralPath $sdkZip -DestinationPath "$toolRoot/android-extract" -Force
    New-Item -ItemType Directory -Force "$env:ANDROID_HOME/cmdline-tools" | Out-Null
    Copy-Item -LiteralPath "$toolRoot/android-extract/cmdline-tools" -Destination "$env:ANDROID_HOME/cmdline-tools/latest" -Recurse
}
1..100 | ForEach-Object { 'y' } | & "$env:ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager.bat" --licenses
if ($LASTEXITCODE -ne 0) { throw 'SDK license acceptance failed' }
& "$env:ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager.bat" 'platforms;android-36' 'build-tools;36.0.0' 'platform-tools'
if ($LASTEXITCODE -ne 0) { throw 'SDK installation failed' }
Write-Output "JAVA_HOME=$env:JAVA_HOME"
Write-Output "ANDROID_HOME=$env:ANDROID_HOME"
