# SPDX-License-Identifier: GPL-3.0-or-later
param(
    [Parameter(Mandatory)][string]$Target,
    [Parameter(Mandatory)][string]$Archive,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{64}$')][string]$ExpectedSHA256,
    [Parameter(Mandatory)][int]$VersionCode,
    [Parameter(Mandatory)][int]$ParentPid,
    [Parameter(Mandatory)][string]$StatusDirectory,
    [switch]$ValidateOnly
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$targetPath = [IO.Path]::GetFullPath($Target).TrimEnd('\')
$parentPath = [IO.Path]::GetDirectoryName($targetPath)
$archivePath = [IO.Path]::GetFullPath($Archive)
$statusPath = [IO.Path]::GetFullPath($StatusDirectory).TrimEnd('\')
$stagePath = Join-Path $parentPath ('Grimhollow.update-' + [guid]::NewGuid().ToString('N'))
$backupPath = $targetPath + '.previous-' + [guid]::NewGuid().ToString('N')
$healthPath = Join-Path $statusPath 'launch-health.txt'
$healthToken = [guid]::NewGuid().ToString('N')
$moved = $false
$candidate = $null

function Status([string]$phase, [string]$message) {
    @{phase=$phase;message=$message;versionCode=$VersionCode} | ConvertTo-Json -Compress |
        Set-Content -LiteralPath (Join-Path $statusPath 'install-status.json') -Encoding UTF8
}
function Checked-Sibling([string]$path) {
    $full = [IO.Path]::GetFullPath($path)
    if ([IO.Path]::GetDirectoryName($full) -ne $parentPath -or $full -eq $targetPath -or
        !([IO.Path]::GetFileName($full).StartsWith('Grimhollow.update-') -or $full.StartsWith($targetPath+'.previous-'))) {
        throw 'Unsafe installation workspace'
    }
    return $full
}
try {
    if (!$parentPath -or $targetPath -eq $parentPath -or [IO.Path]::GetFileName($targetPath) -ne 'Grimhollow') { throw 'Not a packaged Grimhollow application' }
    if (!(Test-Path -LiteralPath (Join-Path $targetPath 'Grimhollow.exe')) -or !(Test-Path -LiteralPath (Join-Path $targetPath 'update-package.json'))) { throw 'Incomplete current application' }
    if ($archivePath -ne (Join-Path $statusPath ('package-'+$VersionCode+'.zip'))) { throw 'Archive is outside the update cache' }
    if ([IO.File]::GetAttributes($targetPath) -band [IO.FileAttributes]::ReparsePoint) { throw 'Application junctions cannot be updated safely' }
    $hashStream = [IO.File]::OpenRead($archivePath)
    $hasher = [Security.Cryptography.SHA256]::Create()
    try { $actualHash = [BitConverter]::ToString($hasher.ComputeHash($hashStream)).Replace('-','').ToLowerInvariant() }
    finally { $hashStream.Dispose(); $hasher.Dispose() }
    if ($actualHash -ne $ExpectedSHA256) { throw 'Package checksum mismatch' }
    $current = Get-Content -LiteralPath (Join-Path $targetPath 'update-package.json') -Raw | ConvertFrom-Json
    if ($current.versionCode -ge $VersionCode) { throw 'Refusing a downgrade or reinstall' }
    $checkedStage = Checked-Sibling $stagePath
    New-Item -ItemType Directory -Path $checkedStage | Out-Null
    Status 'preparing' 'Validating update files; the current application is unchanged.'
    $zip = [IO.Compression.ZipFile]::OpenRead($archivePath)
    try {
        $seen = New-Object 'Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
        [long]$expanded = 0
        if ($zip.Entries.Count -gt 12000) { throw 'Too many update files' }
        foreach ($entry in $zip.Entries) {
            $relative = $entry.FullName.Replace('/', '\')
            if (!$relative.StartsWith('Grimhollow\') -or $relative.Contains(':') -or $relative.Contains('..') -or
                $relative.Contains([char]0) -or !$seen.Add($relative)) { throw 'Unsafe or duplicate ZIP entry' }
            foreach ($part in $relative.Split('\')) {
                if ($part -match '^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\..*)?$' -or $part.EndsWith('.') -or $part.EndsWith(' ')) { throw 'Unsafe Windows filename' }
            }
            $mode = ($entry.ExternalAttributes -shr 16) -band 0xF000
            if ($mode -eq 0xA000) { throw 'Archive symlinks are forbidden' }
            $expanded += $entry.Length
            if ($expanded -gt 2GB) { throw 'Expanded update exceeds the safety limit' }
            $destination = [IO.Path]::GetFullPath((Join-Path $checkedStage $relative))
            if (!$destination.StartsWith($checkedStage+'\', [StringComparison]::OrdinalIgnoreCase)) { throw 'ZIP entry leaves the checked workspace' }
            if ($entry.FullName.EndsWith('/')) {
                New-Item -ItemType Directory -Force -Path $destination | Out-Null
            } else {
                New-Item -ItemType Directory -Force -Path ([IO.Path]::GetDirectoryName($destination)) | Out-Null
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $destination, $false)
            }
        }
    } finally { $zip.Dispose() }
    $newApp = Join-Path $checkedStage 'Grimhollow'
    $new = Get-Content -LiteralPath (Join-Path $newApp 'update-package.json') -Raw | ConvertFrom-Json
    if ($new.schema -ne 1 -or $new.platform -ne 'windows-x64' -or $new.versionCode -ne $VersionCode -or
        !(Test-Path -LiteralPath (Join-Path $newApp 'Grimhollow.exe')) -or !(Test-Path -LiteralPath (Join-Path $newApp 'runtime/bin/server/jvm.dll'))) { throw 'Invalid extracted application' }
    if ($ValidateOnly) { Status 'validated' 'Package validated without replacing or launching the application.'; exit 0 }
    $old = Get-Process -Id $ParentPid -ErrorAction SilentlyContinue
    if ($old -and !$old.WaitForExit(90000)) { throw 'The game did not exit; installation cancelled' }
    $checkedBackup = Checked-Sibling $backupPath
    Move-Item -LiteralPath $targetPath -Destination $checkedBackup
    $moved = $true
    try { Move-Item -LiteralPath $newApp -Destination $targetPath }
    catch { Move-Item -LiteralPath $checkedBackup -Destination $targetPath; $moved=$false; throw }
    if (Test-Path -LiteralPath $healthPath) { Remove-Item -LiteralPath $healthPath }
    Status 'starting' 'Starting the new game; the previous application is retained.'
    $env:GRIMHOLLOW_UPDATE_HEALTH_FILE = $healthPath
    $env:GRIMHOLLOW_UPDATE_HEALTH_TOKEN = $healthToken
    $candidate = Start-Process -FilePath (Join-Path $targetPath 'Grimhollow.exe') -WorkingDirectory $targetPath -PassThru
    Remove-Item Env:GRIMHOLLOW_UPDATE_HEALTH_FILE,Env:GRIMHOLLOW_UPDATE_HEALTH_TOKEN
    $deadline = (Get-Date).AddSeconds(60)
    $healthy = $false
    while ((Get-Date) -lt $deadline) {
        if ((Test-Path -LiteralPath $healthPath) -and (Get-Content -LiteralPath $healthPath -Raw).Trim() -eq $healthToken) { $healthy=$true; break }
        if ($candidate.HasExited) { break }
        Start-Sleep -Milliseconds 250
    }
    if (!$healthy) { throw 'New game failed its startup check' }
    Status 'complete' 'Update installed. Previous application and save backup retained.'
} catch {
    $failure = $_.Exception.Message
    if ($moved) {
        if ($candidate -and !$candidate.HasExited) {
            $candidate.CloseMainWindow() | Out-Null
            if (!$candidate.WaitForExit(5000)) { $candidate.Kill(); $candidate.WaitForExit() }
        }
        # Every recursive operation is checked against this application workspace.
        if ([IO.Path]::GetFullPath($targetPath) -ne [IO.Path]::Combine($parentPath,'Grimhollow')) { throw 'Unsafe rollback target' }
        if (Test-Path -LiteralPath $targetPath) { Remove-Item -LiteralPath $targetPath -Recurse -Force }
        $checkedBackup = Checked-Sibling $backupPath
        Move-Item -LiteralPath $checkedBackup -Destination $targetPath
        Status 'rolled-back' ('Previous application restored: '+$failure)
        Start-Process -FilePath (Join-Path $targetPath 'Grimhollow.exe') -WorkingDirectory $targetPath
    } else { Status 'failed' ('Current application unchanged: '+$failure) }
    exit 1
} finally {
    Remove-Item Env:GRIMHOLLOW_UPDATE_HEALTH_FILE,Env:GRIMHOLLOW_UPDATE_HEALTH_TOKEN -ErrorAction SilentlyContinue
    $checkedStage = Checked-Sibling $stagePath
    if (Test-Path -LiteralPath $checkedStage) { Remove-Item -LiteralPath $checkedStage -Recurse -Force }
}
