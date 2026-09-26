# Package the local JDK and painted application icon into a native Windows launcher.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (Test-Path -LiteralPath (Join-Path $projectRoot '.toolchain/jdk-17/bin/jpackage.exe')) {
    . (Join-Path $PSScriptRoot 'env.ps1')
}
if (-not $env:JAVA_HOME) { throw 'JAVA_HOME must point to JDK 17 with jpackage.' }
$jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'desktop/build/libs') -Filter 'desktop-*.jar' |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) { throw 'Build desktop:dist first.' }
$version = $jar.BaseName.Substring('desktop-'.Length)
$destination = Join-Path $projectRoot "desktop/build/windows/$version"
$application = Join-Path $destination 'Grimhollow'
if (Test-Path -LiteralPath $application) {
    $resolved = [IO.Path]::GetFullPath($application)
    $allowed = [IO.Path]::GetFullPath((Join-Path $projectRoot 'desktop/build/windows')) + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid packaging directory.' }
    Remove-Item -LiteralPath $resolved -Recurse -Force
}
$inputDir = Join-Path $destination 'input'
New-Item -ItemType Directory -Force -Path $inputDir | Out-Null
Copy-Item -LiteralPath $jar.FullName -Destination $inputDir
& "$env:JAVA_HOME/bin/jpackage.exe" --type app-image --name Grimhollow --app-version $version `
    --input $inputDir --main-jar $jar.Name --dest $destination --vendor Grimhollow `
    --icon (Join-Path $projectRoot 'desktop/src/main/assets/icons/windows.ico')
if ($LASTEXITCODE -ne 0) { throw "jpackage failed: $LASTEXITCODE" }
Write-Output (Join-Path $application 'Grimhollow.exe')
