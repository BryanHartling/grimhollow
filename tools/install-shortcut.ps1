$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$desktopDirectory = [Environment]::GetFolderPath('Desktop')
$launcher = Join-Path $projectRoot 'tools/play.bat'
$icon = Join-Path $projectRoot 'desktop/src/main/assets/icons/windows.ico'
if (!(Test-Path -LiteralPath $launcher) -or !(Test-Path -LiteralPath $icon)) {
    throw 'Build the Grimhollow launcher assets first.'
}
$shellObject = New-Object -ComObject WScript.Shell
$shortcutPath = Join-Path $desktopDirectory 'Grimhollow.lnk'
$shortcut = $shellObject.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $launcher
$shortcut.WorkingDirectory = $projectRoot
$shortcut.IconLocation = "$icon,0"
$shortcut.Description = 'Play Grimhollow'
$shortcut.WindowStyle = 7
$shortcut.Save()
Write-Output "Installed desktop launcher: $shortcutPath"
