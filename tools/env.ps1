# Dot-source from PowerShell: . ./tools/env.ps1
$repoRoot = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = Join-Path $repoRoot '.toolchain/jdk-17'
$env:ANDROID_HOME = Join-Path $repoRoot '.toolchain/android-sdk'
$env:GRADLE_USER_HOME = Join-Path $repoRoot '.gradle-user-home'
$env:Path = "$env:JAVA_HOME/bin;$env:ANDROID_HOME/platform-tools;$env:Path"
