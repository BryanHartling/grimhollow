# SPDX-License-Identifier: GPL-3.0-or-later
param([ValidateSet('Push','Status')][string]$Mode='Status')
$ErrorActionPreference='Stop'
if (!$env:GRIMHOLLOW_TOKEN) { throw 'Set GRIMHOLLOW_TOKEN in the process environment.' }
$repoRoot=Split-Path -Parent $PSScriptRoot
$gitExe='C:\Program Files\Git\cmd\git.exe'
if (!(Test-Path $gitExe)) { $gitExe='git' }
if ($Mode -eq 'Push') {
    try {
        $env:GIT_CONFIG_COUNT='1'
        $env:GIT_CONFIG_KEY_0='http.https://github.com/.extraheader'
        $env:GIT_CONFIG_VALUE_0='Authorization: Basic '+[Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('x-access-token:'+$env:GRIMHOLLOW_TOKEN))
        & $gitExe -C $repoRoot push -u origin grimhollow
        if ($LASTEXITCODE -ne 0) { throw 'GitHub push failed (see sanitized Git output above).' }
    } finally {
        Remove-Item Env:GIT_CONFIG_COUNT,Env:GIT_CONFIG_KEY_0,Env:GIT_CONFIG_VALUE_0 -ErrorAction SilentlyContinue
    }
}
$headers=@{Authorization='Bearer '+$env:GRIMHOLLOW_TOKEN; Accept='application/vnd.github+json'; 'X-GitHub-Api-Version'='2022-11-28'}
$result=Invoke-RestMethod -Headers $headers -Uri 'https://api.github.com/repos/bryanhartling/grimhollow/actions/runs?branch=grimhollow&per_page=3'
foreach($run in $result.workflow_runs) {
    $run | Select-Object id,head_sha,status,conclusion,html_url
    $jobs=Invoke-RestMethod -Headers $headers -Uri $run.jobs_url
    $jobs.jobs | Select-Object name,status,conclusion,html_url
}
